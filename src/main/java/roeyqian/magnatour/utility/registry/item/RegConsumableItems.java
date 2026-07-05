/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.item;

// Minecraft
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Rarity;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.consumable.StrangeLingeringPotion;
import roeyqian.magnatour.item.consumable.StrangePotion;
import roeyqian.magnatour.item.consumable.StrangeSplashPotion;
import roeyqian.magnatour.item.consumable.UniverseBanquet;
import roeyqian.magnatour.item.consumable.UniverseGuardianSpawnEgg;
import roeyqian.magnatour.item.consumable.UniverseStar;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.entity.RegLiveEntities;

/*
 * Supreme Group: Material, Tonic, Spawn Egg
 * Universe Group: Material, Tonic, Spawn Egg
 */
public final class RegConsumableItems {

  // Supreme Group: Material
  public static final Item SUPREME_CORE = ItemRegHelper.registerConsumableItem(
      "supreme_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item HARVEST_CORE = ItemRegHelper.registerConsumableItem(
      "harvest_core", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item ORE_CORE = ItemRegHelper.registerConsumableItem(
      "ore_core", 64, Item::new,
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
  public static final Item STRANGE_MATTER = ItemRegHelper.registerConsumableItem(
      "strange_matter", 64, Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
  );
  public static final Item SEED_OF_ALL_THINGS = ItemRegHelper.registerConsumableItem(
      "seed_of_all_things", 64, settings -> new BlockItem(RegInsertBlocks.CROP_OF_ALL_THINGS, settings),
      new Item.Properties().rarity(Rarity.RARE)
  );
  public static final Item RAINBOW_THING = ItemRegHelper.registerConsumableItem(
      "rainbow_thing", 64, Item::new,
      new Item.Properties().rarity(Rarity.RARE)
  );

  // Supreme Group: Tonic
  public static final Item FRUIT_OF_ALL_THINGS = ItemRegHelper.registerConsumableItem(
      "fruit_of_all_things", 64, Item::new,
      new Item.Properties()
          .food(new FoodProperties(10, 100.0F, false))
          .rarity(Rarity.RARE)
  );
  public static final Item SUPREME_BANQUET = ItemRegHelper.registerConsumableItem(
      "supreme_banquet", 64, Item::new,
      new Item.Properties()
          .food(new FoodProperties(100, 10000.0F, true))
          .rarity(Rarity.RARE)
  );
  public static final Item STRANGE_POTION = ItemRegHelper.registerDurableItem(
      "strange_potion",
      StrangePotion::new, new Item.Properties()
  );
  public static final Item STRANGE_SPLASH_POTION = ItemRegHelper.registerDurableItem(
      "strange_splash_potion",
      StrangeSplashPotion::new, new Item.Properties()
  );
  public static final Item STRANGE_LINGERING_POTION = ItemRegHelper.registerDurableItem(
      "strange_lingering_potion",
      StrangeLingeringPotion::new, new Item.Properties()
  );

  // Supreme Group: Spawn Egg
  public static final Item WITHER_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "wither_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(EntityTypes.WITHER)
  );
  public static final Item ENDER_DRAGON_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "ender_dragon_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(EntityTypes.ENDER_DRAGON)
  );
  public static final Item SCULK_BEHEMOTH_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "sculk_behemoth_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.SCULK_BEHEMOTH)
  );
  public static final Item PALE_LORD_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "pale_lord_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.PALE_LORD)
  );
  public static final Item THE_UNNAMEABLE_EGG = ItemRegHelper.registerConsumableItem(
      "the_unnameable_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.THE_UNNAMEABLE_THING)
  );
  public static final Item BELL_RINGER_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "bell_ringer_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.BELL_RINGER)
  );
  public static final Item BELL_SOUL_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "bell_soul_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.BELL_SOUL)
  );
  public static final Item OBSIDIAN_GOLEM_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "obsidian_golem_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.OBSIDIAN_GOLEM)
  );
  public static final Item NETHERITE_GOLEM_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "netherite_golem_spawn_egg", 64, SpawnEggItem::new,
      new Item.Properties().rarity(Rarity.RARE).spawnEgg(RegLiveEntities.NETHERITE_GOLEM)
  );

  // Universe Group: Material
  public static final Item UNIVERSE_GEMRED = ItemRegHelper.registerConsumableItem(
      "universe_gemred", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMBLUE = ItemRegHelper.registerConsumableItem(
      "universe_gemblue", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMYELLOW = ItemRegHelper.registerConsumableItem(
      "universe_gemyellow", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMGREEN = ItemRegHelper.registerConsumableItem(
      "universe_gemgreen", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMBLACK = ItemRegHelper.registerConsumableItem(
      "universe_gemblack", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMWHITE = ItemRegHelper.registerConsumableItem(
      "universe_gemwhite", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_PRIMARY_FRAGMENT = ItemRegHelper.registerConsumableItem(
      "universe_primary_fragment", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_LIGHT = ItemRegHelper.registerConsumableItem(
      "universe_light", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_DARK = ItemRegHelper.registerConsumableItem(
      "universe_dark", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_STAR = ItemRegHelper.registerConsumableItem(
      "universe_star", 64,
      UniverseStar::new, new Item.Properties()
  );

  // Universe Group: Tonic
  public static final Item UNIVERSE_BANQUET = ItemRegHelper.registerConsumableItem(
      "universe_banquet", 16,
      UniverseBanquet::new, new Item.Properties()
  );

  // Universe Group: Spawn Egg
  public static final Item UNIVERSE_GUARDIAN_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "universe_guardian_spawn_egg", 16,
      UniverseGuardianSpawnEgg::new, new Item.Properties()
  );

  private RegConsumableItems() {}

  public static void init() {
    int universeFuelTime = Integer.MAX_VALUE / 5;

    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMRED);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMBLUE);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMYELLOW);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMGREEN);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMBLACK);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMWHITE);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_STAR);

    Magnatour.LOGGER.info("[Server] Initializing 'RegConsumableItems'");
  }

}
