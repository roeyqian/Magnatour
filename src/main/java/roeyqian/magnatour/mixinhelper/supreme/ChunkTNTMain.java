/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

// Magnatour
import roeyqian.magnatour.block.supreme.ChunkTntBlock;

public final class ChunkTNTMain {

  private ChunkTNTMain() {}

  public static void handleFireTick(
      ServerLevel level,
      BlockPos pos
  ) {
    for (Direction direction : Direction.values()) {
      BlockPos neighbor = pos.relative(direction);
      if (!(level.getBlockState(neighbor).getBlock() instanceof ChunkTntBlock)) continue;

      ChunkTntBlock.prime(level, neighbor, null);
      level.removeBlock(neighbor, false);
    }
  }

}
