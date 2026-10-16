/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Java Standard
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;

// Magnatour
import roeyqian.magnatour.registry.stuff.MagnaBlocks;

public final class UniverseDragonRewardMain {

  private static final Set<EnderDragon> CACHED_REWARDS =
      Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

  private UniverseDragonRewardMain() {}

  public static void cacheReward(
      EnderDragon dragon
  ) {
    CACHED_REWARDS.add(dragon);
  }

  public static void spawnReward(
      ServerLevel world,
      BlockPos origin
  ) {
    BlockPos eggPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin);
    BlockPos rewardPos = findRewardPos(world, eggPos);
    world.setBlockAndUpdate(rewardPos, MagnaBlocks.UNIVERSE_FORGE.defaultBlockState());
  }

  public static boolean takeCachedReward(
      EnderDragon dragon
  ) {
    return CACHED_REWARDS.remove(dragon);
  }

  private static boolean canPlaceReward(
      ServerLevel world,
      BlockPos pos
  ) {
    BlockPos belowPos = pos.below();
    return world.getWorldBorder().isWithinBounds(pos)
        && world.getBlockState(pos).canBeReplaced()
        && world.getBlockState(belowPos).isFaceSturdy(world, belowPos, Direction.UP);
  }

  private static BlockPos findRewardPos(
      ServerLevel world,
      BlockPos eggPos
  ) {
    for (int radius = 1; radius <= 3; radius++) {
      for (int offsetX = -radius; offsetX <= radius; offsetX++) {
        for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
          if (Math.abs(offsetX) != radius && Math.abs(offsetZ) != radius) continue;

          BlockPos candidatePos = eggPos.offset(offsetX, 0, offsetZ);
          if (canPlaceReward(world, candidatePos)) {
            return candidatePos;
          }
        }
      }
    }

    return eggPos.above();
  }

}
