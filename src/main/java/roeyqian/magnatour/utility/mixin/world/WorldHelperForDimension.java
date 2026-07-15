/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.world;

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
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.level.RegDimensions;

public final class WorldHelperForDimension {

  private static final int BIOME_VOTE_STEP = 2;
  private static final int CENTER_BIOME_BONUS = 8;
  private static final int CONSTRAINT_PASSES_CENTER = 12;
  private static final int HARVEST_BOUNDARY_BLEND_RANGE = 20;
  private static final int HARVEST_BOUNDARY_MAX_ADJUST_FULL = 128;
  private static final int HARVEST_LAKE_BLEND_RANGE = 24;
  private static final int LOCAL_SAMPLE_RADIUS = 10;
  private static final int MAX_DELTA_DIFF_BIOME = 2;
  private static final int MAX_DELTA_GORGE_INTERNAL = 7;
  private static final int MAX_DELTA_LAKE_BORDER = 1;
  private static final int MAX_DELTA_MELON_INTERNAL = 4;
  private static final int MAX_DELTA_WHEAT_INTERNAL = 4;
  private static final int MAX_HEIGHT = 72;
  private static final int SEA_LEVEL = 63;
  private static final int LAKE_CENTER_ISLAND_MAX_HEIGHT = SEA_LEVEL + 6;
  private static final int LAKE_CENTER_ISLAND_MIN_HEIGHT = SEA_LEVEL + 1;
  private static final int TREE_CLEARING_RADIUS = 2;
  private static final int TREE_GRID_SIZE = 32;
  private static final int WHEAT_BASE = MAX_HEIGHT - 5;
  private static final int WHEAT_BOUNDARY_EXTRA_MAX = 96;
  private static final int WHEAT_INTERNAL_MAX = MAX_HEIGHT + 2;
  private static final int WHEAT_MIN_HEIGHT = SEA_LEVEL - 1;
  private static final int WINDOW_RADIUS = HARVEST_LAKE_BLEND_RANGE;

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

  private WorldHelperForDimension(
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
    new WorldHelperForDimension(generator).onBuildSurface(region, noiseConfig, chunk);
  }

  public static int lakeCenterIslandHeight(
      long seed,
      int worldX,
      int worldZ
  ) {
    double shape = fbmPerlin(seed ^ 0x6A09E667F3BCC909L, worldX, worldZ, 0.026, 2);
    int h = Math.round(SEA_LEVEL + 2 + (float) (shape * 1.5D));
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
    if (world.dimension() != RegDimensions.HARVEST_CONTINENT) return;

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
        seed, region, noiseConfig, chunk, originalSurfaces,
        stableBiomes, targetHeights
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
    float[][] boundaryMask = computeStableBiomesAndInitialTargetHeights(
        seed, region, noiseConfig, chunk,
        originalSurfaces16, outStableBiomes16, outTargetHeights16
    );

    applyHeightConstraints(outStableBiomes16, outTargetHeights16, boundaryMask);
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
        ? RegInsertBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState()
        : RegInsertBlocks.EVER_WATER_FARMLAND.defaultBlockState();
    BlockState soil = RegInsertBlocks.EVER_WATER_SOIL.defaultBlockState();

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
        RegInsertBlocks.EVER_WATER_SOIL.defaultBlockState(),
        Blocks.AIR.defaultBlockState()
    );

    enforceNoWaterColumn(
        chunk, localX, localZ,
        RegInsertBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState(),
        RegInsertBlocks.EVER_WATER_SOIL.defaultBlockState()
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
    BlockState top = RegInsertBlocks.EVER_WATER_GRASS_BLOCK.defaultBlockState();
    BlockState soil = RegInsertBlocks.EVER_WATER_SOIL.defaultBlockState();

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

  private float[][] computeStableBiomesAndInitialTargetHeights(
      long seed,
      WorldGenRegion region,
      RandomState noiseConfig,
      ChunkAccess chunk,
      int[][] originalSurfaces16,
      ResourceKey<Biome>[][] outStableBiomes16,
      int[][] outTargetHeights16
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
        int qy = MAX_HEIGHT >> 2;

        var entry = gen.getBiomeSource().getNoiseBiome(qx, qy, qz, sampler);
        ResourceKey<Biome> key = resolveTrackedBiome(entry);
        trackedBiome[gx][gz] = (key != null) ? key : WHEAT_PLAIN;
      }
    }

    Long2IntOpenHashMap heightCache = new Long2IntOpenHashMap(256);
    heightCache.defaultReturnValue(Integer.MIN_VALUE);

    float[][] boundaryMask = new float[16][16];

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        int gx = x + r;
        int gz = z + r;

