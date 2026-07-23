/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.item.ItemStackRenderState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = ItemStackRenderState.LayerRenderState.class, priority = 3600000)
public class LayerRenderStateGlintMixin implements RenderHelperForGlint.UniverseGlintHolder {

  @Unique
  private RenderHelperForGlint.GlintType universe$glintType = RenderHelperForGlint.GlintType.NONE;

  @Override
  public void setUniverseGlint(
      RenderHelperForGlint.GlintType glintType
  ) {
    this.universe$glintType = glintType;
  }

  @Override
  public RenderHelperForGlint.GlintType universeGlintType() {
    return this.universe$glintType;
  }

  /* Universe Items: Reset Glint Flag on Clear
   */
  @Inject(method = "clear", at = @At("HEAD"))
  private void inClear(
      CallbackInfo ci
  ) {
    RenderHelperForGlint.clearGlint(this);
  }

}
