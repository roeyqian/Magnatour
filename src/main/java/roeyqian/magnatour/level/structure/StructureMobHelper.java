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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

// JSpecify
import org.jspecify.annotations.Nullable;

final class StructureMobHelper {

  private static final int AIR_SPAWN_ATTEMPTS = 48;

  private StructureMobHelper() {}

  static <T extends Mob> int spawnPersistentGroundMobs(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      Predicate<BlockState> floorPredicate
  ) {
    return spawnPersistentGroundMobs(
        level,
        random,
        box,
        entityType,
        count,
        floorPredicate,
        false,
        _ -> true
    );
  }

  private static <T extends Mob> int spawnPersistentGroundMobs(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      Predicate<BlockState> floorPredicate,
      boolean distributeByFloor,
      Predicate<BlockPos> spawnSitePredicate
  ) {
    T probeMob = entityType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
    if (probeMob == null) return 0;

    List<BlockPos> candidates = findGroundSpawnCandidates(
        level,
        box,
        probeMob,
        floorPredicate,
        spawnSitePredicate
    );
    if (candidates.isEmpty()) return 0;
    java.util.Random shuffleRandom = new java.util.Random(random.nextLong());
    Collections.shuffle(candidates, shuffleRandom);
    if (distributeByFloor) {
      candidates = distributeCandidatesByFloor(candidates, count, shuffleRandom);
    }

    int spawned = 0;

    for (int index = 0; index < count && index < candidates.size(); index++) {
      T mob = entityType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
      if (mob == null) return spawned;

      BlockPos spawnPos = candidates.get(index);

      prepareMob(level, random, mob, spawnPos);
      level.addFreshEntityWithPassengers(mob);
      spawned++;
    }

    return spawned;
  }

