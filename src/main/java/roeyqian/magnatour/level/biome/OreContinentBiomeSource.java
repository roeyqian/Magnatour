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

public final class OreContinentBiomeSource extends BiomeSource {

  private static final int DEFAULT_CELL_SIZE = 512;

  public static final MapCodec<OreContinentBiomeSource> CODEC =
      RecordCodecBuilder.mapCodec((instance) -> instance.group(
              RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                  .fieldOf("ore_land").forGetter((source) -> source.oreLand),
              RegistryFileCodec.create(Registries.BIOME, Biome.DIRECT_CODEC)
                  .fieldOf("ore_forest").forGetter((source) -> source.oreForest),
              Codec.LONG.optionalFieldOf("seed", 0L)
                  .forGetter((source) -> source.seed),
              Codec.INT.optionalFieldOf("cell_size", DEFAULT_CELL_SIZE)
                  .forGetter((source) -> source.cellSize),
              Codec.FLOAT.optionalFieldOf("jitter", 0.35F)
                  .forGetter((source) -> source.jitter)
          )
          .apply(instance, OreContinentBiomeSource::new)
      );

  private static final long CELL_X_SEED_SALT = 0x3C6EF372FE94F82BL;
  private static final long CELL_Z_SEED_SALT = 0xBB67AE8584CAA73BL;

  private final int cellSize;

  private final long seed;

  private final float jitter;

  private final Holder<Biome> oreForest;
  private final Holder<Biome> oreLand;

  public OreContinentBiomeSource(
      Holder<Biome> oreLand,
      Holder<Biome> oreForest,
      long seed,
      int cellSize,
      float jitter
  ) {
    super();
    this.oreLand = oreLand;
    this.oreForest = oreForest;
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
    Cell cell = nearestCell(x * 4.0D, z * 4.0D);
    return pickBiome(cell.x(), cell.z());
  }

  @Override @NonNull
  protected MapCodec<? extends BiomeSource> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected Stream<Holder<Biome>> collectPossibleBiomes() {
    return Stream.of(this.oreLand, this.oreForest);
  }

  private static int fastFloor(
      double value
  ) {
    int i = (int) value;
    return value < i ? i - 1 : i;
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

  private Cell nearestCell(
      double worldX,
      double worldZ
  ) {
    int baseCellX = fastFloor(worldX / this.cellSize);
    int baseCellZ = fastFloor(worldZ / this.cellSize);

    int nearestX = baseCellX;
    int nearestZ = baseCellZ;
    double bestDistance = Double.MAX_VALUE;

    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        int cellX = baseCellX + dx;
        int cellZ = baseCellZ + dz;
        double centerX = cellCenter(cellX, cellZ, CELL_X_SEED_SALT, true);
        double centerZ = cellCenter(cellX, cellZ, CELL_Z_SEED_SALT, false);
        double distanceX = worldX - centerX;
        double distanceZ = worldZ - centerZ;
        double distance = distanceX * distanceX + distanceZ * distanceZ;
        if (distance < bestDistance) {
          bestDistance = distance;
          nearestX = cellX;
          nearestZ = cellZ;
        }
      }
    }

    return new Cell(nearestX, nearestZ);
  }

  private Holder<Biome> pickBiome(
      int cellX,
      int cellZ
  ) {
    long selector = Math.floorMod(mix(this.seed, cellX, cellZ), 3L);
    return selector == 0L ? this.oreForest : this.oreLand;
  }

  private double cellCenter(
      int cellX,
      int cellZ,
      long salt,
      boolean xAxis
  ) {
    long hash = mix(this.seed ^ salt, cellX, cellZ);
    long shifted = xAxis ? hash : hash >>> 16;
    double offset = signedUnit(shifted) * this.jitter;
    return (xAxis ? cellX : cellZ) * (double) this.cellSize
        + (0.5D + offset) * this.cellSize;
  }

  private record Cell(
      int x,
      int z
  ) {}

}
