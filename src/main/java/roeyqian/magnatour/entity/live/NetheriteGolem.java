/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.live;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.entity.CustomGolem;
import roeyqian.magnatour.entity.EntityLootTableHelper;

public class NetheriteGolem extends AbstractGolem implements NeutralMob {

  protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
      CustomGolem.createDataFlagsId(NetheriteGolem.class);

  private static final int FASTER_ATTACK_INTERVAL_TICKS = 10;

  private static final double PLAYER_PROTECTION_RANGE = 32.0F;

  private @Nullable CustomGolem<NetheriteGolem> customGolem;

  public NetheriteGolem(
      final EntityType<? extends NetheriteGolem> type,
      final Level level
  ) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return CustomGolem.createBaseAttributes(
        8000.0F, 0.6F, 2.0F,
        100.0F, 100.0F, 128.0F,1.75F
    );
  }

  @Override
  public void aiStep() {
    super.aiStep();
    this.customGolem().aiStep();
  }

  @Override
  public boolean canAttack(
      final @NonNull LivingEntity target
  ) {
    if (target.is(EntityTypes.PLAYER)) {
      return false;
    }

    return super.canAttack(target);
  }

  @Override
  public boolean canSpawnSprintParticle() {
    return this.customGolem().canSpawnSprintParticle();
  }

  @Override
  public boolean checkSpawnObstruction(
      final @NonNull LevelReader level
  ) {
    return this.customGolem().checkSpawnObstruction(level);
  }

  @Override
  public boolean doHurtTarget(
      final @NonNull ServerLevel level,
      final @NonNull Entity target
  ) {
    return this.customGolem().doHurtTarget(level, target);
  }

  public int getAttackAnimationTick() {
    return this.customGolem().getAttackAnimationTick();
  }

  public Crackiness.Level getCrackiness() {
    return this.customGolem().getCrackiness();
  }

  @Override
  public @NonNull Vec3 getLeashOffset() {
    return this.customGolem().getLeashOffset();
  }

  public int getOfferFlowerTick() {
    return this.customGolem().getOfferFlowerTick();
  }

  @Override
  public long getPersistentAngerEndTime() {
    return this.customGolem().getPersistentAngerEndTime();
  }

  @Override
  public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
    return this.customGolem().getPersistentAngerTarget();
  }

  @Override
  public void handleEntityEvent(
      final byte id
  ) {
    if (this.customGolem().handleEntityEvent(id)) {
      super.handleEntityEvent(id);
    }
  }

  @Override
  public boolean hurtServer(
      final @NonNull ServerLevel level,
      final @NonNull DamageSource source,
      final float damage
  ) {
    Crackiness.Level previousCrackiness = this.getCrackiness();
    boolean wasHurt = super.hurtServer(level, source, damage);
    return this.customGolem().afterHurt(previousCrackiness, wasHurt);
  }

  public boolean isNotPlayerCreated() {
    return !this.customGolem().isPlayerCreated();
  }

  @Override
  public void setPersistentAngerEndTime(
      final long endTime
  ) {
    this.customGolem().setPersistentAngerEndTime(endTime);
  }

  @Override
  public void setPersistentAngerTarget(
      final @Nullable EntityReference<LivingEntity> persistentAngerTarget
  ) {
    this.customGolem().setPersistentAngerTarget(persistentAngerTarget);
  }

  public void setPlayerCreated(
      final boolean value
  ) {
    this.customGolem().setPlayerCreated(value);
  }

  @Override
  public void setTarget(
      final @Nullable LivingEntity target
  ) {
    if (target == null) {
      super.setTarget(null);
      return;
    }

    if (!this.shouldTargetEntity(target)) {
      if (this.getTarget() == target) {
        super.setTarget(null);
      }

      return;
    }

    super.setTarget(target);
  }

  @Override
  public void startPersistentAngerTimer() {
    this.customGolem().startPersistentAngerTimer();
  }

  @Override
  protected void addAdditionalSaveData(
      final @NonNull ValueOutput output
  ) {
    super.addAdditionalSaveData(output);
    this.customGolem().addAdditionalSaveData(output);
  }

  @Override
  protected int decreaseAirSupply(
      final int currentSupply
  ) {
    return this.customGolem().decreaseAirSupply(currentSupply);
  }

  @Override
  protected void defineSynchedData(
      final SynchedEntityData.@NonNull Builder entityData
  ) {
    super.defineSynchedData(entityData);
    this.customGolem().defineSynchedData(entityData);
  }

  @Override
  protected void doPush(
      final @NonNull Entity entity
  ) {
    this.customGolem().handlePushTargeting(entity, this::shouldAttackOnSight);
    super.doPush(entity);
  }

  @Override
  protected void dropFromLootTable(
      final @NonNull ServerLevel world,
      final @NonNull DamageSource source,
      final boolean causedByPlayer
  ) {
    EntityLootTableHelper.dropMagnatourEntityLoot(this, world, source, causedByPlayer);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return this.customGolem().getDeathSound();
  }

  @Override
  protected SoundEvent getHurtSound(
      final @NonNull DamageSource source
  ) {
    return this.customGolem().getHurtSound();
  }

  @Override
  protected @NonNull InteractionResult mobInteract(
      final @NonNull Player player,
      final @NonNull InteractionHand hand
  ) {
    return this.customGolem().mobInteract(player, hand);
  }

  @Override
  protected void playStepSound(
      final @NonNull BlockPos pos,
      final @NonNull BlockState blockState
  ) {
    this.customGolem().playStepSound();
  }

  @Override
  protected void readAdditionalSaveData(
      final @NonNull ValueInput input
  ) {
    super.readAdditionalSaveData(input);
    this.customGolem().readAdditionalSaveData(input);
  }

  @Override
  protected void registerGoals() {
    this.customGolem().registerBaseGoals(
        this.goalSelector,
        this.customGolem().createFasterMeleeAttackGoal(FASTER_ATTACK_INTERVAL_TICKS)
    );
    this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
        this, LivingEntity.class, 5, false, false,
        (target, _) -> this.shouldProtectPlayersAgainst(target))
    );
    this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
        this, LivingEntity.class, 5, false, false,
        (target, _) -> this.shouldAttackOnSight(target))
    );
    this.customGolem().addUniversalAngerResetGoal(this.targetSelector, 4);
  }

  private CustomGolem<NetheriteGolem> customGolem() {
    if (this.customGolem == null) {
      this.customGolem = new CustomGolem<>(this, DATA_FLAGS_ID);
    }
    return this.customGolem;
  }

  private boolean shouldTargetEntity(
      final LivingEntity target
  ) {
    if (!this.canAttack(target)) {
      return false;
    }

    return target instanceof Enemy
        || this.isNotPlayerCreated()
        || this.shouldProtectPlayersAgainst(target);
  }

  private boolean shouldProtectPlayersAgainst(
      final LivingEntity target
  ) {
    if (this.isNotPlayerCreated() || !this.canAttack(target)) {
      return false;
    }

    for (Player player : this.level().getEntitiesOfClass(
        Player.class,
        this.getBoundingBox().inflate(PLAYER_PROTECTION_RANGE)
    )) {
      if (player.getLastHurtByMob() == target) {
        return true;
      }
    }

    return false;
  }

  private boolean shouldAttackOnSight(
      final LivingEntity target
  ) {
    return this.canAttack(target) && target instanceof Enemy;
  }

}
