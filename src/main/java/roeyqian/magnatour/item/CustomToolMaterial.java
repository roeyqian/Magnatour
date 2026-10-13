/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface CustomToolMaterial {

  ToolMaterial SUPREME_TOOL = new ToolMaterial(
      BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
      100, 100, 100, 10,
      createRepairItemsTag("supreme_core")
  );
  ToolMaterial UNIVERSE_TOOL = new ToolMaterial(
      BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
      3600000, 1000, 1000, 3600000,
      createRepairItemsTag("universe_star")
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

  private static TagKey<Item> createRepairItemsTag(
      String itemId
  ) {
    return TagKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, itemId)
    );
  }

}
