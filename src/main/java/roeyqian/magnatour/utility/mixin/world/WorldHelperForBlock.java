/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.world;

// Minecraft
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.VirtualBlockLightManager;

public final class WorldHelperForBlock {

  private WorldHelperForBlock() {}

  public static void handleVirtualBlockLight(
      LightChunkGetter chunkSource,
      long pos,
      BlockState state,
      CallbackInfoReturnable<Integer> cir
  ) {
    if (cir.getReturnValueI() >= VirtualBlockLightManager.LIGHT_LEVEL) return;
    if (chunkSource == null) return;

    BlockGetter world = chunkSource.getLevel();
    int virtualLight = VirtualBlockLightManager.getLightEmission(world, pos, state);
    if (virtualLight > cir.getReturnValueI()) cir.setReturnValue(virtualLight);
  }

}
