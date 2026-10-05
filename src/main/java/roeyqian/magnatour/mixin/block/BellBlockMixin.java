/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.levelgen.structure.StructureMobSpawner;

@Mixin(BellBlock.class)
public class BellBlockMixin {

  @Inject(method = "useWithoutItem", at = @At("RETURN"))
  private void afterUseWithoutItem(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      BlockHitResult hit,
      CallbackInfoReturnable<InteractionResult> cir
  ) {
    if (cir.getReturnValue().consumesAction()
        && level instanceof ServerLevel serverLevel
        && player instanceof ServerPlayer serverPlayer) {
      StructureMobSpawner.onGoldBellTowerBellRung(serverLevel, serverPlayer, pos);
    }
  }

}
