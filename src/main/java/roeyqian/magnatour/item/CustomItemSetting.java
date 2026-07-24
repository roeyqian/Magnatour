/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemLore;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.logic.CustomComponents;

public interface CustomItemSetting {

  TagKey<DamageType> SUPREME_IMMORTAL_DAMAGE_TYPES = TagKey.create(
      Registries.DAMAGE_TYPE,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_immortal")
  );
  TagKey<DamageType> UNIVERSE_IMMORTAL_DAMAGE_TYPES = TagKey.create(
      Registries.DAMAGE_TYPE,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_immortal")
  );

  static Item.Properties applySupremeDefaults(
      Item.Properties settings
  ) {
    return settings
        .rarity(Rarity.RARE)
        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
        .component(CustomComponents.SUPREME_GLINT_OVERRIDE, true)
        .delayedComponent(
            DataComponents.DAMAGE_RESISTANT,
            provider -> new DamageResistant(provider.lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(SUPREME_IMMORTAL_DAMAGE_TYPES)
            )
        );
  }

  static Item.Properties applyUniverseDefaults(
      Item.Properties settings
  ) {
    return settings
        .rarity(Rarity.EPIC)
        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
        .component(CustomComponents.UNIVERSE_GLINT_OVERRIDE, true)
        .delayedComponent(
            DataComponents.DAMAGE_RESISTANT,
            provider -> new DamageResistant(provider.lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(UNIVERSE_IMMORTAL_DAMAGE_TYPES)
            )
        );
  }

  static ItemLore supremeLore(
      String itemId,
      int lineCount
  ) {
    List<Component> lines = new ArrayList<>();
    lines.add(
        Component.translatable("lore.magnatour." + itemId + ".0")
            .withStyle(ChatFormatting.DARK_GREEN)
    );
    for (int i = 1; i <= lineCount; i++) {
      lines.add(
          Component.translatable("lore.magnatour." + itemId + "." + i)
              .withStyle(ChatFormatting.GREEN)
      );
    }
    return new ItemLore(lines);
  }

  static ItemLore universeLore(
      String itemId,
      int lineCount
  ) {
    List<Component> lines = new ArrayList<>();
    lines.add(
        Component.translatable("lore.magnatour." + itemId + ".0")
            .withStyle(ChatFormatting.DARK_RED)
    );
    for (int i = 1; i <= lineCount; i++) {
      lines.add(
          Component.translatable("lore.magnatour." + itemId + "." + i)
              .withStyle(ChatFormatting.RED)
      );
    }
    return new ItemLore(lines);
  }

}
