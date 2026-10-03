/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.blockentity.supreme;

// Java Standard
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

// Mojang
import com.mojang.serialization.Codec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.item.Items;
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
import roeyqian.magnatour.menu.supreme.ItemHubMenu;
import roeyqian.magnatour.registry.content.SupremeBlockEntities;

public class ItemHubEntity extends RandomizableContainerBlockEntity implements Hopper {

  public static final int HOPPER_CONTAINER_SIZE = 5;
  public static final int MAX_ANCHORED_ITEMS = 64;
  public static final int MOVE_ITEM_SPEED = 1;

  private static final int NO_COOLDOWN_TIME = -1;
  private static final int OUTPUT_ITEMS_PER_TRANSFER = 2;

  private static final int[][] CACHED_SLOTS = new int[54][];

  private static final String ANCHORED_ITEM_IDS_KEY = "AnchoredItemIds";
  private static final String LEGACY_FILTER_ITEM_ID_KEY = "FilterItemId";
  private static final String TRANSFER_COOLDOWN_KEY = "TransferCooldown";

  private static final Component DEFAULT_NAME =
      Component.translatable("block.magnatour.item_hub");

  private int cooldownTime = NO_COOLDOWN_TIME;

  private long tickedGameTime;

  private List<String> anchoredItemIds = List.of();

  private Set<Item> anchoredItems = Set.of();

  private NonNullList<ItemStack> items = NonNullList.withSize(HOPPER_CONTAINER_SIZE, ItemStack.EMPTY);

  public ItemHubEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(SupremeBlockEntities.ITEM_HUB_ENTITY, pos, state);
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
        && itemHubEntity.matchesAnchor(itemEntity.getItem())
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
  public static String normalizeAnchorItemId(
      @Nullable String itemId
  ) {
    String trimmed = itemId == null ? "" : itemId.trim();
    if (trimmed.isEmpty()) {
      return null;
    }

    Identifier identifier = Identifier.tryParse(trimmed);
    if (identifier == null || !BuiltInRegistries.ITEM.containsKey(identifier)
        || BuiltInRegistries.ITEM.getValue(identifier) == Items.AIR) {
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
          if (itemHubEntity.matchesAnchor(entity.getItem()) && addItem(itemHubEntity, entity)) {
            return true;
          }
        }
      }

      return false;
    }
  }

  public boolean addAnchoredItem(
      String rawItemId
  ) {
    String itemId = normalizeAnchorItemId(rawItemId);
    if (itemId == null || this.anchoredItemIds.contains(itemId)
        || this.anchoredItemIds.size() >= MAX_ANCHORED_ITEMS) {
      return false;
    }
    List<String> updated = new ArrayList<>(this.anchoredItemIds);
    updated.add(itemId);
    this.setAnchoredItemsInternal(updated);
    this.syncChanged();
    return true;
  }

  @Override
  public boolean canPlaceItem(
      int slot,
      @NonNull ItemStack itemStack
  ) {
    return this.matchesAnchor(itemStack);
  }

  public List<String> getAnchoredItemIds() {
    return this.anchoredItemIds;
  }

  @Override
  public int getContainerSize() {
    return this.items.size();
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

  public boolean removeAnchoredItem(
      String rawItemId
  ) {
    String itemId = normalizeAnchorItemId(rawItemId);
    if (itemId == null || !this.anchoredItemIds.contains(itemId)) {
      return false;
    }
    List<String> updated = new ArrayList<>(this.anchoredItemIds);
    updated.remove(itemId);
    this.setAnchoredItemsInternal(updated);
    this.syncChanged();
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
        this.anchoredItemIds
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
    // Migrate worlds saved before Item Hub supported multiple anchored items.
    List<String> savedItems = input.read(ANCHORED_ITEM_IDS_KEY, Codec.STRING.listOf())
        .orElseGet(() -> List.of(input.getStringOr(LEGACY_FILTER_ITEM_ID_KEY, "")));
    this.setAnchoredItemsInternal(savedItems);
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
    output.store(ANCHORED_ITEM_IDS_KEY, Codec.STRING.listOf(), this.anchoredItemIds);
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
        && itemHubEntity.matchesAnchor(itemStack)
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
        int remaining = OUTPUT_ITEMS_PER_TRANSFER;
        for (int slot = 0; slot < itemHubEntity.getContainerSize() && remaining > 0; slot++) {
          // Keep restoration independent of the stack handed to the target.
          ItemStack itemStack = itemHubEntity.getItem(slot).copy();
          if (!itemStack.isEmpty()) {
            int originalCount = itemStack.getCount();
            int requestedCount = Math.min(remaining, originalCount);
            ItemStack result = addItem(
                itemHubEntity,
                container,
                itemHubEntity.removeItem(slot, requestedCount),
                direction
            );
            int movedCount = requestedCount - result.getCount();
            if (movedCount > 0) {
              container.setChanged();
              remaining -= movedCount;
            }

            // Restore only the unaccepted items, including partial transfers.
            itemStack.setCount(originalCount - movedCount);
            itemHubEntity.setItem(slot, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack);
          }
        }

        return remaining < OUTPUT_ITEMS_PER_TRANSFER;
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

  private boolean matchesAnchor(
      ItemStack itemStack
  ) {
    return this.anchoredItems.isEmpty() || this.anchoredItems.contains(itemStack.getItem());
  }

  private boolean isOnCooldown() {
    return this.cooldownTime > 0;
  }

  private void setCooldown(
      int cooldownTime
  ) {
    this.cooldownTime = cooldownTime;
  }

  private void setAnchoredItemsInternal(
      List<String> itemIds
  ) {
    Set<String> normalizedIds = new LinkedHashSet<>();
    for (String rawItemId : itemIds) {
      String itemId = normalizeAnchorItemId(rawItemId);
      if (itemId != null) {
        normalizedIds.add(itemId);
      }
      if (normalizedIds.size() >= MAX_ANCHORED_ITEMS) {
        break;
      }
    }
    this.anchoredItemIds = List.copyOf(normalizedIds);
    Set<Item> items = new LinkedHashSet<>();
    for (String itemId : this.anchoredItemIds) {
      items.add(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)));
    }
    this.anchoredItems = Set.copyOf(items);
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
