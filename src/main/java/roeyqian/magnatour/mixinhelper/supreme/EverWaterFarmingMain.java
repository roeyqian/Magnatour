/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.supreme;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.registry.stuff.MagnaBlocks;

public final class EverWaterFarmingMain {

  private EverWaterFarmingMain() {}

  public static void handleEatBlockCanUse(
      Mob mob,
      Level level,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (cir.getReturnValue()) {
      return;
    }
    if (!(mob instanceof net.minecraft.world.entity.animal.sheep.Sheep sheep)) {
      return;
    }

    int chance = sheep.isBaby() ? 50 : 1000;
    if (sheep.getRandom().nextInt(chance) != 0 && !sheep.isSheared()) {
      return;
    }

    BlockPos blockPos = mob.blockPosition();
    if (level.getBlockState(blockPos.below()).is(MagnaBlocks.EVER_WATER_GRASS_BLOCK)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleEatBlockTick(
      Mob mob,
      Level level,
      int eatAnimationTick
  ) {
    if (eatAnimationTick != 2) {
      return;
    }

    BlockPos blockPos = mob.blockPosition();
    BlockPos downPos = blockPos.below();
    BlockState downState = level.getBlockState(downPos);
    boolean vanillaHandled = level.getBlockState(blockPos).is(BlockTags.EDIBLE_FOR_SHEEP)
        || level.getBlockState(downPos).is(Blocks.GRASS_BLOCK);

    if (!vanillaHandled && downState.is(MagnaBlocks.EVER_WATER_GRASS_BLOCK)) {
      if (level instanceof ServerLevel serverWorld
          && serverWorld.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING)
      ) {
        level.levelEvent(2001, downPos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
        level.setBlock(downPos, MagnaBlocks.EVER_WATER_SOIL.defaultBlockState(), 2);
      }
      mob.ate();
    }
  }

  public static void handleMayPlaceOn(
      BlockState floor,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (floor.is(MagnaBlocks.EVER_WATER_FARMLAND)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleStemMayPlaceOn(
      BlockState floor,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (floor.is(MagnaBlocks.EVER_WATER_FARMLAND)) {
      cir.setReturnValue(true);
    }
  }

}
