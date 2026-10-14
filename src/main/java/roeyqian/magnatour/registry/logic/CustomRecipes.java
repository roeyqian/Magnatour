/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
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
import roeyqian.magnatour.level.recipe.SupremeShapedCraftingRecipe;
import roeyqian.magnatour.level.recipe.SupremeShapelessCraftingRecipe;
import roeyqian.magnatour.level.recipe.SupremeSmeltingRecipe;
import roeyqian.magnatour.level.recipe.UniverseShapedCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseShapelessCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseSmeltingRecipe;
import roeyqian.magnatour.registry.LogicRegHelper;

/*
 * Supreme Group: Crafting, Smelting
 * Universe Group: Crafting, Smelting
 */
public final class CustomRecipes {

  // Supreme Group: Smelting
  public static final RecipeBookCategory SUPREME_SMELTING =
      LogicRegHelper.registerRecipeBookCategory("supreme_smelting");
  // Supreme Group: Crafting
  public static final RecipeBookCategory SUPREME_CRAFTING =
      LogicRegHelper.registerRecipeBookCategory("supreme_crafting");
  // Universe Group: Smelting
  public static final RecipeBookCategory UNIVERSE_SMELTING =
      LogicRegHelper.registerRecipeBookCategory("universe_smelting");
  // Universe Group: Crafting
  public static final RecipeBookCategory UNIVERSE_CRAFTING =
      LogicRegHelper.registerRecipeBookCategory("universe_crafting");

  public static final RecipeType<CraftingRecipe> SUPREME_CRAFTING_TYPE =
      LogicRegHelper.registerRecipeType("supreme_crafting");
  public static final RecipeType<CraftingRecipe> UNIVERSE_CRAFTING_TYPE =
      LogicRegHelper.registerRecipeType("universe_crafting");

  public static final RecipeType<SupremeSmeltingRecipe> SUPREME_SMELTING_TYPE =
      LogicRegHelper.registerRecipeType("supreme_smelting");

  public static final RecipeType<UniverseSmeltingRecipe> UNIVERSE_SMELTING_TYPE =
      LogicRegHelper.registerRecipeType("universe_smelting");

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeSmeltingRecipe> SUPREME_SMELTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("supreme_smelting", SupremeSmeltingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeShapedCraftingRecipe> SUPREME_SHAPED_CRAFTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("supreme_crafting_shaped", SupremeShapedCraftingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<SupremeShapelessCraftingRecipe> SUPREME_SHAPELESS_CRAFTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("supreme_crafting_shapeless", SupremeShapelessCraftingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseSmeltingRecipe> UNIVERSE_SMELTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("universe_smelting", UniverseSmeltingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseShapedCraftingRecipe> UNIVERSE_SHAPED_CRAFTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("universe_crafting_shaped", UniverseShapedCraftingRecipe.SERIALIZER);

  @SuppressWarnings("unused") // loaded for registry side effect
  public static final RecipeSerializer<UniverseShapelessCraftingRecipe> UNIVERSE_SHAPELESS_CRAFTING_SERIALIZER =
      LogicRegHelper.registerRecipeSerializer("universe_crafting_shapeless", UniverseShapelessCraftingRecipe.SERIALIZER);

  private CustomRecipes() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'CustomRecipes'");
  }

}
