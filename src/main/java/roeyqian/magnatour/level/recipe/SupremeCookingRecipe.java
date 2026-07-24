/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.recipe;

// Minecraft
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.logic.CustomRecipes;

public class SupremeCookingRecipe extends AbstractCookingRecipe {

  public static final RecipeSerializer<SupremeCookingRecipe> SERIALIZER =
      new RecipeSerializer<>(
          AbstractCookingRecipe.cookingMapCodec(SupremeCookingRecipe::new, 960),
          AbstractCookingRecipe.cookingStreamCodec(SupremeCookingRecipe::new)
      );

  public SupremeCookingRecipe(
      Recipe.CommonInfo commonInfo,
      AbstractCookingRecipe.CookingBookInfo bookInfo,
      Ingredient ingredient,
      ItemStackTemplate result,
      float experience,
      int cookingTime
  ) {
    super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
  }

  @Override @NonNull
  public RecipeSerializer<SupremeCookingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override @NonNull
  public RecipeType<? extends SupremeCookingRecipe> getType() {
    return CustomRecipes.SUPREME_COOKING_TYPE;
  }

  @Override @NonNull
  public RecipeBookCategory recipeBookCategory() {
    return CustomRecipes.SUPREME_COOKING;
  }

  @Override @NonNull
  protected Item furnaceIcon() {
    return SupremeBlocks.SUPREME_FURNACE.asItem();
  }

}
