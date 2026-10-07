/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.structure;

// Java Standard
import java.util.Optional;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.biome.HarvestContinentBiomeSource;
import roeyqian.magnatour.levelgen.terrain.HarvestContinentTerrain;
import roeyqian.magnatour.registry.worldgen.CustomStructures;

public final class GoldBellTowerStructure extends Structure {

  public static final MapCodec<GoldBellTowerStructure> CODEC =
      simpleCodec(GoldBellTowerStructure::new);

  private static final int CELL_SIZE = 128;
  private static final int LAND_MARGIN = 16;
  private static final int MAX_GROUND_HEIGHT_DIFFERENCE = 2;

  private static final long CELL_SELECTION_BUCKETS = 3L;

  private static final Identifier LOWER_TEMPLATE =
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "gold_bell_tower_1");
  private static final Identifier UPPER_TEMPLATE =
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "gold_bell_tower_2");

  public GoldBellTowerStructure(
      StructureSettings settings
  ) {
    super(settings);
  }

  @Override @NonNull
  public Optional<GenerationStub> findGenerationPoint(
      @NonNull GenerationContext context
  ) {
    ChunkPos chunkPos = context.chunkPos();

    int originX = (chunkPos.getMinBlockX() & ~15) + 8;
    int originZ = (chunkPos.getMinBlockZ() & ~15) + 8;

    long seed = context.seed();
    int gridX = Math.floorDiv(originX, CELL_SIZE);
    int gridZ = Math.floorDiv(originZ, CELL_SIZE);
    // Accept two of three hash buckets for a deterministic 2/3 selection rate.
    if (Long.remainderUnsigned(mix(seed, gridX, gridZ), CELL_SELECTION_BUCKETS) == 0L) {
      return Optional.empty();
    }

    Climate.Sampler sampler = context.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
    Holder<Biome> biome = context.biomeSource().createResolver(sampler).getNoiseBiome(
        originX >> 2,
        0,
        originZ >> 2
    );
    if (!context.validBiome().test(biome)) {
      return Optional.empty();
    }

    StructureTemplateManager templates = context.structureTemplateManager();
    Optional<StructureTemplate> lowerOpt = templates.get(LOWER_TEMPLATE);
    Optional<StructureTemplate> upperOpt = templates.get(UPPER_TEMPLATE);
    if (lowerOpt.isEmpty() || upperOpt.isEmpty()) {
      return Optional.empty();
    }

    Vec3i lowerSize = lowerOpt.get().getSize();
    Vec3i upperSize = upperOpt.get().getSize();

    int baseY = context.chunkGenerator().getBaseHeight(originX, originZ,
        Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

    BlockPos lowerPos = new BlockPos(
        originX - lowerSize.getX() / 2,
        baseY,
        originZ - lowerSize.getZ() / 2
    );
    int totalTopY = baseY + lowerSize.getY() + upperSize.getY();

    if (baseY < context.heightAccessor().getMinY()
        || totalTopY > context.heightAccessor().getMaxY()) {
      return Optional.empty();
    }

    if (!hasSuitableTerrain(context, sampler, lowerPos, lowerSize, upperSize)) {
      return Optional.empty();
    }

    GoldBellTowerPiece piece = new GoldBellTowerPiece(
        CustomStructures.GOLD_BELL_TOWER_PIECE,
        lowerPos,
        lowerSize,
        upperSize
    );

    return Optional.of(new GenerationStub(
        lowerPos,
        (StructurePiecesBuilder builder) -> builder.addPiece(piece)
    ));
  }

  @Override @NonNull
  public StructureType<?> type() {
    return CustomStructures.GOLD_BELL_TOWER;
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

  private static boolean hasSuitableTerrain(
      GenerationContext context,
      Climate.Sampler sampler,
      BlockPos lowerPos,
      Vec3i lowerSize,
      Vec3i upperSize
  ) {
    int minX = lowerPos.getX() - LAND_MARGIN;
    int minZ = lowerPos.getZ() - LAND_MARGIN;
    int maxX = lowerPos.getX() + Math.max(lowerSize.getX(), upperSize.getX()) - 1 + LAND_MARGIN;
    int maxZ = lowerPos.getZ() + Math.max(lowerSize.getZ(), upperSize.getZ()) - 1 + LAND_MARGIN;
    BiomeResolver resolver = context.biomeSource().createResolver(sampler);

    // Check every column, including the shore buffer, so narrow inlets cannot be missed.
    // Harvest's shared surface sampler avoids generating full cave columns for this check.
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        int groundHeight;
        if (context.biomeSource() instanceof HarvestContinentBiomeSource source) {
          HarvestContinentBiomeSource.SurfaceSample surface = source.sampleSurface(x, z, context.seed());
          if (!surface.biome().equals(HarvestContinentTerrain.LAKE_CENTER_ISLAND)) {
            return false;
          }
          groundHeight = surface.height() + 1;
        } else {
          if (!context.validBiome().test(resolver.getNoiseBiome(x >> 2, 0, z >> 2))) {
            return false;
          }
          groundHeight = context.chunkGenerator().getBaseHeight(x, z,
              Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        }
        if (groundHeight <= context.chunkGenerator().getSeaLevel() + 1
            || Math.abs(groundHeight - lowerPos.getY()) > MAX_GROUND_HEIGHT_DIFFERENCE) {
          return false;
        }
      }
    }
    return true;
  }

}
