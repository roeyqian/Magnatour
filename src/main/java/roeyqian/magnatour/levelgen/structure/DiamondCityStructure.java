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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.worldgen.CustomStructures;

public final class DiamondCityStructure extends Structure {

  public static final MapCodec<DiamondCityStructure> CODEC =
      simpleCodec(DiamondCityStructure::new);

  public DiamondCityStructure(
      StructureSettings settings
  ) {
    super(settings);
  }

  @Override @NonNull
  public Optional<GenerationStub> findGenerationPoint(
      @NonNull GenerationContext context
  ) {
    ChunkPos chunkPos = context.chunkPos();
    int originX = chunkPos.getMiddleBlockX();
    int originZ = chunkPos.getMiddleBlockZ();

    Climate.Sampler sampler = context.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
    Holder<Biome> biome = context.biomeSource().createResolver(sampler).getNoiseBiome(
        originX >> 2,
        0,
        originZ >> 2
    );
    if (!context.validBiome().test(biome)) {
      return Optional.empty();
    }

    long layoutSeed = DiamondCityLayout.seed(context.seed(), chunkPos.x(), chunkPos.z());
    Optional<DiamondCityLayout> layoutOpt = DiamondCityLayout.create(
        context.structureTemplateManager(),
        layoutSeed
    );
    if (layoutOpt.isEmpty()) {
      return Optional.empty();
    }

    Vec3i citySize = layoutOpt.get().citySize();
    // Sample the enlarged footprint, using bare ground rather than treetops.
    int minSurface = Integer.MAX_VALUE;
    int maxSurface = Integer.MIN_VALUE;
    for (int x = 0; x <= 9; x++) {
      for (int z = 0; z <= 9; z++) {
        int sampleX = originX - citySize.getX() / 2 + x * (citySize.getX() - 1) / 9;
        int sampleZ = originZ - citySize.getZ() / 2 + z * (citySize.getZ() - 1) / 9;
        int surface = context.chunkGenerator().getFirstOccupiedHeight(
            sampleX, sampleZ, Heightmap.Types.OCEAN_FLOOR_WG,
            context.heightAccessor(), context.randomState()
        );
        if (surface < context.chunkGenerator().getSeaLevel()) return Optional.empty();
        minSurface = Math.min(minSurface, surface);
        maxSurface = Math.max(maxSurface, surface);
        if (maxSurface - minSurface > 12) return Optional.empty();
      }
    }
    int baseY = maxSurface + 1;

    BlockPos cityPos = new BlockPos(
        originX - citySize.getX() / 2,
        baseY,
        originZ - citySize.getZ() / 2
    );

    if (baseY - DiamondCityLayout.FOUNDATION_DEPTH < context.heightAccessor().getMinY()
        || baseY + citySize.getY() > context.heightAccessor().getMaxY()) {
      return Optional.empty();
    }

    DiamondCityPiece piece = new DiamondCityPiece(
        CustomStructures.DIAMOND_CITY_PIECE,
        cityPos,
        citySize,
        layoutSeed
    );

    return Optional.of(new GenerationStub(
        cityPos,
        (StructurePiecesBuilder builder) -> builder.addPiece(piece)
    ));
  }

  @Override @NonNull
  public StructureType<?> type() {
    return CustomStructures.DIAMOND_CITY;
  }

}
