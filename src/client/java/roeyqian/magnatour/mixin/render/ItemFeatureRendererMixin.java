/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = ItemFeatureRenderer.class, priority = 3600000)
public abstract class ItemFeatureRendererMixin extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {

  @Inject(method = "getFoilBuffer", at = @At("HEAD"), cancellable = true)
  private void inGetFoilBuffer(
      RenderType baseRenderType,
      PoseStack.Pose decalPose,
      CallbackInfoReturnable<VertexConsumer> cir
  ) {
    RenderHelperForGlint.GlintType glintType = RenderHelperForGlint.currentFoilGlint();
    if (glintType == RenderHelperForGlint.GlintType.NONE) {
      return;
    }

    VertexConsumer vertexConsumer = this.getVertexBuilder(
        RenderHelperForGlint.glintRenderType(baseRenderType, glintType)
    );
    if (decalPose != null) {
      vertexConsumer = new SheetedDecalTextureGenerator(vertexConsumer, decalPose, 0.0078125F);
    }
    cir.setReturnValue(vertexConsumer);
  }

  /* Universe Items: Render Custom Rainbow Glint
   */
  @Inject(method = "prepareFoilSubmit", at = @At("HEAD"))
  private void inPrepareFoilSubmit(
      ItemFeatureRenderer.Submit submit,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.armFoilSubmit(submit);
  }

  @Inject(method = "prepareFoilSubmit", at = @At("RETURN"))
  private void inPrepareFoilSubmit2(
      ItemFeatureRenderer.Submit submit,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.disarmFoilSubmit();
  }

}
