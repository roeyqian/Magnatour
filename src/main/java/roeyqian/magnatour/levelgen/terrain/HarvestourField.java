/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

// Java Standard
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/** Continuous harvestour normalized to percentiles of the same seeded terrain field. */
final class HarvestourField {

  private static final int CACHE_LIMIT = 8192;
  private static final int DISTRIBUTION_CACHE_LIMIT = 4;
  private static final int DISTRIBUTION_SAMPLES = 16384;
  private static final int GRID_SIZE = 512;

  private static final long NOISE_SALT = 0xA54FF53A5F1D36F1L;

  private static final double AMPLITUDE = 150.0;
  private static final double DETAIL_WEIGHT = 0.25;
  // Preserve the original 4096-, 2048- and 1024-block wavelengths.
  private static final double NOISE_SCALE = 1.0 / 4096.0;

  private static final ConcurrentHashMap<Node, Double> MEDIAN_NODES = new ConcurrentHashMap<>();

  private static final ConcurrentHashMap<Long, double[]> DISTRIBUTIONS = new ConcurrentHashMap<>();

  private HarvestourField() {}

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

  private static double rawSample(
      long seed,
      int worldX,
      int worldZ
  ) {
    int gx = Math.floorDiv(worldX, GRID_SIZE);
    int gz = Math.floorDiv(worldZ, GRID_SIZE);
    double tx = fade(Math.floorMod(worldX, GRID_SIZE) / (double) GRID_SIZE);
    double tz = fade(Math.floorMod(worldZ, GRID_SIZE) / (double) GRID_SIZE);
    double north = lerp(tx, medianNode(new Node(seed, gx, gz)), medianNode(new Node(seed, gx + 1, gz)));
    double south = lerp(tx, medianNode(new Node(seed, gx, gz + 1)), medianNode(new Node(seed, gx + 1, gz + 1)));
    // Median nodes suppress isolated extrema before convex interpolation.
    return lerp(tz, north, south);
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

  /** Deterministic reference samples calibrate area shares without a second selection field. */
  private static double[] createDistribution(
      long seed
  ) {
    Random random = new Random(seed ^ 0xD1310BA698DFB5ACL);
    double[] distribution = new double[DISTRIBUTION_SAMPLES];
    for (int index = 0; index < distribution.length; index++) {
      int x = random.nextInt(16777216) - 8388608;
      int z = random.nextInt(16777216) - 8388608;
      distribution[index] = rawSample(seed, x, z);
    }
    Arrays.sort(distribution);
    return distribution;
  }

  static double sample(
      long seed,
      int worldX,
      int worldZ
  ) {
    if (DISTRIBUTIONS.size() > DISTRIBUTION_CACHE_LIMIT) DISTRIBUTIONS.clear();
    double[] distribution = DISTRIBUTIONS.computeIfAbsent(seed, HarvestourField::createDistribution);
    double value = rawSample(seed, worldX, worldZ);
    int index = Arrays.binarySearch(distribution, value);
    if (index >= 0) return index * 100.0 / (distribution.length - 1);
    int upper = -index - 1;
    if (upper == 0) return 0.0;
    if (upper == distribution.length) return 100.0;
    double span = distribution[upper] - distribution[upper - 1];
    double fraction = span == 0.0 ? 0.0 : (value - distribution[upper - 1]) / span;
    return (upper - 1 + fraction) * 100.0 / (distribution.length - 1);
  }

  private record Node(
      long seed,
      int x,
      int z
  ) {}

}
