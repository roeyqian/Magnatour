/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Java Standard
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

// Fabric
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.level.network.DurableItemModePayload;
import roeyqian.magnatour.level.network.UniverseBootsDashPayload;
import roeyqian.magnatour.level.network.UniverseBucketPickupPayload;
import roeyqian.magnatour.level.network.UniverseConsoleBoundBlockPayload;
import roeyqian.magnatour.item.RemoteAccessManager;
import roeyqian.magnatour.item.supreme.SupremeMobile;
import roeyqian.magnatour.item.universe.UniverseBucket;
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.mixinhelper.server.ServerHelperForEquipment;
import roeyqian.magnatour.registry.logic.CustomComponents;
import roeyqian.magnatour.registry.content.UniverseItems;

public final class NetworkManagerForBlock {

  public static final long UNIVERSE_BOOTS_COOLDOWN_MS = 150;

  public static final double UNIVERSE_BOOTS_DASH_DISTANCE = 5.0;

  public static final HashMap<UUID, Long> cooldowns = new HashMap<>();

  private NetworkManagerForBlock() {}

  public static void registerDurableItemModeNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        DurableItemModePayload.UNIVERSE_MODE_CHANGE,
        DurableItemModePayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        DurableItemModePayload.UNIVERSE_MODE_CHANGE,
        (_, context) -> context.server().execute(() -> {
          ServerPlayer player = context.player();
          ItemStack stack = player.getMainHandItem();
          Item item = stack.getItem();

          switch (item) {
            case SupremeMobile _ -> execTogglingMode(
                player, stack,
                CustomComponents.SUPREME_MOBILE_MODE, "msg.magnatour.supreme_mobile"
            );
            case UniverseUltimaSword _ -> execTogglingMode(
                player, stack,
                CustomComponents.UNIVERSE_ULTIMA_SWORD_MODE, "msg.magnatour.universe_ultima_sword"
            );
            case UniverseOmniBlade _ -> execTogglingMode(
                player, stack,
                CustomComponents.UNIVERSE_OMNI_BLADE_MODE, "msg.magnatour.universe_omni_blade"
            );
            case UniverseConsole _ -> execTogglingMode(
                player, stack,
                CustomComponents.UNIVERSE_CONSOLE_MODE, "msg.magnatour.universe_console"
            );
            case UniverseBucket _ -> execTogglingMode(
                player, stack,
                CustomComponents.UNIVERSE_BUCKET_MODE, "msg.magnatour.universe_bucket"
            );
            default -> {}
          }
        })
    );
  }

  public static void registerUniverseBootsNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseBootsDashPayload.ID,
        UniverseBootsDashPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseBootsDashPayload.ID,
        (payload, context) -> {
          ServerPlayer player = context.player();
          int dir = payload.direction();

          context.server().execute(() -> {
            if (dir < 0 || dir > 3) return;
            if (!player.isShiftKeyDown()) return;
            if (!(player.getItemBySlot(EquipmentSlot.FEET).is(UniverseItems.UNIVERSE_BOOTS))) {
              return;
            }

            long now = System.currentTimeMillis();
            long cooldown = cooldowns.getOrDefault(player.getUUID(), 0L);
            if (now - cooldown < UNIVERSE_BOOTS_COOLDOWN_MS) return;
            cooldowns.put(player.getUUID(), now);

            Vec3 dashVec = calcDirection(player).scale(UNIVERSE_BOOTS_DASH_DISTANCE);
            Vec3 target = player.position().add(dashVec);

            ServerLevel world = player.level();
            world.sendParticles(
                ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5,
                player.getZ(), 15,
                0.3, 0.3,
                0.3, 0.05
            );

            player.teleportTo(
                world,
                target.x, target.y,
                target.z, Set.of(),
                player.getYRot(), player.getXRot(),
                false
            );

            world.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                target.x, target.y + 0.5,
                target.z, 20,
                0.3, 0.5,
                0.3, 0.05
            );
            world.playSound(
                null,
                player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F, 1.0F
            );
          });
        }
    );
  }

  public static void registerUniverseBucketPickupNetworking() {
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseBucketPickupPayload.ID,
        UniverseBucketPickupPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseBucketPickupPayload.ID,
        (_, context) -> context.server().execute(() -> ServerHelperForEquipment.handleBucketPickup(context.player()))
    );
  }

  public static void registerUniverseConsoleBoundBlockNetworking() {
    RemoteAccessManager.REMOTE_ACCESS_TICKET = RemoteAccessManager.register();
    PayloadTypeRegistry.serverboundPlay().register(
        UniverseConsoleBoundBlockPayload.ID,
        UniverseConsoleBoundBlockPayload.CODEC
    );
    ServerPlayNetworking.registerGlobalReceiver(
        UniverseConsoleBoundBlockPayload.ID,
        (payload, context) -> context.server().execute(
            () -> handleUniverseConsoleBoundBlockAction(payload, context)
        )
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

  private static Vec3 calcDirection(
      ServerPlayer player
  ) {
    double rad = Math.toRadians(player.getYRot());
    return new Vec3(-Math.sin(rad), 0, Math.cos(rad));
  }

  private static void handleUniverseConsoleBoundBlockAction(
      UniverseConsoleBoundBlockPayload payload,
      ServerPlayNetworking.Context context
  ) {
    switch (payload.act()) {
      case OPEN -> execBlockOpen(payload, context);
      case REMOVE -> execBlockRemove(payload, context);
    }
  }

  private static void execBlockOpen(
      UniverseConsoleBoundBlockPayload payload,
      ServerPlayNetworking.Context context
  ) {
    ServerPlayer player = context.player();
    ServerLevel targetWorld = context.server().getLevel(payload.dimension());

    if (targetWorld == null) {
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.universe_console.unknown_world")
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
          Component.translatable("msg.magnatour.universe_console.no_target")
              .withStyle(ChatFormatting.YELLOW)
      );
      return;
    }

    try {
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
              Component.translatable("msg.magnatour.universe_console.unable_open")
                  .withStyle(ChatFormatting.YELLOW)
          );
          return;
        }
      }

      RemoteAccessManager.finishOpenRemote(player);
    } catch (Exception e) {
      RemoteAccessManager.forceEndRemoteAccess(player);
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.universe_console.error")
              .withStyle(ChatFormatting.YELLOW)
      );
    }
  }

  private static void execBlockRemove(
      UniverseConsoleBoundBlockPayload payload,
      ServerPlayNetworking.Context context
  ) {
    ServerPlayer player = context.player();
    ItemStack consoleStack = player.getItemInHand(player.getUsedItemHand());

    if (!(consoleStack.getItem() instanceof UniverseConsole)) return;

    UniverseConsole.BoundBlockList currentList = consoleStack.getOrDefault(
        CustomComponents.UNIVERSE_CONSOLE_BOUND_LIST,
        UniverseConsole.BoundBlockList.EMPTY
    );
    UniverseConsole.BoundBlockList newList = currentList.withRemoved(
        payload.pos(), payload.dimension()
    );

    consoleStack.set(CustomComponents.UNIVERSE_CONSOLE_BOUND_LIST, newList);
    player.sendOverlayMessage(
        Component.translatable("msg.magnatour.universe_console.remove")
            .withStyle(ChatFormatting.GREEN)
    );
  }

}
