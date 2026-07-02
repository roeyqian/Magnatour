/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */

package roeyqian.magnatour.gen.network;

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
import roeyqian.magnatour.block.active.entity.UniverseTeleportPointEntity;

public record UniverseTeleportPointPayload(
    Action action,
    BlockPos blockPos,
    int index,
    UniverseTeleportPointEntity.Destination destination
) implements CustomPacketPayload {

  public static final Type<UniverseTeleportPointPayload> ID = new Type<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_teleport_point")
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseTeleportPointPayload> CODEC =
      StreamCodec.composite(
          StreamCodec.ofMember((value, buf) -> buf.writeEnum(value), buf -> buf.readEnum(Action.class)),
          UniverseTeleportPointPayload::action,
          BlockPos.STREAM_CODEC,
          UniverseTeleportPointPayload::blockPos,
          ByteBufCodecs.VAR_INT,
          UniverseTeleportPointPayload::index,
          UniverseTeleportPointEntity.Destination.PACKET_CODEC,
          UniverseTeleportPointPayload::destination,
          UniverseTeleportPointPayload::new
      );

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

public enum Action {ADD, DELETE, TELEPORT}

}
