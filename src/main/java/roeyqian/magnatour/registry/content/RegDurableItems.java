/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomArmorMaterial;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.item.universe.UniverseBucket;
import roeyqian.magnatour.item.universe.UniverseStick;
import roeyqian.magnatour.item.supreme.SupremeMobile;
import roeyqian.magnatour.item.universe.UniverseBoots;
import roeyqian.magnatour.item.universe.UniverseChestplate;
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.item.universe.UniverseHelmet;
import roeyqian.magnatour.item.universe.UniverseLeggings;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.registry.ItemRegHelper;

/*
 * Supreme Group: Handheld, Armor
 * Universe Group: Handheld, Armor
 */
public final class RegDurableItems {

  public static final Item SUPREME_AXE = ItemRegHelper.registerDurableItem(
      "supreme_axe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .axe(CustomToolMaterial.SUPREME_TOOL, 5.0F, -3.0F)
  );
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
  // Supreme Group: Armor
  public static final Item SUPREME_HELMET = ItemRegHelper.registerDurableItem(
      "supreme_helmet", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_ARMOR, ArmorType.HELMET).enchantable(100)
  );
  public static final Item SUPREME_HOE = ItemRegHelper.registerDurableItem(
      "supreme_hoe",
      Item::new, CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .hoe(CustomToolMaterial.SUPREME_TOOL, -4.0F, 0.0F)
  );
  public static final Item SUPREME_LEGGINGS = ItemRegHelper.registerDurableItem(
      "supreme_leggings", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .humanoidArmor(CustomArmorMaterial.SUPREME_ARMOR, ArmorType.LEGGINGS).enchantable(100)
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
  // Supreme Group: Handheld
  public static final Item SUPREME_SWORD = ItemRegHelper.registerDurableItem(
      "supreme_sword", Item::new,
      CustomItemSetting.applySupremeDefaults(new Item.Properties())
          .sword(CustomToolMaterial.SUPREME_TOOL, 3.0F, -2.4F)
  );
  public static final Item UNIVERSE_BOOTS = ItemRegHelper.registerDurableItem(
      "universe_boots",
      UniverseBoots::new, new Item.Properties()
          .humanoidArmor(CustomArmorMaterial.UNIVERSE_ARMOR, ArmorType.BOOTS)
  );
  public static final Item UNIVERSE_BUCKET = ItemRegHelper.registerDurableItem(
      "universe_bucket",
      UniverseBucket::new, new Item.Properties()
  );
  public static final Item UNIVERSE_CHESTPLATE = ItemRegHelper.registerDurableItem(
      "universe_chestplate",
      UniverseChestplate::new, new Item.Properties()
          .humanoidArmor(CustomArmorMaterial.UNIVERSE_ARMOR, ArmorType.CHESTPLATE)
  );
  public static final Item UNIVERSE_CONSOLE = ItemRegHelper.registerDurableItem(
      "universe_console",
      UniverseConsole::new, new Item.Properties()
  );
  // Universe Group: Armor
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
  public static final Item UNIVERSE_OMNI_BLADE = ItemRegHelper.registerDurableItem(
      "universe_omni_blade",
      UniverseOmniBlade::new, new Item.Properties()
          .sword(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .axe(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .pickaxe(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
          .shovel(CustomToolMaterial.UNIVERSE_TOOL, 0, 0)
  );
  // Universe Group: Handheld
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

  private RegDurableItems() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegDurableItems'");
  }

}
