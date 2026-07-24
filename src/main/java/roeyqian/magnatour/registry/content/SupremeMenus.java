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
import roeyqian.magnatour.registry.MenuRegHelper;

/*
 * Supreme Group: All Menus (Block Menus)
 */
public final class SupremeMenus {

  // Block Menus
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

  public static final MenuType<RedstoneTriggerMenu> REDSTONE_TRIGGER_HANDLER =
      MenuRegHelper.registerExtended(
          "redstone_trigger",
          (syncId, playerInventory, data) -> new RedstoneTriggerMenu(syncId, data),
          RedstoneTriggerMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<SupremeFurnaceMenu> SUPREME_FURNACE_HANDLER =
      MenuRegHelper.register("supreme_furnace", SupremeFurnaceMenu::new);

  public static final MenuType<SupremeReserverMenu> SUPREME_RESERVER_HANDLER =
      MenuRegHelper.register("supreme_reserver", SupremeReserverMenu::new);

  public static final MenuType<SupremeWorktableMenu> SUPREME_WORKTABLE_HANDLER =
      MenuRegHelper.register("supreme_worktable", SupremeWorktableMenu::new);


  private SupremeMenus() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'SupremeMenus'");
  }

}
