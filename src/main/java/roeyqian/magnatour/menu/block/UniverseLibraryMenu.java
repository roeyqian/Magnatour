/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.block;

// Minecraft
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.active.entity.UniverseLibraryEntity;
import roeyqian.magnatour.utility.registry.menu.RegBlockMenus;

public class UniverseLibraryMenu extends AbstractContainerMenu {

  public final DataSlot scrollOffset = DataSlot.standalone();

  private final boolean liveSourceInventory;

  private final Container displayInventory = new DisplayInventory();
  private final Container sourceInventory;

  public UniverseLibraryMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, new SimpleContainer(252));
  }

  public UniverseLibraryMenu(
      int syncId,
      Inventory playerInventory,
      Container inventory
  ) {
    super(RegBlockMenus.UNIVERSE_LIBRARY_HANDLER, syncId);
    this.sourceInventory = inventory;
    this.liveSourceInventory = inventory instanceof UniverseLibraryEntity;
    this.addDataSlot(this.scrollOffset);
    inventory.startOpen(playerInventory.player);

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(
            new DisplaySlot(
                displayInventory,
                col + row * 9,
                8 + col * 18,
                18 + row * 18
            )
        );
      }
    }
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(
            new Slot(
                playerInventory,
                col + row * 9 + 9,
                8 + col * 18,
                140 + row * 18
            )
        );
      }
    }

    for (int col = 0; col < 9; col++) {
      this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
    }

    if (!playerInventory.player.level().isClientSide()) {
      this.scrollOffset.set(0);
      this.refreshDisplay();
    }
  }

  @Override
  public boolean clickMenuButton(
      Player player,
      int id
  ) {
    if (player.level().isClientSide()) return true;

    int maxOffset = Math.max(0, (int) Math.ceil(getInventorySize() / 9.0) - 6);
    if (id >= 0 && id <= maxOffset) {
      if (!this.getCarried().isEmpty()) {
        player.drop(this.getCarried(), false);
        this.setCarried(ItemStack.EMPTY);
      }

      this.scrollOffset.set(id);
      refreshDisplay();
      return true;
    }
    return false;
  }

  public int getInventorySize() {
    return this.sourceInventory.getContainerSize();
  }

  public boolean isFor(
      Container inventory
  ) {
    return this.sourceInventory == inventory;
  }

  @Override @NonNull
  public ItemStack quickMoveStack(
      @NonNull Player player,
      int index
  ) {
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack original = slot.getItem();
      ItemStack copy = original.copy();

      if (index < 54) {
        if (!this.moveItemStackTo(original, 54, 90, true)) return ItemStack.EMPTY;
      } else {
        if (!this.moveItemStackToSourceInventory(original)) return ItemStack.EMPTY;
      }

      if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
      else slot.setChanged();
      return copy;
    }
    return ItemStack.EMPTY;
  }

  public void refreshFromSource() {
    super.broadcastFullState();
  }

  @Override
  public void removed(
      @NonNull Player player
  ) {
    super.removed(player);
    this.sourceInventory.stopOpen(player);
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return this.sourceInventory.stillValid(player);
  }

  private void refreshDisplay() {
    super.broadcastFullState();
  }

  private boolean moveItemStackToSourceInventory(
      ItemStack stack
  ) {
    if (stack.isEmpty()) return false;

    int originalCount = stack.getCount();
    mergeIntoExistingSourceStacks(stack);
    fillEmptySourceSlots(stack);

    return stack.getCount() < originalCount;
  }

  private void mergeIntoExistingSourceStacks(
      ItemStack stack
  ) {
    for (int slotIndex = 0; slotIndex < this.sourceInventory.getContainerSize(); slotIndex++) {
      if (stack.isEmpty()) return;
      if (!this.sourceInventory.canPlaceItem(slotIndex, stack)) continue;

      ItemStack existing = this.sourceInventory.getItem(slotIndex);
      if (existing.isEmpty()) continue;
      if (!ItemStack.isSameItemSameComponents(existing, stack)) continue;

      int maxStackSize = Math.min(
          existing.getMaxStackSize(),
          this.sourceInventory.getMaxStackSize(existing)
      );
      int space = maxStackSize - existing.getCount();
      if (space <= 0) continue;

      int moved = Math.min(space, stack.getCount());
      existing.grow(moved);
      stack.shrink(moved);
      this.sourceInventory.setChanged();
    }
  }

  private void fillEmptySourceSlots(
      ItemStack stack
  ) {
    for (int slotIndex = 0; slotIndex < this.sourceInventory.getContainerSize(); slotIndex++) {
      if (stack.isEmpty()) return;
      if (!this.sourceInventory.canPlaceItem(slotIndex, stack)) continue;
      if (!this.sourceInventory.getItem(slotIndex).isEmpty()) continue;

      int maxStackSize = Math.min(
          stack.getMaxStackSize(),
          this.sourceInventory.getMaxStackSize(stack)
      );
      int moved = Math.min(maxStackSize, stack.getCount());

      ItemStack movedStack = stack.copyWithCount(moved);
      stack.shrink(moved);
      this.sourceInventory.setItem(slotIndex, movedStack);
    }
  }

  private int getRealIndex(
      int displayIndex
  ) {
    return displayIndex + (this.scrollOffset.get() * 9);
  }

