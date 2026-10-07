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
import roeyqian.magnatour.levelgen.terrain.HarvestContinentTerrain;
import roeyqian.magnatour.levelgen.terrain.HarvestLakeIslands;

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
                  Biome.CODEC.optionalFieldOf("golden_summit").forGetter(source -> Optional.of(source.goldenSummit)),
                  Biome.CODEC.optionalFieldOf("sugarcane_marsh").forGetter(source -> Optional.of(source.sugarcaneMarsh)),
                  Biome.CODEC.optionalFieldOf("cactus_desert").forGetter(source -> Optional.of(source.cactusDesert)),
                  Biome.CODEC.optionalFieldOf("frost_snowfield").forGetter(source -> Optional.of(source.frostSnowfield)),
                  RegistryOps.retrieveElement(HarvestContinentTerrain.LAKE_CENTER_ISLAND),
                  RegistryOps.retrieveElement(HarvestContinentTerrain.GOLDEN_SUMMIT),
                  RegistryOps.retrieveElement(HarvestContinentTerrain.SUGARCANE_MARSH),
                  RegistryOps.retrieveElement(HarvestContinentTerrain.CACTUS_DESERT),
                  RegistryOps.retrieveElement(HarvestContinentTerrain.FROST_SNOWFIELD)
              )
              .apply(instance, (wheat, lake, melon, pumpkin, seed, island, summit, marsh, desert, snowfield, defaultIsland, defaultSummit, defaultMarsh, defaultDesert, defaultSnowfield) ->
                  new HarvestContinentBiomeSource(wheat, lake, melon, pumpkin,
                      island.orElse(defaultIsland), summit.orElse(defaultSummit), marsh.orElse(defaultMarsh),
                      desert.orElse(defaultDesert), snowfield.orElse(defaultSnowfield), seed))
      );

  private volatile long worldSeed;

  private volatile HarvestLakeIslands islands;

  private final Holder<Biome> bigLake;
  private final Holder<Biome> cactusDesert;
  private final Holder<Biome> frostSnowfield;
  private final Holder<Biome> goldenSummit;
  private final Holder<Biome> lakeCenterIsland;
  private final Holder<Biome> melonJungle;
  private final Holder<Biome> pumpkinGorge;
  private final Holder<Biome> sugarcaneMarsh;
  private final Holder<Biome> wheatPlain;

  public HarvestContinentBiomeSource(
      Holder<Biome> wheatPlain,
      Holder<Biome> bigLake,
      Holder<Biome> melonJungle,
      Holder<Biome> pumpkinGorge,
      Holder<Biome> lakeCenterIsland,
      Holder<Biome> goldenSummit,
      Holder<Biome> sugarcaneMarsh,
      Holder<Biome> cactusDesert,
      Holder<Biome> frostSnowfield,
      long seed
  ) {
    this.goldenSummit = goldenSummit;
    this.sugarcaneMarsh = sugarcaneMarsh;
    this.cactusDesert = cactusDesert;
    this.frostSnowfield = frostSnowfield;
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
      Climate.@NonNull Sampler sampler
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
    double harvestour = sampleHarvestour(x * 4, z * 4);
    if (harvestour < HarvestContinentTerrain.LAKE_HARVESTOUR_LIMIT) {
      HarvestLakeIslands.Island island = this.islands.at(x * 4, z * 4, harvestour);
      return island != null && island.distance(x * 4, z * 4) <= island.radius()
          ? this.lakeCenterIsland : this.bigLake;
    }
    ResourceKey<Biome> biome = HarvestContinentTerrain.biomeForHarvestour(harvestour);
    if (biome.equals(HarvestContinentTerrain.WHEAT_PLAIN)) return this.wheatPlain;
    if (biome.equals(HarvestContinentTerrain.MELON_JUNGLE)) return this.melonJungle;
    if (biome.equals(HarvestContinentTerrain.PUMPKIN_GORGE)) return this.pumpkinGorge;
    if (biome.equals(HarvestContinentTerrain.FROST_SNOWFIELD)) return this.frostSnowfield;
    if (biome.equals(HarvestContinentTerrain.SUGARCANE_MARSH)) return this.sugarcaneMarsh;
    if (biome.equals(HarvestContinentTerrain.CACTUS_DESERT)) return this.cactusDesert;
    return this.goldenSummit;
  }

  /** Shared by biome selection and terrain blending, in block coordinates. */
  public double sampleHarvestour(
      int worldX,
      int worldZ
  ) {
    return HarvestContinentTerrain.sampleHarvestour(this.worldSeed ^ this.seed, worldX, worldZ);
  }

  /** Biome ownership and blended terrain use the same harvestour intervals. */
  public SurfaceSample sampleSurface(
      int x,
      int z,
      long terrainSeed
  ) {
    double harvestour = sampleHarvestour(x, z);
    ResourceKey<Biome> biome = HarvestContinentTerrain.biomeForHarvestour(harvestour);
    HarvestLakeIslands.Island island = this.islands.at(x, z, harvestour);
    if (island != null) {
      if (island.distance(x, z) <= island.radius()) biome = HarvestContinentTerrain.LAKE_CENTER_ISLAND;
      return new SurfaceSample(biome,
          HarvestContinentTerrain.islandSurfaceHeight(island, harvestour, terrainSeed, x, z), harvestour, 0.0);
    }
    return new SurfaceSample(biome,
        HarvestContinentTerrain.surfaceHeight(harvestour, terrainSeed, x, z), harvestour,
        HarvestContinentTerrain.melonBlendWeight(harvestour));
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
    return Stream.of(this.bigLake, this.lakeCenterIsland, this.wheatPlain, this.melonJungle, this.pumpkinGorge,
        this.goldenSummit, this.sugarcaneMarsh, this.cactusDesert, this.frostSnowfield);
  }

  public record SurfaceSample(
      ResourceKey<Biome> biome,
      int height,
      double harvestour,
      double melonWeight
  ) {}

}
