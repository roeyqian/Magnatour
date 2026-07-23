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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

// Magnatour
import roeyqian.magnatour.item.StrangePotionEffect;

public class StrangeAreaEffectCloud extends AreaEffectCloud {

  private int applyInterval = 10;
  private int tickCounter = 0;

  public StrangeAreaEffectCloud(
      EntityType<? extends AreaEffectCloud> type,
      Level level
  ) {
    super(type, level);
  }

  public StrangeAreaEffectCloud(
      Level level,
      double x,
      double y,
      double z
  ) {
    super(level, x, y, z);
  }

  @Override
  public void tick() {
    // 让 vanilla 的 tick 处理半径缩小、存活时间等
    super.tick();

    if (this.level().isClientSide()) {
      return;
    }

    tickCounter++;

    if (tickCounter < applyInterval) {
      return;
    }

    tickCounter = 0;

    float radius = this.getRadius();
    if (radius <= 0.0F) {
      return;
    }

    // 在云的范围内找实体
    AABB area = new AABB(
        this.getX() - radius, this.getY(), this.getZ() - radius,
        this.getX() + radius, this.getY() + 2.0D, this.getZ() + radius
    );

    List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);

    for (LivingEntity target : entities) {
      if (!target.isAffectedByPotions()) {
        continue;
      }

      // 检查是否真的在圆形范围内（不是方形）
      double dx = target.getX() - this.getX();
      double dz = target.getZ() - this.getZ();
      double distSqr = dx * dx + dz * dz;

      if (distSqr <= (double) (radius * radius)) {
        StrangePotionEffect.applyRandomEffects(target, this.getRandom());

        // 每次施加效果后缩小半径（和原版一样）
        this.setRadius(this.getRadius() + this.getRadiusOnUse());
        if (this.getRadius() <= 0.5F) {
          this.discard();
          return;
        }
      }
    }
  }

}
