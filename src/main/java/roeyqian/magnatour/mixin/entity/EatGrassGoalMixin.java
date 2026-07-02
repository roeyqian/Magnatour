/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.Level;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.entity.EntityHelperForCreature;

@Mixin(value = EatBlockGoal.class, priority = 3600000)
public abstract class EatGrassGoalMixin {

  @Shadow
  private int eatAnimationTick;

  @Shadow @Final
  private Mob mob;

  @Shadow @Final
  private Level level;

  /* Ever-Water Farmland: Enable Eating on Custom Farmland
   */
  @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
  private void inCanUse(
      CallbackInfoReturnable<Boolean> cir
  ) {
    EntityHelperForCreature.handleEatBlockCanUse(this.mob, this.level, cir);
  }

  /* Ever-Water Farmland: Eating Tick Animation on Custom Farmland
   */
  @Inject(method = "tick", at = @At("TAIL"))
  private void inTick(
      CallbackInfo ci
  ) {
    EntityHelperForCreature.handleEatBlockTick(this.mob, this.level, this.eatAnimationTick);
  }

}
