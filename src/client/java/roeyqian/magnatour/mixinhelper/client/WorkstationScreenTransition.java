/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.client;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

// JSpecify
import org.jspecify.annotations.Nullable;

// Lightweight Java Game Library
import org.lwjgl.sdl.SDLMouse;

// Magnatour
import roeyqian.magnatour.screen.universe.UniverseWorkstationCraftingScreen;
import roeyqian.magnatour.screen.universe.UniverseWorkstationSmeltingScreen;

public final class WorkstationScreenTransition {

  private static final long TIMEOUT_NANOS = 5_000_000_000L;

  private static boolean pending;
  private static boolean smelting;

  private static long deadline;

  private static double mouseX;
  private static double mouseY;

  private WorkstationScreenTransition() {}

  public static void afterSetScreen(
      Minecraft client,
      @Nullable Screen newScreen
  ) {
    if (!isPending() || newScreen == null) return;
    pending = false;
    if (!isTarget(newScreen)) return;
    SDLMouse.SDL_WarpMouseInWindow(client.getWindow().handle(), (float) mouseX, (float) mouseY);
    client.mouseHandler.resyncMousePosition();
  }

  public static void beforeSetScreen(
      Minecraft client,
      @Nullable Screen currentScreen, @Nullable Screen newScreen
  ) {
    if (!isPending()) return;
    // Capture the latest position before the close packet grabs and centers the mouse.
    boolean leavingSource = smelting
        ? currentScreen instanceof UniverseWorkstationCraftingScreen
        : currentScreen instanceof UniverseWorkstationSmeltingScreen;
    if (leavingSource && (newScreen == null || isTarget(newScreen))) {
      mouseX = client.mouseHandler.xpos();
      mouseY = client.mouseHandler.ypos();
    }
  }

  public static void begin(
      Minecraft client,
      boolean toSmelting
  ) {
    pending = true;
    smelting = toSmelting;
    deadline = System.nanoTime() + TIMEOUT_NANOS;
    mouseX = client.mouseHandler.xpos();
    mouseY = client.mouseHandler.ypos();
  }

  private static boolean isPending() {
    if (pending && System.nanoTime() - deadline >= 0) pending = false;
    return pending;
  }

  private static boolean isTarget(
      Screen screen
  ) {
    return smelting
        ? screen instanceof UniverseWorkstationSmeltingScreen
        : screen instanceof UniverseWorkstationCraftingScreen;
  }

}
