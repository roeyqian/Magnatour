/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

// Java Standard
import java.util.LinkedHashMap;

// Minecraft
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/** Vanilla terrain splines with a reduced-octave density field on a 4x4x8 grid. */
public final class HarvestMelonTerrain {

  private static final int CACHE_LIMIT = 8;
  private static final int MAX_Y = 192;
  private static final int MIN_Y = 64;
  private static final int Y_STEP = 8;
  private static final int Y_NODES = (MAX_Y - MIN_Y) / Y_STEP + 1;

  // A worker owns its noise generators and bounded tile cache: no shared mutable
  // interpolation cursor, cross-world cache reuse, or generation-order dependency.
  private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

  private static final CubicSpline<Coordinate> FACTOR = TerrainProvider.overworldFactor(
      Coordinate.CONTINENTS, Coordinate.EROSION, Coordinate.RIDGES, Coordinate.FOLDED_RIDGES, false);
  private static final CubicSpline<Coordinate> JAGGEDNESS = TerrainProvider.overworldJaggedness(
      Coordinate.CONTINENTS, Coordinate.EROSION, Coordinate.RIDGES, Coordinate.FOLDED_RIDGES, false);
  private static final CubicSpline<Coordinate> OFFSET = TerrainProvider.overworldOffset(
      Coordinate.CONTINENTS, Coordinate.EROSION, Coordinate.FOLDED_RIDGES, false);

  private HarvestMelonTerrain() {}

  /** Precompute occupancy once; chunk filling and height queries use the same mask. */
  public static Column column(
      long seed,
      int x,
      int z,
      int surfaceY,
      double weight
  ) {
    if (weight <= 0.0) return null;
    Tile tile = tile(seed, x, z);
    int columnIndex = index(x, z);
    int shift = surfaceY - (int) Math.round(tile.heights[columnIndex]);
    int bottom = MIN_Y + shift;
    long lower = 0L;
    long upper = 0L;
    int base = columnIndex * Y_NODES;
    int limit = Math.min(MAX_Y - MIN_Y, surfaceY - bottom + 1);
    for (int offset = 0; offset < limit; offset++) {
      int y = bottom + offset;
      boolean solid = y >= surfaceY - 3;
      if (!solid) {
        int node = offset / Y_STEP;
        double density = Mth.lerp((offset % Y_STEP) / (double) Y_STEP,
            tile.densities[base + node], tile.densities[base + node + 1]);
        // Translate the density field with the blended surface. Fade its openings
        // into solid terrain towards wheat/pumpkin instead of cutting a vertical seam.
        solid = Mth.lerp(weight, (surfaceY - y) / 32.0, density) > 0.0;
      }
      if (solid) {
        if (offset < 64) lower |= 1L << offset;
        else upper |= 1L << (offset - 64);
      }
    }
    return new Column(bottom, lower, upper);
  }

  public static double height(
      long seed,
      int x,
      int z
  ) {
    return tile(seed, x, z).heights[index(x, z)];
  }

  private static Tile tile(
      long seed,
      int x,
      int z
  ) {
    Context context = CONTEXT.get();
    if (context == null || context.seed != seed) {
      context = new Context(seed);
      CONTEXT.set(context);
    }
    int chunkX = Math.floorDiv(x, 16);
    int chunkZ = Math.floorDiv(z, 16);
    long key = ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
    if (context.lastTile != null && context.lastKey == key) return context.lastTile;
    Tile tile = context.tiles.get(key);
    if (tile == null) {
      tile = new Tile(context, chunkX * 16, chunkZ * 16);
      if (context.tiles.size() >= CACHE_LIMIT) {
        context.tiles.remove(context.tiles.keySet().iterator().next());
      }
      context.tiles.put(key, tile);
    }
    context.lastKey = key;
    context.lastTile = tile;
    return tile;
  }

  private static int index(
      int x,
      int z
  ) {
    return Math.floorMod(x, 16) * 16 + Math.floorMod(z, 16);
  }

  private static double climate(
      long seed,
      int x,
      int z,
      double scale,
      int octaves
  ) {
    return HarvestContinentTerrain.fbmPerlin(seed, x, z, scale, octaves);
  }

