/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Java Standard
import java.util.List;
import java.util.Optional;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumables;

public final class UniversePotionEffects {

  private static final int COLOR = 0x6959DF;

  private static final String POTION_NAME = "magnatour_universe";

  private UniversePotionEffects() {}

  public static PotionContents contents() {
    return new PotionContents(Optional.empty(), Optional.of(COLOR), createEffects(), Optional.of(POTION_NAME));
  }

  public static Item.Properties settings(
      Item.Properties settings,
      boolean drinkable
  ) {
    CustomItemSetting.applyUniverseDefaults(settings)
        .component(DataComponents.POTION_CONTENTS, contents())
        .component(DataComponents.LORE, CustomItemSetting.universeLore("universe_potion", 1));
    if (drinkable) {
      settings.component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
          .usingConvertsTo(Items.GLASS_BOTTLE);
    }
    return settings;
  }

  private static List<MobEffectInstance> createEffects() {
    return List.of(
        new MobEffectInstance(MobEffects.HASTE, MobEffectInstance.INFINITE_DURATION, 9),
        new MobEffectInstance(MobEffects.STRENGTH, MobEffectInstance.INFINITE_DURATION, 9),
        new MobEffectInstance(MobEffects.REGENERATION, MobEffectInstance.INFINITE_DURATION, 5),
        new MobEffectInstance(MobEffects.RESISTANCE, MobEffectInstance.INFINITE_DURATION, 4),
        new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0),
        new MobEffectInstance(MobEffects.CONDUIT_POWER, MobEffectInstance.INFINITE_DURATION, 0),
        new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0),
        new MobEffectInstance(MobEffects.HEALTH_BOOST, MobEffectInstance.INFINITE_DURATION, 9),
        new MobEffectInstance(MobEffects.ABSORPTION, MobEffectInstance.INFINITE_DURATION, 19),
        new MobEffectInstance(MobEffects.LUCK, MobEffectInstance.INFINITE_DURATION, 9),
        new MobEffectInstance(MobEffects.DOLPHINS_GRACE, MobEffectInstance.INFINITE_DURATION, 2),
        new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0),
        new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, MobEffectInstance.INFINITE_DURATION, 9)
    );
  }

}