  private static <T extends Mob> List<BlockPos> findGroundSpawnCandidates(
      WorldGenLevel level,
      BoundingBox box,
      T mob,
      Predicate<BlockState> floorPredicate,
      Predicate<BlockPos> spawnSitePredicate
  ) {
    List<BlockPos> candidates = new ArrayList<>();
    int minX = box.minX() + 1;
    int maxX = box.maxX() - 1;
    int minZ = box.minZ() + 1;
    int maxZ = box.maxZ() - 1;
    if (minX > maxX) {
      minX = box.minX();
      maxX = box.maxX();
    }
    if (minZ > maxZ) {
      minZ = box.minZ();
      maxZ = box.maxZ();
    }

    int minFloorY = box.minY() - 1;
    int maxFloorY = box.maxY() - 1;
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (int y = maxFloorY; y >= minFloorY; y--) {
          mutablePos.set(x, y, z);
          BlockState floorState = level.getBlockState(mutablePos);
          if (floorState.isAir()
              || !floorPredicate.test(floorState)
              || !floorState.isFaceSturdy(level, mutablePos, Direction.UP)) {
            continue;
          }

          BlockPos spawnPos = mutablePos.above().immutable();
          if (!hasAirColumn(level, spawnPos)
              || !spawnSitePredicate.test(spawnPos)) {
            continue;
          }

          mob.snapTo(
              spawnPos.getX() + 0.5D,
              spawnPos.getY(),
              spawnPos.getZ() + 0.5D,
              0.0F,
              0.0F
          );
          if (mob.checkSpawnObstruction(level)) {
            candidates.add(spawnPos);
          }
        }
      }
    }

    return candidates;
  }

  private static List<BlockPos> distributeCandidatesByFloor(
      List<BlockPos> candidates,
      int targetCount,
      java.util.Random random
  ) {
    Map<Integer, List<BlockPos>> byFloor = new LinkedHashMap<>();
    candidates.stream()
        .sorted(Comparator.comparingInt(BlockPos::getY))
        .forEach(pos -> byFloor.computeIfAbsent(pos.getY(), _ -> new ArrayList<>()).add(pos));

    List<Integer> floors = new ArrayList<>(byFloor.keySet());
    for (List<BlockPos> floorCandidates : byFloor.values()) {
      Collections.shuffle(floorCandidates, random);
    }

    if (floors.isEmpty()) return candidates;

    int startIndex = random.nextInt(floors.size());
    List<BlockPos> distributed = new ArrayList<>(Math.min(targetCount, candidates.size()));

    while (distributed.size() < targetCount) {
      boolean addedThisRound = false;

      for (int offset = 0; offset < floors.size() && distributed.size() < targetCount; offset++) {
        int floorY = floors.get((startIndex + offset) % floors.size());
        List<BlockPos> floorCandidates = byFloor.get(floorY);
        if (floorCandidates == null || floorCandidates.isEmpty()) continue;

        distributed.add(floorCandidates.removeLast());
        addedThisRound = true;
      }

      if (!addedThisRound) break;
      startIndex = (startIndex + 1) % floors.size();
    }

    return distributed;
  }

  private static <T extends Mob> void prepareMob(
      WorldGenLevel level,
      RandomSource random,
      T mob,
      BlockPos spawnPos
  ) {
    mob.snapTo(
        spawnPos.getX() + 0.5D,
        spawnPos.getY(),
        spawnPos.getZ() + 0.5D,
        random.nextFloat() * 360.0F,
        0.0F
    );
    mob.setYBodyRot(mob.getYRot());
    mob.finalizeSpawn(
        level,
        level.getCurrentDifficultyAt(spawnPos),
        EntitySpawnReason.STRUCTURE,
        null
    );
    mob.setPersistenceRequired();
  }

  private static boolean hasAirColumn(
      WorldGenLevel level,
      BlockPos pos
  ) {
    return level.getBlockState(pos).isAir()
        && level.getBlockState(pos.above()).isAir();
  }

  private static int clamp(
      int value,
      int min,
      int max
  ) {
    return Math.max(min, Math.min(max, value));
  }

  private static int randomBetween(
      RandomSource random,
      int min,
      int max
  ) {
    if (min >= max) return min;
    return min + random.nextInt(max - min + 1);
  }

  private static boolean hasCeilingNearby(
      WorldGenLevel level,
      BlockPos pos,
      int maxDistance
  ) {
    for (int offset = 1; offset <= maxDistance; offset++) {
      if (!level.getBlockState(pos.above(offset)).isAir()) {
        return true;
      }
    }

    return false;
  }

  private static <T extends Mob> @Nullable BlockPos findAirSpawnPos(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      T mob,
      int horizontalRadius,
      int minYOffset,
      int maxYOffset
  ) {
    BlockPos center = box.getCenter();
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    for (int attempt = 0; attempt < AIR_SPAWN_ATTEMPTS; attempt++) {
      int x = clamp(
          center.getX() + randomBetween(random, -horizontalRadius, horizontalRadius),
          box.minX(),
          box.maxX()
      );
      int y = box.maxY() + randomBetween(random, minYOffset, maxYOffset);
      int z = clamp(
          center.getZ() + randomBetween(random, -horizontalRadius, horizontalRadius),
          box.minZ(),
          box.maxZ()
      );
      mutablePos.set(x, y, z);

      mob.snapTo(
          x + 0.5D,
          y,
          z + 0.5D,
          0.0F,
          0.0F
      );
      if (hasAirColumn(level, mutablePos)
          && mob.checkSpawnObstruction(level)) {
        return mutablePos.immutable();
      }
    }

    return null;
  }

  private static <T extends Mob> @Nullable BlockPos findAirSpawnPosAboveFloor(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      T mob,
      Predicate<BlockState> floorPredicate,
      int minYOffset,
      int maxYOffset
  ) {
    int minX = box.minX() + 1;
    int maxX = box.maxX() - 1;
    int minZ = box.minZ() + 1;
    int maxZ = box.maxZ() - 1;
    if (minX > maxX) {
      minX = box.minX();
      maxX = box.maxX();
    }
    if (minZ > maxZ) {
      minZ = box.minZ();
      maxZ = box.maxZ();
    }

    int minFloorY = box.minY() - 1;
    int maxFloorY = box.maxY() - 1;
    BlockPos.MutableBlockPos floorPos = new BlockPos.MutableBlockPos();
    BlockPos.MutableBlockPos spawnPos = new BlockPos.MutableBlockPos();

    for (int attempt = 0; attempt < AIR_SPAWN_ATTEMPTS; attempt++) {
      int x = randomBetween(random, minX, maxX);
      int z = randomBetween(random, minZ, maxZ);

      for (int y = maxFloorY; y >= minFloorY; y--) {
        floorPos.set(x, y, z);
        BlockState floorState = level.getBlockState(floorPos);
        if (floorState.isAir() || !floorPredicate.test(floorState)) continue;

        int spawnY = y + randomBetween(random, minYOffset, maxYOffset);
        spawnPos.set(x, spawnY, z);
        mob.snapTo(
            x + 0.5D,
            spawnY,
            z + 0.5D,
            0.0F,
            0.0F
        );
        if (hasAirColumn(level, spawnPos)
            && mob.checkSpawnObstruction(level)) {
          return spawnPos.immutable();
        }
      }
    }

    return null;
  }

  private static List<BlockPos> distributeCandidatesByChunk(
      List<BlockPos> candidates,
      int targetCount,
      java.util.Random random
  ) {
    Map<Long, List<BlockPos>> byChunk = new LinkedHashMap<>();
    for (BlockPos candidate : candidates) {
      long chunk = ChunkPos.pack(
          Math.floorDiv(candidate.getX(), 16),
          Math.floorDiv(candidate.getZ(), 16)
      );
      byChunk.computeIfAbsent(chunk, _ -> new ArrayList<>()).add(candidate);
    }

    List<List<BlockPos>> chunks = new ArrayList<>(byChunk.values());
    for (List<BlockPos> chunkCandidates : chunks) {
      Collections.shuffle(chunkCandidates, random);
    }
    Collections.shuffle(chunks, random);

    List<BlockPos> distributed = new ArrayList<>(Math.min(targetCount, candidates.size()));
    while (distributed.size() < targetCount) {
      boolean addedThisRound = false;

      for (List<BlockPos> chunkCandidates : chunks) {
        if (chunkCandidates.isEmpty() || distributed.size() >= targetCount) continue;

        distributed.add(chunkCandidates.removeLast());
        addedThisRound = true;
      }

      if (!addedThisRound) break;
    }

    return distributed;
  }

  static <T extends Mob> GroundSpawnCandidates findGroundSpawnCandidatesByCeiling(
      WorldGenLevel level,
      BoundingBox box,
      EntityType<T> entityType,
      Predicate<BlockState> spawnFloorPredicate,
      Predicate<BlockState> interiorFloorPredicate,
      int maxCeilingDistance
  ) {
    T probeMob = entityType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
    if (probeMob == null) return GroundSpawnCandidates.empty();

    List<BlockPos> underCoverInterior = new ArrayList<>();
    List<BlockPos> openAir = new ArrayList<>();
    List<BlockPos> fallback = new ArrayList<>();

    int minX = box.minX() + 1;
    int maxX = box.maxX() - 1;
    int minZ = box.minZ() + 1;
    int maxZ = box.maxZ() - 1;
    if (minX > maxX) {
      minX = box.minX();
      maxX = box.maxX();
    }
    if (minZ > maxZ) {
      minZ = box.minZ();
      maxZ = box.maxZ();
    }

    int minFloorY = box.minY() - 1;
    int maxFloorY = box.maxY() - 1;
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (int y = maxFloorY; y >= minFloorY; y--) {
          mutablePos.set(x, y, z);
          BlockState floorState = level.getBlockState(mutablePos);
          if (floorState.isAir()
              || !spawnFloorPredicate.test(floorState)
              || !floorState.isFaceSturdy(level, mutablePos, Direction.UP)) {
            continue;
          }

          BlockPos spawnPos = mutablePos.above().immutable();
          if (!hasAirColumn(level, spawnPos)) {
            continue;
          }

          probeMob.snapTo(
              spawnPos.getX() + 0.5D,
              spawnPos.getY(),
              spawnPos.getZ() + 0.5D,
              0.0F,
              0.0F
          );
          if (!probeMob.checkSpawnObstruction(level)) {
            continue;
          }

          boolean underCover = hasCeilingNearby(level, spawnPos, maxCeilingDistance);
          fallback.add(spawnPos);
          if (underCover && interiorFloorPredicate.test(floorState)) {
            underCoverInterior.add(spawnPos);
          } else if (!underCover) {
            openAir.add(spawnPos);
          }
        }
      }
    }

    return new GroundSpawnCandidates(underCoverInterior, openAir, fallback);
  }

  static <T extends Mob> int spawnPersistentAirMobs(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      int horizontalRadius,
      int minYOffset,
      int maxYOffset
  ) {
    int spawned = 0;

    for (int index = 0; index < count; index++) {
      T mob = entityType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
      if (mob == null) return spawned;

      BlockPos spawnPos = findAirSpawnPos(
          level,
          random,
          box,
          mob,
          horizontalRadius,
          minYOffset,
          maxYOffset
      );
      if (spawnPos == null) continue;

      prepareMob(level, random, mob, spawnPos);
      level.addFreshEntityWithPassengers(mob);
      spawned++;
    }

    return spawned;
  }

  static <T extends Mob> int spawnPersistentAirMobsAboveFloors(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      Predicate<BlockState> floorPredicate,
      int minYOffset,
      int maxYOffset
  ) {
    int spawned = 0;

    for (int index = 0; index < count; index++) {
      T mob = entityType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
      if (mob == null) return spawned;

      BlockPos spawnPos = findAirSpawnPosAboveFloor(
          level,
          random,
          box,
          mob,
          floorPredicate,
          minYOffset,
          maxYOffset
      );
      if (spawnPos == null) continue;

      prepareMob(level, random, mob, spawnPos);
      level.addFreshEntityWithPassengers(mob);
      spawned++;
    }

    return spawned;
  }

  static <T extends Mob> int spawnPersistentGroundMobsDistributedByFloor(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      Predicate<BlockState> floorPredicate
  ) {
    return spawnPersistentGroundMobs(
        level,
        random,
        box,
        entityType,
        count,
        floorPredicate,
        true,
        _ -> true
    );
  }

  static <T extends Mob> int spawnPersistentGroundMobsFromCandidates(
      WorldGenLevel level,
      RandomSource random,
      EntityType<T> entityType,
      int count,
      List<BlockPos> candidates,
      boolean distributeByChunk,
      Predicate<BlockPos> spawnSitePredicate,
      Consumer<BlockPos> onSpawn
  ) {
    if (count <= 0 || candidates.isEmpty()) return 0;

    java.util.Random shuffleRandom = new java.util.Random(random.nextLong());
    List<BlockPos> spawnOrder = new ArrayList<>(candidates);
    Collections.shuffle(spawnOrder, shuffleRandom);
    if (distributeByChunk) {
      spawnOrder = distributeCandidatesByChunk(spawnOrder, spawnOrder.size(), shuffleRandom);
    }

    int spawned = 0;
    for (BlockPos spawnPos : spawnOrder) {
      if (spawned >= count) return spawned;
      if (!spawnSitePredicate.test(spawnPos)) continue;

      T mob = entityType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
      if (mob == null) return spawned;

      mob.snapTo(
          spawnPos.getX() + 0.5D,
          spawnPos.getY(),
          spawnPos.getZ() + 0.5D,
          0.0F,
          0.0F
      );
      if (!mob.checkSpawnObstruction(level)) {
        continue;
      }

      prepareMob(level, random, mob, spawnPos);
      level.addFreshEntityWithPassengers(mob);
      onSpawn.accept(spawnPos);
      spawned++;
    }

    return spawned;
  }

  static <T extends Mob> int spawnPersistentGroundMobsOpenAir(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      Predicate<BlockState> floorPredicate,
      int maxCeilingDistance
  ) {
    return spawnPersistentGroundMobs(
        level,
        random,
        box,
        entityType,
        count,
        floorPredicate,
        false,
        spawnPos -> !hasCeilingNearby(level, spawnPos, maxCeilingDistance)
    );
  }

  static <T extends Mob> int spawnPersistentGroundMobsUnderCoverDistributedByFloor(
      WorldGenLevel level,
      RandomSource random,
      BoundingBox box,
      EntityType<T> entityType,
      int count,
      Predicate<BlockState> floorPredicate,
      int maxCeilingDistance
  ) {
    return spawnPersistentGroundMobs(
        level,
        random,
        box,
        entityType,
        count,
        floorPredicate,
        true,
        spawnPos -> hasCeilingNearby(level, spawnPos, maxCeilingDistance)
    );
  }

  record GroundSpawnCandidates(
      List<BlockPos> underCoverInterior,
      List<BlockPos> openAir,
      List<BlockPos> fallback
  ) {

    private static GroundSpawnCandidates empty() {
      return new GroundSpawnCandidates(List.of(), List.of(), List.of());
    }

  }

}
