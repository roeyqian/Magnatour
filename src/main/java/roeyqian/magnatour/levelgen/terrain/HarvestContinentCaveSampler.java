/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

/**
 * Task-local cave density grid. World-aligned nodes are identical across chunk
 * boundaries, including negative coordinates and single-column terrain queries.
 */
public final class HarvestContinentCaveSampler {

  private static final int HORIZONTAL_STEP = 4;
  private static final int VERTICAL_STEP = 8;

  private final int maxY;
  private final int originX;
  private final int originY;
  private final int originZ;
  private final int sizeY;
  private final int sizeZ;

  private final double[] densities;

  public HarvestContinentCaveSampler(
      long seed,
      int minX, int minZ, int width, int depth, int maxSurfaceY
  ) {
    this.originX = Math.floorDiv(minX, HORIZONTAL_STEP) * HORIZONTAL_STEP;
    this.originY = Math.floorDiv(HarvestContinentTerrain.CAVE_MIN_Y, VERTICAL_STEP) * VERTICAL_STEP;
    this.originZ = Math.floorDiv(minZ, HORIZONTAL_STEP) * HORIZONTAL_STEP;
    this.maxY = Math.min(HarvestContinentTerrain.CAVE_MAX_Y,
        maxSurfaceY - HarvestContinentTerrain.CAVE_SURFACE_COVER);

    int sizeX = Math.floorDiv(minX + width - 1 - this.originX, HORIZONTAL_STEP) + 2;
    this.sizeZ = Math.floorDiv(minZ + depth - 1 - this.originZ, HORIZONTAL_STEP) + 2;
    this.sizeY = this.maxY <= HarvestContinentTerrain.CAVE_MIN_Y + 1
        ? 0 : Math.floorDiv(this.maxY - 1 - this.originY, VERTICAL_STEP) + 2;
    this.densities = new double[sizeX * this.sizeY * this.sizeZ];

    for (int gx = 0; gx < sizeX; gx++) {
      int worldX = this.originX + gx * HORIZONTAL_STEP;
      for (int gy = 0; gy < this.sizeY; gy++) {
        int y = this.originY + gy * VERTICAL_STEP;
        for (int gz = 0; gz < this.sizeZ; gz++) {
          int worldZ = this.originZ + gz * HORIZONTAL_STEP;
          this.densities[index(gx, gy, gz)] =
              HarvestContinentTerrain.caveDensity(seed, worldX, y, worldZ);
        }
      }
    }
  }

  public boolean isCave(
      int worldX, int y, int worldZ, int surfaceY
  ) {
    if (this.sizeY == 0 || y >= this.maxY || !HarvestContinentTerrain.canCarveCave(y, surfaceY)) {
      return false;
    }
    return interpolatedDensity(worldX, y, worldZ)
        > HarvestContinentTerrain.caveThreshold(y, surfaceY);
  }

  private static double lerp(
      double delta, double first, double second
  ) {
    return first + delta * (second - first);
  }

  private int index(
      int x, int y, int z
  ) {
    return (x * this.sizeY + y) * this.sizeZ + z;
  }

  private double at(
      int x, int y, int z
  ) {
    return this.densities[index(x, y, z)];
  }

  private double interpolatedDensity(
      int worldX, int y, int worldZ
  ) {
    int offsetX = worldX - this.originX;
    int offsetY = y - this.originY;
    int offsetZ = worldZ - this.originZ;
    int gx = offsetX / HORIZONTAL_STEP;
    int gy = offsetY / VERTICAL_STEP;
    int gz = offsetZ / HORIZONTAL_STEP;
    double tx = (offsetX % HORIZONTAL_STEP) / (double) HORIZONTAL_STEP;
    double ty = (offsetY % VERTICAL_STEP) / (double) VERTICAL_STEP;
    double tz = (offsetZ % HORIZONTAL_STEP) / (double) HORIZONTAL_STEP;

    double x00 = lerp(tx, at(gx, gy, gz), at(gx + 1, gy, gz));
    double x10 = lerp(tx, at(gx, gy + 1, gz), at(gx + 1, gy + 1, gz));
    double x01 = lerp(tx, at(gx, gy, gz + 1), at(gx + 1, gy, gz + 1));
    double x11 = lerp(tx, at(gx, gy + 1, gz + 1), at(gx + 1, gy + 1, gz + 1));
    return lerp(tz, lerp(ty, x00, x10), lerp(ty, x01, x11));
  }

}
