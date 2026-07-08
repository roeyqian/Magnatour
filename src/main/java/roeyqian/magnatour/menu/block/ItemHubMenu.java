/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.block;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.utility.registry.menu.RegBlockMenus;

public class ItemHubMenu extends AbstractContainerMenu {

  private String filterItemId;

  private final BlockPos blockPos;

  private final Container hopper;

  private final ResourceKey<Level> dimension;

  public ItemHubMenu(
      int containerId,
      Inventory inventory
  ) {
    this(
        containerId,
        inventory,
        new SimpleContainer(5),
        BlockPos.ZERO,
        Level.OVERWORLD,
        ""
    );
  }

  public ItemHubMenu(
      int containerId,
      Inventory inventory,
      OpeningData openingData
  ) {
    this(
        containerId,
        inventory,
        new SimpleContainer(5),
        openingData.blockPos(),
        openingData.dimension(),
        openingData.filterItemId()
    );
  }

  public ItemHubMenu(
      int containerId,
      Inventory inventory,
      Container hopper,
      BlockPos blockPos,
      ResourceKey<Level> dimension,
      String filterItemId
  ) {
    super(RegBlockMenus.ITEM_HUB_HANDLER, containerId);
    this.hopper = hopper;
    this.blockPos = blockPos;
    this.dimension = dimension;
    this.filterItemId = filterItemId;

    checkContainerSize(hopper, 5);
    hopper.startOpen(inventory.player);

    for (int x = 0; x < 5; x++) {
      this.addSlot(new Slot(hopper, x, 44 + x * 18, 20));
    }

    this.addStandardInventorySlots(inventory, 8, 51);
  }

  public BlockPos getBlockPos() {
    return this.blockPos;
  }

  public ResourceKey<Level> getDimension() {
    return this.dimension;
  }

  public String getFilterItemId() {
    return this.filterItemId;
  }

  @Override
  public @NonNull ItemStack quickMoveStack(
      @NonNull Player player,
      int slotIndex
  ) {
    ItemStack clicked = ItemStack.EMPTY;
    Slot slot = this.slots.get(slotIndex);
    if (slot != null && slot.hasItem()) {
      ItemStack stack = slot.getItem();
      clicked = stack.copy();
      if (slotIndex < this.hopper.getContainerSize()) {
        if (!this.moveItemStackTo(stack, this.hopper.getContainerSize(), this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(stack, 0, this.hopper.getContainerSize(), false)) {
        return ItemStack.EMPTY;
      }

      if (stack.isEmpty()) {
        slot.setByPlayer(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return clicked;
  }

  @Override
  public void removed(
      Player player
  ) {
    super.removed(player);
    this.hopper.stopOpen(player);
  }

  public void setFilterItemId(
      String filterItemId
  ) {
    this.filterItemId = filterItemId;
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return this.hopper.stillValid(player);
  }

  public record OpeningData(
      BlockPos blockPos,
      ResourceKey<Level> dimension,
      String filterItemId
  ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> PACKET_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpeningData::blockPos,
            ResourceKey.streamCodec(Registries.DIMENSION),
            OpeningData::dimension,
            ByteBufCodecs.STRING_UTF8,
            OpeningData::filterItemId,
            OpeningData::new
        );

  }

}
