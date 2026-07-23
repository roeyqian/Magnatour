/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.output;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.content.RegBlockEntities;
import roeyqian.magnatour.registry.content.RegItemMenus;
import roeyqian.magnatour.screen.supreme.ItemHubScreen;
import roeyqian.magnatour.renderer.supreme.SupremeChestRenderer;
import roeyqian.magnatour.screen.supreme.RedstoneTriggerScreen;
import roeyqian.magnatour.screen.supreme.SupremeChestScreen;
import roeyqian.magnatour.screen.supreme.SupremeFurnaceScreen;
import roeyqian.magnatour.screen.supreme.SupremeReserverScreen;
import roeyqian.magnatour.screen.supreme.SupremeWorktableScreen;
import roeyqian.magnatour.registry.content.RegBlockMenus;
import roeyqian.magnatour.renderer.universe.UniverseLibraryRenderer;
import roeyqian.magnatour.screen.universe.UniverseLibraryScreen;
import roeyqian.magnatour.screen.universe.UniverseRefineryScreen;
import roeyqian.magnatour.screen.universe.UniverseTeleportPointScreen;
import roeyqian.magnatour.screen.universe.UniverseVoidPoolScreen;
import roeyqian.magnatour.screen.universe.UniverseWorkstationScreen;
import roeyqian.magnatour.screen.universe.UniverseConsoleScreen;

@Environment(EnvType.CLIENT)
public final class RegScreens {

  private RegScreens() {}

  public static void init() {
    MenuScreens.register(RegBlockMenus.SUPREME_FURNACE_HANDLER, SupremeFurnaceScreen::new);
    MenuScreens.register(RegBlockMenus.SUPREME_WORKTABLE_HANDLER, SupremeWorktableScreen::new);
    MenuScreens.register(RegBlockMenus.SUPREME_RESERVER_HANDLER, SupremeReserverScreen::new);
    MenuScreens.register(RegBlockMenus.SUPREME_CHEST_HANDLER, SupremeChestScreen::new);
    MenuScreens.register(RegBlockMenus.REDSTONE_TRIGGER_HANDLER, RedstoneTriggerScreen::new);
    MenuScreens.register(RegBlockMenus.ITEM_HUB_HANDLER, ItemHubScreen::new);

    MenuScreens.register(RegItemMenus.UNIVERSE_CONSOLE_HANDLER, UniverseConsoleScreen::new);
    MenuScreens.register(RegBlockMenus.UNIVERSE_WORKSTATION_HANDLER, UniverseWorkstationScreen::new);
    MenuScreens.register(RegBlockMenus.UNIVERSE_REFINERY_HANDLER, UniverseRefineryScreen::new);
    MenuScreens.register(RegBlockMenus.UNIVERSE_LIBRARY_HANDLER, UniverseLibraryScreen::new);
    MenuScreens.register(RegBlockMenus.UNIVERSE_VOID_POOL_HANDLER, UniverseVoidPoolScreen::new);
    MenuScreens.register(RegBlockMenus.UNIVERSE_TELEPORT_POINT_HANDLER, UniverseTeleportPointScreen::new);

    BlockEntityRenderers.register(RegBlockEntities.UNIVERSE_LIBRARY_ENTITY, UniverseLibraryRenderer::new);
    BlockEntityRenderers.register(RegBlockEntities.SUPREME_CHEST_ENTITY, SupremeChestRenderer::new);

    Magnatour.LOGGER.info("[Client] Initializing 'RegScreens'");
  }

}
