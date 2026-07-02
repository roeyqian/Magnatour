/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.durable;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;

public class UniverseBucket extends Item {

  public UniverseBucket(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public ItemStack finishUsingItem(
      @NonNull ItemStack stack,
      @NonNull Level world,
      @NonNull LivingEntity user
  ) {
    if (user instanceof Player player) {
      var milky = stack.get(DataComponents.CONSUMABLE);
      if (milky != null) {
        player.removeAllEffects();
      }
    }
    return stack;
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    Player player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;

    Level level = context.getLevel();
    InteractionHand hand = context.getHand();
    ItemStack stack = context.getItemInHand();
    int mode = stack.getOrDefault(RegComponentTypes.UNIVERSE_BUCKET_MODE, 0);

    Fluid fluidToPlace;
    SoundEvent placeSound;
    if (mode == 0) {
      fluidToPlace = Fluids.WATER;
      placeSound = SoundEvents.BUCKET_EMPTY;
    } else if (mode == 1) {
      fluidToPlace = Fluids.LAVA;
      placeSound = SoundEvents.BUCKET_EMPTY_LAVA;
    } else {
      return InteractionResult.PASS;
    }

    BlockPos pos = context.getClickedPos();
    Direction direction = context.getClickedFace();
    BlockPos placePos = pos.relative(direction);

    if (!level.mayInteract(player, pos) || !player.mayUseItemAt(placePos, direction, stack)) {
      return InteractionResult.FAIL;
    }

    BlockState blockState = level.getBlockState(placePos);
    Block block = blockState.getBlock();
    boolean mayReplace = blockState.canBeReplaced(fluidToPlace);
    boolean canPlace = blockState.isAir() || mayReplace
        || block instanceof LiquidBlockContainer container
            && container.canPlaceLiquid(player, level, placePos, blockState, fluidToPlace);

    if (!canPlace) {
      return InteractionResult.FAIL;
    }

    player.swing(hand);

    if (!level.isClientSide()) {
      if (block instanceof LiquidBlockContainer container && fluidToPlace == Fluids.WATER) {
        container.placeLiquid(level, placePos, blockState, fluidToPlace.defaultFluidState());
      } else {
        if (mayReplace && blockState.getFluidState().isEmpty()) {
          level.destroyBlock(placePos, true);
        }
        level.setBlock(placePos, fluidToPlace.defaultFluidState().createLegacyBlock(), 11);
      }

      level.playSound(null, placePos, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
      level.gameEvent(player, GameEvent.FLUID_PLACE, placePos);
    }

    return InteractionResult.SUCCESS;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_bucket", 2)
        )
        .component(
            DataComponents.CONSUMABLE,
            Consumables.MILK_BUCKET
        );
  }

}
