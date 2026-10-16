/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.world;

// Minecraft
import net.minecraft.resources.Identifier;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.biome.HarvestContinentBiomeSource;
import roeyqian.magnatour.levelgen.biome.OreContinentBiomeSource;
import roeyqian.magnatour.registry.WorldHelper;

/*
 * Supreme Group: Biome Source
 * Universe Group: Biome Source
 */
public final class MagnaBiomeSources {

  // Supreme Group: Biome Source
  public static final Identifier HARVEST_CONTINENT = WorldHelper.id("harvest_continent");
  public static final Identifier ORE_CONTINENT = WorldHelper.id("ore_continent");

  private MagnaBiomeSources() {}

  public static void init() {
    WorldHelper.registerBiomeSource(
        HARVEST_CONTINENT.getPath(),
        HarvestContinentBiomeSource.CODEC
    );
    WorldHelper.registerBiomeSource(
        ORE_CONTINENT.getPath(),
        OreContinentBiomeSource.CODEC
    );
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaBiomeSources'");
  }

}
