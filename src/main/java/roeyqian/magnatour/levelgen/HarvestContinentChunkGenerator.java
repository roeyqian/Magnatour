/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen;

// Java Standard
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Mojang
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.mixinhelper.world.WorldHelperForDimension;

/**
 * Harvest Continent's dedicated generator.
 *
 * <p>The wrapped vanilla generator keeps the full NoiseBasedChunkGenerator
 * pipeline (noise, aquifers, caves, structures, and biome decoration). The
 * harvest surface pass is deliberately owned here instead of being injected
 * into every vanilla noise generator through a mixin.</p>
 */
public final class HarvestContinentChunkGenerator extends ChunkGenerator {

  public static final MapCodec<HarvestContinentChunkGenerator> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
                  BiomeSource.CODEC.fieldOf("biome_source")
                      .forGetter((generator) -> generator.biomeSource),
                  NoiseGeneratorSettings.CODEC.fieldOf("settings")
                      .forGetter(HarvestContinentChunkGenerator::settings)
              )
              .apply(instance, HarvestContinentChunkGenerator::new)
      );

  private final NoiseBasedChunkGenerator vanillaGenerator;

  private final Holder<NoiseGeneratorSettings> settings;

  public HarvestContinentChunkGenerator(
      BiomeSource biomeSource,
      Holder<NoiseGeneratorSettings> settings
  ) {
    super(biomeSource);
    this.settings = settings;
    this.vanillaGenerator = new NoiseBasedChunkGenerator(biomeSource, settings);
  }

  @Override
  public void addDebugScreenInfo(
      @NonNull List<String> info,
      @NonNull RandomState randomState,
      @NonNull BlockPos pos
  ) {
    this.vanillaGenerator.addDebugScreenInfo(info, randomState, pos);
  }

  @Override
  public void applyCarvers(
      @NonNull WorldGenRegion region,
      long seed,
      @NonNull RandomState randomState,
      @NonNull BiomeManager biomeManager,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {
    this.vanillaGenerator.applyCarvers(
        region, seed, randomState, biomeManager, structureManager, chunk
    );
  }

  @Override
  public void buildSurface(
      @NonNull WorldGenRegion region,
      @NonNull StructureManager structureManager,
      @NonNull RandomState randomState,
      @NonNull ChunkAccess chunk
  ) {
    this.vanillaGenerator.buildSurface(region, structureManager, randomState, chunk);
    WorldHelperForDimension.handleBuildSurface(this.vanillaGenerator, region, randomState, chunk);
  }

  @Override
  public CompletableFuture<ChunkAccess> createBiomes(
      @NonNull RandomState randomState,
      @NonNull Blender blender,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {
    return this.vanillaGenerator.createBiomes(randomState, blender, structureManager, chunk);
  }

  @Override @NonNull
  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> structureSets,
      RandomState randomState,
      long seed
  ) {
    return this.vanillaGenerator.createState(structureSets, randomState, seed);
  }

  @Override @NonNull
  public CompletableFuture<ChunkAccess> fillFromNoise(
      @NonNull Blender blender,
      @NonNull RandomState randomState,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {
    return this.vanillaGenerator.fillFromNoise(blender, randomState, structureManager, chunk);
  }

  @Override @NonNull
  public NoiseColumn getBaseColumn(
      int x,
      int z,
      @NonNull LevelHeightAccessor level,
      @NonNull RandomState randomState
  ) {
    return this.vanillaGenerator.getBaseColumn(x, z, level, randomState);
  }

  @Override
  public int getBaseHeight(
      int x,
      int z,
      Heightmap.@NonNull Types heightmap,
      @NonNull LevelHeightAccessor level,
      @NonNull RandomState randomState
  ) {
    return this.vanillaGenerator.getBaseHeight(x, z, heightmap, level, randomState);
  }

  @Override
  public int getGenDepth() {
    return this.vanillaGenerator.getGenDepth();
  }

  @Override
  public int getMinY() {
    return this.vanillaGenerator.getMinY();
  }

  @Override
  public int getSeaLevel() {
    return this.vanillaGenerator.getSeaLevel();
  }

  @Override
  public int getSpawnHeight(
      @NonNull LevelHeightAccessor level
  ) {
    return this.vanillaGenerator.getSpawnHeight(level);
  }

  public Holder<NoiseGeneratorSettings> settings() {
    return this.settings;
  }

  @Override
  public void spawnOriginalMobs(
      @NonNull WorldGenRegion region
  ) {
    this.vanillaGenerator.spawnOriginalMobs(region);
  }

  @Override @NonNull
  protected MapCodec<? extends ChunkGenerator> codec() {
    return CODEC;
  }

}
