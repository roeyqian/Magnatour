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
import net.minecraft.world.level.block.Blocks;
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
import roeyqian.magnatour.utility.registry.entity.RegLiveEntities;

public final class DiamondCityPiece extends StructurePiece {

  private static final int MOB_SHARED_SPAWN_DIVISOR = 3;
  private static final int MOB_SITE_CEILING_SCAN_DISTANCE = 4;

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
    spawnStructureMobs(level, random, chunkPos);
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

  private void spawnStructureMobs(
      WorldGenLevel level,
      RandomSource random,
      ChunkPos chunkPos
  ) {
    if (!ChunkPos.containing(this.cityPos).equals(chunkPos)) return;

    spawnDiamondCityMobs(level, random, chunkSpawnBox(chunkPos), 18);
  }

  private void spawnDiamondCityMobs(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox spawnBox,
      int requestedCount
  ) {
    int indoorTarget = requestedCount >= 2
        ? Math.max(1, requestedCount / MOB_SHARED_SPAWN_DIVISOR)
        : requestedCount;
    int outdoorTarget = requestedCount - indoorTarget >= 1
        ? Math.min(indoorTarget, requestedCount - indoorTarget)
        : 0;

    int spawned = 0;
    if (indoorTarget > 0) {
      spawned += StructureMobHelper.spawnPersistentGroundMobsUnderCoverDistributedByFloor(
          level,
          random,
          spawnBox,
          RegLiveEntities.OBSIDIAN_GOLEM,
          indoorTarget,
          floorState -> floorState.is(Blocks.OBSIDIAN),
          MOB_SITE_CEILING_SCAN_DISTANCE
      );
    }
    if (outdoorTarget > 0) {
      spawned += StructureMobHelper.spawnPersistentGroundMobsOpenAir(
          level,
          random,
          spawnBox,
          RegLiveEntities.OBSIDIAN_GOLEM,
          outdoorTarget,
          _ -> true,
          MOB_SITE_CEILING_SCAN_DISTANCE
      );
    }

    int remaining = requestedCount - spawned;
    if (remaining > 0) {
      StructureMobHelper.spawnPersistentGroundMobs(
          level,
          random,
          spawnBox,
          RegLiveEntities.OBSIDIAN_GOLEM,
          remaining,
          _ -> true
      );
    }
  }

  private BoundingBox chunkSpawnBox(
      ChunkPos chunkPos
  ) {
    BoundingBox structureBox = this.getBoundingBox();
    return new BoundingBox(
        Math.max(structureBox.minX(), chunkPos.getMinBlockX()),
        structureBox.minY(),
        Math.max(structureBox.minZ(), chunkPos.getMinBlockZ()),
        Math.min(structureBox.maxX(), chunkPos.getMaxBlockX()),
        structureBox.maxY(),
        Math.min(structureBox.maxZ(), chunkPos.getMaxBlockZ())
    );
  }

}
