/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.block.insert.ChunkTntBlock;

@Mixin(value = FireBlock.class, priority = 3600000)
public class FireBlockMixin {

  @Inject(method = "tick", at = @At("HEAD"))
  private void beforeTick(
      BlockState state,
      ServerLevel level,
      BlockPos pos,
      net.minecraft.util.RandomSource random,
      CallbackInfo ci
  ) {
    for (Direction dir : Direction.values()) {
      BlockPos neighbor = pos.relative(dir);
      if (level.getBlockState(neighbor).getBlock() instanceof ChunkTntBlock) {
        ChunkTntBlock.prime(level, neighbor, null);
        level.removeBlock(neighbor, false);
      }
    }
  }

}
