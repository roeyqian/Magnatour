/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.tree;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;

/** Generates a golden tree rooted in the single reserved grass block. */
public final class ReservedGoldenTree {

  private ReservedGoldenTree() {}

  public static void place(
      WorldGenLevel level,
      long seed,
      BlockPos ground
  ) {
    BlockPos root = ground.above();
    RandomSource random = RandomSource.create(
        seed ^ ground.getX() * 341873128712L ^ ground.getZ() * 132897987541L);
    int height = 6 + random.nextInt(3);
    BlockState log = SupremeBlocks.GOLDEN_LOG.defaultBlockState();
    BlockState leaves = SupremeBlocks.GOLDEN_LEAVES.defaultBlockState();
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    // Direct placement avoids the random selector and tree clearance rejection.
    // The grass marker stays intact instead of being converted to vanilla dirt.
    for (int dy = 0; dy < height; dy++) {
      pos.setWithOffset(root, 0, dy, 0);
      level.setBlock(pos, log, Block.UPDATE_CLIENTS);
    }
    for (int dy = height - 3; dy <= height; dy++) {
      int radius = dy == height ? 1 : 2;
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (dx * dx + dz * dz > radius * radius + 1) continue;
          pos.setWithOffset(root, dx, dy, dz);
          BlockState existing = level.getBlockState(pos);
          if (!existing.isAir() && !existing.is(Blocks.WHEAT)
              && !existing.is(SupremeBlocks.GOLDEN_LEAVES)) continue;
          int distance = Math.max(1, Math.abs(dx) + Math.abs(dz) + Math.max(0, dy - height + 1));
          level.setBlock(pos, leaves.setValue(BlockStateProperties.DISTANCE, distance), Block.UPDATE_CLIENTS);
        }
      }
    }
  }

}
