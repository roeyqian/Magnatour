/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;

public class UniverseVoidBoots extends Item {

  public UniverseVoidBoots(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomToolMaterial.applyEternity(CustomItemSetting.applyUniverseDefaults(settings), 10).component(
        DataComponents.LORE, CustomItemSetting.universeLore("universe_void_boots")
    );
  }

}
