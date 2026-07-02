/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.live;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.entity.EntityLootTableHelper;
import roeyqian.magnatour.item.durable.UniverseUltimaSword;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;
import roeyqian.magnatour.utility.registry.gen.RegParticles;

public class UniverseGuardian extends TamableAnimal {

  private int attackCooldown = 0;

  private Vec3 lastOwnerPos = null;

  public UniverseGuardian(
      EntityType<? extends TamableAnimal> entityType,
      Level world
  ) {
    super(entityType, world);
    this.noPhysics = true;
    this.moveControl = new FlyingMoveControl(this, 20, true);
    this.setPathfindingMalus(PathType.WATER, 0.0F);
    this.setPathfindingMalus(PathType.LAVA, 0.0F);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, Integer.MAX_VALUE)
        .add(Attributes.ATTACK_DAMAGE, Integer.MAX_VALUE)
        .add(Attributes.ARMOR, Integer.MAX_VALUE)
        .add(Attributes.FOLLOW_RANGE, 64.0F)
        .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0F);
  }

  @Override
  public void aiStep() {
    this.setNoGravity(true);
    super.aiStep();
  }

  @Override
  public boolean canAttack(
      @NonNull LivingEntity target
  ) {
    if (this.isOwnedBy(target)) return false;
    if (target instanceof UniverseGuardian) return false;

    return super.canAttack(target);
  }

  @Override
  public AgeableMob getBreedOffspring(
      @NonNull ServerLevel world,
      @NonNull AgeableMob entity
  ) {
    return null;
  }

  @Override
  public boolean hurtServer(
      @NonNull ServerLevel world,
      DamageSource source,
      float amount
  ) {
    if (source.getEntity() instanceof LivingEntity attacker) {
      ItemStack stack = attacker.getItemInHand(attacker.getUsedItemHand());
      if (!(attacker instanceof Player)) return false;

      if (stack.getItem() instanceof UniverseUltimaSword
          && stack.getOrDefault(RegComponentTypes.UNIVERSE_ULTIMA_SWORD_MODE, 0) == 1) {
        return super.hurtServer(world, source, amount);
      }
    }
    if (source.is(DamageTypes.GENERIC_KILL)) {
      return super.hurtServer(world, source, amount);
    }
    return false;
  }

  @Override
  public boolean isFood(
      @NonNull ItemStack stack
  ) {
    return false;
  }

  @Override @NonNull
  public InteractionResult mobInteract(
      @NonNull Player player,
      @NonNull InteractionHand hand
  ) {
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    player.swing(hand);

    if (!this.level().isClientSide()) return execTame(player);
    return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
  }

  @Override
  public void onClientRemoval() {
    super.onClientRemoval();
    LivingEntity owner = this.getOwner();
    if (owner != null && !this.level().isClientSide()) {
      this.snapTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
    }
  }

  @Override
  public void tick() {
    super.tick();

    if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth())
      this.heal(Integer.MAX_VALUE);
    if (this.level().isClientSide() || !this.isTame()) return;

    LivingEntity owner = this.getOwner();
    if (owner == null) return;
    if (!execTeleport(owner)) execFollow(owner);

    if (this.getTarget() != null && this.getTarget().isAlive()) {
      if (attackCooldown-- <= 0) {
        this.execSonicBoom(this.getTarget());
        attackCooldown = 2;
      }
    }
  }

  @Override
  protected void defineSynchedData(
      SynchedEntityData.@NonNull Builder builder
  ) {
    super.defineSynchedData(builder);
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
  protected void registerGoals() {
    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.targetSelector.addGoal(
        1,
        new NearestAttackableTargetGoal<>(
            this,
            Mob.class,
            10,
            false,
            false,
            (entity, _) -> {
              if (!this.isTame()) return false;
              if (!(entity instanceof net.minecraft.world.entity.monster.Enemy)) return false;
              if (entity instanceof UniverseGuardian) return false;
              return !entity.equals(this.getOwner());
            }
        )
    );
    this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
    this.targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
  }

  private InteractionResult execTame(
      Player player
  ) {
    if (!this.isTame()) {
      this.setOwner(player);
      this.setTame(true, true);

      this.level().broadcastEntityEvent(this, (byte) 7);
      player.sendOverlayMessage(
          Component
              .translatable("msg.magnatour.universe_guardian.guard")
              .withStyle(ChatFormatting.GOLD)
      );

      return InteractionResult.SUCCESS;
    } else if (player.equals(this.getOwner())) {
      this.setTame(false, true);
      this.setOwner(null);

      this.level().broadcastEntityEvent(this, (byte) 6);
      player.sendOverlayMessage(
          Component
              .translatable("msg.magnatour.universe_guardian.sleep")
              .withStyle(ChatFormatting.BLUE)
      );
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  private boolean execTeleport(
      LivingEntity owner
  ) {
    boolean shouldTeleport = false;
    if (this.level() != owner.level()) shouldTeleport = true;
    else if (this.distanceToSqr(owner) > 4096.0) shouldTeleport = true;
    else if (lastOwnerPos != null
        && owner.distanceToSqr(lastOwnerPos) > 64.0
    ) shouldTeleport = true;

    if (shouldTeleport) {
      float spawnAngle = (float) Math.toRadians(owner.getYRot() + 135.0F);
      double targetDistance = 2.5;

      double spawnX = owner.getX() + Math.cos(spawnAngle) * targetDistance;
      double spawnZ = owner.getZ() + Math.sin(spawnAngle) * targetDistance;
      double spawnY = owner.getY() + 1.0;

      this.snapTo(spawnX, spawnY, spawnZ, owner.getYRot(), owner.getXRot());

      this.setDeltaMovement(0, 0, 0);

      this.lastOwnerPos = owner.position();
      return true;
    }

    return false;
  }

  private void execFollow(
      LivingEntity owner
  ) {
    double dx = this.getX() - owner.getX();
    double dz = this.getZ() - owner.getZ();
    double currentDistSq = dx * dx + dz * dz;

    double distance = 2.5;
    double angle = (currentDistSq < 0.01) ? Math.toRadians(owner.getYRot() + 135.0F) : Math.atan2(dz, dx);

    double targetX = owner.getX() + Math.cos(angle) * distance;
    double targetY = owner.getY() + 1.0;
    double targetZ = owner.getZ() + Math.sin(angle) * distance;

    this.setPos(
        Mth.lerp(0.15, this.getX(), targetX),
        Mth.lerp(0.15, this.getY(), targetY),
        Mth.lerp(0.15, this.getZ(), targetZ)
    );

    this.lastOwnerPos = owner.position();
    float targetYaw = owner.getYRot() + 180.0F;
    this.setYRot(Mth.rotLerp(0.15F, this.getYRot(), targetYaw));
    this.setYBodyRot(this.getYRot());
  }

  private void execSonicBoom(
      LivingEntity target
  ) {
    if (this.level() instanceof ServerLevel serverWorld) {
      double startX = this.getX();
      double startY = this.getY(0.5);
      double startZ = this.getZ();

      double dX = target.getX() - startX;
      double dY = target.getEyeY() - startY;
      double dZ = target.getZ() - startZ;

      double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
      dX /= distance;
      dY /= distance;
      dZ /= distance;

      for (int i = 1; i < (int) distance; ++i) {
        serverWorld.sendParticles(
            RegParticles.UNIVERSE_SONIC_BOOM,
            startX + dX * i, startY + dY * i, startZ + dZ * i,
            1, 0.0F, 0.0F, 0.0F, 0.0F
        );
      }

      this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.0F, 1.0F);
      target.hurtServer(serverWorld, this.damageSources().sonicBoom(this), Integer.MAX_VALUE);
      target.knockback(3.0, -dX, -dZ);
    }
  }

}
