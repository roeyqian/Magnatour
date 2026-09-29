/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.equipment;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.glint.RenderHelperForGlint;
import roeyqian.magnatour.item.CustomArmorMaterial;

@Environment(EnvType.CLIENT) @Mixin(value = EquipmentLayerRenderer.class, priority = 3600000)
public class EquipmentLayerRendererMixin {

  private static final int UNIVERSE_ARMOR_FRAMES = 32;

  @Unique private boolean magnatour$animateUniverseHumanoid;

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

  @ModifyArg(
      method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;" +
          "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;" +
          "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;" +
          "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;" +
          "ILnet/minecraft/resources/Identifier;II)V",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(" +
              "Lnet/minecraft/client/model/Model;Ljava/lang/Object;" +
              "Lcom/mojang/blaze3d/vertex/PoseStack;" +
              "Lnet/minecraft/client/renderer/rendertype/RenderType;III" +
              "Lnet/minecraft/client/renderer/texture/UvMapping;I)V",
          ordinal = 0
      ),
      index = 7
  )
  private UvMapping magnatour$animateUniverseArmor(
      UvMapping original
  ) {
    if (!magnatour$animateUniverseHumanoid) {
      return original;
    }

    int frame = (int) Math.floorMod(System.currentTimeMillis() / 50L, UNIVERSE_ARMOR_FRAMES);
    return new UvMapping() {
      @Override
      public float getU(float u) {
        return u;
      }

      @Override
      public float getV(float v) {
        return (frame + v) / UNIVERSE_ARMOR_FRAMES;
      }
    };
  }

  @Inject(
      method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;" +
          "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;" +
          "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;" +
          "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;" +
          "ILnet/minecraft/resources/Identifier;II)V",
      at = @At("HEAD")
  )
  private <S> void magnatour$selectUniverseArmorAnimation(
      EquipmentClientInfo.LayerType layerType,
      ResourceKey<EquipmentAsset> equipmentAssetId,
      Model<? super S> model,
      S state,
      ItemStack itemStack,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int lightCoords,
      Identifier playerTexture,
      int layerColor,
      int outlineColor,
      CallbackInfo ci
  ) {
    magnatour$animateUniverseHumanoid = layerType == EquipmentClientInfo.LayerType.HUMANOID
        && equipmentAssetId.equals(CustomArmorMaterial.UNIVERSE_ARMOR.assetId());
  }

}
