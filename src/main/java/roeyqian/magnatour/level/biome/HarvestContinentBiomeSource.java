/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.biome;

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

  private static final int SHAPE_OCTAVES = 2;
  private static final int WARP_OCTAVES = 2;

  private static final long DOMAIN_WARP_X_SEED_SALT = 0xBB67AE8584CAA73BL;
  private static final long DOMAIN_WARP_Z_SEED_SALT = 0x3C6EF372FE94F82BL;
  private static final long ISLAND_SHAPE_SEED_SALT = 0xA54FF53A5F1D36F1L;
  private static final long LAKE_SHAPE_SEED_SALT = 0x9B05688C2B3E6C1FL;
  private static final long LAND_BIOME_SEED_SALT = 0x6A09E667F3BCC909L;

  private static final double DOMAIN_WARP_SCALE = 0.85D;
  private static final double ISLAND_THRESHOLD = 0.48D;
  private static final double LAKE_DOMAIN_WARP = 0.24D;
  private static final double LAKE_ISLAND_MIN_SCORE = 0.24D;
  private static final double LAKE_THRESHOLD = 0.07D;
  private static final double LAND_DOMAIN_WARP = 0.18D;
  private static final double MELON_SELECTOR_THRESHOLD = 0.28D;
  private static final double PUMPKIN_SELECTOR_THRESHOLD = -0.24D;

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
    this.cellSize = Math.max(32, cellSize);
    this.landCellSize = Math.max(32, landCellSize);
    this.islandCellSize = Math.max(32, islandCellSize);
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

    WarpedPoint lakePoint = warpPoint(this.seed, worldX, worldZ, this.cellSize, LAKE_DOMAIN_WARP);
    double lakeScore = lakeScore(lakePoint.x(), lakePoint.z());
    if (lakeScore > LAKE_THRESHOLD) {
      if (isLakeCenterIsland(lakePoint.x(), lakePoint.z(), lakeScore)) {
        return this.lakeCenterIsland;
      }

      return this.bigLake;
    }

    WarpedPoint landPoint = warpPoint(
        this.seed ^ LAND_BIOME_SEED_SALT,
        worldX, worldZ,
        this.landCellSize,
        LAND_DOMAIN_WARP
    );
    return pickLandBiome(landSelector(landPoint.x(), landPoint.z()));
  }

  @Override @NonNull
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.of(this.wheatPlain, this.bigLake, this.lakeCenterIsland, this.melonJungle, this.pumpkinGorge);
  }

  private static int fastFloor(
      double value
  ) {
    int i = (int) value;
    return value < i ? i - 1 : i;
  }

  private static double fade(
      double t
  ) {
    return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
  }

  private static double signedUnit(
      long value
  ) {
    return ((value & 0xFFFFL) / 65535.0D) * 2.0D - 1.0D;
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

  private static double lerp(
      double t,
      double a,
      double b
  ) {
    return a + t * (b - a);
  }

  private static double valueNoise(
      long seed,
      double x,
      double z
  ) {
    int x0 = fastFloor(x);
    int z0 = fastFloor(z);
    int x1 = x0 + 1;
    int z1 = z0 + 1;

    double tx = x - x0;
    double tz = z - z0;
    double u = fade(tx);
    double v = fade(tz);

    double n00 = signedUnit(mix(seed, x0, z0));
    double n10 = signedUnit(mix(seed, x1, z0));
    double n01 = signedUnit(mix(seed, x0, z1));
    double n11 = signedUnit(mix(seed, x1, z1));

    return lerp(v, lerp(u, n00, n10), lerp(u, n01, n11));
  }

  private static double fbmValue(
      long seed,
      double x,
      double z,
      double scale,
      int octaves
  ) {
    double amplitude = 1.0D;
    double frequency = scale;
    double sum = 0.0D;
    double norm = 0.0D;

    for (int i = 0; i < octaves; i++) {
      sum += amplitude * valueNoise(seed + (long) i * 0x9E3779B97F4A7C15L, x * frequency, z * frequency);
      norm += amplitude;
      amplitude *= 0.5D;
      frequency *= 2.0D;
    }

    return norm <= 0.0D ? 0.0D : Mth.clamp(sum / norm, -1.0D, 1.0D);
  }

  private WarpedPoint warpPoint(
      long seed,
      double x,
      double z,
      int featureSize,
      double amount
  ) {
    if (amount <= 0.0D) return new WarpedPoint(x, z);

    double scale = DOMAIN_WARP_SCALE / Math.max(32.0D, featureSize);
    double warpX = fbmValue(seed ^ DOMAIN_WARP_X_SEED_SALT, x, z, scale, WARP_OCTAVES);
    double warpZ = fbmValue(seed ^ DOMAIN_WARP_Z_SEED_SALT, x, z, scale, WARP_OCTAVES);
    double blocks = featureSize * amount * (0.5D + this.jitter);
    return new WarpedPoint(x + warpX * blocks, z + warpZ * blocks);
  }

  private double lakeScore(
      double worldX,
      double worldZ
  ) {
    double baseScale = 1.0D / this.cellSize;
    return fbmValue(this.seed ^ LAKE_SHAPE_SEED_SALT, worldX, worldZ, baseScale, SHAPE_OCTAVES);
  }

  private boolean isLakeCenterIsland(
      double worldX,
      double worldZ,
      double lakeScore
  ) {
    if (lakeScore < LAKE_ISLAND_MIN_SCORE) return false;

    double islandScale = 1.0D / Math.max(96.0D, this.islandCellSize * 1.25D);
    return fbmValue(this.seed ^ ISLAND_SHAPE_SEED_SALT, worldX, worldZ, islandScale, SHAPE_OCTAVES)
        > ISLAND_THRESHOLD;
  }

  private Holder<Biome> pickLandBiome(
      double selector
  ) {
    if (selector < PUMPKIN_SELECTOR_THRESHOLD) return this.pumpkinGorge;
    if (selector > MELON_SELECTOR_THRESHOLD) return this.melonJungle;
    return this.wheatPlain;
  }

  private double landSelector(
      double worldX,
      double worldZ
  ) {
    double baseScale = 1.0D / this.landCellSize;
    return fbmValue(this.seed ^ LAND_BIOME_SEED_SALT, worldX, worldZ, baseScale, SHAPE_OCTAVES);
  }

  private record WarpedPoint(
      double x,
      double z
  ) {}

}
