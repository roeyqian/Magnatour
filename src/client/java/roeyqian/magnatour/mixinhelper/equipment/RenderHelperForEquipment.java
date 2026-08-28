/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.equipment;

// Minecraft
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.material.FogType;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// JOML
import org.joml.Vector4f;

// Magnatour
import roeyqian.magnatour.registry.content.UniverseItems;

public final class RenderHelperForEquipment {

  private RenderHelperForEquipment() {}

  public static void handleUniverseHelmetVision(
      Camera camera,
      CallbackInfoReturnable<FogData> cir
  ) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || !player.getItemBySlot(EquipmentSlot.HEAD).is(UniverseItems.UNIVERSE_HELMET))
      return;

    FogType type = camera.getFluidInCamera();
    FogData fogData = cir.getReturnValue();
    if (fogData == null) {
      return;
    }

    Vector4f color = fogData.color;
    if (type == FogType.LAVA) {
      color.set(color.x, color.y, color.z, 0.75f);
      fogData.environmentalStart = Math.max(fogData.environmentalStart, 0.0f);
      fogData.environmentalEnd = Math.max(fogData.environmentalEnd, 8.0f);
    } else if (type == FogType.WATER) {
      color.set(color.x, color.y, color.z, 0.5f);
      fogData.environmentalStart = Math.max(fogData.environmentalStart, 0.0f);
      fogData.environmentalEnd = Math.max(fogData.environmentalEnd, 96.0f);
    }
  }

}
