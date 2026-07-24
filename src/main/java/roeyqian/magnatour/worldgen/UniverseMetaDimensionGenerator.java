/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.worldgen;

// Java Standard
import java.util.HashSet;
import java.util.Set;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

// Magnatour
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public class UniverseMetaDimensionGenerator {

  private static final Set<ServerLevel> GENERATED_WORLDS = new HashSet<>();

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      ServerLevel world = server.getLevel(CustomDimensions.UNIVERSE_META);
      if (world != null && !GENERATED_WORLDS.contains(world)) {
        GENERATED_WORLDS.add(world);
        generateCube(world);
      }
    });
  }

  private static void generateCube(
      ServerLevel world
  ) {
    System.out.println("[UniverseMeta] Generating 64x64x64 cube at origin...");

    // Generate 64x64x64 cube centered at (0, 0, 0)
    // From (-32, -32, -32) to (31, 31, 31)
    for (int x = -32; x < 32; x++) {
      for (int y = -32; y < 32; y++) {
        for (int z = -32; z < 32; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          world.setBlock(pos, UniverseBlocks.UNIVERSE_PRIMARY_BLOCK.defaultBlockState(), 3);
        }
      }
    }

    System.out.println("[UniverseMeta] Cube generation complete!");
  }

}
