/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Java Standard
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.CustomPortalVertex;
import roeyqian.magnatour.level.data.PortalLinkSavedData;
import roeyqian.magnatour.registry.world.MagnaDimensions;

/** Universe portal ignition, destination lookup and persistent Nether portal links. */
public final class UniversePortalMain {

  private UniversePortalMain() {}

  public static void handleBaseFireCanBePlacedAt(
      Level level,
      BlockPos pos,
      Direction forwardDirection,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (cir.getReturnValue()) return;

    cir.setReturnValue(
        CustomPortalVertex.canBePlacedAt(level, pos, forwardDirection)
            || canCreateNetherPortal(level, pos, forwardDirection)
    );
  }

  public static void handleBaseFireOnPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState,
      CallbackInfo ci
  ) {
    if (oldState.is(state.getBlock())) return;
    if (CustomPortalVertex.tryCreatePortalFromFire(level, pos)) {
      ci.cancel();
      return;
    }
    if (tryCreateNetherPortal(level, pos)) ci.cancel();
  }

  public static void handleDestinationLookup(
      ServerLevel sourceLevel,
      Entity entity,
      BlockPos portalPos,
      CallbackInfoReturnable<TeleportTransition> cir
  ) {
    if (!isUniverseMetaOrNether(sourceLevel.dimension())) return;

    PortalLinkSavedData linkData = PortalLinkSavedData.get(sourceLevel.getServer());
    PortalLinkSavedData.Endpoint source = endpointAt(sourceLevel, portalPos);
    Optional<PortalLinkSavedData.Endpoint> linked = linkData.getDestination(source);
    if (linked.isEmpty() || !isUniverseMetaNetherPair(source.dimension(), linked.get().dimension())) return;

    ServerLevel destinationLevel = sourceLevel.getServer().getLevel(linked.get().dimension());
    if (destinationLevel != null) {
      loadPortalChunk(destinationLevel, linked.get().pos());
    }
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
    if (!MagnaDimensions.UNIVERSE_META.equals(sourceLevel.dimension())
        || transition == null
        || !Level.NETHER.equals(transition.newLevel().dimension())) {
      return;
    }

    PortalLinkSavedData.Endpoint source = endpointAt(sourceLevel, sourcePortalPos);
    PortalLinkSavedData.Endpoint destination = endpointAt(
        transition.newLevel(),
        BlockPos.containing(transition.position())
    );
    PortalLinkSavedData.get(sourceLevel.getServer()).link(source, destination);
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
      BlockPos first, BlockPos second
  ) {
    int compareX = Integer.compare(first.getX(), second.getX());
    if (compareX != 0) return compareX;

    int compareY = Integer.compare(first.getY(), second.getY());
    return compareY != 0 ? compareY : Integer.compare(first.getZ(), second.getZ());
  }

  /**
   * Vanilla only considers the Overworld and Nether valid Nether-portal dimensions. Universe
   * Meta is intentionally a portal hub, so let its obsidian frames use the standard shape.
   */
  private static boolean canCreateNetherPortal(
      Level level,
      BlockPos pos,
      Direction forwardDirection
  ) {
    if (level.dimension() != MagnaDimensions.UNIVERSE_META) return false;

    Direction.Axis axis = forwardDirection.getAxis().isHorizontal()
        ? forwardDirection.getCounterClockWise().getAxis()
        : Direction.Plane.HORIZONTAL.getRandomAxis(level.getRandom());
    return PortalShape.findEmptyPortalShape(level, pos, axis).isPresent();
  }

  private static boolean tryCreateNetherPortal(
      Level level,
      BlockPos pos
  ) {
    if (level.dimension() != MagnaDimensions.UNIVERSE_META) return false;

    var portalShape = PortalShape.findEmptyPortalShape(level, pos, Direction.Axis.X);
    if (portalShape.isEmpty()) return false;

    portalShape.get().createPortalBlocks(level);
    return true;
  }

  private static boolean isUniverseMetaOrNether(
      ResourceKey<Level> dimension
  ) {
    return MagnaDimensions.UNIVERSE_META.equals(dimension) || Level.NETHER.equals(dimension);
  }

  private static PortalLinkSavedData.Endpoint endpointAt(
      ServerLevel level,
      BlockPos portalPos
  ) {
    return new PortalLinkSavedData.Endpoint(level.dimension(), findPortalOrigin(level, portalPos));
  }

  private static boolean isUniverseMetaNetherPair(
      ResourceKey<Level> sourceDimension, ResourceKey<Level> destinationDimension
  ) {
    return (MagnaDimensions.UNIVERSE_META.equals(sourceDimension) && Level.NETHER.equals(destinationDimension))
        || (Level.NETHER.equals(sourceDimension) && MagnaDimensions.UNIVERSE_META.equals(destinationDimension));
  }

  /**
   * A target portal's chunk is normally unloaded after a server restart. Load it before checking
   * the saved endpoint so an unloaded chunk is not mistaken for a destroyed portal.
   */
  private static void loadPortalChunk(
      ServerLevel level,
      BlockPos portalPos
  ) {
    level.getChunkSource().getChunk(portalPos.getX() >> 4, portalPos.getZ() >> 4, true);
  }

}
