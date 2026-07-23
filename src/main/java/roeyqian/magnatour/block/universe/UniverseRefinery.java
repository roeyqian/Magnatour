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

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseRefineryEntity;
import roeyqian.magnatour.registry.content.RegBlockEntities;

public class UniverseRefinery extends AbstractFurnaceBlock {

  public static final MapCodec<UniverseRefinery> CODEC = simpleCodec(UniverseRefinery::new);

  public UniverseRefinery(
      Properties settings
  ) {
    super(settings);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    if (!world.isClientSide()) {
      return checkType(
          type, (tickWorld, pos, tickState, entity) -> {
            if (tickWorld instanceof ServerLevel serverWorld) {
              UniverseRefineryEntity.tick(serverWorld, pos, tickState, (UniverseRefineryEntity) entity);
            }
          }
      );
    } else {
      return null;
    }
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseRefineryEntity(pos, state);
  }

  @Override @NonNull
  protected MapCodec<? extends UniverseRefinery> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.@NonNull Builder<Block, BlockState> builder
  ) {
    super.createBlockStateDefinition(builder);
  }

  @Override
  protected void openContainer(
      Level world,
      @NonNull BlockPos pos,
      @NonNull Player player
  ) {
    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof UniverseRefineryEntity) {
      player.openMenu((MenuProvider) blockEntity);
      player.awardStat(Stats.INTERACT_WITH_FURNACE);
    }
  }

  @SuppressWarnings("unchecked")
  private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(
      BlockEntityType<A> givenType,
      BlockEntityTicker<? super E> ticker
  ) {
    return RegBlockEntities.UNIVERSE_REFINERY_ENTITY == givenType ? (BlockEntityTicker<A>) ticker : null;
  }

}
