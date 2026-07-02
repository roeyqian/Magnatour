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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.block.SummonStructureHelper;
import roeyqian.magnatour.utility.mixin.block.BlockHelperForFunction;

@Mixin(value = BlockBehaviour.class, priority = 3600000)
public class BlockBehaviorMixin {

  @Inject(method = "onPlace", at = @At("TAIL"))
  private void inOnPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState,
      boolean movedByPiston,
      CallbackInfo ci
  ) {
    BlockHelperForFunction.handleVanillaSummonTriggerOnPlace(state, level, pos, oldState);
  }

}
