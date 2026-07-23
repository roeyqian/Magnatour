/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.Minecraft;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.client.ClientHelperForEquipment;

@Mixin(value = Minecraft.class, priority = 3600000)
public class MinecraftMixin {

  @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
  private void inStartAttack(
      CallbackInfoReturnable<Boolean> cir
  ) {
    ClientHelperForEquipment.handleStartAttack((Minecraft) (Object) this, cir);
  }

}
