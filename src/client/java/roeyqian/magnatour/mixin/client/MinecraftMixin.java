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
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Lightweight Java Game Library
import org.lwjgl.glfw.GLFW;

// Magnatour
import roeyqian.magnatour.mixin.screen.WindowAccessor;
import roeyqian.magnatour.screen.item.UniverseConsoleScreen;
import roeyqian.magnatour.utility.mixin.client.ClientHelperForEquipment;

@Mixin(value = Minecraft.class, priority = 3600000)
public class MinecraftMixin {

  @Final @Shadow
  public MouseHandler mouseHandler;

  @Shadow
  public Screen screen;

  @Unique
  private double magnatour$savedMouseX = 0.0;
  @Unique
  private double magnatour$savedMouseY = 0.0;

  @Unique
  private boolean magnatour$shouldRestoreMouse = false;

  /* Restore the saved mouse position once the new screen has been fully set up.
   * This runs after releaseMouse() has made the cursor visible again.
   */
  @Inject(method = "setScreen", at = @At("RETURN"))
  private void afterSetScreen(
      Screen newScreen,
      CallbackInfo ci
  ) {
    if (this.magnatour$shouldRestoreMouse && newScreen != null) {
      Minecraft self = (Minecraft) (Object) this;
      long windowHandle = ((WindowAccessor) (Object) self.getWindow()).getHandle();
      GLFW.glfwSetCursorPos(windowHandle, this.magnatour$savedMouseX, this.magnatour$savedMouseY);
      this.magnatour$shouldRestoreMouse = false;
    }
  }

  @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
  private void inStartAttack(
      CallbackInfoReturnable<Boolean> cir
  ) {
    ClientHelperForEquipment.handleStartAttack((Minecraft) (Object) this, cir);
  }

  /* Universe Console: Save mouse cursor position before the Universe Console Screen is closed.
   */
  @Inject(method = "setScreen", at = @At("HEAD"))
  private void onSetScreen(
      Screen newScreen,
      CallbackInfo ci
  ) {
    if (newScreen == null && this.screen instanceof UniverseConsoleScreen) {
      this.magnatour$savedMouseX = this.mouseHandler.xpos();
      this.magnatour$savedMouseY = this.mouseHandler.ypos();
      this.magnatour$shouldRestoreMouse = true;
    }
  }

}
