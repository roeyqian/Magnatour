/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.universe;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SingleRecipeInput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseWorkstationEntity;
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.content.UniverseMenus;
import roeyqian.magnatour.registry.logic.CustomRecipes;

public class UniverseWorkstationSmeltingMenu extends AbstractFurnaceMenu {

  // Container data packets encode values as signed 16-bit integers.
  private static final int MAX_SYNCED_FUEL_VALUE = Short.MAX_VALUE;

  private final ContainerData propertyDelegate;

  private final ContainerLevelAccess context;

  private final UniverseWorkstationEntity workstation;

  public UniverseWorkstationSmeltingMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(4));
  }

  public UniverseWorkstationSmeltingMenu(
      int syncId,
      Inventory playerInventory,
      Container inventory,
      ContainerData propertyDelegate
  ) {
    this(syncId, playerInventory, ContainerLevelAccess.NULL, inventory, propertyDelegate);
  }

  public UniverseWorkstationSmeltingMenu(
      int syncId,
      Inventory playerInventory,
      UniverseWorkstationEntity workstation,
      ContainerData data
  ) {
    this(syncId, playerInventory,
        ContainerLevelAccess.create(workstation.getLevel(), workstation.getBlockPos()), workstation, data);
  }

  private UniverseWorkstationSmeltingMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context,
      Container inventory,
      FuelProgressData syncedData
  ) {
    super(
        UniverseMenus.UNIVERSE_WORKSTATION_SMELTING_HANDLER,
        RecipePropertySet.FURNACE_INPUT,
        RecipeBookType.FURNACE,
        syncId,
        playerInventory,
        inventory,
        syncedData
    );
    this.context = context;
    this.workstation = inventory instanceof UniverseWorkstationEntity entity ? entity : null;
    this.propertyDelegate = syncedData;
    // Keep the inherited slot IDs and transfer rules, with the workstation layout.
    this.replaceSlot(0, new Slot(inventory, 0, 56, 35));
    this.replaceSlot(1, new FurnaceFuelSlot(this, inventory, 1, 56, 72));
    this.replaceSlot(2, new FurnaceResultSlot(playerInventory.player, inventory, 2, 116, 53));
    for (int index = 3; index < this.slots.size(); index++) {
      Slot slot = this.slots.get(index);
      this.replaceSlot(index, new Slot(slot.container, slot.getContainerSlot(), slot.x, slot.y + 36));
    }
  }

  public UniverseWorkstationSmeltingMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context,
      Container inventory,
      ContainerData propertyDelegate
  ) {
    this(
        syncId,
        playerInventory,
        context,
        inventory,
        new FuelProgressData(propertyDelegate)
    );
  }

  @Override
  public boolean clickMenuButton(
      @NonNull Player player,
      int id
  ) {
    return id == 0 && this.workstation != null && this.stillValid(player)
        && this.workstation.switchMenu(player, false);
  }

  public float getCookProgress() {
    int cookingTimer = this.propertyDelegate.get(2);
    int cookingTotalTime = this.propertyDelegate.get(3);
    if (cookingTotalTime == 0) {
      return 0.0F;
    }
    return (float) cookingTimer / (float) cookingTotalTime;
  }

  public float getFuelProgress() {
    int litTime = this.propertyDelegate.get(0);
    int litDuration = this.propertyDelegate.get(1);
    if (litDuration == 0) {
      return 0.0F;
    }
    return (float) litTime / (float) litDuration;
  }

  public boolean isBurning() {
    return this.propertyDelegate.get(0) > 0;
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return stillValid(this.context, player, UniverseBlocks.UNIVERSE_WORKSTATION);
  }

  @Override
  protected boolean canSmelt(
      @NonNull ItemStack itemStack
  ) {
    if (super.canSmelt(itemStack)) return true;

    if (this.level instanceof ServerLevel serverWorld) {
      SingleRecipeInput input = new SingleRecipeInput(itemStack);
      var recipes = serverWorld.recipeAccess().getSynchronizedRecipes();

      if (recipes.getFirstMatch(CustomRecipes.SUPREME_COOKING_TYPE, input, serverWorld).isPresent()) {
        return true;
      }

      return recipes
          .getFirstMatch(CustomRecipes.UNIVERSE_COOKING_TYPE, input, serverWorld)
          .isPresent();
    } else {
      return false;
    }
  }

  @Override
  protected boolean isFuel(
      @NonNull ItemStack itemStack
  ) {
    return !itemStack.isEmpty();
  }

  private void replaceSlot(
      int index,
      Slot slot
  ) {
    slot.index = index;
    this.slots.set(index, slot);
  }

  private static final class FuelProgressData implements ContainerData {

    private final ContainerData delegate;

    private FuelProgressData(
        ContainerData delegate
    ) {
      this.delegate = delegate;
    }

    @Override
    public int get(
        int index
    ) {
      if (index == 0) {
        int totalTime = this.delegate.get(1);
        int remainingTime = this.delegate.get(0);
        if (totalTime > MAX_SYNCED_FUEL_VALUE) {
          if (remainingTime <= 0) return 0;
          long scaledTime = ((long) remainingTime * MAX_SYNCED_FUEL_VALUE + totalTime - 1L)
              / totalTime;
          return (int) Math.min(scaledTime, MAX_SYNCED_FUEL_VALUE);
        }
        return Math.max(remainingTime, 0);
      }

      if (index == 1) {
        return Math.max(0, Math.min(this.delegate.get(1), MAX_SYNCED_FUEL_VALUE));
      }

      return this.delegate.get(index);
    }

    @Override
    public int getCount() {
      return this.delegate.getCount();
    }

    @Override
    public void set(
        int index, int value
    ) {
      this.delegate.set(index, value);
    }

  }

}
