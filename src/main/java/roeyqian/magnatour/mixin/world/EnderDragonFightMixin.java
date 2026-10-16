/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.supreme.SupremeDragonRewardMain;
import roeyqian.magnatour.mixinhelper.universe.UniverseDragonRewardMain;
import roeyqian.magnatour.registry.stuff.MagnaItems;

@Mixin(value = EnderDragonFight.class, priority = 3600000)
public abstract class EnderDragonFightMixin {

  @Shadow
  private boolean hasPreviouslyKilledDragon;

  @Unique
  private Boolean magnatour$pendingUniverseReward;

  @Shadow
  private java.util.UUID dragonUUID;

  @Shadow
  private BlockPos origin;

  @Shadow
  private ServerLevel level;

  /* Ender Dragon: Spawn the post-first-kill forge reward near the podium
   */
  @Inject(method = "setDragonKilled", at = @At("HEAD"))
  private void inSetDragonKilledHead(
      EnderDragon dragon,
      CallbackInfo ci
  ) {
    this.magnatour$pendingUniverseReward = null;
    boolean cachedUniverseReward = UniverseDragonRewardMain.takeCachedReward(dragon);
    boolean cachedSupremeReward = SupremeDragonRewardMain.takeCachedReward(dragon);
    if (this.hasPreviouslyKilledDragon && dragon.getUUID().equals(this.dragonUUID)) {
      if (cachedUniverseReward || cachedSupremeReward) {
        this.magnatour$pendingUniverseReward = cachedUniverseReward;
      } else {
        Player player = dragon.getLastHurtByPlayer();
        this.magnatour$pendingUniverseReward = player != null
            && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMRED))
            && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMBLUE))
            && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMYELLOW))
            && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMGREEN))
            && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMBLACK))
            && player.getInventory().contains(new ItemStack(MagnaItems.UNIVERSE_GEMWHITE));
      }
    }
  }

  /* Ender Dragon: Spawn the post-first-kill forge reward near the podium
   */
  @Inject(method = "setDragonKilled", at = @At("TAIL"))
  private void inSetDragonKilledTail(
      EnderDragon dragon,
      CallbackInfo ci
  ) {
    Boolean universeReward = this.magnatour$pendingUniverseReward;
    this.magnatour$pendingUniverseReward = null;
    if (universeReward == null) return;
    if (universeReward) {
      UniverseDragonRewardMain.spawnReward(this.level, this.origin);
    } else {
      SupremeDragonRewardMain.spawnReward(this.level, this.origin);
    }
  }

}
