/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.menu;

// Minecraft
import net.minecraft.world.inventory.MenuType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.menu.block.ItemHubMenu;
import roeyqian.magnatour.menu.block.RedstoneTriggerMenu;
import roeyqian.magnatour.menu.block.SupremeChestMenu;
import roeyqian.magnatour.menu.block.SupremeFurnaceMenu;
import roeyqian.magnatour.menu.block.SupremeReserverMenu;
import roeyqian.magnatour.menu.block.SupremeWorktableMenu;
import roeyqian.magnatour.menu.block.UniverseLibraryMenu;
import roeyqian.magnatour.menu.block.UniverseRefineryMenu;
import roeyqian.magnatour.menu.block.UniverseTeleportPointMenu;
import roeyqian.magnatour.menu.block.UniverseVoidPoolMenu;
import roeyqian.magnatour.menu.block.UniverseWorkstationMenu;

/*
 * Supreme Group: Stateless, Stateful
 * Universe Group: Stateless, Stateful
 */
public final class RegBlockMenus {

  // Supreme Group: Stateless
  public static final MenuType<SupremeWorktableMenu> SUPREME_WORKTABLE_HANDLER =
      MenuRegHelper.register("supreme_worktable", SupremeWorktableMenu::new);

  // Supreme Group: Stateful
  public static final MenuType<SupremeFurnaceMenu> SUPREME_FURNACE_HANDLER =
      MenuRegHelper.register("supreme_furnace", SupremeFurnaceMenu::new);
  public static final MenuType<SupremeReserverMenu> SUPREME_RESERVER_HANDLER =
      MenuRegHelper.register("supreme_reserver", SupremeReserverMenu::new);
  public static final MenuType<SupremeChestMenu> SUPREME_CHEST_HANDLER =
      MenuRegHelper.registerExtended(
          "supreme_chest",
          (syncId, playerInventory, data) ->
              new SupremeChestMenu(syncId, playerInventory, data.inventorySize()),
          SupremeChestMenu.OpeningData.PACKET_CODEC
      );
  public static final MenuType<RedstoneTriggerMenu> REDSTONE_TRIGGER_HANDLER =
      MenuRegHelper.registerExtended(
          "redstone_trigger",
          (syncId, playerInventory, data) -> new RedstoneTriggerMenu(syncId, data),
          RedstoneTriggerMenu.OpeningData.PACKET_CODEC
      );
  public static final MenuType<ItemHubMenu> ITEM_HUB_HANDLER =
      MenuRegHelper.registerExtended(
          "item_hub",
          (syncId, playerInventory, data) -> new ItemHubMenu(syncId, playerInventory, data),
          ItemHubMenu.OpeningData.PACKET_CODEC
      );

  // Universe Group: Stateless
  public static final MenuType<UniverseWorkstationMenu> UNIVERSE_WORKSTATION_HANDLER =
      MenuRegHelper.register("universe_workstation", UniverseWorkstationMenu::new);

  // Universe Group: Stateful
  public static final MenuType<UniverseRefineryMenu> UNIVERSE_REFINERY_HANDLER =
      MenuRegHelper.register("universe_refinery", UniverseRefineryMenu::new);
  public static final MenuType<UniverseVoidPoolMenu> UNIVERSE_VOID_POOL_HANDLER =
      MenuRegHelper.register("universe_void_pool", UniverseVoidPoolMenu::new);
  public static final MenuType<UniverseLibraryMenu> UNIVERSE_LIBRARY_HANDLER =
      MenuRegHelper.register("universe_library", UniverseLibraryMenu::new);
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
