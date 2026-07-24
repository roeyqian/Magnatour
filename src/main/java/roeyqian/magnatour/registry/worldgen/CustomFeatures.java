/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.worldgen;

// Minecraft
import net.minecraft.world.level.levelgen.feature.Feature;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.tree.OreTreeConfiguration;
import roeyqian.magnatour.levelgen.tree.OreTreeFeature;
import roeyqian.magnatour.registry.WorldgenRegHelper;

/*
 * Supreme Group: Feature
 * Universe Group: Feature
 */
public final class CustomFeatures {

  // Supreme Group: Ore Continent
  public static final Feature<OreTreeConfiguration> ORE_TREE =
      WorldgenRegHelper.registerFeature(
          "ore_tree",
          new OreTreeFeature(OreTreeConfiguration.CODEC)
      );

  private CustomFeatures() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'CustomFeatures'");
  }

}
