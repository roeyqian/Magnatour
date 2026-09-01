/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 */
package roeyqian.magnatour.levelgen;

// Java Standard
import java.util.Random;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

// Magnatour
import roeyqian.magnatour.Magnatour;

/**
 * Pure terrain functions for the Harvest Continent.
 *
 * <p>This class deliberately has no dependency on vanilla's noise chunk,
 * surface rules, carvers, or aquifer implementation.  The chunk generator is
 * the sole caller and uses these functions for both bulk generation and all
 * base-height queries.</p>
 */
public final class HarvestContinentTerrain {

  public static final int MAX_TERRAIN_SLOPE = 2;
  public static final int SEA_LEVEL = 64;
  public static final int SHORE_BLEND_DISTANCE = 128;

  public static final ResourceKey<Biome> BIG_LAKE = key("big_lake");
  public static final ResourceKey<Biome> LAKE_CENTER_ISLAND = key("lake_center_island");
  public static final ResourceKey<Biome> MELON_JUNGLE = key("melon_jungle");
  public static final ResourceKey<Biome> PUMPKIN_GORGE = key("pumpkin_gorge");
  public static final ResourceKey<Biome> WHEAT_PLAIN = key("wheat_plain");

  private static final int LAKE_CENTER_ISLAND_BASE_HEIGHT = 65;
  private static final int LAKE_CENTER_ISLAND_MAX_HEIGHT = 70;
  private static final int MELON_JUNGLE_BASE_HEIGHT = 96;
  private static final int MELON_JUNGLE_INTERIOR_MAX_HEIGHT = 190;
  private static final int MELON_JUNGLE_INTERIOR_MIN_HEIGHT = 68;
  private static final int PUMPKIN_GORGE_BASE_HEIGHT = 256;
  private static final int PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT = 250;
  private static final int PUMPKIN_GORGE_MAX_HEIGHT = 301;
  private static final int TREE_CLEARING_RADIUS = 2;
  private static final int TREE_GRID_SIZE = 32;
  private static final int WHEAT_BASE_HEIGHT = 128;
  private static final int WHEAT_INTERIOR_MAX_HEIGHT = 135;
  private static final int WHEAT_INTERIOR_MIN_HEIGHT = 123;

  private HarvestContinentTerrain() {}

  /**
   * Applies the explicit 128-block lake shore profile before the global slope
   * limiter. This is a height blend only: the owning biome and its material
   * profile never change.
   */
  public static int blendLakeShoreHeight(
      ResourceKey<Biome> biome,
      int rawHeight,
      int lakeDistance
  ) {
    if (biome.equals(BIG_LAKE) || biome.equals(LAKE_CENTER_ISLAND)
        || lakeDistance < 0 || lakeDistance > SHORE_BLEND_DISTANCE) {
      return rawHeight;
    }
    double t = lakeDistance / (double) SHORE_BLEND_DISTANCE;
    double smooth = t * t * (3.0 - 2.0 * t);
    return Math.round((float) (SEA_LEVEL + (rawHeight - SEA_LEVEL) * smooth));
  }

  public static double fbmPerlin(
      long seed,
      double x,
      double z,
      double scale,
      int octaves
  ) {
    double amplitude = 1.0;
    double frequency = scale;
    double sum = 0.0;
    double normalization = 0.0;
    for (int octave = 0; octave < octaves; octave++) {
      sum += amplitude * perlin2D(seed + octave * 1013L, x * frequency, z * frequency);
      normalization += amplitude;
      amplitude *= 0.5;
      frequency *= 2.0;
    }
    return normalization == 0.0 ? 0.0 : Mth.clamp(sum / normalization, -1.0, 1.0);
  }

  /** Returns the custom aquifer liquid for an already-carved cave block. */
  public static boolean isAquiferWater(
      long seed,
      int worldX,
      int y,
      int worldZ
  ) {
    if (y <= -54) return false;
    double region = fbmPerlin(seed ^ 0xA4093822299F31D0L, worldX, worldZ, 0.006, 2);
    int waterLevel = 36 + Math.round((float) (region * 13.0));
    return y <= waterLevel;
  }

