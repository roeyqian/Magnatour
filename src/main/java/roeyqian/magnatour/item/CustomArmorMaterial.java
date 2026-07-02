/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Java Standard
import java.util.Map;

// Google Guava
import com.google.common.collect.Maps;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface CustomArmorMaterial extends ArmorMaterials {

  int supreme = 10000;
  int universe = 3600000;

  ArmorMaterial SUPREME_ARMOR = new ArmorMaterial(
      supreme,
      makeDefense(supreme, supreme, supreme, supreme, supreme),
      supreme,
      SoundEvents.ARMOR_EQUIP_NETHERITE,
      supreme,
      (supreme * 0.1F),
      TagKey.create(
          Registries.ITEM,
          Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_core")
      ),
      ResourceKey.create(
          EquipmentAssets.ROOT_ID,
          Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme")
      )
  );
  ArmorMaterial UNIVERSE_ARMOR = new ArmorMaterial(
      universe,
      makeDefense(universe, universe, universe, universe, universe),
      universe,
      SoundEvents.ARMOR_EQUIP_NETHERITE,
      universe,
      (universe * 0.1F),
      TagKey.create(
          Registries.ITEM,
          Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_star")
      ),
      ResourceKey.create(
          EquipmentAssets.ROOT_ID,
          Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe")
      )
  );

  private static Map<ArmorType, Integer> makeDefense(
      int feet,
      int legs,
      int chest,
      int head,
      int body
  ) {
    return Maps.newEnumMap(
        Map.of(
            ArmorType.BOOTS, feet,
            ArmorType.LEGGINGS, legs,
            ArmorType.CHESTPLATE, chest,
            ArmorType.HELMET, head,
            ArmorType.BODY, body
        )
    );
  }

}
