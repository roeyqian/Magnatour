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

public record UniverseBucketPickupPayload() implements CustomPacketPayload {

  public static final Type<UniverseBucketPickupPayload> ID =
      new Type<>(Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_bucket_pickup"));

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseBucketPickupPayload> CODEC =
      StreamCodec.unit(new UniverseBucketPickupPayload());

  @Override @NonNull
  public Type<? extends CustomPacketPayload> type() {
    return ID;
  }

}
