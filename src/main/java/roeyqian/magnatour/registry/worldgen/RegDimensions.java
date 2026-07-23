/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.worldgen;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.biome.BiomeMobSpawner;
import roeyqian.magnatour.registry.LevelRegHelper;

/*
 * Supreme Group: Dimension
 * Universe Group: Dimension
 */
public final class RegDimensions {

  // Supreme Group: Dimension
  public static final ResourceKey<Level> HARVEST_CONTINENT =
      ResourceKey.create(Registries.DIMENSION, LevelRegHelper.id("harvest_continent"));
  public static final ResourceKey<Level> ORE_CONTINENT =
      ResourceKey.create(Registries.DIMENSION, LevelRegHelper.id("ore_continent"));

  private RegDimensions() {}

  public static void init() {
    BiomeMobSpawner.registerTickEvent();
    Magnatour.LOGGER.info("[Server] Initializing 'RegDimensions'");
  }

}
