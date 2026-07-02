/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.active;

// Java Standard
import java.util.Collections;
import java.util.List;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.active.entity.UniverseLibraryEntity;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class UniverseLibrary extends BaseEntityBlock {

  public static final MapCodec<UniverseLibrary> CODEC = simpleCodec(UniverseLibrary::new);

  public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

  private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);

  public UniverseLibrary(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  public BlockState getStateForPlacement(
      BlockPlaceContext ctx
  ) {
    return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockEntityType<T> type
  ) {
    return createTickerHelper(
        type,
        RegBlockEntities.UNIVERSE_LIBRARY_ENTITY,
        (_, _, _, be) -> UniverseLibraryEntity.tick(be)
    );
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseLibraryEntity(pos, state);
  }

  @Override @NonNull
  public BlockState playerWillDestroy(
      Level world,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      @NonNull Player player
  ) {
    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (!world.isClientSide()) execShulker(world, pos, blockEntity, player);

    return super.playerWillDestroy(world, pos, state, player);
  }

  @Override @NonNull
  protected MapCodec<? extends UniverseLibrary> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(FACING);
  }

  @Override @NonNull
  protected List<ItemStack> getDrops(
      @NonNull BlockState state,
      LootParams.@NonNull Builder builder
  ) {
    return Collections.emptyList();
  }

  @Override @NonNull
  protected VoxelShape getShape(
      @NonNull BlockState state,
      @NonNull BlockGetter world,
      @NonNull BlockPos pos,
      @NonNull CollisionContext context
  ) {
    return SHAPE;
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
      BlockEntity be = world.getBlockEntity(pos);
      if (be instanceof UniverseLibraryEntity libraryBe) openLibrary(libraryBe, world, pos, player);
    }
    return InteractionResult.SUCCESS;
  }

  private void execShulker(
      Level world,
      BlockPos pos,
      BlockEntity blockEntity,
      Player player
  ) {
    if (blockEntity instanceof UniverseLibraryEntity libraryEntity) {
      if (!player.isCreative() || !libraryEntity.isEmpty()) {
        ItemStack dropStack;
        dropStack = new ItemStack(this);
        if (!libraryEntity.isEmpty()) dropStack.applyComponents(blockEntity.collectComponents());

        ItemEntity itemEntity = new ItemEntity(
            world,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            dropStack
        );
        itemEntity.setDefaultPickUpDelay();
        world.addFreshEntity(itemEntity);
      }

      libraryEntity.clearContent();
    }
  }

  private void openLibrary(
      UniverseLibraryEntity libraryBe,
      Level world,
      BlockPos pos,
      Player player
  ) {
    world.blockEvent(pos, this, 1, 1);
    world.playSound(
        null, pos,
        net.minecraft.sounds.SoundEvents.ENDER_CHEST_OPEN,
        net.minecraft.sounds.SoundSource.BLOCKS,
        0.5F, world.getRandom().nextFloat() * 0.1f + 0.9f
    );

    if (!libraryBe.isOpened()) {
      world.blockEvent(pos, this, 1, 1);
      player.openMenu(libraryBe);
      libraryBe.setOpened(true);
    } else {
      world.blockEvent(pos, this, 1, 0);
      libraryBe.setOpened(false);
    }
  }

}
