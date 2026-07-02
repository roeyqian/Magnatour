/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.live;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.entity.EntityLootTableHelper;

public class TheUnnameableThing extends Mob {

  public TheUnnameableThing(
      EntityType<? extends Mob> entityType,
      Level world
  ) {
    super(entityType, world);
    this.setNoAi(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 100000)
        .add(Attributes.KNOCKBACK_RESISTANCE, 1)
        .add(Attributes.ARMOR, 0)
        .add(Attributes.ARMOR_TOUGHNESS, 8);
  }

  @Override
  public boolean hurtServer(
      @NonNull ServerLevel world,
      DamageSource source,
      float amount
  ) {
    if (source.getEntity() instanceof LivingEntity attacker
        && attacker.isAlive()
        && attacker != this
        && !source.is(DamageTypes.THORNS)
    ) {
      float reflectDamage = Math.max(0, amount * 2);
      if (reflectDamage > 0.0F) {
        attacker.hurtServer(world, this.damageSources().thorns(this), reflectDamage);
      }
    }

    return super.hurtServer(world, source, amount);
  }

  @Override
  protected void dropFromLootTable(
      @NonNull ServerLevel world,
      @NonNull DamageSource source,
      boolean causedByPlayer
  ) {
    EntityLootTableHelper.dropMagnatourEntityLoot(this, world, source, causedByPlayer);
  }

  @Override
  protected void registerGoals() {}

}
