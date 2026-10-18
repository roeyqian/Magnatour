/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Fabric
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.VirtualBlockLighter;
import roeyqian.magnatour.block.VirtualBlockLighter.UniverseLightPayload;
import roeyqian.magnatour.entity.universe.UniverseAnnihilation;
import roeyqian.magnatour.mixinhelper.universe.UniverseVoidArmorMain;

public final class MagnaEvents {

  private MagnaEvents() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaEvents'");
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      for (var player : server.getPlayerList().getPlayers()) {
        UniverseVoidArmorMain.dropExcess(player);
      }
    });
    ServerTickEvents.END_SERVER_TICK.register(UniverseAnnihilation::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(UniverseAnnihilation::forget);
    Registry.register(BuiltInRegistries.TICKET_TYPE, "magnatour:universe_light", VirtualBlockLighter.UNIVERSE_TICKET);
    PayloadTypeRegistry.clientboundPlay().register(UniverseLightPayload.ID, UniverseLightPayload.CODEC);
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server)
        -> VirtualBlockLighter.sendUniverseSnapshot(handler.player));
    ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive)
        -> VirtualBlockLighter.sendUniverseSnapshot(newPlayer));
    ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination)
        -> VirtualBlockLighter.sendUniverseSnapshot(player));
    ServerTickEvents.END_LEVEL_TICK.register(VirtualBlockLighter::tick);

    ServerChunkEvents.CHUNK_LOAD.register((world, chunk, _) -> VirtualBlockLighter.onChunkLoad(world, chunk));
    ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> VirtualBlockLighter.onChunkUnload(world, chunk.getPos()));
  }

}
