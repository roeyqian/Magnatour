/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Java Standard
import java.util.function.Supplier;

// Fabric
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;

public class UniverseBanquet extends Item {

  private static final int RESTORATION_AMOUNT = 3600000;

  private static final ThreadLocal<LivingEntity> FEEDING_TARGET = new ThreadLocal<>();

  public UniverseBanquet(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  public static void healFeedingTarget() {
    LivingEntity target = FEEDING_TARGET.get();
    if (target != null) {
      restoreHealth(target);
    }
  }

  public static void init() {
    UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
      if (player.isSpectator()
          || !(entity instanceof Mob mob)
          || !(player.getItemInHand(hand).getItem() instanceof UniverseBanquet)) {
        return InteractionResult.PASS;
      }
      if (!mob.isAlive() || !player.isWithinEntityInteractionRange(mob, 0.0)) {
        return InteractionResult.FAIL;
      }
      // Some mobs only accept food on the server; send the interaction without predicting it.
      if (world.isClientSide()) {
        return InteractionResult.SUCCESS;
      }
      if (mob instanceof AbstractHorse horse && horse.isFood(player.getItemInHand(hand))) {
        return feedHorse(horse, player);
      }
      InteractionResult result = withFeedingTarget(
          mob,
          () -> player.interactOn(
              mob, hand, hit == null ? Vec3.ZERO : hit.getLocation().subtract(mob.position())
          )
      );
      // The original interaction already ran; prevent a second attempt when it passed.
      return result == InteractionResult.PASS ? InteractionResult.FAIL : result;
    });
  }

  @Override @NonNull
  public ItemStack finishUsingItem(
      @NonNull ItemStack stack,
      @NonNull Level world,
      @NonNull LivingEntity user
  ) {
    ItemStack result = super.finishUsingItem(stack, world, user);
    restoreHealth(user);
    return result;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .stacksTo(1)
        .food(new FoodProperties(RESTORATION_AMOUNT, RESTORATION_AMOUNT, true))
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_banquet", 2)
        );
  }

  private static void restoreHealth(
      LivingEntity consumer
  ) {
    if (!consumer.level().isClientSide() && consumer.isAlive()) {
      consumer.heal(RESTORATION_AMOUNT);
    }
  }

  private static InteractionResult feedHorse(
      AbstractHorse horse,
      Player player
  ) {
    // Horse food tags alone do not define feeding effects. Reuse golden-carrot behavior
    // with a temporary stack so the banquet remains in the player's hand.
    InteractionResult result = horse.fedFood(player, new ItemStack(Items.GOLDEN_CARROT));
    restoreHealth(horse);
    if (!result.consumesAction()) {
      horse.playSound(SoundEvents.HORSE_EAT, 1.0F, 1.0F);
      horse.gameEvent(GameEvent.EAT, player);
    }
    return InteractionResult.SUCCESS_SERVER;
  }

  private static InteractionResult withFeedingTarget(
      LivingEntity target,
      Supplier<InteractionResult> interaction
  ) {
    LivingEntity previousTarget = FEEDING_TARGET.get();
    FEEDING_TARGET.set(target);
    try {
      return interaction.get();
    } finally {
      if (previousTarget == null) {
        FEEDING_TARGET.remove();
      } else {
        FEEDING_TARGET.set(previousTarget);
      }
    }
  }

}
