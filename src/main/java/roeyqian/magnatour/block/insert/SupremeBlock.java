/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.insert;

// Mojang
import com.mojang.serialization.MapCodec;

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

// Magnatour
import roeyqian.magnatour.block.SummonStructureHelper;
import roeyqian.magnatour.block.VirtualBlockLightManager;
import roeyqian.magnatour.block.insert.entity.SupremeBlockEntity;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class SupremeBlock extends BaseEntityBlock {

  public static final int LIGHT_LEVEL = 12;

  public static final MapCodec<SupremeBlock> CODEC = simpleCodec(SupremeBlock::new);

  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  public SupremeBlock(
      BlockBehaviour.Properties properties
  ) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return createTickerHelper(
        type,
        RegBlockEntities.SUPREME_BLOCK_ENTITY,
        SupremeBlockEntity::tick
    );
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new SupremeBlockEntity(pos, state);
  }

  @Override
  protected void affectNeighborsAfterRemoval(
      BlockState state,
      ServerLevel world,
      @NonNull BlockPos pos,
      boolean moved
  ) {
    BlockState newState = world.getBlockState(pos);
    if (!state.is(newState.getBlock())) {
      VirtualBlockLightManager.setActive(world, pos, LIGHT_LEVEL, false);
      super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
  }

  @Override @NonNull
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(LIT);
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (!world.isClientSide()) {
      boolean newLit = !state.getValue(LIT);
      world.setBlockAndUpdate(pos, state.setValue(LIT, newLit));

      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof SupremeBlockEntity supremeBlockEntity) {
        supremeBlockEntity.setLightRegistered(newLit);
      } else {
        VirtualBlockLightManager.setActive(world, pos, LIGHT_LEVEL, newLit);
      }
    }
    return InteractionResult.SUCCESS;
  }

}
