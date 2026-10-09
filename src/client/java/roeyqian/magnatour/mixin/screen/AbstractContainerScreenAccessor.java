/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.screen;

// Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

  @Mutable @Accessor("imageHeight")
  void magnatour$setImageHeight(
      int height
  );

  @Mutable @Accessor("imageWidth")
  void magnatour$setImageWidth(
      int width
  );

}
