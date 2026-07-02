/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.dead;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;

public class UniverseFireball extends LargeFireball {

  private final int power;

  public UniverseFireball(
      Level world,
      LivingEntity owner,
      Vec3 velocity,
      int explosionPower
  ) {
    super(world, owner, velocity, explosionPower);
    this.power = explosionPower;
  }

  @Override
  protected void onHit(
      @NonNull HitResult hitResult
  ) {
    if (!(this.level() instanceof ServerLevel serverWorld)) return;

    BlockPos center = BlockPos.containing(this.getEyePosition());
    int radius = this.power - 1;

    breakAllBlocks(radius, center, serverWorld);

    Vec3 centerVec = this.getEyePosition();
    AABB box = this.getBoundingBox().inflate(radius);

    damageAllCreatures(radius, serverWorld, box, centerVec);

    serverWorld.explode(
        this,
        this.getX(), this.getY(), this.getZ(),
        this.power, true, Level.ExplosionInteraction.MOB
    );
    this.discard();
  }

  private static void breakAllBlocks(
      int radius,
      BlockPos center,
      ServerLevel serverWorld
  ) {
    for (BlockPos pos : BlockPos.betweenClosed(
        center.offset(-radius, -radius, -radius),
        center.offset(radius, radius, radius)
    )) {
      if (pos.distSqr(center) <= (double) radius * radius) {
        if (!serverWorld.isEmptyBlock(pos)) {
          serverWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
      }
    }
  }

  private static void damageAllCreatures(
      int radius,
      ServerLevel serverWorld,
      AABB box,
      Vec3 centerVec
  ) {
    List<LivingEntity> entities = serverWorld.getEntitiesOfClass(
        LivingEntity.class, box,
        entity -> entity.distanceToSqr(centerVec) <= (double) radius * radius
    );

    for (LivingEntity entity : entities) {
      entity.hurtServer(serverWorld, serverWorld.damageSources().generic(), Float.MAX_VALUE);
    }
  }

}
