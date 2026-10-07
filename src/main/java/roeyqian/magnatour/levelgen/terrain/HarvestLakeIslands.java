/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

// Java Standard
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Seeded islands around deep lake-basin minima, independently of biome intervals. */
public final class HarvestLakeIslands {

  public static final int SHORE_WIDTH = 32;

  private static final int CACHE_LIMIT = 2048;
  private static final int CENTER_GRID = 512;

  private static final double ISLAND_CHANCE = 0.55;

  private final long seed;

  private final ConcurrentHashMap<Long, Optional<Island>> centers = new ConcurrentHashMap<>();

  public HarvestLakeIslands(
      long seed
  ) {
    this.seed = seed;
  }

  /** The cache is bounded and stores immutable candidates, never generated chunks. */
  public Island at(
      int x,
      int z,
      double harvestour
  ) {
    if (harvestour >= HarvestContinentTerrain.LAKE_HARVESTOUR_LIMIT) return null;
    int gx = Math.floorDiv(x, CENTER_GRID);
    int gz = Math.floorDiv(z, CENTER_GRID);
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        int cellX = gx + dx;
        int cellZ = gz + dz;
        int centerX = cellX * CENTER_GRID + CENTER_GRID / 2;
        int centerZ = cellZ * CENTER_GRID + CENTER_GRID / 2;
        // Even the largest island and shore cannot reach farther than 205 blocks.
        if (Math.abs(x - centerX) > 205 || Math.abs(z - centerZ) > 205) continue;
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        if (this.centers.size() > CACHE_LIMIT) this.centers.clear();
        Island island = this.centers.computeIfAbsent(key,
            ignored -> Optional.ofNullable(createIsland(cellX, cellZ))).orElse(null);
        if (island != null && island.distance(x, z) <= island.radius() + SHORE_WIDTH) return island;
      }
    }
    return null;
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

  private static double unit(
      long value
  ) {
    return (value >>> 11) * 0x1.0p-53;
  }

  private Island createIsland(
      int gx,
      int gz
  ) {
    long choice = mix(this.seed ^ 0x6A09E667F3BCC909L, gx, gz);
    if (unit(choice) >= ISLAND_CHANCE) return null;
    int x = gx * CENTER_GRID + CENTER_GRID / 2;
    int z = gz * CENTER_GRID + CENTER_GRID / 2;
    double center = HarvestContinentTerrain.unclampedHarvestour(this.seed, x, z);
    if (center >= 25.0) return null;
    // Use the unclamped signal so a plateau at harvestour=0 does not produce many centers.
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        if (dx == 0 && dz == 0) continue;
        double neighbor = HarvestContinentTerrain.unclampedHarvestour(this.seed,
            x + dx * CENTER_GRID, z + dz * CENTER_GRID);
        if (neighbor < center || (neighbor == center && (dx < 0 || (dx == 0 && dz < 0)))) return null;
      }
    }
    double radius = 128.0 * (1.0 + unit(mix(this.seed ^ 0x510E527FADE682D1L, gx, gz)) * 0.35);
    // Require deep water around the island, keeping candidates away from mainland shores.
    for (int direction = 0; direction < 16; direction++) {
      double angle = direction * Math.PI / 8.0;
      int ringX = x + (int) Math.round(Math.cos(angle) * (radius + 64));
      int ringZ = z + (int) Math.round(Math.sin(angle) * (radius + 64));
      if (HarvestContinentTerrain.sampleHarvestour(this.seed, ringX, ringZ) >= 25.0) return null;
    }
    return new Island(x, z, radius);
  }

  public record Island(
      int x,
      int z,
      double radius
  ) {

    public double distance(
        int worldX,
        int worldZ
    ) {
      return Math.hypot(worldX - this.x, worldZ - this.z);
    }

  }

}
