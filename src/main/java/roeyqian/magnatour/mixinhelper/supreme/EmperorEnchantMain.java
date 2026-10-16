/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.supreme;

// Minecraft
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.effects.AddValue;

// Magnatour
import roeyqian.magnatour.registry.stuff.MagnaItems;

// Other
// Apache Commons Lang
import org.apache.commons.lang3.mutable.MutableFloat;

public final class EmperorEnchantMain {

  private EmperorEnchantMain() {}

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

  public static boolean isEmperorTool(
      ItemStack stack
  ) {
    return stack.is(MagnaItems.EMPEROR_SWORD)
        || stack.is(MagnaItems.EMPEROR_AXE_PICKAXE)
        || stack.is(MagnaItems.EMPEROR_SHOVEL)
        || stack.is(MagnaItems.EMPEROR_HOE);
  }

  public static boolean supportsAxeOrPickaxe(
      HolderSet<Item> items
  ) {
    return items.stream().anyMatch(item -> item.is(ItemTags.AXES) || item.is(ItemTags.PICKAXES));
  }

}
