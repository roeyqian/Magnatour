/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.supreme;

// Minecraft
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Magnatour
import roeyqian.magnatour.item.supreme.EmperorArmor;

public final class EmperorArmorMain {

  private EmperorArmorMain() {}

  public static float handleSupremeArmorDefense(
      Player player,
      float amount
  ) {
    double damageMultiplier = 1.0;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      ItemStack armorStack = player.getItemBySlot(slot);
      if (!slot.isArmor() || armorStack.isEmpty()) {
        continue;
      }
      if (!(armorStack.getItem() instanceof EmperorArmor armor)) {
        continue;
      }
      // Each piece reduces the remaining damage independently.
      damageMultiplier *= 1.0 - armor.getDamageReduction(armorStack);
    }
    return Math.max(0, (float) (amount * damageMultiplier));
  }

}
