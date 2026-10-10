/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.levelgen.terrain.PercentileBiomeField;

public final class OreContinentBiomeSource extends BiomeSource {

  public static final double ORETOUR_MAX = PercentileBiomeField.PARAMETER_MAX;
  // Percentile intervals retain an approximately equal area share for both biomes.
  public static final double FOREST_ORETOUR_LIMIT = ORETOUR_MAX / 2.0;

  public static final MapCodec<OreContinentBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
              Biome.CODEC
                  .fieldOf("ore_land").forGetter((source) -> source.oreLand),
              Biome.CODEC
                  .fieldOf("ore_forest").forGetter((source) -> source.oreForest),
              Codec.LONG.optionalFieldOf("seed", 0L)
                  .forGetter((source) -> source.seed)
          )
          .apply(instance, OreContinentBiomeSource::new)
      );

  private static final long ORETOUR_SEED_SALT = 0x3C6EF372FE94F82BL;

  // Retained as an optional salt when decoding existing dimension settings.
  private final long seed;

  private final Holder<Biome> oreForest;
  private final Holder<Biome> oreLand;

  private volatile long worldSeed;

  public OreContinentBiomeSource(
      Holder<Biome> oreLand, Holder<Biome> oreForest,
      long seed
  ) {
    super();
    this.oreLand = oreLand;
    this.oreForest = oreForest;
    this.seed = seed;
  }

  @Override @NonNull
  public BiomeResolver createResolver(
      Climate.@NonNull Sampler sampler
  ) {
    return (x, y, z) -> getNoiseBiome(x, y, z, sampler);
  }

  @NonNull
  public Holder<Biome> getNoiseBiome(
      int x, int y, int z,
      Climate.@NonNull Sampler noise
  ) {
    double oretour = sampleOretour(x * 4, z * 4);
    return oretour < FOREST_ORETOUR_LIMIT ? this.oreForest : this.oreLand;
  }

  /** Continuous 0-100 biome parameter sampled in block coordinates; independent of terrain. */
  public double sampleOretour(
      int worldX, int worldZ
  ) {
    return PercentileBiomeField.sample(this.worldSeed ^ this.seed ^ ORETOUR_SEED_SALT, worldX, worldZ);
  }

  public void setWorldSeed(
      long seed
  ) {
    this.worldSeed = seed;
  }

  @Override @NonNull
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.of(this.oreLand, this.oreForest);
  }

}
