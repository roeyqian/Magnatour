/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// Magnatour
import roeyqian.magnatour.block.SummonStructureHelper;

public final class SummonTriggerMain {

  private SummonTriggerMain() {}

  public static void handleVanillaSummonTriggerOnPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState
  ) {
    SummonStructureHelper.execBlockTrigger(state, level, pos, oldState);
  }

}
