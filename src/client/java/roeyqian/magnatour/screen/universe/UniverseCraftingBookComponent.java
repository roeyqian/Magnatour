/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.universe;

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
import net.minecraft.world.item.crafting.display.RecipeDisplay;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.mixin.screen.GhostSlotsInvoker;
import roeyqian.magnatour.registry.content.SupremeItems;
import roeyqian.magnatour.registry.content.UniverseItems;
import roeyqian.magnatour.registry.logic.CustomRecipes;
import roeyqian.magnatour.menu.universe.UniverseWorkstationMenu;
import roeyqian.magnatour.screen.CustomRecipeDisplay;

public final class UniverseCraftingBookComponent extends RecipeBookComponent<UniverseWorkstationMenu> {

  private static final Component TOGGLE_CRAFTABLE_TEXT =
      Component.translatable("gui.recipebook.toggleRecipes.craftable");

  private static final WidgetSprites FILTER_BUTTON_TEXTURES = new WidgetSprites(
      Identifier.withDefaultNamespace("recipe_book/filter_enabled"),
      Identifier.withDefaultNamespace("recipe_book/filter_disabled"),
      Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
      Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
  );

  private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
      new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING),
      new RecipeBookComponent.TabInfo(Items.IRON_AXE, Items.GOLDEN_SWORD, RecipeBookCategories.CRAFTING_EQUIPMENT),
      new RecipeBookComponent.TabInfo(Items.BRICKS, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS),
      new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.APPLE, RecipeBookCategories.CRAFTING_MISC),
      new RecipeBookComponent.TabInfo(Items.REDSTONE, RecipeBookCategories.CRAFTING_REDSTONE),
      new RecipeBookComponent.TabInfo(SupremeItems.SUPREME_CORE, CustomRecipes.SUPREME_CRAFTING),
      new RecipeBookComponent.TabInfo(UniverseItems.UNIVERSE_STAR, CustomRecipes.UNIVERSE_CRAFTING)
  );

  public UniverseCraftingBookComponent(
      UniverseWorkstationMenu screenHandler
  ) {
    super(screenHandler, TABS);
  }

  @Override
  protected void fillGhostRecipe(
      @NonNull GhostSlots ghostSlots,
      RecipeDisplay display,
      @NonNull ContextMap context
  ) {
    GhostSlotsInvoker invoker = (GhostSlotsInvoker) ghostSlots;
    invoker.invokeSetResult(this.menu.getResultSlot(), context, display.result());

    CustomRecipeDisplay.displayRecipe(
        display, context, invoker,
        this.menu.getInputGridSlots(),
        this.menu.getGridWidth(),
        this.menu.getGridHeight()
    );
  }

  @Override @NonNull
  protected WidgetSprites getFilterButtonTextures() {
    return FILTER_BUTTON_TEXTURES;
  }

  @Override @NonNull
  protected Component getRecipeFilterName() {
    return TOGGLE_CRAFTABLE_TEXT;
  }

  @Override
  protected boolean isCraftingSlot(
      @NonNull Slot slot
  ) {
    return this.menu.getResultSlot() == slot
        || this.menu.getInputGridSlots().contains(slot);
  }

  @Override
  protected void selectMatchingRecipes(
      RecipeCollection recipeCollection,
      @NonNull StackedItemContents stackedContents
  ) {
    int width = this.menu.getGridWidth();
    int height = this.menu.getGridHeight();
    recipeCollection.selectRecipes(
        stackedContents,
        display -> CustomRecipeDisplay.canDisplay(display, width, height)
    );
  }

}
