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
                  Codec.INT.optionalFieldOf("cell_size", 192)
                      .forGetter((source) -> source.cellSize),
                  Codec.FLOAT.optionalFieldOf("jitter", 0.35F)
                      .forGetter((source) -> source.jitter)
              )
              .apply(instance, HarvestContinentBiomeSource::new)
      );

  private static final float BIG_LAKE_WEIGHT = 0.33F;
  private static final float MELON_WEIGHT = 0.1375F;
  private static final float WHEAT_WEIGHT = 0.395F;

  private static final double LAKE_ISLAND_RADIUS = 0.18D;
  private static final double LAKE_ISLAND_RADIUS_VARIATION = 0.03D;

  private final int cellSize;

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
    this.jitter = Mth.clamp(jitter, 0.0F, 0.48F);
  }

  @Override @NonNull
  public Holder<Biome> getNoiseBiome(
      int x,
      int y,
      int z,
      Climate.@NonNull Sampler noise
  ) {
    int cellSizeInBiomeCoords = Math.max(1, this.cellSize >> 2);
    int cellX = Math.floorDiv(x, cellSizeInBiomeCoords);
    int cellZ = Math.floorDiv(z, cellSizeInBiomeCoords);

    double sampleX = x / (double) cellSizeInBiomeCoords;
    double sampleZ = z / (double) cellSizeInBiomeCoords;

    Holder<Biome> bestBiome = this.wheatPlain;
    double bestDistance = Double.MAX_VALUE;
    long bestHash = 0L;

    for (int offsetX = -1; offsetX <= 1; offsetX++) {
      for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
        int candidateX = cellX + offsetX;
        int candidateZ = cellZ + offsetZ;
        long hash = mix(this.seed, candidateX, candidateZ);

        double centerX = candidateX + signedUnit(hash >>> 24) * this.jitter;
        double centerZ = candidateZ + signedUnit(hash >>> 40) * this.jitter;

        double dx = sampleX - centerX;
        double dz = sampleZ - centerZ;
        double distance = dx * dx + dz * dz;

        if (distance < bestDistance) {
          bestDistance = distance;
          bestBiome = pickBiome(hash);
          bestHash = hash;
        }
      }
    }

    if (bestBiome == this.bigLake && isLakeCenterIsland(bestDistance, bestHash)) {
      return this.lakeCenterIsland;
    }

    return bestBiome;
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

  private static double signedUnit(
      long value
  ) {
    return ((value & 0xFFFFL) / 65535.0D) * 2.0D - 1.0D;
  }

  private Holder<Biome> pickBiome(
      long hash
  ) {
    float roll = ((hash >>> 8) & 0xFFFFL) / 65535.0F;

    if (roll < WHEAT_WEIGHT) return this.wheatPlain;
    if (roll < WHEAT_WEIGHT + BIG_LAKE_WEIGHT) return this.bigLake;
    if (roll < WHEAT_WEIGHT + BIG_LAKE_WEIGHT + MELON_WEIGHT) return this.melonJungle;
    return this.pumpkinGorge;
  }

  private boolean isLakeCenterIsland(
      double normalizedDistanceSquared,
      long hash
  ) {
    double radius = LAKE_ISLAND_RADIUS + (((hash >>> 56) & 0xFFL) / 255.0D) * LAKE_ISLAND_RADIUS_VARIATION;
    return normalizedDistanceSquared <= radius * radius;
  }

}
