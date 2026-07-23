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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseStick extends Item {

  public UniverseStick(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override
  public void hurtEnemy(
      @NonNull ItemStack stack,
      @NonNull LivingEntity target,
      @NonNull LivingEntity user
  ) {
    if (!(user instanceof Player player)) return;
    if (!(player.level() instanceof ServerLevel world)) return;

    target.hurtServer(world, world.damageSources().playerAttack(player), player.experienceLevel);
    for (int i = 0; i < player.experienceLevel; i++) {
      target.dropFromLootTable(
          world, world.damageSources().playerAttack(player),
          true, target.getLootTable().orElseThrow()
      );
    }
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_stick", 2)
        );
  }

}
