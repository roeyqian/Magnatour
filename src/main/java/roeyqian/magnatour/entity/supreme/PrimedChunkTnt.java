/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.content.SupremeEntities;

public class PrimedChunkTnt extends PrimedTnt {

  public static final int DEFAULT_FUSE = 80;

  public static final float DAMAGE = 10000.0F;

  public PrimedChunkTnt(
      EntityType<? extends PrimedChunkTnt> type,
      Level level
  ) {
    super(type, level);
    this.blocksBuilding = true;
    this.setBlockState(SupremeBlocks.PRIMED_CHUNK_TNT_VISUAL.defaultBlockState());
  }

  public static PrimedChunkTnt prime(
      ServerLevel level,
      BlockPos pos,
      @Nullable LivingEntity igniter
  ) {
    PrimedChunkTnt entity = new PrimedChunkTnt(
        SupremeEntities.PRIMED_CHUNK_TNT, level
    );
    double x = pos.getX() + 0.5;
    double y = pos.getY();
    double z = pos.getZ() + 0.5;
    entity.setPos(x, y, z);
    double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
    entity.setDeltaMovement(
        -Math.sin(angle) * 0.02,
        0.2F,
        -Math.cos(angle) * 0.02
    );
    entity.xo = x;
    entity.yo = y;
    entity.zo = z;
    entity.setFuse(DEFAULT_FUSE);
    return entity;
  }

  @Override
  public boolean isPickable() {
    return super.isPickable();
  }

  @Override
  public void tick() {
    this.handlePortal();
    this.applyGravity();
    this.move(MoverType.SELF, this.getDeltaMovement());
    this.applyEffectsFromBlocks();
    this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    if (this.onGround()) {
      this.setDeltaMovement(
          this.getDeltaMovement().multiply(0.7, -0.5, 0.7)
      );
    }

    int fuse = this.getFuse() - 1;
    this.setFuse(fuse);
    if (fuse <= 0) {
      this.discard();
      if (!this.level().isClientSide()) {
        explodeChunk();
      }
    } else {
      this.updateFluidInteraction();
      if (this.level().isClientSide()) {
        this.level().addParticle(
            ParticleTypes.SMOKE,
            this.getX(), this.getY() + 0.5, this.getZ(),
            0.0, 0.0, 0.0
        );
      }
    }
  }

  @Override
  protected double getDefaultGravity() {
    return super.getDefaultGravity();
  }

  @Override
  protected Entity.@NonNull MovementEmission getMovementEmission() {
    return super.getMovementEmission();
  }

  private void explodeChunk() {
    if (this.level() instanceof ServerLevel serverWorld) {
      if (!serverWorld.getGameRules()
          .get(GameRules.TNT_EXPLODES)) {
        return;
      }
      destroyChunk(serverWorld);
    }
  }

  private void destroyChunk(
      ServerLevel serverWorld
  ) {
    BlockPos pos = this.blockPosition();

    // Calculate chunk boundaries
    int chunkX = pos.getX() >> 4;
    int chunkZ = pos.getZ() >> 4;
    int minX = chunkX << 4;
    int minZ = chunkZ << 4;
    int maxX = minX + 15;
    int maxZ = minZ + 15;
    int minY = this.level().getMinY();
    int maxY = this.level().getMaxY() - 1;

    // Delete all blocks in the chunk
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        for (int z = minZ; z <= maxZ; z++) {
          mutablePos.set(x, y, z);
          if (!serverWorld.isEmptyBlock(mutablePos)) {
            serverWorld.setBlock(
                mutablePos, Blocks.AIR.defaultBlockState(),
                Block.UPDATE_CLIENTS
            );
          }
        }
      }
    }

    // Deal 10000 damage to all entities in the chunk
    AABB chunkBox = new AABB(
        minX, minY, minZ,
        maxX + 1, maxY + 1, maxZ + 1
    );
    for (Entity entity : serverWorld.getEntities(null, chunkBox)) {
      entity.hurtServer(
          serverWorld, serverWorld.damageSources().generic(), DAMAGE
      );
    }

    // Sound effect
    serverWorld.playSound(
        null, pos, SoundEvents.GENERIC_EXPLODE.value(),
        SoundSource.BLOCKS, 4.0F, 1.0F
    );
  }

}
