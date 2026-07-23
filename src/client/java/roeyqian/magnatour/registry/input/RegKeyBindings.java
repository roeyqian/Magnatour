/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.input;

// Mojang
import com.mojang.blaze3d.platform.InputConstants;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

// Lightweight Java Game Library
import org.lwjgl.glfw.GLFW;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.network.DurableItemModePayload;
import roeyqian.magnatour.item.universe.UniverseBucket;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.item.supreme.SupremeMobile;
import roeyqian.magnatour.item.universe.UniverseConsole;

@Environment(EnvType.CLIENT)
public final class RegKeyBindings {

  public static final KeyMapping.Category UNIVERSE_CATEGORY =
      KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "general"));

  public static final KeyMapping universeModeKey = KeyMappingHelper.registerKeyMapping(
      new KeyMapping(
          "key.magnatour.universe_mode",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_U,
          UNIVERSE_CATEGORY
      )
  );

  private RegKeyBindings() {}

  public static void init() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (RegKeyBindings.universeModeKey.consumeClick()) {
        if (client.player == null) return;

        Item item = client.player.getMainHandItem().getItem();
        if (item instanceof SupremeMobile ||
            item instanceof UniverseUltimaSword ||
            item instanceof UniverseOmniBlade ||
            item instanceof UniverseConsole ||
            item instanceof UniverseBucket) {
          ClientPlayNetworking.send(new DurableItemModePayload());
        }
      }
    });

    Magnatour.LOGGER.info("[Client] Initializing 'RegKeyBindings'");
  }

}
