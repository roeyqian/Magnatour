/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.block;

// Minecraft
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.utility.registry.menu.RegBlockMenus;

public class UniverseVoidPoolMenu extends AbstractContainerMenu {

  private final Container inventory;

  public UniverseVoidPoolMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, new SimpleContainer(7));
  }

  public UniverseVoidPoolMenu(
      int syncId,
      Inventory playerInventory,
      Container inventory
  ) {
    super(RegBlockMenus.UNIVERSE_VOID_POOL_HANDLER, syncId);
    checkContainerSize(inventory, 7);
    this.inventory = inventory;
    inventory.startOpen(playerInventory.player);
    this.addSlot(new Slot(inventory, 0, 44, 35));

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 2; ++j) {
        this.addSlot(
            new OutputSlot(
                inventory,
                1 + j + i * 2,
                116 + j * 18,
                17 + i * 18
            )
        );
      }
    }
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlot(
            new Slot(
                playerInventory,
                j + i * 9 + 9,
                8 + j * 18,
                84 + i * 18
            )
        );
      }
    }
    for (int i = 0; i < 9; ++i) {
      this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
    }
  }

  @Override @NonNull
  public ItemStack quickMoveStack(
      @NonNull Player player,
      int invSlot
  ) {
    ItemStack newStack = ItemStack.EMPTY;
    Slot slot = this.slots.get(invSlot);
    if (slot.hasItem()) {
      ItemStack originalStack = slot.getItem();
      newStack = originalStack.copy();

      if (invSlot < 7) {
        if (!this.moveItemStackTo(originalStack, 7, 43, true)) return ItemStack.EMPTY;
      } else {
        if (!this.moveItemStackTo(originalStack, 0, 1, false)) return ItemStack.EMPTY;
      }

      if (originalStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
      else slot.setChanged();

      if (originalStack.getCount() == newStack.getCount()) return ItemStack.EMPTY;
      slot.onTake(player, originalStack);
    }
    return newStack;
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return this.inventory.stillValid(player);
  }

  private static class OutputSlot extends Slot {

    public OutputSlot(
        Container inventory,
        int index,
        int x,
        int y
    ) {
      super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(
        @NonNull ItemStack stack
    ) {
      return false;
    }

  }

}
