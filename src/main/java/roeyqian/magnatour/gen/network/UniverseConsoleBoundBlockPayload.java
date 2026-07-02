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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;

public record UniverseConsoleBoundBlockPayload(
    Action act,
    BlockPos pos,
    ResourceKey<Level> dimension
) implements CustomPacketPayload {

  public static final Type<UniverseConsoleBoundBlockPayload> ID = new Type<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "bound_block_action")
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseConsoleBoundBlockPayload> CODEC =
      StreamCodec.composite(
          StreamCodec.ofMember((value, buf) -> buf.writeEnum(value), buf -> buf.readEnum(Action.class)),
          UniverseConsoleBoundBlockPayload::act,
          BlockPos.STREAM_CODEC,
          UniverseConsoleBoundBlockPayload::pos,
          ResourceKey.streamCodec(Registries.DIMENSION),
          UniverseConsoleBoundBlockPayload::dimension,
          UniverseConsoleBoundBlockPayload::new
      );

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

public enum Action {OPEN, REMOVE}

}
