/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
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
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SingleRecipeInput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.RegActiveBlocks;
import roeyqian.magnatour.registry.logic.RegRecipes;
import roeyqian.magnatour.registry.content.RegBlockMenus;

public class UniverseRefineryMenu extends AbstractFurnaceMenu {

  private final ContainerData propertyDelegate;

  private final ContainerLevelAccess context;

  public UniverseRefineryMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(4));
  }

  public UniverseRefineryMenu(
      int syncId,
      Inventory playerInventory,
      Container inventory,
      ContainerData propertyDelegate
  ) {
    this(syncId, playerInventory, ContainerLevelAccess.NULL, inventory, propertyDelegate);
  }

  public UniverseRefineryMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context,
      Container inventory,
      ContainerData propertyDelegate
  ) {
    super(
        RegBlockMenus.UNIVERSE_REFINERY_HANDLER,
        RecipePropertySet.FURNACE_INPUT,
        RecipeBookType.FURNACE,
        syncId,
        playerInventory,
        inventory,
        propertyDelegate
    );
    this.context = context;
    this.propertyDelegate = propertyDelegate;
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
    return stillValid(this.context, player, RegActiveBlocks.UNIVERSE_REFINERY);
  }

  @Override
  protected boolean canSmelt(
      @NonNull ItemStack itemStack
  ) {
    if (super.canSmelt(itemStack)) return true;

    if (this.level instanceof ServerLevel serverWorld) {
      SingleRecipeInput input = new SingleRecipeInput(itemStack);
      var recipes = serverWorld.recipeAccess().getSynchronizedRecipes();

      if (recipes.getFirstMatch(RegRecipes.SUPREME_COOKING_TYPE, input, serverWorld).isPresent()) {
        return true;
      }

      return recipes
          .getFirstMatch(RegRecipes.UNIVERSE_COOKING_TYPE, input, serverWorld)
          .isPresent();
    } else {
      return false;
    }
  }

}
