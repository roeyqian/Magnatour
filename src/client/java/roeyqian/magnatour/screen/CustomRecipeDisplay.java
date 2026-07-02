/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;

// Magnatour
import roeyqian.magnatour.mixin.screen.GhostSlotsInvoker;

public interface CustomRecipeDisplay {

  static boolean canDisplay(
      RecipeDisplay display,
      int width,
      int height
  ) {
    return switch (display) {
      case ShapedCraftingRecipeDisplay shaped ->
          width >= shaped.width() && height >= shaped.height();
      case ShapelessCraftingRecipeDisplay shapeless ->
          width * height >= shapeless.ingredients().size();
      default -> false;
    };
  }

  static void displayRecipe(
      RecipeDisplay display,
      ContextMap context,
      GhostSlotsInvoker invoker,
      List<Slot> inputSlots,
      int width,
      int height
  ) {
    switch (display) {
      case ShapedCraftingRecipeDisplay shaped -> PlaceRecipeHelper.placeRecipe(
          width, height,
          shaped.width(), shaped.height(),
          shaped.ingredients(),
          (slotDisplay, index, _, _) -> {
            Slot slot = inputSlots.get(index);
            invoker.invokeSetInput(slot, context, slotDisplay);
          }
      );
      case ShapelessCraftingRecipeDisplay shapeless -> {
        int count = Math.min(shapeless.ingredients().size(), inputSlots.size());
        for (int i = 0; i < count; ++i) {
          invoker.invokeSetInput(inputSlots.get(i), context, shapeless.ingredients().get(i));
        }
      }
      default -> {
      }
    }
  }

}