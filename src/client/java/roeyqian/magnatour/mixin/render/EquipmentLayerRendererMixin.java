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
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = EquipmentLayerRenderer.class, priority = 3600000)
public class EquipmentLayerRendererMixin {

  /* Universe Armors: Render Custom Armor Glint
   */
  @Inject(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;" +
      "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;" +
      "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;" +
      "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V",
      at = @At("TAIL"))
  private <S> void inRenderLayers(
      EquipmentClientInfo.LayerType layerType,
      ResourceKey<EquipmentAsset> equipmentAssetId,
      Model<? super S> model,
      S state,
      ItemStack itemStack,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int lightCoords,
      int outlineColor,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.submitArmorGlint(
        model,
        state,
        itemStack,
        poseStack,
        submitNodeCollector,
        lightCoords,
        outlineColor
    );
  }

}
