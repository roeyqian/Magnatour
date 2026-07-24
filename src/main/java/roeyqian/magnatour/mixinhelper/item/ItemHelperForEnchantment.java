/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.item;

// Minecraft
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.registry.content.UniverseItems;

public final class ItemHelperForEnchantment {

  private ItemHelperForEnchantment() {}

  public static void handleEnchant(
      ItemStack stack,
      CallbackInfo ci
  ) {
    if (isUniverseEnchantBlocked(stack)) {
      ci.cancel();
    }
  }

  public static <T> void handleSet(
      ItemStack stack,
      DataComponentType<T> componentType,
      T value,
      CallbackInfoReturnable<T> cir
  ) {
    if (!shouldBlockEnchantComponentWrite(stack, componentType, value)) {
      return;
    }

    cir.setReturnValue(stack.get(componentType));
  }

  public static <T> void handleSetTyped(
      ItemStack stack,
      TypedDataComponent<T> component,
      CallbackInfoReturnable<T> cir
  ) {
    DataComponentType<T> componentType = component.type();

    if (!shouldBlockEnchantComponentWrite(stack, componentType, component.value())) {
      return;
    }

    cir.setReturnValue(stack.get(componentType));
  }

  public static boolean hasAnyEnchantments(
      ItemStack stack
  ) {
    return !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
  }

  public static boolean isUniverseEnchantBlocked(
      ItemStack stack
  ) {
    return stack.is(UniverseItems.UNIVERSE_HELMET)
        || stack.is(UniverseItems.UNIVERSE_CHESTPLATE)
        || stack.is(UniverseItems.UNIVERSE_LEGGINGS)
        || stack.is(UniverseItems.UNIVERSE_BOOTS)
        || stack.is(UniverseItems.UNIVERSE_ULTIMA_SWORD)
        || stack.is(UniverseItems.UNIVERSE_OMNI_BLADE);
  }

  private static <T> boolean shouldBlockEnchantComponentWrite(
      ItemStack stack,
      DataComponentType<T> componentType,
      T value
  ) {
    if (!isUniverseEnchantBlocked(stack)) {
      return false;
    }
    if (componentType != DataComponents.ENCHANTMENTS
        && componentType != DataComponents.STORED_ENCHANTMENTS) {
      return false;
    }
    return value instanceof ItemEnchantments enchantments && !enchantments.isEmpty();
  }

}
