/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Mojang
import com.mojang.blaze3d.vertex.QuadInstance;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = ItemFeatureRenderer.class, priority = 3600000)
public abstract class ItemFeatureRendererMixin {

  @Shadow @Final
  private QuadInstance quadInstance;

  /* Universe Items: Render Custom Rainbow Glint
   */
  @Inject(method = "renderItem(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"
      + "Lnet/minecraft/client/renderer/OutlineBufferSource;"
      + "Lnet/minecraft/client/renderer/SubmitNodeStorage$ItemSubmit;)V",
      at = @At("TAIL"))
  private void inRenderItem(
      MultiBufferSource.BufferSource bufferSource,
      OutlineBufferSource outlineBufferSource,
      SubmitNodeStorage.ItemSubmit submit,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.renderRainbowGlint(this.quadInstance, bufferSource, submit);
  }

}
