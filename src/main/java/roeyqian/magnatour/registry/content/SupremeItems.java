/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomArmorMaterial;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.item.supreme.EmperorArmor;
import roeyqian.magnatour.item.supreme.EmperorBow;
import roeyqian.magnatour.item.supreme.MirrorMobile;
import roeyqian.magnatour.item.supreme.StrangeLingeringPotion;
import roeyqian.magnatour.item.supreme.StrangePotion;
import roeyqian.magnatour.item.supreme.StrangeSplashPotion;
import roeyqian.magnatour.registry.ItemRegHelper;

/*
 * Supreme Group: All Items (Handheld, Armor, Material, Tonic, Spawn Egg)
 */
public final class SupremeItems {

  private static final TagKey<Block> EMPEROR_AXE_PICKAXE_MINEABLE = TagKey.create(
      Registries.BLOCK,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "mineable/emperor_axe_pickaxe")
  );

  // Handheld - Tools and Weapons
  public static final Item EMPEROR_BOW = ItemRegHelper.registerDurableItem(
      "emperor_bow", EmperorBow::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .durability(100)
          .enchantable(1)
  );
  public static final Item EMPEROR_AXE_PICKAXE = ItemRegHelper.registerDurableItem(
      "emperor_axe_pickaxe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .axe(CustomToolMaterial.SUPREME_TOOL, 110.0F, -2.5F)
          .tool(CustomToolMaterial.SUPREME_TOOL, EMPEROR_AXE_PICKAXE_MINEABLE,
              110.0F, -2.5F, 5.0F)
          .durability(100)
  );
  public static final Item EMPEROR_HOE = ItemRegHelper.registerDurableItem(
      "emperor_hoe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .hoe(CustomToolMaterial.SUPREME_TOOL, 10.0F, -1.0F)
          .durability(100)
  );
  public static final Item MIRROR_MOBILE = ItemRegHelper.registerDurableItem(
      "mirror_mobile",
      MirrorMobile::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
  );
  public static final Item EMPEROR_SHOVEL = ItemRegHelper.registerDurableItem(
      "emperor_shovel",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .shovel(CustomToolMaterial.SUPREME_TOOL, 10.0F, -1.0F)
          .durability(100)
  );
  public static final Item EMPEROR_SWORD = ItemRegHelper.registerDurableItem(
      "emperor_sword", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .sword(CustomToolMaterial.SUPREME_TOOL, 100.0F, 1.0F)
          .durability(100)
  );

  // Armor
  public static final Item EMPEROR_BOOTS = ItemRegHelper.registerDurableItem(
      "emperor_boots", settings -> new EmperorArmor(settings, 1, 25),
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_BOOTS_ARMOR, ArmorType.BOOTS)
          .durability(100)
  );
  public static final Item EMPEROR_CHESTPLATE = ItemRegHelper.registerDurableItem(
      "emperor_chestplate", settings -> new EmperorArmor(settings, 4, 40),
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_CHESTPLATE_ARMOR, ArmorType.CHESTPLATE)
          .component(DataComponents.GLIDER, Unit.INSTANCE)
          .durability(100)
  );
  public static final Item EMPEROR_HELMET = ItemRegHelper.registerDurableItem(
      "emperor_helmet", settings -> new EmperorArmor(settings, 2, 30),
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_HELMET_ARMOR, ArmorType.HELMET)
          .durability(100)
  );
  public static final Item EMPEROR_LEGGINGS = ItemRegHelper.registerDurableItem(
      "emperor_leggings", settings -> new EmperorArmor(settings, 3, 35),
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_LEGGINGS_ARMOR, ArmorType.LEGGINGS)
          .durability(100)
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
  public static final Item RAINBOW_MIRROR = ItemRegHelper.registerConsumableItem(
      "rainbow_mirror", 64, Item::new,
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
  public static final Item FORTUNE_CRYSTAL = ItemRegHelper.registerConsumableItem(
      "fortune_crystal", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item FORTUNE_METAL = ItemRegHelper.registerConsumableItem(
      "fortune_metal", 64, Item::new,
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
  public static final Item GREAT_BANQUET = ItemRegHelper.registerConsumableItem(
      "great_banquet", 64, Item::new,
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
