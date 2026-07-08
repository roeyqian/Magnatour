/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.menu;

// Minecraft
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixin.menu.AnvilMenuAccessor;
import roeyqian.magnatour.mixin.menu.ItemCombinerMenuAccessor;
import roeyqian.magnatour.utility.mixin.item.ItemHelperForEnchantment;

public final class MenuHelperForEnchantment {

  private MenuHelperForEnchantment() {}

  public static void handleCreateResult(
      AnvilMenu menu,
      CallbackInfo ci
  ) {
    ItemCombinerMenuAccessor combinerAccessor = (ItemCombinerMenuAccessor) menu;
    ItemStack baseStack = combinerAccessor.getInputSlots().getItem(0);
    ItemStack additionStack = combinerAccessor.getInputSlots().getItem(1);

    if (!ItemHelperForEnchantment.isUniverseEnchantBlocked(baseStack)) {
      return;
    }
    if (!ItemHelperForEnchantment.hasAnyEnchantments(baseStack)
        && !ItemHelperForEnchantment.hasAnyEnchantments(additionStack)) {
      return;
    }

    combinerAccessor.getResultSlots().setItem(0, ItemStack.EMPTY);
    ((AnvilMenuAccessor) menu).getCost().set(0);
    menu.broadcastChanges();
    ci.cancel();
  }

}
