/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Java Standard
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

// JSpecify
import org.jspecify.annotations.Nullable;

public final class RemoteAccessManager {

  public static @Nullable TicketType REMOTE_ACCESS_TICKET = null;

  private static final Set<UUID> OPENING_REMOTE = ConcurrentHashMap.newKeySet();

  private static final Map<UUID, AccessInfo> ACCESS_INFO = new ConcurrentHashMap<>();

  public static void beginOpenRemote(
      Player player
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    OPENING_REMOTE.add(player.getUUID());
  }

  public static void endRemoteAccess(
      Player player
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    UUID uuid = player.getUUID();

    if (OPENING_REMOTE.contains(uuid)) return;

    AccessInfo info = ACCESS_INFO.remove(uuid);
    if (info != null && info.world() != null) {
      info.world().setChunkForced(
          info.chunkPos().x(),
          info.chunkPos().z(),
          false
      );
    }
  }

  public static void finishOpenRemote(
      Player player
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    OPENING_REMOTE.remove(player.getUUID());
  }

  public static void forceEndRemoteAccess(
      Player player
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    UUID uuid = player.getUUID();

    OPENING_REMOTE.remove(uuid);
    AccessInfo info = ACCESS_INFO.remove(uuid);
    if (info != null && info.world() != null) {
      info.world().setChunkForced(
          info.chunkPos().x(),
          info.chunkPos().z(),
          false
      );
    }
  }

  public static boolean isRemoteAccessing(
      Player player
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    return ACCESS_INFO.containsKey(player.getUUID());
  }

  public static TicketType register() {
    return Registry.register(
        BuiltInRegistries.TICKET_TYPE,
        "remote_access_ticket",
        new TicketType(20L, 2)
    );
  }

  public static void startRemoteAccess(
      Player player,
      ServerLevel world,
      BlockPos pos
  ) {
    Objects.requireNonNull(player, "player cannot be null");
    Objects.requireNonNull(world, "world cannot be null");
    Objects.requireNonNull(pos, "pos cannot be null");

    UUID uuid = player.getUUID();
    ChunkPos chunkPos = ChunkPos.containing(pos);

    ACCESS_INFO.compute(uuid, (_, oldInfo) -> {
      if (oldInfo != null && oldInfo.world() != null) {
        oldInfo.world().setChunkForced(
            oldInfo.chunkPos().x(),
            oldInfo.chunkPos().z(),
            false
        );
      }

      world.setChunkForced(chunkPos.x(), chunkPos.z(), true);
      world.getChunkSource().getChunk(chunkPos.x(), chunkPos.z(), true);
      return new AccessInfo(world, chunkPos, pos);
    });
  }

  private record AccessInfo(
      ServerLevel world,
      ChunkPos chunkPos,
      BlockPos pos
  ) {}

}
