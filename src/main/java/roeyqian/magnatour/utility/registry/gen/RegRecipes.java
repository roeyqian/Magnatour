/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.gen;

// Minecraft
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.gen.recipe.SupremeCookingRecipe;
import roeyqian.magnatour.gen.recipe.SupremeCraftingRecipe;
import roeyqian.magnatour.gen.recipe.UniverseCraftingRecipe;
import roeyqian.magnatour.gen.recipe.UniverseCookingRecipe;

/*
 * Supreme Group: Crafting, Cooking
 * Universe Group: Crafting, Cooking
 */
public final class RegRecipes {

  // Supreme Group: Crafting
  public static final RecipeBookCategory SUPREME_CRAFTING =
      GenRegHelper.registerRecipeBookCategory("supreme_crafting");
  public static final RecipeType<CraftingRecipe> SUPREME_CRAFTING_TYPE =
      GenRegHelper.registerRecipeType("supreme_crafting");
  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeCraftingRecipe> SUPREME_CRAFTING_SERIALIZER =
      GenRegHelper.registerRecipeSerializer("supreme_crafting", SupremeCraftingRecipe.SERIALIZER);

  // Supreme Group: Cooking
  public static final RecipeBookCategory SUPREME_COOKING =
      GenRegHelper.registerRecipeBookCategory("supreme_cooking");
  public static final RecipeType<SupremeCookingRecipe> SUPREME_COOKING_TYPE =
      GenRegHelper.registerRecipeType("supreme_cooking");
  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeCookingRecipe> SUPREME_COOKING_SERIALIZER =
      GenRegHelper.registerRecipeSerializer("supreme_cooking", SupremeCookingRecipe.SERIALIZER);

  // Universe Group: Crafting
  public static final RecipeBookCategory UNIVERSE_CRAFTING =
      GenRegHelper.registerRecipeBookCategory("universe_crafting");
  public static final RecipeType<CraftingRecipe> UNIVERSE_CRAFTING_TYPE =
      GenRegHelper.registerRecipeType("universe_crafting");
  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseCraftingRecipe> UNIVERSE_CRAFTING_SERIALIZER =
      GenRegHelper.registerRecipeSerializer("universe_crafting", UniverseCraftingRecipe.SERIALIZER);

  // Universe Group: Cooking
  public static final RecipeBookCategory UNIVERSE_COOKING =
      GenRegHelper.registerRecipeBookCategory("universe_cooking");
  public static final RecipeType<UniverseCookingRecipe> UNIVERSE_COOKING_TYPE =
      GenRegHelper.registerRecipeType("universe_cooking");
  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseCookingRecipe> UNIVERSE_COOKING_SERIALIZER =
      GenRegHelper.registerRecipeSerializer("universe_cooking", UniverseCookingRecipe.SERIALIZER);

  private RegRecipes() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegRecipes'");
  }

}
