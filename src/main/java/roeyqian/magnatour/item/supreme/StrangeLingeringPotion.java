/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.supreme;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;

// Magnatour
import roeyqian.magnatour.entity.supreme.ThrownStrangeLingeringPotion;
import roeyqian.magnatour.item.CustomItemSetting;

public class StrangeLingeringPotion extends LingeringPotionItem {

  public StrangeLingeringPotion(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override
  public InteractionResult use(
      Level level,
      Player user,
      InteractionHand hand
  ) {
    ItemStack stack = user.getItemInHand(hand);

    level.playSound(
        null,
        user.getX(),
        user.getY(),
        user.getZ(),
        SoundEvents.LINGERING_POTION_THROW,
        SoundSource.PLAYERS,
        0.5F,
        0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
    );

    if (!level.isClientSide()) {
      ItemStack thrownStack = stack.copy();
      thrownStack.setCount(1);

      ThrownStrangeLingeringPotion potion = new ThrownStrangeLingeringPotion(level, user, thrownStack);
      potion.setItem(thrownStack);
      potion.shootFromRotation(
          user,
          user.getXRot(),
          user.getYRot(),
          -20.0F,
          0.5F,
          1.0F
      );

      level.addFreshEntity(potion);
    }

    user.awardStat(Stats.ITEM_USED.get(this));

    if (!user.getAbilities().instabuild) {
      stack.shrink(1);
    }

    return InteractionResult.SUCCESS;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applySupremeDefaults(settings)
        .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK);
  }

}
