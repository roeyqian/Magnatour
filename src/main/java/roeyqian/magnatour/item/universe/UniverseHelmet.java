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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseHelmet extends Item {

  public UniverseHelmet(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override
  public void inventoryTick(
      @NonNull ItemStack stack,
      ServerLevel world,
      @NonNull Entity entity,
      EquipmentSlot slot
  ) {
    if (!world.isClientSide() && entity instanceof LivingEntity living) {
      if (living.getItemBySlot(EquipmentSlot.HEAD) == stack) {
        living.removeEffect(MobEffects.HUNGER);
        living.removeEffect(MobEffects.NAUSEA);
        living.removeEffect(MobEffects.DARKNESS);
        living.removeEffect(MobEffects.BLINDNESS);
      }
    }
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings).component(
        DataComponents.LORE, CustomItemSetting.universeLore("universe_helmet", 3)
    );
  }

}
