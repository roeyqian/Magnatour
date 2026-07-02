/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */

package roeyqian.magnatour.gen.network;

// Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;

public record UniverseBootsDashPayload(
    int direction
) implements CustomPacketPayload {

  public static final Type<UniverseBootsDashPayload> ID =
      new Type<>(Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "dash"));

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseBootsDashPayload> CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          UniverseBootsDashPayload::direction,
          UniverseBootsDashPayload::new
      );

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

}
