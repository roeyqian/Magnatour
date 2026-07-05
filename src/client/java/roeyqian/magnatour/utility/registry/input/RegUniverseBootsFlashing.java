/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.input;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.Options;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.gen.network.UniverseBootsDashPayload;

@Environment(EnvType.CLIENT)
public final class RegUniverseBootsFlashing {

  private static final long DASH_COOLDOWN = 150;
  private static final long DOUBLE_TAP_WINDOW = 150;
  private static long lastDashTime = 0;
  private static long lastReleaseTime = 0;

  private static boolean waitingSecondTap = false;
  private static boolean wasPressed = false;

  private RegUniverseBootsFlashing() {}

  public static void init() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player == null || client.gui.screen() != null) return;

      if (!client.player.isCrouching()) {
        waitingSecondTap = false;
        wasPressed = false;
        return;
      }

      Options opt = client.options;
      boolean pressed = opt.keyUp.isDown();

      long now = System.currentTimeMillis();

      if (pressed && !wasPressed) {
        if (waitingSecondTap
            && (now - lastReleaseTime) < DOUBLE_TAP_WINDOW
            && (now - lastDashTime) > DASH_COOLDOWN) {
          ClientPlayNetworking.send(new UniverseBootsDashPayload(0));
          lastDashTime = now;
          waitingSecondTap = false;
        }
      } else if (!pressed && wasPressed) {
        lastReleaseTime = now;
        waitingSecondTap = true;
      }

      if (waitingSecondTap && (now - lastReleaseTime) > DOUBLE_TAP_WINDOW) {
        waitingSecondTap = false;
      }

      wasPressed = pressed;
    });

    Magnatour.LOGGER.info("[Client] Initializing 'RegUniverseBootsFlashing'");
  }

}
