/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.feature;

// Mojang
import com.mojang.serialization.Codec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class OreTreeFeature extends Feature<OreTreeConfiguration> {

  public OreTreeFeature(
      Codec<OreTreeConfiguration> codec
  ) {
    super(codec);
  }

  @Override
  public boolean place(
      FeaturePlaceContext<OreTreeConfiguration> context
  ) {
    WorldGenLevel level = context.level();
    BlockPos origin = context.origin();
    RandomSource random = context.random();
    OreTreeConfiguration config = context.config();

    int height = config.minHeight();
    int heightRange = config.maxHeight() - config.minHeight();
    if (heightRange > 0) {
      height += random.nextInt(heightRange + 1);
    }

    if (!canRootAt(level, origin)
        || !hasRoomForTree(level, origin, height, config.foliageRadius())) {
      return false;
    }

    placeTrunk(level, origin, height, config.trunk());
    placeFoliage(level, origin, height, config.foliageRadius(), config.foliage());
    return true;
  }

  private static boolean canRootAt(
      WorldGenLevel level,
      BlockPos origin
  ) {
    if (level.isOutsideBuildHeight(origin) || level.isOutsideBuildHeight(origin.below())) {
      return false;
    }

    return level.getBlockState(origin).isAir()
        && !level.getBlockState(origin.below()).isAir();
  }

  private static boolean hasRoomForTree(
      WorldGenLevel level,
      BlockPos origin,
      int height,
      int radius
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (int y = 0; y < height; y++) {
      mutable.setWithOffset(origin, 0, y, 0);
      if (!canReplace(level, mutable)) return false;
    }

    for (int dy = -radius; dy <= radius; dy++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (!isFoliagePosition(dx, dy, dz, radius)) continue;

          int relativeY = height + dy;
          if (relativeY < 0 || isTrunkPosition(dx, relativeY, dz, height)) continue;

          mutable.setWithOffset(origin, dx, relativeY, dz);
          if (!canReplace(level, mutable)) return false;
        }
      }
    }

    return true;
  }

  private static boolean canReplace(
      WorldGenLevel level,
      BlockPos pos
  ) {
    return !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir();
  }

  private static boolean isFoliagePosition(
      int dx,
      int dy,
      int dz,
      int radius
  ) {
    int distance = dx * dx + dy * dy + dz * dz;
    return distance <= radius * radius + 1;
  }

  private static boolean isTrunkPosition(
      int dx,
      int relativeY,
      int dz,
      int height
  ) {
    return dx == 0 && dz == 0 && relativeY >= 0 && relativeY < height;
  }

  private void placeTrunk(
      WorldGenLevel level,
      BlockPos origin,
      int height,
      BlockState state
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    for (int y = 0; y < height; y++) {
      mutable.setWithOffset(origin, 0, y, 0);
      this.setBlock(level, mutable, state);
    }
  }

  private void placeFoliage(
      WorldGenLevel level,
      BlockPos origin,
      int height,
      int radius,
      BlockState state
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (int dy = -radius; dy <= radius; dy++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (!isFoliagePosition(dx, dy, dz, radius)) continue;

          int relativeY = height + dy;
          if (relativeY < 0 || isTrunkPosition(dx, relativeY, dz, height)) continue;

          mutable.setWithOffset(origin, dx, relativeY, dz);
          this.setBlock(level, mutable, state);
        }
      }
    }
  }

}
