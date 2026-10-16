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
import roeyqian.magnatour.registry.logic.MagnaRecipes;
import roeyqian.magnatour.registry.stuff.MagnaBlocks;

public class UniverseSmeltingRecipe extends AbstractCookingRecipe {

  public static final RecipeSerializer<UniverseSmeltingRecipe> SERIALIZER =
      new RecipeSerializer<>(
          AbstractCookingRecipe.cookingMapCodec(UniverseSmeltingRecipe::new),
          AbstractCookingRecipe.cookingStreamCodec(UniverseSmeltingRecipe::new)
      );

  public UniverseSmeltingRecipe(
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
  public RecipeSerializer<UniverseSmeltingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override @NonNull
  public RecipeType<? extends UniverseSmeltingRecipe> getType() {
    return MagnaRecipes.UNIVERSE_SMELTING_TYPE;
  }

  @Override @NonNull
  public RecipeBookCategory recipeBookCategory() {
    return MagnaRecipes.UNIVERSE_SMELTING;
  }

  @Override @NonNull
  protected Item furnaceIcon() {
    return MagnaBlocks.UNIVERSE_REFINERY.asItem();
  }

}
