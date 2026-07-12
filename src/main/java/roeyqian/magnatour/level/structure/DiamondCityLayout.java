/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.structure;

// Java Standard
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

// Magnatour
import roeyqian.magnatour.Magnatour;

final class DiamondCityLayout {

  private static final int BUILDING_PADDING = 8;
  private static final int HOUSE_III_COLUMNS = 4;
  private static final int HOUSE_II_COLUMNS = 2;
  private static final int MAX_BUILDINGS = 12;
  private static final int MAX_COLUMNS = 4;
  private static final int MIN_BUILDINGS = 8;
  private static final int MIN_COLUMNS = 3;

  private static final Identifier HOUSE_I = id("diamond_house_i");

  private static final Identifier[] HOUSE_II = {
      id("diamond_house_ii_1"),
      id("diamond_house_ii_2"),
      id("diamond_house_ii_3"),
      id("diamond_house_ii_4")
  };
  private static final Identifier[] HOUSE_III = {
      id("diamond_house_iii_1"),
      id("diamond_house_iii_2"),
      id("diamond_house_iii_3"),
      id("diamond_house_iii_4"),
      id("diamond_house_iii_5"),
      id("diamond_house_iii_6"),
      id("diamond_house_iii_7"),
      id("diamond_house_iii_8"),
      id("diamond_house_iii_9"),
      id("diamond_house_iii_10"),
      id("diamond_house_iii_11"),
      id("diamond_house_iii_12"),
      id("diamond_house_iii_13"),
      id("diamond_house_iii_14"),
      id("diamond_house_iii_15"),
      id("diamond_house_iii_16")
  };

  private final List<Building> buildings;

  private final Vec3i citySize;

  private DiamondCityLayout(
      Vec3i citySize,
      List<Building> buildings
  ) {
    this.citySize = citySize;
    this.buildings = buildings;
  }

