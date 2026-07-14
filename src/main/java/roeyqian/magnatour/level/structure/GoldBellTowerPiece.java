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
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.entity.RegLiveEntities;

public final class GoldBellTowerPiece extends StructurePiece {

  private static final Identifier LOWER_TEMPLATE =
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "gold_bell_tower_1");
  private static final Identifier UPPER_TEMPLATE =
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "gold_bell_tower_2");

  private final Vec3i lowerSize;
  private final Vec3i upperSize;

  private final BlockPos lowerPos;

  public GoldBellTowerPiece(
      StructurePieceType type,
      CompoundTag tag
  ) {
    super(type, tag);
    this.lowerPos = readBlockPos(tag);
    this.lowerSize = readVec3i(tag, "LowerSize");
    this.upperSize = readVec3i(tag, "UpperSize");
  }

  public GoldBellTowerPiece(
      StructurePieceType type,
      BlockPos lowerPos,
      Vec3i lowerSize,
      Vec3i upperSize
  ) {
    super(type, 0, makeBoundingBox(lowerPos, lowerSize, upperSize));
    this.lowerPos = lowerPos;
    this.lowerSize = lowerSize;
    this.upperSize = upperSize;
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
    Optional<StructureTemplate> lowerOpt = templateManager.get(LOWER_TEMPLATE);
    Optional<StructureTemplate> upperOpt = templateManager.get(UPPER_TEMPLATE);
    if (lowerOpt.isEmpty() || upperOpt.isEmpty()) {
      Magnatour.LOGGER.warn(
          "[GoldBellTower] Missing template(s); skipping placement"
      );
      return;
    }

    StructureTemplate lower = lowerOpt.get();
    StructureTemplate upper = upperOpt.get();
    BlockPos upperPos = this.lowerPos.above(this.lowerSize.getY());

    StructurePlaceSettings settings = new StructurePlaceSettings()
        .setMirror(Mirror.NONE)
        .setRotation(Rotation.NONE)
        .setIgnoreEntities(false)
        .setFinalizeEntities(true)
        .setKnownShape(true)
        .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
        .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
        .setBoundingBox(box);

    lower.placeInWorld(level, this.lowerPos, this.lowerPos, settings, random, 2);
    upper.placeInWorld(level, upperPos, upperPos, settings, random, 2);

    replaceGrassBelow(level, box);
    spawnStructureMobs(level, random, chunkPos);
  }

  @Override
  protected void addAdditionalSaveData(
      @NonNull StructurePieceSerializationContext context,
      @NonNull CompoundTag tag
  ) {
    writeBlockPos(tag, this.lowerPos);
    writeVec3i(tag, "LowerSize", this.lowerSize);
    writeVec3i(tag, "UpperSize", this.upperSize);
  }

  private static BlockPos readBlockPos(
      CompoundTag tag
  ) {
    CompoundTag posTag = tag.getCompound("LowerPos").orElse(new CompoundTag());
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
      BlockPos lowerPos,
      Vec3i lowerSize,
      Vec3i upperSize
  ) {
    int totalHeight = lowerSize.getY() + upperSize.getY();
    return new BoundingBox(
        lowerPos.getX(),
        lowerPos.getY(),
        lowerPos.getZ(),
        lowerPos.getX() + lowerSize.getX() - 1,
        lowerPos.getY() + totalHeight - 1,
        lowerPos.getZ() + lowerSize.getZ() - 1
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
    tag.put("LowerPos", posTag);
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

  private static boolean isSpawnFloor(
      BlockState floorState
  ) {
    return floorState.is(Blocks.POLISHED_BLACKSTONE)
        || floorState.is(Blocks.GILDED_BLACKSTONE);
  }

  private void replaceGrassBelow(
      WorldGenLevel level,
      BoundingBox box
  ) {
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    int surfaceY = this.lowerPos.getY() - 1;
    int minX = this.lowerPos.getX();
    int maxX = this.lowerPos.getX() + this.lowerSize.getX() - 1;
    int minZ = this.lowerPos.getZ();
    int maxZ = this.lowerPos.getZ() + this.lowerSize.getZ() - 1;

    BlockState dirt = RegInsertBlocks.EVER_WATER_SOIL.defaultBlockState();

    for (int x = minX; x <= maxX; x++) {
      mutablePos.setX(x);
      for (int z = minZ; z <= maxZ; z++) {
        mutablePos.setZ(z);
        mutablePos.setY(surfaceY);
        if (!box.isInside(mutablePos)) continue;

        BlockState state = level.getBlockState(mutablePos);
        if (state.is(Blocks.GRASS_BLOCK)
            || state.is(RegInsertBlocks.EVER_WATER_GRASS_BLOCK)) {
          level.setBlock(mutablePos, dirt, 2);
        }
      }
    }
  }

  private void spawnStructureMobs(
      WorldGenLevel level,
      RandomSource random,
      ChunkPos chunkPos
  ) {
    if (!ChunkPos.containing(this.lowerPos).equals(chunkPos)) return;
    if (level.getLevel().getDifficulty() == Difficulty.PEACEFUL) return;

    BoundingBox spawnBox = chunkSpawnBox(chunkPos);
    StructureMobHelper.spawnPersistentGroundMobsDistributedByFloor(
        level,
        random,
        spawnBox,
        RegLiveEntities.BELL_RINGER,
        12,
        GoldBellTowerPiece::isSpawnFloor
    );
    StructureMobHelper.spawnPersistentAirMobsAboveFloors(
        level,
        random,
        spawnBox,
        RegLiveEntities.BELL_SOUL,
        14,
        GoldBellTowerPiece::isSpawnFloor,
        1,
        3
    );
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
