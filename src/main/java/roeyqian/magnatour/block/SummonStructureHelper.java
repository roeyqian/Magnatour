/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

// JSpecify
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.entity.live.NetheriteGolem;
import roeyqian.magnatour.entity.live.ObsidianGolem;
import roeyqian.magnatour.entity.live.PaleLord;
import roeyqian.magnatour.entity.live.SculkBehemoth;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.entity.RegLiveEntities;

public final class SummonStructureHelper {

  private static final int NETHERITE_GOLEM_HEIGHT = 3;
  private static final int NETHERITE_GOLEM_HORIZONTAL_SEARCH_RADIUS = 1;
  private static final int NETHERITE_GOLEM_WIDTH = 3;
  private static final int OBSIDIAN_GOLEM_HEIGHT = 3;
  private static final int OBSIDIAN_GOLEM_HORIZONTAL_SEARCH_RADIUS = 1;
  private static final int OBSIDIAN_GOLEM_WIDTH = 3;
  private static final int PALE_LORD_HEIGHT = 3;
  private static final int PALE_LORD_WIDTH = 3;
  private static final int PALE_LORD_HORIZONTAL_SEARCH_RADIUS = PALE_LORD_WIDTH - 1;
  private static final int SCULK_BEHEMOTH_DEPTH = 4;
  private static final int SCULK_BEHEMOTH_HEIGHT = 3;
  private static final int SCULK_BEHEMOTH_WIDTH = 3;
  private static final int SCULK_BEHEMOTH_HORIZONTAL_SEARCH_RADIUS =
      Math.max(SCULK_BEHEMOTH_WIDTH, SCULK_BEHEMOTH_DEPTH) - 1;

  private static final Queue<ScheduledSummonCheck> SCHEDULED_SUMMON_CHECKS =
      new ConcurrentLinkedQueue<>();

  @Nullable
  private static BlockPattern netheriteGolemPattern;
  @Nullable
  private static BlockPattern obsidianGolemPattern;
  @Nullable
  private static BlockPattern paleLordPattern;
  @Nullable
  private static BlockPattern sculkBehemothPattern;

  private SummonStructureHelper() {}

  public static void execBlockTrigger(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState
  ) {
    if (!oldState.is(state.getBlock()) && isVanillaSummonTriggerBlock(state)) {
      if (level instanceof ServerLevel serverLevel) {
        SCHEDULED_SUMMON_CHECKS.add(new ScheduledSummonCheck(serverLevel, pos.immutable()));
      }
    }
  }

  public static boolean isVanillaSummonTriggerBlock(
      BlockState state
  ) {
    return state.is(Blocks.SCULK_CATALYST)
        || state.is(Blocks.SCULK)
        || state.is(Blocks.CREAKING_HEART)
        || state.is(RegInsertBlocks.SUPREME_PUMPKIN_HEAD)
        || state.is(Blocks.CRYING_OBSIDIAN)
        || state.is(Blocks.NETHERITE_BLOCK);
  }

  public static void registerTickEvent() {
    ServerTickEvents.END_SERVER_TICK.register(_ -> {
      ScheduledSummonCheck check;
      while ((check = SCHEDULED_SUMMON_CHECKS.poll()) != null) {
        ServerLevel level = check.level();
        if (!level.isLoaded(check.pos())) continue;

        BlockState state = level.getBlockState(check.pos());
        trySpawnSculkBehemothFromPlacedBlock(level, check.pos(), state);
        trySpawnPaleLordFromPlacedBlock(level, check.pos(), state);
        trySpawnObsidianGolemFromPlacedBlock(level, check.pos(), state);
        trySpawnNetheriteGolemFromPlacedBlock(level, check.pos(), state);
      }
    });
  }

  public static void trySpawnNetheriteGolemFromPlacedBlock(
      Level level,
      BlockPos pos,
      BlockState state
  ) {
    if (level.isClientSide() || level.getDifficulty() == Difficulty.PEACEFUL) return;
    if (!isNetheriteGolemTriggerBlock(state)) return;

    BlockPattern.BlockPatternMatch match = findMatchingPattern(
        level, pos,
        getOrCreateNetheriteGolemPattern(),
        NETHERITE_GOLEM_HORIZONTAL_SEARCH_RADIUS,
        NETHERITE_GOLEM_HEIGHT
    );
    if (match == null) return;

    NetheriteGolem netheriteGolem = RegLiveEntities.NETHERITE_GOLEM.create(level, EntitySpawnReason.TRIGGERED);
    if (netheriteGolem == null) return;

    netheriteGolem.setPlayerCreated(true);
    clearPatternBlocks(level, match);
    snapEntityToPattern(netheriteGolem, match, match.getBlock(1, 2, 0).getPos());
    level.addFreshEntity(netheriteGolem);
    triggerSummonedEntity(level, netheriteGolem);
    updatePatternBlocks(level, match);
  }

