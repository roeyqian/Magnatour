/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
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

public record UniverseCommandStarBoundBlockPayload(
    Action act,
    BlockPos pos,
    ResourceKey<Level> dimension
) implements CustomPacketPayload {

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseCommandStarBoundBlockPayload> CODEC =
      StreamCodec.composite(
          ByteBufCodecs.idMapper(id -> Action.values()[id], Action::ordinal),
          UniverseCommandStarBoundBlockPayload::act,
          BlockPos.STREAM_CODEC,
          UniverseCommandStarBoundBlockPayload::pos,
          ResourceKey.streamCodec(Registries.DIMENSION),
          UniverseCommandStarBoundBlockPayload::dimension,
          UniverseCommandStarBoundBlockPayload::new
      );

  public static final Type<UniverseCommandStarBoundBlockPayload> ID = new Type<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "bound_block_action")
  );

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

  public enum Action {OPEN, REMOVE}

}
