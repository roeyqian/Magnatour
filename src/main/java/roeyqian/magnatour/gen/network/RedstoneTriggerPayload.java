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
import roeyqian.magnatour.block.active.entity.RedstoneTriggerEntity;

public record RedstoneTriggerPayload(
    BlockPos blockPos,
    ResourceKey<Level> dimension,
    RedstoneTriggerEntity.TriggerMode mode,
    boolean enabled,
    int intervalTicks
) implements CustomPacketPayload {

  public static final Type<RedstoneTriggerPayload> ID = new Type<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "redstone_trigger")
  );

  private static final StreamCodec<RegistryFriendlyByteBuf, Boolean> BOOL_CODEC =
      StreamCodec.ofMember(
          (value, buf) -> buf.writeBoolean(value),
          RegistryFriendlyByteBuf::readBoolean
      );

  public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneTriggerPayload> CODEC =
      StreamCodec.composite(
          BlockPos.STREAM_CODEC,
          RedstoneTriggerPayload::blockPos,
          ResourceKey.streamCodec(Registries.DIMENSION),
          RedstoneTriggerPayload::dimension,
          RedstoneTriggerEntity.TriggerMode.PACKET_CODEC,
          RedstoneTriggerPayload::mode,
          BOOL_CODEC,
          RedstoneTriggerPayload::enabled,
          ByteBufCodecs.VAR_INT,
          RedstoneTriggerPayload::intervalTicks,
          RedstoneTriggerPayload::new
      );

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

}
