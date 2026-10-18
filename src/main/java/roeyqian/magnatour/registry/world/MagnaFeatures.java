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
import roeyqian.magnatour.levelgen.supreme.HarvestIceSpikes;
import roeyqian.magnatour.levelgen.supreme.OreMineralTrees;
import roeyqian.magnatour.registry.WorldHelper;

/*
 * Supreme Group: Feature
 * Universe Group: Feature
 */
  public final class MagnaFeatures {

  // Supreme Group: Harvest Continent
  public static final MapCodec<HarvestIceSpikes> GIANT_ICE_SPIKE =
      WorldHelper.registerFeature(
          "giant_ice_spike",
          HarvestIceSpikes.CODEC
      );

  // Supreme Group: Ore Continent
  public static final MapCodec<OreMineralTrees> ORE_TREE =
      WorldHelper.registerFeature(
          "ore_tree",
          OreMineralTrees.CODEC
      );

  private MagnaFeatures() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaFeatures'");
  }

}
