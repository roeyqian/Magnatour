/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

// Magnatour
import roeyqian.magnatour.level.UniverseMetaGenerationSavedData;
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public final class UniverseMetaDimensionGenerator {

  // 32 batches finish the cube in about 1.6 seconds at 20 TPS without a single-tick spike.
  private static final int BLOCKS_PER_TICK = 8192;
  private static final int BORDER_THICKNESS = 4;
  private static final int CUBE_MAX = 31;
  private static final int CUBE_MIN = -32;
  private static final int CUBE_SIZE = CUBE_MAX - CUBE_MIN + 1;
  private static final int CUBE_LAYER_SIZE = CUBE_SIZE * CUBE_SIZE;
  private static final int TOTAL_BLOCKS = CUBE_SIZE * CUBE_LAYER_SIZE;

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      ServerLevel world = server.getLevel(CustomDimensions.UNIVERSE_META);
      UniverseMetaGenerationSavedData generationData = UniverseMetaGenerationSavedData.get(server);
      if (world != null && !generationData.isGenerated()) {
        int startIndex = generationData.nextBlockIndex();
        int endIndex = Math.min(startIndex + BLOCKS_PER_TICK, TOTAL_BLOCKS);
        generateCubeSlice(world, startIndex, endIndex);

        if (endIndex == TOTAL_BLOCKS) {
          generationData.markGenerated();
        } else {
          generationData.setNextBlockIndex(endIndex);
        }
      }
    });
  }

  private static void generateCubeSlice(
      ServerLevel world,
      int startIndex,
      int endIndex
  ) {
    for (int index = startIndex; index < endIndex; index++) {
      int x = CUBE_MIN + index / CUBE_LAYER_SIZE;
      int remaining = index % CUBE_LAYER_SIZE;
      int y = CUBE_MIN + remaining / CUBE_SIZE;
      int z = CUBE_MIN + remaining % CUBE_SIZE;

      int boundaryAxes = 0;
      if (isNearBoundary(x)) {
        boundaryAxes++;
      }
      if (isNearBoundary(y)) {
        boundaryAxes++;
      }
      if (isNearBoundary(z)) {
        boundaryAxes++;
      }

      boolean isSurfaceBorder = boundaryAxes >= 2;
      world.setBlock(
          new BlockPos(x, y, z),
          (isSurfaceBorder ? UniverseBlocks.UNIVERSE_LIGHT_BLOCK : UniverseBlocks.UNIVERSE_DARK_BLOCK)
              .defaultBlockState(),
          3
      );
    }
  }

  private static boolean isNearBoundary(
      int coordinate
  ) {
    return coordinate < CUBE_MIN + BORDER_THICKNESS
        || coordinate > CUBE_MAX - BORDER_THICKNESS;
  }

}
