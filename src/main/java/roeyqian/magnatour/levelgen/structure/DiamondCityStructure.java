/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
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
      GenerationContext context
  ) {
    ChunkPos chunkPos = context.chunkPos();
    int originX = chunkPos.getMiddleBlockX();
    int originZ = chunkPos.getMiddleBlockZ();

    Climate.Sampler sampler = context.randomState().sampler();
    Holder<Biome> biome = context.biomeSource().getNoiseBiome(
        originX >> 2,
        0,
        originZ >> 2,
        sampler
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
    int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
        originX,
        originZ,
        Heightmap.Types.WORLD_SURFACE_WG,
        context.heightAccessor(),
        context.randomState()
    );
    int baseY = surfaceY + 1;

    BlockPos cityPos = new BlockPos(
        originX - citySize.getX() / 2,
        baseY,
        originZ - citySize.getZ() / 2
    );

    if (baseY < context.heightAccessor().getMinY()
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
