/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.blockentity.supreme;

// Java Standard
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

// JSpecify
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.block.supreme.LogisticsFiber;
import roeyqian.magnatour.block.supreme.SupremeChest;
import roeyqian.magnatour.registry.content.RegBlockEntities;

public class LogisticsFiberEntity extends BlockEntity {

  private static final int MAX_SUCCESSFUL_MOVES_PER_TICK = 64;

  private static final Comparator<BlockPos> POSITION_ORDER = Comparator
      .comparingInt((BlockPos pos) -> pos.getX())
      .thenComparingInt(pos -> pos.getY())
      .thenComparingInt(pos -> pos.getZ());

  public LogisticsFiberEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.LOGISTICS_FIBER_ENTITY, pos, state);
  }

  public static @Nullable Container getContainerAt(
      Level level,
      BlockPos pos
  ) {
    if (level.getBlockState(pos).getBlock() instanceof SupremeChest) {
      return SupremeChest.getCombinedContainer(level, pos);
    }
    return HopperBlockEntity.getContainerAt(level, pos);
  }

  public static void tick(
      Level level,
      BlockPos pos,
      BlockState state,
      LogisticsFiberEntity blockEntity
  ) {
    if (level.isClientSide()) return;
    if (!(state.getBlock() instanceof LogisticsFiber)) return;

    FiberNetwork network = collectNetwork(level, pos);
    if (!pos.equals(network.controller())) return;
    if (network.sources().isEmpty()) return;

    transferItems(network);
  }

  /**
   * Collects every physically connected fiber into a single routing graph.
   * Each fiber still treats its facing side as the only output side and all
   * other container-adjacent sides as inputs, but items may now traverse any
   * connected fiber path, including turns and branches.
   * <p>
   * Containers are deduplicated by their logical anchor (handles double
   * chests and SupremeChest groupings). The controller is the first fiber
   * in sorted position order; only the controller performs transfers.
   */
  private static FiberNetwork collectNetwork(
      Level level,
      BlockPos origin
  ) {
    Set<BlockPos> visited = new HashSet<>();
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    List<BlockPos> fibers = new ArrayList<>();

    visited.add(origin);
    queue.addLast(origin);

    // BFS over the physically connected component; directional routing is
    // resolved afterward from each fiber's facing state.
    while (!queue.isEmpty()) {
      BlockPos current = queue.removeFirst();
      fibers.add(current);

      for (Direction direction : Direction.values()) {
        BlockPos next = current.relative(direction);
        if (visited.contains(next)) continue;
        if (!(level.getBlockState(next).getBlock() instanceof LogisticsFiber)) continue;

        visited.add(next);
        queue.addLast(next);
      }
    }

    fibers.sort(POSITION_ORDER);

    Map<BlockPos, FiberNode> fiberNodes = new LinkedHashMap<>();
    Map<BlockPos, List<BlockPos>> connections = new LinkedHashMap<>();
    Map<BlockPos, SourceEndpoint> sources = new LinkedHashMap<>();
    Map<BlockPos, DestinationEndpoint> destinations = new LinkedHashMap<>();

    for (BlockPos fiberPos : fibers) {
      BlockState fiberState = level.getBlockState(fiberPos);
      fiberNodes.put(fiberPos, new FiberNode(fiberPos, fiberState.getValue(LogisticsFiber.FACING)));
    }

    for (FiberNode fiber : fiberNodes.values()) {
      List<BlockPos> neighbors = new ArrayList<>();

      for (Direction direction : Direction.values()) {
        BlockPos neighborPos = fiber.pos().relative(direction);
        if (fiberNodes.containsKey(neighborPos)) {
          neighbors.add(neighborPos);
          continue;
        }

        if (direction == fiber.facing()) {
          addDestination(level, fiber, direction, destinations);
          continue;
        }

        addSource(level, fiber, direction, sources);
      }

      neighbors.sort(POSITION_ORDER);
      connections.put(fiber.pos(), List.copyOf(neighbors));
    }

    return new FiberNetwork(
        fibers.getFirst(),
        fiberNodes,
        connections,
        List.copyOf(sources.values()),
        destinations
    );
  }

  /**
   * Pulls from containers attached to non-facing sides of the network and
   * pushes items into containers attached to any reachable fiber output side.
   */
  private static void transferItems(
      FiberNetwork network
  ) {
    int remainingMoves = MAX_SUCCESSFUL_MOVES_PER_TICK;

    for (SourceEndpoint source : network.sources()) {
      if (remainingMoves <= 0) return;
      if (source.container().isEmpty()) continue;

      for (DestinationEndpoint destination : collectReachableDestinations(network, source.entryFiber())) {
        if (remainingMoves <= 0) return;
        if (source.anchor().equals(destination.anchor())) continue;

        remainingMoves = moveItems(
            source.container(),
            destination.container(),
            source.accessDirection(),
            destination.accessDirection(),
            remainingMoves
        );

        if (source.container().isEmpty()) break;
      }
    }
  }

  private static void addDestination(
      Level level,
      FiberNode fiber,
      Direction direction,
      Map<BlockPos, DestinationEndpoint> destinations
  ) {
    BlockPos pos = fiber.pos().relative(direction);
    Container container = getContainerAt(level, pos);
    if (container == null) return;

    BlockPos anchor = resolveContainerAnchor(level, pos);
    destinations.put(fiber.pos(), new DestinationEndpoint(anchor, container, direction.getOpposite()));
  }

  private static void addSource(
      Level level,
      FiberNode fiber,
      Direction direction,
      Map<BlockPos, SourceEndpoint> sources
  ) {
    BlockPos pos = fiber.pos().relative(direction);
    Container container = getContainerAt(level, pos);
    if (container == null) return;

    BlockPos anchor = resolveContainerAnchor(level, pos);
    sources.putIfAbsent(
        anchor,
        new SourceEndpoint(anchor, container, direction.getOpposite(), fiber.pos())
    );
  }

  private static List<DestinationEndpoint> collectReachableDestinations(
      FiberNetwork network,
      BlockPos startFiber
  ) {
    List<DestinationEndpoint> destinations = new ArrayList<>();
    Set<BlockPos> visitedFibers = new HashSet<>();
    Set<BlockPos> seenAnchors = new HashSet<>();
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();

    if (!network.fibers().containsKey(startFiber)) {
      return destinations;
    }

    visitedFibers.add(startFiber);
    queue.addLast(startFiber);

    while (!queue.isEmpty()) {
      BlockPos current = queue.removeFirst();
      DestinationEndpoint destination = network.destinations().get(current);
      if (destination != null && seenAnchors.add(destination.anchor())) {
        destinations.add(destination);
      }

      for (BlockPos next : network.connections().getOrDefault(current, List.of())) {
        if (visitedFibers.add(next)) {
          queue.addLast(next);
        }
      }
    }

    return destinations;
  }

  private static int moveItems(
      Container source,
      Container destination,
      Direction sourceAccess,
      Direction destinationAccess,
      int remainingMoves
  ) {
    if (remainingMoves <= 0 || isFullContainer(destination, destinationAccess)) {
      return remainingMoves;
    }

    for (int slot : getSlots(source, sourceAccess)) {
      if (remainingMoves <= 0 || isFullContainer(destination, destinationAccess)) {
        break;
      }

      remainingMoves = moveSourceSlot(
          source,
          destination,
          slot,
          sourceAccess,
          destinationAccess,
          remainingMoves
      );
    }

    return remainingMoves;
  }

  private static BlockPos resolveContainerAnchor(
      Level level,
      BlockPos pos
  ) {
    BlockState state = level.getBlockState(pos);

    if (state.getBlock() instanceof SupremeChest) {
      List<SupremeChestEntity> chests = SupremeChest.getConnectedChestsForRender(level, pos);
      if (!chests.isEmpty()) {
        BlockPos anchor = chests.getFirst().getBlockPos();
        for (SupremeChestEntity chest : chests) {
          anchor = minPos(anchor, chest.getBlockPos());
        }
        return anchor;
      }
    }

    if (state.getBlock() instanceof ChestBlock
        && state.hasProperty(BlockStateProperties.CHEST_TYPE)
        && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
      if (type != ChestType.SINGLE) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction otherDirection = type == ChestType.LEFT
            ? facing.getClockWise()
            : facing.getCounterClockWise();
        return minPos(pos, pos.relative(otherDirection));
      }
    }

    return pos;
  }

  private static boolean isFullContainer(
      Container container,
      Direction direction
  ) {
    for (int slot : getSlots(container, direction)) {
      ItemStack stack = container.getItem(slot);
      if (stack.isEmpty() || stack.getCount() < container.getMaxStackSize(stack)) {
        return false;
      }
    }
    return true;
  }

  private static int[] getSlots(
      Container container,
      Direction direction
  ) {
    if (container instanceof WorldlyContainer worldlyContainer) {
      return worldlyContainer.getSlotsForFace(direction);
    }

    int[] slots = new int[container.getContainerSize()];
    for (int index = 0; index < slots.length; index++) {
      slots[index] = index;
    }
    return slots;
  }

  private static int moveSourceSlot(
      Container source,
      Container destination,
      int slot,
      Direction sourceAccess,
      Direction destinationAccess,
      int remainingMoves
  ) {
    ItemStack stack = source.getItem(slot);
    if (stack.isEmpty()) return remainingMoves;
    if (!canTakeItemFromContainer(destination, source, stack, slot, sourceAccess)) {
      return remainingMoves;
    }
    if (!canInsertIntoContainer(destination, stack, destinationAccess)) {
      return remainingMoves;
    }

    ItemStack original = stack.copy();
    ItemStack extracted = source.removeItem(slot, stack.getCount());
    if (extracted.isEmpty()) return remainingMoves;

    int extractedCount = extracted.getCount();
    ItemStack remainder = HopperBlockEntity.addItem(source, destination, extracted, destinationAccess);
    int movedCount = extractedCount - remainder.getCount();

    if (movedCount <= 0) {
      restoreSourceSlot(source, slot, original);
      return remainingMoves;
    }

    if (!remainder.isEmpty()) {
      restoreSourceSlot(source, slot, remainder);
    }

    source.setChanged();
    destination.setChanged();
    return remainingMoves - 1;
  }

  private static BlockPos minPos(
      BlockPos first,
      BlockPos second
  ) {
    return POSITION_ORDER.compare(first, second) <= 0 ? first : second;
  }

  private static boolean canTakeItemFromContainer(
      Container into,
      Container from,
      ItemStack itemStack,
      int slot,
      Direction direction
  ) {
    if (!from.canTakeItem(into, slot, itemStack)) {
      return false;
    }

    return !(from instanceof WorldlyContainer worldlyContainer)
        || worldlyContainer.canTakeItemThroughFace(slot, itemStack, direction);
  }

  private static boolean canInsertIntoContainer(
      Container destination,
      ItemStack itemStack,
      Direction destinationAccess
  ) {
    for (int slot : getSlots(destination, destinationAccess)) {
      if (!canPlaceItemInContainer(destination, itemStack, slot, destinationAccess)) {
        continue;
      }

      ItemStack destinationStack = destination.getItem(slot);
      if (destinationStack.isEmpty()) {
        return true;
      }

      int maxCount = Math.min(
          destinationStack.getMaxStackSize(),
          destination.getMaxStackSize(destinationStack)
      );
      if (ItemStack.isSameItemSameComponents(destinationStack, itemStack)
          && destinationStack.getCount() < maxCount
      ) {
        return true;
      }
    }

    return false;
  }

  private static void restoreSourceSlot(
      Container source,
      int slot,
      ItemStack stack
  ) {
    ItemStack current = source.getItem(slot);
    if (current.isEmpty()) {
      source.setItem(slot, stack);
      return;
    }

    if (ItemStack.isSameItemSameComponents(current, stack)) {
      current.grow(stack.getCount());
      source.setChanged();
    }
  }

  private static boolean canPlaceItemInContainer(
      Container container,
      ItemStack itemStack,
      int slot,
      Direction direction
  ) {
    if (!container.canPlaceItem(slot, itemStack)) {
      return false;
    }

    return !(container instanceof WorldlyContainer worldlyContainer)
        || worldlyContainer.canPlaceItemThroughFace(slot, itemStack, direction);
  }

  private record DestinationEndpoint(
      BlockPos anchor,
      Container container,
      Direction accessDirection
  ) {}

  private record FiberNetwork(
      BlockPos controller,
      Map<BlockPos, FiberNode> fibers,
      Map<BlockPos, List<BlockPos>> connections,
      List<SourceEndpoint> sources,
      Map<BlockPos, DestinationEndpoint> destinations
  ) {}

  private record FiberNode(
      BlockPos pos,
      Direction facing
  ) {}

  private record SourceEndpoint(
      BlockPos anchor,
      Container container,
      Direction accessDirection,
      BlockPos entryFiber
  ) {}

}
