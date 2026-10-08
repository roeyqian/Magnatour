/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

// Fabric
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// FastUtil
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

// Magnatour
import roeyqian.magnatour.block.universe.UniverseBlock;
import roeyqian.magnatour.blockentity.universe.UniverseBlockEntity;

public final class VirtualBlockLightManager {

  public static final int LIGHT_LEVEL = 15;
  public static final int MAX_BLOCKS_PER_TICK = 4096;

  private static final long MAX_BATCH_NANOS = 2_000_000L;

  private static final int MAX_UNIVERSE_REFRESHES_PER_FRAME = 8;

  private static final TicketType UNIVERSE_TICKET = new TicketType(
      TicketType.NO_TIMEOUT,
      TicketType.FLAG_PERSIST | TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE
  );

  private static final int UNIVERSE_CHUNK_SIDE = 15;

  private static final ChunkPos[] UNIVERSE_CHUNK_OFFSETS = createUniverseChunkOffsets();

  private static final int HALF_X = 32;
  private static final int HALF_Y = 16;
  private static final int HALF_Z = 32;

  private static final long[] SPARSE_OFFSETS = createSourceOffsets(2);

  private static final int[] SPARSE_RANKS = createSourceRanks(SPARSE_OFFSETS);

  private static final Map<BlockGetter, WorldSources> LIGHT_SOURCES = new WeakHashMap<>();

  private VirtualBlockLightManager() {}

  public static void beginUniverseBuild(
      Level world,
      long key
  ) {
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    synchronized (sources) { sources.universeBuilds.put(key, Util.getNanos()); }
  }

  public static void finishUniverseBuilds(
      Level world,
      BiPredicate<Long, Long> completed
  ) {
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    synchronized (sources) {
      sources.universeBuilds.entrySet().removeIf(entry -> completed.test(entry.getKey(), entry.getValue()));
    }
  }

  public static int getLightEmission(
      BlockGetter world,
      long pos,
      BlockState state
  ) {
    if (!state.isAir()) return 0;

    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return 0;
    SectionSources section = sources.sections.get(sectionKey(pos));
    return section == null ? 0 : section.getPackedLight(localIndex(pos));
  }

