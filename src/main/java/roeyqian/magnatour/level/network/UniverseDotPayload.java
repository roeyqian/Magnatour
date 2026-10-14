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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.blockentity.universe.UniverseDotEntity;

public record UniverseDotPayload(
    Action action,
    BlockPos blockPos,
    int index,
    UniverseDotEntity.Destination destination
) implements CustomPacketPayload {

  public static final Type<UniverseDotPayload> ID = new Type<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_dot")
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseDotPayload> CODEC =
      StreamCodec.composite(
          ByteBufCodecs.idMapper(id -> Action.values()[id], Action::ordinal),
          UniverseDotPayload::action,
          BlockPos.STREAM_CODEC,
          UniverseDotPayload::blockPos,
          ByteBufCodecs.VAR_INT,
          UniverseDotPayload::index,
          UniverseDotEntity.Destination.PACKET_CODEC,
          UniverseDotPayload::destination,
          UniverseDotPayload::new
      );

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

  public enum Action {ADD, DELETE, TELEPORT}

}
