/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.block;

// Minecraft
import net.minecraft.world.level.block.state.BlockState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.registry.content.RegInsertBlocks;

public final class BlockHelperForFarming {

  private BlockHelperForFarming() {}

  public static void handleMayPlaceOn(
      BlockState floor,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (floor.is(RegInsertBlocks.EVER_WATER_FARMLAND)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleStemMayPlaceOn(
      BlockState floor,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (floor.is(RegInsertBlocks.EVER_WATER_FARMLAND)) {
      cir.setReturnValue(true);
    }
  }

}
