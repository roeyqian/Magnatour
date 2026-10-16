/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.item.universe.UniverseVoidBoots;
import roeyqian.magnatour.item.universe.UniverseVoidChestplate;
import roeyqian.magnatour.item.universe.UniverseVoidHelmet;
import roeyqian.magnatour.item.universe.UniverseVoidLeggings;
import roeyqian.magnatour.mixin.world.AnvilMenuAccessor;
import roeyqian.magnatour.mixin.world.ItemCombinerMenuAccessor;
import roeyqian.magnatour.registry.stuff.MagnaItems;

/** Shared Universe equipment enchanting rules for item components, enchanting tables and anvils. */
public final class UniverseEnchantMain {

  // These marker tags identify the dynamically unrestricted divine item sets.
  private static final TagKey<Item> ETERNITY_ITEMS = TagKey.create(
      Registries.ITEM,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "enchantable/eternity")
  );
  private static final TagKey<Item> FINALITY_ITEMS = TagKey.create(
      Registries.ITEM,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "enchantable/finality")
  );
  private static final TagKey<Item> OMNISCIENCE_ITEMS = TagKey.create(
      Registries.ITEM,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "enchantable/omniscience")
  );
  private static final TagKey<Item> REVELATION_ITEMS = TagKey.create(
      Registries.ITEM,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "enchantable/revelation")
  );

  private UniverseEnchantMain() {}

  public static void handleCreateResult(
      AnvilMenu menu,
      CallbackInfo ci
  ) {
    ItemCombinerMenuAccessor combinerAccessor = (ItemCombinerMenuAccessor) menu;
    ItemStack baseStack = combinerAccessor.getInputSlots().getItem(0);
    ItemStack additionStack = combinerAccessor.getInputSlots().getItem(1);

    // Existing divine enchantments may be retained, but never transferred or upgraded.
    if (!hasEternity(additionStack)
        && !hasFinality(additionStack)
        && !hasOmniscience(additionStack)
        && !hasRevelation(additionStack)) {
      if (!isUniverseEnchantBlocked(baseStack)) {
        return;
      }
      if (!hasNonDivineEnchantments(baseStack)
          && !hasNonDivineEnchantments(additionStack)) {
        return;
      }
    }

    combinerAccessor.getResultSlots().setItem(0, ItemStack.EMPTY);
    ((AnvilMenuAccessor) menu).getCost().set(0);
    menu.broadcastChanges();
    ci.cancel();
  }

  public static void handleEnchant(
      ItemStack stack,
      Holder<Enchantment> enchantment,
      CallbackInfo ci
  ) {
    if (isDivineEnchantment(enchantment)) {
      var componentType = EnchantmentHelper.getComponentType(stack);
      if (!stack.has(componentType)) {
        stack.set(componentType, ItemEnchantments.EMPTY);
      }
      return;
    }
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

  public static void handleSlotsChanged(
      Container inventory,
      CallbackInfo ci
  ) {
    ItemStack itemStack = inventory.getItem(0);

    if (itemStack.getItem() instanceof UniverseVoidHelmet
        || itemStack.getItem() instanceof UniverseVoidChestplate
        || itemStack.getItem() instanceof UniverseVoidLeggings
        || itemStack.getItem() instanceof UniverseVoidBoots
        || itemStack.getItem() instanceof UniverseUltimaSword
        || itemStack.getItem() instanceof UniverseOmniBlade) {
      ci.cancel();
    }
  }

  public static boolean hasAnyEnchantments(
      ItemStack stack
  ) {
    return !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
  }

  public static boolean hasEternity(
      ItemStack stack
  ) {
    return EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream()
        .anyMatch(enchantment -> enchantment.is(CustomToolMaterial.ETERNITY));
  }

  public static boolean hasFinality(
      ItemStack stack
  ) {
    return EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream()
        .anyMatch(enchantment -> enchantment.is(CustomToolMaterial.FINALITY));
  }

  public static boolean hasNonDivineEnchantments(
      ItemStack stack
  ) {
    return EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream()
        .anyMatch(enchantment -> !isDivineEnchantment(enchantment));
  }

  public static boolean hasOmniscience(
      ItemStack stack
  ) {
    return EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream()
        .anyMatch(enchantment -> enchantment.is(CustomToolMaterial.OMNISCIENCE));
  }

  public static boolean hasRevelation(
      ItemStack stack
  ) {
    return EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream()
        .anyMatch(enchantment -> enchantment.is(CustomToolMaterial.REVELATION));
  }

  public static boolean isDivineEnchantment(
      Holder<Enchantment> enchantment
  ) {
    return enchantment.is(CustomToolMaterial.ETERNITY)
        || enchantment.is(CustomToolMaterial.FINALITY)
        || enchantment.is(CustomToolMaterial.OMNISCIENCE)
        || enchantment.is(CustomToolMaterial.REVELATION);
  }

  public static boolean isFinality(
      Enchantment enchantment
  ) {
    return enchantment.definition().supportedItems().unwrapKey()
        .filter(FINALITY_ITEMS::equals).isPresent();
  }

  public static boolean isUniverseEnchantBlocked(
      ItemStack stack
  ) {
    return stack.is(MagnaItems.UNIVERSE_VOID_HELMET)
        || stack.is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)
        || stack.is(MagnaItems.UNIVERSE_VOID_LEGGINGS)
        || stack.is(MagnaItems.UNIVERSE_VOID_BOOTS)
        || stack.is(MagnaItems.UNIVERSE_ULTIMA_SWORD)
        || stack.is(MagnaItems.UNIVERSE_OMNI_BLADE);
  }

  public static boolean isUnrestrictedDivineEnchantment(
      Enchantment enchantment
  ) {
    return enchantment.definition().supportedItems().unwrapKey()
        .filter(tag -> tag.equals(ETERNITY_ITEMS)
            || tag.equals(FINALITY_ITEMS) || tag.equals(OMNISCIENCE_ITEMS)
            || tag.equals(REVELATION_ITEMS)).isPresent();
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
    return value instanceof ItemEnchantments enchantments && enchantments.keySet().stream()
        .anyMatch(enchantment -> !isDivineEnchantment(enchantment));
  }

}
