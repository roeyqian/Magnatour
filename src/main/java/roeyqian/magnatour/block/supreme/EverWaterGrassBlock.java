/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;

public class EverWaterGrassBlock extends SpreadingSnowyBlock implements BonemealableBlock {

  public EverWaterGrassBlock(
      BlockBehaviour.Properties settings,
      ResourceKey<Block> baseBlock
  ) {
    super(settings, baseBlock);
  }

  @Override
  public boolean isBonemealSuccess(
      @NonNull Level world,
      @NonNull RandomSource random,
      @NonNull BlockPos pos,
      @NonNull BlockState state,
      BonemealSource source
  ) {
    return true;
  }

  @Override
  public boolean isValidBonemealTarget(
      LevelReader world,
      BlockPos pos,
      @NonNull BlockState state,
      BonemealSource source
  ) {
    return world.getBlockState(pos.above()).isAir();
  }

  @Override
  public void performBonemeal(
      @NonNull ServerLevel world,
      @NonNull RandomSource random,
      BlockPos pos,
      @NonNull BlockState state,
      BonemealSource source
  ) {
    BlockPos above = pos.above();
    var placedFeatureRegistry = world.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
    var grassFeature = placedFeatureRegistry.get(VegetationPlacements.GRASS_BONEMEAL);
    if (grassFeature.isEmpty()) return;

    for (int i = 0; i < 48; ++i) {
      BlockPos target = above.offset(
          random.nextInt(5) - 2,
          random.nextInt(3) - 1,
          random.nextInt(5) - 2);
      if (!world.getBlockState(target.below()).is(this) || !world.getBlockState(target).isAir()) {
        continue;
      }

      if (random.nextInt(4) == 0) {
        var biome = world.getBiome(target);
        var features = biome.value().getGenerationSettings().features();

        boolean placedFlower = false;

        int vegetalIndex = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();

        if (features.size() > vegetalIndex) {
          List<Holder<PlacedFeature>> decorationList =
              features.get(vegetalIndex).stream().toList();

          if (!decorationList.isEmpty() && random.nextBoolean()) {
            var randomFlower = decorationList.get(random.nextInt(decorationList.size()));
            if (randomFlower.value().place(
                world,
                world.getChunkSource().getGenerator(),
                random,
                target)) {
              placedFlower = true;
            }
          }
        }

        if (placedFlower) continue;
      }

      grassFeature.get().value().place(
          world, world.getChunkSource().getGenerator(),
          random, target
      );
    }
  }

  @Override
  protected void randomTick(
      @NonNull BlockState state,
      @NonNull ServerLevel world,
      @NonNull BlockPos pos,
      @NonNull RandomSource random
  ) {
    if (cannotSurvive(world, pos)) {
      world.setBlockAndUpdate(pos, SupremeBlocks.EVER_WATER_SOIL.defaultBlockState());
      return;
    }
    execSpread(world, pos);
  }

  private static boolean cannotSurvive(
      LevelReader world,
      BlockPos pos
  ) {
    BlockState upState = world.getBlockState(pos.above());

    boolean upIsAir = upState.is(Blocks.AIR);
    boolean upIsWater = upState.is(Blocks.WATER);
    boolean upIsPlant = upState.getBlock() instanceof VegetationBlock;
    boolean upIsSimpleSnow = upState.is(Blocks.SNOW) && upState.getValue(SnowLayerBlock.LAYERS) == 1;

    return !upIsSimpleSnow && !upIsAir && !upIsPlant && !upIsWater;
  }

  private void execSpread(
      ServerLevel world,
      BlockPos pos
  ) {
    int range = 4;
    BlockPos.betweenClosedStream(
            pos.offset(-range, -range, -range),
            pos.offset(range, range, range)
        )
        .forEach(targetPos -> {
          BlockState target = world.getBlockState(targetPos);
          if (!target.is(SupremeBlocks.EVER_WATER_SOIL) && !target.is(Blocks.DIRT)) return;
          if (cannotSurvive(world, targetPos)) return;

          world.setBlockAndUpdate(
              targetPos,
              this.defaultBlockState().setValue(
                  SNOWY,
                  isSnowySetting(world.getBlockState(targetPos.above()))
              )
          );
        });
  }

}
