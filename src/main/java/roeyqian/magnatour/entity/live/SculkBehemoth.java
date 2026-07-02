/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.live;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.entity.CustomBossEntity;
import roeyqian.magnatour.entity.EntityLootTableHelper;

public class SculkBehemoth extends Mob implements Enemy, CustomBossEntity {

  private static final int EXPERIENCE_REWARD = 5000;
  private static final int MOVE_BACK_DISTANCE = 15;
  private static final int MOVE_FORWARD_DISTANCE = 35;
  private static final int REGEN_INTERVAL = 40;

  private static final float FIRE_DAMAGE_MULTIPLIER = 5.0F;
  private static final float REGEN_PERCENT = 0.02F;
  private static final float REGEN_THRESHOLD = 0.5F;

  private static final double CHARGE_MOVEMENT_SEGMENT = 0.75;
  private static final double CHARGE_SPEED = 2.5;
  private static final double CHARGE_STEP_ASSIST_CLEARANCE = 0.05;
  private static final double CHARGE_STEP_ASSIST_HEIGHT = 1.25;
  private static final double CHARGE_STEP_ASSIST_INCREMENT = 0.25;
  private static final double MOVEMENT_EPSILON = 1.0E-4;

  private static final EntityDataAccessor<Integer> PHASE_TYPE = SynchedEntityData.defineId(
      SculkBehemoth.class, EntityDataSerializers.INT
  );

  private int chargeStunTimer = 0;
  private int phaseTicks = 0;
  private int regenCooldown = 0;
  private int smashAttackCount = 0;
  private int smashStateTicks = 0;
  private int sonicBoomCooldown = 0;

  private Phase currentPhase = Phase.IDLE;

  private boolean chargeHit = false;

  private SmashState smashState = SmashState.JUMPING;

  private Vec3 chargeDirection = null;
  private Vec3 smashTargetEntityPos = null;

  private final ServerBossEvent bossBar;

