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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = ItemStackRenderState.LayerRenderState.class, priority = 3600000)
public class LayerRenderStateSubmitMixin {

  /* Universe Items: Arm Glint Bridge Before Submit
   */
  @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;"
      + "Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V",
      at = @At("HEAD"))
  private void inSubmit(
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int lightCoords,
      int overlayCoords,
      int outlineColor,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.armGlintBridge(this);
  }

  /* Universe Items: Disarm Glint Bridge After Submit
   */
  @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;"
      + "Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V",
      at = @At("RETURN"))
  private void inSubmit2(
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int lightCoords,
      int overlayCoords,
      int outlineColor,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.disarmGlintBridge();
  }

}
