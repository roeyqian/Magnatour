/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.structure;

// Java Standard
import java.util.Optional;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;

public final class DiamondCityPiece extends StructurePiece {

  private final long layoutSeed;

  private final Vec3i citySize;

  private final BlockPos cityPos;

  public DiamondCityPiece(
      StructurePieceType type,
      CompoundTag tag
  ) {
    super(type, tag);
    this.cityPos = readBlockPos(tag);
    this.citySize = readVec3i(tag, "CitySize");
    this.layoutSeed = tag.getLong("LayoutSeed").orElseGet(
        () -> DiamondCityLayout.seed(0L, this.cityPos.getX(), this.cityPos.getZ())
    );
  }

  public DiamondCityPiece(
      StructurePieceType type,
      BlockPos cityPos,
      Vec3i citySize,
      long layoutSeed
  ) {
    super(type, 0, makeBoundingBox(cityPos, citySize));
    this.cityPos = cityPos;
    this.citySize = citySize;
    this.layoutSeed = layoutSeed;
  }

  @Override
  public void postProcess(
      WorldGenLevel level,
      @NonNull StructureManager structureManager,
      @NonNull ChunkGenerator generator,
      @NonNull RandomSource random,
      @NonNull BoundingBox box,
      @NonNull ChunkPos chunkPos,
      @NonNull BlockPos pivot
  ) {
    StructureTemplateManager templateManager = level.getLevel().getStructureManager();
    Optional<DiamondCityLayout> layoutOpt = DiamondCityLayout.create(
        templateManager,
        this.layoutSeed
    );
    if (layoutOpt.isEmpty()) {
      Magnatour.LOGGER.warn(
          "[DiamondCity] Missing template(s); skipping placement"
      );
      return;
    }

    StructurePlaceSettings settings = new StructurePlaceSettings()
        .setMirror(Mirror.NONE)
        .setRotation(Rotation.NONE)
        .setIgnoreEntities(false)
        .setFinalizeEntities(true)
        .setKnownShape(true)
        .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
        .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
        .setBoundingBox(box);

    layoutOpt.get().place(
        templateManager,
        level,
        random,
        settings,
        this.cityPos
    );
  }

  @Override
  protected void addAdditionalSaveData(
      @NonNull StructurePieceSerializationContext context,
      @NonNull CompoundTag tag
  ) {
    writeBlockPos(tag, this.cityPos);
    writeVec3i(tag, "CitySize", this.citySize);
    tag.putLong("LayoutSeed", this.layoutSeed);
  }

  private static BlockPos readBlockPos(
      CompoundTag tag
  ) {
    CompoundTag posTag = tag.getCompound("CityPos").orElse(new CompoundTag());
    return new BlockPos(
        posTag.getInt("X").orElse(0),
        posTag.getInt("Y").orElse(0),
        posTag.getInt("Z").orElse(0)
    );
  }

  private static Vec3i readVec3i(
      CompoundTag tag,
      String key
  ) {
    CompoundTag sizeTag = tag.getCompound(key).orElse(new CompoundTag());
    return new Vec3i(
        sizeTag.getInt("X").orElse(0),
        sizeTag.getInt("Y").orElse(0),
        sizeTag.getInt("Z").orElse(0)
    );
  }

  private static BoundingBox makeBoundingBox(
      BlockPos cityPos,
      Vec3i citySize
  ) {
    return new BoundingBox(
        cityPos.getX(),
        cityPos.getY(),
        cityPos.getZ(),
        cityPos.getX() + citySize.getX() - 1,
        cityPos.getY() + citySize.getY() - 1,
        cityPos.getZ() + citySize.getZ() - 1
    );
  }

  private static void writeBlockPos(
      CompoundTag tag,
      BlockPos pos
  ) {
    CompoundTag posTag = new CompoundTag();
    posTag.putInt("X", pos.getX());
    posTag.putInt("Y", pos.getY());
    posTag.putInt("Z", pos.getZ());
    tag.put("CityPos", posTag);
  }

  private static void writeVec3i(
      CompoundTag tag,
      String key,
      Vec3i vec
  ) {
    CompoundTag sizeTag = new CompoundTag();
    sizeTag.putInt("X", vec.getX());
    sizeTag.putInt("Y", vec.getY());
    sizeTag.putInt("Z", vec.getZ());
    tag.put(key, sizeTag);
  }

}