  public SculkBehemoth(
      EntityType<? extends Mob> entityType,
      Level world
  ) {
    super(entityType, world);

    this.bossBar = new ServerBossEvent(
        this.getUUID(),
        Component.translatable("entity.magnatour.sculk_behemoth"),
        BossEvent.BossBarColor.BLUE,
        BossEvent.BossBarOverlay.NOTCHED_10
    );

    this.xpReward = EXPERIENCE_REWARD;
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 20000F)
        .add(Attributes.MOVEMENT_SPEED, 0.4F)
        .add(Attributes.ATTACK_DAMAGE, 100.0F)
        .add(Attributes.ARMOR, 100.0F)
        .add(Attributes.KNOCKBACK_RESISTANCE, 10.0F)
        .add(Attributes.STEP_HEIGHT, 2.0F)
        .add(Attributes.FOLLOW_RANGE, 128.0F);
  }

  @Override
  public boolean addEffect(
      @NonNull MobEffectInstance effect,
      Entity source
  ) {
    return false;
  }

  @Override
  public boolean canBeHitByProjectile() {
    return true;
  }

  @Override
  public boolean canUsePortal(
      boolean allowVehicles
  ) {
    return false;
  }

  @Override
  public void checkDespawn() {}

  public int getPhaseType() {
    return this.entityData.get(PHASE_TYPE);
  }

  @Override
  public boolean hurtServer(
      @NonNull ServerLevel world,
      DamageSource source,
      float amount
  ) {
    if (source.is(DamageTypeTags.IS_FALL)) return false;
    if (source.is(DamageTypeTags.IS_FIRE) || isOnFire() || isInLava()) {
      amount *= FIRE_DAMAGE_MULTIPLIER;
    }
    return super.hurtServer(world, source, amount);
  }

  @Override
  public boolean isPickable() {
    return true;
  }

  @Override
  public boolean isPushable() {
    return false;
  }

  @Override
  public void knockback(
      double strength,
      double x,
      double z
  ) {}

  @Override
  public void push(
      double deltaX,
      double deltaY,
      double deltaZ
  ) {}

  @Override
  public void setCustomName(
      Component name
  ) {
    super.setCustomName(name);
    bossBar.setName(getDisplayName());
  }

  @Override
  public void startSeenByPlayer(
      @NonNull ServerPlayer player
  ) {
    super.startSeenByPlayer(player);
    bossBar.addPlayer(player);
  }

  @Override
  public void stopSeenByPlayer(
      @NonNull ServerPlayer player
  ) {
    super.stopSeenByPlayer(player);
    bossBar.removePlayer(player);
  }

  @Override
  public void tick() {
    super.tick();

    if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverWorld) {
      this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
      tickPhase(serverWorld);
      tickRegeneration(serverWorld);
    }

    if (this.level().isClientSide()) {
      spawnAmbientParticles();
    }
  }

  @Override
  protected void addAdditionalSaveData(
      @NonNull ValueOutput view
  ) {
    super.addAdditionalSaveData(view);
    view.putInt("Phase", currentPhase.getId());
  }

  @Override
  protected void defineSynchedData(
      SynchedEntityData.@NonNull Builder builder
  ) {
    super.defineSynchedData(builder);
    builder.define(PHASE_TYPE, Phase.IDLE.getId());
  }

  @Override
  protected void doPush(
      @NonNull Entity entity
  ) {}

  @Override
  protected void dropExperience(
      @NonNull ServerLevel world,
      Entity attacker
  ) {
    this.dropExperienceAsSmallOrbs(
        this,
        world,
        attacker,
        this.isAlwaysExperienceDropper()
    );
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
  protected SoundEvent getAmbientSound() {
    return SoundEvents.WARDEN_AMBIENT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.WARDEN_DEATH;
  }

  @Override
  protected SoundEvent getHurtSound(
      @NonNull DamageSource source
  ) {
    return SoundEvents.WARDEN_HURT;
  }

  @Override
  protected float getSoundVolume() {
    return 4.0F;
  }

  @Override
  protected void readAdditionalSaveData(
      @NonNull ValueInput view
  ) {
    super.readAdditionalSaveData(view);
    currentPhase = Phase.fromId(view.getIntOr("Phase", 0));
    if (hasCustomName()) bossBar.setName(getDisplayName());
  }

  @Override
  protected void registerGoals() {
    this.targetSelector.addGoal(
        1,
        new NearestAttackableTargetGoal<>(
            this, LivingEntity.class,
            10, true, false,
            (entity, _) -> !(entity instanceof SculkBehemoth)
        )
    );
    this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 32.0F));
    this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
  }

  private static double horizontalDistanceSqr(
      Vec3 movement
  ) {
    return movement.x * movement.x + movement.z * movement.z;
  }

  private void tickPhase(
      ServerLevel world
  ) {
    phaseTicks++;
    LivingEntity target = getTarget();

    switch (currentPhase) {
      case IDLE -> tickIdle(target);
      case CHARGE -> tickCharge(world, target);
      case SONIC_BOOM -> tickSonicBoom(world, target);
      case SMASH -> tickSmash(world, target);
    }
  }

  private void tickRegeneration(
      ServerLevel world
  ) {
    if (getHealth() < getMaxHealth() * REGEN_THRESHOLD && getHealth() < getMaxHealth()) {
      regenCooldown--;
      if (regenCooldown <= 0) {
        heal(getMaxHealth() * REGEN_PERCENT);
        regenCooldown = REGEN_INTERVAL;
        world.sendParticles(ParticleTypes.HEART, getX(), getY() + getBbHeight(), getZ(),
            5, 0.5, 0.5, 0.5, 0.1);
      }
    }
  }

  private void spawnAmbientParticles() {
    level().addParticle(
        ParticleTypes.SCULK_SOUL,
        getX() + (random.nextDouble() - 0.5) * getBbWidth(),
        getY() + random.nextDouble() * getBbHeight(),
        getZ() + (random.nextDouble() - 0.5) * getBbWidth(),
        0, 0.05, 0
    );
  }

  private void tickIdle(
      LivingEntity target
  ) {
    if (target != null && target.isAlive()) {
      Vec3 dir = target.position().subtract(position()).normalize();
      setVelocityInternal(dir.x * 0.3, getDeltaMovement().y, dir.z * 0.3);
      setYRot((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)));
      setYBodyRot(getYRot());
    }

    if (phaseTicks >= 15 && target != null) {
      selectNextPhase();
    }
  }

  private void tickCharge(
      ServerLevel world,
      LivingEntity target
  ) {
    if (chargeStunTimer > 0) {
      chargeStunTimer--;
      if (chargeStunTimer <= 0) {
        chargeHit = false;
        if (target != null) {
          chargeDirection = target.position().subtract(position()).normalize();
          playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 0.5F);
        }
      }
      return;
    }

    if (target == null || chargeDirection == null) {
      selectNextPhase();
      return;
    }

    if (tickCount % 3 == 0) {
      chargeDirection = target.position().subtract(position()).normalize();
    }

    setYRot((float) Math.toDegrees(Math.atan2(-chargeDirection.x, chargeDirection.z)));
    setYBodyRot(getYRot());

    Vec3 chargeVelocity = new Vec3(chargeDirection.x * CHARGE_SPEED, 0, chargeDirection.z * CHARGE_SPEED);
    setVelocityInternal(chargeVelocity);
    moveChargeWithStepAssist(chargeVelocity);

    for (int i = 0; i < 5; i++) {
      world.sendParticles(
          ParticleTypes.SOUL_FIRE_FLAME,
          getX() + (random.nextDouble() - 0.5) * getBbWidth(),
          getY() + random.nextDouble() * getBbHeight(),
          getZ() + (random.nextDouble() - 0.5) * getBbWidth(),
          1, 0, 0, 0, 0.05
      );
    }

    if (!chargeHit) {
      List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
          getBoundingBox().inflate(1.5), e -> e != this && e.isAlive());

      for (LivingEntity entity : entities) {
        entity.hurtServer(world, damageSources().mobAttack(this), 200.0F);
        Vec3 knockback = chargeDirection.scale(4.0).add(0, 2.0, 0);
        entity.setDeltaMovement(knockback);
        entity.hurtMarked = true;

        chargeHit = true;
        chargeStunTimer = 60;
        setVelocityInternal(Vec3.ZERO);
        playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 0.8F);
        world.sendParticles(
            ParticleTypes.EXPLOSION,
            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
            15, 0.5, 0.5, 0.5, 0.1
        );
        break;
      }
    }

    if (phaseTicks > 120 || (chargeHit && chargeStunTimer <= 0)) {
      selectNextPhase();
    }
  }

  private void tickSonicBoom(
      ServerLevel world,
      LivingEntity target
  ) {
    if (target == null) {
      setVelocityInternal(0, getDeltaMovement().y, 0);
      selectNextPhase();
      return;
    }

    double dx = target.getX() - getX();
    double dz = target.getZ() - getZ();
    double distance = Math.sqrt(dx * dx + dz * dz);

    float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
    float yawDiff = targetYaw - getYRot();
    while (yawDiff > 180) yawDiff -= 360;
    while (yawDiff < -180) yawDiff += 360;
    setYRot(getYRot() + Math.max(-8, Math.min(8, yawDiff)));
    setYBodyRot(getYRot());

    Vec3 moveVel = null;
    if (distance < MOVE_BACK_DISTANCE) {
      moveVel = new Vec3(-dx, 0, -dz).normalize().scale(0.4);
    } else if (distance > MOVE_FORWARD_DISTANCE) {
      moveVel = new Vec3(dx, 0, dz).normalize().scale(0.4);
    } else if (distance < MOVE_BACK_DISTANCE + 8) {
      moveVel = new Vec3(-dx, 0, -dz).normalize().scale(0.25);
    } else if (distance > MOVE_FORWARD_DISTANCE - 8) {
      moveVel = new Vec3(dx, 0, dz).normalize().scale(0.25);
    }

    if (moveVel != null) {
      setVelocityInternal(moveVel.x, getDeltaMovement().y, moveVel.z);
      moveInternal(getDeltaMovement());
    } else {
      setVelocityInternal(0, getDeltaMovement().y, 0);
    }

    sonicBoomCooldown--;
    if (sonicBoomCooldown <= 0) {
      playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);

      Vec3 start = position().add(0, getBbHeight() * 0.6, 0);
      Vec3 end = target.position().add(0, target.getBbHeight() * 0.5, 0);
      Vec3 dir = end.subtract(start).normalize();
      double dist = start.distanceTo(end);

      for (double d = 0; d < dist; d += 0.5) {
        Vec3 pos = start.add(dir.scale(d));
        world.sendParticles(
            ParticleTypes.SONIC_BOOM,
            pos.x, pos.y, pos.z,
            1, 0, 0, 0, 0
        );
      }

      target.hurtServer(world, damageSources().sonicBoom(this), 20.0F);
      target.push(dir.x * 0.3, 0.2, dir.z * 0.3);
      target.hurtMarked = true;

      sonicBoomCooldown = 40;
    }

    if (tickCount % 10 == 0) {
      world.sendParticles(
          ParticleTypes.SCULK_SOUL,
          getX(), getY() + getBbHeight() / 2, getZ(),
          5, 1.0, 1.0, 1.0, 0.02
      );
    }

    if (phaseTicks > 200) {
      selectNextPhase();
    }
  }

  private void tickSmash(
      ServerLevel world,
      LivingEntity target
  ) {
    smashStateTicks++;

    switch (smashState) {
      case JUMPING -> {
        setVelocityInternal(getDeltaMovement().add(0, 0.1, 0));
        moveInternal(getDeltaMovement());
        world.sendParticles(
            ParticleTypes.CLOUD, getX(), getY(), getZ(),
            8, 0.5, 0.2, 0.5, 0.1
        );

        if (getDeltaMovement().y < 0 || smashStateTicks > 25) {
          smashState = SmashState.FALLING;
          smashStateTicks = 0;
        }
      }
      case FALLING -> {
        if (target != null && smashStateTicks < 10) {
          smashTargetEntityPos = target.position();
        }
        if (smashTargetEntityPos == null) smashTargetEntityPos = position();

        Vec3 toTarget = smashTargetEntityPos.subtract(position());
        double hDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        double hSpeed = Math.min(hDist * 0.15, 1.2);

        Vec3 vel = hDist > 0.1
            ? new Vec3(toTarget.x / hDist * hSpeed, -2.0, toTarget.z / hDist * hSpeed)
            : new Vec3(0, -2.0, 0);

        setVelocityInternal(vel);
        moveInternal(getDeltaMovement());
        world.sendParticles(
            ParticleTypes.FLAME, getX(), getY(), getZ(),
            15, 0.3, 0.3, 0.3, 0.15
        );

        if (onGround() || smashStateTicks > 80) {
          smashState = SmashState.LANDING;
          smashStateTicks = 0;
        }
      }
      case LANDING -> {
        if (smashStateTicks == 1) {
          double radius = 10.0;
          List<LivingEntity> entities = world.getEntitiesOfClass(
              LivingEntity.class,
              getBoundingBox().inflate(radius), e -> e != this && e.isAlive()
          );

          for (LivingEntity entity : entities) {
            double dist = entity.position().distanceTo(position());
            if (dist <= radius) {
              entity.hurtServer(world, damageSources().mobAttack(this), 500.0F);
              Vec3 knockDir = entity.position().subtract(position()).normalize();
              double strength = 3.5 * (1.0 - dist / radius);
              entity.setDeltaMovement(
                  knockDir.x * strength * 2.5,
                  2.0 + strength,
                  knockDir.z * strength * 2.5
              );
              entity.hurtMarked = true;
            }
          }

          playSound(SoundEvents.WARDEN_EMERGE, 2.0F, 0.5F);
          playSound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 2.0F, 0.8F);
          for (int i = 0; i < 360; i += 15) {
            double rad = Math.toRadians(i);
            for (double r = 1; r <= radius; r += 1) {
              world.sendParticles(ParticleTypes.EXPLOSION,
                  getX() + Math.cos(rad) * r, getY() + 0.5, getZ() + Math.sin(rad) * r,
                  1, 0, 0, 0, 0);
            }
          }
          smashAttackCount++;
        }

        if (smashStateTicks >= 15) {
          smashState = SmashState.COOLDOWN;
          smashStateTicks = 0;
        }
      }
      case COOLDOWN -> {
        if (smashStateTicks >= 25 && smashAttackCount < 2 && target != null && target.isAlive()) {
          smashState = SmashState.JUMPING;
          smashStateTicks = 0;
          smashTargetEntityPos = target.position();
          setVelocityInternal(0, 1.8, 0);
          playSound(SoundEvents.GOAT_LONG_JUMP, 2.0F, 0.5F);
        } else if (smashStateTicks >= 25 || smashAttackCount >= 2) {
          selectNextPhase();
        }
      }
    }

    if (phaseTicks > 180 && smashState == SmashState.COOLDOWN) selectNextPhase();
  }

  private void setVelocityInternal(
      Vec3 velocity
  ) {
    super.setDeltaMovement(velocity);
  }

  private void setVelocityInternal(
      double x,
      double y,
      double z
  ) {
    super.setDeltaMovement(x, y, z);
  }

  private void selectNextPhase() {
    if (getTarget() == null) {
      setPhase(Phase.IDLE);
      return;
    }

    if (currentPhase == Phase.IDLE) {
      float rand = random.nextFloat();
      if (rand < 0.60F) setPhase(Phase.CHARGE);
      else if (rand < 0.90F) setPhase(Phase.SONIC_BOOM);
      else setPhase(Phase.SMASH);
    } else {
      setPhase(Phase.IDLE);
    }
  }

  private void moveChargeWithStepAssist(
      Vec3 movement
  ) {
    double horizontalDistance = horizontalDistanceSqr(movement);
    if (horizontalDistance <= MOVEMENT_EPSILON) {
      moveInternal(movement);
      return;
    }

    int segments = Math.max(
        1,
        (int) Math.ceil(Math.sqrt(horizontalDistance) / CHARGE_MOVEMENT_SEGMENT)
    );
    Vec3 segment = movement.scale(1.0 / segments);

    for (int i = 0; i < segments; i++) {
      moveChargeSegment(segment);
    }
  }

  private void moveInternal(
      Vec3 movement
  ) {
    super.move(MoverType.SELF, movement);
  }

  private void setPhase(
      Phase phase
  ) {
    if (this.currentPhase != phase) {
      this.currentPhase = phase;
      this.phaseTicks = 0;
      this.entityData.set(PHASE_TYPE, phase.getId());
      onPhaseStart(phase);
    }
  }

  private void moveChargeSegment(
      Vec3 movement
  ) {
    Vec3 startPos = position();
    moveInternal(movement);
    Vec3 moved = position().subtract(startPos);

    if (horizontalDistanceSqr(movement) - horizontalDistanceSqr(moved) <= MOVEMENT_EPSILON) {
      return;
    }

    Vec3 blockedPos = position();
    setPos(startPos.x, startPos.y, startPos.z);

    if (!tryChargeStepAssist(movement, startPos)) {
      setPos(blockedPos.x, blockedPos.y, blockedPos.z);
    }
  }

  private void onPhaseStart(
      Phase phase
  ) {
    switch (phase) {
      case CHARGE -> {
        chargeDirection = null;
        chargeHit = false;
        chargeStunTimer = 0;
        LivingEntity target = getTarget();
        if (target != null) {
          chargeDirection = target.position().subtract(position()).normalize();
          playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 0.5F);
        }
      }
      case SONIC_BOOM -> {
        sonicBoomCooldown = 20;
        playSound(SoundEvents.WARDEN_ANGRY, 2.0F, 1.0F);
      }
      case SMASH -> {
        smashState = SmashState.JUMPING;
        smashStateTicks = 0;
        smashAttackCount = 0;
        LivingEntity target = getTarget();
        if (target != null) {
          smashTargetEntityPos = target.position();
          setVelocityInternal(0, 1.8, 0);
          playSound(SoundEvents.GOAT_LONG_JUMP, 2.0F, 0.5F);
          playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
        }
      }
    }
  }

  private boolean tryChargeStepAssist(
      Vec3 movement,
      Vec3 startPos
  ) {
    double maxStepHeight = Math.min(getAttributeValue(Attributes.STEP_HEIGHT), CHARGE_STEP_ASSIST_HEIGHT);
    if (maxStepHeight <= 0.0) return false;

    AABB startBox = getBoundingBox();

    for (double step = CHARGE_STEP_ASSIST_INCREMENT;
         step <= maxStepHeight + MOVEMENT_EPSILON;
         step += CHARGE_STEP_ASSIST_INCREMENT) {
      double yOffset = step + CHARGE_STEP_ASSIST_CLEARANCE;
      AABB raisedBox = startBox.move(0, yOffset, 0);
      if (!level().noCollision(this, raisedBox)) continue;
      if (!level().noCollision(this, raisedBox.move(movement))) continue;

      setPos(startPos.x, startPos.y + yOffset, startPos.z);
      moveInternal(movement);

      if (horizontalDistanceSqr(position().subtract(startPos)) > MOVEMENT_EPSILON) {
        return true;
      }

      setPos(startPos.x, startPos.y, startPos.z);
    }

    return false;
  }

public enum Phase {

    IDLE(0), CHARGE(1), SONIC_BOOM(2), SMASH(3);

    private final int phaseId;

    Phase(
        int phaseId
    ) {
      this.phaseId = phaseId;
    }

    public int getId() {
      return phaseId;
    }

    public static Phase fromId(
        int phaseId
    ) {
      for (Phase p : values()) {
        if (p.phaseId == phaseId) return p;
      }
      return IDLE;
    }

  }

private enum SmashState {
    JUMPING, FALLING, LANDING, COOLDOWN
  }

}
