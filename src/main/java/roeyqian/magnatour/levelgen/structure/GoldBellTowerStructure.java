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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.mixinhelper.world.WorldHelperForDimension;
import roeyqian.magnatour.registry.worldgen.RegStructures;

public final class GoldBellTowerStructure extends Structure {

  public static final MapCodec<GoldBellTowerStructure> CODEC =
      simpleCodec(GoldBellTowerStructure::new);

  private static final int CELL_SIZE = 128;

  private static final long CELL_SELECTION_MASK = 1L;

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
      GenerationContext context
  ) {
    ChunkPos chunkPos = context.chunkPos();

    int originX = (chunkPos.getMinBlockX() & ~15) + 8;
    int originZ = (chunkPos.getMinBlockZ() & ~15) + 8;

    long seed = context.seed();
    int gridX = Math.floorDiv(originX, CELL_SIZE);
    int gridZ = Math.floorDiv(originZ, CELL_SIZE);
    if ((mix(seed, gridX, gridZ) & CELL_SELECTION_MASK) != 0L) {
      return Optional.empty();
    }

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

    StructureTemplateManager templates = context.structureTemplateManager();
    Optional<StructureTemplate> lowerOpt = templates.get(LOWER_TEMPLATE);
    Optional<StructureTemplate> upperOpt = templates.get(UPPER_TEMPLATE);
    if (lowerOpt.isEmpty() || upperOpt.isEmpty()) {
      return Optional.empty();
    }

    Vec3i lowerSize = lowerOpt.get().getSize();
    Vec3i upperSize = upperOpt.get().getSize();

    int surfaceY = WorldHelperForDimension.lakeCenterIslandHeight(
        seed, originX, originZ
    );

    int baseY = surfaceY + 1;

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

    GoldBellTowerPiece piece = new GoldBellTowerPiece(
        RegStructures.GOLD_BELL_TOWER_PIECE,
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
    return RegStructures.GOLD_BELL_TOWER;
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

}
