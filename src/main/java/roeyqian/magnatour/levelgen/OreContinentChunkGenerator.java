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
import java.util.stream.Stream;

// Mojang
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Util;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

// JSpecify
import org.jspecify.annotations.NonNull;

public final class OreContinentChunkGenerator extends ChunkGenerator {

  public static final MapCodec<OreContinentChunkGenerator> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
              BiomeSource.CODEC.fieldOf("biome_source")
                  .forGetter((generator) -> generator.biomeSource),
              FlatLevelGeneratorSettings.CODEC.fieldOf("settings")
                  .forGetter(OreContinentChunkGenerator::settings)
          )
          .apply(instance, OreContinentChunkGenerator::new)
      );

  private final FlatLevelGeneratorSettings settings;

  public OreContinentChunkGenerator(
      BiomeSource biomeSource,
      FlatLevelGeneratorSettings settings
  ) {
    super(biomeSource, Util.memoize(settings::adjustGenerationSettings));
    this.settings = settings;
  }

  @Override
  public void addDebugScreenInfo(
      @NonNull List<String> info,
      @NonNull RandomState randomState,
      @NonNull BlockPos pos
  ) {}

  @Override
  public void applyCarvers(
      @NonNull WorldGenRegion region,
      long seed,
      @NonNull RandomState randomState,
      @NonNull BiomeManager biomeManager,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {}

  @Override
  public void buildSurface(
      @NonNull WorldGenRegion region,
      @NonNull StructureManager structureManager,
      @NonNull RandomState randomState,
      @NonNull ChunkAccess chunk
  ) {}

  @Override @NonNull
  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> structureSets,
      RandomState randomState,
      long seed
  ) {
    Stream<Holder<StructureSet>> stream = this.settings.structureOverrides()
        .map((overrides) -> overrides.stream())
        .orElseGet(() -> structureSets.listElements().map((holder) -> holder));
    return ChunkGeneratorStructureState.createForFlat(
        randomState,
        seed,
        this.biomeSource,
        stream
    );
  }

  @Override @NonNull
  public CompletableFuture<ChunkAccess> fillFromNoise(
      @NonNull Blender blender,
      @NonNull RandomState randomState,
      @NonNull StructureManager structureManager,
      ChunkAccess chunk
  ) {
    List<BlockState> layers = this.settings.getLayers();
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

    int layerCount = Math.min(chunk.getHeight(), layers.size());
    for (int layer = 0; layer < layerCount; layer++) {
      BlockState state = layers.get(layer);
      if (state == null) continue;

      int y = chunk.getMinY() + layer;
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          mutable.set(x, y, z);
          chunk.setBlockState(mutable, state);
          oceanFloor.update(x, y, z, state);
          worldSurface.update(x, y, z, state);
        }
      }
    }

    return CompletableFuture.completedFuture(chunk);
  }

  @Override @NonNull
  public NoiseColumn getBaseColumn(
      int x,
      int z,
      @NonNull LevelHeightAccessor level,
      @NonNull RandomState randomState
  ) {
    List<BlockState> layers = this.settings.getLayers();
    int height = Math.min(level.getHeight(), layers.size());
    BlockState[] states = new BlockState[height];

    for (int i = 0; i < height; i++) {
      BlockState state = layers.get(i);
      states[i] = state == null ? Blocks.AIR.defaultBlockState() : state;
    }

    return new NoiseColumn(level.getMinY(), states);
  }

  @Override
  public int getBaseHeight(
      int x,
      int z,
      Heightmap.@NonNull Types heightmap,
      @NonNull LevelHeightAccessor level,
      @NonNull RandomState randomState
  ) {
    List<BlockState> layers = this.settings.getLayers();
    int topLayer = Math.min(layers.size() - 1, level.getMaxY());

    for (int layer = topLayer; layer >= 0; layer--) {
      BlockState state = layers.get(layer);
      if (state != null && heightmap.isOpaque().test(state)) {
        return level.getMinY() + layer + 1;
      }
    }

    return level.getMinY();
  }

  @Override
  public int getGenDepth() {
    return 384;
  }

  @Override
  public int getMinY() {
    return 0;
  }

  @Override
  public int getSeaLevel() {
    return -63;
  }

  @Override
  public int getSpawnHeight(
      @NonNull LevelHeightAccessor level
  ) {
    return level.getMinY()
        + Math.min(level.getHeight(), this.settings.getLayers().size());
  }

  public FlatLevelGeneratorSettings settings() {
    return this.settings;
  }

  @Override
  public void spawnOriginalMobs(
      @NonNull WorldGenRegion region
  ) {}

  @Override @NonNull
  protected MapCodec<? extends ChunkGenerator> codec() {
    return CODEC;
  }

}
