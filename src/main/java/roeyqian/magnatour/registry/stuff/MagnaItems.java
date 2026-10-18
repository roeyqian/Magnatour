/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.stuff;

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
import roeyqian.magnatour.item.MagnaArmorMaterials;
import roeyqian.magnatour.item.MagnaItemSettings;
import roeyqian.magnatour.item.supreme.EmperorArmor;
import roeyqian.magnatour.item.supreme.EmperorBow;
import roeyqian.magnatour.item.supreme.MirrorMobile;
import roeyqian.magnatour.item.supreme.StarAtlas;
import roeyqian.magnatour.item.supreme.StrangeLingeringPotion;
import roeyqian.magnatour.item.supreme.StrangePotion;
import roeyqian.magnatour.item.supreme.StrangeSplashPotion;
import roeyqian.magnatour.item.universe.UniverseBanquet;
import roeyqian.magnatour.item.universe.UniverseBucket;
import roeyqian.magnatour.item.universe.UniverseCommandStar;
import roeyqian.magnatour.item.universe.UniverseCore;
import roeyqian.magnatour.item.universe.UniverseLingeringPotion;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniversePotion;
import roeyqian.magnatour.item.universe.UniverseSplashPotion;
import roeyqian.magnatour.item.universe.UniverseStick;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.item.universe.UniverseVoidBoots;
import roeyqian.magnatour.item.universe.UniverseVoidChestplate;
import roeyqian.magnatour.item.universe.UniverseVoidHelmet;
import roeyqian.magnatour.item.universe.UniverseVoidLeggings;
import roeyqian.magnatour.registry.StuffHelper;

/*
 * Supreme and Universe Groups: All Items (Handheld, Armor, Material, Tonic, Spawn Egg)
 */
public final class MagnaItems {

