/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomArmorMaterial;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.item.universe.UniverseBoots;
import roeyqian.magnatour.item.universe.UniverseBucket;
import roeyqian.magnatour.item.universe.UniverseChestplate;
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.item.universe.UniverseHelmet;
import roeyqian.magnatour.item.universe.UniverseLeggings;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseStar;
import roeyqian.magnatour.item.universe.UniverseStick;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.registry.ItemRegHelper;

/*
 * Universe Group: All Items (Handheld, Armor, Material, Tonic, Spawn Egg)
 */
public final class UniverseItems {

  // Handheld
  public static final Item UNIVERSE_BUCKET = ItemRegHelper.registerDurableItem(
      "universe_bucket",
      UniverseBucket::new, new Item.Properties()
  );
  public static final Item UNIVERSE_CONSOLE = ItemRegHelper.registerDurableItem(
      "universe_console",
      UniverseConsole::new, new Item.Properties()
  );
  public static final Item UNIVERSE_OMNI_BLADE = ItemRegHelper.registerDurableItem(
      "universe_omni_blade",
      UniverseOmniBlade::new, new Item.Properties()
          .sword(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .axe(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .pickaxe(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .shovel(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
  );
  public static final Item UNIVERSE_STICK = ItemRegHelper.registerDurableItem(
      "universe_stick",
      UniverseStick::new, new Item.Properties()
  );
  public static final Item UNIVERSE_ULTIMA_SWORD = ItemRegHelper.registerDurableItem(
      "universe_ultima_sword",
      UniverseUltimaSword::new, new Item.Properties()
          .sword(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .axe(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .pickaxe(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .shovel(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
  );

  // Armor
  public static final Item UNIVERSE_BOOTS = ItemRegHelper.registerDurableItem(
      "universe_boots",
      UniverseBoots::new, new Item.Properties()
          .humanoidArmor(CustomArmorMaterial.UNIVERSE_ARMOR, ArmorType.BOOTS)
  );
  public static final Item UNIVERSE_CHESTPLATE = ItemRegHelper.registerDurableItem(
      "universe_chestplate",
      UniverseChestplate::new, new Item.Properties()
          .humanoidArmor(CustomArmorMaterial.UNIVERSE_ARMOR, ArmorType.CHESTPLATE)
  );
  public static final Item UNIVERSE_HELMET = ItemRegHelper.registerDurableItem(
      "universe_helmet",
      UniverseHelmet::new, new Item.Properties()
          .humanoidArmor(CustomArmorMaterial.UNIVERSE_ARMOR, ArmorType.HELMET)
  );
  public static final Item UNIVERSE_LEGGINGS = ItemRegHelper.registerDurableItem(
      "universe_leggings",
      UniverseLeggings::new, new Item.Properties()
          .humanoidArmor(CustomArmorMaterial.UNIVERSE_ARMOR, ArmorType.LEGGINGS)
  );

  // Material
  public static final Item UNIVERSE_DARK = ItemRegHelper.registerConsumableItem(
      "universe_dark", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMBLACK = ItemRegHelper.registerConsumableItem(
      "universe_gemblack", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMBLUE = ItemRegHelper.registerConsumableItem(
      "universe_gemblue", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMGREEN = ItemRegHelper.registerConsumableItem(
      "universe_gemgreen", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMRED = ItemRegHelper.registerConsumableItem(
      "universe_gemred", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMWHITE = ItemRegHelper.registerConsumableItem(
      "universe_gemwhite", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_GEMYELLOW = ItemRegHelper.registerConsumableItem(
      "universe_gemyellow", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_LIGHT = ItemRegHelper.registerConsumableItem(
      "universe_light", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_PRIMARY_FRAGMENT = ItemRegHelper.registerConsumableItem(
      "universe_primary_fragment", 64, Item::new,
      new Item.Properties().rarity(Rarity.EPIC)
  );
  public static final Item UNIVERSE_STAR = ItemRegHelper.registerConsumableItem(
      "universe_star", 64,
      UniverseStar::new, new Item.Properties()
  );

  // Tonic
  public static final Item UNIVERSE_BANQUET = ItemRegHelper.registerConsumableItem(
      "universe_banquet", 64, Item::new,
      CustomItemSetting.applyUniverseDefaults(
          new Item.Properties().food(new FoodProperties(100000, 10000000.0F, true))
      )
  );

  // Spawn Egg
  public static final Item UNIVERSE_GUARDIAN_SPAWN_EGG = ItemRegHelper.registerConsumableItem(
      "universe_guardian_spawn_egg", 64, SpawnEggItem::new,
      CustomItemSetting.applyUniverseDefaults(
          new Item.Properties().spawnEgg(UniverseLiveEntities.UNIVERSE_GUARDIAN)
      )
  );

  private UniverseItems() {}

  public static void init() {
    int universeFuelTime = Integer.MAX_VALUE / 5;

    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMRED);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMBLUE);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMYELLOW);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMGREEN);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMBLACK);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_GEMWHITE);
    ItemRegHelper.registerFuel(universeFuelTime, UNIVERSE_STAR);

    Magnatour.LOGGER.info("[Server] Initializing 'UniverseItems'");
  }

}
