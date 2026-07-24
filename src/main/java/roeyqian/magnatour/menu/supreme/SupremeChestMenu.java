/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.supreme;

// Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
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
import roeyqian.magnatour.blockentity.supreme.SupremeChestEntity;
import roeyqian.magnatour.registry.content.SupremeMenus;

public class SupremeChestMenu extends AbstractContainerMenu {

  public static final int COLUMNS = 12;
  public static final int DOUBLE_ROWS = 6;
  public static final int PLAYER_INVENTORY_X = 34;
  public static final int SINGLE_ROWS = 3;
  public static final int TRIPLE_ROWS = 9;
  public static final int MAX_ROWS = TRIPLE_ROWS;
  public static final int MAX_SLOT_COUNT = COLUMNS * MAX_ROWS;

  public final DataSlot inventorySize = DataSlot.standalone();

  private final int rows;

  private final Container sourceInventory;

  public SupremeChestMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(
        syncId,
        playerInventory,
        new SimpleContainer(SupremeChestEntity.SLOT_COUNT),
        SupremeChestEntity.SLOT_COUNT
    );
  }

  public SupremeChestMenu(
      int syncId,
      Inventory playerInventory,
      int inventorySize
  ) {
    this(
        syncId,
        playerInventory,
        new SimpleContainer(MAX_SLOT_COUNT),
        inventorySize
    );
  }

  public SupremeChestMenu(
      int syncId,
      Inventory playerInventory,
      Container inventory,
      int inventorySize
  ) {
    super(SupremeMenus.SUPREME_CHEST_HANDLER, syncId);
    this.sourceInventory = inventory;
    this.rows = rowsFromInventorySize(inventorySize);
    this.addDataSlot(this.inventorySize);
    inventory.startOpen(playerInventory.player);

    for (int row = 0; row < MAX_ROWS; row++) {
      for (int col = 0; col < COLUMNS; col++) {
        this.addSlot(
            new SupremeChestSlot(
                inventory,
                col + row * COLUMNS,
                8 + col * 18,
                18 + row * 18
            )
        );
      }
    }

    int playerInventoryY = 18 + this.rows * 18 + 13;
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(
            new Slot(
                playerInventory,
                col + row * 9 + 9,
                PLAYER_INVENTORY_X + col * 18,
                playerInventoryY + row * 18
            )
        );
      }
    }

    for (int col = 0; col < 9; col++) {
      this.addSlot(
          new Slot(
              playerInventory,
              col,
              PLAYER_INVENTORY_X + col * 18,
              playerInventoryY + 58
          )
      );
    }

    if (!playerInventory.player.level().isClientSide()) {
      this.inventorySize.set(inventorySize);
    }
  }

  public static int rowsFromInventorySize(
      int inventorySize
  ) {
    if (inventorySize <= SupremeChestEntity.SLOT_COUNT) return SINGLE_ROWS;
    if (inventorySize <= SupremeChestEntity.SLOT_COUNT * 2) return DOUBLE_ROWS;
    return TRIPLE_ROWS;
  }

  public Container getContainer() {
    return this.sourceInventory;
  }

  public int getInventorySize() {
    return this.inventorySize.get() > 0
        ? this.inventorySize.get()
        : this.sourceInventory.getContainerSize();
  }

  public int getRowCount() {
    return this.rows;
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

      if (index < MAX_SLOT_COUNT) {
        if (!this.moveItemStackTo(original, MAX_SLOT_COUNT, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else {
        if (!this.moveItemStackTo(original, 0, this.getInventorySize(), false)) {
          return ItemStack.EMPTY;
        }
      }

      if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
      else slot.setChanged();
      return copy;
    }
    return ItemStack.EMPTY;
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

  public record OpeningData(
      int inventorySize
  ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> PACKET_CODEC =
        new StreamCodec<>() {

          @Override
          public OpeningData decode(
              RegistryFriendlyByteBuf input
          ) {
            return new OpeningData(ByteBufCodecs.VAR_INT.decode(input));
          }

          @Override
          public void encode(
              RegistryFriendlyByteBuf output,
              OpeningData value
          ) {
            ByteBufCodecs.VAR_INT.encode(output, value.inventorySize());
          }

        };

  }

  private class SupremeChestSlot extends Slot {

    public SupremeChestSlot(
        Container inventory,
        int index,
        int x,
        int y
    ) {
      super(inventory, index, x, y);
    }

    @Override @NonNull
    public ItemStack getItem() {
      return isActive() ? super.getItem() : ItemStack.EMPTY;
    }

    @Override
    public boolean isActive() {
      return this.getContainerSlot() < SupremeChestMenu.this.getInventorySize();
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
        int amount
    ) {
      return isActive() ? super.remove(amount) : ItemStack.EMPTY;
    }

    @Override
    public void setByPlayer(
        @NonNull ItemStack stack
    ) {
      if (isActive()) super.setByPlayer(stack);
    }

  }

}
