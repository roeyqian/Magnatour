/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.logic.MagnaComponents;

public interface MagnaItemSettings {

  ToolMaterial SUPREME_TOOL = new ToolMaterial(
      BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
      100, 100, 100, 10,
      createRepairItemsTag("supreme_core")
  );
  ToolMaterial UNIVERSE_TOOL = new ToolMaterial(
      BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
      3600000, 1000, 1000, 3600000,
      createRepairItemsTag("universe_core")
  );

  TagKey<DamageType> SUPREME_IMMORTAL_DAMAGE_TYPES = TagKey.create(
      Registries.DAMAGE_TYPE,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_immortal")
  );
  TagKey<DamageType> UNIVERSE_IMMORTAL_DAMAGE_TYPES = TagKey.create(
      Registries.DAMAGE_TYPE,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_immortal")
  );

  ResourceKey<Enchantment> ETERNITY = ResourceKey.create(
      Registries.ENCHANTMENT,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "eternity")
  );
  ResourceKey<Enchantment> FINALITY = ResourceKey.create(
      Registries.ENCHANTMENT,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "finality")
  );
  ResourceKey<Enchantment> OMNISCIENCE = ResourceKey.create(
      Registries.ENCHANTMENT,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "omniscience")
  );
  ResourceKey<Enchantment> REVELATION = ResourceKey.create(
      Registries.ENCHANTMENT,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "revelation")
  );

  static Item.Properties applyEternity(
      Item.Properties settings,
      int level
  ) {
    return settings.delayedComponent(DataComponents.ENCHANTMENTS, registries -> {
      ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
      enchantments.set(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ETERNITY), level);
      return enchantments.toImmutable();
    });
  }

  static Item.Properties applyFinality(
      Item.Properties settings,
      int level
  ) {
    return settings.delayedComponent(DataComponents.ENCHANTMENTS, registries -> {
      ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
      enchantments.set(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FINALITY), level);
      return enchantments.toImmutable();
    });
  }

  static Item.Properties applyOmniscience(
      Item.Properties settings,
      int level
  ) {
    return settings.delayedComponent(DataComponents.ENCHANTMENTS, registries -> {
      ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
      enchantments.set(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(OMNISCIENCE), level);
      return enchantments.toImmutable();
    });
  }

  static Item.Properties applyRevelation(
      Item.Properties settings,
      int level
  ) {
    return settings.delayedComponent(DataComponents.ENCHANTMENTS, registries -> {
      ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
      enchantments.set(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(REVELATION), level);
      return enchantments.toImmutable();
    });
  }

  static Item.Properties applySupremeDefaults(
      Item.Properties settings
  ) {
    return settings
        .rarity(Rarity.RARE)
        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
        .component(MagnaComponents.SUPREME_GLINT_OVERRIDE, true)
        .delayedComponent(
            DataComponents.DAMAGE_RESISTANT,
            provider -> new DamageResistant(provider.lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(SUPREME_IMMORTAL_DAMAGE_TYPES)
            )
        );
  }

  static Item.Properties applyUniverseDefaults(
      Item.Properties settings
  ) {
    return settings
        .rarity(Rarity.EPIC)
        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
        .component(MagnaComponents.UNIVERSE_GLINT_OVERRIDE, true)
        .delayedComponent(
            DataComponents.DAMAGE_RESISTANT,
            provider -> new DamageResistant(provider.lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(UNIVERSE_IMMORTAL_DAMAGE_TYPES)
            )
        );
  }

  static ItemLore supremeLore(
      String itemId
  ) {
    return new ItemLore(List.of(
        Component.translatable("lore.magnatour." + itemId).withStyle(ChatFormatting.DARK_GREEN)
    ));
  }

  static ItemLore universeLore(
      String itemId
  ) {
    return new ItemLore(List.of(
        Component.translatable("lore.magnatour." + itemId).withStyle(ChatFormatting.DARK_RED)
    ));
  }

  private static TagKey<Item> createRepairItemsTag(
      String itemId
  ) {
    return TagKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, itemId)
    );
  }

}
