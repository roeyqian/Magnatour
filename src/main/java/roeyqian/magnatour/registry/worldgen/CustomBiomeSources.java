/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.worldgen;

// Minecraft
import net.minecraft.resources.Identifier;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.biome.HarvestContinentBiomeSource;
import roeyqian.magnatour.levelgen.biome.OreContinentBiomeSource;
import roeyqian.magnatour.registry.WorldgenRegHelper;

/*
 * Supreme Group: Biome Source
 * Universe Group: Biome Source
 */
public final class CustomBiomeSources {

  // Supreme Group: Biome Source
  public static final Identifier HARVEST_CONTINENT = WorldgenRegHelper.id("harvest_continent");
  public static final Identifier ORE_CONTINENT = WorldgenRegHelper.id("ore_continent");

  private CustomBiomeSources() {}

  public static void init() {
    WorldgenRegHelper.registerBiomeSource(
        HARVEST_CONTINENT.getPath(),
        HarvestContinentBiomeSource.CODEC
    );
    WorldgenRegHelper.registerBiomeSource(
        ORE_CONTINENT.getPath(),
        OreContinentBiomeSource.CODEC
    );
    Magnatour.LOGGER.info("[Server] Initializing 'CustomBiomeSources'");
  }

}
