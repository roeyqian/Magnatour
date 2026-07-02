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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.entity.EntityLootTableHelper;
import roeyqian.magnatour.entity.PaleLordCommon;

public class PaleLordClone extends Monster {

  private static final SoundEvent AMBIENT_SOUND = PaleLordCommon.resolveSound(
      "entity.creaking.ambient",
      SoundEvents.CREAKING_AMBIENT
  );
  private static final SoundEvent DEATH_SOUND = PaleLordCommon.resolveSound(
      "entity.creaking.death",
      SoundEvents.CREAKING_DEATH
  );
  private static final SoundEvent HURT_SOUND = PaleLordCommon.resolveSound(
      "entity.creaking.hurt",
      SoundEvents.SKELETON_HURT
  );

  public PaleLordClone(
      EntityType<? extends Monster> entityType,
      Level world
  ) {
    super(entityType, world);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 1.0)
        .add(Attributes.ATTACK_DAMAGE, 1.0)
        .add(Attributes.MOVEMENT_SPEED, 0.25)
        .add(Attributes.FOLLOW_RANGE, 40.0)
        .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
  }

  @Override
  public void checkDespawn() {}

  @Override
  public boolean hurtServer(
      @NonNull ServerLevel world,
      @NonNull DamageSource source,
      float amount
  ) {
    if (amount < PaleLordCommon.DAMAGE_THRESHOLD) {
      return false;
    }

    return super.hurtServer(world, source, amount);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide() || !(this.level() instanceof ServerLevel world)) {
      return;
    }

    if (this.tickCount % 10 == 0) {
      updateAttackDamage(world);
      ensureBodyExists(world);
    }
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
    return AMBIENT_SOUND;
  }

  @Override
  protected @NonNull SoundEvent getDeathSound() {
    return DEATH_SOUND;
  }

  @Override
  protected @NonNull SoundEvent getHurtSound(
      @NonNull DamageSource source
  ) {
    return HURT_SOUND;
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
    this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
    this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
    this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
  }

  private void updateAttackDamage(
      ServerLevel world
  ) {
    int cloneCount = PaleLordCommon.countNearbyClones(world, this);
    AttributeInstance attribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
    if (attribute != null) {
      attribute.setBaseValue(PaleLordCommon.computeAttackDamage(cloneCount));
    }
  }

  private void ensureBodyExists(
      ServerLevel world
  ) {
    boolean hasBody = !world.getEntitiesOfClass(
        PaleLord.class,
        this.getBoundingBox().inflate(PaleLordCommon.SYNC_RADIUS),
        Entity::isAlive
    ).isEmpty();

    if (!hasBody) {
      this.discard();
    }
  }

}
