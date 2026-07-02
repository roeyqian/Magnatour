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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.StrangePotionEffect;

public class StrangePotion extends PotionItem {

  public StrangePotion(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public ItemStack finishUsingItem(
      @NonNull ItemStack stack,
      @NonNull Level level,
      @NonNull LivingEntity entity
  ) {
    ItemStack result = super.finishUsingItem(stack, level, entity);

    if (!level.isClientSide() && entity instanceof Player player) {
      StrangePotionEffect.applyRandomEffects(player, player.getRandom());
    }

    return result;
  }

  @Override @NonNull
  public InteractionResult use(
      @NonNull Level level,
      @NonNull Player user,
      @NonNull InteractionHand hand
  ) {
    return super.use(level, user, hand);
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applySupremeDefaults(settings)
        .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK);
  }

}
