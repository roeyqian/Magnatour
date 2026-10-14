/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
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

public class SupremeSmeltingRecipe extends AbstractCookingRecipe {

  public static final RecipeSerializer<SupremeSmeltingRecipe> SERIALIZER =
      new RecipeSerializer<>(
          AbstractCookingRecipe.cookingMapCodec(SupremeSmeltingRecipe::new),
          AbstractCookingRecipe.cookingStreamCodec(SupremeSmeltingRecipe::new)
      );

  public SupremeSmeltingRecipe(
      Recipe.CommonInfo commonInfo,
      AbstractCookingRecipe.CookingBookInfo bookInfo,
      Ingredient ingredient,
      ItemStackTemplate result,
      float experience,
      int smeltingTime
  ) {
    super(commonInfo, bookInfo, ingredient, result, experience, smeltingTime);
  }

  @Override @NonNull
  public RecipeSerializer<SupremeSmeltingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override @NonNull
  public RecipeType<? extends SupremeSmeltingRecipe> getType() {
    return CustomRecipes.SUPREME_SMELTING_TYPE;
  }

  @Override @NonNull
  public RecipeBookCategory recipeBookCategory() {
    return CustomRecipes.SUPREME_SMELTING;
  }

  @Override @NonNull
  protected Item furnaceIcon() {
    return SupremeBlocks.SUPREME_FURNACE.asItem();
  }

}
