/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.supreme;

// Java Standard
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomItemSetting;

public class StarAtlas extends Item {

  private static Runnable clientOpenHandler = () -> {};

  public StarAtlas(
      Properties settings
  ) {
    super(CustomItemSetting.applySupremeDefaults(settings)
        .component(DataComponents.LORE, CustomItemSetting.itemLore("star_atlas", 2)));
  }

  /** Use natural colors for specified biomes and stable pseudorandom colors for unspecified ones. */
  public static int biomeColor(
      Identifier id
  ) {
    int color = 0;
    if (id.getNamespace().equals("minecraft")) {
      color = switch (id.getPath()) {
        case "plains" -> 0x91BD59;
        case "sunflower_plains" -> 0xA8C55F;
        case "forest" -> 0x486E38;
        case "flower_forest" -> 0x67974C;
        case "birch_forest" -> 0x80A755;
        case "dappled_forest" -> 0x74944A;
        case "old_growth_birch_forest" -> 0x6B9047;
        case "dark_forest" -> 0x364C2E;
        case "taiga" -> 0x587867;
        case "old_growth_pine_taiga" -> 0x546E49;
        case "old_growth_spruce_taiga" -> 0x416451;
        case "savanna" -> 0xB3AA55;
        case "savanna_plateau" -> 0x9F994B;
        case "windswept_savanna" -> 0xA39760;
        case "jungle" -> 0x39923E;
        case "sparse_jungle" -> 0x5D9C49;
        case "bamboo_jungle" -> 0x75A64A;
        case "meadow" -> 0x90B965;
        case "swamp" -> 0x6A754A;
        case "mangrove_swamp" -> 0x49664A;
        case "desert" -> 0xDED3A0;
        case "beach" -> 0xE7DDB0;
        case "badlands" -> 0xB56B46;
        case "eroded_badlands" -> 0xC98556;
        case "wooded_badlands" -> 0x986C45;
        case "snowy_plains" -> 0xE8EEEE;
        case "ice_spikes" -> 0xD1E6EF;
        case "snowy_slopes" -> 0xE0E8EC;
        case "frozen_peaks" -> 0xD3DBE2;
        case "jagged_peaks" -> 0xCCD1D0;
        case "snowy_beach" -> 0xDEDCD0;
        case "snowy_taiga" -> 0xB6C7BA;
        case "grove" -> 0xA5B8A7;
        case "windswept_hills" -> 0x819078;
        case "windswept_forest" -> 0x687C5C;
        case "windswept_gravelly_hills" -> 0x96938D;
        case "stony_peaks" -> 0x92928E;
        case "stony_shore" -> 0xAAA79E;
        case "cherry_grove" -> 0xDCAEBE;
        case "pale_garden" -> 0xA0A799;
        case "ocean" -> 0x477BBB;
        case "river" -> 0x568DC3;
        case "deep_ocean" -> 0x355E95;
        case "lukewarm_ocean" -> 0x459BAC;
        case "deep_lukewarm_ocean" -> 0x397D91;
        case "warm_ocean" -> 0x48B6B1;
        case "cold_ocean" -> 0x4D709E;
        case "deep_cold_ocean" -> 0x3A577E;
        case "frozen_ocean" -> 0xA7C8DC;
        case "frozen_river" -> 0xBFDDE5;
        case "deep_frozen_ocean" -> 0x85AAC6;
        case "mushroom_fields" -> 0x91858D;
        case "dripstone_caves" -> 0x9A806B;
        case "lush_caves" -> 0x688D43;
        case "deep_dark" -> 0x234244;
        case "sulfur_caves" -> 0xB8AC61;
        case "nether_wastes" -> 0x8F403A;
        case "crimson_forest" -> 0x8D293B;
        case "warped_forest" -> 0x318C81;
        case "soul_sand_valley" -> 0x746052;
        case "basalt_deltas" -> 0x555456;
        case "the_end" -> 0xDDD8A4;
        case "end_highlands" -> 0xBFC58C;
        case "end_midlands" -> 0xCCC993;
        case "end_barrens" -> 0xCBC19E;
        case "small_end_islands" -> 0xE5DEB3;
        case "the_void" -> 0x252328;
        default -> 0;
      };
    } else if (id.getNamespace().equals("magnatour")) {
      color = switch (id.getPath()) {
        case "big_lake" -> 0x3F76B8;
        case "lake_center_island" -> 0x8BBE68;
        case "melon_jungle" -> 0x589345;
        case "wheat_plain" -> 0xC4AC58;
        case "pumpkin_gorge" -> 0xC78349;
        case "ore_land" -> 0x92908B;
        case "ore_forest" -> 0x788B91;
        case "universe_meta_void" -> 0x34334D;
        default -> 0;
      };
    }
    if (color == 0) {
      int hash = id.toString().hashCode();
      hash ^= hash >>> 16;
      hash *= 0x7FEB352D;
      hash ^= hash >>> 15;
      color = (64 + ((hash >>> 16) & 127)) << 16
          | (64 + ((hash >>> 8) & 127)) << 8 | (64 + (hash & 127));
    }
    return 0xFF000000 | (color & 0xFFFFFF);
  }

