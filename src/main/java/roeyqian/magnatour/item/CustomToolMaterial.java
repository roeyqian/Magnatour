/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

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
      3600000, 3600000, 3600000, 3600000,
      createRepairItemsTag("universe_star")
  );

  private static TagKey<Item> createRepairItemsTag(
      String itemId
  ) {
    return TagKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, itemId)
    );
  }

}
