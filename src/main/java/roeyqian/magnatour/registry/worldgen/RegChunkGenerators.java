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
import roeyqian.magnatour.levelgen.OreContinentChunkGenerator;
import roeyqian.magnatour.registry.LevelRegHelper;

/*
 * Supreme Group: Chunk Generator
 * Universe Group: Chunk Generator
 */
public final class RegChunkGenerators {

  // Supreme Group: Chunk Generator
  public static final Identifier ORE_CONTINENT = LevelRegHelper.id("ore_continent");

  private RegChunkGenerators() {}

  public static void init() {
    LevelRegHelper.registerChunkGenerator(
        ORE_CONTINENT.getPath(),
        OreContinentChunkGenerator.CODEC
    );
    Magnatour.LOGGER.info("[Server] Initializing 'RegChunkGenerators'");
  }

}
