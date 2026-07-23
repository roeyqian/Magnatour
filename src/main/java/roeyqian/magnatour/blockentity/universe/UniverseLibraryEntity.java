/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.CustomContainer;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;
import roeyqian.magnatour.registry.content.RegBlockEntities;

public class UniverseLibraryEntity extends BlockEntity implements MenuProvider, CustomContainer {

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
        Player player
    ) {
      return player.containerMenu instanceof UniverseLibraryMenu menu && menu.isFor(UniverseLibraryEntity.this);
    }

  };

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
  public boolean stillValid(
      @NonNull Player player
  ) {
    return Container.stillValidBlockEntity(this, player);
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
