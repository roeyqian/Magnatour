/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.biome;

// Java Standard
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.PotentialCalculator;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.HarvestContinentChunkGenerator;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

/** Creature-only adaptation of Minecraft 26.3 NaturalSpawner and SpawnState. */
public final class BiomeMobSpawner {

  private static final int LOCAL_POPULATION_LIMIT = populationLimit(17 * 17);
  private static final int MAX_SPAWN_PER_CYCLE = 10;

  private static final long SPAWN_INTERVAL_TICKS = 400L;

  private static boolean tickEventRegistered;

  private BiomeMobSpawner() {}

  public static void registerTickEvent() {
    if (tickEventRegistered) return;
    tickEventRegistered = true;
    // The completed level tick has the game time used by vanilla's chunk spawner.
    ServerTickEvents.END_LEVEL_TICK.register(level -> {
      if (!level.dimension().equals(CustomDimensions.HARVEST_CONTINENT)
          || level.getGameTime() % SPAWN_INTERVAL_TICKS != 0L
          || !level.tickRateManager().runsNormally()
          || !level.getGameRules().get(GameRules.SPAWN_MOBS)
          || level.players().isEmpty()) return;
      if (level.getChunkSource().getGenerator() instanceof HarvestContinentChunkGenerator generator) {
        spawnAnimals(level, generator);
      }
    });
  }

  private static int populationLimit(
      int spawnableChunkCount
  ) {
    return Math.max(0, spawnableChunkCount) / 5;
  }

  private static void spawnAnimals(
      ServerLevel level,
      HarvestContinentChunkGenerator generator
  ) {
    int chunkCount = level.getChunkSource().chunkMap.getDistanceManager().getNaturalSpawnChunkCount();
    SpawnState state = new SpawnState(level, populationLimit(chunkCount));
    if (!state.canSpawn()) return;

    // Use vanilla's spawning chunk collection, including its player and chunk
    // readiness filters, rather than building a radius around each player.
    List<LevelChunk> chunks = new ArrayList<>();
    level.getChunkSource().chunkMap.collectSpawningChunks(chunks);
    // Vanilla shuffles spawning chunks; share the world's random source.
    RandomSource random = level.getRandom();
    for (int i = chunks.size() - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      LevelChunk swap = chunks.get(i);
      chunks.set(i, chunks.get(j));
      chunks.set(j, swap);
    }
    for (LevelChunk chunk : chunks) {
      if (!state.canSpawn()) break;
      if (!level.getWorldBorder().isWithinBounds(chunk.getPos())
          || !level.canSpawnEntitiesInChunk(chunk.getPos())
          || !state.canSpawnLocally(chunk.getPos())) continue;
      spawnForChunk(level, generator, chunk, state);
    }
  }

