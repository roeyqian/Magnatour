/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Minecraft
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.registry.logic.MagnaComponents;
import roeyqian.magnatour.registry.stuff.MagnaItems;

/** Universe Ultima Sword mining and hit effects. */
public final class UniverseUltimaSwordMain {

  private static final float UNIVERSE_ULTIMA_SWORD_DESTROY_PROGRESS = 0.5F;

  private UniverseUltimaSwordMain() {}

  public static void handleDestroyProgress(
      Player player,
      CallbackInfoReturnable<Float> cir
  ) {
    ItemStack stack = player.getItemInHand(player.getUsedItemHand());
    if (stack.is(MagnaItems.UNIVERSE_ULTIMA_SWORD)) {
      cir.setReturnValue(UNIVERSE_ULTIMA_SWORD_DESTROY_PROGRESS);
    }
  }

  public static void handleUniverseHitEffect(
      LivingEntity target,
      boolean damaged,
      DamageSource source
  ) {
    if (!damaged) return;
    if (!(source.getEntity() instanceof Player player)) return;

    ItemStack weaponStack = player.getMainHandItem();
    if (!weaponStack.is(MagnaItems.UNIVERSE_ULTIMA_SWORD)) return;

    if (weaponStack.getOrDefault(MagnaComponents.UNIVERSE_ULTIMA_SWORD_MODE, 0) == 1) {
      target.hurtTime = 0;
      target.setInvulnerableTime(0);
    }
  }

}
