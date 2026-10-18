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
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.item.MagnaItemSettings;

public class UniverseVoidHelmet extends Item {

  public UniverseVoidHelmet(
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
      if (living.getItemBySlot(EquipmentSlot.HEAD) == stack) {
        if (living instanceof Player player) {
          var foodData = player.getFoodData();
          foodData.setFoodLevel(20);
          foodData.setSaturation(20.0F);
        }
        living.setAirSupply(living.getMaxAirSupply());
        // Snapshot the effects before removal mutates the active effects collection.
        var harmfulEffects = living.getActiveEffects().stream()
            .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
            .map(MobEffectInstance::getEffect)
            .toList();
        harmfulEffects.forEach(living::removeEffect);
      }
    }
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return MagnaItemSettings.applyEternity(MagnaItemSettings.applyUniverseDefaults(settings), 10).component(
        DataComponents.LORE, MagnaItemSettings.universeLore("universe_void_helmet")
    );
  }

}