  public static void trySpawnObsidianGolemFromPlacedBlock(
      Level level,
      BlockPos pos,
      BlockState state
  ) {
    if (level.isClientSide() || level.getDifficulty() == Difficulty.PEACEFUL) return;
    if (!isObsidianGolemTriggerBlock(state)) return;

    BlockPattern.BlockPatternMatch match = findMatchingPattern(
        level, pos,
        getOrCreateObsidianGolemPattern(),
        OBSIDIAN_GOLEM_HORIZONTAL_SEARCH_RADIUS,
        OBSIDIAN_GOLEM_HEIGHT
    );
    if (match == null) return;

    ObsidianGolem obsidianGolem = RegLiveEntities.OBSIDIAN_GOLEM.create(level, EntitySpawnReason.TRIGGERED);
    if (obsidianGolem == null) return;

    obsidianGolem.setPlayerCreated(true);
    clearPatternBlocks(level, match);
    snapEntityToPattern(obsidianGolem, match, match.getBlock(1, 2, 0).getPos());
    level.addFreshEntity(obsidianGolem);
    triggerSummonedEntity(level, obsidianGolem);
    updatePatternBlocks(level, match);
  }

  public static void trySpawnPaleLordFromPlacedBlock(
      Level level,
      BlockPos pos,
      BlockState state
  ) {
    if (level.isClientSide() || level.getDifficulty() == Difficulty.PEACEFUL) return;
    if (!isPaleLordTriggerBlock(state)) return;

    BlockPattern.BlockPatternMatch match = findMatchingPattern(
        level, pos,
        getOrCreatePaleLordPattern(),
        PALE_LORD_HORIZONTAL_SEARCH_RADIUS,
        PALE_LORD_HEIGHT
    );
    if (match == null) return;

    PaleLord paleLord = RegLiveEntities.PALE_LORD.create(level, EntitySpawnReason.TRIGGERED);
    if (paleLord == null) return;

    clearPatternBlocks(level, match);
    snapEntityToPattern(paleLord, match, match.getBlock(1, 2, 0).getPos());
    level.addFreshEntity(paleLord);
    triggerSummonedEntity(level, paleLord);
    updatePatternBlocks(level, match);
  }

  public static void trySpawnSculkBehemothFromPlacedBlock(
      Level level,
      BlockPos pos,
      BlockState state
  ) {
    if (level.isClientSide() || level.getDifficulty() == Difficulty.PEACEFUL) return;
    if (!isSculkBehemothTriggerBlock(state)) return;

    BlockPattern.BlockPatternMatch match = findMatchingPattern(
        level, pos,
        getOrCreateSculkBehemothPattern(),
        SCULK_BEHEMOTH_HORIZONTAL_SEARCH_RADIUS,
        SCULK_BEHEMOTH_HEIGHT
    );
    if (match == null) return;

    SculkBehemoth sculkBehemoth = RegLiveEntities.SCULK_BEHEMOTH.create(
        level, EntitySpawnReason.TRIGGERED
    );
    if (sculkBehemoth == null) return;

    clearPatternBlocks(level, match);
    snapEntityToPattern(sculkBehemoth, match, match.getBlock(1, 2, 1).getPos());
    level.addFreshEntity(sculkBehemoth);
    triggerSummonedEntity(level, sculkBehemoth);
    updatePatternBlocks(level, match);
  }

  private static boolean isNetheriteGolemTriggerBlock(
      BlockState state
  ) {
    return state.is(RegInsertBlocks.SUPREME_PUMPKIN_HEAD)
        || state.is(Blocks.NETHERITE_BLOCK);
  }

  private static BlockPattern.BlockPatternMatch findMatchingPattern(
      Level level,
      BlockPos placedPos,
      BlockPattern pattern,
      int horizontalSearchRadius,
      int height
  ) {
    for (BlockPos origin : BlockPos.betweenClosed(
        placedPos.offset(-horizontalSearchRadius, -height + 1, -horizontalSearchRadius),
        placedPos.offset(horizontalSearchRadius, height - 1, horizontalSearchRadius)
    )) {
      for (Direction forwards : Direction.Plane.HORIZONTAL) {
        BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, Direction.UP);
        if (match != null && containsPosition(match, placedPos)) return match;
      }
    }

