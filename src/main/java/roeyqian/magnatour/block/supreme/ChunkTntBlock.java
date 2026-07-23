/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.entity.supreme.PrimedChunkTnt;

public class ChunkTntBlock extends Block {

  public ChunkTntBlock(
      BlockBehaviour.Properties properties
  ) {
    super(properties);
  }

  public static boolean prime(
      Level level,
      BlockPos pos,
      @Nullable LivingEntity igniter
  ) {
    if (!(level instanceof ServerLevel serverLevel)) {
      return false;
    }
    if (!serverLevel.getGameRules()
        .get(GameRules.TNT_EXPLODES)) {
      return false;
    }
    PrimedChunkTnt entity = PrimedChunkTnt.prime(serverLevel, pos, igniter);
    level.addFreshEntity(entity);
    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
        SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
    level.gameEvent(entity, GameEvent.PRIME_FUSE, pos);
    return true;
  }

  @Override
  public boolean dropFromExplosion(
      @NonNull Explosion explosion
  ) {
    return false;
  }

  @Override
  public void onPlace(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull BlockState oldState,
      boolean notify
  ) {
    if (oldState.is(state.getBlock())) return;
    if (level.hasNeighborSignal(pos)) {
      if (prime(level, pos, null)) {
        level.removeBlock(pos, false);
      }
    }
  }

  @Override
  public void wasExploded(
      ServerLevel serverLevel,
      @NonNull BlockPos pos,
      @NonNull Explosion explosion
  ) {
    if (!serverLevel.getGameRules()
        .get(GameRules.TNT_EXPLODES)) {
      return;
    }
    PrimedChunkTnt entity = PrimedChunkTnt.prime(
        serverLevel, pos, explosion.getIndirectSourceEntity());
    int fuse = entity.getFuse();
    entity.setFuse(
        serverLevel.getRandom().nextInt(fuse / 4) + fuse / 8);
    serverLevel.addFreshEntity(entity);
  }

  @Override
  protected void neighborChanged(
      @NonNull BlockState state,
      Level level,
      @NonNull BlockPos pos,
      @NonNull Block sourceBlock,
      @Nullable Orientation orientation,
      boolean notify
  ) {
    if (level.hasNeighborSignal(pos)) {
      if (prime(level, pos, null)) {
        level.removeBlock(pos, false);
      }
    }
  }

  @Override
  protected void onProjectileHit(
      @NonNull Level level,
      @NonNull BlockState state,
      @NonNull BlockHitResult hit,
      @NonNull Projectile projectile
  ) {
    if (!(level instanceof ServerLevel serverLevel)) return;
    BlockPos pos = hit.getBlockPos();
    var owner = projectile.getOwner();
    if (projectile.isOnFire() && projectile.mayInteract(serverLevel, pos)) {
      prime(level, pos,
          owner instanceof LivingEntity living ? living : null);
      level.removeBlock(pos, false);
    }
  }

  @Override @NonNull
  protected InteractionResult useItemOn(
      @NonNull ItemStack stack,
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull InteractionHand hand,
      @NonNull BlockHitResult hitResult
  ) {
    if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
      return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
    if (prime(level, pos, player)) {
      level.setBlock(pos, Blocks.AIR.defaultBlockState(),
          Block.UPDATE_ALL_IMMEDIATE);
      if (stack.is(Items.FLINT_AND_STEEL)) {
        stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
      } else {
        stack.consume(1, player);
      }
    }
    return InteractionResult.SUCCESS;
  }

}
