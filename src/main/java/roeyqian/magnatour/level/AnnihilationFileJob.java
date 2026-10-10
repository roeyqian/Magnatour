/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Java Standard
import java.io.InterruptedIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/** Bounded-memory file work; never accesses world or entity objects on the IO worker. */
public final class AnnihilationFileJob {

  private static final List<String> OVERWORLD_DIRECTORIES = List.of("region", "entities", "poi");

  private AnnihilationFileJob() {}

  public static void complete(
      Path backup, Path journal
  ) throws IOException {
    Files.move(journal, backup.resolve("completed-journal"), StandardCopyOption.REPLACE_EXISTING);
  }

  /** Rename only chunk storage; retain dimension settings and shared generation bookkeeping. */
  public static void detachOverworld(
      Path storage, Path staging, Path backup
  ) throws IOException {
    Path detached = backup.resolve("overworld-detached");
    if (Files.exists(detached)) return;
    Files.createDirectories(staging);
    try {
      for (String name : OVERWORLD_DIRECTORIES) {
        Path source = storage.resolve(name), destination = staging.resolve(name);
        if (Files.isSymbolicLink(source) || Files.isSymbolicLink(destination)) {
          throw new IOException("Symbolic link in Overworld storage");
        }
        if (!Files.exists(source)) continue;
        if (Files.exists(destination)) throw new IOException("Conflicting staged Overworld storage: " + name);
        Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
      }
      // Recovery must never touch the replacement world's newly generated chunks.
      Files.writeString(detached, "Original Overworld chunk storage detached; do not move live storage again.\n",
          StandardOpenOption.CREATE_NEW);
    } catch (IOException | RuntimeException exception) {
      for (String name : OVERWORLD_DIRECTORIES) {
        Path source = staging.resolve(name), destination = storage.resolve(name);
        if (!Files.exists(source) || Files.exists(destination)) continue;
        try { Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE); }
        catch (IOException rollback) { exception.addSuppressed(rollback); }
      }
      throw exception;
    }
  }

  /** Two-line journals predate the option and always require a world image. */
  public static boolean readCreateImage(
      List<String> journal
  ) throws IOException {
    if (journal.size() == 2) return true;
    if (journal.size() == 3) {
      if (journal.get(2).equals("true")) return true;
      if (journal.get(2).equals("false")) return false;
    }
    throw new IOException("Invalid annihilation journal image option");
  }

  public static void run(
      Path target, Path backup,
      Progress progress,
      Budget budget
  ) throws IOException {
    run(target, backup, progress, budget, true);
  }

  public static void run(
      Path target, Path backup,
      Progress progress,
      Budget budget,
      boolean createImage
  ) throws IOException {
    Path copied = backup.resolve("dimension"), complete = backup.resolve("backup-complete");
    long[] totals = {0, 0};
    progress.update(3, 200);
    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      Files.walkFileTree(target, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
          check(path, attrs); totals[1]++; budget.entry(); return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
          check(path, attrs); totals[0] += attrs.size(); totals[1]++; budget.entry();
          return FileVisitResult.CONTINUE;
        }
      });
    }
    if (createImage && !Files.exists(complete)) {
      Files.createDirectories(copied);
      long[] copiedBytes = {0};
      CopyBudget copyBudget = budget.newCopyBudget();
      progress.update(4, 500);
      if (Files.exists(target)) {
        Files.walkFileTree(target, new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
            check(path, attrs);
            Files.createDirectories(copied.resolve(target.relativize(path)));
            budget.entry(); return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            check(path, attrs);
            Path destination = copied.resolve(target.relativize(path));
            if (Files.isSymbolicLink(destination)) throw new IOException("Symbolic link in backup");
            byte[] buffer = new byte[65536];
            try (var input = Files.newInputStream(path); var output = Files.newOutputStream(destination)) {
              while (true) {
                budget.pause(0);
                long started = System.nanoTime();
                int read = input.read(buffer);
                if (read == -1) break;
                output.write(buffer, 0, read);
                budget.pause(copyBudget.record(read, System.nanoTime() - started));
                copiedBytes[0] += read;
                progress.update(4, 500 + (int) (7000L * copiedBytes[0] / Math.max(1, totals[0])));
              }
            }
            if (Files.size(destination) != attrs.size()) throw new IOException("Incomplete backup: " + path);
            Files.setLastModifiedTime(destination, attrs.lastModifiedTime());
            budget.entry(); return FileVisitResult.CONTINUE;
          }
        });
      }
      // When an image is requested, all copy streams must close before deleting any original.
      Files.writeString(complete, "Complete backup; safe to resume deletion.\n");
    }
    int deletionStart = createImage ? 7500 : 500;
    int deletionRange = 9500 - deletionStart;
    progress.update(5, deletionStart);
    long[] deleted = {0};
    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      Files.walkFileTree(target, new SimpleFileVisitor<>() {
        private void remove(Path path) throws IOException {
          budget.entry(); Files.delete(path); deleted[0]++;
          progress.update(5, deletionStart + (int) ((long) deletionRange * deleted[0] / Math.max(1, totals[1])));
        }
        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
          check(path, attrs); return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
          check(path, attrs); remove(path); return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
          if (exception != null) throw exception;
          remove(path); return FileVisitResult.CONTINUE;
        }
      });
    }
    progress.update(6, 9500);
  }

  private static void check(
      Path path,
      BasicFileAttributes attrs
  ) throws IOException {
    if (attrs.isSymbolicLink() || (!attrs.isDirectory() && !attrs.isRegularFile())) {
      throw new IOException("Unsupported link or special file: " + path);
    }
  }

  public static final class Budget {

    private final boolean adaptive;

    private final long bytesPerSecond, entryDelayNanos;

    public Budget(
        long bytesPerSecond, long entryDelayNanos
    ) {
      this(bytesPerSecond, entryDelayNanos, false);
    }

    private Budget(
        long bytesPerSecond, long entryDelayNanos,
        boolean adaptive
    ) {
      if (bytesPerSecond <= 0 || entryDelayNanos < 0) throw new IllegalArgumentException("Invalid IO budget");
      this.bytesPerSecond = bytesPerSecond;
      this.entryDelayNanos = entryDelayNanos;
      this.adaptive = adaptive;
    }

    public static Budget adaptiveBackup(
        long entryDelayNanos
    ) {
      return new Budget(4L * 1024 * 1024, entryDelayNanos, true);
    }

    CopyBudget newCopyBudget() { return new CopyBudget(bytesPerSecond, adaptive); }

    private void pause(
        long nanos
    ) throws InterruptedIOException {
      if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Annihilation paused");
      long started = System.nanoTime();
      long remaining = nanos;
      while (remaining > 0) {
        LockSupport.parkNanos(remaining);
        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Annihilation paused");
        remaining = nanos - (System.nanoTime() - started);
      }
      if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Annihilation paused");
    }

    private void entry() throws InterruptedIOException { pause(entryDelayNanos); }

  }

  /** Each backup gets a fresh sample; deliberate pacing never contributes to measured IO time. */
  static final class CopyBudget {

    private boolean sampling;

    private long bytesPerSecond, sampleBytes, sampleNanos;

    CopyBudget(
        long bytesPerSecond,
        boolean sampling
    ) {
      this.bytesPerSecond = bytesPerSecond;
      this.sampling = sampling;
    }

    long record(
        int count,
        long ioNanos
    ) {
      ioNanos = Math.max(0, ioNanos);
      if (sampling) {
        sampleBytes += count;
        sampleNanos += ioNanos;
        if (sampleBytes >= 1024 * 1024 || sampleNanos >= 250_000_000L) {
          bytesPerSecond = (double) sampleBytes * 1_000_000_000L
              > (double) (10L * 1024 * 1024) * sampleNanos
              ? 8L * 1024 * 1024 : 4L * 1024 * 1024;
          sampling = false;
        }
      }
      return Math.max(0, (long) count * 1_000_000_000L / bytesPerSecond - ioNanos);
    }

  }

  @FunctionalInterface
  public interface Progress {

    void update(
        int phase, int percent
    );

  }

}