  /** Custom cave mask; it is intentionally independent from vanilla carvers. */
  public static boolean isCave(
      long seed,
      int worldX,
      int y,
      int worldZ,
      int surfaceY
  ) {
    if (y <= -58 || y >= surfaceY - 7) return false;

    double winding = fbmValue3D(seed ^ 0x243F6A8885A308D3L, worldX, y, worldZ, 0.043, 3);
    double chambers = fbmValue3D(seed ^ 0x13198A2E03707344L, worldX, y, worldZ, 0.017, 2);
    double depthBias = Mth.clamp((surfaceY - y - 12) / 92.0, 0.0, 0.17);
    return winding + chambers * 0.38 > 0.53 - depthBias;
  }

  public static boolean isInTreeClearing(
      int worldX,
      int worldZ
  ) {
    int gridX = Math.floorDiv(worldX, TREE_GRID_SIZE);
    int gridZ = Math.floorDiv(worldZ, TREE_GRID_SIZE);
    for (int gx = gridX - 1; gx <= gridX + 1; gx++) {
      for (int gz = gridZ - 1; gz <= gridZ + 1; gz++) {
        if (isNearTreeInGrid(worldX, worldZ, gx, gz)) return true;
      }
    }
    return false;
  }

  /** A locally computed lake bed, used only in big-lake columns. */
  public static int lakeBedHeight(
      long seed,
      int worldX,
      int worldZ
  ) {
    double shape = fbmPerlin(seed ^ 0x67E6096A85AE67BBL, worldX, worldZ, 0.012, 3);
    return Mth.clamp(38 + Math.round((float) (shape * 9.0)), 26, 52);
  }

  /** Kept public because the Gold Bell Tower anchors itself to this terrain. */
  public static int lakeCenterIslandHeight(
      long seed,
      int worldX,
      int worldZ
  ) {
    double shape = fbmPerlin(seed ^ 0x6A09E667F3BCC909L, worldX, worldZ, 0.026, 2);
    int height = Math.round(LAKE_CENTER_ISLAND_BASE_HEIGHT + (float) (shape * 1.5));
    return Mth.clamp(height, LAKE_CENTER_ISLAND_BASE_HEIGHT, LAKE_CENTER_ISLAND_MAX_HEIGHT);
  }

  /**
   * Limits a sampled height field to two vertical blocks per horizontal block.
   * A 128-block halo is supplied by the generator, so a lake contributes its
   * full shore distance to every central chunk column without changing biome
   * ownership or borrowing another biome's raw profile.
   */
  public static void limitTerrainSlope(
      int[][] heights
  ) {
    int sizeX = heights.length;
    int sizeZ = heights[0].length;

    for (int z = 0; z < sizeZ; z++) {
      for (int x = 1; x < sizeX; x++) {
        heights[x][z] = Math.min(heights[x][z], heights[x - 1][z] + MAX_TERRAIN_SLOPE);
      }
      for (int x = sizeX - 2; x >= 0; x--) {
        heights[x][z] = Math.min(heights[x][z], heights[x + 1][z] + MAX_TERRAIN_SLOPE);
      }
    }

    for (int x = 0; x < sizeX; x++) {
      for (int z = 1; z < sizeZ; z++) {
        heights[x][z] = Math.min(heights[x][z], heights[x][z - 1] + MAX_TERRAIN_SLOPE);
      }
      for (int z = sizeZ - 2; z >= 0; z--) {
        heights[x][z] = Math.min(heights[x][z], heights[x][z + 1] + MAX_TERRAIN_SLOPE);
      }
    }
  }

  /** The unblended terrain profile owned by one biome at one column. */
  public static int rawSurfaceHeight(
      ResourceKey<Biome> biome,
      long seed,
      int worldX,
      int worldZ
  ) {
    if (biome.equals(BIG_LAKE)) return SEA_LEVEL;
    if (biome.equals(LAKE_CENTER_ISLAND)) return lakeCenterIslandHeight(seed, worldX, worldZ);
    if (biome.equals(MELON_JUNGLE)) return melonJungleHeight(seed, worldX, worldZ);
    if (biome.equals(PUMPKIN_GORGE)) return pumpkinGorgeHeight(seed, worldX, worldZ);
    return wheatPlainHeight(seed, worldX, worldZ);
  }

  /** Resolves the small fixed biome set used by this dimension. */
  public static ResourceKey<Biome> resolveBiome(
      Holder<Biome> biome
  ) {
    if (biome.is(BIG_LAKE)) return BIG_LAKE;
    if (biome.is(LAKE_CENTER_ISLAND)) return LAKE_CENTER_ISLAND;
    if (biome.is(MELON_JUNGLE)) return MELON_JUNGLE;
    if (biome.is(PUMPKIN_GORGE)) return PUMPKIN_GORGE;
    return WHEAT_PLAIN;
  }

