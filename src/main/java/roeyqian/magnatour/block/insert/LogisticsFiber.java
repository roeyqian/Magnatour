/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.insert;

// Java Standard
import java.util.Map;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.block.active.entity.LogisticsFiberEntity;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class LogisticsFiber extends BaseEntityBlock {

  public static final MapCodec<LogisticsFiber> CODEC = simpleCodec(LogisticsFiber::new);

  public static final BooleanProperty DOWN = BooleanProperty.create("down");
  public static final BooleanProperty EAST = BlockStateProperties.EAST;
  public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
  public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
  public static final BooleanProperty UP = BlockStateProperties.UP;
  public static final BooleanProperty WEST = BlockStateProperties.WEST;

  public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

  private static final VoxelShape CORE_SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
  private static final VoxelShape DOWN_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);
  private static final VoxelShape EAST_SHAPE = Block.box(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
  private static final VoxelShape NORTH_SHAPE = Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
  private static final VoxelShape SOUTH_SHAPE = Block.box(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
  private static final VoxelShape UP_SHAPE = Block.box(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
  private static final VoxelShape WEST_SHAPE = Block.box(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);

  private static final Map<Direction, VoxelShape> CONNECTION_SHAPES = Map.of(
      Direction.DOWN, DOWN_SHAPE,
      Direction.NORTH, NORTH_SHAPE,
      Direction.EAST, EAST_SHAPE,
      Direction.SOUTH, SOUTH_SHAPE,
      Direction.WEST, WEST_SHAPE,
      Direction.UP, UP_SHAPE
  );

  private static final Map<Direction, BooleanProperty> CONNECTION_PROPERTIES = Map.of(
      Direction.DOWN, DOWN,
      Direction.NORTH, NORTH,
      Direction.EAST, EAST,
      Direction.SOUTH, SOUTH,
      Direction.WEST, WEST,
      Direction.UP, UP
  );

  public LogisticsFiber(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any()
        .setValue(DOWN, false)
        .setValue(NORTH, false)
        .setValue(EAST, false)
        .setValue(FACING, Direction.NORTH)
        .setValue(SOUTH, false)
        .setValue(WEST, false)
        .setValue(UP, false));
  }

  @Override @Nullable
  public BlockState getStateForPlacement(
      BlockPlaceContext context
  ) {
    return updateConnections(
        this.defaultBlockState().setValue(FACING, determineVisualFacing(context)),
        context.getLevel(),
        context.getClickedPos()
    );
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return world.isClientSide()
        ? null
        : createTickerHelper(type, RegBlockEntities.LOGISTICS_FIBER_ENTITY, LogisticsFiberEntity::tick);
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new LogisticsFiberEntity(pos, state);
  }

  @Override @NonNull
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(DOWN, NORTH, EAST, FACING, SOUTH, WEST, UP);
  }

  @Override @NonNull
  protected VoxelShape getShape(
      @NonNull BlockState state,
      @NonNull BlockGetter world,
      @NonNull BlockPos pos,
      @NonNull CollisionContext context
  ) {
    VoxelShape shape = CORE_SHAPE;

    for (Direction direction : Direction.values()) {
      if (state.getValue(CONNECTION_PROPERTIES.get(direction))) {
        shape = Shapes.or(shape, CONNECTION_SHAPES.get(direction));
      }
    }

    return shape;
  }

  @Override @NonNull
  protected BlockState mirror(
      BlockState state,
      Mirror mirror
  ) {
    Direction facing = state.getValue(FACING);
    boolean north = state.getValue(NORTH);
    boolean east = state.getValue(EAST);
    boolean south = state.getValue(SOUTH);
    boolean west = state.getValue(WEST);

    return switch (mirror) {
      case LEFT_RIGHT -> state
          .setValue(NORTH, south)
          .setValue(EAST, east)
          .setValue(FACING, mirrorFacing(facing, mirror))
          .setValue(SOUTH, north)
          .setValue(WEST, west);
      case FRONT_BACK -> state
          .setValue(NORTH, north)
          .setValue(EAST, west)
          .setValue(FACING, mirrorFacing(facing, mirror))
          .setValue(SOUTH, south)
          .setValue(WEST, east);
      default -> state;
    };
  }

  @Override @NonNull
  protected BlockState rotate(
      BlockState state,
      Rotation rotation
  ) {
    Direction facing = state.getValue(FACING);
    boolean north = state.getValue(NORTH);
    boolean east = state.getValue(EAST);
    boolean south = state.getValue(SOUTH);
    boolean west = state.getValue(WEST);

    return switch (rotation) {
      case NONE -> state.setValue(FACING, facing);
      case CLOCKWISE_90 -> state
          .setValue(NORTH, west)
          .setValue(EAST, north)
          .setValue(FACING, rotation.rotate(facing))
          .setValue(SOUTH, east)
          .setValue(WEST, south);
      case CLOCKWISE_180 -> state
          .setValue(NORTH, south)
          .setValue(EAST, west)
          .setValue(FACING, rotation.rotate(facing))
          .setValue(SOUTH, north)
          .setValue(WEST, east);
      case COUNTERCLOCKWISE_90 -> state
          .setValue(NORTH, east)
          .setValue(EAST, south)
          .setValue(FACING, rotation.rotate(facing))
          .setValue(SOUTH, west)
          .setValue(WEST, north);
    };
  }

  @Override @NonNull
  protected BlockState updateShape(
      @NonNull BlockState state,
      @NonNull LevelReader level,
      @NonNull ScheduledTickAccess ticks,
      @NonNull BlockPos pos,
      @NonNull Direction direction,
      @NonNull BlockPos neighborPos,
      @NonNull BlockState neighborState,
      @NonNull RandomSource random
  ) {
    return state.setValue(CONNECTION_PROPERTIES.get(direction), connectsTo(level, neighborPos, neighborState));
  }

  private static BlockState updateConnections(
      BlockState state,
      LevelReader level,
      BlockPos pos
  ) {
    for (Direction direction : Direction.values()) {
      BlockPos neighborPos = pos.relative(direction);
      state = state.setValue(
          CONNECTION_PROPERTIES.get(direction),
          connectsTo(level, neighborPos, level.getBlockState(neighborPos))
      );
    }

    return state;
  }

  private static Direction determineVisualFacing(
      BlockPlaceContext context
  ) {
    Direction outputDirection = context.getClickedFace();
    BlockPos pos = context.getClickedPos();
    Level level = context.getLevel();

    BlockPos attachedPos = pos.relative(outputDirection.getOpposite());
    if (connectsTo(level, attachedPos, level.getBlockState(attachedPos))) {
      return outputDirection;
    }

    return context.getPlayer() == null
        ? context.getClickedFace()
        : context.getNearestLookingDirection().getOpposite();
  }

  private static Direction mirrorFacing(
      Direction facing,
      Mirror mirror
  ) {
    return switch (mirror) {
      case LEFT_RIGHT -> switch (facing) {
        case NORTH -> Direction.SOUTH;
        case SOUTH -> Direction.NORTH;
        default -> facing;
      };
      case FRONT_BACK -> switch (facing) {
        case EAST -> Direction.WEST;
        case WEST -> Direction.EAST;
        default -> facing;
      };
      default -> facing;
    };
  }

  private static boolean connectsTo(
      LevelReader level,
      BlockPos pos,
      BlockState neighborState
  ) {
    if (neighborState.getBlock() instanceof LogisticsFiber) {
      return true;
    }

    if (level instanceof Level world
        && LogisticsFiberEntity.getContainerAt(world, pos) != null) {
      return true;
    }

    return isContainerBlock(neighborState, pos);
  }

  private static boolean isContainerBlock(
      BlockState state,
      BlockPos pos
  ) {
    if (!state.hasBlockEntity()) {
      return false;
    }

    if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
      return false;
    }

    BlockEntity probe = entityBlock.newBlockEntity(pos, state);
    return probe instanceof net.minecraft.world.Container;
  }

}
