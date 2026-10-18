/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.world;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.MobBiomeSpawner;
import roeyqian.magnatour.registry.WorldHelper;

/*
 * Supreme Group: Dimension
 * Universe Group: Dimension
 */
public final class MagnaDimensions {

  // Supreme Group: Dimension
  public static final ResourceKey<Level> HARVEST_CONTINENT =
      ResourceKey.create(Registries.DIMENSION, WorldHelper.id("harvest_continent"));
  public static final ResourceKey<Level> ORE_CONTINENT =
      ResourceKey.create(Registries.DIMENSION, WorldHelper.id("ore_continent"));

  // Universe Group: Dimension
  public static final ResourceKey<Level> UNIVERSE_META =
      ResourceKey.create(Registries.DIMENSION, WorldHelper.id("universe_meta"));

  private MagnaDimensions() {}

  public static void init() {
    MobBiomeSpawner.registerTickEvent();
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaDimensions'");
  }

}
