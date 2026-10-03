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
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.levelgen.HarvestContinentTerrain;
import roeyqian.magnatour.levelgen.HarvestLakeIslands;

public final class HarvestContinentBiomeSource extends BiomeSource {

  // Retained as an optional salt when decoding existing dimension settings.
  private final long seed;

  public static final MapCodec<HarvestContinentBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
                  Biome.CODEC.fieldOf("wheat_plain").forGetter((source) -> source.wheatPlain),
                  Biome.CODEC.fieldOf("big_lake").forGetter((source) -> source.bigLake),
                  Biome.CODEC.fieldOf("melon_jungle").forGetter((source) -> source.melonJungle),
                  Biome.CODEC.fieldOf("pumpkin_gorge").forGetter((source) -> source.pumpkinGorge),
                  Codec.LONG.optionalFieldOf("seed", 0L).forGetter((source) -> source.seed),
                  Biome.CODEC.optionalFieldOf("lake_center_island")
                      .forGetter((source) -> Optional.of(source.lakeCenterIsland)),
                  RegistryOps.retrieveElement(HarvestContinentTerrain.LAKE_CENTER_ISLAND)
              )
              .apply(instance, (wheat, lake, melon, pumpkin, seed, island, defaultIsland) ->
                  new HarvestContinentBiomeSource(wheat, lake, melon, pumpkin,
                      island.orElse(defaultIsland), seed))
      );

  private volatile long worldSeed;

  private volatile HarvestLakeIslands islands;

  private final Holder<Biome> bigLake;
  private final Holder<Biome> lakeCenterIsland;
  private final Holder<Biome> melonJungle;
  private final Holder<Biome> pumpkinGorge;
  private final Holder<Biome> wheatPlain;

  public HarvestContinentBiomeSource(
      Holder<Biome> wheatPlain,
      Holder<Biome> bigLake,
      Holder<Biome> melonJungle,
      Holder<Biome> pumpkinGorge,
      Holder<Biome> lakeCenterIsland,
      long seed
  ) {
    this.wheatPlain = wheatPlain;
    this.bigLake = bigLake;
    this.melonJungle = melonJungle;
    this.pumpkinGorge = pumpkinGorge;
    this.lakeCenterIsland = lakeCenterIsland;
    this.seed = seed;
    this.islands = new HarvestLakeIslands(seed);
  }

  @Override @NonNull
  public BiomeResolver createResolver(
      Climate.Sampler sampler
  ) {
    return (x, y, z) -> getNoiseBiome(x, y, z, sampler);
  }

  @NonNull
  public Holder<Biome> getNoiseBiome(
      int x,
      int y,
      int z,
      Climate.@NonNull Sampler noise
  ) {
    double strange = sampleStrange(x * 4, z * 4);
    if (strange < HarvestContinentTerrain.LAKE_STRANGE_LIMIT) {
      HarvestLakeIslands.Island island = this.islands.at(x * 4, z * 4, strange);
      return island != null && island.distance(x * 4, z * 4) <= island.radius()
          ? this.lakeCenterIsland : this.bigLake;
    }
    if (strange < HarvestContinentTerrain.WHEAT_STRANGE_LIMIT) return this.wheatPlain;
    if (strange < HarvestContinentTerrain.MELON_STRANGE_LIMIT) return this.melonJungle;
    return this.pumpkinGorge;
  }

  /** Shared by biome selection and terrain blending, in block coordinates. */
  public double sampleStrange(
      int worldX,
      int worldZ
  ) {
    return HarvestContinentTerrain.sampleStrange(this.worldSeed ^ this.seed, worldX, worldZ);
  }

  /** Includes the special lake overlay without changing strange's four intervals. */
  public SurfaceSample sampleSurface(
      int x,
      int z,
      long terrainSeed
  ) {
    double strange = sampleStrange(x, z);
    ResourceKey<Biome> biome = HarvestContinentTerrain.biomeForStrange(strange);
    HarvestLakeIslands.Island island = this.islands.at(x, z, strange);
    if (island != null) {
      if (island.distance(x, z) <= island.radius()) biome = HarvestContinentTerrain.LAKE_CENTER_ISLAND;
      return new SurfaceSample(biome,
          HarvestContinentTerrain.islandSurfaceHeight(island, strange, terrainSeed, x, z));
    }
    return new SurfaceSample(biome,
        HarvestContinentTerrain.surfaceHeight(strange, terrainSeed, x, z));
  }

  /** Initialized by the generator before structure and biome generation. */
  public void setWorldSeed(
      long worldSeed
  ) {
    this.worldSeed = worldSeed;
    this.islands = new HarvestLakeIslands(worldSeed ^ this.seed);
  }

  @Override @NonNull
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.of(this.bigLake, this.lakeCenterIsland, this.wheatPlain, this.melonJungle, this.pumpkinGorge);
  }

  public record SurfaceSample(
      ResourceKey<Biome> biome,
      int height
  ) {}

}