  private static double smooth(
      double t
  ) {
    t = Mth.clamp(t, 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
  }

  public record Column(
      int bottom,
      long lower,
      long upper
  ) {

    public boolean solid(
        int y
    ) {
      int offset = y - this.bottom;
      if (offset < 0) return true;
      if (offset >= 128) return false;
      return offset < 64 ? (this.lower & (1L << offset)) != 0L
          : (this.upper & (1L << (offset - 64))) != 0L;
    }

  }

  private static final class Context {

    private Tile lastTile;

    private long lastKey;
    private final long seed;

    private final LinkedHashMap<Long, Tile> tiles = new LinkedHashMap<>(CACHE_LIMIT, 0.75F, true);

    private final PerlinNoise lower;
    private final PerlinNoise selector;
    private final PerlinNoise upper;

    private Context(
        long seed
    ) {
      this.seed = seed;
      this.lower = new PerlinNoise(new XoroshiroRandomSource(seed ^ 0x6A09E667F3BCC909L));
      this.upper = new PerlinNoise(new XoroshiroRandomSource(seed ^ 0xBB67AE8584CAA73BL));
      this.selector = new PerlinNoise(new XoroshiroRandomSource(seed ^ 0x3C6EF372FE94F82BL));
    }

  }

  private enum Coordinate implements BoundedFloatFunction<Point> {
    CONTINENTS, EROSION, RIDGES, FOLDED_RIDGES;

    @Override
    public float apply(Point point) {
      return switch (this) {
        case CONTINENTS -> point.continents();
        case EROSION -> point.erosion();
        case RIDGES -> point.ridges();
        case FOLDED_RIDGES -> TerrainProvider.peaksAndValleys(point.ridges());
      };
    }

    @Override
    public Interval range() {
      return Interval.of(-1.0F, 1.0F);
    }

  }

  private record Point(
      float continents,
      float erosion,
      float ridges
  ) {}

  private static final class Tile {

    private final float[] densities = new float[256 * Y_NODES];

    private final double[] heights = new double[256];

    private Tile(
        Context context,
        int minX,
        int minZ
    ) {
      float[] nodes = new float[25 * Y_NODES];
      for (int gx = 0; gx <= 4; gx++) {
        for (int gz = 0; gz <= 4; gz++) {
          int x = minX + gx * 4;
          int z = minZ + gz * 4;
          Point point = new Point(
              // Keep this continent inland; remap the narrower custom Perlin
              // signal so the original splines reach valleys and mountain branches.
              (float) Mth.clamp(0.35 + climate(context.seed ^ 0xD1B54A32D192ED03L, x, z, 0.00075, 4) * 1.5, -0.1, 1.0),
              (float) Mth.clamp(climate(context.seed ^ 0x94D049BB133111EBL, x, z, 0.00145, 3) * 2.0, -1.0, 1.0),
              (float) Mth.clamp(climate(context.seed ^ 0x2545F4914F6CDD1DL, x, z, 0.00320, 3) * 2.0, -1.0, 1.0));
          double offset = CubicSpline.sample(OFFSET, point);
          double factor = CubicSpline.sample(FACTOR, point);
          double jagged = CubicSpline.sample(JAGGEDNESS, point)
              * context.lower.get(x * 0.014, z * 0.014);
          if (jagged < 0.0) jagged *= 0.5;
          int base = (gx * 5 + gz) * Y_NODES;
          for (int gy = 0; gy < Y_NODES; gy++) {
            int y = MIN_Y + gy * Y_STEP;
            // Vanilla depth gradient + offset, raised ~32 blocks for Harvest.
            double depth = (96.0 - y) / 128.0 + offset + jagged;
            double shaped = depth * factor;
            if (shaped < 0.0) shaped *= 0.25;
            double blend = Mth.clamp(0.5 + context.selector.get(x * 0.008, y * 0.012, z * 0.008), 0.0, 1.0);
            double noise = Mth.lerp(blend,
                context.lower.get(x * 0.018, y * 0.022, z * 0.018),
                context.upper.get(x * 0.036, y * 0.044, z * 0.036));
            // Only the noise octave budget is reduced; offset/factor/jaggedness
            // use the game's real, unamplified Overworld splines.
            double density = shaped * 4.0 + noise * 1.5;
            density += 4.0 * (1.0 - smooth((y - MIN_Y) / 16.0));
            density -= 4.0 * smooth((y - 176.0) / 16.0);
            // Bracket every surface crossing even on an extreme spline branch.
            if (gy == 0) density = Math.max(density, 1.0);
            if (gy == Y_NODES - 1) density = Math.min(density, -1.0);
            nodes[base + gy] = (float) density;
          }
        }
      }
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          int base = (x * 16 + z) * Y_NODES;
          int corner = ((x / 4) * 5 + z / 4) * Y_NODES;
          double tx = (x % 4) / 4.0;
          double tz = (z % 4) / 4.0;
          for (int gy = 0; gy < Y_NODES; gy++) {
            this.densities[base + gy] = (float) Mth.lerp(tz,
                Mth.lerp(tx, nodes[corner + gy], nodes[corner + 5 * Y_NODES + gy]),
                Mth.lerp(tx, nodes[corner + Y_NODES + gy], nodes[corner + 6 * Y_NODES + gy]));
          }
          double height = 68.0;
          // Highest positive-to-negative crossing preserves overhangs below it.
          for (int gy = Y_NODES - 2; gy >= 0; gy--) {
            double below = this.densities[base + gy];
            double above = this.densities[base + gy + 1];
            if (below > 0.0) {
              height = MIN_Y + gy * Y_STEP + Y_STEP * Mth.clamp(below / (below - above), 0.0, 1.0);
              break;
            }
          }
          this.heights[x * 16 + z] = Mth.clamp(height, 68.0, 190.0);
        }
      }
    }

  }

}
