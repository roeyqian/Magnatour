/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

// Minecraft
import net.minecraft.util.Mth;

/** Height profiles only; biome selection is owned by the shared harvestour field. */
public final class HarvestRegionalTerrain {

  private HarvestRegionalTerrain() {}

  static double berrySnowfieldHeight(
      long seed,
      int x,
      int z
  ) {
    double rolling = HarvestContinentTerrain.fbmPerlin(seed ^ 0x629A292A367CD507L, x, z, 0.004, 3);
    double detail = HarvestContinentTerrain.fbmPerlin(seed ^ 0x9159015A3070DD17L, x, z, 0.018, 2);
    return Mth.clamp(112.0 + rolling * 15.0 + detail * 3.0, 100.0, 124.0) + HarvestContinentTerrain.SURFACE_Y_OFFSET;
  }

  static double desertHeight(
      long seed,
      int x,
      int z
  ) {
    double dunes = HarvestContinentTerrain.fbmPerlin(seed ^ 0xCBBB9D5DC1059ED8L, x, z, 0.006, 3);
    double ripples = Math.sin(x * 0.045 + z * 0.018 + dunes * 5.0);
    return Mth.clamp(100.0 + dunes * 48.0 + ripples * 7.0, 78.0, 132.0) + HarvestContinentTerrain.SURFACE_Y_OFFSET;
  }

  static double marshHeight(
      long seed,
      int x,
      int z
  ) {
    double pools = HarvestContinentTerrain.fbmPerlin(seed ^ 0x5BE0CD19137E2179L, x, z, 0.025, 3);
    return Mth.clamp(64.0 + pools * 12.0, 59.0, 69.0) + HarvestContinentTerrain.SURFACE_Y_OFFSET;
  }

  static double sacredMountainHeight(
      long seed,
      int x,
      int z
  ) {
    double broad = HarvestContinentTerrain.fbmPerlin(seed ^ 0x510E527FADE682D1L, x, z, 0.002, 3);
    double ridge = 1.0 - Math.abs(HarvestContinentTerrain.fbmPerlin(seed ^ 0x9B05688C2B3E6C1FL, x, z, 0.008, 3));
    double detail = HarvestContinentTerrain.fbmPerlin(seed ^ 0x1F83D9ABFB41BD6BL, x, z, 0.035, 2);
    return Mth.clamp(380.0 + broad * 100.0 + ridge * ridge * 145.0 + detail * 18.0,
        380.0, 520.0 - HarvestContinentTerrain.SURFACE_Y_OFFSET) + HarvestContinentTerrain.SURFACE_Y_OFFSET;
  }

}
