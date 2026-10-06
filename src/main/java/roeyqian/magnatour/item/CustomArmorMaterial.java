/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
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

  int supreme = 2400;
  int universe = 3600000;

  ArmorMaterial SUPREME_BOOTS_ARMOR = makeSupremeArmor(ArmorType.BOOTS, 2, 1.0F, 0.15F);
  ArmorMaterial SUPREME_CHESTPLATE_ARMOR = makeSupremeArmor(ArmorType.CHESTPLATE, 8, 7.0F, 0.2F);
  ArmorMaterial SUPREME_HELMET_ARMOR = makeSupremeArmor(ArmorType.HELMET, 4, 3.0F, 0.1F);
  ArmorMaterial SUPREME_LEGGINGS_ARMOR = makeSupremeArmor(ArmorType.LEGGINGS, 6, 5.0F, 0.15F);
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

  private static ArmorMaterial makeSupremeArmor(
      ArmorType type,
      int defense,
      float toughness,
      float knockbackResistance
  ) {
    return new ArmorMaterial(
        supreme,
        Maps.newEnumMap(Map.of(type, defense)),
        24,
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        toughness,
        knockbackResistance,
        TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_core")
        ),
        ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme")
        )
    );
  }

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
