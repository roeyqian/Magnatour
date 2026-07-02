/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.block.BlockHelperForEquipment;

@Mixin(value = PowderSnowBlock.class, priority = 3600000)
public class PowderSnowBlockMixin {

  /* Universe Boots: Walking on Powder Snow
   */
  @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
  private static void inCanEntityWalkOnPowderSnow(
      Entity entity,
      CallbackInfoReturnable<Boolean> cir
  ) {
    BlockHelperForEquipment.handleCanEntityWalkOnPowderSnow(entity, cir);
  }

}
