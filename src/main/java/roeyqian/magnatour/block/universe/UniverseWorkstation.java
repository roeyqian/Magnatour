/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.universe.UniverseWorkstationMenu;

public class UniverseWorkstation extends Block {

  public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

  public UniverseWorkstation(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  public BlockState getStateForPlacement(
      BlockPlaceContext context
  ) {
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(FACING);
  }

  @Override
  protected MenuProvider getMenuProvider(
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos
  ) {
    return new SimpleMenuProvider(
        (syncId, inventory, _) -> new UniverseWorkstationMenu(
            syncId,
            inventory,
            ContainerLevelAccess.create(world, pos)
        ),
        Component.translatable("container.crafting")
    );
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
