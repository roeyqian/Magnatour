/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.supreme;

// Java Standard
import java.util.Comparator;
import java.util.List;

// Minecraft
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.entity.CustomBossEntity;
import roeyqian.magnatour.entity.EntityLootTableHelper;
import roeyqian.magnatour.entity.PaleLordCommon;
import roeyqian.magnatour.registry.content.SupremeEntities;

public class PaleLord extends Monster implements CustomBossEntity {

  private static final int EXPERIENCE_REWARD = 5000;

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

  private long pendingSplitGameTime = -1L;

  private boolean pendingSplit = false;
  private boolean pendingTransfer = false;

  private final ServerBossEvent bossBar;

  public PaleLord(
      EntityType<? extends Monster> entityType,
      Level world
  ) {
    super(entityType, world);
    this.xpReward = EXPERIENCE_REWARD;
    this.bossBar = new ServerBossEvent(
        this.getUUID(),
        Component.translatable("entity.magnatour.pale_lord"),
        BossEvent.BossBarColor.WHITE,
        BossEvent.BossBarOverlay.NOTCHED_10
    );
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 20.0F)
        .add(Attributes.ATTACK_DAMAGE, 1.0F)
        .add(Attributes.MOVEMENT_SPEED, 0.25F)
        .add(Attributes.FOLLOW_RANGE, 128.0F)
        .add(Attributes.ARMOR, 10.0F);
  }

  @Override
  public void checkDespawn() {}

  @Override
  public void die(
      @NonNull DamageSource source
  ) {
    super.die(source);
    if (this.level() instanceof ServerLevel world) {
      clearNearbyClones(world);
    }
  }

  @Override
  public boolean hurtServer(
      @NonNull ServerLevel world,
      @NonNull DamageSource source,
      float amount
  ) {
    boolean lowDamageHit = amount < PaleLordCommon.DAMAGE_THRESHOLD;
    float normalized = lowDamageHit ? 1.0F : amount;
    boolean damaged = super.hurtServer(world, source, normalized);

    if (!damaged || !this.isAlive()) {
      return damaged;
    }

    if (lowDamageHit) {
      this.pendingSplit = true;
      this.pendingTransfer = this.random.nextBoolean();
      this.pendingSplitGameTime = world.getGameTime();
    } else if (this.pendingSplit) {
      clearPendingSplit();
    }

    return damaged;
  }

  @Override
  public void setCustomName(
      Component name
  ) {
    super.setCustomName(name);
    this.bossBar.setName(this.getDisplayName());
  }

  @Override
  public void startSeenByPlayer(
      @NonNull ServerPlayer player
  ) {
    super.startSeenByPlayer(player);
    this.bossBar.addPlayer(player);
  }

  @Override
  public void stopSeenByPlayer(
      @NonNull ServerPlayer player
  ) {
    super.stopSeenByPlayer(player);
    this.bossBar.removePlayer(player);
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide()) {
      spawnBodyFlameParticles();
      return;
    }

    if (!(this.level() instanceof ServerLevel world)) {
      return;
    }

    this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    processPendingSplit(world);

    if (this.tickCount % 10 == 0) {
      enforceSingleBody(world);
      updateAttackDamage(world);
    }
  }

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

  private void clearNearbyClones(
      ServerLevel world
  ) {
    List<PaleLordClone> clones = world.getEntitiesOfClass(
        PaleLordClone.class,
        this.getBoundingBox().inflate(PaleLordCommon.SYNC_RADIUS),
        Entity::isAlive
    );

    for (PaleLordClone clone : clones) {
      clone.discard();
    }
  }

  private void clearPendingSplit() {
    this.pendingSplit = false;
    this.pendingTransfer = false;
    this.pendingSplitGameTime = -1L;
  }

  private void spawnBodyFlameParticles() {
    if (this.random.nextInt(3) != 0) {
      return;
    }

    this.level().addParticle(
        ParticleTypes.FLAME,
        this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
        this.getY() + this.random.nextDouble() * this.getBbHeight(),
        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
        0.0,
        0.01,
        0.0
    );
  }

  private void processPendingSplit(
      ServerLevel world
  ) {
    if (!this.pendingSplit) {
      return;
    }

    if (!this.isAlive()) {
      clearPendingSplit();
      return;
    }

    // Delay by one tick to avoid consuming the same attack chain that may include a lethal high-damage hit.
    if (world.getGameTime() <= this.pendingSplitGameTime) {
      return;
    }

    if (this.pendingTransfer) {
      transferConsciousness(world);
    } else {
      spawnClone(world, randomSpawnPosition(world));
    }

    clearPendingSplit();
  }

  private void enforceSingleBody(
      ServerLevel world
  ) {
    List<PaleLord> bodies = world.getEntitiesOfClass(
        PaleLord.class,
        this.getBoundingBox().inflate(PaleLordCommon.SYNC_RADIUS),
        Entity::isAlive
    );

    if (bodies.size() <= 1) {
      return;
    }

    PaleLord keeper = bodies.stream()
        .min(Comparator.comparingInt(Entity::getId))
        .orElse(this);

    if (keeper != this) {
      transformCurrentBodyToClone(world);
    }
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

  private void transferConsciousness(
      ServerLevel world
  ) {
    PaleLord newBody = new PaleLord(SupremeEntities.PALE_LORD, world);

    Vec3 newPos = randomSpawnPosition(world);
    newBody.setPos(newPos.x, newPos.y, newPos.z);
    newBody.setYRot(this.getYRot());
    newBody.setXRot(this.getXRot());
    newBody.setYBodyRot(this.getYRot());
    newBody.setHealth(this.getHealth());
    newBody.setTarget(this.getTarget());
    if (this.hasCustomName()) {
      newBody.setCustomName(this.getCustomName());
    }

    if (world.addFreshEntity(newBody)) {
      transformCurrentBodyToClone(world);
    } else {
      spawnClone(world, randomSpawnPosition(world));
    }
  }

  private void spawnClone(
      ServerLevel world,
      Vec3 position
  ) {
    PaleLordClone clone = new PaleLordClone(SupremeEntities.PALE_LORD_CLONE, world);

    clone.setPos(position.x, position.y, position.z);
    clone.setYRot(this.getYRot());
    clone.setXRot(this.getXRot());
    clone.setYBodyRot(this.getYRot());
    clone.setTarget(this.getTarget());
    world.addFreshEntity(clone);
  }

  private Vec3 randomSpawnPosition(
      ServerLevel world
  ) {
    double x = this.getX() + (this.random.nextDouble() - 0.5) * 16.0;
    double z = this.getZ() + (this.random.nextDouble() - 0.5) * 16.0;

    BlockPos surface = world.getHeightmapPos(
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        BlockPos.containing(x, this.getY(), z)
    );
    return new Vec3(x, surface.getY() + 1.0, z);
  }

  private void transformCurrentBodyToClone(
      ServerLevel world
  ) {
    spawnClone(world, this.position());
    this.discard();
  }

}
