/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.blockentity.supreme;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
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
import roeyqian.magnatour.block.supreme.SupremeChest;
import roeyqian.magnatour.menu.supreme.SupremeChestContainer;
import roeyqian.magnatour.menu.supreme.SupremeChestMenu;
import roeyqian.magnatour.registry.content.RegBlockEntities;

public class SupremeChestEntity extends BlockEntity implements CustomContainer {

  public static final int SLOT_COUNT = 36;

  public static final long NO_GROUP_ORIGIN = Long.MIN_VALUE;

  private static final Component DEFAULT_NAME =
      Component.translatable("block.magnatour.supreme_chest");

  private long groupOrigin = NO_GROUP_ORIGIN;

  private final ChestLidController lidAnimator = new ChestLidController();

  private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {

    @Override
    protected void onOpen(
        @NonNull Level level,
        @NonNull BlockPos pos,
        @NonNull BlockState blockState
    ) {
      SupremeChest.playSound(level, pos, blockState, SoundEvents.CHEST_OPEN);
    }

    @Override
    protected void onClose(
        @NonNull Level level,
        @NonNull BlockPos pos,
        @NonNull BlockState blockState
    ) {
      SupremeChest.playSound(level, pos, blockState, SoundEvents.CHEST_CLOSE);
    }

    @Override
    protected void openerCountChanged(
        Level level,
        @NonNull BlockPos pos,
        BlockState blockState,
        int previous,
        int current
    ) {
      level.blockEvent(pos, blockState.getBlock(), 1, current);
    }

    @Override
    public boolean isOwnContainer(
        Player player
    ) {
      if (!(player.containerMenu instanceof SupremeChestMenu menu)) return false;

      Container container = menu.getContainer();
      return container == SupremeChestEntity.this
          || container instanceof SupremeChestContainer compound
          && compound.contains(SupremeChestEntity.this);
    }

  };

  private final NonNullList<ItemStack> inventory =
      NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

  public SupremeChestEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.SUPREME_CHEST_ENTITY, pos, state);
  }

  public static void tick(
      SupremeChestEntity chestEntity
  ) {
    chestEntity.lidAnimator.tickLid();
  }

  public AbstractContainerMenu createMenu(
      int syncId,
      @NonNull Inventory playerInventory
  ) {
    return new SupremeChestMenu(syncId, playerInventory, this, this.getContainerSize());
  }

  public float getAnimationProgress(
      float tickDelta
  ) {
    return this.lidAnimator.getOpenness(tickDelta);
  }

  public Component getDisplayName() {
    return DEFAULT_NAME;
  }

  @Override
  public @NonNull List<ContainerUser> getEntitiesWithContainerOpen() {
    return this.openersCounter.getEntitiesWithContainerOpen(
        this.getLevel(), this.getBlockPos()
    );
  }

  public long getGroupOrigin() {
    return this.groupOrigin;
  }

  @Override
  public NonNullList<ItemStack> getItems() {
    return this.inventory;
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

  public boolean hasGroupOrigin() {
    return this.groupOrigin != NO_GROUP_ORIGIN;
  }

  public void recheckOpen() {
    if (!this.remove) {
      this.openersCounter.recheckOpeners(
          this.getLevel(), this.getBlockPos(), this.getBlockState()
      );
    }
  }

  @Override
  public void setChanged() {
    super.setChanged();
  }

  public void setGroupOrigin(
      long groupOrigin
  ) {
    if (this.groupOrigin == groupOrigin) return;

    this.groupOrigin = groupOrigin;
    this.setChanged();

    if (this.level != null && !this.level.isClientSide()) {
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
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
    builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.inventory));
  }

  @Override
  protected void loadAdditional(
      @NonNull ValueInput input
  ) {
    super.loadAdditional(input);
    this.groupOrigin = input.getLongOr("GroupOrigin", NO_GROUP_ORIGIN);
    ContainerHelper.loadAllItems(input, this.inventory);
  }

  @Override
  protected void saveAdditional(
      @NonNull ValueOutput output
  ) {
    super.saveAdditional(output);
    output.putLong("GroupOrigin", this.groupOrigin);
    ContainerHelper.saveAllItems(output, this.inventory);
  }

}
