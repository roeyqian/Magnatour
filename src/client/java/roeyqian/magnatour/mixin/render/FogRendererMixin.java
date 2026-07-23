/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Minecraft
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.FogData;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.render.RenderHelperForEquipment;

@Mixin(value = FogRenderer.class, priority = 3600000)
public class FogRendererMixin {

  /* Universe Helmet: Better Vision in Fluid
   */
  @Inject(method = "setupFog", at = @At("RETURN"))
  private void inSetupFog(
      Camera camera,
      int renderDistanceInChunks,
      DeltaTracker deltaTracker,
      float darkenWorldAmount,
      ClientLevel level,
      CallbackInfoReturnable<FogData> cir
  ) {
    RenderHelperForEquipment.handleUniverseHelmetVision(camera, cir);
  }

}
