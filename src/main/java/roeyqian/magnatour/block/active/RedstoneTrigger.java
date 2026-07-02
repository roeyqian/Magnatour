/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.active;

// Mojang
import com.mojang.serialization.MapCodec;

// Fabric
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.active.entity.RedstoneTriggerEntity;
import roeyqian.magnatour.menu.block.RedstoneTriggerMenu;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class RedstoneTrigger extends BaseEntityBlock {

  public static final MapCodec<RedstoneTrigger> CODEC = simpleCodec(RedstoneTrigger::new);

  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

  public RedstoneTrigger(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
  }

  public static void updateSignalNeighbours(
      Level world,
      BlockPos pos,
      Block source
  ) {
    world.updateNeighborsAt(pos, source);
    for (Direction direction : Direction.values()) {
      world.updateNeighborsAt(pos.relative(direction), source);
    }
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return createTickerHelper(
        type,
        RegBlockEntities.REDSTONE_TRIGGER_ENTITY,
        RedstoneTriggerEntity::tick
    );
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new RedstoneTriggerEntity(pos, state);
  }

  @Override
  protected void affectNeighborsAfterRemoval(
      @NonNull BlockState state,
      ServerLevel world,
      @NonNull BlockPos pos,
      boolean moved
  ) {
    if (state.getValue(POWERED)) {
      updateSignalNeighbours(world, pos, state.getBlock());
    }
    super.affectNeighborsAfterRemoval(state, world, pos, moved);
  }

  @Override @NonNull
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(POWERED);
  }

  @Override
  protected int getDirectSignal(
      @NonNull BlockState state,
      @NonNull BlockGetter world,
      @NonNull BlockPos pos,
      @NonNull Direction direction
  ) {
    return state.getValue(POWERED) ? 15 : 0;
  }

  @Override
  protected int getSignal(
      @NonNull BlockState state,
      @NonNull BlockGetter world,
      @NonNull BlockPos pos,
      @NonNull Direction direction
  ) {
    return state.getValue(POWERED) ? 15 : 0;
  }

  @Override
  protected boolean isSignalSource(
      @NonNull BlockState state
  ) {
    return true;
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (world.isClientSide()) return InteractionResult.SUCCESS;

    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof RedstoneTriggerEntity triggerEntity) {
      player.openMenu(new ExtendedMenuProvider<RedstoneTriggerMenu.OpeningData>() {

        @Override @NonNull
        public AbstractContainerMenu createMenu(
            int syncId,
            @NonNull Inventory inventory,
            @NonNull Player player
        ) {
          return new RedstoneTriggerMenu(
              syncId,
              pos,
              world.dimension(),
              triggerEntity.getMode(),
              triggerEntity.isEnabled(),
              triggerEntity.getIntervalTicks()
          );
        }

        @Override @NonNull
        public Component getDisplayName() {
          return triggerEntity.getDisplayName();
        }

        @Override
        public RedstoneTriggerMenu.OpeningData getScreenOpeningData(
            @NonNull ServerPlayer player
        ) {
          return new RedstoneTriggerMenu.OpeningData(
              pos,
              world.dimension(),
              triggerEntity.getMode(),
              triggerEntity.isEnabled(),
              triggerEntity.getIntervalTicks()
          );
        }
      });
    }

    return InteractionResult.CONSUME;
  }

}
