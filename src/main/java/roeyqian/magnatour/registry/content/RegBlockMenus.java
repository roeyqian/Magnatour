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
import roeyqian.magnatour.menu.supreme.ItemHubMenu;
import roeyqian.magnatour.menu.supreme.RedstoneTriggerMenu;
import roeyqian.magnatour.menu.supreme.SupremeChestMenu;
import roeyqian.magnatour.menu.supreme.SupremeFurnaceMenu;
import roeyqian.magnatour.menu.supreme.SupremeReserverMenu;
import roeyqian.magnatour.menu.supreme.SupremeWorktableMenu;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;
import roeyqian.magnatour.menu.universe.UniverseRefineryMenu;
import roeyqian.magnatour.menu.universe.UniverseTeleportPointMenu;
import roeyqian.magnatour.menu.universe.UniverseVoidPoolMenu;
import roeyqian.magnatour.menu.universe.UniverseWorkstationMenu;
import roeyqian.magnatour.registry.MenuRegHelper;

/*
 * Supreme Group: Stateless, Stateful
 * Universe Group: Stateless, Stateful
 */
public final class RegBlockMenus {

  public static final MenuType<ItemHubMenu> ITEM_HUB_HANDLER =
      MenuRegHelper.registerExtended(
          "item_hub",
          (syncId, playerInventory, data) -> new ItemHubMenu(syncId, playerInventory, data),
          ItemHubMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<SupremeChestMenu> SUPREME_CHEST_HANDLER =
      MenuRegHelper.registerExtended(
          "supreme_chest",
          (syncId, playerInventory, data) ->
              new SupremeChestMenu(syncId, playerInventory, data.inventorySize()),
          SupremeChestMenu.OpeningData.PACKET_CODEC
      );

  // Supreme Group: Stateful
  public static final MenuType<SupremeFurnaceMenu> SUPREME_FURNACE_HANDLER =
      MenuRegHelper.register("supreme_furnace", SupremeFurnaceMenu::new);

  public static final MenuType<RedstoneTriggerMenu> REDSTONE_TRIGGER_HANDLER =
      MenuRegHelper.registerExtended(
          "redstone_trigger",
          (syncId, playerInventory, data) -> new RedstoneTriggerMenu(syncId, data),
          RedstoneTriggerMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<SupremeReserverMenu> SUPREME_RESERVER_HANDLER =
      MenuRegHelper.register("supreme_reserver", SupremeReserverMenu::new);

  // Supreme Group: Stateless
  public static final MenuType<SupremeWorktableMenu> SUPREME_WORKTABLE_HANDLER =
      MenuRegHelper.register("supreme_worktable", SupremeWorktableMenu::new);

  public static final MenuType<UniverseLibraryMenu> UNIVERSE_LIBRARY_HANDLER =
      MenuRegHelper.register("universe_library", UniverseLibraryMenu::new);

  // Universe Group: Stateful
  public static final MenuType<UniverseRefineryMenu> UNIVERSE_REFINERY_HANDLER =
      MenuRegHelper.register("universe_refinery", UniverseRefineryMenu::new);

  public static final MenuType<UniverseVoidPoolMenu> UNIVERSE_VOID_POOL_HANDLER =
      MenuRegHelper.register("universe_void_pool", UniverseVoidPoolMenu::new);

  // Universe Group: Stateless
  public static final MenuType<UniverseWorkstationMenu> UNIVERSE_WORKSTATION_HANDLER =
      MenuRegHelper.register("universe_workstation", UniverseWorkstationMenu::new);

  public static final MenuType<UniverseTeleportPointMenu> UNIVERSE_TELEPORT_POINT_HANDLER =
      MenuRegHelper.registerExtended(
          "universe_teleport_point",
          (syncId, playerInventory, data) -> new UniverseTeleportPointMenu(syncId, data),
          UniverseTeleportPointMenu.OpeningData.PACKET_CODEC
      );

  private RegBlockMenus() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegBlockMenus'");
  }

}
