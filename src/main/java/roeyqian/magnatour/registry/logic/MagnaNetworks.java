/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Fabric
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.blockentity.supreme.ItemHubPayload;
import roeyqian.magnatour.blockentity.supreme.RedstoneTriggerEntity;
import roeyqian.magnatour.blockentity.supreme.RedstoneTriggerPayload;
import roeyqian.magnatour.blockentity.universe.UniverseDotEntity;
import roeyqian.magnatour.blockentity.universe.UniverseDotPayload;
import roeyqian.magnatour.inventory.supreme.ItemHubMenu;
import roeyqian.magnatour.inventory.supreme.RedstoneTriggerMenu;
import roeyqian.magnatour.inventory.universe.UniverseCommandStarMenu;
import roeyqian.magnatour.inventory.universe.UniverseDotMenu;
import roeyqian.magnatour.item.RemoteAccessManager;
import roeyqian.magnatour.item.supreme.MirrorMobile;
import roeyqian.magnatour.item.supreme.StarAtlas;
import roeyqian.magnatour.item.universe.UniverseBucket;
import roeyqian.magnatour.item.universe.UniverseBucketPayload;
import roeyqian.magnatour.item.universe.UniverseCommandStar;
import roeyqian.magnatour.item.universe.UniverseCommandStarPayload;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.level.CommonModePayload;
import roeyqian.magnatour.mixinhelper.universe.UniverseBucketMain;

public final class MagnaNetworks {

  private MagnaNetworks() {}

  public static void init() {
    StarAtlas.initNetworking();
    registerDurableItemModeNetworking();
    registerUniverseBucketPickupNetworking();
    registerUniverseCommandStarBoundBlockNetworking();

    registerItemHubNetworking();
    registerUniverseDotNetworking();
    registerRedstoneTriggerNetworking();

    Magnatour.LOGGER.info("[Server] Initializing 'MagnaNetworks'");
  }

