/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.gen;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.gen.BlockNetworkManager;
import roeyqian.magnatour.gen.ItemNetworkManager;

public final class RegNetworks {

  private RegNetworks() {}

  public static void init() {
    ItemNetworkManager.registerDurableItemModeNetworking();
    ItemNetworkManager.registerUniverseBucketPickupNetworking();
    ItemNetworkManager.registerUniverseBootsNetworking();
    ItemNetworkManager.registerUniverseConsoleBoundBlockNetworking();

    BlockNetworkManager.registerItemHubNetworking();
    BlockNetworkManager.registerUniverseTeleportPointNetworking();
    BlockNetworkManager.registerRedstoneTriggerNetworking();

    Magnatour.LOGGER.info("[Server] Initializing 'RegNetworks'");
  }

}
