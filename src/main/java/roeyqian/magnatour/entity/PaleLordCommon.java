/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity;

// Minecraft
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.entity.supreme.PaleLordClone;

public final class PaleLordCommon {

  public static final float DAMAGE_THRESHOLD = 1_000_000.0F;

  public static final double ATTACK_MULTIPLIER = 2.0;
  public static final double MAX_ATTACK_DAMAGE = Integer.MAX_VALUE;
  public static final double SYNC_RADIUS = 256.0;

  private static final double PROJECTILE_LAUNCH_HEIGHT = 1.2;
  private static final double PROJECTILE_LAUNCH_SPEED = 6.0;

  private PaleLordCommon() {}

  public static void applyProjectileLaunch(
      LivingEntity entity,
      DamageSource source
  ) {
    Entity projectile = source.getDirectEntity();
    if (!entity.isAlive() || !(source.is(DamageTypeTags.IS_PROJECTILE) || projectile instanceof Projectile)) {
      return;
    }

    Vec3 direction = projectile != null ? projectile.getDeltaMovement() : Vec3.ZERO;
    direction = new Vec3(direction.x, 0.0, direction.z);
    if (direction.lengthSqr() < 1.0E-6 && source.getSourcePosition() != null) {
      Vec3 away = entity.position().subtract(source.getSourcePosition());
      direction = new Vec3(away.x, 0.0, away.z);
    }
    if (direction.lengthSqr() < 1.0E-6) {
      Vec3 look = entity.getLookAngle();
      direction = new Vec3(-look.x, 0.0, -look.z);
    }
    if (direction.lengthSqr() < 1.0E-6) {
      direction = new Vec3(0.0, 0.0, 1.0);
    }

    Vec3 launch = direction.normalize().scale(PROJECTILE_LAUNCH_SPEED);
    entity.setDeltaMovement(launch.x, PROJECTILE_LAUNCH_HEIGHT, launch.z);
    entity.syncVelocity = true;
  }

  public static double computeAttackDamage(
      int cloneCount
  ) {
    double damage = 1.0;
    for (int i = 0; i < cloneCount && damage < MAX_ATTACK_DAMAGE; i++) {
      damage *= ATTACK_MULTIPLIER;
      if (damage >= MAX_ATTACK_DAMAGE) {
        return MAX_ATTACK_DAMAGE;
      }
    }
    return damage;
  }

  public static int countNearbyClones(
      ServerLevel world,
      Entity center
  ) {
    return world.getEntitiesOfClass(
        PaleLordClone.class,
        center.getBoundingBox().inflate(SYNC_RADIUS),
        Entity::isAlive
    ).size();
  }

  public static boolean isImmuneToDamage(
      DamageSource source
  ) {
    return source.is(DamageTypeTags.IS_FIRE)
        || source.is(DamageTypes.MAGIC)
        || source.is(DamageTypes.INDIRECT_MAGIC)
        || source.is(DamageTypes.WITHER);
  }

  public static boolean isImmuneToEffect(
      MobEffectInstance effect
  ) {
    return effect.is(MobEffects.POISON)
        || effect.is(MobEffects.WITHER)
        || effect.is(MobEffects.INSTANT_DAMAGE);
  }

  public static SoundEvent resolveSound(
      String path,
      SoundEvent fallback
  ) {
    return BuiltInRegistries.SOUND_EVENT.getOptional(
        Identifier.fromNamespaceAndPath("minecraft", path)
    ).orElse(fallback);
  }

}
