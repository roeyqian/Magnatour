/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public interface CustomBossEntity {

  int DEFAULT_MAX_EXPERIENCE_ORB_VALUE = 37;

  static void spawnExperienceOrbs(
      ServerLevel world,
      Vec3 position,
      int experience,
      int maxOrbValue
  ) {
    int cappedOrbValue = Math.max(1, maxOrbValue);
    int remainingExperience = experience;

    while (remainingExperience > 0) {
      int orbValue = ExperienceOrb.getExperienceValue(
          Math.min(remainingExperience, cappedOrbValue)
      );
      world.addFreshEntity(
          new ExperienceOrb(
              world,
              position.x,
              position.y,
              position.z,
              orbValue
          )
      );
      remainingExperience -= orbValue;
    }
  }

  default int maxExperienceOrbValue() {
    return DEFAULT_MAX_EXPERIENCE_ORB_VALUE;
  }

  default void dropExperienceAsSmallOrbs(
      LivingEntity entity,
      ServerLevel world,
      Entity attacker,
      boolean alwaysDropExperience
  ) {
    if (entity.wasExperienceConsumed()) {
      return;
    }

    if (!alwaysDropExperience) {
      if (entity.getLastHurtByPlayerMemoryTime() <= 0 || !entity.shouldDropExperience()) {
        return;
      }
      if (!world.getGameRules().get(GameRules.MOB_DROPS)) {
        return;
      }
    }

    spawnExperienceOrbs(
        world,
        entity.position(),
        entity.getExperienceReward(world, attacker),
        this.maxExperienceOrbValue()
    );
  }

}
