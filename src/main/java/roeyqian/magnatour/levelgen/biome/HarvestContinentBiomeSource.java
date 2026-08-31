/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.biome;

// Java Standard
import java.util.stream.Stream;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

// JSpecify
import org.jspecify.annotations.NonNull;

public final class HarvestContinentBiomeSource extends BiomeSource {

  private static final int DEFAULT_CELL_SIZE = 1024;
  private static final int DEFAULT_ISLAND_CELL_SIZE = 256;
  private static final int DEFAULT_LAND_CELL_SIZE = 1024;

  public static final MapCodec<HarvestContinentBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
                  RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                      .fieldOf("wheat_plain").forGetter((source) -> source.wheatPlain),
                  RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                      .fieldOf("big_lake").forGetter((source) -> source.bigLake),
                  RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                      .fieldOf("lake_center_island").forGetter((source) -> source.lakeCenterIsland),
                  RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                      .fieldOf("melon_jungle").forGetter((source) -> source.melonJungle),
                  RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                      .fieldOf("pumpkin_gorge").forGetter((source) -> source.pumpkinGorge),
                  Codec.LONG.optionalFieldOf("seed", 0L)
                      .forGetter((source) -> source.seed),
                  Codec.INT.optionalFieldOf("cell_size", DEFAULT_CELL_SIZE)
                      .forGetter((source) -> source.cellSize),
                  Codec.INT.optionalFieldOf("land_cell_size", DEFAULT_LAND_CELL_SIZE)
                      .forGetter((source) -> source.landCellSize),
                  Codec.INT.optionalFieldOf("island_cell_size", DEFAULT_ISLAND_CELL_SIZE)
                      .forGetter((source) -> source.islandCellSize),
                  Codec.FLOAT.optionalFieldOf("jitter", 0.35F)
                      .forGetter((source) -> source.jitter)
              )
              .apply(instance, HarvestContinentBiomeSource::new)
      );

  /*
   * A harvest biome must always own a complete macro cell.  The previous
   * thresholded FBM approach could form arbitrarily small islands wherever a
   * noise value crossed a threshold.  A jittered Voronoi layout has a bounded
   * site distance instead, so accidental micro-biomes cannot occur.
   */
  private static final int MIN_CELL_SIZE = 256;

  private static final long CELL_JITTER_X_SEED_SALT = 0xBB67AE8584CAA73BL;
  private static final long CELL_JITTER_Z_SEED_SALT = 0x3C6EF372FE94F82BL;
  private static final long CELL_TYPE_SEED_SALT = 0xA54FF53A5F1D36F1L;
  private static final long LAKE_ISLAND_RADIUS_SEED_SALT = 0x510E527FADE682D1L;
  private static final long LAKE_ISLAND_SEED_SALT = 0x6A09E667F3BCC909L;
  private static final long LAND_TYPE_SEED_SALT = 0x9B05688C2B3E6C1FL;

  private static final double CELL_JITTER_SCALE = 0.45D;
  private static final double LAKE_CHANCE = 0.16D;
  private static final double LAKE_ISLAND_CHANCE = 0.55D;
  private static final double MELON_CHANCE_THRESHOLD = 0.38D;
  private static final double PUMPKIN_CHANCE_THRESHOLD = -0.34D;

  private final int cellSize;
  private final int islandCellSize;
  private final int landCellSize;

  private final long seed;

  private final float jitter;

  private final Holder<Biome> bigLake;
  private final Holder<Biome> lakeCenterIsland;
  private final Holder<Biome> melonJungle;
  private final Holder<Biome> pumpkinGorge;
  private final Holder<Biome> wheatPlain;

  public HarvestContinentBiomeSource(
      Holder<Biome> wheatPlain,
      Holder<Biome> bigLake,
      Holder<Biome> lakeCenterIsland,
      Holder<Biome> melonJungle,
      Holder<Biome> pumpkinGorge,
      long seed,
      int cellSize,
      int landCellSize,
      int islandCellSize,
      float jitter
  ) {
    super();
    this.wheatPlain = wheatPlain;
    this.bigLake = bigLake;
    this.lakeCenterIsland = lakeCenterIsland;
    this.melonJungle = melonJungle;
    this.pumpkinGorge = pumpkinGorge;

    this.seed = seed;
    this.cellSize = Math.max(MIN_CELL_SIZE, Math.max(cellSize, landCellSize));
    this.landCellSize = Math.max(MIN_CELL_SIZE, landCellSize);
    this.islandCellSize = Math.max(64, islandCellSize);
    this.jitter = Mth.clamp(jitter, 0.0F, 0.48F);
  }

  @Override @NonNull
  public Holder<Biome> getNoiseBiome(
      int x,
      int y,
      int z,
      Climate.@NonNull Sampler noise
  ) {
    double worldX = x * 4.0D;
    double worldZ = z * 4.0D;

    MacroCell cell = nearestCell(worldX, worldZ);
    if (cell.type() == MacroBiome.LAKE) {
      if (hasLakeCenterIsland(cell, worldX, worldZ)) return this.lakeCenterIsland;
      return this.bigLake;
    }

    return switch (cell.type()) {
      case MELON -> this.melonJungle;
      case PUMPKIN -> this.pumpkinGorge;
      case WHEAT -> this.wheatPlain;
      case LAKE -> throw new IllegalStateException("Lake cell was not handled");
    };
  }

  @Override @NonNull
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.of(this.wheatPlain, this.bigLake, this.lakeCenterIsland, this.melonJungle, this.pumpkinGorge);
  }

  private static long mix(
      long seed,
      int x,
      int z
  ) {
    long h = seed;
    h ^= (long) x * 0x9E3779B97F4A7C15L;
    h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
    h ^= h >>> 27;
    h *= 0x3C79AC492BA7B653L;
    h ^= h >>> 33;
    h *= 0x1C69B3F74AC4AE35L;
    h ^= h >>> 27;
    return h;
  }

  private static double unit(
      long value
  ) {
    return ((value >>> 11) * 0x1.0p-53);
  }

  private static double signedUnit(
      long value
  ) {
    return ((value & 0xFFFFL) / 65535.0D) * 2.0D - 1.0D;
  }

  private MacroCell nearestCell(
      double worldX,
      double worldZ
  ) {
    int baseX = Math.floorDiv(Mth.floor(worldX), this.cellSize);
    int baseZ = Math.floorDiv(Mth.floor(worldZ), this.cellSize);
    MacroCell closest = null;
    double closestDistanceSq = Double.MAX_VALUE;

    for (int gridX = baseX - 1; gridX <= baseX + 1; gridX++) {
      for (int gridZ = baseZ - 1; gridZ <= baseZ + 1; gridZ++) {
        MacroCell candidate = createCell(gridX, gridZ);
        double dx = worldX - candidate.centerX();
        double dz = worldZ - candidate.centerZ();
        double distanceSq = dx * dx + dz * dz;
        if (distanceSq < closestDistanceSq) {
          closest = candidate;
          closestDistanceSq = distanceSq;
        }
      }
    }

    if (closest == null) throw new IllegalStateException("No macro cell was sampled");
    return closest;
  }

  private boolean hasLakeCenterIsland(
      MacroCell cell,
      double worldX,
      double worldZ
  ) {
    long islandSeed = mix(this.seed ^ LAKE_ISLAND_SEED_SALT, cell.gridX(), cell.gridZ());
    if (unit(islandSeed) >= LAKE_ISLAND_CHANCE) return false;

    int maxRadius = Math.max(64, this.cellSize / 4);
    int baseRadius = Mth.clamp(this.islandCellSize, 64, maxRadius);
    double radiusScale = 1.0D + unit(
        mix(this.seed ^ LAKE_ISLAND_RADIUS_SEED_SALT, cell.gridX(), cell.gridZ())
    ) * 0.35D;
    double radius = Math.min(maxRadius, baseRadius * radiusScale);
    double dx = worldX - cell.centerX();
    double dz = worldZ - cell.centerZ();
    return dx * dx + dz * dz <= radius * radius;
  }

  private MacroCell createCell(
      int gridX,
      int gridZ
  ) {
    double jitterAmount = this.cellSize * this.jitter * CELL_JITTER_SCALE;
    double centerX = (gridX + 0.5D) * this.cellSize
        + signedUnit(mix(this.seed ^ CELL_JITTER_X_SEED_SALT, gridX, gridZ)) * jitterAmount;
    double centerZ = (gridZ + 0.5D) * this.cellSize
        + signedUnit(mix(this.seed ^ CELL_JITTER_Z_SEED_SALT, gridX, gridZ)) * jitterAmount;
    return new MacroCell(gridX, gridZ, centerX, centerZ, pickMacroBiome(gridX, gridZ));
  }

  private MacroBiome pickMacroBiome(
      int gridX,
      int gridZ
  ) {
    if (unit(mix(this.seed ^ CELL_TYPE_SEED_SALT, gridX, gridZ)) < LAKE_CHANCE) {
      return MacroBiome.LAKE;
    }

    double landValue = signedUnit(mix(this.seed ^ LAND_TYPE_SEED_SALT, gridX, gridZ));
    if (landValue > MELON_CHANCE_THRESHOLD) return MacroBiome.MELON;
    if (landValue < PUMPKIN_CHANCE_THRESHOLD) return MacroBiome.PUMPKIN;
    return MacroBiome.WHEAT;
  }

  private enum MacroBiome {
    LAKE,
    MELON,
    PUMPKIN,
    WHEAT
  }

  private record MacroCell(
      int gridX,
      int gridZ,
      double centerX,
      double centerZ,
      MacroBiome type
  ) {}

}
