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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseLeggings extends Item {

  public UniverseLeggings(
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
    if (world.isClientSide() || !(entity instanceof Player player)) return;
    if (player.getItemBySlot(EquipmentSlot.LEGS) == stack) {
      var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
      Identifier universeSpeedId = Identifier.fromNamespaceAndPath(
          Magnatour.MOD_ID, "universe_leggings_speed"
      );

      if (speedAttribute == null) return;
      if (player.isSprinting()) {
        if (!speedAttribute.hasModifier(universeSpeedId)) {
          speedAttribute.addTransientModifier(new AttributeModifier(
              universeSpeedId, 0.5F,
              AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
          ));
        }
      } else if (speedAttribute.hasModifier(universeSpeedId)) {
        speedAttribute.removeModifier(universeSpeedId);
      }
    }
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings).component(
        DataComponents.LORE, CustomItemSetting.universeLore("universe_leggings", 3)
    );
  }

}
