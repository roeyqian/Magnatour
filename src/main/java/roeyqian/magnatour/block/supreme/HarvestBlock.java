/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.SummonStructureHelper;

public class HarvestBlock extends Block {

  public HarvestBlock(
      BlockBehaviour.Properties properties
  ) {
    super(properties);
  }

  @Override
  public void onPlace(
      @NonNull BlockState state,
      @NonNull Level level,
      @NonNull BlockPos pos,
      @NonNull BlockState oldState,
      boolean movedByPiston
  ) {
    if (!oldState.is(state.getBlock())) {
      SummonStructureHelper.trySpawnPaleLordFromPlacedBlock(level, pos, state);
    }
  }

}
