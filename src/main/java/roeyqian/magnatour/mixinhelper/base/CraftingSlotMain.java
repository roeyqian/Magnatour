/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.base;

// Minecraft
import net.minecraft.core.NonNullList;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.level.recipe.CraftingResultHelper;
import roeyqian.magnatour.level.recipe.SupremeShapedCraftingRecipe;
import roeyqian.magnatour.level.recipe.SupremeShapelessCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseShapedCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseShapelessCraftingRecipe;
import roeyqian.magnatour.registry.logic.MagnaRecipes;

public final class CraftingSlotMain {

  private CraftingSlotMain() {}

  public static void handleOnTake(
      Player player,
      CraftingContainer craftSlots,
      ItemStack stack,
      int removeCount,
      AchievementConsumer achievementConsumer,
      CallbackInfo ci
  ) {
    RecipeHolder<? extends CraftingRecipe> recipeHolder = getActiveCustomRecipe(player, craftSlots);
    if (recipeHolder == null) return;

    CraftingRecipe recipe = recipeHolder.value();
    int baseResultCount = getBaseResultCount(recipe);
    int removedItemCount = removeCount > 0 ? removeCount : Math.max(stack.getCount(), baseResultCount);
    int craftsToConsume = CraftingResultHelper.getConsumedCraftCount(
        removedItemCount,
        baseResultCount
    );
    ItemStack achievementStack = stack.isEmpty()
        ? recipe.assemble(craftSlots.asCraftInput()).copyWithCount(baseResultCount)
        : stack;

    achievementConsumer.accept(achievementStack);

    for (int i = 0; i < craftsToConsume; i++) {
      consumeSingleCraft(player, craftSlots, recipe);
    }

    ci.cancel();
  }

  public static <T> boolean handlePlaceRecipe(
      int width, int height,
      Recipe<?> recipe,
      Iterable<T> slots,
      PlaceRecipeHelper.Output<T> filler
  ) {
    if (recipe instanceof SupremeShapedCraftingRecipe supremeRecipe) {
      PlaceRecipeHelper.placeRecipe(
          width, height,
          supremeRecipe.getWidth(), supremeRecipe.getHeight(),
          slots, filler
      );
      return true;
    }

    if (recipe instanceof UniverseShapedCraftingRecipe universeRecipe) {
      PlaceRecipeHelper.placeRecipe(
          width, height,
          universeRecipe.getWidth(), universeRecipe.getHeight(),
          slots, filler
      );
      return true;
    }

    return false;
  }

  public static int handleQuickCraft(
      Player player,
      CraftingContainer craftSlots,
      int removeCount, int amount,
      CallbackInfo ci
  ) {
    if (!shouldHandleCustomRecipe(player, craftSlots)) {
      return removeCount;
    }

    ci.cancel();
    return removeCount + amount;
  }

  public static boolean shouldHandleCustomRecipe(
      Player player,
      CraftingContainer craftSlots
  ) {
    return getActiveCustomRecipe(player, craftSlots) != null;
  }

  private static int getBaseResultCount(
      CraftingRecipe recipe
  ) {
    if (recipe instanceof SupremeShapedCraftingRecipe supremeRecipe) {
      return supremeRecipe.getBaseResultCount();
    }
    if (recipe instanceof UniverseShapedCraftingRecipe universeRecipe) {
      return universeRecipe.getBaseResultCount();
    }

    if (recipe instanceof SupremeShapelessCraftingRecipe supremeRecipe) {
      return supremeRecipe.getBaseResultCount();
    }
    if (recipe instanceof UniverseShapelessCraftingRecipe universeRecipe) {
      return universeRecipe.getBaseResultCount();
    }

    return 1;
  }

  private static RecipeHolder<? extends CraftingRecipe> getActiveCustomRecipe(
      Player player,
      CraftingContainer craftSlots
  ) {
    if (player.level().isClientSide()) return null;

    Level world = player.level();
    CraftingInput recipeInput = craftSlots.asCraftInput();
    var recipeManager = world.recipeAccess().getSynchronizedRecipes();

    if (recipeManager.getFirstMatch(RecipeType.CRAFTING, recipeInput, world).isPresent()) {
      return null;
    }

    var supremeMatch = recipeManager.getFirstMatch(MagnaRecipes.SUPREME_CRAFTING_TYPE, recipeInput, world);
    if (supremeMatch.isPresent()) return supremeMatch.get();

    var universeMatch = recipeManager.getFirstMatch(MagnaRecipes.UNIVERSE_CRAFTING_TYPE, recipeInput, world);
    return universeMatch.orElse(null);
  }

  private static void consumeSingleCraft(
      Player player,
      CraftingContainer craftSlots,
      CraftingRecipe recipe
  ) {
    CraftingInput.Positioned positionedInput = craftSlots.asPositionedCraftInput();
    CraftingInput currentInput = positionedInput.input();
    NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(currentInput);
    int left = positionedInput.left();
    int top = positionedInput.top();

    for (int y = 0; y < currentInput.height(); y++) {
      for (int x = 0; x < currentInput.width(); x++) {
        int slotIndex = x + left + (y + top) * craftSlots.getWidth();
        ItemStack inputStack = craftSlots.getItem(slotIndex);
        ItemStack remainingStack = remainingItems.get(x + y * currentInput.width());

        if (!inputStack.isEmpty()) {
          craftSlots.removeItem(slotIndex, 1);
          inputStack = craftSlots.getItem(slotIndex);
        }

        if (remainingStack.isEmpty()) continue;

        if (inputStack.isEmpty()) {
          craftSlots.setItem(slotIndex, remainingStack);
        } else if (ItemStack.isSameItemSameComponents(inputStack, remainingStack)) {
          remainingStack.grow(inputStack.getCount());
          craftSlots.setItem(slotIndex, remainingStack);
        } else if (!player.getInventory().add(remainingStack)) {
          player.drop(remainingStack, false, Prediction.SERVER_ONLY);
        }
      }
    }
  }

  @FunctionalInterface
  public interface AchievementConsumer {

    void accept(
        ItemStack stack
    );

  }

}
