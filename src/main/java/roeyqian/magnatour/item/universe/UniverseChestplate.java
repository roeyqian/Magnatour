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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;

public class UniverseChestplate extends Item {

  public UniverseChestplate(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override
  public void inventoryTick(
      @NonNull ItemStack stack,
      @NonNull ServerLevel world,
      @NonNull Entity entity,
      @Nullable EquipmentSlot slot
  ) {
    if (!world.isClientSide() && entity instanceof LivingEntity living) {
      if (living.getItemBySlot(EquipmentSlot.CHEST) == stack) {
        living.setRemainingFireTicks(0);
        living.setTicksFrozen(0);
      }
    }
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomToolMaterial.applyEternity(CustomItemSetting.applyUniverseDefaults(settings), 10).component(
        DataComponents.LORE, CustomItemSetting.universeLore("universe_chestplate")
    );
  }

}
