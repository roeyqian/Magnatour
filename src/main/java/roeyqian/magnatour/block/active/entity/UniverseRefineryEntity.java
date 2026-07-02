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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.gen.recipe.SupremeCookingRecipe;
import roeyqian.magnatour.gen.recipe.UniverseCookingRecipe;
import roeyqian.magnatour.menu.block.UniverseRefineryMenu;
import roeyqian.magnatour.mixin.block.AbstractFurnaceBlockEntityAccessor;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;
import roeyqian.magnatour.utility.registry.gen.RegRecipes;

public class UniverseRefineryEntity extends AbstractFurnaceBlockEntity {

  private static final int FUEL_EFFICIENCY_MULTIPLIER = 32;
  private static final int ITEMS_PER_TICK = 2;

  private final RecipeManager.CachedCheck<SingleRecipeInput, SupremeCookingRecipe> supremeMatchGetter;

  private final RecipeManager.CachedCheck<SingleRecipeInput, UniverseCookingRecipe> universeMatchGetter;

  public UniverseRefineryEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.UNIVERSE_REFINERY_ENTITY, pos, state, RecipeType.SMELTING);
    this.supremeMatchGetter = RecipeManager.createCheck(RegRecipes.SUPREME_COOKING_TYPE);
    this.universeMatchGetter = RecipeManager.createCheck(RegRecipes.UNIVERSE_COOKING_TYPE);
  }

  public static void tick(
      ServerLevel world,
      BlockPos pos,
      BlockState state,
      UniverseRefineryEntity blockEntity
  ) {
    AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) blockEntity;

    boolean wasLit = accessor.getLitTimeRemaining() > 0;
    boolean changed = false;

    if (accessor.getLitTimeRemaining() > 0) {
      accessor.setLitTimeRemaining(accessor.getLitTimeRemaining() - 1);
    }

    ItemStack fuelStack = blockEntity.items.get(1);
    ItemStack inputStack = blockEntity.items.get(0);
    boolean hasInput = !inputStack.isEmpty();
    boolean hasFuel = !fuelStack.isEmpty();

    if (accessor.getLitTimeRemaining() > 0 || (hasFuel && hasInput)) {
      SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
      RecipeHolder<? extends AbstractCookingRecipe> recipe = blockEntity.findRecipe(recipeInput, world);

      int maxCount = blockEntity.getMaxStackSize();

      boolean thisAcceptable = canBurn(
          recipe,
          recipeInput,
          blockEntity.items,
          maxCount
      );
      if (accessor.getLitTimeRemaining() <= 0 && thisAcceptable) {
        int fuelTime = blockEntity.getBurnDuration(world.fuelValues(), fuelStack);
        accessor.setLitTimeRemaining(fuelTime);
        accessor.setLitTotalTime(fuelTime);

        if (accessor.getLitTimeRemaining() > 0) {
          changed = true;
          if (hasFuel) {
            Item fuelItem = fuelStack.getItem();
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
              if (fuelItem.getCraftingRemainder() != null) {
                blockEntity.items.set(1, fuelItem.getCraftingRemainder().create());
              }
            }
          }
        }
      }

      if (accessor.getLitTimeRemaining() > 0) {
        for (int i = 0; i < ITEMS_PER_TICK; i++) {
          inputStack = blockEntity.items.getFirst();
          if (inputStack.isEmpty()) break;

          recipeInput = new SingleRecipeInput(inputStack);
          recipe = blockEntity.findRecipe(recipeInput, world);

          thisAcceptable = canBurn(
              recipe,
              recipeInput,
              blockEntity.items,
              maxCount
          );
          if (recipe != null && thisAcceptable) {
            if (burn(
                recipe,
                recipeInput,
                blockEntity.items,
                maxCount
            )) {
              blockEntity.setRecipeUsed(recipe);
              changed = true;
            }
          } else {
            break;
          }
        }

        accessor.setCookingTimeSpent(0);
        accessor.setCookingTotalTime(1);
      }
    } else if (accessor.getLitTimeRemaining() <= 0 && accessor.getCookingTimeSpent() > 0) {
      accessor.setCookingTimeSpent(0);
    }

    boolean isLit = accessor.getLitTimeRemaining() > 0;
    if (wasLit != isLit) {
      changed = true;
      state = state.setValue(AbstractFurnaceBlock.LIT, isLit);
      world.setBlock(pos, state, 3);
    }

    if (changed) {
      setChanged(world, pos, state);
    }
  }

  @Override @NonNull
  protected AbstractContainerMenu createMenu(
      int syncId,
      @NonNull Inventory playerInventory
  ) {
    if (this.level != null) {
      return new UniverseRefineryMenu(
          syncId,
          playerInventory,
          ContainerLevelAccess.create(this.level, this.worldPosition),
          this,
          this.dataAccess
      );
    } else {
      return new UniverseRefineryMenu(
          syncId,
          playerInventory,
          this,
          this.dataAccess
      );
    }
  }

  @Override
  protected int getBurnDuration(
      FuelValues fuelRegistry,
      @NonNull ItemStack stack
  ) {
    int baseFuelTime = fuelRegistry.burnDuration(stack);
    return baseFuelTime * FUEL_EFFICIENCY_MULTIPLIER;
  }

  @Override @NonNull
  protected Component getDefaultName() {
    return Component.translatable("block.magnatour.universe_refinery");
  }

  private static boolean canBurn(
      RecipeHolder<? extends AbstractCookingRecipe> recipe,
      SingleRecipeInput input,
      NonNullList<ItemStack> inventory,
      int maxCount
  ) {
    if (inventory.getFirst().isEmpty() || recipe == null) {
      return false;
    }

    ItemStack result = recipe.value().assemble(input);
    if (result.isEmpty()) {
      return false;
    }

    ItemStack outputStack = inventory.get(2);
    if (outputStack.isEmpty()) {
      return true;
    }
    if (!ItemStack.isSameItemSameComponents(outputStack, result)) {
      return false;
    }
    if (outputStack.getCount() < maxCount
        && outputStack.getCount() < outputStack.getMaxStackSize()
    ) return true;

    return outputStack.getCount() < result.getMaxStackSize();
  }

  private static boolean burn(
      RecipeHolder<? extends AbstractCookingRecipe> recipe,
      SingleRecipeInput input,
      NonNullList<ItemStack> inventory,
      int maxCount
  ) {
    if (recipe == null || !canBurn(recipe, input, inventory, maxCount)) {
      return false;
    }

    ItemStack inputStack = inventory.get(0);
    ItemStack result = recipe.value().assemble(input);
    ItemStack outputStack = inventory.get(2);

    if (outputStack.isEmpty()) {
      inventory.set(2, result.copy());
    } else if (ItemStack.isSameItemSameComponents(outputStack, result)) {
      outputStack.grow(1);
    }

    inputStack.shrink(1);
    return true;
  }

  private RecipeHolder<? extends AbstractCookingRecipe> findRecipe(
      SingleRecipeInput input,
      ServerLevel world
  ) {
    var supremeRecipe = this.supremeMatchGetter.getRecipeFor(input, world);
    if (supremeRecipe.isPresent()) return supremeRecipe.get();

    var universeRecipe = this.universeMatchGetter.getRecipeFor(input, world);
    if (universeRecipe.isPresent()) return universeRecipe.get();

    var vanillaRecipe = ((AbstractFurnaceBlockEntityAccessor) this)
        .getMatchGetter()
        .getRecipeFor(input, world);
    return vanillaRecipe.orElse(null);
  }

}
