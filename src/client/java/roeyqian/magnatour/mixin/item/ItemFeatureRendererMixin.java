/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.item;

// Mojang
import com.mojang.blaze3d.vertex.QuadInstance;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.glint.RenderHelperForGlint;
import roeyqian.magnatour.renderer.GlintRenderTypes;

@Environment(EnvType.CLIENT) @Mixin(value = ItemFeatureRenderer.class, priority = 3600000)
public abstract class ItemFeatureRendererMixin extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {

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
    var glintType = ((RenderHelperForGlint.UniverseGlintHolder) (Object) submit).universeGlintType();
    if (glintType != RenderHelperForGlint.GlintType.NONE) {
      QuadInstance instance = new QuadInstance();
      instance.setColor(-1);
      instance.setLightCoords(0x00F000F0);
      instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
      var glint = getVertexBuilder(GlintRenderTypes.itemArmorGlint(glintType == RenderHelperForGlint.GlintType.SUPREME));
      for (var quad : submit.quads()) {
        glint.putBakedQuad(submit.pose(), quad, instance);
      }
    }
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
