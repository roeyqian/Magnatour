/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.portal;

// Minecraft
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;

public final class RenderHelperForPortalEffect {

  public static float handleTick(
      LocalPlayer player,
      float spinningEffectSpeed
  ) {
    if (player.portalEffectIntensity <= 0
        && player.getEffectBlendFactor(MobEffects.NAUSEA, 1.0F) > 0) {
      return 20.0F;
    }
    return spinningEffectSpeed;
  }

}