  public static void registerDurableItemModeNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        CommonModePayload.UNIVERSE_MODE_CHANGE,
        CommonModePayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        CommonModePayload.UNIVERSE_MODE_CHANGE,
        (_, context) -> context.server().execute(() -> {
          ServerPlayer player = context.player();
          ItemStack stack = player.getMainHandItem();
          Item item = stack.getItem();

          switch (item) {
            case MirrorMobile _ -> execTogglingMode(
                player, stack,
                MagnaComponents.MIRROR_MOBILE_MODE, "msg.magnatour.mirror_mobile"
            );
            case UniverseUltimaSword _ -> execTogglingMode(
                player, stack,
                MagnaComponents.UNIVERSE_ULTIMA_SWORD_MODE, "msg.magnatour.universe_ultima_sword"
            );
            case UniverseOmniBlade _ -> execTogglingMode(
                player, stack,
                MagnaComponents.UNIVERSE_OMNI_BLADE_MODE, "msg.magnatour.universe_omni_blade"
            );
            case UniverseCommandStar _ -> execTogglingMode(
                player, stack,
                MagnaComponents.UNIVERSE_COMMAND_STAR_MODE, "msg.magnatour.universe_command_star"
            );
            case UniverseBucket _ -> execTogglingMode(
                player, stack,
                MagnaComponents.UNIVERSE_BUCKET_MODE, "msg.magnatour.universe_bucket"
            );
            default -> {}
          }
        })
    );
  }

  public static void registerUniverseBucketPickupNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseBucketPayload.ID,
        UniverseBucketPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseBucketPayload.ID,
        (_, context) -> context.server().execute(() -> UniverseBucketMain.handleBucketPickup(context.player()))
    );
  }

  public static void registerUniverseCommandStarBoundBlockNetworking() {
    RemoteAccessManager.REMOTE_ACCESS_TICKET = RemoteAccessManager.register();
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseCommandStarPayload.ID,
        UniverseCommandStarPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseCommandStarPayload.ID,
        (payload, context) -> context.server().execute(
            () -> handleUniverseCommandStarBoundBlockAction(payload, context)
        )
    );
  }

  private static void execBlockOpen(
      UniverseCommandStarPayload payload,
      ServerPlayNetworking.Context context
  ) {
    ServerPlayer player = context.player();
    ServerLevel targetWorld = context.server().getLevel(payload.dimension());

    if (targetWorld == null) {
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.universe_command_star.unknown_world")
              .withStyle(ChatFormatting.YELLOW)
      );
      return;
    }

    BlockPos pos = payload.pos();

    RemoteAccessManager.beginOpenRemote(player);
    RemoteAccessManager.startRemoteAccess(player, targetWorld, pos);

    targetWorld.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, true);
    BlockState state = targetWorld.getBlockState(pos);

    if (state.isAir()) {
      RemoteAccessManager.forceEndRemoteAccess(player);
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.universe_command_star.no_target")
              .withStyle(ChatFormatting.YELLOW)
      );
      return;
    }

    try {
      AbstractContainerMenu previousMenu = player.containerMenu;
      BlockHitResult hitResult = new BlockHitResult(
          Vec3.atCenterOf(pos),
          Direction.UP,
          pos,
          false
      );
      InteractionResult actResult = state.useWithoutItem(targetWorld, player, hitResult);

      if (actResult == InteractionResult.PASS || actResult == InteractionResult.FAIL) {
        BlockEntity blockEntity = targetWorld.getBlockEntity(pos);
        if (blockEntity instanceof MenuProvider factory) {
          player.openMenu(factory);
        } else {
          RemoteAccessManager.forceEndRemoteAccess(player);
          player.sendOverlayMessage(
              Component.translatable("msg.magnatour.universe_command_star.unable_open")
                  .withStyle(ChatFormatting.YELLOW)
          );
          return;
        }
      }

      if (player.containerMenu == previousMenu || player.containerMenu == player.inventoryMenu) {
        RemoteAccessManager.forceEndRemoteAccess(player);
      } else {
        RemoteAccessManager.finishOpenRemote(player);
      }
    } catch (Exception e) {
      RemoteAccessManager.forceEndRemoteAccess(player);
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.universe_command_star.error")
              .withStyle(ChatFormatting.YELLOW)
      );
    }
  }

  private static void execBlockRemove(
      UniverseCommandStarPayload payload,
      ServerPlayNetworking.Context context
  ) {
    ServerPlayer player = context.player();
    if (!(player.containerMenu instanceof UniverseCommandStarMenu menu)) return;

    ItemStack commandStarStack = player.getItemInHand(menu.getCommandStarHand());

    if (!(commandStarStack.getItem() instanceof UniverseCommandStar)) return;

    UniverseCommandStar.BoundBlockList currentList = commandStarStack.getOrDefault(
        MagnaComponents.UNIVERSE_COMMAND_STAR_BOUND_LIST,
        UniverseCommandStar.BoundBlockList.EMPTY
    );
    UniverseCommandStar.BoundBlockList newList = currentList.withRemoved(
        payload.pos(), payload.dimension()
    );

    commandStarStack.set(MagnaComponents.UNIVERSE_COMMAND_STAR_BOUND_LIST, newList);
    player.sendOverlayMessage(
        Component.translatable("msg.magnatour.universe_command_star.remove")
            .withStyle(ChatFormatting.GREEN)
    );
  }

  private static void execTogglingMode(
      ServerPlayer player,
      ItemStack stack,
      DataComponentType<Integer> component,
      String translation
  ) {
    int current = stack.getOrDefault(component, 0);
    int next = (current == 0) ? 1 : 0;
    stack.set(component, next);

    Component message = Component.translatable(next == 1 ? translation + ".mode_1" : translation + ".mode_0")
        .withStyle(next == 1 ? ChatFormatting.DARK_RED : ChatFormatting.RED);
    player.sendOverlayMessage(message);
  }

  private static void handleUniverseCommandStarBoundBlockAction(
      UniverseCommandStarPayload payload,
      ServerPlayNetworking.Context context
  ) {
    switch (payload.act()) {
      case OPEN -> execBlockOpen(payload, context);
      case REMOVE -> execBlockRemove(payload, context);
    }
  }

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

  public static void registerUniverseDotNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseDotPayload.ID,
        UniverseDotPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseDotPayload.ID,
        (payload, context) -> context.server().execute(
            () -> handleUniverseDot(payload, context.player())
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

    if (!menu.stillValid(player)) return;
    // Apply individual actions to the current list to preserve concurrent edits.
    switch (payload.action()) {
      case ItemHubPayload.ADD -> itemHubEntity.addAnchoredItem(payload.itemId());
      case ItemHubPayload.REMOVE -> itemHubEntity.removeAnchoredItem(payload.itemId());
      default -> { return; }
    }
    menu.broadcastChanges();
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

  private static void handleUniverseDot(
      UniverseDotPayload payload,
      ServerPlayer player
  ) {
    if (!(player.containerMenu instanceof UniverseDotMenu menu)) return;
    if (!menu.getBlockPos().equals(payload.blockPos())) return;
    player.level();

    ServerLevel targetLevel = player.level().getServer().getLevel(menu.getDimension());
    if (targetLevel == null) return;

    BlockEntity blockEntity = targetLevel.getBlockEntity(menu.getBlockPos());
    if (!(blockEntity instanceof UniverseDotEntity dot)) return;

    switch (payload.action()) {
      case ADD -> {
        boolean added = dot.addDestination(payload.destination());
        player.closeContainer();
        player.sendOverlayMessage(
            Component.translatable(
                added
                    ? "msg.magnatour.universe_dot.added"
                    : "msg.magnatour.universe_dot.full"
            ).withStyle(added ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
        );
      }
      case DELETE -> dot.deleteDestination(payload.index());
      case TELEPORT -> {
        boolean teleported = dot.teleport(player, payload.index());
        if (!teleported) {
          player.sendOverlayMessage(
              Component.translatable("msg.magnatour.universe_dot.invalid")
                  .withStyle(ChatFormatting.RED)
          );
        }
      }
    }
  }

}
