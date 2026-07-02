/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.insert.portal;

// Java Standard
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.CustomPortalBlock;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.level.RegDimensions;

public class OreContinentPortal extends Block {

  public static int clientPortalTicks = 0;

  public static boolean clientInPortal = false;

  private static final Set<UUID> IN_PORTAL_THIS_TICK = ConcurrentHashMap.newKeySet();

  private static final Map<UUID, Integer> PORTAL_TICKS = new ConcurrentHashMap<>();

  public OreContinentPortal(
      Properties settings
  ) {
    super(settings);
    this.registerDefaultState(this.stateDefinition.any().setValue(CustomPortalBlock.AXIS, Direction.Axis.X));
  }

  public static void registerTickEvent() {
    ServerTickEvents.END_SERVER_TICK.register(_ -> {
      PORTAL_TICKS.keySet().removeIf(uuid -> !IN_PORTAL_THIS_TICK.contains(uuid));
      IN_PORTAL_THIS_TICK.clear();
    });
  }

  @Override @NonNull
  public VoxelShape getShape(
      @NonNull BlockState state,
      @NonNull BlockGetter world,
      @NonNull BlockPos pos,
      @NonNull CollisionContext context
  ) {
    return CustomPortalBlock.getOutlineShape(state);
  }

  @Override
  public void playerDestroy(
      @NonNull Level world,
      @NonNull Player player,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      BlockEntity blockEntity,
      @NonNull ItemStack tool
  ) {}

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<Block, BlockState> builder
  ) {
    builder.add(CustomPortalBlock.AXIS);
  }

  @Override
  protected void entityInside(
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos,
      @NonNull Entity entity,
      @NonNull InsideBlockEffectApplier handler,
      boolean bl
  ) {
    boolean[] clientFlag = new boolean[]{clientInPortal};
    CustomPortalBlock.handleEntityCollision(world, pos, entity,
        PORTAL_TICKS, IN_PORTAL_THIS_TICK, clientFlag,
        RegInsertBlocks.SUPREME_GEM_BLOCK, RegInsertBlocks.ORE_CONTINENT_PORTAL,
        RegDimensions.ORE_CONTINENT, Level.OVERWORLD);
    clientInPortal = clientFlag[0];
  }

  @Override @NonNull
  protected BlockState updateShape(
      @NonNull BlockState state,
      @NonNull LevelReader world,
      @NonNull ScheduledTickAccess tickView,
      @NonNull BlockPos pos,
      @NonNull Direction direction,
      @NonNull BlockPos neighborPos,
      @NonNull BlockState neighborState,
      @NonNull RandomSource random
  ) {
    if (CustomPortalBlock.shouldBreakPortal(
        RegInsertBlocks.ORE_CONTINENT_PORTAL, RegInsertBlocks.SUPREME_GEM_BLOCK,
        state, neighborState, pos, direction, world
    )) {
      return Blocks.AIR.defaultBlockState();
    }
    return super.updateShape(
        state, world, tickView, pos, direction, neighborPos, neighborState, random
    );
  }

}
