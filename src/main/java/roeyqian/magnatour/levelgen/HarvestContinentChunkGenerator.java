/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen;

// Java Standard
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// Mojang
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.levelgen.biome.HarvestContinentBiomeSource;
import roeyqian.magnatour.levelgen.terrain.HarvestContinentCaveSampler;
import roeyqian.magnatour.levelgen.terrain.HarvestContinentTerrain;
import roeyqian.magnatour.levelgen.terrain.HarvestMelonTerrain;
import roeyqian.magnatour.levelgen.tree.ReservedGoldenTree;
import roeyqian.magnatour.registry.content.SupremeBlocks;

public final class HarvestContinentChunkGenerator extends ChunkGenerator {

  public static final MapCodec<HarvestContinentChunkGenerator> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
                  BiomeSource.CODEC.fieldOf("biome_source")
                      .forGetter((generator) -> generator.biomeSource),
                  Identifier.CODEC.fieldOf("settings")
                      .forGetter(HarvestContinentChunkGenerator::settings)
              )
              .apply(instance, HarvestContinentChunkGenerator::new)
      );

  private static final int GEN_DEPTH = HarvestContinentTerrain.GEN_DEPTH;
  private static final int MIN_Y = -64;
  private static final int MAX_Y = MIN_Y + GEN_DEPTH - 1;

  private volatile long terrainSeed;

  private final Identifier settings;

  private final HarvestContinentBiomeSource harvestourSource;

  public HarvestContinentChunkGenerator(
      BiomeSource biomeSource,
      Identifier settings
  ) {
    super(biomeSource);
    if (!(biomeSource instanceof HarvestContinentBiomeSource source)) {
      throw new IllegalArgumentException("Harvest terrain requires a harvest biome source");
    }
    this.harvestourSource = source;
    this.settings = settings;
  }

  @Override
  public void addDebugScreenInfo(
      @NonNull List<String> info,
      @NonNull RandomState randomState,
      @NonNull BlockPos pos,
      @NonNull SamplerContext samplerContext
  ) {
    double harvestour = this.harvestourSource.sampleHarvestour(pos.getX(), pos.getZ());
    info.add("Harvest harvestour: " + harvestour);
    info.add("Harvest terrain: blended profiles + custom caves/aquifers");
  }

  @Override
  public void applyBiomeDecoration(
      @NonNull WorldGenLevel level,
      @NonNull ChunkAccess chunk,
      @NonNull StructureManager structureManager
  ) {
    super.applyBiomeDecoration(level, chunk, structureManager);
    // Neighbor terrain already exists here, so crowns can cross chunk boundaries.
    // Read the grass marker instead of repeating the per-column reservation test.
    int minX = chunk.getPos().getMinBlockX();
    int minZ = chunk.getPos().getMinBlockZ();
    BlockPos.MutableBlockPos ground = new BlockPos.MutableBlockPos();
    for (int x = minX; x < minX + 16; x++) {
      for (int z = minZ; z < minZ + 16; z++) {
        HarvestContinentBiomeSource.SurfaceSample surface = this.harvestourSource.sampleSurface(x, z, this.terrainSeed);
        if (!surface.biome().equals(HarvestContinentTerrain.WHEAT_PLAIN)) {
          continue;
        }
        int y = surface.height();
        ground.set(x, y, z);
        if (level.getBlockState(ground).is(SupremeBlocks.EVER_WATER_GRASS_BLOCK)) {
          ReservedGoldenTree.place(level, this.terrainSeed, ground);
        }
      }
    }
  }

  /** Caves are filled in buildTerrain, so vanilla carvers are intentionally not run. */
  public void applyCarvers(
      @NonNull WorldGenRegion region,
      long seed,
      @NonNull RandomState randomState,
      @NonNull BiomeManager biomeManager,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {}

  /** Surface material is placed while the custom terrain is filled. */
  public void buildSurface(
      @NonNull WorldGenRegion region,
      @NonNull StructureManager structureManager,
      @NonNull RandomState randomState,
      @NonNull ChunkAccess chunk
  ) {}

  @Override @NonNull
  public CompletableFuture<ChunkAccess> buildTerrain(
      @NonNull ChunkAccess chunk,
      @NonNull Blender blender,
      @NonNull RandomState randomState,
      @NonNull StructureManager structureManager,
      @NonNull BiomeManager biomeManager,
      @NonNull WorldGenRegion region,
      @NonNull Set<Holder<Biome>> availableBiomes
  ) {
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    long seed = this.terrainSeed;
    int minX = chunk.getPos().getMinBlockX();
    int minZ = chunk.getPos().getMinBlockZ();
    ResourceKeyBiome[][] profiles = new ResourceKeyBiome[16][16];
    int maxSurfaceY = MIN_Y;
    for (int localX = 0; localX < 16; localX++) {
      for (int localZ = 0; localZ < 16; localZ++) {
        ResourceKeyBiome profile = sampleSingleColumn(minX + localX, minZ + localZ);
        profiles[localX][localZ] = profile;
        maxSurfaceY = Math.max(maxSurfaceY, profile.surfaceY());
      }
    }
    HarvestContinentCaveSampler caves =
        new HarvestContinentCaveSampler(seed, minX, minZ, 16, 16, maxSurfaceY);

    for (int localX = 0; localX < 16; localX++) {
      int worldX = chunk.getPos().getMinBlockX() + localX;
      for (int localZ = 0; localZ < 16; localZ++) {
        int worldZ = chunk.getPos().getMinBlockZ() + localZ;
        ResourceKeyBiome profile = profiles[localX][localZ];

        for (int y = MIN_Y; y <= MAX_Y; y++) {
          BlockState state = blockAt(caves, profile, worldX, y, worldZ);
          pos.set(localX, y, localZ);
          chunk.setBlockState(pos, state);
          oceanFloor.update(localX, y, localZ, state);
          worldSurface.update(localX, y, localZ, state);
        }

        placeSurface(profile, pos, chunk, oceanFloor, worldSurface);
      }
    }
    return CompletableFuture.completedFuture(chunk);
  }

  @Override @NonNull
  public ChunkGeneratorStructureState createState(
      @NonNull HolderLookup<StructureSet> structureSets,
      @NonNull RandomState randomState,
      long seed
  ) {
    this.terrainSeed = seed;
    this.harvestourSource.setWorldSeed(seed);
    return super.createState(structureSets, randomState, seed);
  }

  @Override @NonNull
  public NoiseColumn getBaseColumn(
      int x,
      int z,
      @NonNull LevelHeightAccessor level,
      @NonNull RandomState randomState
  ) {
    ResourceKeyBiome profile = sampleSingleColumn(x, z);
    HarvestContinentCaveSampler caves =
        new HarvestContinentCaveSampler(this.terrainSeed, x, z, 1, 1, profile.surfaceY());
    BlockState[] states = new BlockState[level.getHeight()];
    for (int i = 0; i < states.length; i++) {
      int y = level.getMinY() + i;
      states[i] = blockAt(caves, profile, x, y, z);
    }
    applySurfaceToColumn(profile, level.getMinY(), states);
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
    NoiseColumn column = getBaseColumn(x, z, level, randomState);
    for (int y = Math.min(MAX_Y, level.getMaxY()); y >= level.getMinY(); y--) {
      if (heightmap.isOpaque().test(column.getBlock(y))) return y + 1;
    }
    return level.getMinY();
  }

  @Override public int getGenDepth() { return GEN_DEPTH; }

  public WeightedList<MobSpawnSettings.SpawnerData> getHarvestAnimalsAt(
      Level level,
      StructureManager structures,
      BlockPos pos
  ) {
    return super.getMobsAt(level, structures, MobCategory.CREATURE, pos);
  }

  @Override public int getMinY() { return MIN_Y; }

  @Override
  public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(
      Level level,
      StructureManager structures,
      MobCategory category,
      BlockPos pos
  ) {
    // Creature spawning is handled once per cycle by BiomeMobSpawner, with its
    // own population and batch limits. Keep vanilla spawning for other groups.
    return category == MobCategory.CREATURE ? WeightedList.of()
        : super.getMobsAt(level, structures, category, pos);
  }

  @Override public int getSeaLevel() { return HarvestContinentTerrain.SEA_LEVEL; }

  @Override public int getSpawnHeight(
      @NonNull LevelHeightAccessor level
  ) { return HarvestContinentTerrain.SEA_LEVEL + 1; }

  public Identifier settings() { return this.settings; }

  @Override public void spawnOriginalMobs(
      @NonNull WorldGenRegion region
  ) {}

  @Override @NonNull protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

  private static void set(
      ChunkAccess chunk,
      BlockPos.MutableBlockPos pos,
      Heightmap oceanFloor,
      Heightmap worldSurface,
      int y,
      BlockState state
  ) {
    if (y < MIN_Y || y > MAX_Y) return;
    int localX = pos.getX();
    int localZ = pos.getZ();
    pos.setY(y);
    chunk.setBlockState(pos, state);
    oceanFloor.update(localX, y, localZ, state);
    worldSurface.update(localX, y, localZ, state);
  }

  private static BlockState blockAt(
      HarvestContinentCaveSampler caves,
      ResourceKeyBiome profile,
      int x,
      int y,
      int z
  ) {
    if (y < MIN_Y || y > MAX_Y) return Blocks.AIR.defaultBlockState();
    if (y > profile.surfaceY()) {
      return (profile.bigLake() || profile.marsh()) && y <= HarvestContinentTerrain.SEA_LEVEL
          ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }
    if (profile.melonTerrain() != null && !profile.melonTerrain().solid(y)) {
      return Blocks.AIR.defaultBlockState();
    }
    if (caves.isCave(x, y, z, profile.surfaceY())) {
      if (y <= -54) return Blocks.LAVA.defaultBlockState();
      return y <= profile.waterLevel()
          ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }
    return profile.pumpkinGorge() ? Blocks.TERRACOTTA.defaultBlockState() : Blocks.STONE.defaultBlockState();
  }

  private static void placeSurface(
      ResourceKeyBiome profile,
      BlockPos.MutableBlockPos pos,
      ChunkAccess chunk,
      Heightmap oceanFloor,
      Heightmap worldSurface
  ) {
    if (profile.bigLake()) return;
    int y = profile.surfaceY();
    BlockState top = surfaceTop(profile);
    BlockState filler = surfaceFiller(profile);
    set(chunk, pos, oceanFloor, worldSurface, y, top);
    set(chunk, pos, oceanFloor, worldSurface, y - 1, filler);
    set(chunk, pos, oceanFloor, worldSurface, y - 2, filler);
    set(chunk, pos, oceanFloor, worldSurface, y - 3, filler);
    if (profile.snowfield() && !profile.berryClearing() || profile.summit() && y >= 480 + HarvestContinentTerrain.SURFACE_Y_OFFSET) {
      set(chunk, pos, oceanFloor, worldSurface, y + 1, Blocks.SNOW.defaultBlockState());
    }
    if (profile.crop()) {
      set(chunk, pos, oceanFloor, worldSurface, y + 1,
          Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7));
    }
  }

  private static void applySurfaceToColumn(
      ResourceKeyBiome profile,
      int minY,
      BlockState[] states
  ) {
    if (profile.bigLake()) return;
    int base = profile.surfaceY() - minY;
    if (base < 0 || base >= states.length) return;
    boolean crop = profile.crop();
    states[base] = surfaceTop(profile);
    for (int depth = 1; depth <= 3 && base - depth >= 0; depth++) {
      states[base - depth] = surfaceFiller(profile);
    }
    if ((profile.snowfield() && !profile.berryClearing() || profile.summit() && profile.surfaceY() >= 480 + HarvestContinentTerrain.SURFACE_Y_OFFSET) && base + 1 < states.length) {
      states[base + 1] = Blocks.SNOW.defaultBlockState();
    }
    if (crop && base + 1 < states.length) states[base + 1] = Blocks.WHEAT.defaultBlockState()
        .setValue(BlockStateProperties.AGE_7, 7);
  }

  private static BlockState surfaceTop(
      ResourceKeyBiome profile
  ) {
    if (profile.pumpkinGorge()) return Blocks.RED_SAND.defaultBlockState();
    if (profile.desert()) return Blocks.SAND.defaultBlockState();
    if (profile.summit()) return Blocks.STONE.defaultBlockState();
    if (profile.marsh()) return profile.surfaceY() < HarvestContinentTerrain.SEA_LEVEL
        ? Blocks.MUD.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
    if (profile.snowfield()) return profile.berryClearing()
        ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
    return profile.crop() ? SupremeBlocks.EVER_WATER_FARMLAND.defaultBlockState()
        : SupremeBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState();
  }

  private static BlockState surfaceFiller(
      ResourceKeyBiome profile
  ) {
    if (profile.pumpkinGorge()) return Blocks.DYED_TERRACOTTA.orange().defaultBlockState();
    if (profile.desert()) return Blocks.SANDSTONE.defaultBlockState();
    if (profile.summit()) return Blocks.STONE.defaultBlockState();
    if (profile.snowfield()) return profile.berryClearing()
        ? Blocks.DIRT.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
    if (profile.marsh()) return Blocks.DIRT.defaultBlockState();
    return SupremeBlocks.EVER_WATER_SOIL.defaultBlockState();
  }

  /** The same pointwise function is used for chunks, columns and structures. */
  private ResourceKeyBiome sampleSingleColumn(
      int x,
      int z
  ) {
    HarvestContinentBiomeSource.SurfaceSample surface = this.harvestourSource.sampleSurface(x, z, this.terrainSeed);
    ResourceKey<Biome> biome = surface.biome();
    return new ResourceKeyBiome(
        biome,
        surface.height(),
        HarvestContinentTerrain.aquiferWaterLevel(this.terrainSeed, x, z),
        biome.equals(HarvestContinentTerrain.WHEAT_PLAIN)
            && HarvestContinentTerrain.isTreeReservation(x, z),
        biome.equals(HarvestContinentTerrain.FROST_SNOWFIELD)
            && HarvestContinentTerrain.fbmPerlin(this.terrainSeed ^ 0xD1310BA698DFB5ACL,
                x, z, 0.045, 2) > 0.10,
        HarvestMelonTerrain.column(this.terrainSeed, x, z, surface.height(),
            surface.melonWeight())
    );
  }

  private record ResourceKeyBiome(
      ResourceKey<Biome> biome,
      int surfaceY,
      int waterLevel,
      boolean treeReservation,
      boolean berryClearing,
      HarvestMelonTerrain.Column melonTerrain
  ) {

    boolean wheatPlain() { return this.biome.equals(HarvestContinentTerrain.WHEAT_PLAIN); }

    boolean bigLake() { return this.biome.equals(HarvestContinentTerrain.BIG_LAKE); }

    boolean crop() { return wheatPlain() && !this.treeReservation; }

    boolean desert() { return this.biome.equals(HarvestContinentTerrain.CACTUS_DESERT); }

    boolean marsh() { return this.biome.equals(HarvestContinentTerrain.SUGARCANE_MARSH); }

    boolean pumpkinGorge() { return this.biome.equals(HarvestContinentTerrain.PUMPKIN_GORGE); }

    boolean snowfield() { return this.biome.equals(HarvestContinentTerrain.FROST_SNOWFIELD); }

    boolean summit() { return this.biome.equals(HarvestContinentTerrain.GOLDEN_SUMMIT); }

  }

}
