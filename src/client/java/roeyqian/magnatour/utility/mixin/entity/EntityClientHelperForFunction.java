/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.entity;

// Minecraft
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.CustomPortalBlock;
import roeyqian.magnatour.block.insert.portal.HarvestContinentPortal;
import roeyqian.magnatour.block.insert.portal.OreContinentPortal;

public final class EntityClientHelperForFunction {

  private EntityClientHelperForFunction() {}

  public static void handleGetEffectBlendFactor(
      LivingEntity living,
      Holder<MobEffect> effect,
      CallbackInfoReturnable<Float> cir
  ) {
    if (!effect.equals(MobEffects.NAUSEA) || !(living instanceof LocalPlayer)) return;

    int oreContinentPortalTicks = OreContinentPortal.clientPortalTicks;
    if (oreContinentPortalTicks > 0) {
      cir.setReturnValue((float) oreContinentPortalTicks / CustomPortalBlock.TELEPORT_TICKS);
    }

    int harvestContinentPortalTicks = HarvestContinentPortal.clientPortalTicks;
    if (harvestContinentPortalTicks > 0) {
      cir.setReturnValue((float) harvestContinentPortalTicks / CustomPortalBlock.TELEPORT_TICKS);
    }
  }

}
