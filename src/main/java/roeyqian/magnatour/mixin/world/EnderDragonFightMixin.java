/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.world.WorldHelperForBossFight;

@Mixin(value = EnderDragonFight.class, priority = 3600000)
public abstract class EnderDragonFightMixin {

  @Shadow
  private boolean hasPreviouslyKilledDragon;

  @Shadow
  private java.util.UUID dragonUUID;

  @Shadow
  private BlockPos origin;

  @Shadow
  private ServerLevel level;

  /* Ender Dragon: Spawn the post-first-kill workstation reward near the podium
   */
  @Inject(method = "setDragonKilled", at = @At("HEAD"))
  private void inSetDragonKilledHead(
      EnderDragon dragon,
      CallbackInfo ci
  ) {
    WorldHelperForBossFight.handleSetDragonKilledHead(
        (EnderDragonFight) (Object) this,
        this.hasPreviouslyKilledDragon,
        this.dragonUUID,
        dragon
    );
  }

  /* Ender Dragon: Spawn the post-first-kill workstation reward near the podium
   */
  @Inject(method = "setDragonKilled", at = @At("TAIL"))
  private void inSetDragonKilledTail(
      EnderDragon dragon,
      CallbackInfo ci
  ) {
    WorldHelperForBossFight.handleSetDragonKilledTail(
        (EnderDragonFight) (Object) this,
        this.level,
        this.origin
    );
  }

}
