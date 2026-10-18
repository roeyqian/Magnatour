/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.output;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.stuff.MagnaBlockEntities;
import roeyqian.magnatour.registry.stuff.MagnaMenus;
import roeyqian.magnatour.render.supreme.SupremeChestRenderer;
import roeyqian.magnatour.render.universe.UniverseLibraryRenderer;
import roeyqian.magnatour.screen.supreme.ItemHubScreen;
import roeyqian.magnatour.screen.supreme.RedstoneTriggerScreen;
import roeyqian.magnatour.screen.supreme.SupremeChestScreen;
import roeyqian.magnatour.screen.supreme.SupremeCraftableScreen;
import roeyqian.magnatour.screen.supreme.SupremeFurnaceScreen;
import roeyqian.magnatour.screen.supreme.SupremeReserverScreen;
import roeyqian.magnatour.screen.universe.UniverseAnnihilatorScreen;
import roeyqian.magnatour.screen.universe.UniverseCommandStarScreen;
import roeyqian.magnatour.screen.universe.UniverseDotScreen;
import roeyqian.magnatour.screen.universe.UniverseForgeCraftingScreen;
import roeyqian.magnatour.screen.universe.UniverseForgeSmeltingScreen;
import roeyqian.magnatour.screen.universe.UniverseLibraryScreen;
import roeyqian.magnatour.screen.universe.UniversePoolScreen;
import roeyqian.magnatour.screen.universe.UniverseRefineryScreen;

@Environment(EnvType.CLIENT)
public final class MagnaScreens {

  private MagnaScreens() {}

  public static void init() {
    MenuScreens.register(MagnaMenus.UNIVERSE_ANNIHILATOR_HANDLER, UniverseAnnihilatorScreen::new);
    MenuScreens.register(MagnaMenus.SUPREME_FURNACE_HANDLER, SupremeFurnaceScreen::new);
    MenuScreens.register(MagnaMenus.SUPREME_CRAFTABLE_HANDLER, SupremeCraftableScreen::new);
    MenuScreens.register(MagnaMenus.SUPREME_RESERVER_HANDLER, SupremeReserverScreen::new);
    MenuScreens.register(MagnaMenus.SUPREME_CHEST_HANDLER, SupremeChestScreen::new);
    MenuScreens.register(MagnaMenus.REDSTONE_TRIGGER_HANDLER, RedstoneTriggerScreen::new);
    MenuScreens.register(MagnaMenus.ITEM_HUB_HANDLER, ItemHubScreen::new);

    MenuScreens.register(MagnaMenus.UNIVERSE_COMMAND_STAR_HANDLER, UniverseCommandStarScreen::new);
    MenuScreens.register(MagnaMenus.UNIVERSE_FORGE_HANDLER, UniverseForgeCraftingScreen::new);
    MenuScreens.register(MagnaMenus.UNIVERSE_FORGE_SMELTING_HANDLER, UniverseForgeSmeltingScreen::new);
    MenuScreens.register(MagnaMenus.UNIVERSE_REFINERY_HANDLER, UniverseRefineryScreen::new);
    MenuScreens.register(MagnaMenus.UNIVERSE_LIBRARY_HANDLER, UniverseLibraryScreen::new);
    MenuScreens.register(MagnaMenus.UNIVERSE_POOL_HANDLER, UniversePoolScreen::new);
    MenuScreens.register(MagnaMenus.UNIVERSE_DOT_HANDLER, UniverseDotScreen::new);

    BlockEntityRenderers.register(MagnaBlockEntities.UNIVERSE_LIBRARY_ENTITY, UniverseLibraryRenderer::new);
    BlockEntityRenderers.register(MagnaBlockEntities.SUPREME_CHEST_ENTITY, SupremeChestRenderer::new);

    Magnatour.LOGGER.info("[Client] Initializing 'MagnaScreens'");
  }

}
