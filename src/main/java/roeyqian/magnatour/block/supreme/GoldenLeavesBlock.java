/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// JSpecify
import org.jspecify.annotations.NonNull;

public class GoldenLeavesBlock extends LeavesBlock {

  public static final MapCodec<GoldenLeavesBlock> CODEC = simpleCodec(GoldenLeavesBlock::new);

  public GoldenLeavesBlock(
      BlockBehaviour.Properties settings
  ) {
    super(0.1F, settings);
  }

  @Override @NonNull
  public MapCodec<? extends LeavesBlock> codec() {
    return CODEC;
  }

  @Override
  protected void spawnFallingLeavesParticle(
      @NonNull Level world,
      BlockPos pos,
      RandomSource random
  ) {
    double x = pos.getX() + random.nextDouble();
    double y = pos.getY() - 0.05;
    double z = pos.getZ() + random.nextDouble();
  }

}
