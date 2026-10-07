/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SkyRenderer;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.levelgen.terrain.HarvestContinentTerrain;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

@Mixin(value = SkyRenderer.class, priority = 3600000)
public class SkyRendererMixin {

  /** Keep the Harvest horizon aligned with its lowered terrain. */
  @Inject(method = "shouldRenderDarkDisc", at = @At("HEAD"), cancellable = true)
  private void inShouldRenderDarkDisc(
      float tickProgress,
      ClientLevel level,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (level.dimension() == CustomDimensions.UNIVERSE_META) {
      cir.setReturnValue(false);
    } else if (level.dimension() == CustomDimensions.HARVEST_CONTINENT) {
      LocalPlayer player = Minecraft.getInstance().player;
      double horizon = level.getLevelData().getHorizonHeight(level)
          + HarvestContinentTerrain.SURFACE_Y_OFFSET;
      cir.setReturnValue(player != null && player.getEyePosition(tickProgress).y < horizon
          && !player.isUnderWater());
    }
  }

}
