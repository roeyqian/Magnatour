/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.supreme;

// Minecraft
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EmperorBow extends BowItem {

  public static final int DRAW_SPEED_MULTIPLIER = 2;
  public static final int MIN_DRAW_TICKS = 2;

  public static final float PROJECTILE_SPEED_MULTIPLIER = 2.0F;

  public static final double MIN_SHOT_DAMAGE = 100.0;
  // Compensate for the faster launch so the weakest unenchanted shot stays at about 100.
  public static final double ARROW_BASE_DAMAGE = MIN_SHOT_DAMAGE
      / (3.0 * BowItem.getPowerForTime(MIN_DRAW_TICKS * DRAW_SPEED_MULTIPLIER))
      / PROJECTILE_SPEED_MULTIPLIER;

  public EmperorBow(
      Properties settings
  ) {
    super(settings);
  }

  @Override
  public boolean releaseUsing(
      @NonNull ItemStack stack,
      @NonNull Level world,
      @NonNull LivingEntity shooter,
      int timeLeft
  ) {
    int duration = getUseDuration(stack, shooter);
    int drawnTicks = duration - timeLeft;
    // Reuse vanilla ammo, enchantments, critical hits, sounds and statistics.
    return super.releaseUsing(stack, world, shooter,
        duration - drawnTicks * DRAW_SPEED_MULTIPLIER);
  }

  @Override
  protected @NonNull Projectile createProjectile(
      @NonNull Level world,
      @NonNull LivingEntity shooter,
      @NonNull ItemStack weapon, @NonNull ItemStack ammunition,
      boolean critical
  ) {
    Projectile projectile = super.createProjectile(world, shooter, weapon, ammunition, critical);
    if (projectile instanceof AbstractArrow arrow) {
      // Retain vanilla speed-based damage and Power enchantments on impact.
      arrow.setBaseDamage(ARROW_BASE_DAMAGE);
    }
    return projectile;
  }

  @Override
  protected void shootProjectile(
      @NonNull LivingEntity shooter,
      @NonNull Projectile projectile,
      int index,
      float velocity, float inaccuracy, float angle,
      @Nullable LivingEntity target
  ) {
    super.shootProjectile(shooter, projectile, index,
        velocity * PROJECTILE_SPEED_MULTIPLIER, inaccuracy, angle, target);
  }

}
