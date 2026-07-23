/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.world.WorldHelperForBlock;

@Mixin(value = BlockLightEngine.class, priority = 3600000)
public abstract class BlockLightEngineMixin {

  @Unique
  private LightChunkGetter magnatour$chunkSource;

  /* Virtual Block Light: Custom Block Emission
   */
  @Inject(method = "getEmission", at = @At("RETURN"), cancellable = true)
  private void inGetEmission(
      long pos,
      BlockState state,
      CallbackInfoReturnable<Integer> cir
  ) {
    WorldHelperForBlock.handleVirtualBlockLight(this.magnatour$chunkSource, pos, state, cir);
  }

  /* Virtual Block Light: Chunk Source Capture
   */
  @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;)V", at = @At("RETURN"))
  private void inInit(
      LightChunkGetter chunkSource,
      CallbackInfo ci
  ) {
    this.magnatour$chunkSource = chunkSource;
  }

  /* Virtual Block Light: Chunk Source Capture with Section Storage
   */
  @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;"
      + "Lnet/minecraft/world/level/lighting/BlockLightSectionStorage;)V",
      at = @At("RETURN"))
  private void inInit(
      LightChunkGetter chunkSource,
      BlockLightSectionStorage storage,
      CallbackInfo ci
  ) {
    this.magnatour$chunkSource = chunkSource;
  }

}
