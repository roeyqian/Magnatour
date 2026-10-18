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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.block.MagnaPortalY;
import roeyqian.magnatour.block.VirtualBlockLighter;
import roeyqian.magnatour.blockentity.universe.UniverseBlockEntity;
import roeyqian.magnatour.registry.stuff.MagnaBlockEntities;
import roeyqian.magnatour.registry.stuff.MagnaBlocks;
import roeyqian.magnatour.registry.world.MagnaDimensions;

public class UniverseBlock extends BaseEntityBlock {

  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  public UniverseBlock(
      BlockBehaviour.Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
  }

  @Nullable @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return createTickerHelper(
        type,
        MagnaBlockEntities.UNIVERSE_BLOCK_ENTITY,
        UniverseBlockEntity::tick
    );
  }

  @Nullable @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseBlockEntity(pos, state);
  }

  @Override
  protected void affectNeighborsAfterRemoval(
      @NonNull BlockState state,
      @NonNull ServerLevel world,
      @NonNull BlockPos pos,
      boolean moved
  ) {
    BlockState newState = world.getBlockState(pos);
    if (!state.is(newState.getBlock())) {
      VirtualBlockLighter.setActive(world, pos, false);
      super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.@NonNull Builder<Block, BlockState> builder
  ) {
    builder.add(LIT);
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (!world.isClientSide()) {
      boolean currentLit = state.getValue(LIT);
      boolean newLit = !currentLit;

      BlockState newState = state.setValue(LIT, newLit);
      world.setBlockAndUpdate(pos, newState);

      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof UniverseBlockEntity universeBlockEntity) {
        universeBlockEntity.setLightRegistered(newLit);
      } else {
        VirtualBlockLighter.setActive(world, pos, newLit);
      }

      // Try to activate portal when block is lit
      if (newLit && (world.dimension() == Level.OVERWORLD
          || world.dimension() == MagnaDimensions.UNIVERSE_META)) {
        MagnaPortalY.tryActivatePortal(world, pos, this, MagnaBlocks.UNIVERSE_META_PORTAL);
      }
    }
    return InteractionResult.SUCCESS;
  }

}
