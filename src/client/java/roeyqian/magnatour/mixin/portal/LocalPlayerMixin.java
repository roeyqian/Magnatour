/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.portal;

// Minecraft
import net.minecraft.client.player.LocalPlayer;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// ObjectWeb ASM
import org.objectweb.asm.Opcodes;

// Magnatour
import roeyqian.magnatour.mixinhelper.portal.RenderHelperForPortalEffect;

@Mixin(value = LocalPlayer.class, priority = 3600000)
public class LocalPlayerMixin {

  @Shadow
  private float spinningEffectSpeed;

  @Inject(method = "tickSpinningEffect", at = @At(value = "FIELD",
      target = "Lnet/minecraft/client/player/LocalPlayer;spinningEffectSpeed:F",
      opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
  private void magnatour$adjustNauseaSpin(
      CallbackInfo ci
  ) {
    LocalPlayer player = (LocalPlayer) (Object) this;
    this.spinningEffectSpeed = RenderHelperForPortalEffect.handleTick(player, this.spinningEffectSpeed);
  }

}