  // Supreme Group
  private static final TagKey<Block> EMPEROR_AXE_PICKAXE_MINEABLE = TagKey.create(
      Registries.BLOCK,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "mineable/emperor_axe_pickaxe")
  );

  // Handheld - Tools and Weapons
  public static final Item STAR_ATLAS = StuffHelper.registerDurableItem(
      "star_atlas", StarAtlas::new, new Item.Properties()
  );
  public static final Item EMPEROR_BOW = StuffHelper.registerDurableItem(
      "emperor_bow", EmperorBow::new,
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .durability(100)
          .enchantable(1)
  );
  public static final Item EMPEROR_AXE_PICKAXE = StuffHelper.registerDurableItem(
      "emperor_axe_pickaxe",
      Item::new, MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .axe(MagnaItemSettings.SUPREME_TOOL, 110.0F, -2.5F)
          .tool(MagnaItemSettings.SUPREME_TOOL, EMPEROR_AXE_PICKAXE_MINEABLE,
              110.0F, -2.5F, 5.0F)
          .durability(100)
  );
  public static final Item EMPEROR_HOE = StuffHelper.registerDurableItem(
      "emperor_hoe",
      Item::new, MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .hoe(MagnaItemSettings.SUPREME_TOOL, 10.0F, -1.0F)
          .durability(100)
  );
  public static final Item MIRROR_MOBILE = StuffHelper.registerDurableItem(
      "mirror_mobile",
      MirrorMobile::new, MagnaItemSettings.applySupremeDefaults(new Item.Properties())
  );
  public static final Item EMPEROR_SHOVEL = StuffHelper.registerDurableItem(
      "emperor_shovel",
      Item::new, MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .shovel(MagnaItemSettings.SUPREME_TOOL, 10.0F, -1.0F)
          .durability(100)
  );
  public static final Item EMPEROR_SWORD = StuffHelper.registerDurableItem(
      "emperor_sword", Item::new,
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .sword(MagnaItemSettings.SUPREME_TOOL, 100.0F, 1.0F)
          .durability(100)
  );

  // Armor
  public static final Item EMPEROR_BOOTS = StuffHelper.registerDurableItem(
      "emperor_boots", settings -> new EmperorArmor(settings, 1, 25),
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(MagnaArmorMaterials.SUPREME_BOOTS_ARMOR, ArmorType.BOOTS)
          .durability(100)
  );
  public static final Item EMPEROR_CHESTPLATE = StuffHelper.registerDurableItem(
      "emperor_chestplate", settings -> new EmperorArmor(settings, 4, 40),
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(MagnaArmorMaterials.SUPREME_CHESTPLATE_ARMOR, ArmorType.CHESTPLATE)
          .component(DataComponents.GLIDER, Unit.INSTANCE)
          .durability(100)
  );
  public static final Item EMPEROR_HELMET = StuffHelper.registerDurableItem(
      "emperor_helmet", settings -> new EmperorArmor(settings, 2, 30),
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(MagnaArmorMaterials.SUPREME_HELMET_ARMOR, ArmorType.HELMET)
          .durability(100)
  );
  public static final Item EMPEROR_LEGGINGS = StuffHelper.registerDurableItem(
      "emperor_leggings", settings -> new EmperorArmor(settings, 3, 35),
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(MagnaArmorMaterials.SUPREME_LEGGINGS_ARMOR, ArmorType.LEGGINGS)
          .durability(100)
  );

  // Material
  public static final Item HARVEST_CORE = StuffHelper.registerConsumableItem(
      "harvest_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item ORE_CORE = StuffHelper.registerConsumableItem(
      "ore_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item RAINBOW_MIRROR = StuffHelper.registerConsumableItem(
      "rainbow_mirror", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item STRANGE_MATTER = StuffHelper.registerConsumableItem(
      "strange_matter", 64, Item::new,
      MagnaItemSettings.applySupremeDefaults(new Item.Properties())
  );
  public static final Item SUPREME_CORE = StuffHelper.registerConsumableItem(
      "supreme_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item FORTUNE_CRYSTAL = StuffHelper.registerConsumableItem(
      "fortune_crystal", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item FORTUNE_METAL = StuffHelper.registerConsumableItem(
      "fortune_metal", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );

  // Tonic
  public static final Item FRUIT_OF_ALL_THINGS = StuffHelper.registerConsumableItem(
      "fruit_of_all_things", 64, Item::new,
      new Item.Properties()
          .food(new FoodProperties(10, 100.0F, false))
          .rarity(Rarity.RARE)
  );
  public static final Item SEED_OF_ALL_THINGS = StuffHelper.registerConsumableItem(
      "seed_of_all_things", 64, settings -> new BlockItem(MagnaBlocks.CROP_OF_ALL_THINGS, settings),
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item STRANGE_LINGERING_POTION = StuffHelper.registerDurableItem(
      "strange_lingering_potion",
      StrangeLingeringPotion::new, new Item.Properties()
  );
  public static final Item STRANGE_POTION = StuffHelper.registerDurableItem(
      "strange_potion",
      StrangePotion::new, new Item.Properties()
  );
  public static final Item STRANGE_SPLASH_POTION = StuffHelper.registerDurableItem(
      "strange_splash_potion",
      StrangeSplashPotion::new, new Item.Properties()
  );
  public static final Item GREAT_BANQUET = StuffHelper.registerConsumableItem(
      "great_banquet", 64, Item::new,
      new Item.Properties()
          .food(new FoodProperties(100, 10000.0F, true))
          .rarity(Rarity.RARE)
  );

  // Spawn Egg
  public static final Item BELL_RINGER_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "bell_ringer_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.BELL_RINGER)
  );
  public static final Item BELL_SOUL_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "bell_soul_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.BELL_SOUL)
  );
  public static final Item ENDER_DRAGON_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "ender_dragon_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(EntityTypes.ENDER_DRAGON)
  );
  public static final Item NETHERITE_GOLEM_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "netherite_golem_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.NETHERITE_GOLEM)
  );
  public static final Item OBSIDIAN_GOLEM_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "obsidian_golem_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.OBSIDIAN_GOLEM)
  );
  public static final Item PALE_LORD_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "pale_lord_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.PALE_LORD)
  );
  public static final Item SCULK_BEHEMOTH_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "sculk_behemoth_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.SCULK_BEHEMOTH)
  );
  public static final Item THE_UNNAMEABLE_EGG = StuffHelper.registerConsumableItem(
      "the_unnameable_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(MagnaEntities.THE_UNNAMEABLE_THING)
  );
  public static final Item WITHER_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "wither_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(EntityTypes.WITHER)
  );

  // Universe Group
  public static final Item UNIVERSE_ANNIHILATOR_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "universe_annihilator_spawn_egg", 64, SpawnEggItem::new,
      MagnaItemSettings.applyUniverseDefaults(
          new Item.Properties().spawnEgg(MagnaEntities.UNIVERSE_ANNIHILATOR)));

  // Handheld
  public static final Item UNIVERSE_BUCKET = StuffHelper.registerDurableItem(
      "universe_bucket",
      UniverseBucket::new, new Item.Properties()
  );
  public static final Item UNIVERSE_COMMAND_STAR = StuffHelper.registerDurableItem(
      "universe_command_star",
      UniverseCommandStar::new, new Item.Properties()
  );
  public static final Item UNIVERSE_OMNI_BLADE = StuffHelper.registerDurableItem(
      "universe_omni_blade",
      UniverseOmniBlade::new, new Item.Properties()
          .sword(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
          .axe(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
          .pickaxe(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
          .shovel(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
  );
  public static final Item UNIVERSE_STICK = StuffHelper.registerDurableItem(
      "universe_stick",
      UniverseStick::new, new Item.Properties()
  );
  public static final Item UNIVERSE_ULTIMA_SWORD = StuffHelper.registerDurableItem(
      "universe_ultima_sword",
      UniverseUltimaSword::new, new Item.Properties()
          .sword(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
          .axe(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
          .pickaxe(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
          .shovel(MagnaItemSettings.UNIVERSE_TOOL, 0, 0)
  );

  // Armor
  public static final Item UNIVERSE_VOID_BOOTS = StuffHelper.registerDurableItem(
      "universe_void_boots",
      UniverseVoidBoots::new, new Item.Properties()
          .humanoidArmor(MagnaArmorMaterials.UNIVERSE_VOID_ARMOR, ArmorType.BOOTS)
  );
  public static final Item UNIVERSE_VOID_CHESTPLATE = StuffHelper.registerDurableItem(
      "universe_void_chestplate",
      UniverseVoidChestplate::new, new Item.Properties()
          .humanoidArmor(MagnaArmorMaterials.UNIVERSE_VOID_ARMOR, ArmorType.CHESTPLATE)
  );
  public static final Item UNIVERSE_VOID_HELMET = StuffHelper.registerDurableItem(
      "universe_void_helmet",
      UniverseVoidHelmet::new, new Item.Properties()
          .humanoidArmor(MagnaArmorMaterials.UNIVERSE_VOID_ARMOR, ArmorType.HELMET)
  );
  public static final Item UNIVERSE_VOID_LEGGINGS = StuffHelper.registerDurableItem(
      "universe_void_leggings",
      UniverseVoidLeggings::new, new Item.Properties()
          .humanoidArmor(MagnaArmorMaterials.UNIVERSE_VOID_ARMOR, ArmorType.LEGGINGS)
  );

  // Material
  public static final Item UNIVERSE_DARK = StuffHelper.registerConsumableItem(
      "universe_dark", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMBLACK = StuffHelper.registerConsumableItem(
      "universe_gemblack", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMBLUE = StuffHelper.registerConsumableItem(
      "universe_gemblue", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMGREEN = StuffHelper.registerConsumableItem(
      "universe_gemgreen", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMRED = StuffHelper.registerConsumableItem(
      "universe_gemred", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMWHITE = StuffHelper.registerConsumableItem(
      "universe_gemwhite", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMYELLOW = StuffHelper.registerConsumableItem(
      "universe_gemyellow", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_LIGHT = StuffHelper.registerConsumableItem(
      "universe_light", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_PRIMARY_FRAGMENT = StuffHelper.registerConsumableItem(
      "universe_primary_fragment", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_CORE = StuffHelper.registerConsumableItem(
      "universe_core", 64,
      UniverseCore::new, new Item.Properties()
  );

  // Tonic
  public static final Item UNIVERSE_BANQUET = StuffHelper.registerConsumableItem(
      "universe_banquet", 1,
      UniverseBanquet::new, new Item.Properties()
  );

  public static final Item UNIVERSE_POTION = StuffHelper.registerConsumableItem(
      "universe_potion", 1, UniversePotion::new, new Item.Properties()
  );
  public static final Item UNIVERSE_SPLASH_POTION = StuffHelper.registerConsumableItem(
      "universe_splash_potion", 1, UniverseSplashPotion::new, new Item.Properties()
  );
  public static final Item UNIVERSE_LINGERING_POTION = StuffHelper.registerConsumableItem(
      "universe_lingering_potion", 1, UniverseLingeringPotion::new, new Item.Properties()
  );

  // Spawn Egg
  public static final Item UNIVERSE_GUARDIAN_SPAWN_EGG = StuffHelper.registerConsumableItem(
      "universe_guardian_spawn_egg", 64, SpawnEggItem::new,
      MagnaItemSettings.applyUniverseDefaults(
          new Item.Properties().spawnEgg(MagnaEntities.UNIVERSE_GUARDIAN)
      )
  );

  private MagnaItems() {}

  public static void init() {
    StarAtlas.init();
    UniverseBanquet.init();
    int universeFuelTime = Integer.MAX_VALUE / 5;

    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_GEMRED);
    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_GEMBLUE);
    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_GEMYELLOW);
    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_GEMGREEN);
    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_GEMBLACK);
    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_GEMWHITE);
    StuffHelper.registerFuel(universeFuelTime, UNIVERSE_CORE);

    Magnatour.LOGGER.info("[Server] Initializing 'MagnaItemSettings'");
  }

}
