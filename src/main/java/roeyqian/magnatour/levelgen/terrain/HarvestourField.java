/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

// Java Standard
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/** Continuous, seeded harvestour values with small threshold excursions suppressed. */
final class HarvestourField {

  private static final int CACHE_LIMIT = 8192;
  // Large components are retained even if a deep core is farther away.
  private static final int EXCURSION_SEARCH_LIMIT = 64;
  private static final int GRID_SIZE = 512;

  private static final long NOISE_SALT = 0xA54FF53A5F1D36F1L;

  private static final double AMPLITUDE = 150.0;
  private static final double DETAIL_WEIGHT = 0.25;
  // Preserve the original 4096-, 2048- and 1024-block wavelengths.
  private static final double NOISE_SCALE = 1.0 / 4096.0;
  // Less than half the narrowest biome interval, leaving every biome a stable core.
  private static final double THRESHOLD_MARGIN = 6.0;

  private static final ConcurrentHashMap<Node, Double> MEDIAN_NODES = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Node, Double> NODES = new ConcurrentHashMap<>();

  private HarvestourField() {}

  private static boolean hasDeepOrLargeRegion(
      Node start,
      double threshold,
      boolean above
  ) {
    ArrayDeque<Node> pending = new ArrayDeque<>();
    HashSet<Node> visited = new HashSet<>();
    pending.add(start);
    visited.add(start);
    while (!pending.isEmpty()) {
      Node current = pending.removeFirst();
      double value = medianNode(current);
      if (Math.abs(value - threshold) >= THRESHOLD_MARGIN) return true;
      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (Math.abs(dx) + Math.abs(dz) != 1) continue;
          Node neighbor = new Node(current.seed(), current.x() + dx, current.z() + dz);
          if (visited.contains(neighbor)) continue;
          if ((medianNode(neighbor) >= threshold) != above) continue;
          visited.add(neighbor);
          if (visited.size() >= EXCURSION_SEARCH_LIMIT) return true;
          pending.addLast(neighbor);
        }
      }
    }
    // The entire small component turned back before reaching threshold +/- margin.
    return false;
  }

  private static double medianNode(
      Node node
  ) {
    if (MEDIAN_NODES.size() > CACHE_LIMIT) MEDIAN_NODES.clear();
    return MEDIAN_NODES.computeIfAbsent(node, HarvestourField::computeMedian);
  }

  private static double raw(
      long seed,
      double x,
      double z
  ) {
    long noiseSeed = seed ^ NOISE_SALT;
    double broad = HarvestContinentTerrain.fbmPerlin(noiseSeed,
        x + 173.25, z - 419.75, NOISE_SCALE, 1);
    double detail = HarvestContinentTerrain.fbmPerlin(noiseSeed + 1013L,
        x + 173.25, z - 419.75, NOISE_SCALE * 2.0, 1);
    double fine = HarvestContinentTerrain.fbmPerlin(noiseSeed + 2026L,
        x + 173.25, z - 419.75, NOISE_SCALE * 4.0, 1);
    double fineWeight = DETAIL_WEIGHT * DETAIL_WEIGHT;
    return 50.0 + (broad + detail * DETAIL_WEIGHT + fine * fineWeight)
        / (1.0 + DETAIL_WEIGHT + fineWeight) * AMPLITUDE;
  }

  private static double bufferThreshold(
      Node node,
      double value,
      double threshold
  ) {
    if (Math.abs(value - threshold) >= THRESHOLD_MARGIN) return value;
    boolean above = value >= threshold;
    double direction = above ? 1.0 : -1.0;
    // A shallow crossing survives only if its connected region has a deep core,
    // or is already large. This is spatial and independent of sampling order.
    if (!hasDeepOrLargeRegion(node, threshold, above)) direction = -direction;
    return threshold + direction * THRESHOLD_MARGIN;
  }

  private static double fade(
      double value
  ) {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
  }

  private static double lerp(
      double delta,
      double start,
      double end
  ) {
    return start + delta * (end - start);
  }

  private static double node(
      long seed,
      int gx,
      int gz
  ) {
    if (NODES.size() > CACHE_LIMIT) NODES.clear();
    return NODES.computeIfAbsent(new Node(seed, gx, gz), HarvestourField::filterNode);
  }

  private static double computeMedian(
      Node node
  ) {
    double[] neighborhood = new double[9];
    int index = 0;
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        neighborhood[index++] = raw(node.seed(),
            (node.x() + dx) * (double) GRID_SIZE,
            (node.z() + dz) * (double) GRID_SIZE);
      }
    }
    // Isolated peaks and pits lose to their neighbors before biome thresholds apply.
    Arrays.sort(neighborhood);
    return neighborhood[4];
  }

  private static double filterNode(
      Node node
  ) {
    double value = medianNode(node);
    value = bufferThreshold(node, value, HarvestContinentTerrain.LAKE_HARVESTOUR_LIMIT);
    value = bufferThreshold(node, value, HarvestContinentTerrain.WHEAT_HARVESTOUR_LIMIT);
    return bufferThreshold(node, value, HarvestContinentTerrain.MELON_HARVESTOUR_LIMIT);
  }

  static double sample(
      long seed,
      int worldX,
      int worldZ
  ) {
    int gx = Math.floorDiv(worldX, GRID_SIZE);
    int gz = Math.floorDiv(worldZ, GRID_SIZE);
    double tx = fade(Math.floorMod(worldX, GRID_SIZE) / (double) GRID_SIZE);
    double tz = fade(Math.floorMod(worldZ, GRID_SIZE) / (double) GRID_SIZE);
    double north = lerp(tx, node(seed, gx, gz), node(seed, gx + 1, gz));
    double south = lerp(tx, node(seed, gx, gz + 1), node(seed, gx + 1, gz + 1));
    // Convex interpolation cannot introduce extrema outside the filtered node range.
    return lerp(tz, north, south);
  }

  private record Node(
      long seed,
      int x,
      int z
  ) {}

}
