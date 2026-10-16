/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Java Standard
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

// Lightweight Java Game Library
import org.lwjgl.sdl.SDLMouse;

// Magnatour
import roeyqian.magnatour.screen.universe.UniverseCommandStarScreen;

@Environment(EnvType.CLIENT)
public final class UniverseCommandStarClient {

  private static final Map<Minecraft, MouseRestoreState> SAVED_MOUSE_POSITIONS = Collections.synchronizedMap(
      new WeakHashMap<>()
  );

  private UniverseCommandStarClient() {}

  public static void handleAfterSetScreen(
      Minecraft client,
      Screen newScreen
  ) {
    if (newScreen == null) return;

    MouseRestoreState state;
    synchronized (SAVED_MOUSE_POSITIONS) {
      state = SAVED_MOUSE_POSITIONS.remove(client);
    }
    if (state == null) return;

    SDLMouse.SDL_WarpMouseInWindow(client.getWindow().handle(), (float) state.mouseX, (float) state.mouseY);
  }

  public static void handleBeforeSetScreen(
      Minecraft client,
      MouseHandler mouseHandler,
      Screen currentScreen, Screen newScreen
  ) {
    if (newScreen != null || !(currentScreen instanceof UniverseCommandStarScreen)) return;

    synchronized (SAVED_MOUSE_POSITIONS) {
      SAVED_MOUSE_POSITIONS.put(client, new MouseRestoreState(mouseHandler.xpos(), mouseHandler.ypos()));
    }
  }

  private static final class MouseRestoreState {

    private final double mouseX;
    private final double mouseY;

    private MouseRestoreState(
        double mouseX, double mouseY
    ) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
    }

  }

}
