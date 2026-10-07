/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.supreme.StarAtlas;
import roeyqian.magnatour.level.NetworkManagerForBlock;
import roeyqian.magnatour.level.NetworkManagerForItem;

public final class CustomNetworks {

  private CustomNetworks() {}

  public static void init() {
    StarAtlas.initNetworking();
    NetworkManagerForBlock.registerDurableItemModeNetworking();
    NetworkManagerForBlock.registerUniverseBucketPickupNetworking();
    NetworkManagerForBlock.registerUniverseBootsNetworking();
    NetworkManagerForBlock.registerUniverseConsoleBoundBlockNetworking();

    NetworkManagerForItem.registerItemHubNetworking();
    NetworkManagerForItem.registerUniverseTeleportPointNetworking();
    NetworkManagerForItem.registerRedstoneTriggerNetworking();

    Magnatour.LOGGER.info("[Server] Initializing 'CustomNetworks'");
  }

}
