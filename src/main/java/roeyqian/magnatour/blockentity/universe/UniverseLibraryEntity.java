/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.blockentity.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.universe.UniverseLibraryContents;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;
import roeyqian.magnatour.registry.content.UniverseBlockEntities;
import roeyqian.magnatour.registry.logic.CustomComponents;

public class UniverseLibraryEntity extends BaseContainerBlockEntity {

  public static final int CONTAINER_SIZE = 252;
  public static final int STORAGE_STACK_LIMIT = 1_000_000_000;

  private final ChestLidController lidAnimator = new ChestLidController();

  private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {

    @Override
    protected void onOpen(
        @NonNull Level level,
        @NonNull BlockPos pos,
        @NonNull BlockState blockState
    ) {
      level.playSound(
          null,
          pos,
          SoundEvents.ENDER_CHEST_OPEN,
          SoundSource.BLOCKS,
          0.5F,
          level.getRandom().nextFloat() * 0.1F + 0.9F
      );
    }

    @Override
    protected void onClose(
        @NonNull Level level,
        @NonNull BlockPos pos,
        @NonNull BlockState blockState
    ) {
      level.playSound(
          null,
          pos,
          SoundEvents.ENDER_CHEST_CLOSE,
          SoundSource.BLOCKS,
          0.5F,
          level.getRandom().nextFloat() * 0.1F + 0.9F
      );
    }

    @Override
    protected void openerCountChanged(
        @NonNull Level level,
        @NonNull BlockPos pos,
        @NonNull BlockState blockState,
        int previous,
        int current
    ) {
      level.blockEvent(pos, blockState.getBlock(), 1, current);
    }

    @Override
    public boolean isOwnContainer(
        @NonNull Player player
    ) {
      return player.containerMenu instanceof UniverseLibraryMenu menu && menu.isFor(UniverseLibraryEntity.this);
    }

  };

  private NonNullList<ItemStack> inventory = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