  public static void init() {
    Registry.register(BuiltInRegistries.TICKET_TYPE, "magnatour:universe_light", UNIVERSE_TICKET);
    PayloadTypeRegistry.clientboundPlay().register(UniverseLightPayload.ID, UniverseLightPayload.CODEC);
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendUniverseSnapshot(handler.player));
    ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sendUniverseSnapshot(newPlayer));
    ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> sendUniverseSnapshot(player));
  }

  public static boolean isUniverseLit(
      BlockGetter world,
      BlockPos pos
  ) {
    return isUniverseLit(world, pos.getX() >> 4, pos.getZ() >> 4, pos.getY());
  }

  // Universe sources are registered once per chunk column, with no per-cell bookkeeping.
  public static boolean isUniverseLit(
      BlockGetter world,
      int chunkX,
      int chunkZ,
      int y
  ) {
    if (!(world instanceof Level level) || level.isOutsideBuildHeight(y)) return false;
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return false;
    return sources.universeCoverage.containsKey(new ChunkPos(chunkX, chunkZ));
  }

  public static void onChunkLoad(
      Level world,
      ChunkPos chunk
  ) {
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    synchronized (sources) {
      for (int y = world.getMinY() >> 4; y <= world.getMaxY() >> 4; y++) {
        long key = SectionPos.asLong(chunk.x(), y, chunk.z());
        boolean affected = sources.knownSections.contains(key);
        if (!affected) {
          for (Source source : sources.origins.values()) {
            if (source.intersects(chunk.x(), y, chunk.z())) {
              affected = true;
              break;
            }
          }
        }
        if (affected && sources.restoreKeys.add(key)) sources.restoring.addLast(new RestoreSection(key));
      }
    }
  }

  // Persisted tickets reload their source chunks after a restart. Reconcile them with block state.
  public static void onChunkLoad(
      Level world,
      LevelChunk chunk
  ) {
    boolean lit = false;
    for (BlockPos pos : chunk.getBlockEntitiesPos()) {
      BlockState state = chunk.getBlockState(pos);
      if (state.getBlock() instanceof UniverseBlock && state.getValue(UniverseBlock.LIT)) {
        lit = true;
        if (chunk.getBlockEntity(pos) instanceof UniverseBlockEntity entity) entity.setLightRegistered(true);
        else setActive(world, pos, true);
      }
    }
    if (!lit && world instanceof ServerLevel server) {
      server.getChunkSource().removeTicketWithRadius(UNIVERSE_TICKET, chunk.getPos(), 2);
    }
    onChunkLoad(world, chunk.getPos());
  }

  public static void onChunkUnload(
      Level world,
      ChunkPos chunk
  ) {
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    synchronized (sources) {
      if (!world.isClientSide()) {
        sources.universeOperations.removeIf(operation ->
            operation.pos().getX() >> 4 == chunk.x() && operation.pos().getZ() >> 4 == chunk.z());
        sources.universeRequested.keySet().removeIf(origin ->
            BlockPos.getX(origin) >> 4 == chunk.x() && BlockPos.getZ(origin) >> 4 == chunk.z());
        Set<Long> origins = sources.universeChunks.remove(chunk);
        if (origins != null) {
          updateUniverseTicket(world, chunk, false);
          updateUniverseCoverage(world, new BlockPos(chunk.x() << 4, world.getMinY(), chunk.z() << 4), sources, false);
          if (world instanceof ServerLevel server && server.getServer().isRunning()) {
            for (long origin : origins) {
              UniverseLightPayload payload = new UniverseLightPayload(world.dimension(), BlockPos.of(origin), false);
              for (ServerPlayer player : server.players()) ServerPlayNetworking.send(player, payload);
            }
          }
        }
      }
      for (int y = world.getMinY() >> 4; y <= world.getMaxY() >> 4; y++) {
        long key = SectionPos.asLong(chunk.x(), y, chunk.z());
        sources.sections.remove(key);
        sources.restoreKeys.remove(key);
      }
      sources.universeBuilds.keySet().removeIf(key -> SectionPos.x(key) == chunk.x() && SectionPos.z(key) == chunk.z());
      sources.restoring.removeIf(restore -> SectionPos.x(restore.key) == chunk.x()
          && SectionPos.z(restore.key) == chunk.z());
    }
  }

  public static void receiveUniverseState(
      Level world,
      UniverseLightPayload payload
  ) {
    if (!world.dimension().equals(payload.dimension())) return;
    enqueueUniverseState(world, payload.pos(), payload.active());
  }

  public static void setActive(
      Level world,
      BlockPos origin,
      boolean active
  ) {
    // Client block-state polling can miss intermediate toggles; only ordered server packets drive lighting.
    if (world.isClientSide()) return;
    enqueueUniverseState(world, origin, active);
  }

  public static void setActive(
      Level world,
      BlockPos origin,
      int lightLevel,
      boolean active
  ) {
    if (lightLevel <= 0 || lightLevel > LIGHT_LEVEL) return;

    WorldSources sources = getWorldSources(world, active);
    if (sources == null) return;
    synchronized (sources) {
      SourceKey key = new SourceKey(origin.asLong(), lightLevel);
      Source source = sources.origins.get(key);
      if (source == null) {
        if (!active) return;
        source = new Source(key);
        sources.origins.put(key, source);
      }
      if (source.active == active) return;
      source.active = active;
      source.cursor = 0;
      if (!source.queued) {
        source.queued = true;
        sources.pending.addLast(source);
      }
    }
  }

  // Drain one FIFO operation fully, including mesh uploads, before changing coverage again.
  // Bound snapshot creation; mesh compilation runs in vanilla workers.
  public static void submitUniverseRefreshes(
      Level world,
      Consumer<SectionPos> refreshSection
  ) {
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    ArrayList<SectionPos> changed = new ArrayList<>(MAX_UNIVERSE_REFRESHES_PER_FRAME);
    synchronized (sources) {
      advanceUniverseQueue(world, sources);
      for (int count = 0; count < MAX_UNIVERSE_REFRESHES_PER_FRAME && !sources.universeRefresh.isEmpty(); count++) {
        long key = sources.universeRefresh.removeFirst();
        SectionPos section = SectionPos.of(key);
        if (world.getChunkSource().hasChunk(section.x(), section.z())) {
          changed.add(section);
        }
      }
    }
    for (SectionPos section : changed) refreshSection.accept(section);
  }

  // Supreme source changes and chunk restoration still share their per-block budget.
  public static void tick(
      Level world
  ) {
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    synchronized (sources) {
      if (!world.isClientSide()) advanceUniverseQueue(world, sources);
      if (sources.pending.isEmpty() && sources.restoring.isEmpty()) return;
      long deadline = System.nanoTime() + MAX_BATCH_NANOS;
      Long2IntOpenHashMap changed = sources.changed;
      changed.clear();
      int remaining = MAX_BLOCKS_PER_TICK;
      boolean restoreFirst = sources.restoreFirst;
      sources.restoreFirst = !restoreFirst;
      int tasks = sources.pending.size();
      while (remaining > 0 && System.nanoTime() < deadline) {
        if (!sources.restoring.isEmpty() && (restoreFirst || tasks == 0)) {
          RestoreSection restore = sources.restoring.peekFirst();
          long pos = restore.position();
          if (world.getChunkSource().hasChunk(BlockPos.getX(pos) >> 4, BlockPos.getZ(pos) >> 4)) {
            if (!world.isOutsideBuildHeight(BlockPos.getY(pos))) rebuildNode(sources, pos, changed);
            restore.index++;
            remaining--;
          } else {
            restore.index = 4096;
          }
          if (restore.index == 4096) {
            sources.restoring.removeFirst();
            sources.restoreKeys.remove(restore.key);
          }
          continue;
        }
        if (tasks-- <= 0) break;
        Source source = sources.pending.removeFirst();
        while (remaining > 0 && source.cursor < source.size && System.nanoTime() < deadline) {
          // Both activation and removal scan outwards; the bitset makes rapid reversals safe.
          int index = source.cursor++;
          long pos = source.position(index);
          if (source.applied.get(index) != source.active) {
            source.applied.set(index, source.active);
            if (!world.isOutsideBuildHeight(BlockPos.getY(pos))
                && world.getChunkSource().hasChunk(BlockPos.getX(pos) >> 4, BlockPos.getZ(pos) >> 4)) {
              updateNode(sources, pos, source.key.lightLevel(), source.active, changed);
            }
          }
          remaining--;
        }
        if (source.cursor < source.size) sources.pending.addLast(source);
        else {
          source.queued = false;
          if (!source.active) sources.origins.remove(source.key);
        }
        restoreFirst = true;
      }
      // Coalesce all contributions before asking the engine to propagate a changed light value.
      for (var entry : changed.long2IntEntrySet()) {
        long pos = entry.getLongKey();
        SectionSources section = sources.sections.get(sectionKey(pos));
        int light = section == null ? 0 : section.getPackedLight(localIndex(pos));
        if (light != entry.getIntValue()) {
          BlockPos blockPos = BlockPos.of(pos);
          if (world.getBlockState(blockPos).isAir()) {
            world.getLightEngine().getLayerListener(LightLayer.BLOCK).checkBlock(blockPos);
          }
        }
      }
      changed.clear();
    }
  }

  // Counting-sort shared offsets by squared distance, so each batch expands from the center.
  private static long[] createSourceOffsets(
      int spacing
  ) {
    int maxDistance = HALF_X * HALF_X + HALF_Y * HALF_Y + HALF_Z * HALF_Z;
    int[] positions = new int[maxDistance + 1];
    int size = 0;
    for (int x = -HALF_X; x <= HALF_X; x += spacing) {
      for (int y = -HALF_Y; y <= HALF_Y; y += spacing) {
        for (int z = -HALF_Z; z <= HALF_Z; z += spacing) {
          positions[x * x + y * y + z * z]++;
          size++;
        }
      }
    }
    int next = 0;
    for (int distance = 0; distance < positions.length; distance++) {
      int count = positions[distance];
      positions[distance] = next;
      next += count;
    }
    long[] offsets = new long[size];
    for (int x = -HALF_X; x <= HALF_X; x += spacing) {
      for (int y = -HALF_Y; y <= HALF_Y; y += spacing) {
        for (int z = -HALF_Z; z <= HALF_Z; z += spacing) {
          offsets[positions[x * x + y * y + z * z]++] = BlockPos.asLong(x, y, z);
        }
      }
    }
    return offsets;
  }

  private static int[] createSourceRanks(
      long[] offsets
  ) {
    int[] ranks = new int[(HALF_X * 2 + 1) * (HALF_Y * 2 + 1) * (HALF_Z * 2 + 1)];
    java.util.Arrays.fill(ranks, -1);
    for (int i = 0; i < offsets.length; i++) {
      long offset = offsets[i];
      ranks[offsetIndex(BlockPos.getX(offset), BlockPos.getY(offset), BlockPos.getZ(offset))] = i;
    }
    return ranks;
  }

  private static ChunkPos[] createUniverseChunkOffsets() {
    var offsets = new ArrayList<ChunkPos>(UNIVERSE_CHUNK_SIDE * UNIVERSE_CHUNK_SIDE);
    int half = UNIVERSE_CHUNK_SIDE / 2;
    for (int x = -half; x <= half; x++) {
      for (int z = -half; z <= half; z++) {
        offsets.add(new ChunkPos(x, z));
      }
    }
    offsets.sort(Comparator.comparingInt(pos -> pos.x() * pos.x() + pos.z() * pos.z()));
    return offsets.toArray(ChunkPos[]::new);
  }

  private static void applyUniverseState(
      Level world,
      BlockPos origin,
      boolean active
  ) {
    WorldSources sources = getWorldSources(world, active);
    if (sources == null) return;
    ChunkPos chunk = new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
    synchronized (sources) {
      Set<Long> origins = sources.universeChunks.get(chunk);
      boolean previouslyActive = origins != null && origins.contains(origin.asLong());
      if (previouslyActive == active) return;
      // Sound each source transition, including removal, even when another source keeps this chunk lit.
      if (!world.isClientSide()) {
        world.playSound(null, origin, active ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
            SoundSource.BLOCKS, 1.0F, 1.0F);
      }
      if (active) {
        boolean wasLit = origins != null;
        if (origins == null) {
          origins = new LinkedHashSet<>();
          sources.universeChunks.put(chunk, origins);
        }
        if (!origins.add(origin.asLong()) || wasLit) return;
      } else {
        if (origins == null || !origins.remove(origin.asLong()) || !origins.isEmpty()) return;
        sources.universeChunks.remove(chunk);
      }
      boolean keepTicket = active || sources.universeOperations.stream().anyMatch(operation ->
          operation.active() && new ChunkPos(operation.pos().getX() >> 4, operation.pos().getZ() >> 4).equals(chunk));
      updateUniverseTicket(world, chunk, keepTicket);
      updateUniverseCoverage(world, origin, sources, active);
    }
  }

  private static WorldSources getWorldSources(
      BlockGetter world,
      boolean create
  ) {
    synchronized (LIGHT_SOURCES) {
      return create ? LIGHT_SOURCES.computeIfAbsent(world, _ -> new WorldSources()) : LIGHT_SOURCES.get(world);
    }
  }

  private static void updateUniverseTicket(
      Level world,
      ChunkPos chunk,
      boolean active
  ) {
    if (!(world instanceof ServerLevel server)) return;
    if (active) server.getChunkSource().addTicketWithRadius(UNIVERSE_TICKET, chunk, 2);
    else if (server.getServer().isRunning()) {
      server.getChunkSource().removeTicketWithRadius(UNIVERSE_TICKET, chunk, 2);
    }
  }

  // Each active source chunk contributes once, regardless of how many lit blocks it contains.
  private static void updateUniverseCoverage(
      Level world,
      BlockPos origin,
      WorldSources sources,
      boolean active
  ) {
    int x = origin.getX() >> 4;
    int z = origin.getZ() >> 4;
    Set<ChunkPos> refreshChunks = new LinkedHashSet<>();
    for (ChunkPos offset : UNIVERSE_CHUNK_OFFSETS) {
      ChunkPos target = new ChunkPos(x + offset.x(), z + offset.z());
      int previous = sources.universeCoverage.getOrDefault(target, 0);
      int next = previous + (active ? 1 : -1);
      if (next == 0) sources.universeCoverage.remove(target);
      else sources.universeCoverage.put(target, next);
      if (world.isClientSide() && (previous == 0 || next == 0)) {
        refreshChunks.add(target);
        for (int dx = -1; dx <= 1; dx++) {
          for (int dz = -1; dz <= 1; dz++) refreshChunks.add(new ChunkPos(target.x() + dx, target.z() + dz));
        }
      }
    }
    for (ChunkPos target : refreshChunks) queueUniverseRefresh(world, origin, target, sources);
  }

  private static int offsetIndex(
      int x,
      int y,
      int z
  ) {
    return ((x + HALF_X) * (HALF_Y * 2 + 1) + y + HALF_Y) * (HALF_Z * 2 + 1) + z + HALF_Z;
  }

  private static long sectionKey(
      long pos
  ) {
    return SectionPos.asLong(BlockPos.getX(pos) >> 4, BlockPos.getY(pos) >> 4, BlockPos.getZ(pos) >> 4);
  }

  private static int localIndex(
      long pos
  ) {
    return ((BlockPos.getY(pos) & 15) << 8) | ((BlockPos.getZ(pos) & 15) << 4) | (BlockPos.getX(pos) & 15);
  }

  private static void sendUniverseSnapshot(
      ServerPlayer player
  ) {
    Level world = player.level();
    WorldSources sources = getWorldSources(world, false);
    if (sources == null) return;
    synchronized (sources) {
      for (Set<Long> origins : sources.universeChunks.values()) {
        for (long origin : origins) {
          ServerPlayNetworking.send(player, new UniverseLightPayload(world.dimension(), BlockPos.of(origin), true));
        }
      }
    }
  }

  private static void enqueueUniverseState(
      Level world,
      BlockPos origin,
      boolean active
  ) {
    WorldSources sources = getWorldSources(world, active);
    if (sources == null) return;
    synchronized (sources) {
      boolean previous = sources.universeRequested.getOrDefault(origin.asLong(), false);
      if (previous == active) return;
      sources.universeRequested.put(origin.asLong(), active);
      sources.universeOperations.addLast(new UniverseOperation(origin.immutable(), active));
      if (active) updateUniverseTicket(world, new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4), true);
    }
  }

  private static void advanceUniverseQueue(
      Level world,
      WorldSources sources
  ) {
    if (!sources.universeRefresh.isEmpty() || !sources.universeBuilds.isEmpty()) return;
    UniverseOperation operation = sources.universeOperations.pollFirst();
    if (operation == null) return;
    applyUniverseState(world, operation.pos(), operation.active());
    if (!operation.active() && sources.universeOperations.stream().noneMatch(next -> next.pos().equals(operation.pos()))) {
      sources.universeRequested.remove(operation.pos().asLong());
    }
    if (world instanceof ServerLevel server) {
      UniverseLightPayload payload = new UniverseLightPayload(world.dimension(), operation.pos(), operation.active());
      for (ServerPlayer player : server.players()) ServerPlayNetworking.send(player, payload);
    }
  }

  private static void rebuildNode(
      WorldSources sources,
      long pos,
      Long2IntOpenHashMap changed
  ) {
    int[] counts = sources.rebuildCounts;
    java.util.Arrays.fill(counts, 0);
    for (Source source : sources.origins.values()) {
      if (source.containsApplied(pos)) counts[source.key.lightLevel()]++;
    }
    long key = sectionKey(pos);
    SectionSources section = sources.sections.get(key);
    boolean lit = false;
    for (int level = 1; level <= LIGHT_LEVEL; level++) lit |= counts[level] > 0;
    if (section == null && lit) {
      section = new SectionSources();
      sources.sections.put(key, section);
      sources.knownSections.add(key);
    }
    if (section != null) {
      section.replace(localIndex(pos), counts);
      if (section.isEmpty()) sources.sections.remove(key);
    }
    // Recheck even zero emission: saved chunk light may come from a removed virtual source.
    changed.put(pos, -1);
  }

  private static void updateNode(
      WorldSources sources,
      long pos,
      int level,
      boolean active,
      Long2IntOpenHashMap changed
  ) {
    long key = sectionKey(pos);
    SectionSources section = sources.sections.get(key);
    if (section == null) {
      if (!active) return;
      section = new SectionSources();
      sources.sections.put(key, section);
      sources.knownSections.add(key);
    }
    int index = localIndex(pos);
    // A partially restored cell may not contain every source contribution yet.
    if (sources.restoreKeys.contains(key)) {
      rebuildNode(sources, pos, changed);
      return;
    }
    int previous = section.getPackedLight(index);
    section.change(index, level, active);
    if (!changed.containsKey(pos)) changed.put(pos, previous);
    if (section.isEmpty()) sources.sections.remove(key);
  }

  private static void queueUniverseRefresh(
      Level world,
      BlockPos origin,
      ChunkPos target,
      WorldSources sources
  ) {
    if (!world.isClientSide()) return;
    if (!(world.getChunkSource().getChunkForLighting(target.x(), target.z()) instanceof ChunkAccess chunk)) return;
    int center = origin.getY() >> 4;
    int min = world.getMinSectionY();
    int max = world.getMaxSectionY();
    // Resolve the chunk once and skip empty sections without allocating distance arrays.
    for (int distance = 0; distance <= Math.max(center - min, max - center); distance++) {
      int below = center - distance;
      if (below >= min && below <= max && !chunk.getSection(chunk.getSectionIndexFromSectionY(below)).hasOnlyAir()) {
        sources.universeRefresh.add(SectionPos.asLong(target.x(), below, target.z()));
      }
      int above = center + distance;
      if (distance > 0 && above >= min && above <= max
          && !chunk.getSection(chunk.getSectionIndexFromSectionY(above)).hasOnlyAir()) {
        sources.universeRefresh.add(SectionPos.asLong(target.x(), above, target.z()));
      }
    }
  }

  private static final class RestoreSection {

    private int index;

    private final long key;

    private RestoreSection(
        long key
    ) {
      this.key = key;
    }

    private long position() {
      return BlockPos.asLong((SectionPos.x(this.key) << 4) + (this.index & 15),
          (SectionPos.y(this.key) << 4) + (this.index >> 8),
          (SectionPos.z(this.key) << 4) + ((this.index >> 4) & 15));
    }

  }

  private static final class SectionSources {

    private final int[][] counts = new int[LIGHT_LEVEL + 1][];

    private final byte[] emission = new byte[4096];

    private int occupied;

    private void refresh(
        int index
    ) {
      int light = 0;
      for (int level = LIGHT_LEVEL; level > 0; level--) {
        if (this.counts[level] != null && this.counts[level][index] > 0) {
          light = level;
          break;
        }
      }
      boolean wasLit = this.emission[index] > 0;
      boolean isLit = light > 0;
      if (!wasLit && isLit) this.occupied++;
      else if (wasLit && !isLit) this.occupied--;
      this.emission[index] = (byte) light;
    }

    private synchronized void change(
        int index,
        int level,
        boolean active
    ) {
      if (this.counts[level] == null) {
        if (!active) return;
        this.counts[level] = new int[4096];
      }
      if (active) this.counts[level][index]++;
      else if (this.counts[level][index] > 0) this.counts[level][index]--;
      this.refresh(index);
    }

    private synchronized int getPackedLight(
        int index
    ) {
      return this.emission[index];
    }

    private synchronized boolean isEmpty() {
      return this.occupied == 0;
    }

    private synchronized void replace(
        int index,
        int[] replacement
    ) {
      for (int level = 1; level <= LIGHT_LEVEL; level++) {
        if (this.counts[level] == null && replacement[level] > 0) this.counts[level] = new int[4096];
        if (this.counts[level] != null) this.counts[level][index] = replacement[level];
      }
      this.refresh(index);
    }

  }

  private static final class Source {

    private int cursor;

    private final BitSet applied = new BitSet();

    private final int size;

    private final long[] offsets;

    private boolean active;
    private boolean queued;

    private final SourceKey key;

    private Source(
        SourceKey key
    ) {
      this.key = key;
      // Preserve the existing spacing of the lower-level Supreme lights.
      this.offsets = SPARSE_OFFSETS;
      this.size = this.offsets.length;
    }

    private boolean containsApplied(
        long pos
    ) {
      int x = BlockPos.getX(pos) - BlockPos.getX(this.key.origin());
      int y = BlockPos.getY(pos) - BlockPos.getY(this.key.origin());
      int z = BlockPos.getZ(pos) - BlockPos.getZ(this.key.origin());
      if (Math.abs(x) > HALF_X || Math.abs(y) > HALF_Y || Math.abs(z) > HALF_Z) return false;
      int[] ranks = SPARSE_RANKS;
      int rank = ranks[offsetIndex(x, y, z)];
      return rank >= 0 && this.applied.get(rank);
    }

    private boolean intersects(
        int x,
        int y,
        int z
    ) {
      int originX = BlockPos.getX(this.key.origin());
      int originY = BlockPos.getY(this.key.origin());
      int originZ = BlockPos.getZ(this.key.origin());
      return (x << 4) <= originX + HALF_X && (x << 4) + 15 >= originX - HALF_X
          && (y << 4) <= originY + HALF_Y && (y << 4) + 15 >= originY - HALF_Y
          && (z << 4) <= originZ + HALF_Z && (z << 4) + 15 >= originZ - HALF_Z;
    }

    private long position(
        int index
    ) {
      long offset = this.offsets[index];
      return BlockPos.asLong(
          BlockPos.getX(this.key.origin()) + BlockPos.getX(offset),
          BlockPos.getY(this.key.origin()) + BlockPos.getY(offset),
          BlockPos.getZ(this.key.origin()) + BlockPos.getZ(offset)
      );
    }

  }

  private record SourceKey(
      long origin,
      int lightLevel
  ) {}

  public record UniverseLightListener(
      LayerLightEventListener natural,
      LightChunkGetter chunkSource
  ) implements LayerLightEventListener {

    @Override
    public void checkBlock(
        @NonNull BlockPos pos
    ) { this.natural.checkBlock(pos); }

    @Override @Nullable
    public DataLayer getDataLayerData(
        @NonNull SectionPos pos
    ) {
      // Only overlay client snapshots: server packets and saves retain vanilla light data.
      if (this.chunkSource.getLevel() instanceof Level level && level.isClientSide()
          && isUniverseLit(level, pos.x(), pos.z(), pos.y() << 4)) {
        return new DataLayer(15);
      }
      return this.natural.getDataLayerData(pos);
    }

    @Override
    public int getLightValue(
        @NonNull BlockPos pos
    ) {
      return isUniverseLit(this.chunkSource.getLevel(), pos)
          ? 15 : this.natural.getLightValue(pos);
    }

    @Override
    public boolean hasLightWork() { return this.natural.hasLightWork(); }

    @Override
    public void propagateLightSources(
        @NonNull ChunkPos pos
    ) {
      this.natural.propagateLightSources(pos);
    }

    @Override
    public int runLightUpdates() { return this.natural.runLightUpdates(); }

    @Override
    public void setLightEnabled(
        @NonNull ChunkPos pos,
        boolean enabled
    ) {
      this.natural.setLightEnabled(pos, enabled);
    }

    @Override
    public void updateSectionStatus(
        @NonNull SectionPos pos,
        boolean empty
    ) {
      this.natural.updateSectionStatus(pos, empty);
    }

  }

  public record UniverseLightPayload(
      ResourceKey<Level> dimension,
      BlockPos pos,
      boolean active
  ) implements CustomPacketPayload {

    public static final Type<UniverseLightPayload> ID = new Type<>(
        Identifier.fromNamespaceAndPath("magnatour", "universe_light_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UniverseLightPayload> CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION), UniverseLightPayload::dimension,
        BlockPos.STREAM_CODEC, UniverseLightPayload::pos,
        ByteBufCodecs.BOOL, UniverseLightPayload::active, UniverseLightPayload::new);

    @Override @NonNull
    public Type<? extends CustomPacketPayload> type() { return ID; }

  }

  private record UniverseOperation(
      BlockPos pos,
      boolean active
  ) {}

  private static final class WorldSources {

    private final Map<ChunkPos, Set<Long>> universeChunks = new HashMap<>();

    private final Map<ChunkPos, Integer> universeCoverage = new ConcurrentHashMap<>();

    private final LinkedHashSet<Long> universeRefresh = new LinkedHashSet<>();

    private final Map<Long, Long> universeBuilds = new HashMap<>();

    private final ArrayDeque<UniverseOperation> universeOperations = new ArrayDeque<>();

    private final Map<Long, Boolean> universeRequested = new HashMap<>();

    private final Long2IntOpenHashMap changed = new Long2IntOpenHashMap();

    private final Map<Long, SectionSources> sections = new ConcurrentHashMap<>();

    private final LongOpenHashSet knownSections = new LongOpenHashSet();
    private final LongOpenHashSet restoreKeys = new LongOpenHashSet();

    private final ArrayDeque<RestoreSection> restoring = new ArrayDeque<>();

    private final int[] rebuildCounts = new int[LIGHT_LEVEL + 1];

    private boolean restoreFirst;

    private final Map<SourceKey, Source> origins = new HashMap<>();

    private final ArrayDeque<Source> pending = new ArrayDeque<>();

  }

}
