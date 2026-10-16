/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.renderer;

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
import roeyqian.magnatour.mixinhelper.base.GlintClient;
import roeyqian.magnatour.mixinhelper.base.GlintClient.RenderTypes;

@Environment(EnvType.CLIENT) @Mixin(value = ItemFeatureRenderer.class, priority = 3600000)
public abstract class ItemFeatureRendererMixin extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {

  @Inject(method = "prepareMainSubmit", at = @At("HEAD"))
  private void magnatour$armGlint(
      ItemFeatureRenderer.Submit submit,
      CallbackInfo ci
  ) {
    GlintClient.armFoilSubmit(submit);
  }

  @Inject(method = "prepareMainSubmit", at = @At("RETURN"))
  private void magnatour$disarmGlint(
      ItemFeatureRenderer.Submit submit,
      CallbackInfo ci
  ) {
    var glintType = ((GlintClient.UniverseGlintHolder) (Object) submit).universeGlintType();
    if (glintType != GlintClient.GlintType.NONE) {
      QuadInstance instance = new QuadInstance();
      instance.setColor(-1);
      instance.setLightCoords(0x00F000F0);
      instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
      var glint = getVertexBuilder(RenderTypes.itemArmorGlint(glintType == GlintClient.GlintType.SUPREME));
      for (var quad : submit.quads()) {
        glint.putBakedQuad(submit.pose(), quad, instance);
      }
    }
    GlintClient.disarmFoilSubmit();
  }

  @ModifyArg(method = "prepareMainSubmit", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getVertexBuilder" +
          "(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
      index = 0)
  private RenderType magnatour$replaceGlintTexture(
      RenderType type
  ) {
    return GlintClient.glintRenderType(type, GlintClient.currentFoilGlint());
  }

}
