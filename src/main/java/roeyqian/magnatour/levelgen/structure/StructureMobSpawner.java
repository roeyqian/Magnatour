/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.structure;

// Java Standard
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.phys.AABB;

// JSpecify
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.supreme.BellRinger;
import roeyqian.magnatour.entity.supreme.BellSoul;
import roeyqian.magnatour.registry.content.SupremeEntities;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;
import roeyqian.magnatour.registry.worldgen.CustomStructures;

public final class StructureMobSpawner {

  private static final int DIAMOND_CITY_SCAN_RADIUS_CHUNKS = 12;
  private static final int DIAMOND_CITY_SHARED_SPAWN_DIVISOR = 3;
  private static final int DIAMOND_CITY_SITE_CEILING_SCAN_DISTANCE = 4;
  private static final int DIAMOND_CITY_SPAWN_SITE_CHUNK_SCAN_BUDGET = 8;
  private static final int DIAMOND_CITY_TARGET_OBSIDIAN_GOLEMS = 64;
  private static final int GOLD_BELL_TOWER_MAX_POPULATION = 64;
  private static final int GOLD_BELL_TOWER_SCAN_RADIUS_CHUNKS = 6;
  private static final int GOLD_BELL_TOWER_TARGET_BELL_RINGERS = 32;
  private static final int GOLD_BELL_TOWER_TARGET_BELL_SOULS = 24;
  private static final int MAX_SPAWNS_PER_CYCLE = 8;
  private static final int TOWN_OF_FORTUNE_MAX_SPAWNS_PER_CYCLE = 2;
  private static final int TOWN_OF_FORTUNE_SCAN_RADIUS_CHUNKS = 10;
  private static final int TOWN_OF_FORTUNE_SPAWN_ATTEMPTS = 48;
  private static final int TOWN_OF_FORTUNE_TARGET_ANIMALS = 16;
  private static final int TOWN_OF_FORTUNE_TARGET_VILLAGERS = 16;

  private static final long SPAWN_INTERVAL_TICKS = 20L;
  private static final long STATE_PRUNE_INTERVAL_TICKS = 200L;
  private static final long STATE_TTL_TICKS = 1200L;
  private static final long TOWN_OF_FORTUNE_SPAWN_INTERVAL_TICKS = 1200L;

  private static final double DIAMOND_CITY_COUNT_HORIZONTAL_PADDING = 6.0D;
  private static final double DIAMOND_CITY_COUNT_VERTICAL_PADDING = 6.0D;
  private static final double DIAMOND_CITY_SPAWN_SEPARATION = 8.0D;
  private static final double GOLD_BELL_TOWER_COUNT_HORIZONTAL_PADDING = 8.0D;
  private static final double GOLD_BELL_TOWER_COUNT_VERTICAL_PADDING = 12.0D;
  private static final double TOWN_OF_FORTUNE_COUNT_HORIZONTAL_PADDING = 8.0D;
  private static final double TOWN_OF_FORTUNE_COUNT_VERTICAL_PADDING = 16.0D;

  private static boolean tickEventRegistered = false;

