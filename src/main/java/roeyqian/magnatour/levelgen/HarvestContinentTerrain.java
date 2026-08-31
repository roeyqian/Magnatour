/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen;

// Java Standard
import java.util.Random;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

// FastUtil
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.mixin.world.WorldGenRegionAccessor;
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public final class HarvestContinentTerrain {

  private static final int LAKE_CENTER_ISLAND_BASE_HEIGHT = 65;
  private static final int LAKE_CENTER_ISLAND_MAX_HEIGHT = LAKE_CENTER_ISLAND_BASE_HEIGHT + 5;
  private static final int LAKE_CENTER_ISLAND_MIN_HEIGHT = LAKE_CENTER_ISLAND_BASE_HEIGHT;
  private static final int MELON_JUNGLE_BOUNDARY_BASE_HEIGHT = 96;
  private static final int MELON_JUNGLE_BOUNDARY_BLEND_RANGE = 128;
  private static final int PUMPKIN_GORGE_BASE_HEIGHT = 256;
  private static final int PUMPKIN_GORGE_BOUNDARY_BLEND_RANGE = 64;
  private static final int PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT = PUMPKIN_GORGE_BASE_HEIGHT - 6;
  private static final int PUMPKIN_GORGE_MAX_HEIGHT = PUMPKIN_GORGE_BASE_HEIGHT + 45;
  private static final int SEA_LEVEL = 64;
  private static final int PUMPKIN_GORGE_BOUNDARY_MIN_HEIGHT = SEA_LEVEL + 1;
  private static final int TREE_CLEARING_RADIUS = 2;
  private static final int TREE_GRID_SIZE = 32;
  private static final int WHEAT_BASE = 128;
  private static final int WHEAT_BOUNDARY_BLEND_RANGE = 32;
  private static final int WHEAT_BOUNDARY_EXTRA_MAX = 96;
  private static final int WHEAT_BOUNDARY_MIN_HEIGHT = SEA_LEVEL;
  private static final int WHEAT_INTERIOR_MIN_HEIGHT = WHEAT_BASE - 5;
  private static final int WHEAT_INTERNAL_MAX = WHEAT_BASE + 7;
  private static final int WINDOW_RADIUS = MELON_JUNGLE_BOUNDARY_BLEND_RANGE;

  private static final ResourceKey<Biome> BIG_LAKE = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "big_lake")
  );
  private static final ResourceKey<Biome> LAKE_CENTER_ISLAND = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "lake_center_island")
  );
  private static final ResourceKey<Biome> MELON_JUNGLE = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "melon_jungle")
  );
  private static final ResourceKey<Biome> PUMPKIN_GORGE = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "pumpkin_gorge")
  );
  private static final ResourceKey<Biome> WHEAT_PLAIN = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "wheat_plain")
  );

  private final NoiseBasedChunkGenerator generator;

  private HarvestContinentTerrain(
      NoiseBasedChunkGenerator generator
  ) {
    this.generator = generator;
  }

  public static double fade(
      double t
  ) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
  }

  public static int fastFloorD(
      double v
  ) {
    int i = (int) v;
    return v < i ? i - 1 : i;
  }

  public static double fbmPerlin(
      long seed,
      double x,
      double z,
      double scale,
      int octaves
  ) {
    double amp = 1.0;
    double freq = scale;
    double sum = 0.0;
    double norm = 0.0;

    for (int i = 0; i < octaves; i++) {
      sum += amp * perlin2D(seed + i * 1013L, x * freq, z * freq);
      norm += amp;
      amp *= 0.5;
      freq *= 2.0;
    }
    return (norm <= 0.0) ? 0.0 : Mth.clamp(sum / norm, -1.0, 1.0);
  }

  public static double[] grad(
      long seed,
      int x,
      int z
  ) {
    long h = mix(seed, x, z);
    int idx = (int) (h & 7L);
    return switch (idx) {
      case 0 -> new double[]{1.0, 0.0};
      case 1 -> new double[]{-1.0, 0.0};
      case 2 -> new double[]{0.0, 1.0};
      case 3 -> new double[]{0.0, -1.0};
      case 4 -> new double[]{0.7071067811865476, 0.7071067811865476};
      case 5 -> new double[]{-0.7071067811865476, 0.7071067811865476};
      case 6 -> new double[]{0.7071067811865476, -0.7071067811865476};
      default -> new double[]{-0.7071067811865476, -0.7071067811865476};
    };
  }

  public static void handleBuildSurface(
      NoiseBasedChunkGenerator generator,
      WorldGenRegion region,
      RandomState noiseConfig,
      ChunkAccess chunk
  ) {
    new HarvestContinentTerrain(generator).onBuildSurface(region, noiseConfig, chunk);
  }

  public static int lakeCenterIslandHeight(
      long seed,
      int worldX,
      int worldZ
  ) {
    double shape = fbmPerlin(seed ^ 0x6A09E667F3BCC909L, worldX, worldZ, 0.026, 2);
    int h = Math.round(LAKE_CENTER_ISLAND_BASE_HEIGHT + (float) (shape * 1.5D));
    return Mth.clamp(h, LAKE_CENTER_ISLAND_MIN_HEIGHT, LAKE_CENTER_ISLAND_MAX_HEIGHT);
  }

  public static double lerp(
      double t,
      double a,
      double b
  ) {
    return a + t * (b - a);
  }

  public static long mix(
      long seed,
      int x,
      int z
  ) {
    long h = seed;
    h ^= (long) x * 0x9E3779B97F4A7C15L;
    h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
    h ^= (h >>> 27);
    h *= 0x3C79AC492BA7B653L;
    h ^= (h >>> 33);
    h *= 0x1C69B3F74AC4AE35L;
    h ^= (h >>> 27);
    return h;
  }

  public static double perlin2D(
      long seed,
      double x,
      double z
  ) {
    int x0 = fastFloorD(x);
    int z0 = fastFloorD(z);
    int x1 = x0 + 1;
    int z1 = z0 + 1;

    double tx = x - x0;
    double tz = z - z0;

    double u = fade(tx);
    double v = fade(tz);

    double[] g00 = grad(seed, x0, z0);
    double[] g10 = grad(seed, x1, z0);
    double[] g01 = grad(seed, x0, z1);
    double[] g11 = grad(seed, x1, z1);

    double n00 = g00[0] * tx + g00[1] * tz;
    double n10 = g10[0] * (tx - 1.0) + g10[1] * tz;
    double n01 = g01[0] * tx + g01[1] * (tz - 1.0);
    double n11 = g11[0] * (tx - 1.0) + g11[1] * (tz - 1.0);

    double nx0 = lerp(u, n00, n10);
    double nx1 = lerp(u, n01, n11);
    return lerp(v, nx0, nx1);
  }

  private void onBuildSurface(
      WorldGenRegion region,
      RandomState noiseConfig,
      ChunkAccess chunk
  ) {
    ServerLevel world = ((WorldGenRegionAccessor) region).getWorld();
    if (world.dimension() != CustomDimensions.HARVEST_CONTINENT) return;

    long seed = world.getSeed();

    @SuppressWarnings("unchecked")
    ResourceKey<Biome>[][] stableBiomes = new ResourceKey[16][16];
    int[][] originalSurfaces = new int[16][16];
    int[][] targetHeights = new int[16][16];

    int minY = chunk.getMinY();
    int maxY = chunk.getMaxY();

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        originalSurfaces[x][z] = findSurfaceY(chunk, x, z, minY, maxY);
      }
    }

    computeStableBiomesAndTargetHeights(
        seed, region, noiseConfig, chunk, originalSurfaces, stableBiomes, targetHeights
    );

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        ResourceKey<Biome> biome = stableBiomes[x][z];
        int targetSurfaceY = Mth.clamp(targetHeights[x][z], minY + 4, maxY - 1);

        if (biome.equals(WHEAT_PLAIN)) {
          processWheatPlainColumn(chunk, x, z, originalSurfaces, targetSurfaceY);
        } else if (biome.equals(MELON_JUNGLE)) {
          processMelonJungleColumn(chunk, x, z, originalSurfaces, targetSurfaceY);
        } else if (biome.equals(LAKE_CENTER_ISLAND)) {
          processLakeCenterIslandColumn(chunk, x, z, originalSurfaces, targetSurfaceY);
        } else if (biome.equals(BIG_LAKE)) {
          processBigLakeColumn(chunk, x, z);
        } else if (biome.equals(PUMPKIN_GORGE)) {
          processPumpkinGorgeColumn(chunk, x, z, originalSurfaces, targetSurfaceY);
        }
      }
    }
  }

  private int findSurfaceY(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int minY,
      int maxY
  ) {
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
        chunk.getPos().getMinBlockX() + localX, 0, chunk.getPos().getMinBlockZ() + localZ
    );

    for (int y = maxY; y >= minY; y--) {
      pos.setY(y);
      BlockState state = chunk.getBlockState(pos);
      if (!state.isAir() && !state.is(Blocks.WATER)) return y;
    }
    return minY;
  }

  private void computeStableBiomesAndTargetHeights(
      long seed,
      WorldGenRegion region,
      RandomState noiseConfig,
      ChunkAccess chunk,
      int[][] originalSurfaces16,
      ResourceKey<Biome>[][] outStableBiomes16,
      int[][] outTargetHeights16
  ) {
    computeInitialTargetHeights(
        seed, region, noiseConfig, chunk, originalSurfaces16, outStableBiomes16, outTargetHeights16
    );
  }

  private void processWheatPlainColumn(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int[][] originalSurfaces,
      int targetSurfaceY
  ) {
    int maxY = chunk.getMaxY();
    int worldX = chunk.getPos().getMinBlockX() + localX;
    int worldZ = chunk.getPos().getMinBlockZ() + localZ;
    boolean treeClearing = isInTreeClearing(worldX, worldZ);

    BlockState topLandState = treeClearing
        ? SupremeBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState()
        : SupremeBlocks.EVER_WATER_FARMLAND.defaultBlockState();
    BlockState soil = SupremeBlocks.EVER_WATER_SOIL.defaultBlockState();

    int originalSurfaceY = originalSurfaces[localX][localZ];

    if (originalSurfaceY > targetSurfaceY) {
      BlockState air = Blocks.AIR.defaultBlockState();
      for (int y = targetSurfaceY + 1; y <= originalSurfaceY + 2; y++) {
        setBlockInChunk(chunk, localX, y, localZ, air);
      }
    } else if (originalSurfaceY < targetSurfaceY) {
      for (int y = originalSurfaceY + 1; y < targetSurfaceY; y++) {
        setBlockInChunk(chunk, localX, y, localZ, soil);
      }
    }

    setBlockInChunk(chunk, localX, targetSurfaceY, localZ, topLandState);
    setBlockInChunk(chunk, localX, targetSurfaceY - 1, localZ, soil);
    setBlockInChunk(chunk, localX, targetSurfaceY - 2, localZ, soil);
    setBlockInChunk(chunk, localX, targetSurfaceY - 3, localZ, soil);

    enforceNoWaterColumn(chunk, localX, localZ, topLandState, soil);

    int farmlandY = findFarmlandY(chunk, localX, localZ, maxY);
    if (farmlandY != -1 && !treeClearing) {
      int wheatY = farmlandY + 1;
      BlockState wheat = Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7);
      setBlockInChunk(chunk, localX, wheatY, localZ, wheat);
    }
  }

  private void processMelonJungleColumn(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int[][] originalSurfaces,
      int targetSurfaceY
  ) {
    int originalSurfaceY = originalSurfaces[localX][localZ];

    reshapeColumnHeight(
        chunk, localX, localZ,
        originalSurfaceY, targetSurfaceY,
        SupremeBlocks.EVER_WATER_SOIL.defaultBlockState(),
        Blocks.AIR.defaultBlockState()
    );

    enforceNoWaterColumn(
        chunk, localX, localZ,
        SupremeBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState(),
        SupremeBlocks.EVER_WATER_SOIL.defaultBlockState()
    );
  }

  private void processLakeCenterIslandColumn(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int[][] originalSurfaces,
      int targetSurfaceY
  ) {
    int originalSurfaceY = originalSurfaces[localX][localZ];
    BlockState top = SupremeBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState();
    BlockState soil = SupremeBlocks.EVER_WATER_SOIL.defaultBlockState();

    reshapeColumnHeight(
        chunk, localX, localZ,
        originalSurfaceY, targetSurfaceY,
        soil,
        Blocks.AIR.defaultBlockState()
    );

    setBlockInChunk(chunk, localX, targetSurfaceY, localZ, top);
    setBlockInChunk(chunk, localX, targetSurfaceY - 1, localZ, soil);
    setBlockInChunk(chunk, localX, targetSurfaceY - 2, localZ, soil);
    setBlockInChunk(chunk, localX, targetSurfaceY - 3, localZ, soil);

    enforceNoWaterColumn(chunk, localX, localZ, top, soil);
  }

  private void processBigLakeColumn(
      ChunkAccess chunk,
      int localX,
      int localZ
  ) {
    int minY = chunk.getMinY();
    int maxY = chunk.getMaxY();
    BlockState water = Blocks.WATER.defaultBlockState();
    BlockState air = Blocks.AIR.defaultBlockState();

    // Keep big_lake interior as a pure water body: no solids anywhere in the column.
    for (int y = minY; y <= SEA_LEVEL; y++) {
      setBlockInChunk(chunk, localX, y, localZ, water);
    }
    for (int y = SEA_LEVEL + 1; y <= maxY; y++) {
      setBlockInChunk(chunk, localX, y, localZ, air);
    }
  }

  private void processPumpkinGorgeColumn(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int[][] originalSurfaces,
      int targetSurfaceY
  ) {
    int surfaceY = originalSurfaces[localX][localZ];

    reshapeColumnHeight(
        chunk, localX, localZ, surfaceY, targetSurfaceY,
        Blocks.TERRACOTTA.defaultBlockState(), Blocks.AIR.defaultBlockState()
    );

    setBlockInChunk(chunk, localX, targetSurfaceY, localZ, Blocks.RED_SAND.defaultBlockState());
    setBlockInChunk(
        chunk,
        localX,
        targetSurfaceY - 1,
        localZ,
        Blocks.DYED_TERRACOTTA.orange().defaultBlockState()
    );
    setBlockInChunk(chunk, localX, targetSurfaceY - 2, localZ, Blocks.TERRACOTTA.defaultBlockState());
    setBlockInChunk(chunk, localX, targetSurfaceY - 3, localZ, Blocks.TERRACOTTA.defaultBlockState());

    enforceNoWaterColumn(
        chunk, localX, localZ,
        Blocks.RED_SAND.defaultBlockState(),
        Blocks.TERRACOTTA.defaultBlockState()
    );
  }

  private void computeInitialTargetHeights(
      long seed,
      WorldGenRegion region,
      RandomState noiseConfig,
      ChunkAccess chunk,
      int[][] originalSurfaces16,
      ResourceKey<Biome>[][] outBiomes16,
      int[][] outHeights16
  ) {
    NoiseBasedChunkGenerator gen = this.generator;

    int r = WINDOW_RADIUS;
    int size = 16 + 2 * r;

    int startX = chunk.getPos().getMinBlockX();
    int startZ = chunk.getPos().getMinBlockZ();

    @SuppressWarnings("unchecked")
    ResourceKey<Biome>[][] trackedBiome = new ResourceKey[size][size];

    var sampler = noiseConfig.sampler();
    for (int gx = 0; gx < size; gx++) {
      int wx = startX + (gx - r);
      int qx = wx >> 2;
      for (int gz = 0; gz < size; gz++) {
        int wz = startZ + (gz - r);
        int qz = wz >> 2;
        int qy = SEA_LEVEL >> 2;

        var entry = gen.getBiomeSource().getNoiseBiome(qx, qy, qz, sampler);
        ResourceKey<Biome> key = resolveTrackedBiome(entry);
        trackedBiome[gx][gz] = (key != null) ? key : WHEAT_PLAIN;
      }
    }

    Long2IntOpenHashMap heightCache = new Long2IntOpenHashMap(256);
    heightCache.defaultReturnValue(Integer.MIN_VALUE);

    BiomeDistanceFields distanceFields = new BiomeDistanceFields(
        computeDistancesToBiome(trackedBiome, WHEAT_PLAIN),
        computeDistancesToBiome(trackedBiome, MELON_JUNGLE),
        computeDistancesToBiome(trackedBiome, PUMPKIN_GORGE),
        computeDistancesToBiome(trackedBiome, BIG_LAKE),
        computeDistancesToBiome(trackedBiome, LAKE_CENTER_ISLAND)
    );

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        int gx = x + r;
        int gz = z + r;

        // Terrain ownership must match the biome source exactly on both sides of a boundary.
        // A locally voted biome shifts that boundary while neighboring samples remain raw,
        // which makes the height blend asymmetric.
        ResourceKey<Biome> stable = trackedBiome[gx][gz];
        outBiomes16[x][z] = stable;

        int worldX = startX + x;
        int worldZ = startZ + z;

        if (stable.equals(BIG_LAKE)) {
          outHeights16[x][z] = SEA_LEVEL;
          continue;
        }

        int interior = baseHeightForBiome(
            seed, gen, region, noiseConfig, stable, worldX, worldZ,
            startX, startZ, originalSurfaces16, heightCache
        );
        HarvestBoundaryGoal boundaryGoal = findHarvestBoundaryGoal(
            stable,
            seed,
            worldX,
            worldZ,
            gx,
            gz,
            distanceFields
        );

        outHeights16[x][z] = blendHarvestBoundaryHeight(
            stable, interior, boundaryGoal
        );
      }
    }
  }

  private boolean isInTreeClearing(
      int worldX,
      int worldZ
  ) {
    int gridX = Math.floorDiv(worldX, TREE_GRID_SIZE);
    int gridZ = Math.floorDiv(worldZ, TREE_GRID_SIZE);

    for (int gx = gridX - 1; gx <= gridX + 1; gx++) {
      for (int gz = gridZ - 1; gz <= gridZ + 1; gz++) {
        if (isNearTreeInGrid(worldX, worldZ, gx, gz)) return true;
      }
    }
    return false;
  }

  private void setBlockInChunk(
      ChunkAccess chunk,
      int x,
      int y,
      int z,
      BlockState state
  ) {
    if (y < chunk.getMinY() || y > chunk.getMaxY()) return;

    BlockPos pos = new BlockPos(
        chunk.getPos().getMinBlockX() + x,
        y,
        chunk.getPos().getMinBlockZ() + z
    );
    chunk.setBlockState(pos, state, 0);
  }

  private void enforceNoWaterColumn(
      ChunkAccess chunk,
      int localX,
      int localZ,
      BlockState topState,
      BlockState fillerState
  ) {
    int minY = chunk.getMinY();
    int maxY = chunk.getMaxY();
    int surfaceY = findSurfaceY(chunk, localX, localZ, minY, maxY);

    for (int y = surfaceY + 1; y <= maxY; y++) {
      BlockState current = getBlockFromChunk(chunk, localX, y, localZ);
      if (current.is(Blocks.WATER))
        setBlockInChunk(chunk, localX, y, localZ, Blocks.AIR.defaultBlockState());
    }
    for (int y = minY; y <= surfaceY; y++) {
      BlockState current = getBlockFromChunk(chunk, localX, y, localZ);
      if (current.is(Blocks.WATER)) setBlockInChunk(chunk, localX, y, localZ, fillerState);
    }

    if (surfaceY >= minY) {
      setBlockInChunk(chunk, localX, surfaceY, localZ, topState);
      setBlockInChunk(chunk, localX, surfaceY - 1, localZ, fillerState);
      setBlockInChunk(chunk, localX, surfaceY - 2, localZ, fillerState);
      setBlockInChunk(chunk, localX, surfaceY - 3, localZ, fillerState);
    }
  }

  private int findFarmlandY(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int maxY
  ) {
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
        chunk.getPos().getMinBlockX() + localX, 0, chunk.getPos().getMinBlockZ() + localZ
    );

    for (int y = maxY; y >= chunk.getMinY(); y--) {
      pos.setY(y);
      if (chunk.getBlockState(pos).is(SupremeBlocks.EVER_WATER_FARMLAND)) return y;
    }
    return -1;
  }

  private void reshapeColumnHeight(
      ChunkAccess chunk,
      int localX,
      int localZ,
      int originalSurfaceY,
      int targetSurfaceY,
      BlockState fillState,
      BlockState carveState
  ) {
    if (originalSurfaceY < targetSurfaceY) {
      for (int y = originalSurfaceY + 1; y <= targetSurfaceY; y++) {
        setBlockInChunk(chunk, localX, y, localZ, fillState);
      }
    } else if (originalSurfaceY > targetSurfaceY) {
      for (int y = targetSurfaceY + 1; y <= originalSurfaceY + 1; y++) {
        setBlockInChunk(chunk, localX, y, localZ, carveState);
      }
    }
  }

  private ResourceKey<Biome> resolveTrackedBiome(
      Holder<Biome> biomeEntry
  ) {
    if (biomeEntry.is(WHEAT_PLAIN)) return WHEAT_PLAIN;
    if (biomeEntry.is(MELON_JUNGLE)) return MELON_JUNGLE;
    if (biomeEntry.is(BIG_LAKE)) return BIG_LAKE;
    if (biomeEntry.is(LAKE_CENTER_ISLAND)) return LAKE_CENTER_ISLAND;
    if (biomeEntry.is(PUMPKIN_GORGE)) return PUMPKIN_GORGE;
    return null;
  }

  private int[][] computeDistancesToBiome(
      ResourceKey<Biome>[][] biomes,
      ResourceKey<Biome> biome
  ) {
    int sizeX = biomes.length;
    int sizeZ = biomes[0].length;
    int[][] distances = new int[sizeX][sizeZ];
    int[] queue = new int[sizeX * sizeZ];
    int head = 0;
    int tail = 0;

    for (int x = 0; x < sizeX; x++) {
      for (int z = 0; z < sizeZ; z++) {
        if (biomes[x][z].equals(biome)) {
          distances[x][z] = 0;
          queue[tail++] = x * sizeZ + z;
        } else {
          distances[x][z] = -1;
        }
      }
    }

    while (head < tail) {
      int current = queue[head++];
      int x = current / sizeZ;
      int z = current % sizeZ;
      int distance = distances[x][z];
      if (distance >= WINDOW_RADIUS) continue;

      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (dx == 0 && dz == 0) continue;

          int nextX = x + dx;
          int nextZ = z + dz;
          if (nextX < 0 || nextX >= sizeX || nextZ < 0 || nextZ >= sizeZ) continue;
          if (distances[nextX][nextZ] != -1) continue;

          distances[nextX][nextZ] = distance + 1;
          queue[tail++] = nextX * sizeZ + nextZ;
        }
      }
    }

    return distances;
  }

  private int baseHeightForBiome(
      long seed,
      NoiseBasedChunkGenerator gen,
      WorldGenRegion region,
      RandomState noiseConfig,
      ResourceKey<Biome> biome,
      int worldX,
      int worldZ,
      int chunkStartX,
      int chunkStartZ,
      int[][] originalSurfaces16,
      Long2IntOpenHashMap heightCache
  ) {
    if (biome.equals(BIG_LAKE)) return SEA_LEVEL;
    if (biome.equals(LAKE_CENTER_ISLAND)) return lakeCenterIslandHeight(seed, worldX, worldZ);
    if (biome.equals(PUMPKIN_GORGE)) return pumpkinGorgeFinalHeight(seed, worldX, worldZ);
    if (biome.equals(WHEAT_PLAIN)) return generateWheatPlainInteriorHeight(seed, worldX, worldZ);

    int localX = worldX - chunkStartX;
    int localZ = worldZ - chunkStartZ;
    if (localX >= 0 && localX < 16 && localZ >= 0 && localZ < 16) {
      return originalSurfaces16[localX][localZ];
    }
    return vanillaSurfaceHeightCached(gen, region, noiseConfig, worldX, worldZ, heightCache);
  }

  private HarvestBoundaryGoal findHarvestBoundaryGoal(
      ResourceKey<Biome> centerBiome,
      long seed,
      int worldX,
      int worldZ,
      int gridX,
      int gridZ,
      BiomeDistanceFields distanceFields
  ) {
    int blendRange = harvestBoundaryBlendRange(centerBiome);
    if (blendRange <= 0) return HarvestBoundaryGoal.NONE;

    int ownReferenceHeight = boundaryReferenceHeightForBiome(centerBiome, seed, worldX, worldZ);
    float totalWeight = 1.0f;
    float weightedHeight = ownReferenceHeight;
    float strongestInfluence = 0.0f;

    if (!centerBiome.equals(WHEAT_PLAIN)) {
      int distance = distanceFields.wheat()[gridX][gridZ];
      float referenceInfluence = boundaryReferenceInfluence(distance);
      weightedHeight += boundaryReferenceHeightForBiome(WHEAT_PLAIN, seed, worldX, worldZ) * referenceInfluence;
      totalWeight += referenceInfluence;
      strongestInfluence = Math.max(strongestInfluence, boundaryInfluence(distance, blendRange));
    }
    if (!centerBiome.equals(MELON_JUNGLE)) {
      int distance = distanceFields.melon()[gridX][gridZ];
      float referenceInfluence = boundaryReferenceInfluence(distance);
      weightedHeight += boundaryReferenceHeightForBiome(MELON_JUNGLE, seed, worldX, worldZ) * referenceInfluence;
      totalWeight += referenceInfluence;
      strongestInfluence = Math.max(strongestInfluence, boundaryInfluence(distance, blendRange));
    }
    if (!centerBiome.equals(PUMPKIN_GORGE)) {
      int distance = distanceFields.pumpkin()[gridX][gridZ];
      float referenceInfluence = boundaryReferenceInfluence(distance);
      weightedHeight += boundaryReferenceHeightForBiome(PUMPKIN_GORGE, seed, worldX, worldZ) * referenceInfluence;
      totalWeight += referenceInfluence;
      strongestInfluence = Math.max(strongestInfluence, boundaryInfluence(distance, blendRange));
    }
    if (!centerBiome.equals(BIG_LAKE)) {
      int distance = distanceFields.lake()[gridX][gridZ];
      float referenceInfluence = boundaryInfluence(distance, blendRange);
      weightedHeight += SEA_LEVEL * referenceInfluence;
      totalWeight += referenceInfluence;
      strongestInfluence = Math.max(strongestInfluence, boundaryInfluence(distance, blendRange));
    }
    if (!centerBiome.equals(LAKE_CENTER_ISLAND)) {
      int distance = distanceFields.island()[gridX][gridZ];
      float referenceInfluence = boundaryInfluence(distance, blendRange);
      weightedHeight += LAKE_CENTER_ISLAND_BASE_HEIGHT * referenceInfluence;
      totalWeight += referenceInfluence;
      strongestInfluence = Math.max(strongestInfluence, boundaryInfluence(distance, blendRange));
    }

    if (strongestInfluence <= 0.0f) return HarvestBoundaryGoal.NONE;
    return new HarvestBoundaryGoal(Math.round(weightedHeight / totalWeight), strongestInfluence);
  }

  private int blendHarvestBoundaryHeight(
      ResourceKey<Biome> biome,
      int interior,
      HarvestBoundaryGoal boundaryGoal
  ) {
    float boundaryFactor = boundaryGoal.boundaryFactor();
    if (boundaryFactor <= 0.0f) {
      return clampHarvestBoundaryHeight(biome, interior, 0.0f);
    }

    float t = 1.0f - boundaryFactor;
    float interiorWeight = t * t * (3.0f - 2.0f * t);
    int desired = Math.round(Mth.lerpInt(interiorWeight, boundaryGoal.height(), interior));
    return clampHarvestBoundaryHeight(biome, desired, boundaryFactor);
  }

  private boolean isNearTreeInGrid(
      int worldX,
      int worldZ,
      int gridX,
      int gridZ
  ) {
    long seed = gridX * 341873128712L + gridZ * 132897987541L;
    Random random = new Random(seed);
    random.nextFloat();

    int offsetX = random.nextInt(TREE_GRID_SIZE);
    int offsetZ = random.nextInt(TREE_GRID_SIZE);

    int treeCenterX = gridX * TREE_GRID_SIZE + offsetX;
    int treeCenterZ = gridZ * TREE_GRID_SIZE + offsetZ;

    int dx = worldX - treeCenterX;
    int dz = worldZ - treeCenterZ;

    return dx * dx + dz * dz <= TREE_CLEARING_RADIUS * TREE_CLEARING_RADIUS;
  }

  private BlockState getBlockFromChunk(
      ChunkAccess chunk,
      int localX,
      int y,
      int localZ
  ) {
    BlockPos pos = new BlockPos(
        chunk.getPos().getMinBlockX() + localX,
        y,
        chunk.getPos().getMinBlockZ() + localZ
    );
    return chunk.getBlockState(pos);
  }

  private int pumpkinGorgeFinalHeight(
      long seed,
      int worldX,
      int worldZ
  ) {
    int base = calculatePumpkinGorgeHeight(worldX, worldZ);
    float detail = computePumpkinGorgeDetail(seed, worldX, worldZ);
    int h = Math.round(base + detail);
    return Mth.clamp(h, PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT, PUMPKIN_GORGE_MAX_HEIGHT);
  }

  private int generateWheatPlainInteriorHeight(
      long seed,
      int worldX,
      int worldZ
  ) {
    double large = fbmPerlin(seed ^ 0x1A2B3C4D5E6F7890L, worldX, worldZ, 0.0026, 3) * 5.2;
    double medium = fbmPerlin(seed ^ 0x9876543210FEDCBAL, worldX, worldZ, 0.0100, 2) * 3.4;
    double micro = fbmPerlin(seed ^ 0xABCDEF0123456789L, worldX, worldZ, 0.0340, 2) * 1.35;

    int h = (int) Math.round(WHEAT_BASE + large + medium + micro);
    return Mth.clamp(h, WHEAT_INTERIOR_MIN_HEIGHT, WHEAT_INTERNAL_MAX);
  }

  private int vanillaSurfaceHeightCached(
      NoiseBasedChunkGenerator gen,
      WorldGenRegion region,
      RandomState noiseConfig,
      int worldX,
      int worldZ,
      Long2IntOpenHashMap cache
  ) {
    long key = (((long) worldX) << 32) ^ (worldZ & 0xffffffffL);
    int value = cache.get(key);
    if (value != Integer.MIN_VALUE) return value;

    int height = gen.getBaseHeight(worldX, worldZ, Heightmap.Types.WORLD_SURFACE_WG, region, noiseConfig);
    cache.put(key, height);
    return height;
  }

  private int harvestBoundaryBlendRange(
      ResourceKey<Biome> biome
  ) {
    if (biome.equals(WHEAT_PLAIN)) return WHEAT_BOUNDARY_BLEND_RANGE;
    if (biome.equals(MELON_JUNGLE)) return MELON_JUNGLE_BOUNDARY_BLEND_RANGE;
    if (biome.equals(PUMPKIN_GORGE)) return PUMPKIN_GORGE_BOUNDARY_BLEND_RANGE;
    return 0;
  }

  private int boundaryReferenceHeightForBiome(
      ResourceKey<Biome> biome,
      long seed,
      int worldX,
      int worldZ
  ) {
    if (biome.equals(BIG_LAKE)) return SEA_LEVEL;
    if (biome.equals(LAKE_CENTER_ISLAND)) return LAKE_CENTER_ISLAND_BASE_HEIGHT;
    if (biome.equals(WHEAT_PLAIN)) return WHEAT_BASE;
    if (biome.equals(PUMPKIN_GORGE)) return PUMPKIN_GORGE_BASE_HEIGHT;
    if (biome.equals(MELON_JUNGLE)) {
      double undulation = fbmPerlin(
          seed ^ 0xD1B54A32D192ED03L,
          worldX,
          worldZ,
          0.0025,
          2
      ) * 6.0D;
      return Math.round(MELON_JUNGLE_BOUNDARY_BASE_HEIGHT + (float) undulation);
    }
    return WHEAT_BASE;
  }

  /**
   * Computes the shared reference-height contribution for a nearby biome.
   *
   * <p>This is deliberately independent of the biome that owns the current
   * column.  At a three-biome junction, every column next to the same border
   * must agree on the third biome's contribution; using the owning biome's
   * 32/64/128 block range here gave each side a different target height and
   * produced a vertical seam.  The owning biome's range is still used by
   * {@link #boundaryInfluence(int, int)} above to control how far its actual
   * terrain transitions extend.</p>
   */
  private float boundaryReferenceInfluence(
      int distance
  ) {
    if (distance <= 0 || distance > WINDOW_RADIUS) return 0.0f;
    return boundaryFactorFromDist(distance, WINDOW_RADIUS);
  }

  private float boundaryInfluence(
      int distance,
      int blendRange
  ) {
    if (distance <= 0 || distance > blendRange) return 0.0f;
    return boundaryFactorFromDist(distance, blendRange);
  }

  private int clampHarvestBoundaryHeight(
      ResourceKey<Biome> biome,
      int height,
      float boundaryFactor
  ) {
    if (biome.equals(WHEAT_PLAIN)) return clampWheatAdaptive(height, boundaryFactor);
    if (biome.equals(LAKE_CENTER_ISLAND)) {
      return Mth.clamp(height, LAKE_CENTER_ISLAND_MIN_HEIGHT, LAKE_CENTER_ISLAND_MAX_HEIGHT);
    }
    if (biome.equals(BIG_LAKE)) return SEA_LEVEL;
    if (biome.equals(PUMPKIN_GORGE)) {
      return Mth.clamp(height, PUMPKIN_GORGE_BOUNDARY_MIN_HEIGHT, PUMPKIN_GORGE_MAX_HEIGHT);
    }
    return height;
  }

  private int calculatePumpkinGorgeHeight(
      int worldX,
      int worldZ
  ) {
    double macro = Math.sin(worldX * 0.035D) + Math.cos(worldZ * 0.032D);
    double ridges = Math.abs(Math.sin((worldX + worldZ) * 0.08D)) * 24.0D;
    double spikes = Math.abs(Math.sin(worldX * 0.19D) * Math.cos(worldZ * 0.17D)) * 14.0D;
    int target = (int) Math.round(PUMPKIN_GORGE_BASE_HEIGHT + macro * 6.0D + ridges + spikes);
    return Mth.clamp(target, PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT, PUMPKIN_GORGE_MAX_HEIGHT);
  }

  private float computePumpkinGorgeDetail(
      long seed,
      int worldX,
      int worldZ
  ) {
    double rough = fbmPerlin(seed ^ 0x3C6EF372FE94F82BL, worldX, worldZ, 0.040, 4);
    double ridged = 1.0 - Math.abs(rough);
    ridged = ridged * ridged;

    double spikes = fbmPerlin(seed ^ 0x510E527FADE682D1L, worldX, worldZ, 0.085, 3);

    return (float) ((ridged * 22.0 - 10.0) + spikes * 8.0);
  }

  private float boundaryFactorFromDist(
      int dist,
      int range
  ) {
    if (dist > range) return 0.0f;
    float t = 1.0f - (dist - 1) / (float) range;
    return Mth.clamp(t, 0.0f, 1.0f);
  }

  private int clampWheatAdaptive(
      int h,
      float boundaryFactor
  ) {
    int extra = Math.round(boundaryFactor * WHEAT_BOUNDARY_EXTRA_MAX);
    int max = WHEAT_INTERNAL_MAX + extra;
    return Mth.clamp(h, WHEAT_BOUNDARY_MIN_HEIGHT, max);
  }

  private record BiomeDistanceFields(
      int[][] wheat,
      int[][] melon,
      int[][] pumpkin,
      int[][] lake,
      int[][] island
  ) {}

  private record HarvestBoundaryGoal(
      int height,
      float boundaryFactor
  ) {

    private static final HarvestBoundaryGoal NONE = new HarvestBoundaryGoal(0, 0.0f);

  }

}
