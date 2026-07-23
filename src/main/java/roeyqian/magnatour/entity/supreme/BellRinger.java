/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.supreme;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.entity.EntityLootTableHelper;

public class BellRinger extends Monster {

  public BellRinger(
      EntityType<? extends Monster> entityType,
      Level world
  ) {
    super(entityType, world);
    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
    this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    this.setCanPickUpLoot(false);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 500.0F)
        .add(Attributes.ATTACK_DAMAGE, 50.0F)
        .add(Attributes.MOVEMENT_SPEED, 0.4F)
        .add(Attributes.FOLLOW_RANGE, 48.0F)
        .add(Attributes.ARMOR, 20.0F)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8F);
  }

  @Override
  public boolean canAttack(
      @NonNull LivingEntity target
  ) {
    if (target instanceof BellRinger || target instanceof BellSoul) return false;
    return super.canAttack(target);
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
  protected @NonNull SoundEvent getAmbientSound() {
    return SoundEvents.VINDICATOR_AMBIENT;
  }

  @Override
  protected @NonNull SoundEvent getDeathSound() {
    return SoundEvents.VINDICATOR_DEATH;
  }

  @Override
  protected @NonNull SoundEvent getHurtSound(
      @NonNull DamageSource source
  ) {
    return SoundEvents.VINDICATOR_HURT;
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
    this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
    this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
    this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

    this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    this.targetSelector.addGoal(
        2,
        new NearestAttackableTargetGoal<>(
            this, LivingEntity.class,
            10, true, false,
            (entity, _) -> !(entity instanceof Player) && !(entity instanceof BellRinger) && !(entity instanceof BellSoul)
        )
    );
  }

}