    return null;
  }

  private static BlockPattern getOrCreateNetheriteGolemPattern() {
    if (netheriteGolemPattern == null) {
      netheriteGolemPattern = BlockPatternBuilder.start()
          .aisle("~P~", "###", "~#~")
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
          .where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(RegInsertBlocks.SUPREME_PUMPKIN_HEAD)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
          .build();
    }

    return netheriteGolemPattern;
  }

  private static void clearPatternBlocks(
      Level level,
      BlockPattern.BlockPatternMatch match
  ) {
    for (int x = 0; x < match.getWidth(); x++) {
      for (int y = 0; y < match.getHeight(); y++) {
        for (int z = 0; z < match.getDepth(); z++) {
          BlockInWorld block = match.getBlock(x, y, z);
          BlockState state = block.getState();
          if (!state.isAir()) {
            level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), 2);
            level.levelEvent(2001, block.getPos(), Block.getId(state));
          }
        }
      }
    }
  }

  private static void snapEntityToPattern(
      Entity entity,
      BlockPattern.BlockPatternMatch match,
      BlockPos spawnPos
  ) {
    entity.snapTo(
        spawnPos.getX() + 0.5,
        spawnPos.getY() + 0.05,
        spawnPos.getZ() + 0.5,
        match.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F,
        0.0F
    );
    entity.setYBodyRot(entity.getYRot());
  }

  private static void triggerSummonedEntity(
      Level level,
      Entity entity
  ) {
    for (ServerPlayer player : level.getEntitiesOfClass(
        ServerPlayer.class, entity.getBoundingBox().inflate(50.0)
    )) {
      CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
    }
  }

  private static void updatePatternBlocks(
      Level level,
      BlockPattern.BlockPatternMatch match
  ) {
    for (int x = 0; x < match.getWidth(); x++) {
      for (int y = 0; y < match.getHeight(); y++) {
        for (int z = 0; z < match.getDepth(); z++) {
          level.updateNeighborsAt(match.getBlock(x, y, z).getPos(), Blocks.AIR);
        }
      }
    }
  }

  private static boolean isObsidianGolemTriggerBlock(
      BlockState state
  ) {
    return state.is(RegInsertBlocks.SUPREME_PUMPKIN_HEAD)
        || state.is(Blocks.CRYING_OBSIDIAN);
  }

  private static BlockPattern getOrCreateObsidianGolemPattern() {
    if (obsidianGolemPattern == null) {
      obsidianGolemPattern = BlockPatternBuilder.start()
          .aisle("~P~", "###", "~#~")
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.CRYING_OBSIDIAN)))
          .where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(RegInsertBlocks.SUPREME_PUMPKIN_HEAD)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
          .build();
    }

    return obsidianGolemPattern;
  }

  private static boolean isPaleLordTriggerBlock(
      BlockState state
  ) {
    return state.is(RegInsertBlocks.SUPREME_FODDER_BLOCK)
        || state.is(Blocks.CREAKING_HEART);
  }

  private static BlockPattern getOrCreatePaleLordPattern() {
    if (paleLordPattern == null) {
      paleLordPattern = BlockPatternBuilder.start()
          .aisle("~#~", "#C#", "~#~")
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(RegInsertBlocks.SUPREME_FODDER_BLOCK)))
          .where('C', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.CREAKING_HEART)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
          .build();
    }

    return paleLordPattern;
  }

  private static boolean isSculkBehemothTriggerBlock(
      BlockState state
  ) {
    return state.is(RegInsertBlocks.SUPREME_GEM_BLOCK)
        || state.is(Blocks.SCULK_CATALYST)
        || state.is(Blocks.SCULK);
  }

  private static BlockPattern getOrCreateSculkBehemothPattern() {
    if (sculkBehemothPattern == null) {
      sculkBehemothPattern = BlockPatternBuilder.start()
          .aisle("~~~", "###", "C~C")
          .aisle("~~~", "###", "~~~")
          .aisle("~~~", "###", "C~C")
          .aisle("~S~", "~S~", "~~~")
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(RegInsertBlocks.SUPREME_GEM_BLOCK)))
          .where('C', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SCULK_CATALYST)))
          .where('S', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SCULK)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
          .build();
    }

    return sculkBehemothPattern;
  }

  private static boolean containsPosition(
      BlockPattern.BlockPatternMatch match,
      BlockPos pos
  ) {
    for (int x = 0; x < match.getWidth(); x++) {
      for (int y = 0; y < match.getHeight(); y++) {
        for (int z = 0; z < match.getDepth(); z++) {
          if (match.getBlock(x, y, z).getPos().equals(pos)) return true;
        }
      }
    }

    return false;
  }

private record ScheduledSummonCheck(
    ServerLevel level,
    BlockPos pos
) {}

}
