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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.entity.EntityHelperForEquipment;
import roeyqian.magnatour.utility.registry.item.RegDurableItems;

@Mixin(value = LivingEntity.class, priority = 3600000)
public abstract class LivingEntityMixin {

  @Unique
  private int flightTicks = 0;

  @Shadow
  protected abstract SoundEvent getDeathSound();

  /* Universe Chestplate & Universe Leggings: Fast-Flying
   */
  @Inject(method = "baseTick", at = @At("TAIL"))
  private void inBaseTick(
      CallbackInfo ci
  ) {
    int result;
    if (!((LivingEntity) (Object) this instanceof Player player)) {
      result = this.flightTicks;
    } else {
      float defaultSpeed = new Abilities().getFlyingSpeed();
      if (player.getItemBySlot(EquipmentSlot.CHEST).is(RegDurableItems.UNIVERSE_CHESTPLATE)) {
        result = EntityHelperForEquipment.handleUniverseFlight(player, this.flightTicks);
      } else {
        if (player.getAbilities().getFlyingSpeed() != defaultSpeed) {
          EntityHelperForEquipment.handleDefaultFlight(player, defaultSpeed);
        }
        result = this.flightTicks;
      }
    }

    this.flightTicks = result;
  }

  /* Universe Helmet: Immunity to Negative Visual & Food Effects
   */
  @Inject(method = "forceAddEffect", at = @At("HEAD"), cancellable = true)
  private void inForceAddEffect(
      MobEffectInstance effect,
      Entity source,
      CallbackInfo ci
  ) {
    if (!((LivingEntity) (Object) this instanceof Player player)) {
      return;
    }
    EntityHelperForEquipment.handleUniverseHelmetImmunity(player, effect, ci);
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
