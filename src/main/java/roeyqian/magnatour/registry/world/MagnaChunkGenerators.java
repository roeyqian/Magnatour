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
import roeyqian.magnatour.levelgen.supreme.HarvestContinentChunkGenerator;
import roeyqian.magnatour.levelgen.supreme.OreContinentChunkGenerator;
import roeyqian.magnatour.registry.WorldHelper;

/*
 * Supreme Group: Chunk Generator
 * Universe Group: Chunk Generator
 */
public final class MagnaChunkGenerators {

  // Supreme Group: Chunk Generator
  public static final Identifier HARVEST_CONTINENT = WorldHelper.id("harvest_continent");
  public static final Identifier ORE_CONTINENT = WorldHelper.id("ore_continent");

  private MagnaChunkGenerators() {}

  public static void init() {
    WorldHelper.registerChunkGenerator(
        HARVEST_CONTINENT.getPath(),
        HarvestContinentChunkGenerator.CODEC
    );
    WorldHelper.registerChunkGenerator(
        ORE_CONTINENT.getPath(),
        OreContinentChunkGenerator.CODEC
    );
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaChunkGenerators'");
  }

}