  private static ResourceKey<Biome> key(
      String path
  ) {
    return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path));
  }

  private static double perlin2D(
      long seed,
      double x,
      double z
  ) {
    int x0 = fastFloor(x);
    int z0 = fastFloor(z);
    double tx = x - x0;
    double tz = z - z0;
    double u = fade(tx);
    double v = fade(tz);
    double n00 = gradientDot(seed, x0, z0, tx, tz);
    double n10 = gradientDot(seed, x0 + 1, z0, tx - 1.0, tz);
    double n01 = gradientDot(seed, x0, z0 + 1, tx, tz - 1.0);
    double n11 = gradientDot(seed, x0 + 1, z0 + 1, tx - 1.0, tz - 1.0);
    return lerp(v, lerp(u, n00, n10), lerp(u, n01, n11));
  }

  private static double fbmValue3D(
      long seed,
      double x,
      double y,
      double z,
      double scale,
      int octaves
  ) {
    double amplitude = 1.0;
    double frequency = scale;
    double sum = 0.0;
    double normalization = 0.0;
    for (int octave = 0; octave < octaves; octave++) {
      sum += amplitude * valueNoise3D(seed + octave * 2089L, x * frequency, y * frequency, z * frequency);
      normalization += amplitude;
      amplitude *= 0.5;
      frequency *= 2.0;
    }
    return sum / normalization;
  }

  private static boolean isNearTreeInGrid(
      int worldX,
      int worldZ,
      int gridX,
      int gridZ
  ) {
    Random random = new Random(gridX * 341873128712L + gridZ * 132897987541L);
    random.nextFloat();
    int treeX = gridX * TREE_GRID_SIZE + random.nextInt(TREE_GRID_SIZE);
    int treeZ = gridZ * TREE_GRID_SIZE + random.nextInt(TREE_GRID_SIZE);
    int dx = worldX - treeX;
    int dz = worldZ - treeZ;
    return dx * dx + dz * dz <= TREE_CLEARING_RADIUS * TREE_CLEARING_RADIUS;
  }

  private static int melonJungleHeight(
      long seed,
      int x,
      int z
  ) {
    // This mirrors the shape of the Overworld terrain model rather than a
    // plain fBm height: continentalness establishes large landforms,
    // erosion suppresses sharp terrain, and the same peaks-and-valleys
    // transform used by TerrainProvider selects ridges and valleys.
    double continentalness = fbmPerlin(seed ^ 0xD1B54A32D192ED03L, x, z, 0.00075, 4);
    double erosion = fbmPerlin(seed ^ 0x94D049BB133111EBL, x, z, 0.00145, 3);
    double weirdness = fbmPerlin(seed ^ 0x2545F4914F6CDD1DL, x, z, 0.00320, 3);
    double peaksAndValleys = vanillaPeaksAndValleys(weirdness);

    // Low erosion exposes strong ridges. High erosion returns to rounded,
    // jungle-sized hills, matching the role erosion has in vanilla splines.
    double erosionFactor = 1.0 - smoothStep((erosion + 1.0) * 0.5);
    double ridgeSignal = Mth.clamp((peaksAndValleys + 0.42) / 0.42, 0.0, 1.0);
    double continentalLift = continentalness * 23.0;
    double ridgeLift = ridgeSignal * (28.0 + erosionFactor * 48.0);
    double valleyCut = (1.0 - ridgeSignal) * (7.0 + (1.0 - erosionFactor) * 8.0);
    double jaggedness = ridgeSignal * erosionFactor
        * fbmPerlin(seed ^ 0x9E3779B97F4A7C15L, x, z, 0.014, 3) * 13.0;
    double surfaceDetail = fbmPerlin(seed ^ 0xBB67AE8584CAA73BL, x, z, 0.041, 2) * 3.5;

    int height = Math.round(MELON_JUNGLE_BASE_HEIGHT + (float) (
        continentalLift + ridgeLift - valleyCut + jaggedness + surfaceDetail
    ));
    return Mth.clamp(height,
        MELON_JUNGLE_INTERIOR_MIN_HEIGHT, MELON_JUNGLE_INTERIOR_MAX_HEIGHT);
  }

  private static int pumpkinGorgeHeight(
      long seed,
      int x,
      int z
  ) {
    double macro = Math.sin(x * 0.035) + Math.cos(z * 0.032);
    double ridges = Math.abs(Math.sin((x + z) * 0.08)) * 24.0;
    double spikes = Math.abs(Math.sin(x * 0.19) * Math.cos(z * 0.17)) * 14.0;
    double detail = (1.0 - Math.abs(fbmPerlin(seed ^ 0x3C6EF372FE94F82BL, x, z, 0.040, 4)));
    detail = detail * detail * 22.0 - 10.0;
    detail += fbmPerlin(seed ^ 0x510E527FADE682D1L, x, z, 0.085, 3) * 8.0;
    int height = Math.round(PUMPKIN_GORGE_BASE_HEIGHT + (float) (macro * 6.0 + ridges + spikes + detail));
    return Mth.clamp(height, PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT, PUMPKIN_GORGE_MAX_HEIGHT);
  }

  private static int wheatPlainHeight(
      long seed,
      int x,
      int z
  ) {
    double large = fbmPerlin(seed ^ 0x1A2B3C4D5E6F7890L, x, z, 0.0026, 3) * 5.2;
    double medium = fbmPerlin(seed ^ 0x9876543210FEDCBAL, x, z, 0.0100, 2) * 3.4;
    double micro = fbmPerlin(seed ^ 0xABCDEF0123456789L, x, z, 0.0340, 2) * 1.35;
    return Mth.clamp((int) Math.round(WHEAT_BASE_HEIGHT + large + medium + micro),
        WHEAT_INTERIOR_MIN_HEIGHT, WHEAT_INTERIOR_MAX_HEIGHT);
  }

  private static int fastFloor(
      double value
  ) {
    int integer = (int) value;
    return value < integer ? integer - 1 : integer;
  }

  private static double fade(
      double value
  ) {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
  }

  private static double gradientDot(
      long seed,
      int x,
      int z,
      double dx,
      double dz
  ) {
    return switch ((int) (mix(seed, x, z) & 7L)) {
      case 0 -> dx;
      case 1 -> -dx;
      case 2 -> dz;
      case 3 -> -dz;
      case 4 -> (dx + dz) * 0.7071067811865476;
      case 5 -> (-dx + dz) * 0.7071067811865476;
      case 6 -> (dx - dz) * 0.7071067811865476;
      default -> (-dx - dz) * 0.7071067811865476;
    };
  }

  private static double lerp(
      double delta,
      double start,
      double end
  ) {
    return start + delta * (end - start);
  }

  private static double valueNoise3D(
      long seed,
      double x,
      double y,
      double z
  ) {
    int x0 = fastFloor(x);
    int y0 = fastFloor(y);
    int z0 = fastFloor(z);
    double tx = fade(x - x0);
    double ty = fade(y - y0);
    double tz = fade(z - z0);
    double x00 = lerp(tx, value(seed, x0, y0, z0), value(seed, x0 + 1, y0, z0));
    double x10 = lerp(tx, value(seed, x0, y0 + 1, z0), value(seed, x0 + 1, y0 + 1, z0));
    double x01 = lerp(tx, value(seed, x0, y0, z0 + 1), value(seed, x0 + 1, y0, z0 + 1));
    double x11 = lerp(tx, value(seed, x0, y0 + 1, z0 + 1), value(seed, x0 + 1, y0 + 1, z0 + 1));
    return lerp(tz, lerp(ty, x00, x10), lerp(ty, x01, x11));
  }

  /** Exact scalar transform used by vanilla TerrainProvider.peaksAndValleys. */
  private static double vanillaPeaksAndValleys(
      double weirdness
  ) {
    return -(Math.abs(Math.abs(weirdness) - 2.0 / 3.0) - 1.0 / 3.0) * 3.0;
  }

  private static double smoothStep(
      double value
  ) {
    double clamped = Mth.clamp(value, 0.0, 1.0);
    return clamped * clamped * (3.0 - 2.0 * clamped);
  }

  private static long mix(
      long seed,
      int x,
      int z
  ) {
    long h = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
    h ^= h >>> 27;
    h *= 0x3C79AC492BA7B653L;
    h ^= h >>> 33;
    h *= 0x1C69B3F74AC4AE35L;
    return h ^ h >>> 27;
  }

  private static double value(
      long seed,
      int x,
      int y,
      int z
  ) {
    long h = mix(mix(seed, x, z), y, x ^ z);
    return ((h >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
  }

}
