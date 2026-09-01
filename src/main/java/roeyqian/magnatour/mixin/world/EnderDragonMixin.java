/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.world.WorldHelperForBossFight;

@Mixin(value = EnderDragon.class, priority = 3600000)
public abstract class EnderDragonMixin {

  @Shadow
  private EnderDragonFight dragonFight;

  /* Ender Dragon: Handle direct kills, which skip the normal death animation. */
  @Inject(method = "kill", at = @At("HEAD"))
  private void inKill(
      ServerLevel world,
      CallbackInfo ci
  ) {
    WorldHelperForBossFight.cacheDragonKillReward(
        this.dragonFight,
        (EnderDragon) (Object) this
    );
  }

  /* Ender Dragon: Cache the killer after the final damage is recorded. */
  @Inject(method = "reallyHurt", at = @At("TAIL"))
  private void inReallyHurt(
      ServerLevel world,
      DamageSource damageSource,
      float damage,
      CallbackInfo ci
  ) {
    EnderDragon dragon = (EnderDragon) (Object) this;
    if (!dragon.isDeadOrDying()) return;

    Player player = damageSource.getEntity() instanceof Player attacker
        ? attacker
        : null;
    WorldHelperForBossFight.cacheDragonKillReward(
        this.dragonFight,
        dragon,
        player
    );
  }

}
