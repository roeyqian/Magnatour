/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.recipe;

// Minecraft
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;

public final class CraftingResultHelper {

  private CraftingResultHelper() {}

  public static ItemStack createResultStack(
      ItemStackTemplate resultTemplate,
      CraftingInput input
  ) {
    ItemStack resultStack = resultTemplate.create();
    int baseResultCount = resultStack.getCount();
    if (baseResultCount <= 0) return resultStack;

    int craftsShown = getCraftsShown(input, resultStack);
    resultStack.setCount(baseResultCount * craftsShown);
    return resultStack;
  }

  public static int getConsumedCraftCount(
      int removedItemCount,
      int baseResultCount
  ) {
    if (baseResultCount <= 0) return 0;

    int effectiveRemovedCount = Math.max(removedItemCount, baseResultCount);
    return Math.max(1, (effectiveRemovedCount + baseResultCount - 1) / baseResultCount);
  }

  private static int getCraftsShown(
      CraftingInput input,
      ItemStack resultStack
  ) {
    int baseResultCount = resultStack.getCount();
    if (baseResultCount <= 0) return 0;

    int maxCraftsByStackSize = resultStack.getMaxStackSize() / baseResultCount;
    if (maxCraftsByStackSize <= 0) return 1;

    return Math.min(getCraftableCount(input), maxCraftsByStackSize);
  }

  private static int getCraftableCount(
      CraftingInput input
  ) {
    int craftableCount = Integer.MAX_VALUE;
    boolean foundIngredient = false;

    for (ItemStack inputStack : input.items()) {
      if (inputStack.isEmpty()) continue;

      craftableCount = Math.min(craftableCount, inputStack.getCount());
      foundIngredient = true;
    }

    return foundIngredient ? craftableCount : 1;
  }

}
