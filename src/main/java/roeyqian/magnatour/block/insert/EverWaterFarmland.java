/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.insert;

// Java Standard
import java.util.Optional;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

// JSpecify
import org.jspecify.annotations.NonNull;

public class EverWaterFarmland extends FarmlandBlock {

  public EverWaterFarmland(
      BlockBehaviour.Properties settings
  ) {
    super(settings);
  }

  @Override
  public void fallOn(
      @NonNull Level world,
      @NonNull BlockState state,
      @NonNull BlockPos pos,
      @NonNull Entity entity,
      double fallDistance
  ) {}

  @Override
  public void onPlace(
      @NonNull BlockState state,
      Level world,
      @NonNull BlockPos pos,
      @NonNull BlockState oldState,
      boolean notify
  ) {
    if (!world.isClientSide()) world.scheduleTick(pos, this, 1);
  }

  @Override
  protected void randomTick(
      @NonNull BlockState state,
      @NonNull ServerLevel world,
      @NonNull BlockPos pos,
      @NonNull RandomSource random
  ) {
    tick(state, world, pos, random);
  }

  @Override
  protected void tick(
      BlockState state,
      ServerLevel world,
      @NonNull BlockPos pos,
      @NonNull RandomSource random
  ) {
    world.setBlock(pos, state.setValue(MOISTURE, 7), Block.UPDATE_ALL);

    BlockPos cropPos = pos.above();
    BlockState cropState = world.getBlockState(cropPos);
    Block cropBlock = cropState.getBlock();

    execInstantCropGrowth(world, cropBlock, cropPos, cropState);
    world.scheduleTick(pos, this, 2);
  }

  private void execInstantCropGrowth(
      Level world,
      Block cropBlock,
      BlockPos cropPos,
      BlockState cropState
  ) {
    Optional<IntegerProperty> agePropOpt = cropState.getProperties().stream()
        .filter(p -> p.getName().equals("age") && p instanceof IntegerProperty)
        .map(p -> (IntegerProperty) p)
        .findFirst();

    if (agePropOpt.isPresent()) {
      IntegerProperty ageProp = agePropOpt.get();
      int maxAge = ageProp.getPossibleValues().size() - 1;
      int currentAge = cropState.getValue(ageProp);

      while (currentAge < maxAge) {
        if (cropBlock instanceof CropBlock crop) {
          crop.growCrops(world, cropPos, cropState);
        } else {
          cropState = cropState.setValue(ageProp, currentAge + 1);
          world.setBlock(cropPos, cropState, Block.UPDATE_ALL);
        }
        cropState = world.getBlockState(cropPos);
        currentAge = cropState.getValue(ageProp);
      }
    } else if (cropBlock instanceof StemBlock) {
      world.setBlock(cropPos, cropState.setValue(StemBlock.AGE, 7), Block.UPDATE_ALL);
    }
  }

}
