/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.NetworkManagerForItem;
import roeyqian.magnatour.level.NetworkManagerForBlock;

public final class RegNetworks {

  private RegNetworks() {}

  public static void init() {
    NetworkManagerForBlock.registerDurableItemModeNetworking();
    NetworkManagerForBlock.registerUniverseBucketPickupNetworking();
    NetworkManagerForBlock.registerUniverseBootsNetworking();
    NetworkManagerForBlock.registerUniverseConsoleBoundBlockNetworking();

    NetworkManagerForItem.registerItemHubNetworking();
    NetworkManagerForItem.registerUniverseTeleportPointNetworking();
    NetworkManagerForItem.registerRedstoneTriggerNetworking();

    Magnatour.LOGGER.info("[Server] Initializing 'RegNetworks'");
  }

}
