/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.recipe;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.supreme.SupremeFurnaceMenu;
import roeyqian.magnatour.mixin.screen.GhostSlotsInvoker;
import roeyqian.magnatour.registry.logic.RegRecipes;
import roeyqian.magnatour.registry.content.RegConsumableItems;

public final class SupremeCookingBookComponent extends RecipeBookComponent<SupremeFurnaceMenu> {

  private static final Component TOGGLE_COOKABLE_TEXT = Component.translatable(
      "gui.recipebook.toggleRecipes.smeltable"
  );

  private static final WidgetSprites FILTER_BUTTON_TEXTURES = new WidgetSprites(
      Identifier.withDefaultNamespace("recipe_book/furnace_filter_enabled"),
      Identifier.withDefaultNamespace("recipe_book/furnace_filter_disabled"),
      Identifier.withDefaultNamespace("recipe_book/furnace_filter_enabled_highlighted"),
      Identifier.withDefaultNamespace("recipe_book/furnace_filter_disabled_highlighted")
  );

  private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
      new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FURNACE),
      new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.FURNACE_FOOD),
      new RecipeBookComponent.TabInfo(Items.STONE, RecipeBookCategories.FURNACE_BLOCKS),
      new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.EMERALD, RecipeBookCategories.FURNACE_MISC),
      new RecipeBookComponent.TabInfo(RegConsumableItems.SUPREME_CORE, RegRecipes.SUPREME_COOKING)
  );

  public SupremeCookingBookComponent(
      SupremeFurnaceMenu handler
  ) {
    super(handler, TABS);
  }

  @Override
  protected void fillGhostRecipe(
      @NonNull GhostSlots ghostSlots,
      @NonNull RecipeDisplay display,
      @NonNull ContextMap context
  ) {
    GhostSlotsInvoker invoker = (GhostSlotsInvoker) ghostSlots;

    if (display instanceof FurnaceRecipeDisplay furnace) {
      invoker.invokeSetInput(
          this.menu.getSlot(0),
          context,
          furnace.ingredient()
      );
      invoker.invokeSetResult(
          this.menu.getSlot(2),
          context,
          furnace.result()
      );
    }
  }

  @Override @NonNull
  protected WidgetSprites getFilterButtonTextures() {
    return FILTER_BUTTON_TEXTURES;
  }

  @Override @NonNull
  protected Component getRecipeFilterName() {
    return TOGGLE_COOKABLE_TEXT;
  }

  @Override
  protected boolean isCraftingSlot(
      @NonNull Slot slot
  ) {
    return slot == this.menu.getSlot(0);
  }

  @Override
  protected void selectMatchingRecipes(
      RecipeCollection recipeCollection,
      @NonNull StackedItemContents stackedContents
  ) {
    recipeCollection.selectRecipes(
        stackedContents,
        display -> display instanceof FurnaceRecipeDisplay
    );
  }

}
