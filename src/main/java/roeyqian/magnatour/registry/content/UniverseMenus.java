/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.world.inventory.MenuType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.menu.universe.UniverseAnnihilatorMenu;
import roeyqian.magnatour.menu.universe.UniverseConsoleMenu;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;
import roeyqian.magnatour.menu.universe.UniverseRefineryMenu;
import roeyqian.magnatour.menu.universe.UniverseTeleportPointMenu;
import roeyqian.magnatour.menu.universe.UniverseVoidPoolMenu;
import roeyqian.magnatour.menu.universe.UniverseWorkstationMenu;
import roeyqian.magnatour.registry.MenuRegHelper;

/*
 * Universe Group: All Menus (Block Menus, Item Menus)
 */
public final class UniverseMenus {

  public static final MenuType<UniverseAnnihilatorMenu> UNIVERSE_ANNIHILATOR_HANDLER =
      MenuRegHelper.registerExtended("universe_annihilator",
          (syncId, inventory, data) -> new UniverseAnnihilatorMenu(syncId, data),
          UniverseAnnihilatorMenu.OpeningData.PACKET_CODEC);

  // Block Menus
  public static final MenuType<UniverseLibraryMenu> UNIVERSE_LIBRARY_HANDLER =
      MenuRegHelper.register("universe_library", UniverseLibraryMenu::new);

  public static final MenuType<UniverseTeleportPointMenu> UNIVERSE_TELEPORT_POINT_HANDLER =
      MenuRegHelper.registerExtended(
          "universe_teleport_point",
          (syncId, playerInventory, data) -> new UniverseTeleportPointMenu(syncId, data),
          UniverseTeleportPointMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<UniverseRefineryMenu> UNIVERSE_REFINERY_HANDLER =
      MenuRegHelper.register("universe_refinery", UniverseRefineryMenu::new);

  public static final MenuType<UniverseVoidPoolMenu> UNIVERSE_VOID_POOL_HANDLER =
      MenuRegHelper.register("universe_void_pool", UniverseVoidPoolMenu::new);

  public static final MenuType<UniverseWorkstationMenu> UNIVERSE_WORKSTATION_HANDLER =
      MenuRegHelper.register("universe_workstation", UniverseWorkstationMenu::new);

  // Item Menus
  public static final MenuType<UniverseConsoleMenu> UNIVERSE_CONSOLE_HANDLER =
      MenuRegHelper.registerExtended(
          "universe_console",
          (syncId, _, boundBlocks) -> new UniverseConsoleMenu(syncId, boundBlocks),
          roeyqian.magnatour.item.universe.UniverseConsole.BoundBlockList.PACKET_CODEC
      );

  private UniverseMenus() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'UniverseMenus'");
  }

}
