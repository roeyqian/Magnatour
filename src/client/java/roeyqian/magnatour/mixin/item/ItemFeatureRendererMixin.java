/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.item;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.glint.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = ItemFeatureRenderer.class, priority = 3600000)
public abstract class ItemFeatureRendererMixin {

  @Inject(method = "prepareMainSubmit", at = @At("HEAD"))
  private void magnatour$armGlint(
      ItemFeatureRenderer.Submit submit,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.armFoilSubmit(submit);
  }

  @Inject(method = "prepareMainSubmit", at = @At("RETURN"))
  private void magnatour$disarmGlint(
      ItemFeatureRenderer.Submit submit,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.disarmFoilSubmit();
  }

  @ModifyArg(method = "prepareMainSubmit", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getVertexBuilder" +
          "(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
      index = 0)
  private RenderType magnatour$replaceGlintTexture(
      RenderType type
  ) {
    return RenderHelperForGlint.glintRenderType(type, RenderHelperForGlint.currentFoilGlint());
  }

}
