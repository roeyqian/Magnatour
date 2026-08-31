/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.block;

// Java Standard
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.level.PortalLinkSavedData;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

/** Links Nether portals made in Universe Meta with their generated Nether-side portal. */
public final class BlockHelperForNetherPortal {

  private BlockHelperForNetherPortal() {}

  public static void handleDestinationLookup(
      ServerLevel sourceLevel,
      Entity entity,
      BlockPos portalPos,
      CallbackInfoReturnable<TeleportTransition> cir
  ) {
    if (sourceLevel.dimension() != Level.NETHER) return;

    PortalLinkSavedData linkData = PortalLinkSavedData.get(sourceLevel.getServer());
    PortalLinkSavedData.Endpoint source = endpointAt(sourceLevel, portalPos);
    Optional<PortalLinkSavedData.Endpoint> linked = linkData.getDestination(source);
    if (linked.isEmpty() || linked.get().dimension() != CustomDimensions.UNIVERSE_META) return;

    ServerLevel destinationLevel = sourceLevel.getServer().getLevel(linked.get().dimension());
    if (destinationLevel == null || !destinationLevel.getBlockState(linked.get().pos()).is(Blocks.NETHER_PORTAL)) {
      linkData.unlink(source);
      return;
    }

    cir.setReturnValue(new TeleportTransition(
        destinationLevel,
        Vec3.atBottomCenterOf(linked.get().pos()),
        Vec3.ZERO,
        entity.getYRot(),
        entity.getXRot(),
        Set.of(),
        TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)
    ));
  }

  public static void recordDestination(
      ServerLevel sourceLevel,
      BlockPos sourcePortalPos,
      TeleportTransition transition
  ) {
    if (sourceLevel.dimension() != CustomDimensions.UNIVERSE_META
        || transition == null
        || transition.newLevel().dimension() != Level.NETHER) {
      return;
    }

    PortalLinkSavedData.Endpoint source = endpointAt(sourceLevel, sourcePortalPos);
    PortalLinkSavedData.Endpoint destination = endpointAt(
        transition.newLevel(),
        BlockPos.containing(transition.position())
    );
    PortalLinkSavedData.get(sourceLevel.getServer()).link(source, destination);
  }

  private static PortalLinkSavedData.Endpoint endpointAt(
      ServerLevel level,
      BlockPos portalPos
  ) {
    return new PortalLinkSavedData.Endpoint(level.dimension(), findPortalOrigin(level, portalPos));
  }

  private static BlockPos findPortalOrigin(
      ServerLevel level,
      BlockPos portalPos
  ) {
    BlockPos start = findNearbyPortalBlock(level, portalPos);
    if (start == null) return portalPos;

    ArrayDeque<BlockPos> pending = new ArrayDeque<>();
    Set<BlockPos> visited = new HashSet<>();
    pending.add(start);
    visited.add(start);
    BlockPos origin = start;

    while (!pending.isEmpty()) {
      BlockPos current = pending.removeFirst();
      if (comparePosition(current, origin) < 0) origin = current;

      for (Direction direction : Direction.values()) {
        BlockPos neighbor = current.relative(direction);
        if (visited.size() >= 512
            || visited.contains(neighbor)
            || !level.getBlockState(neighbor).is(Blocks.NETHER_PORTAL)) {
          continue;
        }
        visited.add(neighbor);
        pending.addLast(neighbor);
      }
    }

    return origin;
  }

  private static BlockPos findNearbyPortalBlock(
      ServerLevel level,
      BlockPos pos
  ) {
    if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) return pos;

    for (Direction direction : Direction.values()) {
      BlockPos neighbor = pos.relative(direction);
      if (level.getBlockState(neighbor).is(Blocks.NETHER_PORTAL)) return neighbor;
    }
    return null;
  }

  private static int comparePosition(
      BlockPos first,
      BlockPos second
  ) {
    int compareX = Integer.compare(first.getX(), second.getX());
    if (compareX != 0) return compareX;

    int compareY = Integer.compare(first.getY(), second.getY());
    return compareY != 0 ? compareY : Integer.compare(first.getZ(), second.getZ());
  }

}
