/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.gen;

// Fabric
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

// Magnatour
import roeyqian.magnatour.block.active.entity.RedstoneTriggerEntity;
import roeyqian.magnatour.block.active.entity.ItemHubEntity;
import roeyqian.magnatour.block.active.entity.UniverseTeleportPointEntity;
import roeyqian.magnatour.gen.network.ItemHubPayload;
import roeyqian.magnatour.gen.network.RedstoneTriggerPayload;
import roeyqian.magnatour.menu.block.ItemHubMenu;
import roeyqian.magnatour.menu.block.RedstoneTriggerMenu;
import roeyqian.magnatour.gen.network.UniverseTeleportPointPayload;
import roeyqian.magnatour.menu.block.UniverseTeleportPointMenu;

public final class BlockNetworkManager {

  private BlockNetworkManager() {}

  public static void registerItemHubNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        ItemHubPayload.ID,
        ItemHubPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        ItemHubPayload.ID,
        (payload, context) -> context.server().execute(
            () -> handleItemHub(payload, context.player())
        )
    );
  }

  public static void registerRedstoneTriggerNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        RedstoneTriggerPayload.ID,
        RedstoneTriggerPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        RedstoneTriggerPayload.ID,
        (payload, context) -> context.server().execute(
            () -> handleRedstoneTrigger(payload, context.player())
        )
    );
  }

  public static void registerUniverseTeleportPointNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseTeleportPointPayload.ID,
        UniverseTeleportPointPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseTeleportPointPayload.ID,
        (payload, context) -> context.server().execute(
            () -> handleUniverseTeleportPoint(payload, context.player())
        )
    );
  }

  private static void handleItemHub(
      ItemHubPayload payload,
      ServerPlayer player
  ) {
    if (!(player.containerMenu instanceof ItemHubMenu menu)) return;
    if (!menu.getBlockPos().equals(payload.blockPos())) return;
    if (!menu.getDimension().equals(payload.dimension())) return;

    ServerLevel targetLevel = player.level().getServer().getLevel(payload.dimension());
    if (targetLevel == null) return;

    BlockEntity blockEntity = targetLevel.getBlockEntity(payload.blockPos());
    if (!(blockEntity instanceof ItemHubEntity itemHubEntity)) return;

    itemHubEntity.applyFilterItemId(payload.filterItemId());
  }

  private static void handleRedstoneTrigger(
      RedstoneTriggerPayload payload,
      ServerPlayer player
  ) {
    if (!(player.containerMenu instanceof RedstoneTriggerMenu menu)) return;
    if (!menu.getBlockPos().equals(payload.blockPos())) return;
    if (!menu.getDimension().equals(payload.dimension())) return;

    ServerLevel targetLevel = player.level().getServer().getLevel(payload.dimension());
    if (targetLevel == null) return;

    BlockEntity blockEntity = targetLevel.getBlockEntity(payload.blockPos());
    if (!(blockEntity instanceof RedstoneTriggerEntity triggerEntity)) return;

    triggerEntity.applySettings(
        payload.mode(),
        payload.enabled(),
        payload.intervalTicks()
    );
  }

  private static void handleUniverseTeleportPoint(
      UniverseTeleportPointPayload payload,
      ServerPlayer player
  ) {
    if (!(player.containerMenu instanceof UniverseTeleportPointMenu menu)) return;
    if (!menu.getBlockPos().equals(payload.blockPos())) return;
    player.level();

    ServerLevel targetLevel = player.level().getServer().getLevel(menu.getDimension());
    if (targetLevel == null) return;

    BlockEntity blockEntity = targetLevel.getBlockEntity(menu.getBlockPos());
    if (!(blockEntity instanceof UniverseTeleportPointEntity teleportPoint)) return;

    switch (payload.action()) {
      case ADD -> {
        boolean added = teleportPoint.addDestination(payload.destination());
        player.closeContainer();
        player.sendOverlayMessage(
            Component.translatable(
                added
                    ? "msg.magnatour.universe_teleport_point.added"
                    : "msg.magnatour.universe_teleport_point.full"
            ).withStyle(added ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
        );
      }
      case DELETE -> teleportPoint.deleteDestination(payload.index());
      case TELEPORT -> {
        boolean teleported = teleportPoint.teleport(player, payload.index());
        if (!teleported) {
          player.sendOverlayMessage(
              Component.translatable("msg.magnatour.universe_teleport_point.invalid")
                  .withStyle(ChatFormatting.RED)
          );
        }
      }
    }
  }

}