  private static final StructureSpawnProfile DIAMOND_CITY_PROFILE =
      new StructureSpawnProfile(
          "diamond_city",
          CustomDimensions.ORE_CONTINENT,
          CustomStructures.DIAMOND_CITY,
          DIAMOND_CITY_SCAN_RADIUS_CHUNKS,
          96.0D,
          DIAMOND_CITY_TARGET_OBSIDIAN_GOLEMS,
          DIAMOND_CITY_TARGET_OBSIDIAN_GOLEMS,
          SPAWN_INTERVAL_TICKS,
          SPAWN_INTERVAL_TICKS,
          DIAMOND_CITY_COUNT_HORIZONTAL_PADDING,
          DIAMOND_CITY_COUNT_VERTICAL_PADDING,
          StructureMobSpawner::countDiamondCityMobs,
          StructureMobSpawner::spawnDiamondCityMobs
      );
  private static final StructureSpawnProfile GOLD_BELL_TOWER_PROFILE =
      new StructureSpawnProfile(
          "gold_bell_tower",
          CustomDimensions.HARVEST_CONTINENT,
          CustomStructures.GOLD_BELL_TOWER,
          GOLD_BELL_TOWER_SCAN_RADIUS_CHUNKS,
          48.0D,
          GOLD_BELL_TOWER_TARGET_BELL_RINGERS + GOLD_BELL_TOWER_TARGET_BELL_SOULS,
          GOLD_BELL_TOWER_MAX_POPULATION,
          SPAWN_INTERVAL_TICKS,
          SPAWN_INTERVAL_TICKS,
          GOLD_BELL_TOWER_COUNT_HORIZONTAL_PADDING,
          GOLD_BELL_TOWER_COUNT_VERTICAL_PADDING,
          StructureMobSpawner::countGoldBellTowerMobs,
          StructureMobSpawner::spawnGoldBellTowerMobs
      );
  private static final StructureSpawnProfile TOWN_OF_FORTUNE_PROFILE =
      new StructureSpawnProfile(
          "town_of_fortune",
          CustomDimensions.ORE_CONTINENT,
          CustomStructures.TOWN_OF_FORTUNE,
          TOWN_OF_FORTUNE_SCAN_RADIUS_CHUNKS,
          80.0D,
          TOWN_OF_FORTUNE_TARGET_VILLAGERS + TOWN_OF_FORTUNE_TARGET_ANIMALS,
          TOWN_OF_FORTUNE_TARGET_VILLAGERS + TOWN_OF_FORTUNE_TARGET_ANIMALS,
          TOWN_OF_FORTUNE_SPAWN_INTERVAL_TICKS,
          TOWN_OF_FORTUNE_SPAWN_INTERVAL_TICKS,
          TOWN_OF_FORTUNE_COUNT_HORIZONTAL_PADDING,
          TOWN_OF_FORTUNE_COUNT_VERTICAL_PADDING,
          StructureMobSpawner::countTownOfFortuneMobs,
          StructureMobSpawner::spawnTownOfFortuneMobs
      );

  private static final Map<StructureInstanceKey, SpawnState> SPAWN_STATES =
      new HashMap<>();

  private static final Predicate<BlockState> DIAMOND_CITY_INTERIOR_FLOOR =
      floorState -> floorState.is(Blocks.OBSIDIAN);
  private static final Predicate<BlockState> DIAMOND_CITY_SPAWN_FLOOR =
      floorState -> !floorState.is(Blocks.POLISHED_DEEPSLATE);
  private static final Predicate<BlockState> GOLD_BELL_TOWER_SPAWN_FLOOR =
      floorState -> floorState.is(Blocks.POLISHED_BLACKSTONE)
          || floorState.is(Blocks.GILDED_BLACKSTONE);

  private StructureMobSpawner() {}

  public static void registerTickEvent() {
    if (tickEventRegistered) return;
    tickEventRegistered = true;

    ServerTickEvents.END_SERVER_TICK.register(server -> {
      long currentTick = server.getTickCount();
      if (currentTick % SPAWN_INTERVAL_TICKS != 0L) return;

      processLevel(
          server.getLevel(GOLD_BELL_TOWER_PROFILE.dimensionKey()),
          GOLD_BELL_TOWER_PROFILE,
          currentTick
      );
      processLevel(
          server.getLevel(DIAMOND_CITY_PROFILE.dimensionKey()),
          DIAMOND_CITY_PROFILE,
          currentTick
      );
      processLevel(
          server.getLevel(TOWN_OF_FORTUNE_PROFILE.dimensionKey()),
          TOWN_OF_FORTUNE_PROFILE,
          currentTick
      );

      if (currentTick % STATE_PRUNE_INTERVAL_TICKS == 0L) {
        pruneStates(currentTick);
      }
    });
  }

