/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.RenderBuffers;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = RenderBuffers.class, priority = 3600000)
public class RenderBuffersMixin {

  /* Universe Items: Register Custom Glint Buffers
   */
  @Inject(method = "<init>", at = @At("TAIL"))
  private void inInit(
      int maxSectionBuilders,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.registerGlintBuffers((RenderBuffers) (Object) this);
  }

}
