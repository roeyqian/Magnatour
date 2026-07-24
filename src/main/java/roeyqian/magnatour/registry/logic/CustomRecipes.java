/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Minecraft
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.recipe.SupremeCookingRecipe;
import roeyqian.magnatour.level.recipe.SupremeCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseCookingRecipe;
import roeyqian.magnatour.registry.LogicRegHelper;

/*
 * Supreme Group: Crafting, Cooking
 * Universe Group: Crafting, Cooking
 */
public final class CustomRecipes {

  // Supreme Group: Cooking
  public static final RecipeBookCategory SUPREME_COOKING =
      LogicRegHelper.registerRecipeBookCategory("supreme_cooking");
  // Supreme Group: Crafting
  public static final RecipeBookCategory SUPREME_CRAFTING =
      LogicRegHelper.registerRecipeBookCategory("supreme_crafting");
  // Universe Group: Cooking
  public static final RecipeBookCategory UNIVERSE_COOKING =
      LogicRegHelper.registerRecipeBookCategory("universe_cooking");
  // Universe Group: Crafting
  public static final RecipeBookCategory UNIVERSE_CRAFTING =
      LogicRegHelper.registerRecipeBookCategory("universe_crafting");

  public static final RecipeType<CraftingRecipe> SUPREME_CRAFTING_TYPE =
      LogicRegHelper.registerRecipeType("supreme_crafting");
  public static final RecipeType<CraftingRecipe> UNIVERSE_CRAFTING_TYPE =
      LogicRegHelper.registerRecipeType("universe_crafting");

  public static final RecipeType<SupremeCookingRecipe> SUPREME_COOKING_TYPE =
      LogicRegHelper.registerRecipeType("supreme_cooking");

  public static final RecipeType<UniverseCookingRecipe> UNIVERSE_COOKING_TYPE =
      LogicRegHelper.registerRecipeType("universe_cooking");

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeCookingRecipe> SUPREME_COOKING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("supreme_cooking", SupremeCookingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeCraftingRecipe> SUPREME_CRAFTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("supreme_crafting", SupremeCraftingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseCookingRecipe> UNIVERSE_COOKING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("universe_cooking", UniverseCookingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseCraftingRecipe> UNIVERSE_CRAFTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("universe_crafting", UniverseCraftingRecipe.SERIALIZER);

  private CustomRecipes() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'CustomRecipes'");
  }

}
