/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity;

// Java Standard
import java.util.Objects;
import java.util.function.Predicate;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class CustomGolem<T extends AbstractGolem & NeutralMob> {

  private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

  private final T owner;

  private int attackAnimationTick;
  private int offerFlowerTick;

  private long persistentAngerEndTime;

  private final EntityDataAccessor<Byte> dataFlagsId;

  private @Nullable EntityReference<LivingEntity> persistentAngerTarget;

  public CustomGolem(
      final T owner,
      final EntityDataAccessor<Byte> dataFlagsId
  ) {
    this.owner = owner;
    this.dataFlagsId = dataFlagsId;
  }

  public static AttributeSupplier.Builder createBaseAttributes(
      final double maxHealth,
      final double movementSpeed,
      final double knockbackResistance,
      final double attackDamage,
      final double armor,
      final double followRange,
      final double stepHeight
  ) {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, maxHealth)
        .add(Attributes.MOVEMENT_SPEED, movementSpeed)
        .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
        .add(Attributes.ATTACK_DAMAGE, attackDamage)
        .add(Attributes.ARMOR, armor)
        .add(Attributes.FOLLOW_RANGE, followRange)
        .add(Attributes.STEP_HEIGHT, stepHeight);
  }

  public static <T extends Entity> EntityDataAccessor<Byte> createDataFlagsId(
      final Class<T> entityClass
  ) {
    return SynchedEntityData.defineId(entityClass, EntityDataSerializers.BYTE);
  }

  public void addAdditionalSaveData(
      final @NonNull ValueOutput output
  ) {
    output.putBoolean("PlayerCreated", this.isPlayerCreated());
    this.owner.addPersistentAngerSaveData(output);
  }

  public void addUniversalAngerResetGoal(
      final GoalSelector targetSelector,
      final int priority
  ) {
    targetSelector.addGoal(priority, new ResetUniversalAngerTargetGoal<>(this.owner, false));
  }

  public boolean afterHurt(
      final Crackiness.Level previousCrackiness,
      final boolean wasHurt
  ) {
    if (wasHurt && this.getCrackiness() != previousCrackiness) {
      this.owner.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
    }

    return wasHurt;
  }

  public void aiStep() {
    if (this.attackAnimationTick > 0) {
      --this.attackAnimationTick;
    }

    if (this.offerFlowerTick > 0) {
      --this.offerFlowerTick;
    }

    if (!this.owner.level().isClientSide()) {
      this.owner.updatePersistentAnger((ServerLevel) this.owner.level(), true);
    }
  }

  public boolean canSpawnSprintParticle() {
    return this.owner.getDeltaMovement().horizontalDistanceSqr() > (double) 2.5000003E-7F
        && this.owner.getRandom().nextInt(5) == 0;
  }

  public boolean checkSpawnObstruction(
      final LevelReader level
  ) {
    BlockPos pos = this.owner.blockPosition();
    BlockPos belowPos = pos.below();
    BlockState below = level.getBlockState(belowPos);
    if (!below.entityCanStandOn(level, belowPos, this.owner)) {
      return false;
    }

    for (int i = 1; i < 3; ++i) {
      BlockPos abovePos = pos.above(i);
      BlockState above = level.getBlockState(abovePos);
      if (!NaturalSpawner.isValidEmptySpawnBlock(
          level, abovePos, above, above.getFluidState(), this.owner.getType()
      )) {
        return false;
      }
    }

    return NaturalSpawner.isValidEmptySpawnBlock(
        level,
        pos,
        level.getBlockState(pos),
        Fluids.EMPTY.defaultFluidState(),
        this.owner.getType()
    ) && level.isUnobstructed(this.owner);
  }

  public MeleeAttackGoal createFasterMeleeAttackGoal(
      final int... attackIntervals
  ) {
    return new FasterMeleeAttackGoal(this.owner, 1.0F, true, attackIntervals);
  }

  public int decreaseAirSupply(
      final int currentSupply
  ) {
    return currentSupply;
  }

  public void defineSynchedData(
      final SynchedEntityData.@NonNull Builder entityData
  ) {
    entityData.define(this.dataFlagsId, (byte) 0);
  }

  public boolean doHurtTarget(
      final ServerLevel level,
      final Entity target
  ) {
    this.attackAnimationTick = 10;
    level.broadcastEntityEvent(this.owner, (byte) 4);
    float attackDamage = this.getAttackDamage();
    float damage = (int) attackDamage > 0
        ? attackDamage / 2.0F + (float) this.owner.getRandom().nextInt((int) attackDamage)
        : attackDamage;
    DamageSource damageSource = this.owner.damageSources().mobAttack(this.owner);
    boolean hurt = target.hurtServer(level, damageSource, damage);
    if (hurt) {
      double knockbackResistance = target instanceof LivingEntity livingEntity
          ? livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)
          : 0.0F;
      double scale = Math.max(0.0F, (double) 1.0F - knockbackResistance);
      target.setDeltaMovement(target.getDeltaMovement().add(0.0F, (double) 0.4F * scale, 0.0F));
      EnchantmentHelper.doPostAttackEffects(level, target, damageSource);
    }

    this.owner.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
    return hurt;
  }

  public int getAttackAnimationTick() {
    return this.attackAnimationTick;
  }

  public Crackiness.Level getCrackiness() {
    return Crackiness.GOLEM.byFraction(this.owner.getHealth() / this.owner.getMaxHealth());
  }

  public SoundEvent getDeathSound() {
    return SoundEvents.IRON_GOLEM_DEATH;
  }

  public SoundEvent getHurtSound() {
    return SoundEvents.IRON_GOLEM_HURT;
  }

  public @NonNull Vec3 getLeashOffset() {
    return new Vec3(0.0F, 0.875F * this.owner.getEyeHeight(), this.owner.getBbWidth() * 0.4F);
  }

  public int getOfferFlowerTick() {
    return this.offerFlowerTick;
  }

  public long getPersistentAngerEndTime() {
    return this.persistentAngerEndTime;
  }

  public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
    return this.persistentAngerTarget;
  }

  public boolean handleEntityEvent(
      final byte id
  ) {
    if (id == 4) {
      this.attackAnimationTick = 10;
      this.owner.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      return false;
    }

    if (id == 11) {
      this.offerFlowerTick = 400;
      return false;
    }

    if (id == 34) {
      this.offerFlowerTick = 0;
      return false;
    }

    return true;
  }

  public void handlePushTargeting(
      final @NonNull Entity entity,
      final Predicate<LivingEntity> shouldAttackOnSight
  ) {
    if (entity instanceof LivingEntity livingEntity
        && shouldAttackOnSight.test(livingEntity)
        && this.owner.getRandom().nextInt(20) == 0) {
      this.owner.setTarget(livingEntity);
    }
  }

  public boolean isPlayerCreated() {
    return (this.owner.getEntityData().get(this.dataFlagsId) & 1) != 0;
  }

  public @NonNull InteractionResult mobInteract(
      final Player player,
      final @NonNull InteractionHand hand
  ) {
    ItemStack itemStack = player.getItemInHand(hand);
    if (!itemStack.is(Items.IRON_INGOT)) {
      return InteractionResult.PASS;
    }

    float healthBefore = this.owner.getHealth();
    this.owner.heal(25.0F);
    if (this.owner.getHealth() == healthBefore) {
      return InteractionResult.PASS;
    }

    float pitch = 1.0F + (this.owner.getRandom().nextFloat() - this.owner.getRandom().nextFloat()) * 0.2F;
    this.owner.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, pitch);
    itemStack.consume(1, player);
    return InteractionResult.SUCCESS;
  }

  public void playStepSound() {
    this.owner.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
  }

  public void readAdditionalSaveData(
      final @NonNull ValueInput input
  ) {
    this.setPlayerCreated(input.getBooleanOr("PlayerCreated", false));
    this.owner.readPersistentAngerSaveData(this.owner.level(), input);
  }

  public void registerBaseGoals(
      final GoalSelector goalSelector,
      final MeleeAttackGoal meleeAttackGoal
  ) {
    goalSelector.addGoal(1, meleeAttackGoal);
    goalSelector.addGoal(2, new MoveTowardsTargetGoal(this.owner, 0.9, 32.0F));
    goalSelector.addGoal(2, new MoveBackToVillageGoal(this.owner, 0.6, false));
    goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this.owner, 0.6));
    goalSelector.addGoal(7, new LookAtPlayerGoal(this.owner, Player.class, 6.0F));
    goalSelector.addGoal(8, new RandomLookAroundGoal(this.owner));
  }

  public void setPersistentAngerEndTime(
      final long endTime
  ) {
    this.persistentAngerEndTime = endTime;
  }

  public void setPersistentAngerTarget(
      final @Nullable EntityReference<LivingEntity> persistentAngerTarget
  ) {
    this.persistentAngerTarget = persistentAngerTarget;
  }

  public void setPlayerCreated(
      final boolean value
  ) {
    byte current = this.owner.getEntityData().get(this.dataFlagsId);
    if (value) {
      this.owner.getEntityData().set(this.dataFlagsId, (byte) (current | 1));
    } else {
      this.owner.getEntityData().set(this.dataFlagsId, (byte) (current & -2));
    }
  }

  public void startPersistentAngerTimer() {
    this.owner.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.owner.getRandom()));
  }

  private float getAttackDamage() {
    return (float) this.owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
  }

  private static final class FasterMeleeAttackGoal extends MeleeAttackGoal {

    private int attackCooldown;
    private int attackPatternIndex;

    private final int[] attackIntervals;

    private FasterMeleeAttackGoal(
        final AbstractGolem mob,
        final double speedModifier,
        final boolean followingTargetEvenIfNotSeen,
        final int... attackIntervals
    ) {
      super(mob, speedModifier, followingTargetEvenIfNotSeen);
      this.attackIntervals = Objects.requireNonNull(attackIntervals, "attackIntervals").clone();
      if (this.attackIntervals.length == 0) {
        throw new IllegalArgumentException("attackIntervals must not be empty");
      }
    }

    @Override
    public void start() {
      super.start();
      this.attackCooldown = 0;
      this.attackPatternIndex = 0;
    }

    @Override
    public void stop() {
      super.stop();
      this.attackCooldown = 0;
      this.attackPatternIndex = 0;
    }

    @Override
    public void tick() {
      if (this.attackCooldown > 0) {
        --this.attackCooldown;
      }

      super.tick();
    }

    @Override
    protected void checkAndPerformAttack(
        final @NonNull LivingEntity target
    ) {
      if (this.attackCooldown > 0
          || !this.mob.isWithinMeleeAttackRange(target)
          || !this.mob.getSensing().hasLineOfSight(target)) {
        return;
      }

      this.attackCooldown = this.adjustedTickDelay(this.attackIntervals[this.attackPatternIndex]);
      this.attackPatternIndex = (this.attackPatternIndex + 1) % this.attackIntervals.length;
      this.mob.swing(InteractionHand.MAIN_HAND);
      if (this.mob.level() instanceof ServerLevel serverLevel) {
        this.mob.doHurtTarget(serverLevel, target);
      }
    }

  }

}
