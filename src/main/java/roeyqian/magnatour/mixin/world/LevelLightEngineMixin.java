/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.VirtualBlockLightManager;

@Mixin(value = LevelLightEngine.class, priority = 3600000)
public abstract class LevelLightEngineMixin {

  @Unique private LightChunkGetter magnatour$chunkSource;

  @Unique private LayerLightEventListener magnatour$blockListener;
  @Unique private LayerLightEventListener magnatour$skyListener;

  @Inject(method = "getLayerListener", at = @At("RETURN"), cancellable = true)
  private void inGetLayerListener(
      LightLayer layer,
      CallbackInfoReturnable<LayerLightEventListener> cir
  ) {
    LayerLightEventListener listener = layer == LightLayer.SKY
        ? this.magnatour$skyListener : this.magnatour$blockListener;
    if (listener != null) cir.setReturnValue(listener);
  }

  @Inject(method = "getRawBrightness", at = @At("RETURN"), cancellable = true)
  private void inGetRawBrightness(
      BlockPos pos,
      int skyDarken,
      CallbackInfoReturnable<Integer> cir
  ) {
    if (this.magnatour$chunkSource != null
        && VirtualBlockLightManager.isUniverseLit(this.magnatour$chunkSource.getLevel(), pos)) {
      cir.setReturnValue(15);
    }
  }

  @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;ZZ)V", at = @At("RETURN"))
  private void inInit(
      LightChunkGetter chunkSource,
      boolean blockLight,
      boolean skyLight,
      CallbackInfo ci
  ) {
    this.magnatour$chunkSource = chunkSource;
    LevelLightEngine engine = (LevelLightEngine) (Object) this;
    this.magnatour$skyListener = new VirtualBlockLightManager.UniverseLightListener(engine.getLayerListener(LightLayer.SKY), chunkSource);
    // Client block-light reads also return 15 to keep terrain and entities full-bright at night.
    // This read-time overlay never changes the actual light engine storage or saved light data.
    if (chunkSource.getLevel() instanceof Level level && level.isClientSide()) {
      this.magnatour$blockListener = new VirtualBlockLightManager.UniverseLightListener(engine.getLayerListener(LightLayer.BLOCK), chunkSource);
    }
  }

}
