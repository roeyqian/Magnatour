/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity;

// Minecraft
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

// Magnatour
import roeyqian.magnatour.entity.live.PaleLordClone;

public final class PaleLordCommon {

  public static final float DAMAGE_THRESHOLD = 1_000_000.0F;

  public static final double ATTACK_MULTIPLIER = 3.0;
  public static final double MAX_ATTACK_DAMAGE = Integer.MAX_VALUE;
  public static final double SYNC_RADIUS = 256.0;

  private PaleLordCommon() {}

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

  public static SoundEvent resolveSound(
      String path,
      SoundEvent fallback
  ) {
    return BuiltInRegistries.SOUND_EVENT.getOptional(
        Identifier.fromNamespaceAndPath("minecraft", path)
    ).orElse(fallback);
  }

}