  public UniverseLibraryEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(UniverseBlockEntities.UNIVERSE_LIBRARY_ENTITY, pos, state);
  }

  public static int getStorageStackLimit(
      ItemStack stack
  ) {
    return STORAGE_STACK_LIMIT;
  }

  public static void tick(
      UniverseLibraryEntity libraryBe
  ) {
    libraryBe.lidAnimator.tickLid();
  }

  public float getAnimationProgress(
      float tickDelta
  ) {
    return this.lidAnimator.getOpenness(tickDelta);
  }

  @Override
  public int getContainerSize() {
    return CONTAINER_SIZE;
  }

  @Override
  public int getMaxStackSize() {
    return STORAGE_STACK_LIMIT;
  }

  @Override
  public int getMaxStackSize(
      @NonNull ItemStack stack
  ) {
    return getStorageStackLimit(stack);
  }

  public ItemStack insertItems(
      ItemStack stack
  ) {
    int originalCount = stack.getCount();
    // Keep slot identities stable for open menus: fill matching stacks first,
    // without moving existing stacks to other slots or changing their components.
    for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
      for (int slot = 0; slot < this.getContainerSize() && !stack.isEmpty(); slot++) {
        if (!this.canPlaceItem(slot, stack)) continue;

        ItemStack existing = this.getItem(slot);
        if (pass == 0) {
          if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) continue;
          int space = this.getMaxStackSize(existing) - existing.getCount();
          if (space <= 0) continue;
          int moved = Math.min(space, stack.getCount());
          existing.grow(moved);
          stack.shrink(moved);
        } else {
          if (!existing.isEmpty()) continue;
          int moved = Math.min(this.getMaxStackSize(stack), stack.getCount());
          if (moved <= 0) continue;
          this.inventory.set(slot, stack.copyWithCount(moved));
          stack.shrink(moved);
        }
      }
    }
    // Commit and refresh open menus once after the entire insertion, rather
    // than once per slot with intermediate inventory states.
    if (stack.getCount() < originalCount) this.setChanged();
    return stack.isEmpty() ? ItemStack.EMPTY : stack;
  }

  public boolean isStorageFull() {
    for (ItemStack stack : this.inventory) {
      if (stack.isEmpty() || stack.getCount() < this.getMaxStackSize(stack)) return false;
    }
    return true;
  }

  @Override
  public void preRemoveSideEffects(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    // The loot table preserves the inventory inside the dropped container item.
  }

  public void recheckOpen() {
    if (!this.remove) {
      this.openersCounter.recheckOpeners(
          this.getLevel(),
          this.getBlockPos(),
          this.getBlockState()
      );
    }
  }

  @Override
  public void removeComponentsFromTag(
      @NonNull ValueOutput view
  ) {
    super.removeComponentsFromTag(view);
    view.discard("LibraryContents");
  }

  @Override
  public void setChanged() {
    super.setChanged();

    if (this.level == null || this.level.isClientSide()) return;

    for (Player player : this.level.players()) {
      if (player instanceof ServerPlayer
          && player.containerMenu instanceof UniverseLibraryMenu menu
          && menu.isFor(this)) {
        menu.refreshFromSource();
      }
    }
  }

  @Override
  public void startOpen(
      @NonNull ContainerUser containerUser
  ) {
    if (!this.remove && !containerUser.getLivingEntity().isSpectator()) {
      this.openersCounter.incrementOpeners(
          containerUser.getLivingEntity(),
          this.getLevel(),
          this.getBlockPos(),
          this.getBlockState(),
          containerUser.getContainerInteractionRange()
      );
    }
  }

  @Override
  public void stopOpen(
      @NonNull ContainerUser containerUser
  ) {
    if (!this.remove && !containerUser.getLivingEntity().isSpectator()) {
      this.openersCounter.decrementOpeners(
          containerUser.getLivingEntity(),
          this.getLevel(),
          this.getBlockPos(),
          this.getBlockState()
      );
    }
  }

  @Override
  public boolean triggerEvent(
      int type, int data
  ) {
    if (type == 1) {
      this.lidAnimator.shouldBeOpen(data > 0);
      return true;
    }
    return super.triggerEvent(type, data);
  }

  @Override
  protected void applyImplicitComponents(
      @NonNull DataComponentGetter components
  ) {
    super.applyImplicitComponents(components);
    UniverseLibraryContents contents = components.get(CustomComponents.UNIVERSE_LIBRARY_CONTENTS);
    if (contents != null) contents.copyInto(this.inventory);
  }

  @Override
  protected void collectImplicitComponents(
      DataComponentMap.@NonNull Builder builder
  ) {
    super.collectImplicitComponents(builder);
    builder.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    builder.set(CustomComponents.UNIVERSE_LIBRARY_CONTENTS, UniverseLibraryContents.fromItems(this.inventory));
  }

  @Override @NonNull
  protected AbstractContainerMenu createMenu(
      int syncId,
      @NonNull Inventory playerInventory
  ) {
    return new UniverseLibraryMenu(syncId, playerInventory, this);
  }

  @Override @NonNull
  protected Component getDefaultName() {
    return Component.translatable("item.magnatour.universe_library");
  }

  @Override
  protected NonNullList<ItemStack> getItems() {
    return this.inventory;
  }

  @Override
  protected void loadAdditional(
      @NonNull ValueInput view
  ) {
    super.loadAdditional(view);
    this.inventory = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    UniverseLibraryContents contents = view.read("LibraryContents", UniverseLibraryContents.CODEC).orElse(null);
    if (contents != null) contents.copyInto(this.inventory);
    else ContainerHelper.loadAllItems(view, this.inventory);
  }

  @Override
  protected void saveAdditional(
      @NonNull ValueOutput view
  ) {
    super.saveAdditional(view);
    view.store("LibraryContents", UniverseLibraryContents.CODEC, UniverseLibraryContents.fromItems(this.inventory));
  }

  @Override
  protected void setItems(
      @NonNull NonNullList<ItemStack> items
  ) {
    this.inventory = items;
  }

}
