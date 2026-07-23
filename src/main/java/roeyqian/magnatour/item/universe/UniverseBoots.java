/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseBoots extends Item {

  public UniverseBoots(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings).component(
        DataComponents.LORE, CustomItemSetting.universeLore("universe_boots", 3)
    );
  }

}
