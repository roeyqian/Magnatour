/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.feature;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

// JSpecify
import org.jspecify.annotations.NonNull;

public final class GiantIceSpikeFeature implements Feature {

  public static final MapCodec<GiantIceSpikeFeature> CODEC =
      MapCodec.unit(GiantIceSpikeFeature::new);

  @NonNull @Override
  public MapCodec<GiantIceSpikeFeature> codec() {
    return CODEC;
  }

  @Override
  public boolean place(
      @NonNull WorldGenLevel level,
      @NonNull ChunkGenerator generator,
      @NonNull RandomSource random,
      @NonNull BlockPos origin
  ) {
    int height = 18 + random.nextInt(37);
    int radius = 3 + random.nextInt(3);
    // WORLD_SURFACE_WG places the origin above the terrain's snow layer.
    // Descend through that layer to root the spike directly on the snow block.
    while (!level.isOutsideBuildHeight(origin.below())
        && level.getBlockState(origin.below()).is(Blocks.SNOW)) {
      origin = origin.below();
    }
    if (!level.getBlockState(origin.below()).is(Blocks.SNOW_BLOCK)
        || level.isOutsideBuildHeight(origin.above(height - 1))) {
      return false;
    }

    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    // Check supported columns before writing. Grass clearings trim the base
    // instead of rejecting an otherwise suitable snow-covered position.
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        if (dx * dx + dz * dz > radius * radius) continue;
        if (!hasFoundation(level, origin, dx, dz)) continue;

        for (int y = 0; y < height; y++) {
          if (!isSpikePosition(dx, y, dz, height, radius)) continue;
          pos.setWithOffset(origin, dx, y, dz);
          BlockState state = level.getBlockState(pos);
          if (!state.isAir() && !state.is(Blocks.SNOW) && !isFoundation(state)) {
            return false;
          }
        }
      }
    }

    BlockState ice = Blocks.BLUE_ICE.defaultBlockState();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        if (dx * dx + dz * dz > radius * radius) continue;
        if (!hasFoundation(level, origin, dx, dz)) continue;
        for (int y = 0; y < height; y++) {
          if (!isSpikePosition(dx, y, dz, height, radius)) continue;
          pos.setWithOffset(origin, dx, y, dz);
          this.setBlock(level, pos, ice);
        }
        // Bury the base so the spike joins the terrain rather than floating.
        for (int depth = 1; depth <= 8; depth++) {
          pos.setWithOffset(origin, dx, -depth, dz);
          if (level.isOutsideBuildHeight(pos)) break;
          BlockState state = level.getBlockState(pos);
          if (!state.isAir() && !state.is(Blocks.SNOW) && !isFoundation(state)) break;
          this.setBlock(level, pos, ice);
        }
      }
    }
    return true;
  }

  private static boolean isFoundation(
      BlockState state
  ) {
    return state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.PACKED_ICE)
        || state.is(Blocks.BLUE_ICE);
  }

  private static boolean hasFoundation(
      WorldGenLevel level,
      BlockPos origin,
      int dx, int dz
  ) {
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int depth = 1; depth <= 8; depth++) {
      pos.setWithOffset(origin, dx, -depth, dz);
      if (level.isOutsideBuildHeight(pos)) return false;
      BlockState state = level.getBlockState(pos);
      if (!state.isAir() && !state.is(Blocks.SNOW)) return isFoundation(state);
    }
    return false;
  }

  private static boolean isSpikePosition(
      int dx, int y, int dz, int height, int radius
  ) {
    double layerRadius = radius * (1.0 - (double) y / height);
    return dx * dx + dz * dz <= layerRadius * layerRadius;
  }

}
