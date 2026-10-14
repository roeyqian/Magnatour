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
import roeyqian.magnatour.menu.universe.UniverseCommandStarMenu;
import roeyqian.magnatour.menu.universe.UniverseDotMenu;
import roeyqian.magnatour.menu.universe.UniverseForgeCraftingMenu;
import roeyqian.magnatour.menu.universe.UniverseForgeSmeltingMenu;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;
import roeyqian.magnatour.menu.universe.UniversePoolMenu;
import roeyqian.magnatour.menu.universe.UniverseRefineryMenu;
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

  public static final MenuType<UniverseDotMenu> UNIVERSE_DOT_HANDLER =
      MenuRegHelper.registerExtended(
          "universe_dot",
          (syncId, playerInventory, data) -> new UniverseDotMenu(syncId, data),
          UniverseDotMenu.OpeningData.PACKET_CODEC
      );

  public static final MenuType<UniverseRefineryMenu> UNIVERSE_REFINERY_HANDLER =
      MenuRegHelper.register("universe_refinery", UniverseRefineryMenu::new);

  public static final MenuType<UniversePoolMenu> UNIVERSE_POOL_HANDLER =
      MenuRegHelper.register("universe_pool", UniversePoolMenu::new);

  public static final MenuType<UniverseForgeCraftingMenu> UNIVERSE_FORGE_HANDLER =
      MenuRegHelper.register("universe_forge", UniverseForgeCraftingMenu::new);

  public static final MenuType<UniverseForgeSmeltingMenu> UNIVERSE_FORGE_SMELTING_HANDLER =
      MenuRegHelper.register("universe_forge_smelting", UniverseForgeSmeltingMenu::new);

  // Item Menus
  public static final MenuType<UniverseCommandStarMenu> UNIVERSE_COMMAND_STAR_HANDLER =
      MenuRegHelper.registerExtended(
          "universe_command_star",
          (syncId, _, boundBlocks) -> new UniverseCommandStarMenu(syncId, boundBlocks),
          roeyqian.magnatour.item.universe.UniverseCommandStar.BoundBlockList.PACKET_CODEC
      );

  private UniverseMenus() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'UniverseMenus'");
  }

}
