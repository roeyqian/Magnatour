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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.CustomContainer;
import roeyqian.magnatour.menu.universe.UniverseVoidPoolMenu;
import roeyqian.magnatour.registry.content.RegBlockEntities;

public class UniverseVoidPoolEntity extends BlockEntity implements MenuProvider, CustomContainer {

  private int progress = 0;

  private final NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

  public UniverseVoidPoolEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.UNIVERSE_VOID_POOL_ENTITY, pos, state);
  }

  @Override
  public AbstractContainerMenu createMenu(
      int syncId,
      @NonNull Inventory playerInventory,
      @NonNull Player player
  ) {
    return new UniverseVoidPoolMenu(syncId, playerInventory, this);
  }

  @Override @NonNull
  public Component getDisplayName() {
    return Component.translatable("item.magnatour.universe_void_pool");
  }

  @Override
  public NonNullList<ItemStack> getItems() {
    return inventory;
  }

  public void tick(
      Level world
  ) {
    if (world.isClientSide()) return;

    ItemStack input = inventory.getFirst();
    if (!input.isEmpty()) {
      progress++;
      if (progress >= 20) {
        if (generating(input)) {
          progress = 0;
          setChanged();
        }
      }
    }
  }

  @Override
  protected void applyImplicitComponents(
      @NonNull DataComponentGetter components
  ) {
    super.applyImplicitComponents(components);
    ItemContainerContents containerComponent = components.get(DataComponents.CONTAINER);
    if (containerComponent != null) containerComponent.copyInto(this.inventory);
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

  private boolean generating(
      ItemStack input
  ) {
    for (int i = 1; i < 7; i++) {
      ItemStack out = inventory.get(i);
      if (out.isEmpty()) {
        inventory.set(i, input.copyWithCount(1));
        return true;
      } else if (ItemStack.isSameItemSameComponents(input, out)
          && out.getCount() < out.getMaxStackSize()
      ) {
        out.grow(1);
        return true;
      }
    }
    return false;
  }

}