  private static void spawnForChunk(
      ServerLevel level,
      HarvestContinentChunkGenerator generator,
      LevelChunk chunk,
      SpawnState state
  ) {
    BlockPos origin = getRandomPosWithin(level, chunk);
    if (origin.getY() < level.getMinY() + 1
        || chunk.getBlockState(origin).isRedstoneConductor(chunk, origin)) return;

    // Vanilla tries up to three groups, walking horizontally at the randomly
    // selected height. There is no surface scan or player-centered spawn ring.
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    RandomSource random = level.getRandom();
    int spawnedInChunk = 0;
    for (int group = 0; group < 3 && state.canSpawn(); group++) {
      int x = origin.getX();
      int z = origin.getZ();
      MobSpawnSettings.SpawnerData spawnData = null;
      SpawnGroupData groupData = null;
      int attempts = Mth.ceil(random.nextFloat() * 4.0F);
      int spawnedInGroup = 0;
      for (int attempt = 0; attempt < attempts && state.canSpawn(); attempt++) {
        x += random.nextInt(6) - random.nextInt(6);
        z += random.nextInt(6) - random.nextInt(6);
        pos.set(x, origin.getY(), z);
        double spawnX = x + 0.5D;
        double spawnZ = z + 0.5D;
        Player nearest = level.getNearestPlayer(spawnX, pos.getY(), spawnZ, -1.0D, false);
        if (nearest == null) continue;
        double distanceSquared = nearest.distanceToSqr(spawnX, pos.getY(), spawnZ);
        if (!isRightDistanceToPlayerAndSpawnPoint(level, chunk, pos, distanceSquared)) continue;
        if (spawnData == null) {
          var selected = generator.getHarvestAnimalsAt(level, level.structureManager(), pos).getRandom(random);
          if (selected.isEmpty()) break;
          spawnData = selected.get();
          attempts = spawnData.count().sample(random);
        }
        if (!isValidSpawnPositionForType(level, generator, spawnData, pos, distanceSquared)
            || !state.canSpawnAt(spawnData.type(), pos)) continue;

        Mob mob = createMob(level, spawnData.type());
        if (mob == null) return;
        mob.snapTo(spawnX, pos.getY(), spawnZ, random.nextFloat() * 360.0F, 0.0F);
        int despawnDistance = mob.getType().getCategory().getDespawnDistance();
        if ((distanceSquared > (double) despawnDistance * despawnDistance
                && mob.removeWhenFarAway(distanceSquared))
            || !mob.checkSpawnRules(level, EntitySpawnReason.NATURAL)
            || !mob.checkSpawnObstruction(level)) continue;

        groupData = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
            EntitySpawnReason.NATURAL, groupData);
        level.addFreshEntityWithPassengers(mob);
        state.afterSpawn(mob);
        spawnedInChunk++;
        spawnedInGroup++;
        if (spawnedInChunk >= mob.getMaxSpawnClusterSize()) return;
        if (mob.isMaxGroupSizeReached(spawnedInGroup)) break;
      }
    }
  }

  private static BlockPos getRandomPosWithin(
      ServerLevel level,
      LevelChunk chunk
  ) {
    RandomSource random = level.getRandom();
    int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
    int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);
    int topY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
    return new BlockPos(x, Mth.randomBetweenInclusive(random, level.getMinY(), topY), z);
  }

  private static boolean isRightDistanceToPlayerAndSpawnPoint(
      ServerLevel level,
      LevelChunk chunk,
      BlockPos pos,
      double distanceSquared
  ) {
    if (distanceSquared <= 24.0D * 24.0D) return false;
    var respawn = level.getRespawnData();
    if (respawn.dimension() == level.dimension()
        && respawn.pos().closerToCenterThan(new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D),
            24.0D)) return false;
    ChunkPos candidateChunk = ChunkPos.containing(pos);
    return Objects.equals(candidateChunk, chunk.getPos()) || level.canSpawnEntitiesInChunk(candidateChunk);
  }

  private static boolean isValidSpawnPositionForType(
      ServerLevel level,
      HarvestContinentChunkGenerator generator,
      MobSpawnSettings.SpawnerData spawnData,
      BlockPos pos,
      double distanceSquared
  ) {
    EntityType<?> type = spawnData.type();
    int despawnDistance = type.getCategory().getDespawnDistance();
    return type.getCategory() == MobCategory.CREATURE
        && (type.canSpawnFarFromPlayer() || distanceSquared <= (double) despawnDistance * despawnDistance)
        && type.canSummon()
        && generator.getHarvestAnimalsAt(level, level.structureManager(), pos).contains(spawnData)
        && SpawnPlacements.isSpawnPositionOk(type, level, pos)
        && SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom())
        && level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));
  }

  private static Mob createMob(
      ServerLevel level,
      EntityType<?> type
  ) {
    try {
      Entity entity = type.create(level, EntitySpawnReason.NATURAL);
      if (entity instanceof Mob mob) return mob;
      Magnatour.LOGGER.warn("Cannot naturally spawn entity of type {}", type);
    } catch (Exception exception) {
      Magnatour.LOGGER.warn("Failed to create naturally spawning mob", exception);
    }
    return null;
  }

  private static final class SpawnState {

    private final int populationLimit;

    private final ServerLevel level;

    private final Map<ServerPlayer, Integer> localCounts = new HashMap<>();

    private final Map<ChunkPos, List<ServerPlayer>> playersNearChunk = new HashMap<>();

    private final PotentialCalculator potential = new PotentialCalculator();

    private int population;
    private int spawned;

    private SpawnState(
        ServerLevel level,
        int populationLimit
    ) {
      this.level = level;
      this.populationLimit = populationLimit;
      // Match NaturalSpawner.createState: count managed entities in full chunks,
      // excluding persistence-required mobs, without filtering spawn origin.
      for (Entity entity : level.getAllEntities()) {
        if (entity instanceof Mob mob
            && (mob.isPersistenceRequired() || mob.requiresCustomPersistence())) continue;
        if (entity.getType().getCategory() == MobCategory.MISC) continue;
        ChunkPos chunkPos = entity.chunkPosition();
        if (level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z()) == null) continue;
        MobSpawnSettings.MobSpawnCost cost = spawnCost(entity.getType(), entity.blockPosition());
        if (cost != null) this.potential.addCharge(entity.blockPosition(), cost.charge());
        if (entity.getType().getCategory() == MobCategory.CREATURE) {
          this.population++;
          if (entity instanceof Mob) countLocalCreature(chunkPos);
        }
      }
    }

    private MobSpawnSettings.MobSpawnCost spawnCost(
        EntityType<?> type,
        BlockPos pos
    ) {
      return this.level.environmentAttributes()
          .getValue(net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos)
          .getMobSpawnCost(type);
    }

    private void countLocalCreature(
        ChunkPos pos
    ) {
      for (ServerPlayer player : playersNear(pos)) this.localCounts.merge(player, 1, Integer::sum);
    }

    private boolean canSpawn() {
      return this.population < this.populationLimit && this.spawned < MAX_SPAWN_PER_CYCLE;
    }

    private List<ServerPlayer> playersNear(
        ChunkPos pos
    ) {
      return this.playersNearChunk.computeIfAbsent(pos,
          chunkPos -> this.level.getChunkSource().chunkMap.getPlayersCloseForSpawning(chunkPos));
    }

    private void afterSpawn(
        Mob mob
    ) {
      this.population++;
      this.spawned++;
      countLocalCreature(mob.chunkPosition());
      MobSpawnSettings.MobSpawnCost cost = spawnCost(mob.getType(), mob.blockPosition());
      if (cost != null) this.potential.addCharge(mob.blockPosition(), cost.charge());
    }

    private boolean canSpawnAt(
        EntityType<?> type,
        BlockPos pos
    ) {
      if (!canSpawn()) return false;
      MobSpawnSettings.MobSpawnCost cost = spawnCost(type, pos);
      return cost == null || this.potential.getPotentialEnergyChange(pos, cost.charge()) <= cost.energyBudget();
    }

    private boolean canSpawnLocally(
        ChunkPos pos
    ) {
      for (ServerPlayer player : playersNear(pos)) {
        if (this.localCounts.getOrDefault(player, 0) < LOCAL_POPULATION_LIMIT) return true;
      }
      return false;
    }

  }

}
