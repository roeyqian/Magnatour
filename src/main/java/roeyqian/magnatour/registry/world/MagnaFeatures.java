/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.world;

// Mojang
import com.mojang.serialization.MapCodec;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.feature.GiantIceSpikeFeature;
import roeyqian.magnatour.levelgen.tree.OreTreeFeature;
import roeyqian.magnatour.registry.WorldHelper;

/*
 * Supreme Group: Feature
 * Universe Group: Feature
 */
  public final class MagnaFeatures {

  // Supreme Group: Harvest Continent
  public static final MapCodec<GiantIceSpikeFeature> GIANT_ICE_SPIKE =
      WorldHelper.registerFeature(
          "giant_ice_spike",
          GiantIceSpikeFeature.CODEC
      );

  // Supreme Group: Ore Continent
  public static final MapCodec<OreTreeFeature> ORE_TREE =
      WorldHelper.registerFeature(
          "ore_tree",
          OreTreeFeature.CODEC
      );

  private MagnaFeatures() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaFeatures'");
  }

}
