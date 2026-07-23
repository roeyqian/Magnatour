/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.client.ClientHelperForEquipment;

@Mixin(value = Gui.class, priority = 3600000)
public class GuiMixin {

  @Final @Shadow
  private Minecraft minecraft;

  @Shadow
  private Screen screen;

  /* Restore the saved mouse position once the new screen has been fully set up.
   */
  @Inject(method = "setScreen", at = @At("RETURN"))
  private void afterSetScreen(
      Screen newScreen,
      CallbackInfo ci
  ) {
    ClientHelperForEquipment.handleAfterSetScreen(this.minecraft, newScreen);
  }

  /* Universe Console: Save mouse cursor position before the Universe Console Screen is closed.
   */
  @Inject(method = "setScreen", at = @At("HEAD"))
  private void onSetScreen(
      Screen newScreen,
      CallbackInfo ci
  ) {
    ClientHelperForEquipment.handleBeforeSetScreen(
        this.minecraft,
        this.minecraft.mouseHandler,
        this.screen,
        newScreen
    );
  }

}