        ResourceKey<Biome> stable = sampleStableBiomeFromGrid(trackedBiome, gx, gz);
        outStableBiomes16[x][z] = stable;

        int worldX = startX + x;
        int worldZ = startZ + z;

        if (stable.equals(BIG_LAKE)) {
          outTargetHeights16[x][z] = SEA_LEVEL;
          boundaryMask[x][z] = 0.0f;
          continue;
        }

        int interior = baseHeightForBiome(
            seed, gen, region, noiseConfig,
            stable, worldX, worldZ,
            startX, startZ, originalSurfaces16,
            heightCache
        );
        HarvestBoundaryGoal boundaryGoal = findNearestHarvestBoundaryGoal(
            seed, gen, region, noiseConfig, trackedBiome, stable,
            gx, gz, r, startX, startZ, originalSurfaces16,
            heightCache
        );

        outTargetHeights16[x][z] = blendHarvestBoundaryHeight(
            stable, interior, boundaryGoal
        );
        boundaryMask[x][z] = boundaryGoal.boundaryFactor();
      }
    }

    return boundaryMask;
  }

  private void applyHeightConstraints(
      ResourceKey<Biome>[][] outStableBiomes16,
      int[][] outTargetHeights16,
      float[][] boundaryMask
  ) {
    int[][] currentHeights = copyHeightGrid16(outTargetHeights16);
    int[][] nextHeights = new int[16][16];

    for (int pass = 0; pass < CONSTRAINT_PASSES_CENTER; pass++) {
      for (int x = 0; x < 16; x++) {
        for (int z = 0; z < 16; z++) {
          int constrained = currentHeights[x][z];
          if (boundaryMask[x][z] > 0.0f) {
            constrained = clampToNeighbors16(outStableBiomes16, currentHeights, x, z);
            constrained = clampHarvestBoundaryHeight(
                outStableBiomes16[x][z],
                constrained,
                boundaryMask[x][z]
            );
          }
          nextHeights[x][z] = constrained;
        }
      }

      int[][] swap = currentHeights;
      currentHeights = nextHeights;
      nextHeights = swap;
    }

    copyHeightGrid16(currentHeights, outTargetHeights16);
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
      if (chunk.getBlockState(pos).is(RegInsertBlocks.EVER_WATER_FARMLAND)) return y;
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

  private ResourceKey<Biome> sampleStableBiomeFromGrid(
      ResourceKey<Biome>[][] trackedBiome,
      int cx,
      int cz
  ) {
    ResourceKey<Biome> center = trackedBiome[cx][cz];

    int wheatVotes = 0;
    int melonVotes = 0;
    int lakeVotes = 0;
    int islandVotes = 0;
    int gorgeVotes = 0;

    if (center.equals(WHEAT_PLAIN)) wheatVotes += CENTER_BIOME_BONUS;
    else if (center.equals(MELON_JUNGLE)) melonVotes += CENTER_BIOME_BONUS;
    else if (center.equals(BIG_LAKE)) lakeVotes += CENTER_BIOME_BONUS;
    else if (center.equals(LAKE_CENTER_ISLAND)) islandVotes += CENTER_BIOME_BONUS;
    else if (center.equals(PUMPKIN_GORGE)) gorgeVotes += CENTER_BIOME_BONUS;

    for (int dx = -LOCAL_SAMPLE_RADIUS; dx <= LOCAL_SAMPLE_RADIUS; dx += BIOME_VOTE_STEP) {
      for (int dz = -LOCAL_SAMPLE_RADIUS; dz <= LOCAL_SAMPLE_RADIUS; dz += BIOME_VOTE_STEP) {
        int sx = cx + dx;
        int sz = cz + dz;
        if (sx < 0 || sx >= trackedBiome.length || sz < 0 || sz >= trackedBiome[0].length) continue;

        ResourceKey<Biome> b = trackedBiome[sx][sz];
        int weight = Math.max(1, 6 - (Math.abs(dx) + Math.abs(dz)) / 3);

        if (b.equals(WHEAT_PLAIN)) wheatVotes += weight;
        else if (b.equals(MELON_JUNGLE)) melonVotes += weight;
        else if (b.equals(BIG_LAKE)) lakeVotes += weight;
        else if (b.equals(LAKE_CENTER_ISLAND)) islandVotes += weight;
        else if (b.equals(PUMPKIN_GORGE)) gorgeVotes += weight;
      }
    }

    int maxVotes = Math.max(Math.max(wheatVotes, melonVotes), Math.max(Math.max(lakeVotes, islandVotes), gorgeVotes));
    if (maxVotes == 0) return center;

    int centerVotes;
    if (center.equals(WHEAT_PLAIN)) centerVotes = wheatVotes;
    else if (center.equals(MELON_JUNGLE)) centerVotes = melonVotes;
    else if (center.equals(BIG_LAKE)) centerVotes = lakeVotes;
    else if (center.equals(LAKE_CENTER_ISLAND)) centerVotes = islandVotes;
    else if (center.equals(PUMPKIN_GORGE)) centerVotes = gorgeVotes;
    else centerVotes = 0;

    if (centerVotes + 3 >= maxVotes) return center;

    if (gorgeVotes == maxVotes) return PUMPKIN_GORGE;
    if (melonVotes == maxVotes) return MELON_JUNGLE;
    if (islandVotes == maxVotes) return LAKE_CENTER_ISLAND;
    if (lakeVotes == maxVotes) return BIG_LAKE;
    return WHEAT_PLAIN;
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

    int lx = worldX - chunkStartX;
    int lz = worldZ - chunkStartZ;
    if (lx >= 0 && lx < 16 && lz >= 0 && lz < 16) {
      return originalSurfaces16[lx][lz];
    }
    return vanillaSurfaceHeightCached(gen, region, noiseConfig, worldX, worldZ, heightCache);
  }

  private HarvestBoundaryGoal findNearestHarvestBoundaryGoal(
      long seed,
      NoiseBasedChunkGenerator gen,
      WorldGenRegion region,
      RandomState noiseConfig,
      ResourceKey<Biome>[][] trackedBiome,
      ResourceKey<Biome> centerBiome,
      int cx,
      int cz,
      int windowRadius,
      int chunkStartX,
      int chunkStartZ,
      int[][] originalSurfaces16,
      Long2IntOpenHashMap heightCache
  ) {
    int sizeX = trackedBiome.length;
    int sizeZ = trackedBiome[0].length;

    for (int d = 1; d <= WINDOW_RADIUS; d++) {
      long sum = 0;
      int count = 0;
      boolean waterBoundary = false;
      float strongestBoundaryFactor = 0.0f;

      for (int dx = -d; dx <= d; dx++) {
        for (int dz = -d; dz <= d; dz++) {
          if (Math.max(Math.abs(dx), Math.abs(dz)) != d) continue;

          int gx = cx + dx;
          int gz = cz + dz;
          if (gx < 0 || gx >= sizeX || gz < 0 || gz >= sizeZ) continue;

          ResourceKey<Biome> nb = trackedBiome[gx][gz];
          if (nb.equals(centerBiome)) continue;

          int blendRange = harvestBoundaryBlendRange(centerBiome, nb);
          if (blendRange <= 0 || d > blendRange) continue;

          waterBoundary |= nb.equals(BIG_LAKE);

          int worldX = chunkStartX + (gx - windowRadius);
          int worldZ = chunkStartZ + (gz - windowRadius);

          int h = baseHeightForBiome(
              seed, gen, region, noiseConfig,
              nb, worldX, worldZ,
              chunkStartX, chunkStartZ, originalSurfaces16,
              heightCache
          );

          sum += h;
          count++;
          strongestBoundaryFactor = Math.max(strongestBoundaryFactor, boundaryFactorFromDist(d, blendRange));
        }
      }

      if (count > 0) {
        int goal = Math.round(sum / (float) count);
        return new HarvestBoundaryGoal(goal, strongestBoundaryFactor, waterBoundary);
      }
    }

    return HarvestBoundaryGoal.NONE;
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

    int goalHeight = boundaryGoal.waterBoundary()
        ? boundaryGoal.height()
        : Math.round((interior + boundaryGoal.height()) * 0.5f);

    float t = boundaryFactor * boundaryFactor * (3.0f - 2.0f * boundaryFactor);
    int desired = Math.round(Mth.lerpInt(t, interior, goalHeight));
    int cap = Math.round(boundaryFactor * HARVEST_BOUNDARY_MAX_ADJUST_FULL);
    desired = Mth.clamp(desired, interior - cap, interior + cap);
    return clampHarvestBoundaryHeight(biome, desired, boundaryFactor);
  }

  private int[][] copyHeightGrid16(
      int[][] source
  ) {
    int[][] copy = new int[16][16];
    copyHeightGrid16(source, copy);
    return copy;
  }

  private void copyHeightGrid16(
      int[][] source,
      int[][] target
  ) {
    for (int x = 0; x < 16; x++) {
      System.arraycopy(source[x], 0, target[x], 0, 16);
    }
  }

  private int clampToNeighbors16(
      ResourceKey<Biome>[][] biome,
      int[][] height,
      int x,
      int z
  ) {
    ResourceKey<Biome> center = biome[x][z];
    int candidate = height[x][z];

    int minAllowed = Integer.MIN_VALUE;
    int maxAllowed = Integer.MAX_VALUE;

    int[][] dirs = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    for (int[] d : dirs) {
      int nx = x + d[0];
      int nz = z + d[1];
      if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16) continue;

      int maxDelta = getPairMaxDelta(center, biome[nx][nz]);
      if (d[0] != 0 && d[1] != 0) maxDelta = Math.max(1, maxDelta - 1);

      minAllowed = Math.max(minAllowed, height[nx][nz] - maxDelta);
      maxAllowed = Math.min(maxAllowed, height[nx][nz] + maxDelta);
    }

    return Mth.clamp(candidate, minAllowed, maxAllowed);
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
    if (biome.equals(PUMPKIN_GORGE)) return Mth.clamp(height, SEA_LEVEL + 1, MAX_HEIGHT + 44);
    return height;
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
    return Mth.clamp(h, SEA_LEVEL + 2, MAX_HEIGHT + 44);
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
    return Mth.clamp(h, WHEAT_MIN_HEIGHT, WHEAT_INTERNAL_MAX);
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
    int v = cache.get(key);
    if (v != Integer.MIN_VALUE) return v;

    int h = gen.getBaseHeight(worldX, worldZ, Heightmap.Types.WORLD_SURFACE_WG, region, noiseConfig);
    cache.put(key, h);
    return h;
  }

  private int harvestBoundaryBlendRange(
      ResourceKey<Biome> centerBiome,
      ResourceKey<Biome> neighborBiome
  ) {
    if (centerBiome.equals(BIG_LAKE)) return 0;
    if (neighborBiome.equals(BIG_LAKE)) return HARVEST_LAKE_BLEND_RANGE;
    if (centerBiome.equals(LAKE_CENTER_ISLAND) || neighborBiome.equals(LAKE_CENTER_ISLAND)) {
      return HARVEST_LAKE_BLEND_RANGE;
    }
    return HARVEST_BOUNDARY_BLEND_RANGE;
  }

  private float boundaryFactorFromDist(
      int dist,
      int range
  ) {
    if (dist > range) return 0.0f;
    float t = 1.0f - (dist - 1) / (float) range;
    return Mth.clamp(t, 0.0f, 1.0f);
  }

  private int getPairMaxDelta(
      ResourceKey<Biome> a,
      ResourceKey<Biome> b
  ) {
    if ((a.equals(LAKE_CENTER_ISLAND) && b.equals(BIG_LAKE))
        || (a.equals(BIG_LAKE) && b.equals(LAKE_CENTER_ISLAND))) {
      return 3;
    }

    boolean lakePair = a.equals(BIG_LAKE) || b.equals(BIG_LAKE);
    if (lakePair) return MAX_DELTA_LAKE_BORDER;

    if (a.equals(LAKE_CENTER_ISLAND) && b.equals(LAKE_CENTER_ISLAND)) return 2;

    if (!a.equals(b)) return MAX_DELTA_DIFF_BIOME;

    if (a.equals(WHEAT_PLAIN)) return MAX_DELTA_WHEAT_INTERNAL;
    if (a.equals(MELON_JUNGLE)) return MAX_DELTA_MELON_INTERNAL;
    if (a.equals(PUMPKIN_GORGE)) return MAX_DELTA_GORGE_INTERNAL;
    if (a.equals(BIG_LAKE)) return 0;
    if (a.equals(LAKE_CENTER_ISLAND)) return 2;

    return 3;
  }

  private int clampWheatAdaptive(
      int h,
      float boundaryFactor
  ) {
    int extra = Math.round(boundaryFactor * WHEAT_BOUNDARY_EXTRA_MAX);
    int max = WHEAT_INTERNAL_MAX + extra;
    return Mth.clamp(h, WHEAT_MIN_HEIGHT, max);
  }

  private int calculatePumpkinGorgeHeight(
      int worldX,
      int worldZ
  ) {
    double macro = Math.sin(worldX * 0.035D) + Math.cos(worldZ * 0.032D);
    double ridges = Math.abs(Math.sin((worldX + worldZ) * 0.08D)) * 24.0D;
    double spikes = Math.abs(Math.sin(worldX * 0.19D) * Math.cos(worldZ * 0.17D)) * 14.0D;
    int target = (int) Math.round(SEA_LEVEL + 8 + macro * 6.0D + ridges + spikes);
    return Mth.clamp(target, SEA_LEVEL + 2, MAX_HEIGHT + 44);
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

  private record HarvestBoundaryGoal(
      int height,
      float boundaryFactor,
      boolean waterBoundary
  ) {

    private static final HarvestBoundaryGoal NONE = new HarvestBoundaryGoal(0, 0.0f, false);

  }

}
