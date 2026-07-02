/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.entity.EntityClientHelperForFunction;

@Mixin(value = LivingEntity.class, priority = 3600000)
public class LivingEntityClientMixin {

  /* Custom Portal: Nausea Effect Render
   */
  @Inject(method = "getEffectBlendFactor", at = @At("HEAD"), cancellable = true)
  private void inGetEffectBlendFactor(
      Holder<MobEffect> effect,
      float tickProgress,
      CallbackInfoReturnable<Float> cir
  ) {
    EntityClientHelperForFunction.handleGetEffectBlendFactor((LivingEntity) (Object) this, effect, cir);
  }

}
