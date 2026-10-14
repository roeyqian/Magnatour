/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseWorkstationEntity;
import roeyqian.magnatour.registry.content.UniverseBlockEntities;

public class UniverseWorkstation extends BaseEntityBlock {

  public static final BooleanProperty LIT = BlockStateProperties.LIT;
  public static final BooleanProperty SMELTING = BooleanProperty.create("smelting");

  public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

  public UniverseWorkstation(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any()
        .setValue(FACING, Direction.NORTH)
        .setValue(LIT, false)
        .setValue(SMELTING, false));
  }

  @Nullable @Override
  public BlockState getStateForPlacement(
      @NonNull BlockPlaceContext context
  ) {
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return world.isClientSide() ? null : createTickerHelper(
        type, UniverseBlockEntities.UNIVERSE_WORKSTATION_ENTITY,
        (level, pos, tickState, entity) -> {
          if (level instanceof ServerLevel serverLevel) {
            UniverseWorkstationEntity.tick(serverLevel, pos, tickState, entity);
          }
        }
    );
  }

  @Override @NonNull
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseWorkstationEntity(pos, state);
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.@NonNull Builder<Block, BlockState> builder
  ) {
    builder.add(FACING, LIT, SMELTING);
  }

  @Nullable @Override
  protected MenuProvider getMenuProvider(
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos
  ) {
    return world.getBlockEntity(pos) instanceof UniverseWorkstationEntity entity ? entity : null;
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      @NonNull Level world,
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
