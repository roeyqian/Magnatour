/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.advancements.triggers.CriteriaTriggers;
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
import roeyqian.magnatour.entity.supreme.NetheriteGolem;
import roeyqian.magnatour.entity.supreme.ObsidianGolem;
import roeyqian.magnatour.entity.supreme.PaleLord;
import roeyqian.magnatour.entity.supreme.SculkBehemoth;
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.content.SupremeEntities;

public final class SummonStructureHelper {

  private static final String[][] NETHERITE_GOLEM_AISLES = {
      {"~P~", "###", "~#~"}
  };
  private static final String[][] OBSIDIAN_GOLEM_AISLES = {
      {"~P~", "###", "~#~"}
  };
  private static final String[][] PALE_LORD_AISLES = {
      {"~#~", "#C#", "~#~"}
  };
  private static final String[][] SCULK_BEHEMOTH_AISLES = {
      {"~~~", "###", "C~C"},
      {"~~~", "###", "~~~"},
      {"~~~", "###", "C~C"},
      {"~S~", "~S~", "~~~"}
  };

  private static final Queue<ScheduledSummonCheck> SCHEDULED_SUMMON_CHECKS =
      new ConcurrentLinkedQueue<>();

  @Nullable
  private static BlockPattern[] netheriteGolemPatterns;
  @Nullable
  private static BlockPattern[] obsidianGolemPatterns;
  @Nullable
  private static BlockPattern[] paleLordPatterns;
  @Nullable
  private static BlockPattern[] sculkBehemothPatterns;

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
        || state.is(SupremeBlocks.SUPREME_PUMPKIN_HEAD)
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
        getOrCreateNetheriteGolemPatterns()
    );
    if (match == null) return;

    NetheriteGolem netheriteGolem = SupremeEntities.NETHERITE_GOLEM.create(level, EntitySpawnReason.TRIGGERED);
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
        getOrCreateObsidianGolemPatterns()
    );
    if (match == null) return;

    ObsidianGolem obsidianGolem = SupremeEntities.OBSIDIAN_GOLEM.create(level, EntitySpawnReason.TRIGGERED);
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
        getOrCreatePaleLordPatterns()
    );
    if (match == null) return;

    PaleLord paleLord = SupremeEntities.PALE_LORD.create(level, EntitySpawnReason.TRIGGERED);
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
        getOrCreateSculkBehemothPatterns()
    );
    if (match == null) return;

    SculkBehemoth sculkBehemoth = SupremeEntities.SCULK_BEHEMOTH.create(
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
    return state.is(SupremeBlocks.SUPREME_PUMPKIN_HEAD)
        || state.is(Blocks.NETHERITE_BLOCK);
  }

  private static BlockPattern.BlockPatternMatch findMatchingPattern(
      Level level,
      BlockPos placedPos,
      BlockPattern... patterns
  ) {
    for (BlockPattern pattern : patterns) {
      int horizontalSearchRadius = Math.max(pattern.getWidth(), pattern.getDepth()) - 1;
      int verticalSearchRadius = pattern.getHeight() - 1;

      for (BlockPos origin : BlockPos.betweenClosed(
          placedPos.offset(-horizontalSearchRadius, -verticalSearchRadius, -horizontalSearchRadius),
          placedPos.offset(horizontalSearchRadius, verticalSearchRadius, horizontalSearchRadius)
      )) {
        for (Direction forwards : Direction.Plane.HORIZONTAL) {
          BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, Direction.UP);
          if (match != null && containsPosition(match, placedPos)) return match;
        }
      }
    }

    return null;
  }

  private static BlockPattern[] getOrCreateNetheriteGolemPatterns() {
    if (netheriteGolemPatterns == null) {
      netheriteGolemPatterns = createHorizontalPatternVariants(
          NETHERITE_GOLEM_AISLES,
          builder -> builder
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
          .where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(SupremeBlocks.SUPREME_PUMPKIN_HEAD)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
      );
    }

    return netheriteGolemPatterns;
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
    return state.is(SupremeBlocks.SUPREME_PUMPKIN_HEAD)
        || state.is(Blocks.CRYING_OBSIDIAN);
  }

  private static BlockPattern[] getOrCreateObsidianGolemPatterns() {
    if (obsidianGolemPatterns == null) {
      obsidianGolemPatterns = createHorizontalPatternVariants(
          OBSIDIAN_GOLEM_AISLES,
          builder -> builder
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.CRYING_OBSIDIAN)))
          .where('P', BlockInWorld.hasState(BlockStatePredicate.forBlock(SupremeBlocks.SUPREME_PUMPKIN_HEAD)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
      );
    }

    return obsidianGolemPatterns;
  }

  private static boolean isPaleLordTriggerBlock(
      BlockState state
  ) {
    return state.is(SupremeBlocks.SUPREME_FODDER_BLOCK)
        || state.is(Blocks.CREAKING_HEART);
  }

  private static BlockPattern[] getOrCreatePaleLordPatterns() {
    if (paleLordPatterns == null) {
      paleLordPatterns = createHorizontalPatternVariants(
          PALE_LORD_AISLES,
          builder -> builder
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(SupremeBlocks.SUPREME_FODDER_BLOCK)))
          .where('C', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.CREAKING_HEART)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
      );
    }

    return paleLordPatterns;
  }

  private static boolean isSculkBehemothTriggerBlock(
      BlockState state
  ) {
    return state.is(SupremeBlocks.SUPREME_GEM_BLOCK)
        || state.is(Blocks.SCULK_CATALYST)
        || state.is(Blocks.SCULK);
  }

  private static BlockPattern[] getOrCreateSculkBehemothPatterns() {
    if (sculkBehemothPatterns == null) {
      sculkBehemothPatterns = createHorizontalPatternVariants(
          SCULK_BEHEMOTH_AISLES,
          builder -> builder
          .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(SupremeBlocks.SUPREME_GEM_BLOCK)))
          .where('C', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SCULK_CATALYST)))
          .where('S', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SCULK)))
          .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::isAir))
      );
    }

    return sculkBehemothPatterns;
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

  private static BlockPattern[] createHorizontalPatternVariants(
      String[][] aisles,
      Consumer<BlockPatternBuilder> builderConsumer
  ) {
    String[][] mirroredAisles = mirrorAislesHorizontally(aisles);

    if (Arrays.deepEquals(aisles, mirroredAisles)) {
      return new BlockPattern[] {buildPattern(aisles, builderConsumer)};
    }

    return new BlockPattern[] {
        buildPattern(aisles, builderConsumer),
        buildPattern(mirroredAisles, builderConsumer)
    };
  }

  // Mirror the width axis so non-symmetrical summons can be built in either handedness.
  private static String[][] mirrorAislesHorizontally(
      String[][] aisles
  ) {
    String[][] mirroredAisles = new String[aisles.length][];

    for (int depth = 0; depth < aisles.length; depth++) {
      mirroredAisles[depth] = new String[aisles[depth].length];
      for (int row = 0; row < aisles[depth].length; row++) {
        mirroredAisles[depth][row] = new StringBuilder(aisles[depth][row]).reverse().toString();
      }
    }

    return mirroredAisles;
  }

  private static BlockPattern buildPattern(
      String[][] aisles,
      Consumer<BlockPatternBuilder> builderConsumer
  ) {
    BlockPatternBuilder builder = BlockPatternBuilder.start();
    for (String[] aisle : aisles) {
      builder.aisle(aisle);
    }
    builderConsumer.accept(builder);
    return builder.build();
  }

  private record ScheduledSummonCheck(
      ServerLevel level,
      BlockPos pos
  ) {}

}
