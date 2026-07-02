/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.dead;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

// Magnatour
import roeyqian.magnatour.utility.registry.item.RegConsumableItems;

public class ThrownStrangeLingeringPotion extends ThrownLingeringPotion {

  public ThrownStrangeLingeringPotion(
      EntityType<? extends ThrownLingeringPotion> type,
      Level level
  ) {
    super(type, level);
  }

  public ThrownStrangeLingeringPotion(
      Level level,
      LivingEntity owner,
      ItemStack stack
  ) {
    super(level, owner, stack);
    this.setItem(stack);
  }

  public ThrownStrangeLingeringPotion(
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
    return RegConsumableItems.STRANGE_LINGERING_POTION;
  }

  @Override
  protected void onHit(
      HitResult hitResult
  ) {
    if (this.level().isClientSide()) {
      return;
    }

    ServerLevel level = (ServerLevel) this.level();

    // 创建自定义的药水云
    StrangeAreaEffectCloud cloud = new StrangeAreaEffectCloud(level, this.getX(), this.getY(), this.getZ());

    if (this.getOwner() instanceof LivingEntity owner) {
      cloud.setOwner(owner);
    }

    cloud.setRadius(3.0F);
    cloud.setRadiusOnUse(-0.5F);
    cloud.setDuration(600);       // 30 秒
    cloud.setWaitTime(10);
    cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());

    level.addFreshEntity(cloud);

    // 破碎粒子
    level.levelEvent(2007, this.blockPosition(), 0x8A2BE2);

    this.discard();
  }

}