  private static Identifier id(
      String path
  ) {
    return Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path);
  }

  private static Optional<Vec3i> templateSize(
      StructureTemplateManager templates,
      Identifier id
  ) {
    return templates.get(id).map(StructureTemplate::getSize);
  }

  private static Optional<Vec3i> calculateGridSize(
      StructureTemplateManager templates,
      Identifier[] ids,
      int columns
  ) {
    int rows = ids.length / columns;
    int width = 0;
    int depth = 0;
    int height = 0;

    for (int row = 0; row < rows; row++) {
      int rowWidth = 0;
      int rowDepth = 0;

      for (int column = 0; column < columns; column++) {
        Optional<Vec3i> sizeOpt = templateSize(templates, ids[row * columns + column]);
        if (sizeOpt.isEmpty()) return Optional.empty();

        Vec3i size = sizeOpt.get();
        rowWidth += size.getX();
        rowDepth = Math.max(rowDepth, size.getZ());
        height = Math.max(height, size.getY());
      }

      width = Math.max(width, rowWidth);
      depth += rowDepth;
    }

    return Optional.of(new Vec3i(width, height, depth));
  }

  private static void placeGrid(
      StructureTemplateManager templateManager,
      WorldGenLevel level,
      RandomSource random,
      StructurePlaceSettings settings,
      BlockPos origin,
      Identifier[] ids,
      int columns
  ) {
    int rows = ids.length / columns;
    int zOffset = 0;

    for (int row = 0; row < rows; row++) {
      int xOffset = 0;
      int rowDepth = 0;

      for (int column = 0; column < columns; column++) {
        Identifier id = ids[row * columns + column];
        Optional<StructureTemplate> templateOpt = templateManager.get(id);
        if (templateOpt.isEmpty()) {
          Magnatour.LOGGER.warn("[DiamondCity] Missing template {}; skipping placement", id);
          return;
        }

        StructureTemplate template = templateOpt.get();
        Vec3i size = template.getSize();
        BlockPos piecePos = origin.offset(xOffset, 0, zOffset);
        template.placeInWorld(level, piecePos, piecePos, settings, random, 2);

        xOffset += size.getX();
        rowDepth = Math.max(rowDepth, size.getZ());
      }

      zOffset += rowDepth;
    }
  }

  private static BuildingPlan createPlan(
      BuildingType type
  ) {
    return new BuildingPlan(type, 1);
  }

  private static BuildingType randomBuildingType(
      Random random
  ) {
    int roll = random.nextInt(10);
    if (roll < 4) return BuildingType.HOUSE_I;
    if (roll < 7) return BuildingType.HOUSE_II;
    return BuildingType.HOUSE_III;
  }

  private static void placeStack(
      StructureTemplateManager templateManager,
      WorldGenLevel level,
      RandomSource random,
      StructurePlaceSettings settings,
      BlockPos origin,
      Identifier id,
      int levels
  ) {
    Optional<StructureTemplate> templateOpt = templateManager.get(id);
    if (templateOpt.isEmpty()) {
      Magnatour.LOGGER.warn("[DiamondCity] Missing template {}; skipping placement", id);
      return;
    }

    StructureTemplate template = templateOpt.get();
    int floorHeight = template.getSize().getY();
    for (int levelIndex = 0; levelIndex < levels; levelIndex++) {
      BlockPos piecePos = origin.above(floorHeight * levelIndex);
      template.placeInWorld(level, piecePos, piecePos, settings, random, 2);
    }
  }

  private static void placeGridStack(
      StructureTemplateManager templateManager,
      WorldGenLevel level,
      RandomSource random,
      StructurePlaceSettings settings,
      BlockPos origin,
      Identifier[] ids,
      int columns,
      int levels
  ) {
    Optional<Vec3i> gridSize = calculateGridSize(templateManager, ids, columns);
    if (gridSize.isEmpty()) return;

    for (int levelIndex = 0; levelIndex < levels; levelIndex++) {
      placeGrid(
          templateManager,
          level,
          random,
          settings,
          origin.above(gridSize.get().getY() * levelIndex),
          ids,
          columns
      );
    }
  }

  static Optional<DiamondCityLayout> create(
      StructureTemplateManager templates,
      long seed
  ) {
    Optional<Vec3i> houseISize = templateSize(templates, HOUSE_I);
    Optional<Vec3i> houseIiSize = calculateGridSize(
        templates,
        HOUSE_II,
        HOUSE_II_COLUMNS
    );
    Optional<Vec3i> houseIiiSize = calculateGridSize(
        templates,
        HOUSE_III,
        HOUSE_III_COLUMNS
    );
    if (houseISize.isEmpty() || houseIiSize.isEmpty() || houseIiiSize.isEmpty()) {
      return Optional.empty();
    }

    Random random = new Random(seed);
    int buildingCount = MIN_BUILDINGS + random.nextInt(MAX_BUILDINGS - MIN_BUILDINGS + 1);
    int columns = MIN_COLUMNS + random.nextInt(MAX_COLUMNS - MIN_COLUMNS + 1);

    List<BuildingPlan> plans = new ArrayList<>(buildingCount);
    plans.add(createPlan(BuildingType.HOUSE_I));
    plans.add(createPlan(BuildingType.HOUSE_II));
    plans.add(createPlan(BuildingType.HOUSE_III));
    while (plans.size() < buildingCount) {
      plans.add(createPlan(randomBuildingType(random)));
    }
    Collections.shuffle(plans, random);

    List<Building> buildings = new ArrayList<>(buildingCount);
    int xOffset = 0;
    int zOffset = 0;
    int rowDepth = 0;
    int maxWidth = 0;
    int maxHeight = 0;

    for (int index = 0; index < plans.size(); index++) {
      if (index > 0 && index % columns == 0) {
        xOffset = 0;
        zOffset += rowDepth + BUILDING_PADDING;
        rowDepth = 0;
      }

      BuildingPlan plan = plans.get(index);
      Vec3i baseSize = switch (plan.type()) {
        case HOUSE_I -> houseISize.get();
        case HOUSE_II -> houseIiSize.get();
        case HOUSE_III -> houseIiiSize.get();
      };
      Vec3i fullSize = new Vec3i(
          baseSize.getX(),
          baseSize.getY() * plan.levels(),
          baseSize.getZ()
      );

      buildings.add(new Building(
          plan.type(),
          xOffset,
          zOffset,
          plan.levels()
      ));

      xOffset += fullSize.getX();
      maxWidth = Math.max(maxWidth, xOffset);
      maxHeight = Math.max(maxHeight, fullSize.getY());
      rowDepth = Math.max(rowDepth, fullSize.getZ());
      xOffset += BUILDING_PADDING;
    }

    int cityDepth = zOffset + rowDepth;
    return Optional.of(new DiamondCityLayout(
        new Vec3i(maxWidth, maxHeight, cityDepth),
        buildings
    ));
  }

  static long seed(
      long worldSeed,
      int chunkX,
      int chunkZ
  ) {
    long h = worldSeed;
    h ^= (long) chunkX * 0x9E3779B97F4A7C15L;
    h ^= (long) chunkZ * 0xC2B2AE3D27D4EB4FL;
    h ^= h >>> 27;
    h *= 0x3C79AC492BA7B653L;
    h ^= h >>> 33;
    h *= 0x1C69B3F74AC4AE35L;
    h ^= h >>> 27;
    return h;
  }

  Vec3i citySize() {
    return this.citySize;
  }

  void place(
      StructureTemplateManager templateManager,
      WorldGenLevel level,
      RandomSource random,
      StructurePlaceSettings settings,
      BlockPos cityPos
  ) {
    for (Building building : this.buildings) {
      BlockPos buildingPos = cityPos.offset(building.offsetX(), 0, building.offsetZ());

      switch (building.type()) {
        case HOUSE_I -> placeStack(
            templateManager,
            level,
            random,
            settings,
            buildingPos,
            HOUSE_I,
            building.levels()
        );
        case HOUSE_II -> placeGridStack(
            templateManager,
            level,
            random,
            settings,
            buildingPos,
            HOUSE_II,
            HOUSE_II_COLUMNS,
            building.levels()
        );
        case HOUSE_III -> placeGridStack(
            templateManager,
            level,
            random,
            settings,
            buildingPos,
            HOUSE_III,
            HOUSE_III_COLUMNS,
            1
        );
      }
    }
  }

  private record Building(
      BuildingType type,
      int offsetX,
      int offsetZ,
      int levels
  ) {}

  private record BuildingPlan(
      BuildingType type,
      int levels
  ) {}

  private enum BuildingType {
    HOUSE_I,
    HOUSE_II,
    HOUSE_III
  }

}
