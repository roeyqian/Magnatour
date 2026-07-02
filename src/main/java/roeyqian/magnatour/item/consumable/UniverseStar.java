/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.consumable;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseStar extends Item {

  public UniverseStar(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public InteractionResult use(
      @NonNull Level world,
      Player player,
      @NonNull InteractionHand hand
  ) {
    player.heal(2);
    return InteractionResult.SUCCESS;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_star", 2)
        );
  }

}
