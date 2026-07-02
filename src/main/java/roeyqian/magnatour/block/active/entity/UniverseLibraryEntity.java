/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.active.entity;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.CustomContainer;
import roeyqian.magnatour.menu.block.UniverseLibraryMenu;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class UniverseLibraryEntity extends BlockEntity implements MenuProvider, CustomContainer {

  private boolean opened = false;

  private final ChestLidController lidAnimator = new ChestLidController();

  private final NonNullList<ItemStack> inventory = NonNullList.withSize(252, ItemStack.EMPTY);

  public UniverseLibraryEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.UNIVERSE_LIBRARY_ENTITY, pos, state);
  }

  public static void tick(
      UniverseLibraryEntity libraryBe
  ) {
    libraryBe.lidAnimator.tickLid();
  }

  @Override
  public AbstractContainerMenu createMenu(
      int syncId,
      @NonNull Inventory playerInventory,
      @NonNull Player player
  ) {
    return new UniverseLibraryMenu(syncId, playerInventory, this);
  }

  public float getAnimationProgress(
      float tickDelta
  ) {
    return this.lidAnimator.getOpenness(tickDelta);
  }

  @Override
  public int getContainerSize() {
    return 252;
  }

  @Override @NonNull
  public Component getDisplayName() {
    return Component.translatable("item.magnatour.universe_library");
  }

  @Override
  public NonNullList<ItemStack> getItems() {
    return this.inventory;
  }

  public boolean isOpened() {
    return this.opened;
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

  public void setOpened(
      boolean opened
  ) {
    this.opened = opened;
    this.setChanged();
  }

  @Override
  public boolean triggerEvent(
      int type,
      int data
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

    ItemContainerContents container = components.get(DataComponents.CONTAINER);
    if (container != null) {
      this.inventory.clear();
      container.copyInto(this.inventory);
    }
  }

  @Override
  protected void collectImplicitComponents(
      DataComponentMap.@NonNull Builder builder
  ) {
    super.collectImplicitComponents(builder);

    if (!this.inventory.isEmpty()) {
      builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.inventory));
    } else {
      builder.set(
          DataComponents.CONTAINER,
          ItemContainerContents.fromItems(NonNullList.withSize(252, ItemStack.EMPTY))
      );
    }
  }

  @Override
  protected void loadAdditional(
      @NonNull ValueInput view
  ) {
    super.loadAdditional(view);
    ContainerHelper.loadAllItems(view, this.inventory);
  }

  @Override
  protected void saveAdditional(
      @NonNull ValueOutput view
  ) {
    super.saveAdditional(view);
    ContainerHelper.saveAllItems(view, this.inventory);
  }

}
