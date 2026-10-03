/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Java Standard
import java.util.Map;
import java.util.function.Function;

// Google Guava
import com.google.common.collect.ImmutableMap;

// Fabric
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.menu.supreme.ItemHubMenu;
import roeyqian.magnatour.registry.content.SupremeBlockEntities;

public class ItemHub extends BaseEntityBlock {

  public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

  public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING_HOPPER;

  private final Map<Direction, VoxelShape> interactionShapes;

  private final Function<BlockState, VoxelShape> shapes;

  public ItemHub(
      BlockBehaviour.Properties properties
  ) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, true));
    this.shapes = this.makeShapes();
    this.interactionShapes = ImmutableMap.<Direction, VoxelShape>builderWithExpectedSize(5)
        .putAll(Shapes.rotateHorizontal(Shapes.or(
            Block.box(3.0, 13.0, 3.0, 13.0, 16.0, 13.0),
            Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 4.0)
        )))
        .put(Direction.DOWN, Block.box(3.0, 13.0, 3.0, 13.0, 16.0, 13.0))
        .build();
  }

  @Nullable @Override
  public BlockState getStateForPlacement(
      @NonNull BlockPlaceContext context
  ) {
    return this.defaultBlockState()
        .setValue(FACING, context.getHorizontalDirection().getOpposite())
        .setValue(ENABLED, true);
  }

  @Nullable @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level level,
      @NonNull BlockState blockState,
      @NonNull BlockEntityType<T> type
  ) {
    return level.isClientSide()
        ? null
        : createTickerHelper(type, SupremeBlockEntities.ITEM_HUB_ENTITY, ItemHubEntity::pushItemsTick);
  }

  @Nullable @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new ItemHubEntity(pos, state);
  }

  @Override
  protected void affectNeighborsAfterRemoval(
      @NonNull BlockState state,
      @NonNull ServerLevel level,
      @NonNull BlockPos pos,
      boolean movedByPiston
  ) {
    Containers.updateNeighboursAfterDestroy(state, level, pos);
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.@NonNull Builder<Block, BlockState> builder
  ) {
    builder.add(FACING, ENABLED);
  }

  @Override
  protected void entityInside(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Entity entity,
      @NonNull InsideBlockEffectApplier effectApplier,
      boolean isPrecise
  ) {
    if (level.getBlockEntity(pos) instanceof ItemHubEntity itemHubEntity) {
      ItemHubEntity.entityInside(level, pos, state, entity, itemHubEntity);
    }
  }

  @Override
  protected int getAnalogOutputSignal(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Direction direction
  ) {
    return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
  }

  @NonNull @Override
  protected VoxelShape getInteractionShape(
      @NonNull BlockState state,
      @NonNull BlockGetter level,
      @NonNull BlockPos pos
  ) {
    return this.interactionShapes.get(state.getValue(FACING));
  }

  @NonNull @Override
  protected VoxelShape getShape(
      @NonNull BlockState state,
      @NonNull BlockGetter level,
      @NonNull BlockPos pos,
      @NonNull CollisionContext context
  ) {
    return this.shapes.apply(state);
  }

  @Override
  protected boolean hasAnalogOutputSignal(
      @NonNull BlockState state
  ) {
    return true;
  }

  @Override
  protected boolean isPathfindable(
      @NonNull BlockState state,
      @NonNull PathComputationType type
  ) {
    return false;
  }

  @NonNull @Override
  protected BlockState mirror(
      @NonNull BlockState state,
      @NonNull Mirror mirror
  ) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }

  @Override
  protected void neighborChanged(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Block block,
      @Nullable Orientation orientation,
      boolean movedByPiston
  ) {
    this.checkPoweredState(level, pos, state);
  }

  @Override
  protected void onPlace(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull BlockState oldState,
      boolean movedByPiston
  ) {
    if (!oldState.is(state.getBlock())) {
      this.checkPoweredState(level, pos, state);
    }
  }

  @NonNull @Override
  protected BlockState rotate(
      @NonNull BlockState state,
      @NonNull Rotation rotation
  ) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @NonNull @Override
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hitResult
  ) {
    if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ItemHubEntity itemHubEntity) {
      player.openMenu(new ExtendedMenuProvider<ItemHubMenu.OpeningData>() {

        @Nullable @Override
        public AbstractContainerMenu createMenu(
            int containerId,
            @NonNull Inventory inventory,
            @NonNull Player player
        ) {
          return new ItemHubMenu(
              containerId,
              inventory,
              itemHubEntity,
              pos,
              level.dimension(),
              itemHubEntity.getAnchoredItemIds()
          );
        }

        @NonNull @Override
        public Component getDisplayName() {
          return itemHubEntity.getDisplayName();
        }

        @Override
        public ItemHubMenu.@NonNull OpeningData getScreenOpeningData(
            @NonNull ServerPlayer player
        ) {
          return new ItemHubMenu.OpeningData(
              pos,
              level.dimension(),
              itemHubEntity.getAnchoredItemIds()
          );
        }

      });
      player.awardStat(Stats.INSPECT_HOPPER);
    }

    return InteractionResult.SUCCESS;
  }

  private Function<BlockState, VoxelShape> makeShapes() {
    VoxelShape node = Shapes.or(
        Block.box(1.0, 4.0, 1.0, 15.0, 6.0, 15.0),
        Block.box(1.0, 0.0, 1.0, 3.0, 4.0, 3.0),
        Block.box(1.0, 6.0, 1.0, 3.0, 14.0, 3.0),
        Block.box(1.0, 14.0, 1.0, 3.0, 16.0, 3.0),
        Block.box(1.0, 0.0, 13.0, 3.0, 4.0, 15.0),
        Block.box(1.0, 6.0, 13.0, 3.0, 14.0, 15.0),
        Block.box(1.0, 14.0, 13.0, 3.0, 16.0, 15.0),
        Block.box(13.0, 0.0, 1.0, 15.0, 4.0, 3.0),
        Block.box(13.0, 6.0, 1.0, 15.0, 14.0, 3.0),
        Block.box(13.0, 14.0, 1.0, 15.0, 16.0, 3.0),
        Block.box(13.0, 0.0, 13.0, 15.0, 4.0, 15.0),
        Block.box(13.0, 6.0, 13.0, 15.0, 14.0, 15.0),
        Block.box(13.0, 14.0, 13.0, 15.0, 16.0, 15.0),
        Block.box(3.0, 14.0, 1.0, 13.0, 16.0, 3.0),
        Block.box(3.0, 14.0, 13.0, 13.0, 16.0, 15.0),
        Block.box(6.0, 13.0, 1.0, 10.0, 14.0, 3.0),
        Block.box(6.0, 13.0, 13.0, 10.0, 14.0, 15.0),
        Block.box(4.0, 6.0, 4.0, 12.0, 12.0, 12.0),
        Block.box(5.0, 12.0, 5.0, 11.0, 13.0, 11.0)
    );
    VoxelShape horizontalPort = Shapes.or(
        Block.box(6.0, 6.0, 1.0, 10.0, 10.0, 4.0),
        Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 1.0)
    );
    VoxelShape downwardPort = Shapes.or(
        Block.box(6.0, 1.0, 6.0, 10.0, 4.0, 10.0),
        Block.box(5.0, 0.0, 5.0, 11.0, 1.0, 11.0)
    );
    Map<Direction, VoxelShape> ports = ImmutableMap.<Direction, VoxelShape>builderWithExpectedSize(5)
        .putAll(Shapes.rotateHorizontal(horizontalPort))
        .put(Direction.DOWN, downwardPort)
        .build();
    return this.getShapeForEachState(
        state -> Shapes.or(node, ports.get(state.getValue(FACING))),
        new Property[]{ENABLED}
    );
  }

  private void checkPoweredState(
      Level level,
      BlockPos pos,
      BlockState state
  ) {
    boolean shouldBeOn = !level.hasNeighborSignal(pos);
    if (shouldBeOn != state.getValue(ENABLED)) {
      level.setBlock(pos, state.setValue(ENABLED, shouldBeOn), 2);
    }
  }

}
