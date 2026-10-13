/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.item;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.AddValue;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.registry.content.SupremeItems;
import roeyqian.magnatour.registry.content.UniverseItems;

// Other
// Apache Commons Lang
import org.apache.commons.lang3.mutable.MutableFloat;

public final class ItemHelperForEnchantment {

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

  private ItemHelperForEnchantment() {}

  public static double amplifyEmperorMiningEfficiency(
      ItemStack stack,
      double miningEfficiency
  ) {
    if (!isEmperorTool(stack)) return miningEfficiency;
    for (var entry : stack.getEnchantments().entrySet()) {
      if (!entry.getKey().is(Enchantments.EFFICIENCY)) continue;
      for (var effect : entry.getKey().value().getEffects(EnchantmentEffectComponents.ATTRIBUTES)) {
        if (effect.attribute().equals(Attributes.MINING_EFFICIENCY)
            && effect.operation() == AttributeModifier.Operation.ADD_VALUE) {
          // Vanilla already includes one copy of the bonus; add the remaining nine.
          miningEfficiency += effect.amount().calculate(entry.getIntValue()) * 9.0;
        }
      }
    }
    return miningEfficiency;
  }

  public static void applyEmperorDamageEnchantments(
      Enchantment enchantment,
      ServerLevel world,
      int level,
      Entity target,
      DamageSource source,
      MutableFloat damage
  ) {
    // Keep vanilla target conditions, effect order and random source.
    Enchantment.applyEffects(
        enchantment.getEffects(EnchantmentEffectComponents.DAMAGE),
        Enchantment.damageContext(world, level, target, source),
        damage,
        (effect, currentDamage) -> {
          if (effect instanceof AddValue addition) {
            return currentDamage + addition.value().calculate(level) * 10.0F;
          }
          return effect.process(level, target.getRandom(), currentDamage);
        }
    );
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

  public static boolean isDivineEnchantment(
      Holder<Enchantment> enchantment
  ) {
    return enchantment.is(CustomToolMaterial.ETERNITY)
        || enchantment.is(CustomToolMaterial.FINALITY)
        || enchantment.is(CustomToolMaterial.OMNISCIENCE);
  }

  public static boolean isEmperorTool(
      ItemStack stack
  ) {
    return stack.is(SupremeItems.EMPEROR_SWORD)
        || stack.is(SupremeItems.EMPEROR_AXE_PICKAXE)
        || stack.is(SupremeItems.EMPEROR_SHOVEL)
        || stack.is(SupremeItems.EMPEROR_HOE);
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
    return stack.is(UniverseItems.UNIVERSE_HELMET)
        || stack.is(UniverseItems.UNIVERSE_CHESTPLATE)
        || stack.is(UniverseItems.UNIVERSE_LEGGINGS)
        || stack.is(UniverseItems.UNIVERSE_BOOTS)
        || stack.is(UniverseItems.UNIVERSE_ULTIMA_SWORD)
        || stack.is(UniverseItems.UNIVERSE_OMNI_BLADE);
  }

  public static boolean isUnrestrictedDivineEnchantment(
      Enchantment enchantment
  ) {
    return enchantment.definition().supportedItems().unwrapKey()
        .filter(tag -> tag.equals(ETERNITY_ITEMS)
            || tag.equals(FINALITY_ITEMS) || tag.equals(OMNISCIENCE_ITEMS)).isPresent();
  }

  public static boolean supportsAxeOrPickaxe(
      HolderSet<Item> items
  ) {
    return items.stream().anyMatch(item -> item.is(ItemTags.AXES) || item.is(ItemTags.PICKAXES));
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
