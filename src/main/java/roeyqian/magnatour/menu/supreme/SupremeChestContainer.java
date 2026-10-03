/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.supreme;

// Java Standard
import java.util.Arrays;
import java.util.List;

// Minecraft
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SupremeChestContainer implements WorldlyContainer {

  private final int size;

  private final int[] slotsForFace;

  private final List<Container> containers;

  public SupremeChestContainer(
      List<? extends Container> containers
  ) {
    this.containers = List.copyOf(containers);
    this.size = this.containers.stream().mapToInt(Container::getContainerSize).sum();
    this.slotsForFace = createFlatSlots(this.size);
  }

  @Override
  public boolean canPlaceItem(
      int slot,
      @NonNull ItemStack stack
  ) {
    SlotLocation location = findSlot(slot);
    return location.container.canPlaceItem(location.slot, stack);
  }

  @Override
  public boolean canPlaceItemThroughFace(
      int slot,
      @NonNull ItemStack stack,
      @Nullable Direction direction
  ) {
    SlotLocation location = findSlot(slot);
    return !(location.container instanceof WorldlyContainer worldly)
        ? location.container.canPlaceItem(location.slot, stack)
        : worldly.canPlaceItemThroughFace(location.slot, stack, direction);
  }

  @Override
  public boolean canTakeItem(
      @NonNull Container into,
      int slot,
      @NonNull ItemStack stack
  ) {
    SlotLocation location = findSlot(slot);
    return location.container.canTakeItem(into, location.slot, stack);
  }

  @Override
  public boolean canTakeItemThroughFace(
      int slot,
      @NonNull ItemStack stack,
      @NonNull Direction direction
  ) {
    SlotLocation location = findSlot(slot);
    return !(location.container instanceof WorldlyContainer worldly)
        || worldly.canTakeItemThroughFace(location.slot, stack, direction);
  }

  @Override
  public void clearContent() {
    for (Container container : this.containers) container.clearContent();
  }

  public boolean contains(
      Container container
  ) {
    return this.containers.contains(container);
  }

  @Override
  public int getContainerSize() {
    return this.size;
  }

  @NonNull @Override
  public ItemStack getItem(
      int slot
  ) {
    SlotLocation location = findSlot(slot);
    return location.container.getItem(location.slot);
  }

  @Override
  public int @NonNull [] getSlotsForFace(
      @NonNull Direction direction
  ) {
    return this.slotsForFace;
  }

  @Override
  public boolean isEmpty() {
    for (Container container : this.containers) {
      if (!container.isEmpty()) return false;
    }
    return true;
  }

  @NonNull @Override
  public ItemStack removeItem(
      int slot,
      int count
  ) {
    SlotLocation location = findSlot(slot);
    return location.container.removeItem(location.slot, count);
  }

  @NonNull @Override
  public ItemStack removeItemNoUpdate(
      int slot
  ) {
    SlotLocation location = findSlot(slot);
    return location.container.removeItemNoUpdate(location.slot);
  }

  @Override
  public void setChanged() {
    for (Container container : this.containers) container.setChanged();
  }

  @Override
  public void setItem(
      int slot,
      @NonNull ItemStack stack
  ) {
    SlotLocation location = findSlot(slot);
    location.container.setItem(location.slot, stack);
  }

  @Override
  public void startOpen(
      @NonNull ContainerUser containerUser
  ) {
    for (Container container : this.containers) container.startOpen(containerUser);
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    for (Container container : this.containers) {
      if (!container.stillValid(player)) return false;
    }
    return true;
  }

  @Override
  public void stopOpen(
      @NonNull ContainerUser containerUser
  ) {
    for (Container container : this.containers) container.stopOpen(containerUser);
  }

  private static int[] createFlatSlots(
      int size
  ) {
    int[] slots = new int[size];
    Arrays.setAll(slots, index -> index);
    return slots;
  }

  private SlotLocation findSlot(
      int slot
  ) {
    int remaining = slot;
    for (Container container : this.containers) {
      int size = container.getContainerSize();
      if (remaining < size) return new SlotLocation(container, remaining);
      remaining -= size;
    }

    throw new IndexOutOfBoundsException("Slot " + slot + " outside Supreme Chest container");
  }

  private record SlotLocation(
      Container container,
      int slot
  ) {}

}
