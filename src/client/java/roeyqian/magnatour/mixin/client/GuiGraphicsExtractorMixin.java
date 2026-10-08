/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.item.universe.UniverseLibraryContents;
import roeyqian.magnatour.screen.universe.UniverseLibraryScreen;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {

  @Final @Shadow
  private Minecraft minecraft;

  @Inject(method = "itemCount", at = @At("HEAD"), cancellable = true)
  private void extractLibraryCount(
      Font font,
      ItemStack stack,
      int x,
      int y,
      String countText,
      CallbackInfo ci
  ) {
    if (!(this.minecraft.gui.screen() instanceof UniverseLibraryScreen screen)
        || !screen.isExtractingStorageSlot()) return;
    ci.cancel();
    if (stack.getCount() == 1 && countText == null) return;

    String text = countText == null ? UniverseLibraryContents.formatCount(stack.getCount()) : countText;
    float scale = UniverseLibraryScreen.getCountScale(text);
    UniverseLibraryScreen.drawCount((GuiGraphicsExtractor) (Object) this, font, text,
        x + 17 - font.width(text) * scale, y + 9 + font.lineHeight * (1.0F - scale),
        scale, 0xFFFFFFFF, true);
  }

}