  private static void processLevel(
      ServerLevel level,
      StructureSpawnProfile profile,
      long currentTick
  ) {
    if (level == null) return;

    List<ServerPlayer> players = level.players();
    if (players.isEmpty()) return;

    Set<Long> scannedChunks = new HashSet<>();
    Set<StructureInstanceKey> seenStructures = new HashSet<>();

    for (ServerPlayer player : players) {
      ChunkPos playerChunk = player.chunkPosition();

      for (int offsetX = -profile.playerChunkScanRadius();
           offsetX <= profile.playerChunkScanRadius();
           offsetX++) {
        int chunkX = playerChunk.x() + offsetX;

        for (int offsetZ = -profile.playerChunkScanRadius();
             offsetZ <= profile.playerChunkScanRadius();
             offsetZ++) {
          int chunkZ = playerChunk.z() + offsetZ;
          long packedChunk = ChunkPos.pack(chunkX, chunkZ);
          if (!scannedChunks.add(packedChunk)
              || !level.getChunkSource().hasChunk(chunkX, chunkZ)) {
            continue;
          }

          for (StructureStart start : level.structureManager().startsForStructure(
              new ChunkPos(chunkX, chunkZ),
              structure -> structure.type() == profile.structureType()
          )) {
            if (!start.isValid()) continue;
            if (!playerNearStructure(players, start.getBoundingBox(), profile.activationPadding())) {
              continue;
            }

            StructureInstanceKey key = new StructureInstanceKey(
                level.dimension(),
                profile.id(),
                start.getChunkPos().pack()
            );
            if (!seenStructures.add(key)) continue;

            processStructureStart(level, profile, start, key, currentTick);
          }
        }
      }
    }
  }

  private static void pruneStates(
      long currentTick
  ) {
    SPAWN_STATES.entrySet().removeIf(
        entry -> currentTick - entry.getValue().lastSeenTick > STATE_TTL_TICKS
    );
  }

  private static boolean playerNearStructure(
      List<ServerPlayer> players,
      BoundingBox structureBox,
      double activationPadding
  ) {
    AABB activationBox = AABB.of(structureBox).inflate(
        activationPadding,
        24.0D,
        activationPadding
    );

    for (ServerPlayer player : players) {
      if (player.isAlive() && activationBox.contains(player.position())) {
        return true;
      }
    }

    return false;
  }

  private static void processStructureStart(
      ServerLevel level,
      StructureSpawnProfile profile,
      StructureStart start,
      StructureInstanceKey key,
      long currentTick
  ) {
    SpawnState state = SPAWN_STATES.computeIfAbsent(
        key,
        _ -> new SpawnState(
            currentTick,
            Math.max(0L, currentTick - profile.boostIntervalTicks())
        )
    );
    state.lastSeenTick = currentTick;

    BoundingBox structureBox = start.getBoundingBox();
    int currentCount = profile.mobCounter().count(level, mobCountBox(structureBox, profile));
    if (currentCount >= profile.maxPopulation()) return;

    long interval = currentCount < profile.boostThreshold()
        ? profile.boostIntervalTicks()
        : profile.normalIntervalTicks();
    if (currentTick - state.lastSpawnTick < interval) return;

    int spawned = profile.mobSpawner().spawn(
        level,
        level.getRandom(),
        structureBox,
        profile,
        currentCount,
        state
    );
    if (spawned > 0 || profile == TOWN_OF_FORTUNE_PROFILE) {
      state.lastSpawnTick = currentTick;
    }
  }

  private static AABB mobCountBox(
      BoundingBox structureBox,
      StructureSpawnProfile profile
  ) {
    return AABB.of(structureBox).inflate(
        profile.mobCountHorizontalPadding(),
        profile.mobCountVerticalPadding(),
        profile.mobCountHorizontalPadding()
    );
  }

