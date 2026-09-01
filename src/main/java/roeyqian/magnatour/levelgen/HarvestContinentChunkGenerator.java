/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
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
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;

/**
 * Fully custom Harvest Continent generator.
 *
 * <p>The height field, terrain material, caves and aquifers are all authored
 * here. The {@code settings} identifier is preserved solely for existing
 * dimension JSON compatibility; it is not dereferenced by this generator.</p>
 */
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

  private static final int GEN_DEPTH = 384;
  private static final int MIN_Y = -64;
  private static final int MAX_Y = MIN_Y + GEN_DEPTH - 1;

  private volatile long terrainSeed;

  private final Identifier settings;

  public HarvestContinentChunkGenerator(
      BiomeSource biomeSource,
      Identifier settings
  ) {
    super(biomeSource);
    this.settings = settings;
  }

  @Override
  public void addDebugScreenInfo(
      @NonNull List<String> info,
      @NonNull RandomState randomState,
      @NonNull BlockPos pos
  ) {
    info.add("Harvest terrain: custom heightfield + custom caves/aquifers");
  }

  /** Caves are filled in fillFromNoise, so vanilla carvers are intentionally not run. */
  @Override
  public void applyCarvers(
      @NonNull WorldGenRegion region,
      long seed,
      @NonNull RandomState randomState,
      @NonNull BiomeManager biomeManager,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {}

  /** Surface material is placed while the custom terrain is filled. */
  @Override
  public void buildSurface(
      @NonNull WorldGenRegion region,
      @NonNull StructureManager structureManager,
      @NonNull RandomState randomState,
      @NonNull ChunkAccess chunk
  ) {}

  @Override @NonNull
  public CompletableFuture<ChunkAccess> createBiomes(
      @NonNull RandomState randomState,
      @NonNull Blender blender,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {
    chunk.fillBiomesFromNoise(this.biomeSource, randomState.sampler());
    return CompletableFuture.completedFuture(chunk);
  }

  @Override @NonNull
  public ChunkGeneratorStructureState createState(
      HolderLookup<StructureSet> structureSets,
      RandomState randomState,
      long seed
  ) {
    this.terrainSeed = seed;
    return super.createState(structureSets, randomState, seed);
  }

  @Override @NonNull
  public CompletableFuture<ChunkAccess> fillFromNoise(
      @NonNull Blender blender,
      @NonNull RandomState randomState,
      @NonNull StructureManager structureManager,
      @NonNull ChunkAccess chunk
  ) {
    SurfaceGrid surface = sampleSurfaceGrid(chunk, randomState);
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
    Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    long seed = this.terrainSeed;

    for (int localX = 0; localX < 16; localX++) {
      int worldX = chunk.getPos().getMinBlockX() + localX;
      for (int localZ = 0; localZ < 16; localZ++) {
        int worldZ = chunk.getPos().getMinBlockZ() + localZ;
        ResourceKeyBiome profile = surface.profile(localX, localZ);
        int surfaceY = profile.surfaceY();
        int lakeBed = profile.bigLake() ? HarvestContinentTerrain.lakeBedHeight(seed, worldX, worldZ) : MIN_Y;

        for (int y = MIN_Y; y <= MAX_Y; y++) {
          BlockState state = blockAt(seed, profile, worldX, y, worldZ, lakeBed);
          pos.set(localX, y, localZ);
          chunk.setBlockState(pos, state);
          oceanFloor.update(localX, y, localZ, state);
          worldSurface.update(localX, y, localZ, state);
        }

        placeSurface(profile, worldX, worldZ, pos, chunk, oceanFloor, worldSurface);
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
    ResourceKeyBiome profile = sampleSingleColumn(x, z, randomState);
    BlockState[] states = new BlockState[level.getHeight()];
    int lakeBed = profile.bigLake() ? HarvestContinentTerrain.lakeBedHeight(this.terrainSeed, x, z) : MIN_Y;
    for (int i = 0; i < states.length; i++) {
      int y = level.getMinY() + i;
      states[i] = blockAt(this.terrainSeed, profile, x, y, z, lakeBed);
    }
    applySurfaceToColumn(profile, x, z, level.getMinY(), states);
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
    ResourceKeyBiome profile = sampleSingleColumn(x, z, randomState);
    if (profile.bigLake() && heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
      return HarvestContinentTerrain.lakeBedHeight(this.terrainSeed, x, z) + 1;
    }
    return Math.min(level.getMaxY(), profile.surfaceY()) + 1;
  }

  @Override public int getGenDepth() { return GEN_DEPTH; }

  @Override public int getMinY() { return MIN_Y; }

  @Override public int getSeaLevel() { return HarvestContinentTerrain.SEA_LEVEL; }

  @Override public int getSpawnHeight(
      @NonNull LevelHeightAccessor level
  ) { return HarvestContinentTerrain.SEA_LEVEL + 1; }

  public Identifier settings() { return this.settings; }

  @Override public void spawnOriginalMobs(
      @NonNull WorldGenRegion region
  ) {}

  @Override @NonNull protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

  private static BlockState blockAt(
      long seed,
      ResourceKeyBiome profile,
      int x,
      int y,
      int z,
      int lakeBed
  ) {
    if (y < MIN_Y || y > MAX_Y) return Blocks.AIR.defaultBlockState();
    if (profile.bigLake() && y > lakeBed && y <= HarvestContinentTerrain.SEA_LEVEL) {
      return Blocks.WATER.defaultBlockState();
    }
    if (y > profile.surfaceY()) return Blocks.AIR.defaultBlockState();
    if (HarvestContinentTerrain.isCave(seed, x, y, z, profile.surfaceY())) {
      if (y <= -54) return Blocks.LAVA.defaultBlockState();
      return HarvestContinentTerrain.isAquiferWater(seed, x, y, z)
          ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }
    return profile.pumpkinGorge() ? Blocks.TERRACOTTA.defaultBlockState() : Blocks.STONE.defaultBlockState();
  }

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

  private static void placeSurface(
      ResourceKeyBiome profile,
      int x,
      int z,
      BlockPos.MutableBlockPos pos,
      ChunkAccess chunk,
      Heightmap oceanFloor,
      Heightmap worldSurface
  ) {
    if (profile.bigLake()) return;
    int y = profile.surfaceY();
    BlockState top;
    BlockState filler;
    if (profile.pumpkinGorge()) {
      top = Blocks.RED_SAND.defaultBlockState();
      filler = Blocks.DYED_TERRACOTTA.orange().defaultBlockState();
    } else {
      filler = SupremeBlocks.EVER_WATER_SOIL.defaultBlockState();
      top = profile.wheatPlain() && !HarvestContinentTerrain.isInTreeClearing(x, z)
          ? SupremeBlocks.EVER_WATER_FARMLAND.defaultBlockState()
          : SupremeBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState();
    }
    set(chunk, pos, oceanFloor, worldSurface, y, top);
    set(chunk, pos, oceanFloor, worldSurface, y - 1, filler);
    set(chunk, pos, oceanFloor, worldSurface, y - 2, filler);
    set(chunk, pos, oceanFloor, worldSurface, y - 3, filler);
    if (profile.wheatPlain() && !HarvestContinentTerrain.isInTreeClearing(x, z)) {
      set(chunk, pos, oceanFloor, worldSurface, y + 1,
          Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7));
    }
  }

  private static void applySurfaceToColumn(
      ResourceKeyBiome profile,
      int x,
      int z,
      int minY,
      BlockState[] states
  ) {
    if (profile.bigLake()) return;
    int base = profile.surfaceY() - minY;
    if (base < 0 || base >= states.length) return;
    if (profile.pumpkinGorge()) {
      states[base] = Blocks.RED_SAND.defaultBlockState();
      if (base > 0) states[base - 1] = Blocks.DYED_TERRACOTTA.orange().defaultBlockState();
      return;
    }
    boolean crop = profile.wheatPlain() && !HarvestContinentTerrain.isInTreeClearing(x, z);
    states[base] = crop ? SupremeBlocks.EVER_WATER_FARMLAND.defaultBlockState()
        : SupremeBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState();
    for (int depth = 1; depth <= 3 && base - depth >= 0; depth++) {
      states[base - depth] = SupremeBlocks.EVER_WATER_SOIL.defaultBlockState();
    }
    if (crop && base + 1 < states.length) states[base + 1] = Blocks.WHEAT.defaultBlockState()
        .setValue(BlockStateProperties.AGE_7, 7);
  }

  /** Eight-connected distance makes the requested 128 blocks radial, not square-only. */
  private static int[][] distancesToBigLake(
      ResourceKey<Biome>[][] biomes
  ) {
    int sizeX = biomes.length;
    int sizeZ = biomes[0].length;
    int[][] distances = new int[sizeX][sizeZ];
    int[] queue = new int[sizeX * sizeZ];
    int head = 0;
    int tail = 0;

    for (int x = 0; x < sizeX; x++) {
      for (int z = 0; z < sizeZ; z++) {
        if (biomes[x][z].equals(HarvestContinentTerrain.BIG_LAKE)) {
          distances[x][z] = 0;
          queue[tail++] = x * sizeZ + z;
        } else {
          distances[x][z] = -1;
        }
      }
    }
    while (head < tail) {
      int index = queue[head++];
      int x = index / sizeZ;
      int z = index % sizeZ;
      int nextDistance = distances[x][z] + 1;
      if (nextDistance > HarvestContinentTerrain.SHORE_BLEND_DISTANCE) continue;
      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (dx == 0 && dz == 0) continue;
          int nextX = x + dx;
          int nextZ = z + dz;
          if (nextX < 0 || nextX >= sizeX || nextZ < 0 || nextZ >= sizeZ
              || distances[nextX][nextZ] != -1) continue;
          distances[nextX][nextZ] = nextDistance;
          queue[tail++] = nextX * sizeZ + nextZ;
        }
      }
    }
    return distances;
  }

  private SurfaceGrid sampleSurfaceGrid(
      ChunkAccess chunk,
      RandomState randomState
  ) {
    int halo = HarvestContinentTerrain.SHORE_BLEND_DISTANCE;
    int size = 16 + halo * 2;
    int originX = chunk.getPos().getMinBlockX() - halo;
    int originZ = chunk.getPos().getMinBlockZ() - halo;
    @SuppressWarnings("unchecked")
    ResourceKey<Biome>[][] biomes = new ResourceKey[size][size];
    int[][] heights = new int[size][size];
    long seed = this.terrainSeed;

    for (int gx = 0; gx < size; gx++) {
      int worldX = originX + gx;
      for (int gz = 0; gz < size; gz++) {
        int worldZ = originZ + gz;
        ResourceKey<Biome> biome = HarvestContinentTerrain.resolveBiome(this.biomeSource.getNoiseBiome(
            worldX >> 2, HarvestContinentTerrain.SEA_LEVEL >> 2, worldZ >> 2, randomState.sampler()));
        biomes[gx][gz] = biome;
        heights[gx][gz] = HarvestContinentTerrain.rawSurfaceHeight(biome, seed, worldX, worldZ);
      }
    }
    int[][] lakeDistances = distancesToBigLake(biomes);
    for (int gx = 0; gx < size; gx++) {
      for (int gz = 0; gz < size; gz++) {
        heights[gx][gz] = HarvestContinentTerrain.blendLakeShoreHeight(
            biomes[gx][gz], heights[gx][gz], lakeDistances[gx][gz]
        );
      }
    }
    HarvestContinentTerrain.limitTerrainSlope(heights);

    ResourceKeyBiome[][] profiles = new ResourceKeyBiome[16][16];
    for (int localX = 0; localX < 16; localX++) {
      for (int localZ = 0; localZ < 16; localZ++) {
        int gx = localX + halo;
        int gz = localZ + halo;
        profiles[localX][localZ] = new ResourceKeyBiome(biomes[gx][gz], heights[gx][gz]);
      }
    }
    return new SurfaceGrid(profiles);
  }

  private ResourceKeyBiome sampleSingleColumn(
      int x,
      int z,
      RandomState randomState
  ) {
    long seed = this.terrainSeed;
    int best = Integer.MAX_VALUE;
    ResourceKey<Biome> center = null;
    int radius = HarvestContinentTerrain.SHORE_BLEND_DISTANCE;
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dz = -radius; dz <= radius; dz++) {
        int worldX = x + dx;
        int worldZ = z + dz;
        ResourceKey<Biome> biome = HarvestContinentTerrain.resolveBiome(this.biomeSource.getNoiseBiome(
            worldX >> 2, HarvestContinentTerrain.SEA_LEVEL >> 2, worldZ >> 2, randomState.sampler()));
        if (dx == 0 && dz == 0) center = biome;
        int candidate = HarvestContinentTerrain.rawSurfaceHeight(biome, seed, worldX, worldZ)
            + HarvestContinentTerrain.MAX_TERRAIN_SLOPE * (Math.abs(dx) + Math.abs(dz));
        best = Math.min(best, candidate);
      }
    }
    return new ResourceKeyBiome(center == null ? HarvestContinentTerrain.WHEAT_PLAIN : center, best);
  }

  private record ResourceKeyBiome(
      ResourceKey<Biome> biome,
      int surfaceY
  ) {

    boolean bigLake() { return this.biome.equals(HarvestContinentTerrain.BIG_LAKE); }

    boolean pumpkinGorge() { return this.biome.equals(HarvestContinentTerrain.PUMPKIN_GORGE); }

    boolean wheatPlain() { return this.biome.equals(HarvestContinentTerrain.WHEAT_PLAIN); }

  }

  private record SurfaceGrid(
      ResourceKeyBiome[][] profiles
  ) {

    ResourceKeyBiome profile(
        int x,
        int z
    ) { return this.profiles[x][z]; }

  }

}
