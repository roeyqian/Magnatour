/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Minecraft
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.UniversePotionEffects;

public class UniversePotion extends PotionItem {

  public UniversePotion(
      Properties settings
  ) {
    super(UniversePotionEffects.settings(settings, true));
  }

  @Override @NonNull
  public ItemStack getDefaultInstance() {
    // PotionItem otherwise replaces the configured contents with plain water.
    return new ItemStack(this);
  }

  @Override @NonNull
  public Component getName(
      @NonNull ItemStack stack
  ) {
    return Component.translatable("item.magnatour.universe_potion");
  }

}
