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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.CustomPortalBlock;

@Mixin(value = BaseFireBlock.class, priority = 3600000)
public class BaseFireBlockMixin {

  /* Custom Portal: Vanilla Fire Placement Ignition
   */
  @Inject(method = "canBePlacedAt", at = @At("RETURN"), cancellable = true)
  private static void inCanBePlacedAt(
      Level level,
      BlockPos pos,
      Direction forwardDirection,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (cir.getReturnValue()) return;
    cir.setReturnValue(CustomPortalBlock.canBePlacedAt(level, pos, forwardDirection));
  }

  /* Custom Portal: Vanilla Fire onPlace Portal Creation
   */
  @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
  private void inOnPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState,
      boolean movedByPiston,
      CallbackInfo ci
  ) {
    if (oldState.is(state.getBlock())) return;
    if (CustomPortalBlock.tryCreatePortalFromFire(level, pos)) ci.cancel();
  }

}
