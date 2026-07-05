/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.active.entity;

// Java Standard
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.menu.block.ItemHubMenu;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class ItemHubEntity extends RandomizableContainerBlockEntity implements Hopper {

  public static final int HOPPER_CONTAINER_SIZE = 5;
  public static final int MOVE_ITEM_SPEED = 1;

  private static final int NO_COOLDOWN_TIME = -1;

  private static final int[][] CACHED_SLOTS = new int[54][];

  private static final String FILTER_ITEM_ID_KEY = "FilterItemId";
  private static final String TRANSFER_COOLDOWN_KEY = "TransferCooldown";

  private static final Component DEFAULT_NAME =
      Component.translatable("block.magnatour.item_hub");

  private int cooldownTime = NO_COOLDOWN_TIME;

  private long tickedGameTime;

  private String filterItemId = "";

  @Nullable
  private Item filterItem;

  private NonNullList<ItemStack> items = NonNullList.withSize(HOPPER_CONTAINER_SIZE, ItemStack.EMPTY);

  public ItemHubEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.ITEM_HUB_ENTITY, pos, state);
  }

  public static boolean addItem(
      Container container,
      ItemEntity entity
  ) {
    boolean changed = false;
    ItemStack copy = entity.getItem().copy();
    ItemStack result = addItem(null, container, copy, null);
    if (result.isEmpty()) {
      changed = true;
      entity.setItem(ItemStack.EMPTY);
      entity.discard();
    } else {
      entity.setItem(result);
    }

    return changed;
  }

  public static ItemStack addItem(
      @Nullable Container from,
      Container container,
      ItemStack itemStack,
      @Nullable Direction direction
  ) {
    if (container instanceof WorldlyContainer worldly && direction != null) {
      int[] slots = worldly.getSlotsForFace(direction);

      for (int i = 0; i < slots.length && !itemStack.isEmpty(); i++) {
        itemStack = tryMoveInItem(from, container, itemStack, slots[i], direction);
      }
    } else {
      int size = container.getContainerSize();

      for (int i = 0; i < size && !itemStack.isEmpty(); i++) {
        itemStack = tryMoveInItem(from, container, itemStack, i, direction);
      }
    }

    return itemStack;
  }

  public static void entityInside(
      Level level,
      BlockPos pos,
      BlockState state,
      Entity entity,
      ItemHubEntity itemHubEntity
  ) {
    if (entity instanceof ItemEntity itemEntity
        && !itemEntity.getItem().isEmpty()
        && itemHubEntity.matchesFilter(itemEntity.getItem())
        && entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())
            .intersects(itemHubEntity.getSuckAabb())
    ) {
      tryMoveItems(level, pos, state, itemHubEntity, () -> addItem(itemHubEntity, itemEntity));
    }
  }

  @Nullable
  public static Container getContainerAt(
      Level level,
      BlockPos pos
  ) {
    return getContainerAt(
        level,
        pos,
        level.getBlockState(pos),
        pos.getX() + 0.5,
        pos.getY() + 0.5,
        pos.getZ() + 0.5
    );
  }

  public static List<ItemEntity> getItemsAtAndAbove(
      Level level,
      Hopper hopper
  ) {
    AABB aabb = hopper.getSuckAabb()
        .move(hopper.getLevelX() - 0.5, hopper.getLevelY() - 0.5, hopper.getLevelZ() - 0.5);
    return level.getEntitiesOfClass(ItemEntity.class, aabb, EntitySelector.ENTITY_STILL_ALIVE);
  }

  @Nullable
  public static String normalizeFilterItemId(
      @Nullable String filterItemId
  ) {
    String trimmed = filterItemId == null ? "" : filterItemId.trim();
    if (trimmed.isEmpty()) {
      return "";
    }

    Identifier identifier = Identifier.tryParse(trimmed);
    if (identifier == null || !BuiltInRegistries.ITEM.containsKey(identifier)) {
      return null;
    }

    return identifier.toString();
  }

  public static void pushItemsTick(
      Level level,
      BlockPos pos,
      BlockState state,
      ItemHubEntity entity
  ) {
    entity.cooldownTime--;
    entity.tickedGameTime = level.getGameTime();
    if (!entity.isOnCooldown()) {
      entity.setCooldown(0);
      tryMoveItems(level, pos, state, entity, () -> suckInItems(level, entity));
    }
  }

  public static boolean suckInItems(
      Level level,
      ItemHubEntity itemHubEntity
  ) {
    BlockPos blockPos = BlockPos.containing(
        itemHubEntity.getLevelX(),
        itemHubEntity.getLevelY() + 1.0,
        itemHubEntity.getLevelZ()
    );
    BlockState blockState = level.getBlockState(blockPos);
    Container container = getSourceContainer(level, itemHubEntity, blockPos, blockState);
    if (container != null) {
      Direction direction = Direction.DOWN;

      for (int slot : getSlots(container, direction)) {
        if (tryTakeInItemFromSlot(itemHubEntity, container, slot, direction)) {
          return true;
        }
      }

      return false;
    } else {
      boolean isBlocked = itemHubEntity.isGridAligned()
          && blockState.isCollisionShapeFullBlock(level, blockPos)
          && !blockState.is(BlockTags.DOES_NOT_BLOCK_HOPPERS);
      if (!isBlocked) {
        for (ItemEntity entity : getItemsAtAndAbove(level, itemHubEntity)) {
          if (itemHubEntity.matchesFilter(entity.getItem()) && addItem(itemHubEntity, entity)) {
            return true;
          }
        }
      }

      return false;
    }
  }

  public boolean applyFilterItemId(
      @Nullable String rawFilterItemId
  ) {
    String normalizedFilterItemId = normalizeFilterItemId(rawFilterItemId);
    if (normalizedFilterItemId == null) {
      return false;
    }
    if (Objects.equals(this.filterItemId, normalizedFilterItemId)) {
      return true;
    }

    this.setFilterItemIdInternal(normalizedFilterItemId);
    this.syncChanged();
    return true;
  }

  @Override
  public boolean canPlaceItem(
      int slot,
      @NonNull ItemStack itemStack
  ) {
    return this.matchesFilter(itemStack);
  }

  @Override
  public int getContainerSize() {
    return this.items.size();
  }

  public String getFilterItemId() {
    return this.filterItemId;
  }

  @Override
  public double getLevelX() {
    return this.worldPosition.getX() + 0.5;
  }

  @Override
  public double getLevelY() {
    return this.worldPosition.getY() + 0.5;
  }

  @Override
  public double getLevelZ() {
    return this.worldPosition.getZ() + 0.5;
  }

  @Override
  public @NonNull Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public @NonNull CompoundTag getUpdateTag(
      HolderLookup.@NonNull Provider registries
  ) {
    return this.saveWithoutMetadata(registries);
  }

  @Override
  public boolean isGridAligned() {
    return true;
  }

  @Override
  public @NonNull ItemStack removeItem(
      int slot,
      int count
  ) {
    this.unpackLootTable(null);
    return ContainerHelper.removeItem(this.getItems(), slot, count);
  }

  @Override
  public void setItem(
      int slot,
      @NonNull ItemStack itemStack
  ) {
    this.unpackLootTable(null);
    this.getItems().set(slot, itemStack);
    itemStack.limitSize(this.getMaxStackSize(itemStack));
  }

  @Override
  protected @NonNull AbstractContainerMenu createMenu(
      int containerId,
      @NonNull Inventory inventory
  ) {
    return new ItemHubMenu(
        containerId,
        inventory,
        this,
        this.worldPosition,
        this.level == null ? Level.OVERWORLD : this.level.dimension(),
        this.filterItemId
    );
  }

  @Override
  protected @NonNull Component getDefaultName() {
    return DEFAULT_NAME;
  }

  @Override
  protected @NonNull NonNullList<ItemStack> getItems() {
    return this.items;
  }

  @Override
  protected void loadAdditional(
      @NonNull ValueInput input
  ) {
    super.loadAdditional(input);
    this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    if (!this.tryLoadLootTable(input)) {
      ContainerHelper.loadAllItems(input, this.items);
    }

    this.cooldownTime = input.getIntOr(TRANSFER_COOLDOWN_KEY, NO_COOLDOWN_TIME);
    String normalizedFilterItemId = normalizeFilterItemId(input.getStringOr(FILTER_ITEM_ID_KEY, ""));
    this.setFilterItemIdInternal(normalizedFilterItemId == null ? "" : normalizedFilterItemId);
  }

  @Override
  protected void saveAdditional(
      @NonNull ValueOutput output
  ) {
    super.saveAdditional(output);
    if (!this.trySaveLootTable(output)) {
      ContainerHelper.saveAllItems(output, this.items);
    }

    output.putInt(TRANSFER_COOLDOWN_KEY, this.cooldownTime);
    output.putString(FILTER_ITEM_ID_KEY, this.filterItemId);
  }

  @Override
  protected void setItems(
      @NonNull NonNullList<ItemStack> items
  ) {
    this.items = items;
  }

  private static ItemStack tryMoveInItem(
      @Nullable Container from,
      Container container,
      ItemStack itemStack,
      int slot,
      @Nullable Direction direction
  ) {
    ItemStack current = container.getItem(slot);
    if (canPlaceItemInContainer(container, itemStack, slot, direction)) {
      boolean success = false;
      boolean wasEmpty = container.isEmpty();
      if (current.isEmpty()) {
        container.setItem(slot, itemStack);
        itemStack = ItemStack.EMPTY;
        success = true;
      } else if (canMergeItems(current, itemStack)) {
        int space = itemStack.getMaxStackSize() - current.getCount();
        int count = Math.min(itemStack.getCount(), space);
        itemStack.shrink(count);
        current.grow(count);
        success = count > 0;
      }

      if (success) {
        if (wasEmpty
            && container instanceof ItemHubEntity itemHubEntity
            && !itemHubEntity.isOnCustomCooldown()
        ) {
          int skipTickCount = 0;
          if (from instanceof ItemHubEntity fromItemHub
              && itemHubEntity.tickedGameTime >= fromItemHub.tickedGameTime
          ) {
            skipTickCount = 1;
          }

          itemHubEntity.setCooldown(Math.max(0, MOVE_ITEM_SPEED - skipTickCount));
        }

        container.setChanged();
      }
    }

    return itemStack;
  }

  private static boolean tryMoveItems(
      Level level,
      BlockPos pos,
      BlockState state,
      ItemHubEntity itemHubEntity,
      BooleanSupplier action
  ) {
    if (level.isClientSide()) {
      return false;
    } else {
      if (!itemHubEntity.isOnCooldown() && state.getValue(HopperBlock.ENABLED)) {
        boolean changed = false;
        if (!itemHubEntity.isEmpty()) {
          changed = ejectItems(level, pos, itemHubEntity);
        }

        if (!itemHubEntity.inventoryFull()) {
          changed |= action.getAsBoolean();
        }

        if (changed) {
          itemHubEntity.setCooldown(MOVE_ITEM_SPEED);
          setChanged(level, pos, state);
          return true;
        }
      }

      return false;
    }
  }

  @Nullable
  private static Container getContainerAt(
      Level level,
      BlockPos pos,
      BlockState state,
      double x,
      double y,
      double z
  ) {
    Container result = getBlockContainer(level, pos, state);
    if (result == null) {
      result = getEntityContainer(level, x, y, z);
    }

    return result;
  }

  @Nullable
  private static Container getSourceContainer(
      Level level,
      Hopper hopper,
      BlockPos pos,
      BlockState state
  ) {
    return getContainerAt(
        level,
        pos,
        state,
        hopper.getLevelX(),
        hopper.getLevelY() + 1.0,
        hopper.getLevelZ()
    );
  }

  private static int[] getSlots(
      Container container,
      Direction direction
  ) {
    if (container instanceof WorldlyContainer worldlyContainer) {
      return worldlyContainer.getSlotsForFace(direction);
    } else {
      int containerSize = container.getContainerSize();
      if (containerSize < CACHED_SLOTS.length) {
        int[] cachedSlots = CACHED_SLOTS[containerSize];
        if (cachedSlots != null) {
          return cachedSlots;
        } else {
          int[] slots = createFlatSlots(containerSize);
          CACHED_SLOTS[containerSize] = slots;
          return slots;
        }
      } else {
        return createFlatSlots(containerSize);
      }
    }
  }

  private static boolean tryTakeInItemFromSlot(
      ItemHubEntity itemHubEntity,
      Container container,
      int slot,
      Direction direction
  ) {
    ItemStack itemStack = container.getItem(slot);
    if (!itemStack.isEmpty()
        && itemHubEntity.matchesFilter(itemStack)
        && canTakeItemFromContainer(itemHubEntity, container, itemStack, slot, direction)
    ) {
      int originalCount = itemStack.getCount();
      ItemStack result = addItem(container, itemHubEntity, container.removeItem(slot, 1), null);
      if (result.isEmpty()) {
        container.setChanged();
        return true;
      }

      itemStack.setCount(originalCount);
      if (originalCount == 1) {
        container.setItem(slot, itemStack);
      }
    }

    return false;
  }

  private static boolean canPlaceItemInContainer(
      Container container,
      ItemStack itemStack,
      int slot,
      @Nullable Direction direction
  ) {
    return !container.canPlaceItem(slot, itemStack)
        ? false
        : !(container instanceof WorldlyContainer worldly
        && !worldly.canPlaceItemThroughFace(slot, itemStack, direction));
  }

  private static boolean canMergeItems(
      ItemStack first,
      ItemStack second
  ) {
    return first.getCount() <= first.getMaxStackSize()
        && ItemStack.isSameItemSameComponents(first, second);
  }

  private static boolean ejectItems(
      Level level,
      BlockPos pos,
      ItemHubEntity itemHubEntity
  ) {
    Container container = getAttachedContainer(level, pos, itemHubEntity);
    if (container == null) {
      return false;
    } else {
      Direction direction = itemHubEntity.getFacing().getOpposite();
      if (isFullContainer(container, direction)) {
        return false;
      } else {
        for (int slot = 0; slot < itemHubEntity.getContainerSize(); slot++) {
          ItemStack itemStack = itemHubEntity.getItem(slot);
          if (!itemStack.isEmpty()) {
            int originalCount = itemStack.getCount();
            ItemStack result = addItem(
                itemHubEntity,
                container,
                itemHubEntity.removeItem(slot, 1),
                direction
            );
            if (result.isEmpty()) {
              container.setChanged();
              return true;
            }

            itemStack.setCount(originalCount);
            if (originalCount == 1) {
              itemHubEntity.setItem(slot, itemStack);
            }
          }
        }

        return false;
      }
    }
  }

  @Nullable
  private static Container getBlockContainer(
      Level level,
      BlockPos pos,
      BlockState state
  ) {
    Block block = state.getBlock();
    if (block instanceof WorldlyContainerHolder) {
      return ((WorldlyContainerHolder) block).getContainer(state, level, pos);
    } else if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof Container container) {
      if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
        container = ChestBlock.getContainer((ChestBlock) block, state, level, pos, true);
      }

      return container;
    } else {
      return null;
    }
  }

  @Nullable
  private static Container getEntityContainer(
      Level level,
      double x,
      double y,
      double z
  ) {
    List<Entity> entities = level.getEntities(
        (Entity) null,
        new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5),
        EntitySelector.CONTAINER_ENTITY_SELECTOR
    );
    return !entities.isEmpty()
        ? (Container) entities.get(level.getRandom().nextInt(entities.size()))
        : null;
  }

  private static int[] createFlatSlots(
      int containerSize
  ) {
    int[] slots = new int[containerSize];
    int i = 0;

    while (i < slots.length) {
      slots[i] = i++;
    }

    return slots;
  }

  private static boolean canTakeItemFromContainer(
      Container into,
      Container from,
      ItemStack itemStack,
      int slot,
      Direction direction
  ) {
    return !from.canTakeItem(into, slot, itemStack)
        ? false
        : !(from instanceof WorldlyContainer worldly
        && !worldly.canTakeItemThroughFace(slot, itemStack, direction));
  }

  @Nullable
  private static Container getAttachedContainer(
      Level level,
      BlockPos pos,
      ItemHubEntity itemHubEntity
  ) {
    return getContainerAt(level, pos.relative(itemHubEntity.getFacing()));
  }

  private static boolean isFullContainer(
      Container container,
      Direction direction
  ) {
    int[] slots = getSlots(container, direction);

    for (int slot : slots) {
      ItemStack itemStack = container.getItem(slot);
      if (itemStack.getCount() < itemStack.getMaxStackSize()) {
        return false;
      }
    }

    return true;
  }

  private boolean matchesFilter(
      ItemStack itemStack
  ) {
    return this.filterItem == null || itemStack.is(this.filterItem);
  }

  private boolean isOnCooldown() {
    return this.cooldownTime > 0;
  }

  private void setCooldown(
      int cooldownTime
  ) {
    this.cooldownTime = cooldownTime;
  }

  private void setFilterItemIdInternal(
      String filterItemId
  ) {
    this.filterItemId = filterItemId;
    if (filterItemId.isEmpty()) {
      this.filterItem = null;
      return;
    }

    Identifier identifier = Identifier.tryParse(filterItemId);
    this.filterItem = identifier == null ? null : BuiltInRegistries.ITEM.getValue(identifier);
  }

  private void syncChanged() {
    this.setChanged();
    if (this.level != null && !this.level.isClientSide()) {
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }
  }

  private boolean isOnCustomCooldown() {
    return this.cooldownTime > MOVE_ITEM_SPEED;
  }

  private boolean inventoryFull() {
    for (ItemStack itemStack : this.items) {
      if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxStackSize()) {
        return false;
      }
    }

    return true;
  }

  private Direction getFacing() {
    return this.getBlockState().getValue(HopperBlock.FACING);
  }

}
