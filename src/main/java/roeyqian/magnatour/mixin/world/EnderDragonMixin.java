/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.supreme.SupremeDragonRewardMain;
import roeyqian.magnatour.mixinhelper.universe.UniverseDragonRewardMain;
import roeyqian.magnatour.registry.stuff.MagnaItems;

@Mixin(value = EnderDragon.class, priority = 3600000)
public abstract class EnderDragonMixin {

  @Unique
  private void magnatour$cacheDragonReward() {
    EnderDragon dragon = (EnderDragon) (Object) this;
    SupremeDragonRewardMain.takeCachedReward(dragon);
    UniverseDragonRewardMain.takeCachedReward(dragon);
    if (this.magnatour$qualifiesForUniverseReward()) {
      UniverseDragonRewardMain.cacheReward(dragon);
    } else {
      SupremeDragonRewardMain.cacheReward(dragon);
    }
  }

  @Unique
  private boolean magnatour$qualifiesForUniverseReward() {
    Player player = ((EnderDragon) (Object) this).getLastHurtByPlayer();
    return player != null
        && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMRED))
        && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMBLUE))
        && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMYELLOW))
        && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMGREEN))
        && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMBLACK))
        && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMWHITE));
  }

  /* Ender Dragon: Preserve the qualified killer through the death animation. */
  @Inject(method = "handleKillingBlow", at = @At("HEAD"))
  private void inHandleKillingBlow(
      CallbackInfo ci
  ) {
    this.magnatour$cacheDragonReward();
  }

  /* Ender Dragon: Handle direct kills, which skip the normal death animation. */
  @Inject(method = "kill", at = @At("HEAD"))
  private void inKill(
      ServerLevel world,
      CallbackInfo ci
  ) {
    this.magnatour$cacheDragonReward();
  }

}
