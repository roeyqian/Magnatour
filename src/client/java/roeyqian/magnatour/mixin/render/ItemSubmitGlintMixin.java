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
import net.minecraft.client.renderer.SubmitNodeStorage;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Magnatour
import roeyqian.magnatour.utility.mixin.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = SubmitNodeStorage.ItemSubmit.class, priority = 3600000)
public class ItemSubmitGlintMixin implements RenderHelperForGlint.UniverseGlintHolder {

  @Unique
  private RenderHelperForGlint.GlintType universeGlintType = RenderHelperForGlint.GlintType.NONE;

  @Override
  public void setUniverseGlint(
      RenderHelperForGlint.GlintType glintType
  ) {
    this.universeGlintType = glintType;
  }

  @Override
  public RenderHelperForGlint.GlintType universeGlintType() {
    return this.universeGlintType;
  }

}