  public static void init() {
    UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
      if (!player.isSpectator() && player.getItemInHand(hand).getItem() instanceof StarAtlas atlas) {
        return atlas.use(level, player, hand);
      }
      return InteractionResult.PASS;
    });
  }

  public static void initNetworking() { WorldMap.init(); }

  public static void setClientOpenHandler(
      Runnable handler
  ) {
    clientOpenHandler = handler;
  }

  @Override @NonNull
  public InteractionResult use(
      @NonNull Level level,
      @NonNull Player player,
      @NonNull InteractionHand hand
  ) {
    if (level.isClientSide()) clientOpenHandler.run();
    return InteractionResult.SUCCESS;
  }

  public record Payload(
      int action,
      ResourceKey<Level> dimension,
      int minX,
      int minZ,
      int maxX,
      int maxZ,
      byte[] pixels
  ) implements CustomPacketPayload {

    public static final int BATCH = 3;
    public static final int BATCH_ENTRY_BYTES = 72;
    public static final int BATCH_SIZE = 128;
    public static final int CLOSE = 2;
    public static final int TILE = 1;
    public static final int VIEW = 0;

    public static final Type<Payload> ID = new Type<>(
        Identifier.fromNamespaceAndPath("magnatour", "star_atlas"));

    private static final StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Level>> DIMENSION_CODEC =
        ResourceKey.<Level>streamCodec(Registries.DIMENSION).cast();

    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.of(
        (buffer, payload) -> {
          buffer.writeVarInt(payload.action());
          DIMENSION_CODEC.encode(buffer, payload.dimension());
          if (payload.action() == VIEW || payload.action() == TILE || payload.action() == BATCH) {
            buffer.writeVarInt(payload.minX());
            buffer.writeVarInt(payload.minZ());
            if (payload.action() == VIEW) {
              buffer.writeVarInt(payload.maxX());
              buffer.writeVarInt(payload.maxZ());
            } else buffer.writeByteArray(payload.pixels());
          }
        }, buffer -> {
          int action = buffer.readVarInt();
          ResourceKey<Level> dimension = DIMENSION_CODEC.decode(buffer);
          return switch (action) {
            case VIEW -> view(dimension, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
            case TILE -> tile(dimension, buffer.readVarInt(), buffer.readVarInt(), buffer.readByteArray(64));
            case BATCH -> batch(dimension, buffer.readVarInt(), buffer.readVarInt(),
                buffer.readByteArray(BATCH_SIZE * BATCH_ENTRY_BYTES));
            case CLOSE -> close(dimension);
            default -> throw new IllegalArgumentException("Unknown star atlas message");
          };
        });

    public Payload {
      boolean validLength = action == TILE ? pixels.length == 64 : action == BATCH
          ? pixels.length > 0 && pixels.length <= BATCH_SIZE * BATCH_ENTRY_BYTES
              && pixels.length % BATCH_ENTRY_BYTES == 0 : pixels.length == 0;
      if (action < VIEW || action > BATCH || !validLength) throw new IllegalArgumentException("Invalid star atlas message");
    }

    public static Payload batch(
        ResourceKey<Level> dimension,
        int unusedX,
        int unusedZ,
        byte[] cells
    ) {
      return new Payload(BATCH, dimension, unusedX, unusedZ, 0, 0, cells);
    }

    public static Payload close(
        ResourceKey<Level> dimension
    ) {
      return new Payload(CLOSE, dimension, 0, 0, 0, 0, new byte[0]);
    }

    public static Payload tile(
        ResourceKey<Level> dimension,
        int x,
        int z,
        byte[] pixels
    ) {
      return new Payload(TILE, dimension, x, z, 0, 0, pixels);
    }

    public static Payload view(
        ResourceKey<Level> dimension,
        int minX,
        int minZ,
        int maxX,
        int maxZ
    ) {
      return new Payload(VIEW, dimension, minX, minZ, maxX, maxZ, new byte[0]);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

  }

  private static final class WorldMap {

    private static final int MAX_ACTIVE_READS = 2048;
    private static final int MAX_WORK_PER_TICK = 4096;
    private static final int READ_THREADS = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    private static final long WORK_BUDGET_NANOS = 35_000_000L;

    private static final Map<UUID, Job> JOBS = new HashMap<>();

    private static ExecutorService workers;

    private static boolean jobContains(
        Payload view,
        ChunkPos pos
    ) {
      return pos.x() >= view.minX() && pos.x() <= view.maxX() && pos.z() >= view.minZ() && pos.z() <= view.maxZ();
    }

    private static void request(
        ServerPlayer player,
        Payload payload
    ) {
      if (!player.level().dimension().equals(payload.dimension())) return;
      if (payload.action() == Payload.CLOSE) {
        Job job = JOBS.remove(player.getUUID());
        if (job != null) job.cancelled = true;
        return;
      }
      if (payload.action() != Payload.VIEW || workers == null || workers.isShutdown()
          || !(player.getMainHandItem().getItem() instanceof StarAtlas
          || player.getOffhandItem().getItem() instanceof StarAtlas)) return;
      if (payload.minX() < -1875000 || payload.minZ() < -1875000
          || payload.maxX() > 1875000 || payload.maxZ() > 1875000
          || payload.maxX() < payload.minX() || payload.maxZ() < payload.minZ()
          || (long) payload.maxX() - payload.minX() > 768 || (long) payload.maxZ() - payload.minZ() > 768) return;
      Job job = JOBS.get(player.getUUID());
      if (job != null && job.level == player.level()) {
        boolean sameView = job.minX == payload.minX() && job.minZ == payload.minZ()
            && job.maxX == payload.maxX() && job.maxZ == payload.maxZ();
        job.started = player.level().getServer().getTickCount();
        if (sameView) return;
        job.minX = payload.minX();
        job.minZ = payload.minZ();
        job.maxX = payload.maxX();
        job.maxZ = payload.maxZ();
        job.sent.removeIf(pos -> !jobContains(payload, pos));
        job.pending.clear();
      } else {
        if (job != null) job.cancelled = true;
        job = new Job(player, payload);
        JOBS.put(player.getUUID(), job);
      }
      List<ChunkPos> visible = new ArrayList<>();
      for (int z = payload.minZ(); z <= payload.maxZ(); z++) {
        for (int x = payload.minX(); x <= payload.maxX(); x++) {
          ChunkPos pos = new ChunkPos(x, z);
          if (!job.sent.contains(pos) && !job.active.containsKey(pos)) visible.add(pos);
        }
      }
      double centerX = (payload.minX() + payload.maxX()) / 2.0;
      double centerZ = (payload.minZ() + payload.maxZ()) / 2.0;
      visible.sort(Comparator.comparingDouble(pos -> {
        double x = pos.x() - centerX;
        double z = pos.z() - centerZ;
        return x * x + z * z;
      }));
      job.pending.addAll(visible);
    }

    private static void sendBatch(
        Job job,
        ByteBuffer batch
    ) {
      ServerPlayNetworking.send(job.player, Payload.batch(job.level.dimension(), 0, 0,
          Arrays.copyOf(batch.array(), batch.position())));
      batch.clear();
    }

    private static CompletableFuture<int[]> predictBiomes(
        Job job,
        ChunkPos pos
    ) {
      return CompletableFuture.supplyAsync(() -> {
        if (job.cancelled) return null;
        int[] cells = new int[16];
        int quartX = pos.x() * 4;
        int quartZ = pos.z() * 4;
        for (int z = 0; z < 4; z++) {
          for (int x = 0; x < 4; x++) {
            if (job.cancelled) return null;
            cells[z * 4 + x] = job.registry.getId(job.resolver
                .getNoiseBiome(quartX + x, job.quartY, quartZ + z).value());
          }
        }
        return cells;
      }, workers);
    }

    private static void init() {
      PayloadTypeRegistry.serverboundPlay().register(Payload.ID, Payload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(Payload.ID, Payload.CODEC);
      ServerLifecycleEvents.SERVER_STARTED.register(server -> workers = Executors.newFixedThreadPool(READ_THREADS, task -> {
        Thread thread = new Thread(task, "Magnatour-StarAtlas-Biomes");
        thread.setDaemon(true);
        return thread;
      }));
      ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
        JOBS.values().forEach(job -> job.cancelled = true);
        JOBS.clear();
        if (workers != null) workers.shutdown();
      });
      ServerPlayNetworking.registerGlobalReceiver(Payload.ID, (payload, context) ->
          context.server().execute(() -> request(context.player(), payload)));
      ServerTickEvents.END_SERVER_TICK.register(WorldMap::tick);
    }

    private static void tick(
        MinecraftServer server
    ) {
      JOBS.values().removeIf(job -> {
        boolean expired = job.player.hasDisconnected() || job.player.level() != job.level
            || server.getTickCount() - job.started > 400
            || !(job.player.getMainHandItem().getItem() instanceof StarAtlas
            || job.player.getOffhandItem().getItem() instanceof StarAtlas);
        if (expired) job.cancelled = true;
        return expired;
      });
      long deadline = System.nanoTime() + WORK_BUDGET_NANOS;
      int scheduled = 0;
      int sent = 0;
      for (Job job : JOBS.values()) {
        ByteBuffer batch = ByteBuffer.allocate(Payload.BATCH_SIZE * Payload.BATCH_ENTRY_BYTES);
        boolean progress;
        do {
          progress = false;
          // Stream predictions while continuously replenishing the parallel calculation queue.
          var ready = job.active.entrySet().iterator();
          int drained = 0;
          while (ready.hasNext() && drained < Payload.BATCH_SIZE && sent < MAX_WORK_PER_TICK
              && System.nanoTime() < deadline) {
            var entry = ready.next();
            if (!entry.getValue().isDone()) continue;
            ready.remove();
            if (!job.contains(entry.getKey())) continue;
            int[] cells = null;
            try { cells = entry.getValue().join(); }
            catch (RuntimeException exception) {
              Magnatour.LOGGER.warn("Cannot predict star atlas biomes {}", entry.getKey(), exception);
            }
            batch.putInt(entry.getKey().x()).putInt(entry.getKey().z());
            for (int i = 0; i < 16; i++) batch.putInt(cells == null ? -1 : cells[i]);
            job.sent.add(entry.getKey());
            sent++;
            drained++;
            progress = true;
            if (!batch.hasRemaining()) sendBatch(job, batch);
          }
          int queued = 0;
          while (job.active.size() < MAX_ACTIVE_READS && scheduled < MAX_WORK_PER_TICK
              && queued < Payload.BATCH_SIZE && System.nanoTime() < deadline && !job.pending.isEmpty()) {
            ChunkPos pos = job.pending.removeFirst();
            job.active.put(pos, predictBiomes(job, pos));
            scheduled++;
            queued++;
            progress = true;
          }
        } while (progress && sent < MAX_WORK_PER_TICK && System.nanoTime() < deadline);
        if (batch.position() != 0) sendBatch(job, batch);
      }
    }

    private static final class Job {

      private int maxX;
      private int maxZ;
      private int minX;
      private int minZ;
      private final int quartY;
      private int started;

      private volatile boolean cancelled;

      private final ServerLevel level;

      private final ServerPlayer player;

      private final BiomeResolver resolver;

      private final Set<ChunkPos> sent = new HashSet<>();

      private final ArrayDeque<ChunkPos> pending = new ArrayDeque<>();

      private final Registry<Biome> registry;

      private final Map<ChunkPos, CompletableFuture<int[]>> active = new HashMap<>();

      private Job(
          ServerPlayer player,
          Payload view
      ) {
        this.player = player;
        minX = view.minX();
        minZ = view.minZ();
        maxX = view.maxX();
        maxZ = view.maxZ();
        level = player.level();
        resolver = level.getChunkSource().getGenerator().getBiomeSource()
            .createUncachedResolver(level.getChunkSource().randomState());
        quartY = Math.max(level.getMinY(), Math.min(level.getMaxY(), 64)) >> 2;
        registry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        started = level.getServer().getTickCount();
      }

      private boolean contains(
          ChunkPos pos
      ) {
        return pos.x() >= minX && pos.x() <= maxX && pos.z() >= minZ && pos.z() <= maxZ;
      }

    }

  }

}
