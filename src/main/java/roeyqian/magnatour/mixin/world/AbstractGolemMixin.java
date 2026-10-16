/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.level.Level;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.entity.DiamondCityGolemTargetGoal;

@Mixin(AbstractGolem.class)
public abstract class AbstractGolemMixin extends Mob {

  protected AbstractGolemMixin(
      EntityType<? extends Mob> type,
      Level level
  ) {
    super(type, level);
  }

  @Inject(method = "<init>", at = @At("RETURN"))
  private void addDiamondCityPlayerTarget(
      CallbackInfo ci
  ) {
    this.targetSelector.addGoal(0, new DiamondCityGolemTargetGoal((AbstractGolem) (Object) this));
  }

}
