/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.universe;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseVoidPoolEntity;
import roeyqian.magnatour.registry.content.UniverseBlockEntities;

public class UniverseVoidPool extends BaseEntityBlock {

  public static final MapCodec<UniverseVoidPool> CODEC = simpleCodec(UniverseVoidPool::new);

  public UniverseVoidPool(
      Properties settings
  ) {
    super(settings);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return createTickerHelper(
        type,
        UniverseBlockEntities.UNIVERSE_VOID_POOL_ENTITY,
        (world1, _, _, blockEntity) -> blockEntity.tick(world1)
    );
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseVoidPoolEntity(pos, state);
  }

  @Override
  protected void affectNeighborsAfterRemoval(
      @NonNull BlockState state,
      ServerLevel world,
      @NonNull BlockPos pos,
      boolean moved
  ) {
    BlockEntity be = world.getBlockEntity(pos);
    if (be instanceof UniverseVoidPoolEntity inventoryBe) {
      Containers.dropContents(world, pos, inventoryBe);
      world.updateNeighbourForOutputSignal(pos, this);
    }
    super.affectNeighborsAfterRemoval(state, world, pos, moved);
  }

  @Override @NonNull
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (world.isClientSide()) {
      return InteractionResult.SUCCESS;
    } else {
      MenuProvider factory = this.getMenuProvider(state, world, pos);
      if (factory != null) player.openMenu(factory);
      return InteractionResult.CONSUME;
    }
  }

}
