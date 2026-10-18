/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.stuff.MagnaBlocks;

public final class StructureUniverseMetaOriginPiece extends StructurePiece {

  private static final int BORDER_THICKNESS = 8;
  private static final int CUBE_MAX = 31;
  private static final int CUBE_MIN = -32;

  public StructureUniverseMetaOriginPiece(
      StructurePieceType type
  ) {
    super(type, 0, new BoundingBox(
        CUBE_MIN, CUBE_MIN, CUBE_MIN, CUBE_MAX, CUBE_MAX, CUBE_MAX
    ));
  }

  public StructureUniverseMetaOriginPiece(
      StructurePieceType type,
      CompoundTag tag
  ) {
    super(type, tag);
  }

  @Override
  public void postProcess(
      @NonNull WorldGenLevel level,
      @NonNull StructureManager structureManager,
      @NonNull ChunkGenerator generator,
      @NonNull RandomSource random,
      @NonNull BoundingBox box,
      @NonNull ChunkPos chunkPos,
      @NonNull BlockPos pivot
  ) {
    // Generate only this chunk's intersection so neighboring chunks can finish independently.
    int minX = Math.max(this.boundingBox.minX(), box.minX());
    int minY = Math.max(this.boundingBox.minY(), box.minY());
    int minZ = Math.max(this.boundingBox.minZ(), box.minZ());
    int maxX = Math.min(this.boundingBox.maxX(), box.maxX());
    int maxY = Math.min(this.boundingBox.maxY(), box.maxY());
    int maxZ = Math.min(this.boundingBox.maxZ(), box.maxZ());
    BlockState dark = MagnaBlocks.UNIVERSE_DARK_BLOCK.defaultBlockState();
    BlockState light = MagnaBlocks.UNIVERSE_LIGHT_BLOCK.defaultBlockState();
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          int boundaryAxes = (isNearBoundary(x) ? 1 : 0)
              + (isNearBoundary(y) ? 1 : 0)
              + (isNearBoundary(z) ? 1 : 0);
          level.setBlock(pos.set(x, y, z), boundaryAxes >= 2 ? light : dark, 2);
        }
      }
    }
  }

  @Override
  protected void addAdditionalSaveData(
      @NonNull StructurePieceSerializationContext context,
      @NonNull CompoundTag tag
  ) {
    // The superclass persists the bounding box; the cube has no additional mutable state.
  }

  private static boolean isNearBoundary(
      int coordinate
  ) {
    return coordinate < CUBE_MIN + BORDER_THICKNESS
        || coordinate > CUBE_MAX - BORDER_THICKNESS;
  }

}
