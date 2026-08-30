/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

@Mixin(value = SkyRenderer.class, priority = 3600000)
public class SkyRendererMixin {

  /* Universe Meta: Never render the vanilla dark disc below the horizon.
   */
  @Inject(method = "shouldRenderDarkDisc", at = @At("HEAD"), cancellable = true)
  private void inShouldRenderDarkDisc(
      float tickProgress,
      ClientLevel level,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (level.dimension() == CustomDimensions.UNIVERSE_META) cir.setReturnValue(false);
  }

}
