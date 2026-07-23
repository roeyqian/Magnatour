/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Java Standard
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.blockentity.supreme.SupremeChestEntity;
import roeyqian.magnatour.menu.supreme.SupremeChestContainer;
import roeyqian.magnatour.menu.supreme.SupremeChestMenu;
import roeyqian.magnatour.registry.content.RegBlockEntities;

public class SupremeChest extends BaseEntityBlock implements SimpleWaterloggedBlock, WorldlyContainerHolder {

  public static final MapCodec<SupremeChest> CODEC = simpleCodec(SupremeChest::new);

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

  public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

  public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;

  private static final int MAX_CONNECTED_CHESTS = 3;

  private static final Component TITLE =
      Component.translatable("block.magnatour.supreme_chest");

  private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);

  private static final Map<Direction, VoxelShape> HALF_SHAPES =
      Shapes.rotateHorizontal(Block.boxZ(14.0, 0.0, 14.0, 0.0, 15.0));

  public SupremeChest(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any()
        .setValue(FACING, Direction.NORTH)
        .setValue(TYPE, ChestType.SINGLE)
        .setValue(WATERLOGGED, false));
  }

  @Nullable
  public static WorldlyContainer getCombinedContainer(
      Level level,
      BlockPos pos
  ) {
    List<SupremeChestEntity> chests = getConnectedChests(level, pos);
    if (chests.isEmpty()) return null;
    return new SupremeChestContainer(chests);
  }

  public static List<SupremeChestEntity> getConnectedChestsForRender(
      Level level,
      BlockPos origin
  ) {
    return getConnectedChests(level, origin);
  }

  public static Direction getConnectedDirection(
      BlockState state
  ) {
    Direction facing = state.getValue(FACING);
    return state.getValue(TYPE) == ChestType.LEFT
        ? facing.getClockWise()
        : facing.getCounterClockWise();
  }

  public static float getOpenNess(
      Level level,
      BlockPos origin,
      float tickDelta
  ) {
    if (level.getBlockEntity(origin) instanceof SupremeChestEntity chestEntity) {
      return chestEntity.getAnimationProgress(tickDelta);
    }
    return 0.0F;
  }

  public static void playSound(
      Level level,
      BlockPos pos,
      BlockState state,
      SoundEvent sound
  ) {
    List<BlockPos> positions = getConnectedPositions(level, pos);
    if (positions.isEmpty() || !positions.getLast().equals(pos)) return;

    ChestType type = state.getValue(TYPE);
    double x = pos.getX() + 0.5;
    double y = pos.getY() + 0.5;
    double z = pos.getZ() + 0.5;
    if (type == ChestType.RIGHT) {
      Direction direction = getConnectedDirection(state);
      x += direction.getStepX() * 0.5;
      z += direction.getStepZ() * 0.5;
    }

    level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F,
        level.getRandom().nextFloat() * 0.1F + 0.9F);
  }

  @Override @Nullable
  public WorldlyContainer getContainer(
      @NonNull BlockState state,
      @NonNull LevelAccessor level,
      @NonNull BlockPos pos
  ) {
    if (!(level instanceof Level actualLevel)) return null;
    return getCombinedContainer(actualLevel, pos);
  }

  @Override
  public BlockState getStateForPlacement(
      BlockPlaceContext context
  ) {
    Direction facing = context.getHorizontalDirection().getOpposite();
    FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
    return this.defaultBlockState()
        .setValue(FACING, facing)
        .setValue(TYPE, getChestTypeForPlacement(context.getLevel(), context.getClickedPos(), facing))
        .setValue(WATERLOGGED, fluidState.is(Fluids.WATER));
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level level,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return level.isClientSide()
        ? createTickerHelper(type, RegBlockEntities.SUPREME_CHEST_ENTITY, (_, _, _, be) -> SupremeChestEntity.tick(be))
        : null;
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new SupremeChestEntity(pos, state);
  }

  @Override @NonNull
  public BlockState playerWillDestroy(
      Level level,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      @NonNull Player player
  ) {
    if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SupremeChestEntity chestEntity) {
      Containers.dropContents(level, pos, chestEntity);
      chestEntity.clearContent();
      clearLockedGroup(level, chestEntity);
    }
    return super.playerWillDestroy(level, pos, state, player);
  }

  @Override
  public void setPlacedBy(
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      LivingEntity placer,
      @NonNull ItemStack stack
  ) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (level.isClientSide()) return;

    List<BlockPos> positions = getConnectedPositions(level, pos);
    if (positions.size() == MAX_CONNECTED_CHESTS) {
      lockGroup(level, positions);
    }
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

  @Override @NonNull
  protected MapCodec<? extends SupremeChest> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(FACING, TYPE, WATERLOGGED);
  }

  @Override
  protected int getAnalogOutputSignal(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Direction direction
  ) {
    Container container = getCombinedContainer(level, pos);
    return AbstractContainerMenu.getRedstoneSignalFromContainer(container);
  }

  @Override @NonNull
  protected FluidState getFluidState(
      @NonNull BlockState state
  ) {
    return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
  }

  @Override
  protected MenuProvider getMenuProvider(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos
  ) {
    List<SupremeChestEntity> chests = getConnectedChests(level, pos);
    if (chests.isEmpty()) return null;
    if (isBlocked(level, chests)) return null;

    Container finalContainer = new SupremeChestContainer(chests);
    return new ExtendedMenuProvider<SupremeChestMenu.OpeningData>() {
      @Override
      public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory,
          @NonNull Player player) {
        return new SupremeChestMenu(containerId, inventory, finalContainer, finalContainer.getContainerSize());
      }

      @Override @NonNull
      public Component getDisplayName() {
        return TITLE;
      }

      @Override
      public SupremeChestMenu.OpeningData getScreenOpeningData(@NonNull ServerPlayer player) {
        return new SupremeChestMenu.OpeningData(finalContainer.getContainerSize());
      }
    };
  }

  @Override @NonNull
  protected RenderShape getRenderShape(
      @NonNull BlockState state
  ) {
    return RenderShape.INVISIBLE;
  }

  @Override @NonNull
  protected VoxelShape getShape(
      @NonNull BlockState state,
      @NonNull BlockGetter world,
      @NonNull BlockPos pos,
      @NonNull CollisionContext context
  ) {
    ChestType type = state.getValue(TYPE);
    return switch (type) {
      case SINGLE -> SHAPE;
      case LEFT, RIGHT -> HALF_SHAPES.get(getConnectedDirection(state));
    };
  }

  @Override
  protected boolean hasAnalogOutputSignal(
      @NonNull BlockState state
  ) {
    return true;
  }

  @Override @NonNull
  protected BlockState mirror(
      BlockState state,
      Mirror mirror
  ) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }

  @Override @NonNull
  protected BlockState rotate(
      BlockState state,
      Rotation rotation
  ) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  protected void tick(
      @NonNull BlockState state,
      ServerLevel level,
      @NonNull BlockPos pos,
      @NonNull RandomSource random
  ) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof SupremeChestEntity chestEntity) chestEntity.recheckOpen();
  }

  @Override @NonNull
  protected BlockState updateShape(
      @NonNull BlockState state,
      @NonNull LevelReader level,
      @NonNull ScheduledTickAccess ticks,
      @NonNull BlockPos pos,
      @NonNull Direction directionToNeighbour,
      @NonNull BlockPos neighbourPos,
      @NonNull BlockState neighbourState,
      @NonNull RandomSource random
  ) {
    if (state.getValue(WATERLOGGED)) {
      ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }

    if (directionToNeighbour.getAxis().isHorizontal()) {
      return updateChestType(level, pos, state);
    }

    return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (level instanceof ServerLevel serverLevel) {
      MenuProvider menuProvider = this.getMenuProvider(state, level, pos);
      if (menuProvider != null) {
        player.openMenu(menuProvider);
        player.awardStat(Stats.OPEN_CHEST);
        PiglinAi.angerNearbyPiglins(serverLevel, player, true);
      }
    }
    return InteractionResult.SUCCESS;
  }

  private static List<SupremeChestEntity> getConnectedChests(
      Level level,
      BlockPos origin
  ) {
    List<BlockPos> positions = getConnectedPositions(level, origin);
    if (positions.isEmpty()) return List.of();

    List<SupremeChestEntity> chests = new ArrayList<>();
    for (BlockPos pos : positions) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof SupremeChestEntity chestEntity) chests.add(chestEntity);
    }
    return chests;
  }

  private static List<BlockPos> getConnectedPositions(
      LevelReader level,
      BlockPos origin
  ) {
    BlockState originState = level.getBlockState(origin);
    if (!(originState.getBlock() instanceof SupremeChest) || !originState.hasProperty(FACING)) return List.of();

    Direction facing = originState.getValue(FACING);
    long groupOrigin = getGroupOrigin(level, origin);
    List<BlockPos> positions = collectConnectedLine(level, origin, facing, groupOrigin);
    return trimToMaxConnectedChests(positions, origin);
  }

  private static ChestType getChestTypeForPlacement(
      LevelReader level,
      BlockPos pos,
      Direction facing
  ) {
    List<BlockPos> positions = collectConnectedLine(level, pos, facing, SupremeChestEntity.NO_GROUP_ORIGIN);
    positions = trimToMaxConnectedChests(positions, pos);
    if (positions.size() < 2) return ChestType.SINGLE;
    if (positions.getFirst().equals(pos)) return ChestType.LEFT;
    if (positions.getLast().equals(pos)) return ChestType.RIGHT;
    return ChestType.SINGLE;
  }

  private static void clearLockedGroup(
      Level level,
      SupremeChestEntity chestEntity
  ) {
    if (!chestEntity.hasGroupOrigin()) return;
    BlockPos origin = BlockPos.of(chestEntity.getGroupOrigin());
    Direction facing = chestEntity.getBlockState().getValue(FACING);
    for (BlockPos pos : collectConnectedLine(level, origin, facing, chestEntity.getGroupOrigin())) {
      if (level.getBlockEntity(pos) instanceof SupremeChestEntity other) {
        other.setGroupOrigin(SupremeChestEntity.NO_GROUP_ORIGIN);
      }
    }
  }

  private static void lockGroup(
      Level level,
      List<BlockPos> positions
  ) {
    positions = trimToMaxConnectedChests(new ArrayList<>(positions), positions.getFirst());
    BlockPos origin = positions.getFirst();

    for (int index = 0; index < positions.size(); index++) {
      BlockPos chestPos = positions.get(index);
      if (level.getBlockEntity(chestPos) instanceof SupremeChestEntity chestEntity) {
        chestEntity.setGroupOrigin(origin.asLong());
      }
      BlockState current = level.getBlockState(chestPos);
      if (current.getBlock() instanceof SupremeChest && current.hasProperty(TYPE)) {
        ChestType type = index == 0 ? ChestType.LEFT
            : index == positions.size() - 1 ? ChestType.RIGHT
            : ChestType.SINGLE;
        level.setBlock(chestPos, current.setValue(TYPE, type), 3);
      }
    }
  }

  private static boolean isBlocked(
      Level level,
      List<SupremeChestEntity> chests
  ) {
    for (SupremeChestEntity chest : chests) {
      if (isChestBlockedAt(level, chest.getBlockPos())) return true;
    }
    return false;
  }

  private static BlockState updateChestType(
      LevelReader level,
      BlockPos pos,
      BlockState state
  ) {
    Direction facing = state.getValue(FACING);
    List<BlockPos> positions = collectConnectedLine(level, pos, facing, getGroupOrigin(level, pos));
    positions = trimToMaxConnectedChests(positions, pos);
    if (positions.size() < 2) return state.setValue(TYPE, ChestType.SINGLE);
    if (positions.getFirst().equals(pos)) return state.setValue(TYPE, ChestType.LEFT);
    if (positions.getLast().equals(pos)) return state.setValue(TYPE, ChestType.RIGHT);
    return state.setValue(TYPE, ChestType.SINGLE);
  }

  private static long getGroupOrigin(
      LevelReader level,
      BlockPos pos
  ) {
    if (level.getBlockEntity(pos) instanceof SupremeChestEntity chestEntity && chestEntity.hasGroupOrigin()) {
      return chestEntity.getGroupOrigin();
    }
    return SupremeChestEntity.NO_GROUP_ORIGIN;
  }

  private static List<BlockPos> collectConnectedLine(
      LevelReader level,
      BlockPos origin,
      Direction facing,
      long requiredGroupOrigin
  ) {
    Direction axis = facing.getClockWise();
    List<BlockPos> positions = new ArrayList<>();
    positions.add(origin);
    collectLine(level, origin, axis, facing, requiredGroupOrigin, positions);
    collectLine(level, origin, axis.getOpposite(), facing, requiredGroupOrigin, positions);
    positions.sort(Comparator.comparingLong(a -> project(a, axis)));
    return positions;
  }

  private static List<BlockPos> trimToMaxConnectedChests(
      List<BlockPos> positions,
      BlockPos origin
  ) {
    if (positions.size() <= MAX_CONNECTED_CHESTS) return List.copyOf(positions);

    int originIndex = positions.indexOf(origin);
    if (originIndex < 0) originIndex = 0;
    int start = Math.max(0, Math.min(originIndex - 1, positions.size() - MAX_CONNECTED_CHESTS));
    return List.copyOf(positions.subList(start, start + MAX_CONNECTED_CHESTS));
  }

  private static boolean isChestBlockedAt(
      LevelAccessor level,
      BlockPos pos
  ) {
    return isBlockedChestByBlock(level, pos) || isCatSittingOnChest(level, pos);
  }

  private static void collectLine(
      LevelReader level,
      BlockPos origin,
      Direction direction,
      Direction facing,
      long requiredGroupOrigin,
      List<BlockPos> positions
  ) {
    BlockPos cursor = origin.relative(direction);
    while (isMatchingChest(level, cursor, facing, requiredGroupOrigin)) {
      positions.add(cursor);
      cursor = cursor.relative(direction);
    }
  }

  private static long project(
      BlockPos pos,
      Direction axis
  ) {
    return (long) pos.getX() * axis.getStepX() + (long) pos.getZ() * axis.getStepZ();
  }

  private static boolean isBlockedChestByBlock(
      BlockGetter level,
      BlockPos pos
  ) {
    BlockPos above = pos.above();
    return level.getBlockState(above).isRedstoneConductor(level, above);
  }

  private static boolean isCatSittingOnChest(
      LevelAccessor level,
      BlockPos pos
  ) {
    List<Cat> cats = level.getEntitiesOfClass(
        Cat.class,
        new AABB(pos.getX(), pos.getY() + 1, pos.getZ(),
            pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1)
    );
    for (Cat cat : cats) {
      if (cat.isInSittingPose()) return true;
    }
    return false;
  }

  private static boolean isMatchingChest(
      LevelReader level,
      BlockPos pos,
      Direction facing,
      long requiredGroupOrigin
  ) {
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof SupremeChest)
        || !state.hasProperty(FACING)
        || state.getValue(FACING) != facing) {
      return false;
    }

    long groupOrigin = getGroupOrigin(level, pos);
    return requiredGroupOrigin == SupremeChestEntity.NO_GROUP_ORIGIN
        ? groupOrigin == SupremeChestEntity.NO_GROUP_ORIGIN
        : groupOrigin == requiredGroupOrigin;
  }

}
