/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.world;

// Java Standard
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.content.UniverseItems;

public final class WorldHelperForBossFight {

  private static final Map<EnderDragonFight, DragonKillReward> CACHED_KILL_REWARDS =
      Collections.synchronizedMap(new WeakHashMap<>());

  private static final Map<EnderDragonFight, Block> PENDING_REWARD_BLOCKS = Collections.synchronizedMap(
      new WeakHashMap<>()
  );

  private WorldHelperForBossFight() {}

  /**
   * Captures the eligible reward when the dragon starts dying, before vanilla's
   * 100-tick last-player-damage memory expires during its 200-tick death animation.
   */
  public static void cacheDragonKillReward(
      EnderDragonFight fight,
      EnderDragon dragon
  ) {
    if (fight == null) return;

    synchronized (CACHED_KILL_REWARDS) {
      CACHED_KILL_REWARDS.put(
          fight,
          new DragonKillReward(dragon.getUUID(), resolveDragonKillRewardBlock(dragon))
      );
    }
  }

  public static void handleSetDragonKilledHead(
      EnderDragonFight fight,
      boolean hasPreviouslyKilledDragon,
      UUID dragonUUID,
      EnderDragon dragon
  ) {
    boolean shouldSpawnReward =
        hasPreviouslyKilledDragon && dragon.getUUID().equals(dragonUUID);

    synchronized (PENDING_REWARD_BLOCKS) {
      if (!shouldSpawnReward) {
        PENDING_REWARD_BLOCKS.remove(fight);
        return;
      }

      PENDING_REWARD_BLOCKS.put(fight, getCachedKillReward(fight, dragon));
    }
  }

  public static void handleSetDragonKilledTail(
      EnderDragonFight fight,
      ServerLevel level,
      BlockPos origin
  ) {
    Block rewardBlock;
    synchronized (PENDING_REWARD_BLOCKS) {
      rewardBlock = PENDING_REWARD_BLOCKS.remove(fight);
    }
    if (rewardBlock == null) return;

    BlockPos eggPos = level.getHeightmapPos(
        Heightmap.Types.MOTION_BLOCKING,
        EndPodiumFeature.getLocation(origin)
    );
    BlockPos worktablePos = findWorktablePos(level, eggPos);
    level.setBlockAndUpdate(worktablePos, rewardBlock.defaultBlockState());
  }

  private static Block resolveDragonKillRewardBlock(
      EnderDragon dragon
  ) {
    Player player = dragon.getLastHurtByPlayer();
    if (player != null && hasAllUniverseGems(player)) {
      return UniverseBlocks.UNIVERSE_WORKSTATION;
    }

    return SupremeBlocks.SUPREME_WORKTABLE;
  }

  private static Block getCachedKillReward(
      EnderDragonFight fight,
      EnderDragon dragon
  ) {
    synchronized (CACHED_KILL_REWARDS) {
      DragonKillReward reward = CACHED_KILL_REWARDS.remove(fight);
      if (reward != null && reward.dragonUuid().equals(dragon.getUUID())) {
        return reward.block();
      }
    }

    return resolveDragonKillRewardBlock(dragon);
  }

  private static BlockPos findWorktablePos(
      ServerLevel world,
      BlockPos eggPos
  ) {
    for (int radius = 1; radius <= 3; radius++) {
      for (int offsetX = -radius; offsetX <= radius; offsetX++) {
        for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
          if (Math.abs(offsetX) != radius && Math.abs(offsetZ) != radius) continue;

          BlockPos candidatePos = eggPos.offset(offsetX, 0, offsetZ);
          if (canPlaceWorktable(world, candidatePos)) {
            return candidatePos;
          }
        }
      }
    }

    return eggPos.above();
  }

  private static boolean hasAllUniverseGems(
      Player player
  ) {
    return player.getInventory().contains(new ItemStack(UniverseItems.UNIVERSE_GEMRED))
        && player.getInventory().contains(new ItemStack(UniverseItems.UNIVERSE_GEMBLUE))
        && player.getInventory().contains(new ItemStack(UniverseItems.UNIVERSE_GEMYELLOW))
        && player.getInventory().contains(new ItemStack(UniverseItems.UNIVERSE_GEMGREEN))
        && player.getInventory().contains(new ItemStack(UniverseItems.UNIVERSE_GEMBLACK))
        && player.getInventory().contains(new ItemStack(UniverseItems.UNIVERSE_GEMWHITE));
  }

  private static boolean canPlaceWorktable(
      ServerLevel world,
      BlockPos pos
  ) {
    BlockPos belowPos = pos.below();
    return world.getWorldBorder().isWithinBounds(pos)
        && world.getBlockState(pos).canBeReplaced()
        && world.getBlockState(belowPos).isFaceSturdy(world, belowPos, Direction.UP);
  }

  private record DragonKillReward(
      UUID dragonUuid,
      Block block
  ) {}

}
