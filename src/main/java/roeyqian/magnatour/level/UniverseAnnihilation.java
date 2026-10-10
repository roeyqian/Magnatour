/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Java Standard
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

// Fabric
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.loader.api.FabricLoader;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.universe.UniverseAnnihilator;
import roeyqian.magnatour.mixin.server.UniverseAnnihilationServerAccessor;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public final class UniverseAnnihilation {

  public static final int IDLE = 0, PREPARING = 1, QUEUED = 2, SCANNING = 3,
      BACKUP = 4, DELETING = 5, REOPENING = 6, COMPLETE = 7, FAILED = 8;

  private static final Path BACKUPS = FabricLoader.getInstance().getGameDir().resolve("magnatour")
      .toAbsolutePath().normalize();
  private static final Path LEGACY_BACKUPS = Path.of("C:/Users/RoeyQ/.Config/.backup/magnatour-annihilation")
      .toAbsolutePath().normalize();

  private static final AnnihilationFileJob.Budget IO_BUDGET =
      AnnihilationFileJob.Budget.adaptiveBackup(8_000_000L);

  private static final List<BlockPos> REFUGE_POSITIONS = List.of(
      new BlockPos(100, 0, 100), new BlockPos(-100, 0, 100),
      new BlockPos(100, 0, -100), new BlockPos(-100, 0, -100));

  private static final Map<MinecraftServer, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());

  private UniverseAnnihilation() {}

  public static boolean canUse(
      ServerPlayer player
  ) {
    var server = player.level().getServer();
    return server.isSingleplayer() && server.getSingleplayerProfile() != null
        && server.getSingleplayerProfile().id().equals(player.getUUID())
        || player.permissions().hasPermission(Permissions.COMMANDS_OWNER);
  }

  public static Component dimensionName(
      ResourceKey<Level> dimension
  ) {
    String id = dimension.identifier().toString();
    return Component.translatableWithFallback("dimension." + id, id);
  }

  public static ResourceKey<Level> evacuationDimension(
      ResourceKey<Level> dimension
  ) {
    return dimension.equals(CustomDimensions.UNIVERSE_META) ? Level.OVERWORLD : CustomDimensions.UNIVERSE_META;
  }

  /** Shared by evacuation, login and respawn so none can redirect into a locked dimension. */
  public static Landing evacuationLanding(
      MinecraftServer server,
      ResourceKey<Level> dimension
  ) {
    ServerLevel destination = server.getLevel(evacuationDimension(dimension));
    if (destination == null || isLocked(destination)) throw new IllegalStateException("Evacuation dimension unavailable");
    if (destination.dimension().equals(CustomDimensions.UNIVERSE_META)) {
      BlockPos feet = REFUGE_POSITIONS.get(destination.getRandom().nextInt(REFUGE_POSITIONS.size()));
      createPlatform(destination, feet);
      return new Landing(destination, feet);
    }
    // Search from the origin outwards; require dry, empty space above a safe solid surface.
    for (int radius = 0; radius <= 32; radius++) {
      for (int x = -radius; x <= radius; x++) {
        for (int z = -radius; z <= radius; z++) {
          if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
          BlockPos feet = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
          if (safeLandingArea(destination, feet)) return new Landing(destination, feet);
        }
      }
    }
    // Ocean and void worlds may have no natural safe surface around the origin.
    int y = Math.clamp(destination.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0),
        destination.getMinY() + 1, destination.getMaxY() - 2);
    BlockPos feet = new BlockPos(0, y, 0);
    createPlatform(destination, feet);
    return new Landing(destination, feet);
  }

  /** Recovery runs before any dimension loads, including journals from the previous implementation. */
  public static void finishPending(
      MinecraftServer server
  ) {
    Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
    try {
      Path legacy = root.resolve("magnatour-annihilation.pending");
      if (Files.exists(legacy)) recover(root, legacy);
      Path journals = root.resolve("magnatour-annihilation");
      if (Files.exists(journals)) {
        if (Files.isSymbolicLink(journals)) throw new IOException("Symbolic link journal directory");
        try (var paths = Files.newDirectoryStream(journals, "*.pending")) {
          for (Path journal : paths) recover(root, journal);
        }
      }
    } catch (IOException | RuntimeException exception) {
      throw new IllegalStateException("Annihilation recovery failed; backups and journals retained", exception);
    }
  }

  public static void forget(
      MinecraftServer server
  ) { STATES.remove(server); }

  public static Task getTask(
      MinecraftServer server,
      UUID owner
  ) {
    State state = STATES.get(server);
    return state == null ? null : state.tasks.get(owner);
  }

  public static boolean hasPendingTask(
      MinecraftServer server
  ) {
    State state = STATES.get(server);
    return state != null && !state.locks.isEmpty();
  }

  public static boolean isLocked(
      ServerLevel level
  ) {
    State state = STATES.get(level.getServer());
    return state != null && state.locks.containsKey(level.dimension());
  }

  public static boolean request(
      ServerPlayer player,
      UniverseAnnihilator entity,
      ResourceKey<Level> dimension,
      boolean createImage
  ) {
    MinecraftServer server = player.level().getServer();
    State state = STATES.computeIfAbsent(server, ignored -> new State());
    Task previous = state.tasks.get(entity.getUUID());
    ResourceKey<Level> refuge = evacuationDimension(dimension);
    if (!canUse(player) || state.stopping || !server.isRunning()
        || dimension.equals(entity.level().dimension()) || server.getLevel(dimension) == null
        || server.getLevel(refuge) == null || state.locks.containsKey(refuge)
        || state.locks.keySet().stream().anyMatch(key -> evacuationDimension(key).equals(dimension))
        || state.locks.containsKey(dimension) || previous != null && previous.busy()) return false;
    try {
      // Minecraft creates levels from the live registry, which also contains datapack dimensions.
      // Resolve and retain the blueprint before creating a journal or touching dimension storage.
      LevelStem stem = requireLevelStem(server, dimension);
      Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
      Path target = checkedTarget(root, dimension);
      Path restoreTarget = target;
      if (dimension.equals(Level.OVERWORLD)) target = overworldStaging(root, entity.getUUID().toString());
      Path journals = root.resolve("magnatour-annihilation");
      Files.createDirectories(journals);
      Path backup;
      if (createImage) {
        Files.createDirectories(BACKUPS);
        backup = Files.createDirectory(BACKUPS.resolve(UUID.randomUUID().toString()));
        Files.writeString(backup.resolve("restore.txt"), "World: " + root + "\nDimension: " + dimension.identifier()
            + "\nRestore dimension/ to: " + restoreTarget
            + "\nStop the server before restoring.\n"
            + (dimension.equals(Level.OVERWORLD) ? "Restore only region/, entities/ and poi/; keep all other save files.\n" : ""));
      } else {
        // Only recovery metadata is retained; no world image is created in the game directory.
        backup = Files.createDirectory(journals.resolve(UUID.randomUUID() + ".task"));
      }
      Path journal = journals.resolve(entity.getUUID() + ".pending");
      Files.writeString(journal, dimension.identifier() + "\n" + backup + "\n" + createImage + "\n", StandardOpenOption.CREATE_NEW);
      Task task = new Task(entity.getUUID(), dimension, target, backup, journal, stem,
          server.getWorldData().isDebugWorld(), BiomeManager.obfuscateSeed(server.getWorldGenSettings().options().seed()), createImage);
      state.locks.put(dimension, task);
      state.tasks.put(entity.getUUID(), task);
      evacuate(server, dimension);
      player.sendSystemMessage(Component.translatable("gui.magnatour.annihilator.started", dimensionName(dimension)));
      return true;
    } catch (IOException | RuntimeException exception) {
      Task task = state.tasks.get(entity.getUUID());
      if (task != null && task.busy()) fail(task, exception);
      Magnatour.LOGGER.error("Could not prepare annihilation", exception);
      player.sendSystemMessage(Component.translatable("gui.magnatour.annihilator.failed"));
      return false;
    }
  }

  /** Stop IO before releasing the save lock; journals resume incomplete tasks at startup. */
  public static void stop(
      MinecraftServer server
  ) {
    State state = STATES.get(server);
    if (state == null) return;
    state.stopping = true;
    state.worker.shutdownNow();
    boolean interrupted = false;
    while (!state.worker.isTerminated()) {
      try { state.worker.awaitTermination(1, TimeUnit.SECONDS); }
      catch (InterruptedException exception) { interrupted = true; }
    }
    // Detached dimensions are absent from MinecraftServer.levels and need explicit closure.
    for (Task task : state.tasks.values()) {
      if (task.closingLevel == null) continue;
      ServerLevel level = task.closingLevel;
      if (task.dimension.equals(Level.OVERWORLD) && server.getLevel(task.dimension) == level) {
        task.closingLevel = null;
        continue; // Vanilla shutdown owns the still-attached Overworld.
      }
      try {
        while (level.getChunkSource().chunkMap.hasWork()) {
          while (level.getChunkSource().pollTask()) {}
          level.getChunkSource().tick(() -> true, false);
          java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L);
        }
        level.save(null, true, false);
        level.close();
        task.closingLevel = null;
      } catch (IOException | RuntimeException exception) { fail(task, exception); }
    }
    if (interrupted) Thread.currentThread().interrupt();
  }

  /** World lifecycle changes happen between server ticks, never on the IO worker. */
  public static void tick(
      MinecraftServer server
  ) {
    State state = STATES.get(server);
    if (state == null || state.stopping) return;
    for (Task task : state.tasks.values()) {
      if (task.stage == REOPENING) {
        try {
          if (!task.overworldReopened) reopen(server, task);
          AnnihilationFileJob.complete(task.backup, task.journal);
          task.update(COMPLETE, 10000);
          state.locks.remove(task.dimension, task);
          Magnatour.LOGGER.info("Annihilation complete; dimension: {}; image created: {}; task files: {}",
              task.dimension.identifier(), task.createImage, task.backup);
          Component notification = Component.translatable("gui.magnatour.annihilator.completed",
              dimensionName(task.dimension));
          for (ServerPlayer player : server.getPlayerList().getPlayers()) player.sendSystemMessage(notification);
        } catch (IOException | RuntimeException exception) { fail(task, exception); }
      }
    }
    // Drain one world per tick with a small budget before the final synchronous close.
    for (Task task : state.tasks.values()) {
      if (task.stage != PREPARING) continue;
      try {
        if (task.closingLevel == null) {
          evacuate(server, task.dimension);
          ServerLevel level = server.getLevel(task.dimension);
          if (level == null || !level.players().isEmpty()) throw new IOException("Dimension not empty");
          task.closingLevel = level;
          // The Overworld must remain available to server services until it can be replaced in this tick.
          if (!task.dimension.equals(Level.OVERWORLD)) {
            ((UniverseAnnihilationServerAccessor) server).magnatour$getLevels().remove(task.dimension);
          }
          level.getChunkSource().deactivateTicketsOnClosing();
          if (!task.dimension.equals(Level.OVERWORLD)) ServerLevelEvents.UNLOAD.invoker().onLevelUnload(server, level);
        }
        ServerLevel level = task.closingLevel;
        long deadline = System.nanoTime() + 2_000_000L;
        while (System.nanoTime() < deadline && level.getChunkSource().pollTask()) {}
        level.getChunkSource().tick(() -> System.nanoTime() < deadline, false);
        if (level.getChunkSource().chunkMap.hasWork()) break;
        if (task.dimension.equals(Level.OVERWORLD)) {
          evacuate(server, task.dimension);
          ServerLevelEvents.UNLOAD.invoker().onLevelUnload(server, level);
        }
        level.save(null, true, false);
        level.close();
        task.closingLevel = null;
        if (task.dimension.equals(Level.OVERWORLD)) {
          ((UniverseAnnihilationServerAccessor) server).magnatour$getLevels().remove(task.dimension);
          try {
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            AnnihilationFileJob.detachOverworld(checkedTarget(root, Level.OVERWORLD), task.target, task.backup);
          } finally {
            // Even a failed move must leave a live Overworld for ticking and shutdown.
            reopen(server, task);
            task.overworldReopened = true;
          }
        }
        task.update(QUEUED, 100);
        state.worker.execute(() -> {
          try { AnnihilationFileJob.run(task.target, task.backup, task::update, IO_BUDGET, task.createImage); }
          catch (IOException | RuntimeException exception) { fail(task, exception); }
        });
      } catch (IOException | RuntimeException exception) { fail(task, exception); }
      break;
    }
  }

  private static boolean safeLandingArea(
      ServerLevel level,
      BlockPos feet
  ) {
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        if (!safeStandingPosition(level, feet.offset(x, 0, z))) return false;
      }
    }
    return true;
  }

  private static void createPlatform(
      ServerLevel level,
      BlockPos feet
  ) {
    if (feet.getY() <= level.getMinY() || feet.getY() + 1 >= level.getMaxY()) {
      throw new IllegalStateException("Evacuation platform outside build height");
    }
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        if (!level.getWorldBorder().isWithinBounds(feet.offset(x, 0, z))) {
          throw new IllegalStateException("Evacuation platform outside world border");
        }
      }
    }
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        BlockPos column = feet.offset(x, 0, z);
        level.setBlock(column.below(), Blocks.BEDROCK.defaultBlockState(), 3);
        level.setBlock(column, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(column.above(), Blocks.AIR.defaultBlockState(), 3);
      }
    }
    if (!safeLandingArea(level, feet)) throw new IllegalStateException("Could not create evacuation platform");
  }

  private static void recover(
      Path root, Path journal
  ) throws IOException {
    if (Files.isSymbolicLink(journal)) throw new IOException("Symbolic link journal");
    var lines = Files.readAllLines(journal);
    boolean createImage = AnnihilationFileJob.readCreateImage(lines);
    var dimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(lines.get(0)));
    Path target = checkedTarget(root, dimension);
    Path backup = Path.of(lines.get(1)).toAbsolutePath().normalize();
    if (Files.isSymbolicLink(backup)) throw new IOException("Symbolic link task directory");
    if (createImage) {
      if (!BACKUPS.equals(backup.getParent()) && !LEGACY_BACKUPS.equals(backup.getParent())) {
        throw new IOException("Invalid backup path");
      }
    } else {
      String name = backup.getFileName().toString();
      if (!root.resolve("magnatour-annihilation").equals(backup.getParent()) || !name.endsWith(".task")) {
        throw new IOException("Invalid task directory");
      }
      UUID.fromString(name.substring(0, name.length() - ".task".length()));
    }
    if (dimension.equals(Level.OVERWORLD)) {
      String owner = journal.getFileName().toString().replace(".pending", "");
      UUID.fromString(owner);
      Path staging = overworldStaging(root, owner);
      AnnihilationFileJob.detachOverworld(target, staging, backup);
      target = staging;
    }
    AnnihilationFileJob.run(target, backup, (phase, percent) -> {}, IO_BUDGET, createImage);
    AnnihilationFileJob.complete(backup, journal);
  }

  private static Path checkedTarget(
      Path root,
      ResourceKey<Level> dimension
  ) throws IOException {
    Path target = DimensionType.getStorageFolder(dimension, root).toAbsolutePath().normalize();
    if (!target.startsWith(root.resolve("dimensions")) || target.equals(root.resolve("dimensions"))) {
      throw new IOException("Dimension path escapes save");
    }
    for (Path path = target; path != null; path = path.getParent()) {
      if (Files.isSymbolicLink(path)) throw new IOException("Symbolic link in dimension path");
      if (Files.exists(path) && !path.toRealPath().startsWith(root.toRealPath())) {
        throw new IOException("Dimension path resolves outside save");
      }
      if (path.equals(root)) break;
    }
    return target;
  }

  private static Path overworldStaging(
      Path root,
      String owner
  ) throws IOException {
    UUID.fromString(owner);
    Path staging = root.resolve("magnatour-annihilation").resolve(owner + ".overworld");
    for (Path path = staging; path != null && !path.equals(root); path = path.getParent()) {
      if (Files.isSymbolicLink(path)
          || Files.exists(path) && !path.toRealPath().startsWith(root.toRealPath())) {
        throw new IOException("Unsafe Overworld staging path");
      }
    }
    return staging;
  }

  private static LevelStem requireLevelStem(
      MinecraftServer server,
      ResourceKey<Level> dimension
  ) throws IOException {
    LevelStem stem = server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM)
        .getValue(ResourceKey.create(Registries.LEVEL_STEM, dimension.identifier()));
    if (stem == null) throw new IOException("Missing live dimension generator: " + dimension.identifier());
    return stem;
  }

  private static void evacuate(
      MinecraftServer server,
      ResourceKey<Level> dimension
  ) {
    for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
      if (!player.level().dimension().equals(dimension)) continue;
      Landing landing = evacuationLanding(server, dimension);
      player.stopRiding(); player.closeContainer();
      if (!player.teleportTo(landing.level(), landing.position().getX() + 0.5,
          landing.position().getY(), landing.position().getZ() + 0.5,
          Set.of(), player.getYRot(), player.getXRot(), true)
          || player.level().dimension().equals(dimension)) {
        throw new IllegalStateException("Could not evacuate player " + player.getUUID());
      }
      player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
    }
  }

  private static void fail(
      Task task,
      Exception exception
  ) {
    int failedStage = task.stage;
    task.stage = FAILED;
    Magnatour.LOGGER.error("Annihilation failed during phase {} at {}%; dimension remains locked; journal: {}",
        failedStage, task.progress / 100.0, task.journal, exception);
  }

  private static boolean safeStandingPosition(
      ServerLevel level,
      BlockPos feet
  ) {
    if (feet.getY() <= level.getMinY() || feet.getY() + 1 >= level.getMaxY()
        || !level.getWorldBorder().isWithinBounds(feet)
        || !level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) return false;
    BlockPos floor = feet.below();
    var state = level.getBlockState(floor);
    return state.isFaceSturdy(level, floor, Direction.UP) && state.getFluidState().isEmpty()
        && !state.is(Blocks.MAGMA_BLOCK) && !state.is(Blocks.CACTUS)
        && !state.is(Blocks.CAMPFIRE) && !state.is(Blocks.SOUL_CAMPFIRE);
  }

  private static void reopen(
      MinecraftServer server,
      Task task
  ) throws IOException {
    ResourceKey<Level> dimension = task.dimension;
    if (server.getLevel(dimension) != null) throw new IOException("Dimension already loaded");
    var access = (UniverseAnnihilationServerAccessor) server;
    boolean overworld = dimension.equals(Level.OVERWORLD);
    ServerLevel level = new ServerLevel(server, access.magnatour$getExecutor(), access.magnatour$getStorage(),
        overworld ? server.getWorldData().overworldData()
            : new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData()), dimension,
        task.stem, task.debugWorld, task.biomeSeed,
        overworld ? List.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(),
            new VillageSiege(), new WanderingTraderSpawner(server.getDataStorage())) : List.of(), overworld);
    level.getWorldBorder().setAbsoluteMaxSize(server.getAbsoluteMaxWorldSize());
    server.getPlayerList().addWorldborderListener(level);
    access.magnatour$getLevels().put(dimension, level);
    ServerLevelEvents.LOAD.invoker().onLevelLoad(server, level);
  }

  public record Landing(
      ServerLevel level,
      BlockPos position
  ) {}

  private static final class State {

    private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();

    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Magnatour annihilation IO");
      thread.setDaemon(true); thread.setPriority(Thread.MIN_PRIORITY); return thread;
    });

    private final Map<ResourceKey<Level>, Task> locks = new ConcurrentHashMap<>();

    private volatile boolean stopping;

  }

  public static final class Task {

    public final UUID owner;

    public final ResourceKey<Level> dimension;

    private final boolean createImage;
    private final boolean debugWorld;

    private final long biomeSeed;

    private final Path target, backup, journal;

    private final LevelStem stem;

    private boolean overworldReopened;

    private volatile int progress;

    private ServerLevel closingLevel;

    private volatile int stage = PREPARING;

    private Task(
        UUID owner,
        ResourceKey<Level> dimension,
        Path target, Path backup, Path journal,
        LevelStem stem,
        boolean debugWorld,
        long biomeSeed,
        boolean createImage
    ) {
      this.owner = owner; this.dimension = dimension; this.target = target;
      this.backup = backup; this.journal = journal;
      this.stem = stem; this.debugWorld = debugWorld; this.biomeSeed = biomeSeed;
      this.createImage = createImage;
    }

    public boolean busy() { return stage != COMPLETE; }

    public int progress() { return progress; }

    public int stage() { return stage; }

    private void update(
        int phase, int percent
    ) {
      progress = Math.max(progress, Math.min(10000, percent)); stage = phase;
    }

  }

}
