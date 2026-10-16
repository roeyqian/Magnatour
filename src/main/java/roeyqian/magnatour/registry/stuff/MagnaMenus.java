/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.stuff;

// Minecraft
import net.minecraft.world.inventory.MenuType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.menu.supreme.ItemHubMenu;
import roeyqian.magnatour.menu.supreme.RedstoneTriggerMenu;
import roeyqian.magnatour.menu.supreme.SupremeChestMenu;
import roeyqian.magnatour.menu.supreme.SupremeCraftableMenu;
import roeyqian.magnatour.menu.supreme.SupremeFurnaceMenu;
import roeyqian.magnatour.menu.supreme.SupremeReserverMenu;
import roeyqian.magnatour.menu.universe.UniverseAnnihilatorMenu;
import roeyqian.magnatour.menu.universe.UniverseCommandStarMenu;
import roeyqian.magnatour.menu.universe.UniverseDotMenu;
import roeyqian.magnatour.menu.universe.UniverseForgeCraftingMenu;
import roeyqian.magnatour.menu.universe.UniverseForgeSmeltingMenu;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;
import roeyqian.magnatour.menu.universe.UniversePoolMenu;
import roeyqian.magnatour.menu.universe.UniverseRefineryMenu;
import roeyqian.magnatour.registry.StuffHelper;

/*
 * Supreme and Universe Groups: All Menus (Block Menus, Item Menus)
 */
public final class MagnaMenus {

  // Supreme Group
  // Block Menus
  public static final MenuType<ItemHubMenu> ITEM_HUB_HANDLER =
      StuffHelper.registerExtended(
          "item_hub",
          (syncId, playerInventory, data) -> new ItemHubMenu(syncId, playerInventory, data),
          ItemHubMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<SupremeChestMenu> SUPREME_CHEST_HANDLER =
      StuffHelper.registerExtended(
          "supreme_chest",
          (syncId, playerInventory, data) ->
              new SupremeChestMenu(syncId, playerInventory, data.inventorySize()),
          SupremeChestMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<RedstoneTriggerMenu> REDSTONE_TRIGGER_HANDLER =
      StuffHelper.registerExtended(
          "redstone_trigger",
          (syncId, playerInventory, data) -> new RedstoneTriggerMenu(syncId, data),
          RedstoneTriggerMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<SupremeFurnaceMenu> SUPREME_FURNACE_HANDLER =
      StuffHelper.register("supreme_furnace", SupremeFurnaceMenu::new);

  public static final MenuType<SupremeReserverMenu> SUPREME_RESERVER_HANDLER =
      StuffHelper.register("supreme_reserver", SupremeReserverMenu::new);

  public static final MenuType<SupremeCraftableMenu> SUPREME_CRAFTABLE_HANDLER =
      StuffHelper.register("supreme_craftable", SupremeCraftableMenu::new);

  // Universe Group
  public static final MenuType<UniverseAnnihilatorMenu> UNIVERSE_ANNIHILATOR_HANDLER =
      StuffHelper.registerExtended("universe_annihilator",
          (syncId, inventory, data) -> new UniverseAnnihilatorMenu(syncId, data),
          UniverseAnnihilatorMenu.OpeningData.PACKET_CODEC);

  // Block Menus
  public static final MenuType<UniverseLibraryMenu> UNIVERSE_LIBRARY_HANDLER =
      StuffHelper.register("universe_library", UniverseLibraryMenu::new);

  public static final MenuType<UniverseDotMenu> UNIVERSE_DOT_HANDLER =
      StuffHelper.registerExtended(
          "universe_dot",
          (syncId, playerInventory, data) -> new UniverseDotMenu(syncId, data),
          UniverseDotMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<UniverseRefineryMenu> UNIVERSE_REFINERY_HANDLER =
      StuffHelper.register("universe_refinery", UniverseRefineryMenu::new);

  public static final MenuType<UniversePoolMenu> UNIVERSE_POOL_HANDLER =
      StuffHelper.register("universe_pool", UniversePoolMenu::new);

  public static final MenuType<UniverseForgeCraftingMenu> UNIVERSE_FORGE_HANDLER =
      StuffHelper.register("universe_forge", UniverseForgeCraftingMenu::new);

  public static final MenuType<UniverseForgeSmeltingMenu> UNIVERSE_FORGE_SMELTING_HANDLER =
      StuffHelper.register("universe_forge_smelting", UniverseForgeSmeltingMenu::new);

  // Item Menus
  public static final MenuType<UniverseCommandStarMenu> UNIVERSE_COMMAND_STAR_HANDLER =
      StuffHelper.registerExtended(
          "universe_command_star",
          (syncId, _, boundBlocks) -> new UniverseCommandStarMenu(syncId, boundBlocks),
          roeyqian.magnatour.item.universe.UniverseCommandStar.BoundBlockList.PACKET_CODEC
      );

  private MagnaMenus() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaMenus'");
  }

}