  private static boolean hasAirColumn(
      WorldGenLevel level,
      BlockPos pos
  ) {
    return level.getBlockState(pos).isAir()
        && level.getBlockState(pos.above()).isAir();
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

  private static int randomBetween(
      RandomSource random,
      int min,
      int max
  ) {
    if (min >= max) return min;
    return min + random.nextInt(max - min + 1);
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

    for (int attempt = 0; attempt < 48; attempt++) {
      int x = randomBetween(random, minX, maxX);
      int z = randomBetween(random, minZ, maxZ);
      for (int y = maxFloorY; y >= minFloorY; y--) {
        floorPos.set(x, y, z);
        BlockState floorState = level.getBlockState(floorPos);
        if (floorState.isAir() || !floorPredicate.test(floorState)) continue;

        int spawnY = y + randomBetween(random, minYOffset, maxYOffset);
        spawnPos.set(x, spawnY, z);
        mob.snapTo(x + 0.5D, spawnY, z + 0.5D, 0.0F, 0.0F);
        if (hasAirColumn(level, spawnPos)
            && mob.checkSpawnObstruction(level)) {
          return spawnPos.immutable();
        }
      }
    }

    return null;
  }

  private static int countEntities(
      ServerLevel level,
      AABB box,
      Predicate<Entity> predicate
  ) {
    return level.getEntities((Entity) null, box, predicate).size();
  }

  private static boolean isTownOfFortuneAnimal(
      EntityType<?> entityType
  ) {
    return entityType == EntityTypes.SHEEP
        || entityType == EntityTypes.PIG
        || entityType == EntityTypes.CHICKEN
        || entityType == EntityTypes.COW;
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

  private static <T extends Mob> int spawnPersistentGroundMobsFromCandidates(
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
      if (!mob.checkSpawnObstruction(level)) continue;

      prepareMob(level, random, mob, spawnPos);
      level.addFreshEntityWithPassengers(mob);
      onSpawn.accept(spawnPos);
      spawned++;
    }

    return spawned;
  }

  private static boolean isDiamondCitySpawnSiteAvailable(
      ServerLevel level,
      DiamondCitySpawnSites spawnSites,
      Set<BlockPos> reservedSpawnSites,
      BlockPos pos
  ) {
    if (!spawnSites.isLoaded(level, pos)) return false;
    for (BlockPos reservedSpawnSite : reservedSpawnSites) {
      double xDistance = pos.getX() - reservedSpawnSite.getX();
      double yDistance = pos.getY() - reservedSpawnSite.getY();
      double zDistance = pos.getZ() - reservedSpawnSite.getZ();
      if (xDistance * xDistance
              + yDistance * yDistance
              + zDistance * zDistance
          < DIAMOND_CITY_SPAWN_SEPARATION * DIAMOND_CITY_SPAWN_SEPARATION) {
        return false;
      }
    }

    AABB separationBox = new AABB(
        pos.getX() - DIAMOND_CITY_SPAWN_SEPARATION,
        pos.getY() - DIAMOND_CITY_SPAWN_SEPARATION,
        pos.getZ() - DIAMOND_CITY_SPAWN_SEPARATION,
        pos.getX() + 1.0D + DIAMOND_CITY_SPAWN_SEPARATION,
        pos.getY() + 1.0D + DIAMOND_CITY_SPAWN_SEPARATION,
        pos.getZ() + 1.0D + DIAMOND_CITY_SPAWN_SEPARATION
    );
    return level.getEntities(
        (Entity) null,
        separationBox,
        entity -> entity.isAlive() && entity.getType() == SupremeEntities.OBSIDIAN_GOLEM
    ).isEmpty();
  }

  private static <T extends Mob> int spawnPersistentGroundMobsDistributedByFloor(
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

  private static <T extends Mob> int spawnPersistentAirMobsAboveFloors(
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

  private static <T extends Mob> boolean spawnTownOfFortuneMob(
      ServerLevel level,
      RandomSource random,
      BoundingBox structureBox,
      EntityType<T> entityType
  ) {
    T mob = entityType.create(level, EntitySpawnReason.STRUCTURE);
    if (mob == null) return false;

    for (int attempt = 0; attempt < TOWN_OF_FORTUNE_SPAWN_ATTEMPTS; attempt++) {
      int x = randomBetween(random, structureBox.minX(), structureBox.maxX());
      int z = randomBetween(random, structureBox.minZ(), structureBox.maxZ());
      if (!level.getChunkSource().hasChunk(x >> 4, z >> 4)) continue;

      int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
      BlockPos floorPos = new BlockPos(x, topY - 1, z);
      BlockPos spawnPos = floorPos.above();
      BlockState floorState = level.getBlockState(floorPos);
      if (floorState.isAir()
          || !floorState.isFaceSturdy(level, floorPos, Direction.UP)
          || !hasAirColumn(level, spawnPos)) {
        continue;
      }
      if (level.getNearestPlayer(
          spawnPos.getX() + 0.5D,
          spawnPos.getY(),
          spawnPos.getZ() + 0.5D,
          16.0D,
          false
      ) != null) {
        continue;
      }

      mob.snapTo(
          spawnPos.getX() + 0.5D,
          spawnPos.getY(),
          spawnPos.getZ() + 0.5D,
          0.0F,
          0.0F
      );
      if (!mob.checkSpawnObstruction(level)) continue;

      prepareMob(level, random, mob, spawnPos);
      level.addFreshEntityWithPassengers(mob);
      return true;
    }

    return false;
  }

  private static EntityType<? extends Animal> pickTownOfFortuneAnimal(
      RandomSource random
  ) {
    return switch (random.nextInt(4)) {
      case 0 -> EntityTypes.SHEEP;
      case 1 -> EntityTypes.PIG;
      case 2 -> EntityTypes.CHICKEN;
      default -> EntityTypes.COW;
    };
  }

  private static int countDiamondCityMobs(
      ServerLevel level,
      AABB box
  ) {
    return countEntities(
        level,
        box,
        entity -> entity.isAlive() && entity.getType() == SupremeEntities.OBSIDIAN_GOLEM
    );
  }

  private static int countGoldBellTowerMobs(
      ServerLevel level,
      AABB box
  ) {
    return countEntities(
        level,
        box,
        entity -> entity.isAlive()
            && (entity instanceof BellRinger || entity instanceof BellSoul)
    );
  }

  private static int countTownOfFortuneMobs(
      ServerLevel level,
      AABB box
  ) {
    int villagers = countEntities(
        level,
        box,
        entity -> entity.isAlive() && entity.getType() == EntityTypes.VILLAGER
    );
    int animals = countEntities(
        level,
        box,
        entity -> entity.isAlive() && isTownOfFortuneAnimal(entity.getType())
    );
    return Math.min(villagers, TOWN_OF_FORTUNE_TARGET_VILLAGERS)
        + Math.min(animals, TOWN_OF_FORTUNE_TARGET_ANIMALS);
  }

  private static <T extends Mob> GroundSpawnCandidates findGroundSpawnCandidatesByCeiling(
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
          if (!hasAirColumn(level, spawnPos)) continue;

          probeMob.snapTo(
              spawnPos.getX() + 0.5D,
              spawnPos.getY(),
              spawnPos.getZ() + 0.5D,
              0.0F,
              0.0F
          );
          if (!probeMob.checkSpawnObstruction(level)) continue;

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

  private static int spawnDiamondCityMobs(
      ServerLevel level,
      RandomSource random,
      BoundingBox structureBox,
      StructureSpawnProfile profile,
      int currentCount,
      SpawnState state
  ) {
    if (currentCount >= profile.maxPopulation()) return 0;

    int spawnBatch = Math.min(
        DIAMOND_CITY_TARGET_OBSIDIAN_GOLEMS - currentCount,
        MAX_SPAWNS_PER_CYCLE
    );
    if (spawnBatch <= 0) return 0;

    int indoorTarget = spawnBatch >= 2
        ? Math.max(1, spawnBatch / DIAMOND_CITY_SHARED_SPAWN_DIVISOR)
        : spawnBatch;
    int outdoorTarget = spawnBatch - indoorTarget >= 1
        ? Math.min(indoorTarget, spawnBatch - indoorTarget)
        : 0;

    DiamondCitySpawnSites spawnSites = state.diamondCitySpawnSites(structureBox);
    spawnSites.scanLoadedChunks(
        level,
        indoorTarget,
        outdoorTarget,
        spawnBatch * 2
    );

    int spawned = 0;
    Set<BlockPos> reservedSpawnSites = new HashSet<>();
    if (indoorTarget > 0) {
      spawned += spawnPersistentGroundMobsFromCandidates(
          level,
          random,
          SupremeEntities.OBSIDIAN_GOLEM,
          indoorTarget,
          spawnSites.underCoverInterior,
          true,
          pos -> isDiamondCitySpawnSiteAvailable(level, spawnSites, reservedSpawnSites, pos),
          reservedSpawnSites::add
      );
    }
    if (outdoorTarget > 0) {
      spawned += spawnPersistentGroundMobsFromCandidates(
          level,
          random,
          SupremeEntities.OBSIDIAN_GOLEM,
          outdoorTarget,
          spawnSites.openAir,
          true,
          pos -> isDiamondCitySpawnSiteAvailable(level, spawnSites, reservedSpawnSites, pos),
          reservedSpawnSites::add
      );
    }

    int remaining = spawnBatch - spawned;
    if (remaining > 0) {
      spawned += spawnPersistentGroundMobsFromCandidates(
          level,
          random,
          SupremeEntities.OBSIDIAN_GOLEM,
          remaining,
          spawnSites.fallback,
          true,
          pos -> isDiamondCitySpawnSiteAvailable(level, spawnSites, reservedSpawnSites, pos),
          reservedSpawnSites::add
      );
    }

    return spawned;
  }

  private static int spawnGoldBellTowerMobs(
      ServerLevel level,
      RandomSource random,
      BoundingBox structureBox,
      StructureSpawnProfile profile,
      int currentCount,
      SpawnState state
  ) {
    int remainingCapacity = profile.maxPopulation() - currentCount;
    if (remainingCapacity <= 0) return 0;

    int bellRingers = countEntities(
        level,
        mobCountBox(structureBox, profile),
        entity -> entity.isAlive() && entity instanceof BellRinger
    );
    int bellSouls = countEntities(
        level,
        mobCountBox(structureBox, profile),
        entity -> entity.isAlive() && entity instanceof BellSoul
    );

    int missingRingers = Math.max(0, GOLD_BELL_TOWER_TARGET_BELL_RINGERS - bellRingers);
    int missingSouls = Math.max(0, GOLD_BELL_TOWER_TARGET_BELL_SOULS - bellSouls);
    if (missingRingers <= 0 && missingSouls <= 0) return 0;

    int cycleBudget = missingRingers + missingSouls;
    cycleBudget = Math.min(cycleBudget, remainingCapacity);
    cycleBudget = Math.min(cycleBudget, MAX_SPAWNS_PER_CYCLE);
    if (cycleBudget <= 0) return 0;

    int spawned = 0;
    if (missingRingers > 0) {
      int ringersSpawned = spawnPersistentGroundMobsDistributedByFloor(
          level,
          random,
          structureBox,
          SupremeEntities.BELL_RINGER,
          Math.min(missingRingers, cycleBudget),
          GOLD_BELL_TOWER_SPAWN_FLOOR
      );
      spawned += ringersSpawned;
      cycleBudget -= ringersSpawned;

      if (ringersSpawned == 0 && level.getGameTime() % 100L == 0L) {
        Magnatour.LOGGER.warn(
            "[GoldBellTower] BellRinger respawn failed at {} missing={} currentRingers={} currentSouls={}",
            structureBox,
            missingRingers,
            bellRingers,
            bellSouls
        );
      }
    }

    if (cycleBudget > 0 && missingSouls > 0) {
      spawned += spawnPersistentAirMobsAboveFloors(
          level,
          random,
          structureBox,
          SupremeEntities.BELL_SOUL,
          Math.min(missingSouls, cycleBudget),
          GOLD_BELL_TOWER_SPAWN_FLOOR,
          1,
          3
      );
    }

    return spawned;
  }

  private static int spawnTownOfFortuneMobs(
      ServerLevel level,
      RandomSource random,
      BoundingBox structureBox,
      StructureSpawnProfile profile,
      int currentCount,
      SpawnState state
  ) {
    AABB countBox = mobCountBox(structureBox, profile);
    int villagers = countEntities(
        level,
        countBox,
        entity -> entity.isAlive() && entity.getType() == EntityTypes.VILLAGER
    );
    int animals = countEntities(
        level,
        countBox,
        entity -> entity.isAlive() && isTownOfFortuneAnimal(entity.getType())
    );
    int missingVillagers = Math.max(0, TOWN_OF_FORTUNE_TARGET_VILLAGERS - villagers);
    int missingAnimals = Math.max(0, TOWN_OF_FORTUNE_TARGET_ANIMALS - animals);
    int cycleBudget = Math.min(
        TOWN_OF_FORTUNE_MAX_SPAWNS_PER_CYCLE,
        missingVillagers + missingAnimals
    );
    if (cycleBudget <= 0) return 0;

    int spawned = 0;
    if (missingVillagers > 0 && cycleBudget > 0
        && spawnTownOfFortuneMob(level, random, structureBox, EntityTypes.VILLAGER)) {
      spawned++;
      cycleBudget--;
    }
    if (missingAnimals > 0 && cycleBudget > 0
        && spawnTownOfFortuneMob(
            level,
            random,
            structureBox,
            pickTownOfFortuneAnimal(random)
        )) {
      spawned++;
      cycleBudget--;
    }

    if (cycleBudget > 0 && missingVillagers > 1
        && spawnTownOfFortuneMob(level, random, structureBox, EntityTypes.VILLAGER)) {
      spawned++;
      cycleBudget--;
    }
    if (cycleBudget > 0 && missingAnimals > 1
        && spawnTownOfFortuneMob(
            level,
            random,
            structureBox,
            pickTownOfFortuneAnimal(random)
        )) {
      spawned++;
    }

    return spawned;
  }

  private static final class DiamondCitySpawnSites {

    private final Set<Long> scannedChunks = new HashSet<>();

    private final List<Long> chunkScanOrder = new ArrayList<>();

    private final List<BlockPos> fallback = new ArrayList<>();
    private final List<BlockPos> openAir = new ArrayList<>();
    private final List<BlockPos> underCoverInterior = new ArrayList<>();

    private final BoundingBox structureBox;

    private DiamondCitySpawnSites(
        BoundingBox structureBox
    ) {
      this.structureBox = structureBox;

      int minChunkX = Math.floorDiv(this.structureBox.minX(), 16);
      int maxChunkX = Math.floorDiv(this.structureBox.maxX(), 16);
      int minChunkZ = Math.floorDiv(this.structureBox.minZ(), 16);
      int maxChunkZ = Math.floorDiv(this.structureBox.maxZ(), 16);
      for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
          this.chunkScanOrder.add(ChunkPos.pack(chunkX, chunkZ));
        }
      }
      java.util.Collections.shuffle(
          this.chunkScanOrder,
          new java.util.Random(ChunkPos.pack(minChunkX, minChunkZ))
      );
    }

    private boolean isLoaded(
        ServerLevel level,
        BlockPos pos
    ) {
      int chunkX = Math.floorDiv(pos.getX(), 16);
      int chunkZ = Math.floorDiv(pos.getZ(), 16);
      return this.scannedChunks.contains(ChunkPos.pack(chunkX, chunkZ))
          && level.getChunkSource().hasChunk(chunkX, chunkZ);
    }

    private int loadedCandidateCount(
        ServerLevel level,
        List<BlockPos> candidates,
        int target
    ) {
      if (target <= 0) return 0;

      int count = 0;
      for (BlockPos candidate : candidates) {
        if (!isLoaded(level, candidate)) continue;

        count++;
        if (count >= target) return count;
      }

      return count;
    }

    private boolean hasCachedCandidates(
        ServerLevel level,
        int targetUnderCoverInterior,
        int targetOpenAir,
        int targetFallback
    ) {
      return loadedCandidateCount(level, this.underCoverInterior, targetUnderCoverInterior)
              >= targetUnderCoverInterior
          && loadedCandidateCount(level, this.openAir, targetOpenAir) >= targetOpenAir
          && loadedCandidateCount(level, this.fallback, targetFallback) >= targetFallback;
    }

    private void scanChunk(
        ServerLevel level,
        int chunkX,
        int chunkZ
    ) {
      int chunkMinX = chunkX << 4;
      int chunkMinZ = chunkZ << 4;
      BoundingBox chunkBox = new BoundingBox(
          Math.max(this.structureBox.minX(), chunkMinX),
          this.structureBox.minY(),
          Math.max(this.structureBox.minZ(), chunkMinZ),
          Math.min(this.structureBox.maxX(), chunkMinX + 15),
          this.structureBox.maxY(),
          Math.min(this.structureBox.maxZ(), chunkMinZ + 15)
      );

      GroundSpawnCandidates candidates =
          findGroundSpawnCandidatesByCeiling(
              level,
              chunkBox,
              SupremeEntities.OBSIDIAN_GOLEM,
              DIAMOND_CITY_SPAWN_FLOOR,
              DIAMOND_CITY_INTERIOR_FLOOR,
              DIAMOND_CITY_SITE_CEILING_SCAN_DISTANCE
          );
      this.underCoverInterior.addAll(candidates.underCoverInterior());
      this.openAir.addAll(candidates.openAir());
      this.fallback.addAll(candidates.fallback());
    }

    private boolean matches(
        BoundingBox other
    ) {
      return this.structureBox.minX() == other.minX()
          && this.structureBox.minY() == other.minY()
          && this.structureBox.minZ() == other.minZ()
          && this.structureBox.maxX() == other.maxX()
          && this.structureBox.maxY() == other.maxY()
          && this.structureBox.maxZ() == other.maxZ();
    }

    private void scanLoadedChunks(
        ServerLevel level,
        int targetUnderCoverInterior,
        int targetOpenAir,
        int targetFallback
    ) {
      if (hasCachedCandidates(level, targetUnderCoverInterior, targetOpenAir, targetFallback)) {
        return;
      }

      int scannedThisCall = 0;

      for (long packedChunk : this.chunkScanOrder) {
        int chunkX = ChunkPos.getX(packedChunk);
        int chunkZ = ChunkPos.getZ(packedChunk);
        if (this.scannedChunks.contains(packedChunk)
            || !level.getChunkSource().hasChunk(chunkX, chunkZ)) {
          continue;
        }

        scanChunk(level, chunkX, chunkZ);
        this.scannedChunks.add(packedChunk);
        scannedThisCall++;
        if (hasCachedCandidates(level, targetUnderCoverInterior, targetOpenAir, targetFallback)
            || scannedThisCall >= DIAMOND_CITY_SPAWN_SITE_CHUNK_SCAN_BUDGET) {
          return;
        }
      }
    }

  }

  private record GroundSpawnCandidates(
      List<BlockPos> underCoverInterior,
      List<BlockPos> openAir,
      List<BlockPos> fallback
  ) {

    private static GroundSpawnCandidates empty() {
      return new GroundSpawnCandidates(List.of(), List.of(), List.of());
    }

  }

  @FunctionalInterface
  private interface MobCounter {

    int count(
        ServerLevel level,
        AABB box
    );

  }

  @FunctionalInterface
  private interface MobSpawner {

    int spawn(
        ServerLevel level,
        RandomSource random,
        BoundingBox structureBox,
        StructureSpawnProfile profile,
        int currentCount,
        SpawnState state
    );

  }

  private static final class SpawnState {

    private long lastSeenTick;
    private long lastSpawnTick;

    private DiamondCitySpawnSites diamondCitySpawnSites;

    private SpawnState(
        long lastSeenTick,
        long lastSpawnTick
    ) {
      this.lastSeenTick = lastSeenTick;
      this.lastSpawnTick = lastSpawnTick;
    }

    private DiamondCitySpawnSites diamondCitySpawnSites(
        BoundingBox structureBox
    ) {
      if (this.diamondCitySpawnSites == null
          || !this.diamondCitySpawnSites.matches(structureBox)) {
        this.diamondCitySpawnSites = new DiamondCitySpawnSites(structureBox);
      }

      return this.diamondCitySpawnSites;
    }

  }

  private record StructureInstanceKey(
      ResourceKey<Level> dimensionKey,
      String structureId,
      long startChunk
  ) {}

  private record StructureSpawnProfile(
      String id,
      ResourceKey<Level> dimensionKey,
      StructureType<?> structureType,
      int playerChunkScanRadius,
      double activationPadding,
      int boostThreshold,
      int maxPopulation,
      long boostIntervalTicks,
      long normalIntervalTicks,
      double mobCountHorizontalPadding,
      double mobCountVerticalPadding,
      MobCounter mobCounter,
      MobSpawner mobSpawner
  ) {}

}
