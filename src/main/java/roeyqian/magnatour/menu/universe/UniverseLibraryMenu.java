/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.universe;

// Minecraft
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;
import roeyqian.magnatour.registry.content.UniverseMenus;

public class UniverseLibraryMenu extends AbstractContainerMenu {

  public static final int SEARCH_PANEL_HEIGHT = 32;
  public static final int SEARCH_BUTTON_BASE = 1000;
  public static final int CLEAR_SEARCH_BUTTON = -1;
  public static final int SEARCH_RESULT_SLOT = 90;

  private final DataSlot searchItemLow = DataSlot.standalone();
  private final DataSlot searchItemHigh = DataSlot.standalone();
  private final DataSlot searchCountLow = DataSlot.standalone();
  private final DataSlot searchCountHigh = DataSlot.standalone();

  public final DataSlot scrollOffset = DataSlot.standalone();

  private final boolean liveSourceInventory;
  private boolean processingInteraction;

  private final Container displayInventory = new DisplayInventory();
  private final Container searchResultInventory = new SimpleContainer(1);
  private final Container sourceInventory;

  public UniverseLibraryMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, new SimpleContainer(UniverseLibraryEntity.CONTAINER_SIZE));
  }

  public UniverseLibraryMenu(
      int syncId,
      Inventory playerInventory,
      Container inventory
  ) {
    super(UniverseMenus.UNIVERSE_LIBRARY_HANDLER, syncId);
    this.sourceInventory = inventory;
    this.liveSourceInventory = inventory instanceof UniverseLibraryEntity;
    this.addDataSlot(this.scrollOffset);
    this.addDataSlot(this.searchItemLow);
    this.addDataSlot(this.searchItemHigh);
    this.addDataSlot(this.searchCountLow);
    this.addDataSlot(this.searchCountHigh);
    inventory.startOpen(playerInventory.player);

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(
            new DisplaySlot(
                displayInventory,
                col + row * 9,
                8 + col * 18,
                18 + SEARCH_PANEL_HEIGHT + row * 18
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
                140 + SEARCH_PANEL_HEIGHT + row * 18
            )
        );
      }
    }

    for (int col = 0; col < 9; col++) {
      this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198 + SEARCH_PANEL_HEIGHT));
    }

    this.addSlot(new SearchResultSlot());

    if (!playerInventory.player.level().isClientSide()) {
      this.scrollOffset.set(0);
      this.refreshDisplay();
    }
  }

  @Override
  public void broadcastChanges() {
    if (this.liveSourceInventory && !this.processingInteraction) this.refreshSearchCount();
    super.broadcastChanges();
  }

  @Override
  public boolean clickMenuButton(
      @NonNull Player player,
      int id
  ) {
    if (player.level().isClientSide()) return true;
    if (!this.stillValid(player)) return false;

    if (id == CLEAR_SEARCH_BUTTON) {
      this.searchItemLow.set(0);
      this.searchItemHigh.set(0);
      this.refreshFromSource();
      return true;
    }
    if (id >= SEARCH_BUTTON_BASE) {
      int itemId = id - SEARCH_BUTTON_BASE;
      Item item = Item.byId(itemId);
      if (item == null || item == Items.AIR || Item.getId(item) != itemId) return false;
      int encoded = itemId + 1;
      this.searchItemLow.set(encoded & 0xFFFF);
      this.searchItemHigh.set(encoded >>> 16);
      this.refreshFromSource();
      return true;
    }
    int maxOffset = Math.max(0, (int) Math.ceil(getInventorySize() / 9.0) - 6);
    if (id >= 0 && id <= maxOffset) {
      this.scrollOffset.set(id);
      refreshDisplay();
      return true;
    }
    return false;
  }

  @Override
  public void clicked(
      int slotIndex,
      int button,
      @NonNull ContainerInput input,
      @NonNull Player player
  ) {
    if (slotIndex > SEARCH_RESULT_SLOT) return;
    if (this.liveSourceInventory) this.refreshSearchCount();
    this.processingInteraction = true;
    try {
      this.clickStorageSlot(slotIndex, button, input, player);
    } finally {
      this.processingInteraction = false;
      if (this.liveSourceInventory) this.refreshFromSource();
    }
  }

  public int getInventorySize() {
    return this.sourceInventory.getContainerSize();
  }

  public int getSearchCount() {
    return (this.searchCountLow.get() & 0xFFFF) | ((this.searchCountHigh.get() & 0xFFFF) << 16);
  }

  public ItemStack getSearchIcon() {
    return this.searchResultInventory.getItem(0);
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
    if (index < 0 || index > SEARCH_RESULT_SLOT) return ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (index == SEARCH_RESULT_SLOT) {
      if (!slot.mayPickup(player)) return ItemStack.EMPTY;
      ItemStack original = slot.getItem().copy();
      ItemStack remaining = original.copy();
      if (!this.moveItemStackTo(remaining, 54, 90, true)) return ItemStack.EMPTY;
      ItemStack taken = slot.remove(original.getCount() - remaining.getCount());
      slot.onTake(player, taken);
      return original;
    }
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
    // Keep the selected variant and preview stable throughout a vanilla click operation.
    if (this.processingInteraction) return;
    this.refreshSearchCount();
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

  private void refreshSearchCount() {
    Item searched = this.getSearchedItem();
    ItemStack icon = ItemStack.EMPTY;
    int total = 0;
    if (searched != Items.AIR) {
      for (int slot = 0; slot < this.sourceInventory.getContainerSize(); slot++) {
        ItemStack stack = this.sourceInventory.getItem(slot);
        if (stack.isEmpty() || !stack.is(searched)) continue;
        if (icon.isEmpty()) icon = stack.copyWithCount(1);
        if (ItemStack.isSameItemSameComponents(icon, stack)) total += stack.getCount();
      }
      if (icon.isEmpty()) icon = new ItemStack(searched);
    }
    if (total > 0) icon.setCount(Math.min(total, icon.getMaxStackSize()));
    this.searchResultInventory.setItem(0, icon);
    this.searchCountLow.set(total & 0xFFFF);
    this.searchCountHigh.set(total >>> 16);
  }

  private void clickStorageSlot(
      int slotIndex,
      int button,
      ContainerInput input,
      Player player
  ) {
    if (slotIndex >= 0 && slotIndex < 54) {
      Slot slot = this.slots.get(slotIndex);
      ItemStack stored = slot.getItem();
      if (stored.getCount() > stored.getMaxStackSize()) {
        if (input == ContainerInput.SWAP && ((button >= 0 && button < 9) || button == 40)) {
          ItemStack hotbar = player.getInventory().getItem(button);
          if (hotbar.isEmpty() && slot.mayPickup(player)) {
            ItemStack taken = slot.remove(stored.getMaxStackSize());
            player.getInventory().setItem(button, taken);
            slot.onTake(player, taken);
          } else if (ItemStack.isSameItemSameComponents(stored, hotbar) && slot.mayPlace(hotbar)) {
            player.getInventory().setItem(button, slot.safeInsert(hotbar));
          }
          return;
        }
        // A direct swap would put an oversized stack on the cursor.
        if (input == ContainerInput.PICKUP
            && !this.getCarried().isEmpty()
            && !ItemStack.isSameItemSameComponents(stored, this.getCarried())) {
          return;
        }
      }
    }
    super.clicked(slotIndex, button, input, player);
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

      int maxStackSize = this.sourceInventory.getMaxStackSize(existing);
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

      int maxStackSize = this.sourceInventory.getMaxStackSize(stack);
      int moved = Math.min(maxStackSize, stack.getCount());

      ItemStack movedStack = stack.copyWithCount(moved);
      stack.shrink(moved);
      this.sourceInventory.setItem(slotIndex, movedStack);
    }
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

  private Item getSearchedItem() {
    int encoded = (this.searchItemLow.get() & 0xFFFF) | ((this.searchItemHigh.get() & 0xFFFF) << 16);
    Item item = encoded == 0 ? Items.AIR : Item.byId(encoded - 1);
    return item == null ? Items.AIR : item;
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
    public int getMaxStackSize() {
      return 64 * UniverseLibraryEntity.STACK_SIZE_MULTIPLIER;
    }

    @Override
    public int getMaxStackSize(
        @NonNull ItemStack stack
    ) {
      return UniverseLibraryEntity.getStorageStackLimit(stack);
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
        stack.limitSize(this.getMaxStackSize(stack));
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
    public int getMaxStackSize(
        @NonNull ItemStack stack
    ) {
      return this.container.getMaxStackSize(stack);
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

    @Override @NonNull
    public ItemStack remove(
        int count
    ) {
      return super.remove(Math.min(count, this.getItem().getMaxStackSize()));
    }

    private int getRealIndex() {
      return UniverseLibraryMenu.this.getRealIndex(this.getContainerSlot());
    }

  }

  private class SearchResultSlot extends Slot {

    private SearchResultSlot() {
      super(UniverseLibraryMenu.this.searchResultInventory, 0, 122, 20);
    }

    @Override
    public boolean allowModification(
        @NonNull Player player
    ) {
      return this.mayPickup(player);
    }

    @Override
    public boolean mayPickup(
        @NonNull Player player
    ) {
      return !player.isSpectator() && UniverseLibraryMenu.this.getSearchCount() > 0 && this.hasItem();
    }

    @Override
    public boolean mayPlace(
        @NonNull ItemStack stack
    ) {
      return false;
    }

    @Override @NonNull
    public ItemStack remove(
        int count
    ) {
      ItemStack preview = this.getItem();
      int requested = Math.min(Math.max(count, 0), preview.getCount());
      if (requested == 0 || UniverseLibraryMenu.this.getSearchCount() == 0) return ItemStack.EMPTY;
      int removed = 0;
      if (UniverseLibraryMenu.this.liveSourceInventory) {
        for (int i = 0; i < UniverseLibraryMenu.this.sourceInventory.getContainerSize() && removed < requested; i++) {
          ItemStack stored = UniverseLibraryMenu.this.sourceInventory.getItem(i);
          if (!ItemStack.isSameItemSameComponents(preview, stored)) continue;
          removed += UniverseLibraryMenu.this.sourceInventory.removeItem(i, requested - removed).getCount();
        }
      } else {
        removed = requested;
      }
      // Replace the preview: vanilla hotbar swaps may retain the old stack reference.
      this.container.setItem(0, preview.copyWithCount(preview.getCount() - removed));
      return preview.copyWithCount(removed);
    }

    @Override
    public void setByPlayer(
        @NonNull ItemStack stack,
        @NonNull ItemStack previous
    ) {
      // Vanilla hotbar swaps clear the slot directly instead of calling remove.
      if (stack.isEmpty()) this.remove(this.getItem().getCount());
    }

  }

}
