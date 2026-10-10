/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.biome;

// Java Standard
import java.util.Optional;
import java.util.stream.Stream;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
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
  // Percentile intervals retain an approximately equal area share for all three biomes.
  public static final double FOREST_ORETOUR_LIMIT = ORETOUR_MAX / 3.0;
  public static final double PLAIN_ORETOUR_LIMIT = ORETOUR_MAX * 2.0 / 3.0;

  public static final ResourceKey<Biome> ORE_PLAIN = ResourceKey.create(
      Registries.BIOME, Identifier.fromNamespaceAndPath("magnatour", "ore_plain"));

  public static final MapCodec<OreContinentBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
              Biome.CODEC
                  .fieldOf("ore_land").forGetter((source) -> source.oreLand),
              Biome.CODEC
                  .fieldOf("ore_forest").forGetter((source) -> source.oreForest),
              Codec.LONG.optionalFieldOf("seed", 0L)
                  .forGetter((source) -> source.seed),
              Biome.CODEC.optionalFieldOf("ore_plain")
                  .forGetter((source) -> Optional.of(source.orePlain)),
              RegistryOps.retrieveElement(ORE_PLAIN)
          )
          .apply(instance, (land, forest, seed, plain, defaultPlain) ->
              new OreContinentBiomeSource(land, forest, plain.orElse(defaultPlain), seed))
      );

  private static final long ORETOUR_SEED_SALT = 0x3C6EF372FE94F82BL;

  // Retained as an optional salt when decoding existing dimension settings.
  private final long seed;

  private final Holder<Biome> oreForest;
  private final Holder<Biome> oreLand;
  private final Holder<Biome> orePlain;

  private volatile long worldSeed;

  public OreContinentBiomeSource(
      Holder<Biome> oreLand, Holder<Biome> oreForest, Holder<Biome> orePlain,
      long seed
  ) {
    super();
    this.oreLand = oreLand;
    this.oreForest = oreForest;
    this.orePlain = orePlain;
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
    if (oretour < FOREST_ORETOUR_LIMIT) return this.oreForest;
    return oretour < PLAIN_ORETOUR_LIMIT ? this.orePlain : this.oreLand;
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
    return Stream.of(this.oreLand, this.oreForest, this.orePlain);
  }

}
