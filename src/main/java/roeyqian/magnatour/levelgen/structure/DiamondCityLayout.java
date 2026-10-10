/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.structure;

// Java Standard
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.Fluids;

// Magnatour
import roeyqian.magnatour.Magnatour;

final class DiamondCityLayout {

  static final int FOUNDATION_DEPTH = 16;

  private static final int BORDER = 6;
  private static final int COLUMNS = 6;
  private static final int PLAZA_COLUMNS = 2;
  private static final int BUILDING_COUNT = COLUMNS * COLUMNS - PLAZA_COLUMNS * PLAZA_COLUMNS;
  private static final int PLOT_SIZE = 40;
  private static final int STREET_WIDTH = 4;
  // A chunk-centered city covers exactly chunks -8 through +8 on both axes.
  private static final int CITY_WIDTH = BORDER * 2 + COLUMNS * PLOT_SIZE + (COLUMNS - 1) * STREET_WIDTH;

  private static final Identifier FOUNTAIN_PLAZA = Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "diamond_city_fountain_plaza");

  private final Vec3i citySize;

  private final List<Building> buildings;

  private DiamondCityLayout(
      Vec3i citySize,
      List<Building> buildings
  ) {
    this.citySize = citySize;
    this.buildings = List.copyOf(buildings);
  }

  static Optional<DiamondCityLayout> create(
      StructureTemplateManager templates,
      long seed
  ) {
    Optional<StructureTemplate> plazaTemplate = templates.get(FOUNTAIN_PLAZA);
    if (plazaTemplate.isEmpty() || !plazaTemplate.get().getSize().equals(new Vec3i(84, 22, 84))) return Optional.empty();
    List<House> houses = new ArrayList<>();
    for (int type = 0; type < 3; type++) {
      int columns = 1 << type;
      String label = switch (type) {
        case 0 -> "hall_i";
        case 1 -> "mansion_ii";
        default -> "tower_iii";
      };
      List<Tile> tiles = new ArrayList<>();
      int width = 0;
      int depth = 0;
      int height = 0;
      for (int row = 0; row < columns; row++) {
        int rowWidth = 0;
        int rowDepth = 0;
        for (int column = 0; column < columns; column++) {
          String suffix = "_" + (row * columns + column + 1);
          Identifier id = Identifier.fromNamespaceAndPath(
              Magnatour.MOD_ID, "diamond_city_" + label + suffix
          );
          Optional<StructureTemplate> template = templates.get(id);
          if (template.isEmpty()) return Optional.empty();
          Vec3i size = template.get().getSize();
          if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) return Optional.empty();
          tiles.add(new Tile(id, rowWidth, depth, size));
          rowWidth += size.getX();
          rowDepth = Math.max(rowDepth, size.getZ());
          height = Math.max(height, size.getY());
        }
        width = Math.max(width, rowWidth);
        depth += rowDepth;
      }
      if (width > PLOT_SIZE || depth > PLOT_SIZE) return Optional.empty();
      houses.add(new House(new Vec3i(width, height, depth), List.copyOf(tiles)));
    }
    // The four central plots form a plaza; distribute the 32 palaces evenly by type.
    List<House> plans = new ArrayList<>();
    for (int i = 0; i < BUILDING_COUNT; i++) plans.add(houses.get(i % houses.size()));
    Collections.shuffle(plans, new Random(seed));
    List<Building> buildings = new ArrayList<>();
    int index = 0;
    int height = 24;
    for (int row = 0; row < COLUMNS; row++) {
      for (int column = 0; column < COLUMNS; column++) {
        if (row >= (COLUMNS - PLAZA_COLUMNS) / 2 && row < (COLUMNS + PLAZA_COLUMNS) / 2
            && column >= (COLUMNS - PLAZA_COLUMNS) / 2 && column < (COLUMNS + PLAZA_COLUMNS) / 2) continue;
        House house = plans.get(index++);
        int x = BORDER + column * (PLOT_SIZE + STREET_WIDTH) + (PLOT_SIZE - house.size().getX()) / 2;
        int z = BORDER + row * (PLOT_SIZE + STREET_WIDTH) + (PLOT_SIZE - house.size().getZ()) / 2;
        buildings.add(new Building(house, x, z));
        height = Math.max(height, house.size().getY() + 1);
      }
    }
    return Optional.of(new DiamondCityLayout(new Vec3i(CITY_WIDTH, height, CITY_WIDTH), buildings));
  }

  static long seed(
      long worldSeed,
      int chunkX, int chunkZ
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

  void place(
      StructureTemplateManager templates,
      WorldGenLevel level,
      ChunkGenerator generator,
      RandomSource random,
      StructurePlaceSettings settings,
      BlockPos origin
  ) {
    BoundingBox clip = settings.getBoundingBox();
    if (clip == null) return;
    placeInfrastructure(templates, level, random, settings, origin, clip);
    int plazaOffset = BORDER + ((COLUMNS - PLAZA_COLUMNS) / 2) * (PLOT_SIZE + STREET_WIDTH);
    BlockPos plazaPos = origin.offset(plazaOffset, 1, plazaOffset);
    if (new BoundingBox(plazaPos.getX(), plazaPos.getY(), plazaPos.getZ(),
        plazaPos.getX() + 83, plazaPos.getY() + 21, plazaPos.getZ() + 83).intersects(clip)) {
      templates.get(FOUNTAIN_PLAZA).ifPresent(template ->
          template.placeInWorld(level, plazaPos, plazaPos, settings, random, 2)
      );
      scheduleFountainWater(level, origin, clip);
      placeGarden(level, generator, origin, clip, CITY_WIDTH / 2);
    }
    for (Building building : this.buildings) {
      BlockPos buildingPos = origin.offset(building.x(), 1, building.z());
      for (Tile tile : building.house().tiles()) {
        BlockPos pos = buildingPos.offset(tile.x(), 0, tile.z());
        Vec3i size = tile.size();
        // Only visit tiles intersecting the chunk currently being generated.
        if (!new BoundingBox(pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + size.getX() - 1, pos.getY() + size.getY() - 1,
            pos.getZ() + size.getZ() - 1).intersects(clip)) continue;
        templates.get(tile.id()).ifPresent(template ->
            template.placeInWorld(level, pos, pos, settings, random, 2)
        );
      }
    }
  }

  Vec3i citySize() {
    return this.citySize;
  }

  private static void placeInfrastructure(
      StructureTemplateManager templates,
      WorldGenLevel level,
      RandomSource random,
      StructurePlaceSettings settings,
      BlockPos origin,
      BoundingBox clip
  ) {
    int columns = CITY_WIDTH / 16;
    for (int row = 0; row < columns; row++) {
      for (int column = 0; column < columns; column++) {
        BlockPos pos = origin.offset(column * 16, -FOUNDATION_DEPTH, row * 16);
        if (!new BoundingBox(pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + 15, origin.getY() + 18, pos.getZ() + 15).intersects(clip)) continue;
        Identifier id = Identifier.fromNamespaceAndPath(
            Magnatour.MOD_ID, "diamond_city_infrastructure_iv_" + (row * columns + column + 1)
        );
        templates.get(id).ifPresent(template ->
            template.placeInWorld(level, pos, pos, settings, random, 2)
        );
      }
    }
  }

  private static void scheduleFountainWater(
      WorldGenLevel level,
      BlockPos origin,
      BoundingBox clip
  ) {
    int center = CITY_WIDTH / 2;
    for (int x = center - 2; x <= center + 2; x++) {
      for (int z = center - 2; z <= center + 2; z++) {
        if (!((Math.abs(x - center) == 2 && Math.abs(z - center) <= 1)
            || (Math.abs(z - center) == 2 && Math.abs(x - center) <= 1))) continue;
        for (int y = 5; y <= 18; y++) {
          BlockPos pos = origin.offset(x, y, z);
          if (clip.isInside(pos)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
      }
    }
  }

  private static void placeGarden(
      WorldGenLevel level,
      ChunkGenerator generator,
      BlockPos origin,
      BoundingBox clip,
      int center
  ) {
    Feature vegetation = level.registryAccess().lookupOrThrow(Registries.FEATURE).getValueOrThrow(
        ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "diamond_city_garden"))
    );
    for (int x = center - 16; x <= center + 16; x++) {
      for (int z = center - 16; z <= center + 16; z++) {
        // Keep planting out of the basin and beneath the corner trees.
        if (Math.abs(x - center) <= 7 && Math.abs(z - center) <= 7) continue;
        if (Math.abs(Math.abs(x - center) - 14) <= 2 && Math.abs(Math.abs(z - center) - 14) <= 2) continue;
        BlockPos pos = origin.offset(x, 2, z);
        if (!clip.isInside(pos)) continue;
        // Each cell gets its own seed so chunk generation order cannot change the garden.
        RandomSource cellRandom = RandomSource.create(seed(origin.asLong(), x, z));
        if (cellRandom.nextInt(8) == 0 && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
            && level.getBlockState(pos).isAir()) {
          vegetation.place(level, generator, cellRandom, pos);
        }
      }
    }
  }

  private record Building(
      House house,
      int x, int z
  ) {}

  private record House(
      Vec3i size,
      List<Tile> tiles
  ) {}

  private record Tile(
      Identifier id,
      int x, int z,
      Vec3i size
  ) {}

}
