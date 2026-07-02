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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.registry.block.RegActiveBlocks;
import roeyqian.magnatour.utility.registry.item.RegConsumableItems;

@Mixin(value = EnderDragonFight.class, priority = 3600000)
public abstract class EnderDragonFightMixin {

  @Shadow
  private boolean hasPreviouslyKilledDragon;

  @Shadow
  private java.util.UUID dragonUUID;

  @Shadow
  private BlockPos origin;

  @Unique
  private Block magnatour$dragonKillRewardBlock;

  @Shadow
  private ServerLevel level;

  @Unique
  private boolean magnatour$hasAllUniverseGems(
      Player player
  ) {
    return player.getInventory().contains(new ItemStack(RegConsumableItems.UNIVERSE_GEMRED))
        && player.getInventory().contains(new ItemStack(RegConsumableItems.UNIVERSE_GEMBLUE))
        && player.getInventory().contains(new ItemStack(RegConsumableItems.UNIVERSE_GEMYELLOW))
        && player.getInventory().contains(new ItemStack(RegConsumableItems.UNIVERSE_GEMGREEN))
        && player.getInventory().contains(new ItemStack(RegConsumableItems.UNIVERSE_GEMBLACK))
        && player.getInventory().contains(new ItemStack(RegConsumableItems.UNIVERSE_GEMWHITE));
  }

  @Unique
  private boolean magnatour$canPlaceWorktable(
      ServerLevel world,
      BlockPos pos
  ) {
    BlockPos belowPos = pos.below();
    return world.getWorldBorder().isWithinBounds(pos)
        && world.getBlockState(pos).canBeReplaced()
        && world.getBlockState(belowPos).isFaceSturdy(world, belowPos, Direction.UP);
  }

  @Unique
  private Block magnatour$resolveDragonKillRewardBlock(
      EnderDragon dragon
  ) {
    Player player = dragon.getLastHurtByPlayer();
    if (player != null && this.magnatour$hasAllUniverseGems(player)) {
      return RegActiveBlocks.UNIVERSE_WORKSTATION;
    }

    return RegActiveBlocks.SUPREME_WORKTABLE;
  }

  @Unique
  private BlockPos magnatour$findWorktablePos(
      ServerLevel world,
      BlockPos eggPos
  ) {
    for (int radius = 1; radius <= 3; radius++) {
      for (int offsetX = -radius; offsetX <= radius; offsetX++) {
        for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
          if (Math.abs(offsetX) != radius && Math.abs(offsetZ) != radius) {
            continue;
          }

          BlockPos candidatePos = eggPos.offset(offsetX, 0, offsetZ);
          if (this.magnatour$canPlaceWorktable(world, candidatePos)) {
            return candidatePos;
          }
        }
      }
    }

    return eggPos.above();
  }

  /* Ender Dragon: Spawn the post-first-kill workstation reward near the podium
   */
  @Inject(method = "setDragonKilled", at = @At("HEAD"))
  private void inSetDragonKilledHead(
      EnderDragon dragon,
      CallbackInfo ci
  ) {
    boolean shouldSpawnReward =
        this.hasPreviouslyKilledDragon && dragon.getUUID().equals(this.dragonUUID);
    this.magnatour$dragonKillRewardBlock =
        shouldSpawnReward ? this.magnatour$resolveDragonKillRewardBlock(dragon) : null;
  }

  /* Ender Dragon: Spawn the post-first-kill workstation reward near the podium
   */
  @Inject(method = "setDragonKilled", at = @At("TAIL"))
  private void inSetDragonKilledTail(
      EnderDragon dragon,
      CallbackInfo ci
  ) {
    if (this.magnatour$dragonKillRewardBlock == null) {
      return;
    }

    BlockPos eggPos = this.level.getHeightmapPos(
        Heightmap.Types.MOTION_BLOCKING,
        EndPodiumFeature.getLocation(this.origin)
    );
    BlockPos worktablePos = this.magnatour$findWorktablePos(this.level, eggPos);
    this.level.setBlockAndUpdate(worktablePos, this.magnatour$dragonKillRewardBlock.defaultBlockState());
    this.magnatour$dragonKillRewardBlock = null;
  }

}
