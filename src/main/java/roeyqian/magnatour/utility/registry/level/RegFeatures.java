/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.level;

// Minecraft
import net.minecraft.world.level.levelgen.feature.Feature;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.feature.OreTreeConfiguration;
import roeyqian.magnatour.level.feature.OreTreeFeature;

/*
 * Supreme Group: Feature
 * Universe Group: Feature
 */
public final class RegFeatures {

  // Supreme Group: Ore Continent
  public static final Feature<OreTreeConfiguration> ORE_TREE =
      LevelRegHelper.registerFeature(
          "ore_tree",
          new OreTreeFeature(OreTreeConfiguration.CODEC)
      );

  private RegFeatures() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegFeatures'");
  }

}