private class DisplayInventory implements Container {

    private final NonNullList<ItemStack> clientItems = NonNullList.withSize(54, ItemStack.EMPTY);

    @Override
    public boolean canPlaceItem(
        int slot,
        @NonNull ItemStack stack
    ) {
      if (!UniverseLibraryMenu.this.liveSourceInventory) return true;

      int realIndex = UniverseLibraryMenu.this.getRealIndex(slot);
      return realIndex < UniverseLibraryMenu.this.sourceInventory.getContainerSize()
          && UniverseLibraryMenu.this.sourceInventory.canPlaceItem(realIndex, stack);
    }

    @Override
    public void clearContent() {
      if (UniverseLibraryMenu.this.liveSourceInventory) {
        for (int slot = 0; slot < this.getContainerSize(); slot++) {
          int realIndex = UniverseLibraryMenu.this.getRealIndex(slot);
          if (realIndex >= UniverseLibraryMenu.this.sourceInventory.getContainerSize()) continue;
          UniverseLibraryMenu.this.sourceInventory.setItem(realIndex, ItemStack.EMPTY);
        }
        return;
      }

      for (int slot = 0; slot < this.clientItems.size(); slot++) {
        this.clientItems.set(slot, ItemStack.EMPTY);
      }
    }

    @Override
    public int getContainerSize() {
      return 54;
    }

    @Override @NonNull
    public ItemStack getItem(
        int slot
    ) {
      if (!UniverseLibraryMenu.this.liveSourceInventory) {
        return this.clientItems.get(slot);
      }

      int realIndex = UniverseLibraryMenu.this.getRealIndex(slot);
      return realIndex < UniverseLibraryMenu.this.sourceInventory.getContainerSize()
          ? UniverseLibraryMenu.this.sourceInventory.getItem(realIndex)
          : ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
      for (int slot = 0; slot < this.getContainerSize(); slot++) {
        if (!this.getItem(slot).isEmpty()) return false;
      }
      return true;
    }

    @Override @NonNull
    public ItemStack removeItem(
        int slot,
        int count
    ) {
      if (!UniverseLibraryMenu.this.liveSourceInventory) {
        ItemStack result = ContainerHelper.removeItem(this.clientItems, slot, count);
        if (!result.isEmpty()) this.setChanged();
        return result;
      }

      int realIndex = UniverseLibraryMenu.this.getRealIndex(slot);
      return realIndex < UniverseLibraryMenu.this.sourceInventory.getContainerSize()
          ? UniverseLibraryMenu.this.sourceInventory.removeItem(realIndex, count)
          : ItemStack.EMPTY;
    }

    @Override @NonNull
    public ItemStack removeItemNoUpdate(
        int slot
    ) {
      if (!UniverseLibraryMenu.this.liveSourceInventory) {
        return ContainerHelper.takeItem(this.clientItems, slot);
      }

      int realIndex = UniverseLibraryMenu.this.getRealIndex(slot);
      return realIndex < UniverseLibraryMenu.this.sourceInventory.getContainerSize()
          ? UniverseLibraryMenu.this.sourceInventory.removeItemNoUpdate(realIndex)
          : ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
      if (UniverseLibraryMenu.this.liveSourceInventory) {
        UniverseLibraryMenu.this.sourceInventory.setChanged();
      }
    }

    @Override
    public void setItem(
        int slot,
        @NonNull ItemStack stack
    ) {
      if (!UniverseLibraryMenu.this.liveSourceInventory) {
        this.clientItems.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        return;
      }

      int realIndex = UniverseLibraryMenu.this.getRealIndex(slot);
      if (realIndex < UniverseLibraryMenu.this.sourceInventory.getContainerSize()) {
        UniverseLibraryMenu.this.sourceInventory.setItem(realIndex, stack);
      }
    }

    @Override
    public boolean stillValid(
        @NonNull Player player
    ) {
      return true;
    }

  }

private class DisplaySlot extends Slot {

    public DisplaySlot(
        Container inventory,
        int index,
        int x,
        int y
    ) {
      super(inventory, index, x, y);
    }

    @Override
    public boolean isActive() {
      return this.getRealIndex() < UniverseLibraryMenu.this.getInventorySize();
    }

    @Override
    public boolean mayPickup(
        @NonNull Player player
    ) {
      return isActive() && super.mayPickup(player);
    }

    @Override
    public boolean mayPlace(
        @NonNull ItemStack stack
    ) {
      return isActive() && super.mayPlace(stack);
    }

    private int getRealIndex() {
      return UniverseLibraryMenu.this.getRealIndex(this.getContainerSlot());
    }

  }

}
