/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.world.inventory.MenuType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.menu.universe.UniverseConsoleMenu;
import roeyqian.magnatour.registry.MenuRegHelper;

/*
 * Supreme Group: Item
 * Universe Group: Item
 */
public final class RegItemMenus {

  // Universe Group: Item
  public static final MenuType<UniverseConsoleMenu> UNIVERSE_CONSOLE_HANDLER =
      MenuRegHelper.registerExtended(
          "universe_console",
          (syncId, _, boundBlocks) -> new UniverseConsoleMenu(syncId, boundBlocks),
          UniverseConsole.BoundBlockList.PACKET_CODEC
      );

  private RegItemMenus() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegItemMenus'");
  }

}
