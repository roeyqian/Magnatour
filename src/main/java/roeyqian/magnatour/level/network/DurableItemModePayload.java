/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */

package roeyqian.magnatour.level.network;

// Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;

public record DurableItemModePayload() implements CustomPacketPayload {

  public static final Type<DurableItemModePayload> UNIVERSE_MODE_CHANGE =
      new Type<>(Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_mode_change"));

  public static final StreamCodec<RegistryFriendlyByteBuf, DurableItemModePayload> CODEC =
      StreamCodec.unit(new DurableItemModePayload());

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return UNIVERSE_MODE_CHANGE;
  }

}
