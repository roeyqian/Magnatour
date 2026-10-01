/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.supreme;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.registry.content.SupremeMenus;

public class ItemHubMenu extends AbstractContainerMenu {

  private int syncedAnchorCount;

  private final int[] syncedAnchoredItems = new int[ItemHubEntity.MAX_ANCHORED_ITEMS];

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
        List.of()
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
        openingData.anchoredItemIds()
    );
  }

  public ItemHubMenu(
      int containerId,
      Inventory inventory,
      Container hopper,
      BlockPos blockPos,
      ResourceKey<Level> dimension,
      List<String> anchoredItemIds
  ) {
    super(SupremeMenus.ITEM_HUB_HANDLER, containerId);
    this.hopper = hopper;
    this.blockPos = blockPos;
    this.dimension = dimension;
    this.syncedAnchorCount = anchoredItemIds.size();
    for (int index = 0; index < anchoredItemIds.size(); index++) {
      this.syncedAnchoredItems[index] = encodeItem(anchoredItemIds.get(index));
    }

    // Each item uses two signed-short properties to preserve the full registry ID.
    // The server reads the live entity, so every viewer receives list changes.
    this.addDataSlots(new ContainerData() {
      @Override
      public int get(int index) {
        if (!(ItemHubMenu.this.hopper instanceof ItemHubEntity entity)) {
          if (index == 0) return ItemHubMenu.this.syncedAnchorCount;
          int itemIndex = (index - 1) / 2;
          return (ItemHubMenu.this.syncedAnchoredItems[itemIndex]
              >>> (((index - 1) % 2) * 16)) & 0xFFFF;
        }
        List<String> anchors = entity.getAnchoredItemIds();
        if (index == 0) return anchors.size();
        int itemIndex = (index - 1) / 2;
        int value = itemIndex < anchors.size() ? encodeItem(anchors.get(itemIndex)) : 0;
        return (value >>> (((index - 1) % 2) * 16)) & 0xFFFF;
      }

      @Override
      public void set(int index, int value) {
        if (index == 0) {
          ItemHubMenu.this.syncedAnchorCount = Math.clamp(value, 0, ItemHubEntity.MAX_ANCHORED_ITEMS);
          return;
        }
        int itemIndex = (index - 1) / 2;
        int shift = ((index - 1) % 2) * 16;
        ItemHubMenu.this.syncedAnchoredItems[itemIndex] =
            (ItemHubMenu.this.syncedAnchoredItems[itemIndex] & ~(0xFFFF << shift))
                | ((value & 0xFFFF) << shift);
      }

      @Override
      public int getCount() {
        return 1 + ItemHubEntity.MAX_ANCHORED_ITEMS * 2;
      }
    });

    checkContainerSize(hopper, 5);
    hopper.startOpen(inventory.player);

    for (int x = 0; x < 5; x++) {
      this.addSlot(new Slot(hopper, x, 44 + x * 18, 20) {
        @Override
        public boolean mayPlace(ItemStack stack) {
          List<String> anchors = ItemHubMenu.this.getAnchoredItemIds();
          return super.mayPlace(stack) && (anchors.isEmpty()
              || anchors.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
        }
      });
    }

    this.addStandardInventorySlots(inventory, 8, 51);
  }

  public List<String> getAnchoredItemIds() {
    if (this.hopper instanceof ItemHubEntity entity) {
      return entity.getAnchoredItemIds();
    }
    List<String> anchors = new ArrayList<>();
    for (int index = 0; index < this.syncedAnchorCount; index++) {
      int encoded = this.syncedAnchoredItems[index];
      if (encoded > 0) {
        anchors.add(BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.byId(encoded - 1)).toString());
      }
    }
    return List.copyOf(anchors);
  }

  public BlockPos getBlockPos() {
    return this.blockPos;
  }

  public ResourceKey<Level> getDimension() {
    return this.dimension;
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

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return this.hopper.stillValid(player);
  }

  private static int encodeItem(
      String itemId
  ) {
    return BuiltInRegistries.ITEM.getId(
        BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId))
    ) + 1;
  }

  public record OpeningData(
      BlockPos blockPos,
      ResourceKey<Level> dimension,
      List<String> anchoredItemIds
  ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> PACKET_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpeningData::blockPos,
            ResourceKey.streamCodec(Registries.DIMENSION),
            OpeningData::dimension,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(ItemHubEntity.MAX_ANCHORED_ITEMS)),
            OpeningData::anchoredItemIds,
            OpeningData::new
        );

  }

}
