/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.supreme;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmperorArmor extends Item {

  private static final int BASE_REDUCTION_PERCENT = 10;

  private final int maxReductionPercent;
  private final int reductionPerLevel;

  public EmperorArmor(
      Properties settings,
      int reductionPerLevel, int maxReductionPercent
  ) {
    super(settings);
    this.reductionPerLevel = reductionPerLevel;
    this.maxReductionPercent = maxReductionPercent;
  }

  public Component getAttributesTooltip(
      ItemStack stack
  ) {
    int reduction = getReductionPercent(stack);
    double growth = (double) (reduction - BASE_REDUCTION_PERCENT)
        / (maxReductionPercent - BASE_REDUCTION_PERCENT);
    String tier;
    if (reduction >= maxReductionPercent) {
      tier = "full";
    } else if (growth >= 2.0 / 3.0) {
      tier = "high";
    } else if (growth >= 1.0 / 3.0) {
      tier = "medium";
    } else {
      tier = "low";
    }
    return Component.translatable(
        "tooltip.magnatour.emperor_armor.attributes",
        Component.translatable("tooltip.magnatour.emperor_armor.tier." + tier)
    ).withStyle(ChatFormatting.GOLD);
  }

  public double getDamageReduction(
      ItemStack stack
  ) {
    return getReductionPercent(stack) / 100.0;
  }

  private int getReductionPercent(
      ItemStack stack
  ) {
    int totalLevels = 0;
    for (var enchantment : stack.getEnchantments().entrySet()) {
      totalLevels += enchantment.getIntValue();
    }
    return Math.min(maxReductionPercent, BASE_REDUCTION_PERCENT + totalLevels * reductionPerLevel);
  }

}
