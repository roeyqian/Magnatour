/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.supreme;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

// Magnatour
import roeyqian.magnatour.item.StrangePotionEffect;
import roeyqian.magnatour.registry.content.SupremeItems;

public class ThrownStrangeSplashPotion extends ThrownSplashPotion {

  public ThrownStrangeSplashPotion(
      EntityType<? extends ThrownSplashPotion> type,
      Level level
  ) {
    super(type, level);
  }

  public ThrownStrangeSplashPotion(
      Level level,
      LivingEntity owner,
      ItemStack stack
  ) {
    super(level, owner, stack);
    this.setItem(stack);
  }

  public ThrownStrangeSplashPotion(
      Level level,
      double x,
      double y,
      double z,
      ItemStack stack
  ) {
    super(level, x, y, z, stack);
    this.setItem(stack);
  }

  @Override
  protected Item getDefaultItem() {
    return SupremeItems.STRANGE_SPLASH_POTION;
  }

  @Override
  protected void onHit(
      HitResult hitResult
  ) {
    if (this.level().isClientSide()) {
      return;
    }

    ServerLevel level = (ServerLevel) this.level();

    System.out.println("[ThrownStrangeSplashPotion] onHit called");

    AABB potionAabb = this.getBoundingBox().move(
        hitResult.getLocation().subtract(this.position())
    );

    AABB effectAabb = potionAabb.inflate(4.0D, 2.0D, 4.0D);
    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectAabb);

    System.out.println("[ThrownStrangeSplashPotion] nearby entities = " + entities.size());

    float margin = ProjectileUtil.computeMargin(this);

    for (LivingEntity target : entities) {
      if (!target.isAffectedByPotions()) {
        continue;
      }

      double distSqr = potionAabb.distanceToSqr(
          target.getBoundingBox().inflate(margin)
      );

      System.out.println("[ThrownStrangeSplashPotion] check target = "
          + target.getName().getString() + ", distSqr = " + distSqr);

      // 原版喷溅药水半径判定基本就是 4 格内
      if (distSqr < 16.0D) {
        System.out.println("[ThrownStrangeSplashPotion] find! " + target.getName().getString());
        StrangePotionEffect.applyRandomEffects(target, this.getRandom());
      }
    }

    // 2002 = 喷溅药水破碎粒子事件
    // 最后一个参数是颜色值，可以随便给一个你想要的颜色
    level.levelEvent(2002, this.blockPosition(), 0x8A2BE2);

    this.discard();
  }

}
