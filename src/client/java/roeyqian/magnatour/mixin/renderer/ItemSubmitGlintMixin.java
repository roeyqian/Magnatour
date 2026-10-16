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
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Magnatour
import roeyqian.magnatour.mixinhelper.base.GlintClient;

@Environment(EnvType.CLIENT) @Mixin(value = ItemFeatureRenderer.Submit.class, priority = 3600000)
public class ItemSubmitGlintMixin implements GlintClient.UniverseGlintHolder {

  @Unique
  private GlintClient.GlintType universeGlintType = GlintClient.GlintType.NONE;

  @Override
  public void setUniverseGlint(
      GlintClient.GlintType glintType
  ) {
    this.universeGlintType = glintType;
  }

  @Override
  public GlintClient.GlintType universeGlintType() {
    return this.universeGlintType;
  }

}
