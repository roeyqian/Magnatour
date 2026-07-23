/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.network;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;

public record ItemHubPayload(
    BlockPos blockPos,
    ResourceKey<Level> dimension,
    String filterItemId
) implements CustomPacketPayload {

  public static final Type<ItemHubPayload> ID = new Type<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "item_hub")
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, ItemHubPayload> CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC,
          ItemHubPayload::blockPos,
          ResourceKey.streamCodec(Registries.DIMENSION),
          ItemHubPayload::dimension,
          ByteBufCodecs.STRING_UTF8,
          ItemHubPayload::filterItemId,
          ItemHubPayload::new
      );

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return ID;
  }

}
