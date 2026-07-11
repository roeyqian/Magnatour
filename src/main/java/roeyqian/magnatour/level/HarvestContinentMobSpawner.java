/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Java Standard
import java.util.List;
import java.util.function.Predicate;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.level.RegDimensions;

public final class HarvestContinentMobSpawner {

  private static final int COUNT_HORIZONTAL_RADIUS = 48;
  private static final int COUNT_VERTICAL_RADIUS = 20;
  private static final int MAX_SPAWN_ATTEMPTS = 24;
  private static final int MAX_SPAWN_DISTANCE = 44;
  private static final int MAX_SURFACE_SCAN_DEPTH = 24;
  private static final int MIN_PLAYER_DISTANCE = 18;
  private static final int MIN_SPAWN_DISTANCE = 20;

  private static final long SPAWN_INTERVAL_TICKS = 200L;

  private static boolean tickEventRegistered = false;

  private static final ResourceKey<Biome> MELON_JUNGLE = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "melon_jungle")
  );

  private static final BiomeSpawnProfile MELON_JUNGLE_PROFILE = new BiomeSpawnProfile(
      MELON_JUNGLE,
      10,
      2,
      floor -> floor.is(RegInsertBlocks.EVER_WATER_GRASS_BLOCK),
      List.of(
          new WeightedAnimalType(EntityTypes.SHEEP, 16),
          new WeightedAnimalType(EntityTypes.PIG, 20),
          new WeightedAnimalType(EntityTypes.CHICKEN, 26),
          new WeightedAnimalType(EntityTypes.COW, 14)
      )
  );

  private static final ResourceKey<Biome> PUMPKIN_GORGE = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "pumpkin_gorge")
  );

  private static final BiomeSpawnProfile PUMPKIN_GORGE_PROFILE = new BiomeSpawnProfile(
      PUMPKIN_GORGE,
      8,
      2,
      floor -> floor.is(Blocks.RED_SAND),
      List.of(
          new WeightedAnimalType(EntityTypes.SHEEP, 16),
          new WeightedAnimalType(EntityTypes.PIG, 14),
          new WeightedAnimalType(EntityTypes.CHICKEN, 16),
          new WeightedAnimalType(EntityTypes.COW, 10)
      )
  );

  private static final ResourceKey<Biome> WHEAT_PLAIN = ResourceKey.create(
      Registries.BIOME,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "wheat_plain")
  );

  private static final BiomeSpawnProfile WHEAT_PLAIN_PROFILE = new BiomeSpawnProfile(
      WHEAT_PLAIN,
      14,
      3,
      floor -> floor.is(RegInsertBlocks.EVER_WATER_GRASS_BLOCK)
          || floor.is(RegInsertBlocks.EVER_WATER_FARMLAND),
      List.of(
          new WeightedAnimalType(EntityTypes.SHEEP, 24),
          new WeightedAnimalType(EntityTypes.PIG, 20),
          new WeightedAnimalType(EntityTypes.CHICKEN, 20),
          new WeightedAnimalType(EntityTypes.COW, 16),
          new WeightedAnimalType(EntityTypes.HORSE, 8),
          new WeightedAnimalType(EntityTypes.DONKEY, 2)
      )
  );

  private HarvestContinentMobSpawner() {}

  public static void registerTickEvent() {
    if (tickEventRegistered) return;
    tickEventRegistered = true;

    ServerTickEvents.END_SERVER_TICK.register(server -> {
      long currentTick = server.getTickCount();
      if (currentTick % SPAWN_INTERVAL_TICKS != 0L) return;

      ServerLevel level = server.getLevel(RegDimensions.HARVEST_CONTINENT);
      if (level == null || level.players().isEmpty()) return;

      for (ServerPlayer player : level.players()) {
        processPlayer(level, player);
      }
    });
  }

  private static void processPlayer(
      ServerLevel level,
      ServerPlayer player
  ) {
    if (!player.isAlive() || player.isSpectator()) return;

    BiomeSpawnProfile profile = profileFor(level.getBiome(player.blockPosition()));
    if (profile == null) return;

    int currentPopulation = countNearbyAnimals(level, player.blockPosition(), profile);
    int missing = profile.targetPopulation() - currentPopulation;
    if (missing <= 0) return;

    int spawnTarget = Math.min(profile.maxSpawnPerCycle(), missing);
    int spawned = 0;

    for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS && spawned < spawnTarget; attempt++) {
      EntityType<? extends Animal> entityType = profile.pickAnimalType(level.getRandom());
      if (entityType == null) break;
      if (trySpawnAnimal(level, player.blockPosition(), profile, entityType)) {
        spawned++;
      }
    }
  }

  private static BiomeSpawnProfile profileFor(
      Holder<Biome> biome
  ) {
    if (WHEAT_PLAIN_PROFILE.matches(biome)) return WHEAT_PLAIN_PROFILE;
    if (MELON_JUNGLE_PROFILE.matches(biome)) return MELON_JUNGLE_PROFILE;
    if (PUMPKIN_GORGE_PROFILE.matches(biome)) return PUMPKIN_GORGE_PROFILE;
    return null;
  }

  private static int countNearbyAnimals(
      ServerLevel level,
      BlockPos center,
      BiomeSpawnProfile profile
  ) {
    AABB box = AABB.ofSize(
        Vec3.atCenterOf(center),
        COUNT_HORIZONTAL_RADIUS * 2.0D,
        COUNT_VERTICAL_RADIUS * 2.0D,
        COUNT_HORIZONTAL_RADIUS * 2.0D
    );

    return level.getEntities((Entity) null, box, entity ->
        entity.isAlive()
            && entity instanceof Animal
            && profile.supports(entity.getType())
            && profile.matches(level.getBiome(entity.blockPosition()))
    ).size();
  }

  private static boolean trySpawnAnimal(
      ServerLevel level,
      BlockPos center,
      BiomeSpawnProfile profile,
      EntityType<? extends Animal> entityType
  ) {
    RandomSource random = level.getRandom();

    for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
      BlockPos spawnPos = findSpawnPos(level, center, profile, random);
      if (spawnPos == null) continue;

      Animal animal = entityType.create(level, EntitySpawnReason.NATURAL);
      if (animal == null) continue;

      animal.snapTo(
          spawnPos.getX() + 0.5D,
          spawnPos.getY(),
          spawnPos.getZ() + 0.5D,
          random.nextFloat() * 360.0F,
          0.0F
      );
      animal.setYBodyRot(animal.getYRot());

      if (!hasValidSpawnSpace(level, spawnPos, entityType)
          || !animal.checkSpawnObstruction(level)) {
        continue;
      }

      animal.finalizeSpawn(
          level,
          level.getCurrentDifficultyAt(spawnPos),
          EntitySpawnReason.NATURAL,
          null
      );
      level.addFreshEntityWithPassengers(animal);
      return true;
    }

    return false;
  }

  private static BlockPos findSpawnPos(
      ServerLevel level,
      BlockPos center,
      BiomeSpawnProfile profile,
      RandomSource random
  ) {
    for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
      int distance = Mth.nextInt(random, MIN_SPAWN_DISTANCE, MAX_SPAWN_DISTANCE);
      double angle = random.nextDouble() * (Math.PI * 2.0D);
      int x = center.getX() + Mth.floor(Math.cos(angle) * distance);
      int z = center.getZ() + Mth.floor(Math.sin(angle) * distance);

      if (!level.getChunkSource().hasChunk(x >> 4, z >> 4)) continue;

      int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
      BlockPos spawnPos = resolveSpawnPos(level, x, z, topY, profile);
      if (spawnPos == null || !profile.matches(level.getBiome(spawnPos))) continue;

      if (level.getNearestPlayer(
          spawnPos.getX() + 0.5D,
          spawnPos.getY(),
          spawnPos.getZ() + 0.5D,
          MIN_PLAYER_DISTANCE,
          false
      ) != null) {
        continue;
      }

      return spawnPos;
    }

    return null;
  }

  private static boolean hasValidSpawnSpace(
      ServerLevel level,
      BlockPos spawnPos,
      EntityType<? extends Animal> entityType
  ) {
    BlockState feetState = level.getBlockState(spawnPos);
    BlockState headState = level.getBlockState(spawnPos.above());

    return NaturalSpawner.isValidEmptySpawnBlock(
        level,
        spawnPos,
        feetState,
        feetState.getFluidState(),
        entityType
    ) && NaturalSpawner.isValidEmptySpawnBlock(
        level,
        spawnPos.above(),
        headState,
        headState.getFluidState(),
        entityType
    );
  }

  private static BlockPos resolveSpawnPos(
      ServerLevel level,
      int x,
      int z,
      int topY,
      BiomeSpawnProfile profile
  ) {
    int minY = Math.max(level.getMinY() + 2, topY - MAX_SURFACE_SCAN_DEPTH);
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, topY, z);

    for (int y = topY; y >= minY; y--) {
      mutablePos.set(x, y, z);
      BlockState state = level.getBlockState(mutablePos);
      if (state.isAir()) continue;

      if (profile.acceptsFloor(state)) {
        return mutablePos.above().immutable();
      }

      BlockState belowState = level.getBlockState(mutablePos.below());
      if (profile.acceptsFloor(belowState) && isPassableGroundCover(level, mutablePos, state)) {
        return mutablePos.immutable();
      }
    }

    return null;
  }

  private static boolean isPassableGroundCover(
      ServerLevel level,
      BlockPos pos,
      BlockState state
  ) {
    return state.getFluidState().isEmpty()
        && !state.isCollisionShapeFullBlock(level, pos)
        && !state.isSignalSource();
  }

  private record BiomeSpawnProfile(
      ResourceKey<Biome> biomeKey,
      int targetPopulation,
      int maxSpawnPerCycle,
      Predicate<BlockState> floorPredicate,
      List<WeightedAnimalType> animalTypes
  ) {

    private boolean acceptsFloor(
        BlockState state
    ) {
      return this.floorPredicate.test(state);
    }

    private boolean matches(
        Holder<Biome> biome
    ) {
      return biome.is(this.biomeKey);
    }

    private EntityType<? extends Animal> pickAnimalType(
        RandomSource random
    ) {
      int totalWeight = 0;
      for (WeightedAnimalType animalType : this.animalTypes) {
        totalWeight += Math.max(0, animalType.weight());
      }
      if (totalWeight <= 0) return null;

      int roll = random.nextInt(totalWeight);
      int cursor = 0;

      for (WeightedAnimalType animalType : this.animalTypes) {
        cursor += Math.max(0, animalType.weight());
        if (roll < cursor) return animalType.entityType();
      }

      return this.animalTypes.getLast().entityType();
    }

    private boolean supports(
        EntityType<?> entityType
    ) {
      for (WeightedAnimalType animalType : this.animalTypes) {
        if (animalType.entityType() == entityType) return true;
      }
      return false;
    }

  }

  private record WeightedAnimalType(
      EntityType<? extends Animal> entityType,
      int weight
  ) {}

}
