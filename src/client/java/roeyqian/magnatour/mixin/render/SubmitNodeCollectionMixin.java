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

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Magnatour
import roeyqian.magnatour.utility.mixin.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = SubmitNodeCollection.class, priority = 3600000)
public class SubmitNodeCollectionMixin {

  /* Universe Items: Mark Item Submit for Glint
   */
  @Inject(method = "submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;"
      + "Lnet/minecraft/world/item/ItemDisplayContext;III[I"
      + "Ljava/util/List;"
      + "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit;hasTranslucency()Z"
      ),
      locals = LocalCapture.CAPTURE_FAILHARD)
  private void inSubmitItem(
      PoseStack poseStack,
      ItemDisplayContext displayContext,
      int lightCoords,
      int overlayCoords,
      int outlineColor,
      int[] tintLayers,
      java.util.List<BakedQuad> quads,
      ItemStackRenderState.FoilType foilType,
      CallbackInfo ci,
      PoseStack.Pose pose,
      ItemFeatureRenderer.Submit submit
  ) {
    RenderHelperForGlint.markLastSubmit(submit);
  }

}
