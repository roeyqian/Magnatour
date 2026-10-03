/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.blockentity.supreme.SupremeFurnaceEntity;
import roeyqian.magnatour.registry.content.SupremeBlockEntities;

public class SupremeFurnace extends AbstractFurnaceBlock {

  public SupremeFurnace(
      Properties settings
  ) {
    super(settings);
  }

  @Nullable @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    if (!world.isClientSide()) {
      return checkType(
          type, (tickWorld, pos, tickState, entity) -> {
            if (tickWorld instanceof ServerLevel serverWorld) {
              SupremeFurnaceEntity.tick(serverWorld, pos, tickState, (SupremeFurnaceEntity) entity);
            }
          }
      );
    } else {
      return null;
    }
  }

  @Nullable @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new SupremeFurnaceEntity(pos, state);
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.@NonNull Builder<Block, BlockState> builder
  ) {
    super.createBlockStateDefinition(builder);
  }

  @Override
  protected void openContainer(
      @NonNull Level world,
      @NonNull BlockPos pos,
      @NonNull Player player
  ) {
    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof SupremeFurnaceEntity) {
      player.openMenu((MenuProvider) blockEntity);
      player.awardStat(Stats.INTERACT_WITH_FURNACE);
    }
  }

  @SuppressWarnings("unchecked")
  private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(
      BlockEntityType<A> givenType,
      BlockEntityTicker<? super E> ticker
  ) {
    return SupremeBlockEntities.SUPREME_FURNACE_ENTITY == givenType ? (BlockEntityTicker<A>) ticker : null;
  }

}
