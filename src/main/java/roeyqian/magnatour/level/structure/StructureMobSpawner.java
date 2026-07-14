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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.phys.AABB;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.live.BellRinger;
import roeyqian.magnatour.entity.live.BellSoul;
import roeyqian.magnatour.utility.registry.entity.RegLiveEntities;
import roeyqian.magnatour.utility.registry.level.RegDimensions;
import roeyqian.magnatour.utility.registry.level.RegStructures;

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

  private static final long SPAWN_INTERVAL_TICKS = 5L;
  private static final long STATE_PRUNE_INTERVAL_TICKS = 200L;
  private static final long STATE_TTL_TICKS = 1200L;

  private static final double DIAMOND_CITY_COUNT_HORIZONTAL_PADDING = 6.0D;
  private static final double DIAMOND_CITY_COUNT_VERTICAL_PADDING = 6.0D;
  private static final double DIAMOND_CITY_SPAWN_SEPARATION = 8.0D;
  private static final double GOLD_BELL_TOWER_COUNT_HORIZONTAL_PADDING = 8.0D;
  private static final double GOLD_BELL_TOWER_COUNT_VERTICAL_PADDING = 12.0D;

  private static boolean tickEventRegistered = false;

  private static final StructureSpawnProfile DIAMOND_CITY_PROFILE =
      new StructureSpawnProfile(
          "diamond_city",
          RegDimensions.ORE_CONTINENT,
          RegStructures.DIAMOND_CITY,
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
          RegDimensions.HARVEST_CONTINENT,
          RegStructures.GOLD_BELL_TOWER,
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
    if (spawned > 0) {
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

  private static int countEntities(
      ServerLevel level,
      AABB box,
      Predicate<Entity> predicate
  ) {
    return level.getEntities((Entity) null, box, predicate).size();
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
        entity -> entity.isAlive() && entity.getType() == RegLiveEntities.OBSIDIAN_GOLEM
    ).isEmpty();
  }

  private static int countDiamondCityMobs(
      ServerLevel level,
      AABB box
  ) {
    return countEntities(
        level,
        box,
        entity -> entity.isAlive() && entity.getType() == RegLiveEntities.OBSIDIAN_GOLEM
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

  private static int spawnDiamondCityMobs(
      ServerLevel level,
      RandomSource random,
      BoundingBox structureBox,
      StructureSpawnProfile profile,
      int currentCount,
      SpawnState state
  ) {
    if (currentCount >= profile.maxPopulation()) return 0;

    int spawnBatch = DIAMOND_CITY_TARGET_OBSIDIAN_GOLEMS - currentCount;
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
        DIAMOND_CITY_TARGET_OBSIDIAN_GOLEMS * 2
    );

    int spawned = 0;
    Set<BlockPos> reservedSpawnSites = new HashSet<>();
    if (indoorTarget > 0) {
      spawned += StructureMobHelper.spawnPersistentGroundMobsFromCandidates(
          level,
          random,
          RegLiveEntities.OBSIDIAN_GOLEM,
          indoorTarget,
          spawnSites.underCoverInterior,
          true,
          pos -> isDiamondCitySpawnSiteAvailable(level, spawnSites, reservedSpawnSites, pos),
          reservedSpawnSites::add
      );
    }
    if (outdoorTarget > 0) {
      spawned += StructureMobHelper.spawnPersistentGroundMobsFromCandidates(
          level,
          random,
          RegLiveEntities.OBSIDIAN_GOLEM,
          outdoorTarget,
          spawnSites.openAir,
          true,
          pos -> isDiamondCitySpawnSiteAvailable(level, spawnSites, reservedSpawnSites, pos),
          reservedSpawnSites::add
      );
    }

    int remaining = spawnBatch - spawned;
    if (remaining > 0) {
      spawned += StructureMobHelper.spawnPersistentGroundMobsFromCandidates(
          level,
          random,
          RegLiveEntities.OBSIDIAN_GOLEM,
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
    if (cycleBudget <= 0) return 0;

    int spawned = 0;
    if (missingRingers > 0) {
      int ringersSpawned = StructureMobHelper.spawnPersistentGroundMobsDistributedByFloor(
          level,
          random,
          structureBox,
          RegLiveEntities.BELL_RINGER,
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
      spawned += StructureMobHelper.spawnPersistentAirMobsAboveFloors(
          level,
          random,
          structureBox,
          RegLiveEntities.BELL_SOUL,
          Math.min(missingSouls, cycleBudget),
          GOLD_BELL_TOWER_SPAWN_FLOOR,
          1,
          3
      );
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

      StructureMobHelper.GroundSpawnCandidates candidates =
          StructureMobHelper.findGroundSpawnCandidatesByCeiling(
              level,
              chunkBox,
              RegLiveEntities.OBSIDIAN_GOLEM,
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
