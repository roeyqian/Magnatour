/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.consumable;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseBanquet extends Item {

  public UniverseBanquet(
      Item.Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public ItemStack finishUsingItem(
      @NonNull ItemStack stack,
      @NonNull Level world,
      @NonNull LivingEntity user
  ) {
    if (user.getRandom().nextInt(0, 9) != 0) {
      if (user instanceof Player player) {
        var food = stack.get(DataComponents.FOOD);
        if (food != null) {
          player.getFoodData().eat(food.nutrition(), food.saturation());
        }
      }
      return stack;
    } else {
      return super.finishUsingItem(stack, world, user);
    }
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    ItemStack stack = context.getItemInHand();

    if (growCropGuaranteed(stack, world, pos)) {
      if (!world.isClientSide()) {
        stack.causeUseVibration(context.getPlayer(), GameEvent.ITEM_INTERACT_FINISH);
        world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
        return InteractionResult.SUCCESS_SERVER;
      }
      return InteractionResult.PASS;
    }

    BlockState state = world.getBlockState(pos);
    BlockPos relativePos = pos.relative(context.getClickedFace());
    if (state.isFaceSturdy(world, pos, context.getClickedFace())
        && BoneMealItem.growWaterPlant(stack, world, relativePos, context.getClickedFace())
    ) {
      if (!world.isClientSide()) {
        stack.causeUseVibration(context.getPlayer(), GameEvent.ITEM_INTERACT_FINISH);
        world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, relativePos, 15);
      }
      return InteractionResult.SUCCESS;
    }

    return InteractionResult.PASS;
  }

  private static Item.Properties applySettings(
      Item.Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .food(new FoodProperties(10000, 1000000.0F, true))
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_banquet", 2)
        );
  }

  private static boolean growCropGuaranteed(
      ItemStack stack,
      Level world,
      BlockPos pos
  ) {
    BlockState state = world.getBlockState(pos);
    if (!(state.getBlock() instanceof BonemealableBlock bonemealableBlock)) {
      return false;
    }
    if (!bonemealableBlock.isValidBonemealTarget(world, pos, state)) {
      return false;
    }
    if (world instanceof ServerLevel serverWorld) {
      bonemealableBlock.performBonemeal(serverWorld, world.getRandom(), pos, state);
      stack.shrink(1);
    }
    return true;
  }

}
