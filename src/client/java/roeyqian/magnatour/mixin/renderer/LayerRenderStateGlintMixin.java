/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.renderer;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.renderer.item.ItemStackRenderState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.base.GlintClient;

@Environment(EnvType.CLIENT) @Mixin(value = ItemStackRenderState.LayerRenderState.class, priority = 3600000)
public class LayerRenderStateGlintMixin implements GlintClient.UniverseGlintHolder {

  @Unique
  private GlintClient.GlintType universe$glintType = GlintClient.GlintType.NONE;

  @Override
  public void setUniverseGlint(
      GlintClient.GlintType glintType
  ) {
    this.universe$glintType = glintType;
  }

  @Override
  public GlintClient.GlintType universeGlintType() {
    return this.universe$glintType;
  }

  /* Universe Items: Reset Glint Flag on Clear
   */
  @Inject(method = "clear", at = @At("HEAD"))
  private void inClear(
      CallbackInfo ci
  ) {
    GlintClient.clearGlint(this);
  }

}
