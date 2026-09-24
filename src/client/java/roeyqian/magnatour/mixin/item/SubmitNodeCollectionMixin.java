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
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Magnatour
import roeyqian.magnatour.mixinhelper.glint.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = SubmitNodeCollection.class, priority = 3600000)
public class SubmitNodeCollectionMixin {

  private static SubmitNode mark(
      SubmitNode node
  ) {
    if (node instanceof ItemFeatureRenderer.Submit submit) {
      RenderHelperForGlint.markLastSubmit(submit);
    }
    return node;
  }

  @ModifyArg(method = "submitItem", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;submit" +
          "(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V"), index = 0)
  private SubmitNode magnatour$markSolidItem(
      SubmitNode node
  ) {
    return mark(node);
  }

  @ModifyArg(method = "submitItem", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/client/renderer/feature/phase/FeatureRenderPhase;submit" +
          "(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V"), index = 0)
  private SubmitNode magnatour$markTranslucentItem(
      SubmitNode node
  ) {
    return mark(node);
  }

}
