/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// FastUtil
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class VirtualBlockLightManager {

  public static final int LIGHT_LEVEL = 15;

  private static final int HALF_X = 32;
  private static final int HALF_Y = 16;
  private static final int HALF_Z = 32;
  private static final int NODE_SPACING = 2;

  private static final Map<BlockGetter, Long2ObjectOpenHashMap<Int2IntOpenHashMap>> LIGHT_SOURCES = Collections.synchronizedMap(
      new WeakHashMap<>()
  );

  private VirtualBlockLightManager() {}

  public static int getLightEmission(
      BlockGetter world,
      long pos,
      BlockState state
  ) {
    if (!state.isAir()) return 0;

    synchronized (LIGHT_SOURCES) {
      Long2ObjectOpenHashMap<Int2IntOpenHashMap> sources = LIGHT_SOURCES.get(world);
      if (sources == null) return 0;
      Int2IntOpenHashMap lightCounts = sources.get(pos);
      if (lightCounts == null) return 0;

      int lightLevel = 0;
      for (int currentLightLevel : lightCounts.keySet()) {
        if (lightCounts.get(currentLightLevel) > 0) lightLevel = Math.max(lightLevel, currentLightLevel);
      }
      return lightLevel;
    }
  }

  public static void setActive(
      Level world,
      BlockPos origin,
      boolean active
  ) {
    setActive(world, origin, LIGHT_LEVEL, active);
  }

  public static void setActive(
      Level world,
      BlockPos origin,
      int lightLevel,
      boolean active
  ) {
    if (lightLevel <= 0 || lightLevel > LIGHT_LEVEL) return;

    if (active) addSources(world, origin, lightLevel);
    else removeSources(world, origin, lightLevel);
  }

  private static void addSources(
      Level world,
      BlockPos origin,
      int lightLevel
  ) {
    synchronized (LIGHT_SOURCES) {
      Long2ObjectOpenHashMap<Int2IntOpenHashMap> sources = LIGHT_SOURCES.computeIfAbsent(
          world,
          _ -> new Long2ObjectOpenHashMap<>()
      );
      forEachSource(origin, sourcePos -> {
        Int2IntOpenHashMap lightCounts = sources.computeIfAbsent(sourcePos, _ -> {
          Int2IntOpenHashMap map = new Int2IntOpenHashMap();
          map.defaultReturnValue(0);
          return map;
        });
        int previousLightLevel = getMaxLightLevel(lightCounts);
        lightCounts.addTo(lightLevel, 1);
        if (previousLightLevel != getMaxLightLevel(lightCounts)) checkSource(world, sourcePos);
      });
    }
  }

  private static void removeSources(
      Level world,
      BlockPos origin,
      int lightLevel
  ) {
    synchronized (LIGHT_SOURCES) {
      Long2ObjectOpenHashMap<Int2IntOpenHashMap> sources = LIGHT_SOURCES.get(world);
      if (sources == null) return;

      forEachSource(origin, sourcePos -> {
        Int2IntOpenHashMap lightCounts = sources.get(sourcePos);
        if (lightCounts == null) return;

        int previousLightLevel = getMaxLightLevel(lightCounts);
        int previousCount = lightCounts.get(lightLevel);
        if (previousCount <= 0) return;

        if (previousCount == 1) lightCounts.remove(lightLevel);
        else lightCounts.put(lightLevel, previousCount - 1);

        if (lightCounts.isEmpty()) sources.remove(sourcePos);
        if (previousLightLevel != getMaxLightLevel(lightCounts)) checkSource(world, sourcePos);
      });

      if (sources.isEmpty()) LIGHT_SOURCES.remove(world);
    }
  }

  private static void forEachSource(
      BlockPos origin,
      LongConsumer consumer
  ) {
    for (int x = -HALF_X; x <= HALF_X; x += NODE_SPACING) {
      for (int y = -HALF_Y; y <= HALF_Y; y += NODE_SPACING) {
        for (int z = -HALF_Z; z <= HALF_Z; z += NODE_SPACING) {
          consumer.accept(BlockPos.asLong(origin.getX() + x, origin.getY() + y, origin.getZ() + z));
        }
      }
    }
  }

  private static int getMaxLightLevel(
      Int2IntOpenHashMap lightCounts
  ) {
    int lightLevel = 0;
    for (int currentLightLevel : lightCounts.keySet()) {
      if (lightCounts.get(currentLightLevel) > 0) lightLevel = Math.max(lightLevel, currentLightLevel);
    }
    return lightLevel;
  }

  private static void checkSource(
      Level world,
      long sourcePos
  ) {
    int y = BlockPos.getY(sourcePos);
    if (world.isOutsideBuildHeight(y)) return;

    world.getLightEngine().checkBlock(BlockPos.of(sourcePos));
  }

}
