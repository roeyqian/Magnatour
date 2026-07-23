/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.entity.EntityHelperForCreature;

@Mixin(value = RangedAttribute.class, priority = 3600000)
public abstract class RangedAttributeMixin {

  @Shadow @Final @Mutable
  private double maxValue;

  /* Universe Equipment: Extend Vanilla Attribute Limits
   */
  @Inject(method = "<init>", at = @At("TAIL"))
  private void inInit(
      String descriptionId,
      double defaultValue,
      double min,
      double max,
      CallbackInfo ci
  ) {
    this.maxValue = EntityHelperForCreature.extendAttributeLimit(descriptionId, this.maxValue);
  }

}
