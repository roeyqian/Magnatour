/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.entity.EntityHelperForEquipment;

@Mixin(value = LivingEntity.class, priority = 3600000)
public abstract class LivingEntityMixin {

  @Unique
  private int flightTicks = 0;

  /* Universe Chestplate & Universe Leggings: Fast-Flying
   */
  @Inject(method = "baseTick", at = @At("TAIL"))
  private void inBaseTick(
      CallbackInfo ci
  ) {
    this.flightTicks = EntityHelperForEquipment.handleLivingBaseTick(
        (LivingEntity) (Object) this,
        this.flightTicks
    );
  }

  /* Universe Helmet: Immunity to Negative Visual & Food Effects
   */
  @Inject(method = "forceAddEffect", at = @At("HEAD"), cancellable = true)
  private void inForceAddEffect(
      MobEffectInstance effect,
      Entity source,
      CallbackInfo ci
  ) {
    EntityHelperForEquipment.handleLivingForceAddEffect(
        (LivingEntity) (Object) this,
        effect,
        ci
    );
  }

  /* Universe Ultima Sword: Absolute Strike
   */
  @Inject(method = "hurtServer", at = @At("TAIL"))
  private void inHurtServer(
      ServerLevel world,
      DamageSource source,
      float amount,
      CallbackInfoReturnable<Boolean> cir
  ) {
    EntityHelperForEquipment.handleUniverseHitEffect(
        (LivingEntity) (Object) this,
        cir.getReturnValue(),
        source
    );
  }

  /* Universe Leggings: Smooth Movement
   */
  @Inject(method = "tick", at = @At("HEAD"))
  private void inTick(
      CallbackInfo ci
  ) {
    EntityHelperForEquipment.handleUniverseLeggingsStepping((LivingEntity) (Object) this);
  }

}
