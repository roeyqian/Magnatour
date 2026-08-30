/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.worldgen;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

// Magnatour
import roeyqian.magnatour.level.UniverseMetaGenerationSavedData;
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public class UniverseMetaDimensionGenerator {

  private static final int BORDER_THICKNESS = 4;
  private static final int CUBE_MAX = 31;
  private static final int CUBE_MIN = -32;

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      ServerLevel world = server.getLevel(CustomDimensions.UNIVERSE_META);
      UniverseMetaGenerationSavedData generationData = UniverseMetaGenerationSavedData.get(server);
      if (world != null && !generationData.isGenerated()) {
        generateCube(world);
        generationData.markGenerated();
      }
    });
  }

  private static void generateCube(
      ServerLevel world
  ) {
    System.out.println("[UniverseMeta] Generating 64x64x64 cube at origin...");

    // Generate 64x64x64 cube centered at (0, 0, 0)
    // From (-32, -32, -32) to (31, 31, 31)
    for (int x = CUBE_MIN; x <= CUBE_MAX; x++) {
      for (int y = CUBE_MIN; y <= CUBE_MAX; y++) {
        for (int z = CUBE_MIN; z <= CUBE_MAX; z++) {
          BlockPos pos = new BlockPos(x, y, z);
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
              pos,
              (isSurfaceBorder ? UniverseBlocks.UNIVERSE_LIGHT_BLOCK : UniverseBlocks.UNIVERSE_DARK_BLOCK)
                  .defaultBlockState(),
              3
          );
        }
      }
    }

    System.out.println("[UniverseMeta] Cube generation complete!");
  }

  private static boolean isNearBoundary(
      int coordinate
  ) {
    return coordinate < CUBE_MIN + BORDER_THICKNESS
        || coordinate > CUBE_MAX - BORDER_THICKNESS;
  }

}
