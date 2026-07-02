/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.world.WorldHelperForDimension;

@Mixin(value = NoiseBasedChunkGenerator.class, priority = 3600000)
public class NoiseChunkGeneratorMixin {

  /* Custom Dimension: Build Custom Terrain Surface
   */
  @Inject(method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;"
      + "Lnet/minecraft/world/level/StructureManager;"
      + "Lnet/minecraft/world/level/levelgen/RandomState;"
      + "Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
      at = @At("RETURN"))
  private void inBuildSurface(
      WorldGenRegion region,
      StructureManager structureAccessor,
      RandomState noiseConfig,
      ChunkAccess chunk,
      CallbackInfo ci
  ) {
    WorldHelperForDimension.handleBuildSurface(
        (NoiseBasedChunkGenerator) (Object) this,
        region, noiseConfig, chunk
    );
  }

}
