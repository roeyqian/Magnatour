/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomArmorMaterial;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.item.supreme.StrangeLingeringPotion;
import roeyqian.magnatour.item.supreme.StrangePotion;
import roeyqian.magnatour.item.supreme.StrangeSplashPotion;
import roeyqian.magnatour.item.supreme.SupremeMobile;
import roeyqian.magnatour.registry.ItemRegHelper;

/*
 * Supreme Group: All Items (Handheld, Armor, Material, Tonic, Spawn Egg)
 */
public final class SupremeItems {

  // Handheld - Tools and Weapons
  public static final Item SUPREME_AXE = ItemRegHelper.registerDurableItem(
      "supreme_axe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .axe(CustomToolMaterial.SUPREME_TOOL, 5.0F, -3.0F)
  );
  public static final Item SUPREME_HOE = ItemRegHelper.registerDurableItem(
      "supreme_hoe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .hoe(CustomToolMaterial.SUPREME_TOOL, -4.0F, 0.0F)
  );
  public static final Item SUPREME_MOBILE = ItemRegHelper.registerDurableItem(
      "supreme_mobile",
      SupremeMobile::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
  );
  public static final Item SUPREME_PICKAXE = ItemRegHelper.registerDurableItem(
      "supreme_pickaxe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .pickaxe(CustomToolMaterial.SUPREME_TOOL, 1.0F, -2.8F)
  );
  public static final Item SUPREME_SHOVEL = ItemRegHelper.registerDurableItem(
      "supreme_shovel",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .shovel(CustomToolMaterial.SUPREME_TOOL, 1.5F, -3.0F)
  );
  public static final Item SUPREME_SWORD = ItemRegHelper.registerDurableItem(
      "supreme_sword", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .sword(CustomToolMaterial.SUPREME_TOOL, 3.0F, -2.4F)
  );

  // Armor
  public static final Item SUPREME_BOOTS = ItemRegHelper.registerDurableItem(
      "supreme_boots", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_ARMOR, ArmorType.BOOTS).enchantable(100)
  );
  public static final Item SUPREME_CHESTPLATE = ItemRegHelper.registerDurableItem(
      "supreme_chestplate", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_ARMOR, ArmorType.CHESTPLATE).enchantable(100)
  );
  public static final Item SUPREME_HELMET = ItemRegHelper.registerDurableItem(
      "supreme_helmet", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_ARMOR, ArmorType.HELMET).enchantable(100)
  );
  public static final Item SUPREME_LEGGINGS = ItemRegHelper.registerDurableItem(
      "supreme_leggings", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_ARMOR, ArmorType.LEGGINGS).enchantable(100)
  );

  // Material
  public static final Item HARVEST_CORE = ItemRegHelper.registerConsumableItem(
      "harvest_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item ORE_CORE = ItemRegHelper.registerConsumableItem(
      "ore_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item RAINBOW_THING = ItemRegHelper.registerConsumableItem(
      "rainbow_thing", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item STRANGE_MATTER = ItemRegHelper.registerConsumableItem(
      "strange_matter", 64, Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
  );
  public static final Item SUPREME_CORE = ItemRegHelper.registerConsumableItem(
      "supreme_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item SUPREME_CRYSTAL = ItemRegHelper.registerConsumableItem(
      "supreme_crystal", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item SUPREME_METAL = ItemRegHelper.registerConsumableItem(
      "supreme_metal", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );

  // Tonic
  public static final Item FRUIT_OF_ALL_THINGS = ItemRegHelper.registerConsumableItem(
      "fruit_of_all_things", 64, Item::new,
      new Item.Properties()
          .food(new FoodProperties(10, 100.0F, false))
          .rarity(Rarity.RARE)
  );
  public static final Item SEED_OF_ALL_THINGS = ItemRegHelper.registerConsumableItem(
      "seed_of_all_things", 64, settings -> new BlockItem(SupremeBlocks.CROP_OF_ALL_THINGS, settings),
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item STRANGE_LINGERING_POTION = ItemRegHelper.registerDurableItem(
      "strange_lingering_potion",
      StrangeLingeringPotion::new, new Item.Properties()
  );
  public static final Item STRANGE_POTION = ItemRegHelper.registerDurableItem(
      "strange_potion",
      StrangePotion::new, new Item.Properties()
  );
  public static final Item STRANGE_SPLASH_POTION = ItemRegHelper.registerDurableItem(
      "strange_splash_potion",
      StrangeSplashPotion::new, new Item.Properties()
  );
  public static final Item SUPREME_BANQUET = ItemRegHelper.registerConsumableItem(
      "supreme_banquet", 64, Item::new,
      new Item.Properties()
          .food(new FoodProperties(100, 10000.0F, true))
          .rarity(Rarity.RARE)
  );

  // Spawn Egg
  public static final Item BELL_RINGER_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "bell_ringer_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.BELL_RINGER)
  );
  public static final Item BELL_SOUL_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "bell_soul_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.BELL_SOUL)
  );
  public static final Item ENDER_DRAGON_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "ender_dragon_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(EntityTypes.ENDER_DRAGON)
  );
  public static final Item NETHERITE_GOLEM_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "netherite_golem_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.NETHERITE_GOLEM)
  );
  public static final Item OBSIDIAN_GOLEM_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "obsidian_golem_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.OBSIDIAN_GOLEM)
  );
  public static final Item PALE_LORD_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "pale_lord_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.PALE_LORD)
  );
  public static final Item SCULK_BEHEMOTH_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "sculk_behemoth_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.SCULK_BEHEMOTH)
  );
  public static final Item THE_UNNAMEABLE_EGG = ItemRegHelper.registerConsumableItem(
      "the_unnameable_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(SupremeEntities.THE_UNNAMEABLE_THING)
  );
  public static final Item WITHER_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "wither_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(EntityTypes.WITHER)
  );

  private SupremeItems() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'SupremeItems'");
  }

}
