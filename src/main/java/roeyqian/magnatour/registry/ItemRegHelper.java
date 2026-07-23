/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry;

// Java Standard
import java.util.function.Function;

// Fabric
import net.fabricmc.fabric.api.registry.FuelValueEvents;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface ItemRegHelper {

  private static Item registerItem(
      String name,
      Function<Item.Properties, Item> factory,
      Item.Properties settings
  ) {
    ResourceKey<Item> key = ResourceKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, name)
    );
    Item item = factory.apply(settings.setId(key));
    if (item instanceof BlockItem blockItem) blockItem.registerBlocks(Item.BY_BLOCK, item);
    return Registry.register(BuiltInRegistries.ITEM, key, item);
  }

  static Item registerConsumableItem(
      String name,
      int stacks,
      Function<Item.Properties, Item> factory,
      Item.Properties settings
  ) {
    return registerItem(name, factory, settings.stacksTo(stacks));
  }

  static Item registerDurableItem(
      String name,
      Function<Item.Properties, Item> factory,
      Item.Properties settings
  ) {
    return registerItem(name, factory, settings.stacksTo(1));
  }

  static void registerFuel(
      int time,
      Item fuel
  ) {
    FuelValueEvents.BUILD.register((builder, _) -> builder.add(fuel, time));
  }

}
