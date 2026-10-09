/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.terrain;

// Java Standard
import java.util.Random;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

// Magnatour
import roeyqian.magnatour.Magnatour;

public final class HarvestContinentTerrain {

  // Exclusive bounds; the bottom remains solid and high plateaus avoid cave work.
  public static final int CAVE_MIN_Y = -58;
  public static final int CAVE_SURFACE_COVER = 7;
  public static final int GEN_DEPTH = 640;
  // Translate every surface profile by the same amount, preserving relief and blending.
  public static final int SURFACE_Y_OFFSET = -64;
  public static final int CAVE_MAX_Y = 128 + SURFACE_Y_OFFSET;
  public static final int SEA_LEVEL = 64 + SURFACE_Y_OFFSET;

  public static final double BERRY_HARVESTOUR_LIMIT = 70.0;
  public static final double DESERT_HARVESTOUR_LIMIT = 54.0;
  public static final double HARVESTOUR_MAX = 100.0;
  public static final double LAKE_HARVESTOUR_LIMIT = 12.0;
  public static final double MARSH_HARVESTOUR_LIMIT = 18.0;
  // Percentile intervals target the requested long-run biome area shares.
  public static final double MELON_HARVESTOUR_LIMIT = 99.0;
  public static final double PUMPKIN_HARVESTOUR_LIMIT = 90.0;
  public static final double WHEAT_HARVESTOUR_LIMIT = 40.0;

  public static final ResourceKey<Biome> BERRY_SNOWFIELD = key("berry_snowfield");
  public static final ResourceKey<Biome> CACTUS_DESERT = key("cactus_desert");
  public static final ResourceKey<Biome> GREAT_LAKE = key("great_lake");
  public static final ResourceKey<Biome> LAKE_CENTER_ISLAND = key("lake_center_island");
  public static final ResourceKey<Biome> MELON_JUNGLE = key("melon_jungle");
  public static final ResourceKey<Biome> PUMPKIN_GORGE = key("pumpkin_gorge");
  public static final ResourceKey<Biome> SACRED_MOUNTAIN = key("sacred_mountain");
  public static final ResourceKey<Biome> SUGARCANE_MARSH = key("sugarcane_marsh");
  public static final ResourceKey<Biome> WHEAT_PLAIN = key("wheat_plain");

  private static final int LAKE_CENTER_ISLAND_BASE_HEIGHT = 65 + SURFACE_Y_OFFSET;
  private static final int LAKE_CENTER_ISLAND_MAX_HEIGHT = 70 + SURFACE_Y_OFFSET;
  private static final int PUMPKIN_GORGE_BASE_HEIGHT = 256 + SURFACE_Y_OFFSET;
  private static final int PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT = 250 + SURFACE_Y_OFFSET;
  private static final int PUMPKIN_GORGE_MAX_HEIGHT = 301 + SURFACE_Y_OFFSET;
  private static final int TREE_GRID_SIZE = 32;
  private static final int WHEAT_BASE_HEIGHT = 128 + SURFACE_Y_OFFSET;
  private static final int WHEAT_INTERIOR_MAX_HEIGHT = 135 + SURFACE_Y_OFFSET;
  private static final int WHEAT_INTERIOR_MIN_HEIGHT = 123 + SURFACE_Y_OFFSET;

  private static final float TREE_RESERVATION_CHANCE = 0.15F;

  private static final double LAKE_SHORE_START = LAKE_HARVESTOUR_LIMIT - 3.0;
  private static final double MARSH_SHORE_END = LAKE_HARVESTOUR_LIMIT + 3.0;
  private static final double MELON_ENTRY_BLEND_END = PUMPKIN_HARVESTOUR_LIMIT + 1.0;
  private static final double MELON_ENTRY_BLEND_START = PUMPKIN_HARVESTOUR_LIMIT - 1.0;
  private static final double MELON_EXIT_BLEND_END = MELON_HARVESTOUR_LIMIT + 1.0;
  private static final double MELON_EXIT_BLEND_START = MELON_HARVESTOUR_LIMIT - 1.0;

  private static final double[] HARVESTOUR_LIMITS = {
      LAKE_HARVESTOUR_LIMIT, MARSH_HARVESTOUR_LIMIT, WHEAT_HARVESTOUR_LIMIT,
      DESERT_HARVESTOUR_LIMIT, BERRY_HARVESTOUR_LIMIT, PUMPKIN_HARVESTOUR_LIMIT, MELON_HARVESTOUR_LIMIT
  };

  private HarvestContinentTerrain() {}

  /** Computed once per column and reused by all carved cave blocks. */
  public static int aquiferWaterLevel(
      long seed,
      int worldX, int worldZ
  ) {
    double region = fbmPerlin(seed ^ 0xA4093822299F31D0L, worldX, worldZ, 0.006, 2);
    return 36 + SURFACE_Y_OFFSET + Math.round((float) (region * 13.0));
  }

  /** Material ownership uses thresholds, independently of the height curve. */
  public static ResourceKey<Biome> biomeForHarvestour(
      double harvestour
  ) {
    if (harvestour < LAKE_HARVESTOUR_LIMIT) return GREAT_LAKE;
    if (harvestour < MARSH_HARVESTOUR_LIMIT) return SUGARCANE_MARSH;
    if (harvestour < WHEAT_HARVESTOUR_LIMIT) return WHEAT_PLAIN;
    if (harvestour < DESERT_HARVESTOUR_LIMIT) return CACTUS_DESERT;
    if (harvestour < BERRY_HARVESTOUR_LIMIT) return BERRY_SNOWFIELD;
    if (harvestour < PUMPKIN_HARVESTOUR_LIMIT) return PUMPKIN_GORGE;
    if (harvestour < MELON_HARVESTOUR_LIMIT) return MELON_JUNGLE;
    return SACRED_MOUNTAIN;
  }

  public static double fbmPerlin(
      long seed,
      double x, double z, double scale,
      int octaves
  ) {
    double amplitude = 1.0;
    double frequency = scale;
    double sum = 0.0;
    double normalization = 0.0;
    for (int octave = 0; octave < octaves; octave++) {
      sum += amplitude * perlin2D(seed + octave * 1013L, x * frequency, z * frequency);
      normalization += amplitude;
      amplitude *= 0.5;
      frequency *= 2.0;
    }
    return normalization == 0.0 ? 0.0 : Mth.clamp(sum / normalization, -1.0, 1.0);
  }

  /** Continuous terrain function shared by chunk filling and height queries. */
  public static double heightForHarvestour(
      double harvestour,
      long seed,
      int worldX, int worldZ
  ) {
    double value = Mth.clamp(harvestour, 0.0, HARVESTOUR_MAX);
    if (value < LAKE_HARVESTOUR_LIMIT) {
      return blendHeight(value, LAKE_SHORE_START, LAKE_HARVESTOUR_LIMIT,
          lakeBedHeight(seed, worldX, worldZ), SEA_LEVEL);
    }
    if (value < MARSH_SHORE_END) {
      return blendHeight(value, LAKE_HARVESTOUR_LIMIT, MARSH_SHORE_END,
          SEA_LEVEL, HarvestRegionalTerrain.marshHeight(seed, worldX, worldZ));
    }
    for (int index = 1; index < HARVESTOUR_LIMITS.length; index++) {
      double boundary = HARVESTOUR_LIMITS[index];
      if (value <= boundary - 1.0) return profileHeight(index, seed, worldX, worldZ);
      if (value < boundary + 1.0) {
        return blendHeight(value, boundary - 1.0, boundary + 1.0,
            profileHeight(index, seed, worldX, worldZ), profileHeight(index + 1, seed, worldX, worldZ));
      }
    }
    return profileHeight(7, seed, worldX, worldZ);
  }

  /** Keep the original grass/root column in 15% of the 32-block grid cells. */
  public static boolean isTreeReservation(
      int worldX, int worldZ
  ) {
    int gridX = Math.floorDiv(worldX, TREE_GRID_SIZE);
    int gridZ = Math.floorDiv(worldZ, TREE_GRID_SIZE);
    Random random = new Random(gridX * 341873128712L + gridZ * 132897987541L);
    if (random.nextFloat() >= TREE_RESERVATION_CHANCE) return false;
    int treeX = gridX * TREE_GRID_SIZE + random.nextInt(TREE_GRID_SIZE);
    int treeZ = gridZ * TREE_GRID_SIZE + random.nextInt(TREE_GRID_SIZE);
    return worldX == treeX && worldZ == treeZ;
  }

  public static int islandSurfaceHeight(
      HarvestLakeIslands.Island island,
      double harvestour,
      long seed,
      int x, int z
  ) {
    double distance = island.distance(x, z);
    if (distance <= island.radius()) {
      double t = Mth.clamp((island.radius() - distance) / HarvestLakeIslands.SHORE_WIDTH, 0.0, 1.0);
      return (int) Math.round(lerp(fade(t), LAKE_CENTER_ISLAND_BASE_HEIGHT,
          lakeCenterIslandHeight(seed, x, z)));
    }
    double t = Mth.clamp((distance - island.radius()) / HarvestLakeIslands.SHORE_WIDTH, 0.0, 1.0);
    return (int) Math.round(lerp(fade(t), SEA_LEVEL, heightForHarvestour(harvestour, seed, x, z)));
  }

  /** Kept public because the Gold Bell Tower anchors itself to this terrain. */
  public static int lakeCenterIslandHeight(
      long seed,
      int worldX, int worldZ
  ) {
    double shape = fbmPerlin(seed ^ 0x6A09E667F3BCC909L, worldX, worldZ, 0.026, 2);
    int height = Math.round(LAKE_CENTER_ISLAND_BASE_HEIGHT + (float) (shape * 1.5));
    return Mth.clamp(height, LAKE_CENTER_ISLAND_BASE_HEIGHT, LAKE_CENTER_ISLAND_MAX_HEIGHT);
  }

  /** Same quintic weights as height blending, including both neighboring biomes. */
  public static double melonBlendWeight(
      double harvestour
  ) {
    if (harvestour <= MELON_ENTRY_BLEND_START || harvestour >= MELON_EXIT_BLEND_END) return 0.0;
    if (harvestour < MELON_ENTRY_BLEND_END) {
      return fade((harvestour - MELON_ENTRY_BLEND_START)
          / (MELON_ENTRY_BLEND_END - MELON_ENTRY_BLEND_START));
    }
    if (harvestour <= MELON_EXIT_BLEND_START) return 1.0;
    return 1.0 - fade((harvestour - MELON_EXIT_BLEND_START)
        / (MELON_EXIT_BLEND_END - MELON_EXIT_BLEND_START));
  }

  /** A continuous percentile field; interval widths target long-run area percentages. */
  public static double sampleHarvestour(
      long seed,
      int worldX, int worldZ
  ) {
    return Mth.clamp(unclampedHarvestour(seed, worldX, worldZ), 0.0, HARVESTOUR_MAX);
  }

  /** Rounded only after blending, so each profile keeps its original shape. */
  public static int surfaceHeight(
      double harvestour,
      long seed,
      int worldX, int worldZ
  ) {
    return (int) Math.round(heightForHarvestour(harvestour, seed, worldX, worldZ));
  }

  public static double unclampedHarvestour(
      long seed,
      int worldX, int worldZ
  ) {
    return HarvestourField.sample(seed, worldX, worldZ);
  }

  static boolean canCarveCave(
      int y, int surfaceY
  ) {
    return y > CAVE_MIN_Y && y < CAVE_MAX_Y && y < surfaceY - CAVE_SURFACE_COVER;
  }

  /** Sampled only at sparse grid nodes, before interpolation and height masking. */
  static double caveDensity(
      long seed,
      int worldX, int y, int worldZ
  ) {
    // Broader shapes compensate for interpolation and the removal of fine octaves.
    double winding = fbmValue3D(seed ^ 0x243F6A8885A308D3L, worldX, y, worldZ, 0.025, 2);
    double chambers = fbmValue3D(seed ^ 0x13198A2E03707344L, worldX, y, worldZ, 0.012, 1);
    return winding + chambers * 0.55;
  }

  static double caveThreshold(
      int y, int surfaceY
  ) {
    double depthBias = Mth.clamp((surfaceY - y - 12) / 92.0, 0.0, 0.16);
    // Close chambers smoothly over the final 16 blocks instead of slicing a flat roof.
    double ceilingClosure = smoothStep((y - (CAVE_MAX_Y - 16)) / 16.0) * 2.0;
    return 0.42 - depthBias + ceilingClosure;
  }

  private static ResourceKey<Biome> key(
      String path
  ) {
    return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path));
  }

  private static double lerp(
      double delta, double start, double end
  ) {
    return start + delta * (end - start);
  }

  private static double fade(
      double value
  ) {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
  }

  private static double fbmValue3D(
      long seed,
      double x, double y, double z, double scale,
      int octaves
  ) {
    double amplitude = 1.0;
    double frequency = scale;
    double sum = 0.0;
    double normalization = 0.0;
    for (int octave = 0; octave < octaves; octave++) {
      sum += amplitude * valueNoise3D(seed + octave * 2089L, x * frequency, y * frequency, z * frequency);
      normalization += amplitude;
      amplitude *= 0.5;
      frequency *= 2.0;
    }
    return sum / normalization;
  }

  private static double smoothStep(
      double value
  ) {
    double clamped = Mth.clamp(value, 0.0, 1.0);
    return clamped * clamped * (3.0 - 2.0 * clamped);
  }

  private static double perlin2D(
      long seed,
      double x, double z
  ) {
    int x0 = fastFloor(x);
    int z0 = fastFloor(z);
    double tx = x - x0;
    double tz = z - z0;
    double u = fade(tx);
    double v = fade(tz);
    double n00 = gradientDot(seed, x0, z0, tx, tz);
    double n10 = gradientDot(seed, x0 + 1, z0, tx - 1.0, tz);
    double n01 = gradientDot(seed, x0, z0 + 1, tx, tz - 1.0);
    double n11 = gradientDot(seed, x0 + 1, z0 + 1, tx - 1.0, tz - 1.0);
    return lerp(v, lerp(u, n00, n10), lerp(u, n01, n11));
  }

  private static double valueNoise3D(
      long seed,
      double x, double y, double z
  ) {
    int x0 = fastFloor(x);
    int y0 = fastFloor(y);
    int z0 = fastFloor(z);
    double tx = fade(x - x0);
    double ty = fade(y - y0);
    double tz = fade(z - z0);
    double x00 = lerp(tx, value(seed, x0, y0, z0), value(seed, x0 + 1, y0, z0));
    double x10 = lerp(tx, value(seed, x0, y0 + 1, z0), value(seed, x0 + 1, y0 + 1, z0));
    double x01 = lerp(tx, value(seed, x0, y0, z0 + 1), value(seed, x0 + 1, y0, z0 + 1));
    double x11 = lerp(tx, value(seed, x0, y0 + 1, z0 + 1), value(seed, x0 + 1, y0 + 1, z0 + 1));
    return lerp(tz, lerp(ty, x00, x10), lerp(ty, x01, x11));
  }

  private static long mix(
      long seed,
      int x, int z
  ) {
    long h = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
    h ^= h >>> 27;
    h *= 0x3C79AC492BA7B653L;
    h ^= h >>> 33;
    h *= 0x1C69B3F74AC4AE35L;
    return h ^ h >>> 27;
  }

  private static double blendHeight(
      double harvestour, double start, double end, double first, double second
  ) {
    double t = Mth.clamp((harvestour - start) / (end - start), 0.0, 1.0);
    return lerp(fade(t), first, second);
  }

  /** The deep-lake profile before its harvestour-driven shore transition. */
  private static double lakeBedHeight(
      long seed,
      int worldX, int worldZ
  ) {
    double shape = fbmPerlin(seed ^ 0x67E6096A85AE67BBL, worldX, worldZ, 0.012, 3);
    return Mth.clamp(38.0 + shape * 9.0, 26.0, 52.0) + SURFACE_Y_OFFSET;
  }

  private static double profileHeight(
      int index,
      long seed,
      int x, int z
  ) {
    return switch (index) {
      case 1 -> HarvestRegionalTerrain.marshHeight(seed, x, z);
      case 2 -> wheatPlainHeight(seed, x, z);
      case 3 -> HarvestRegionalTerrain.desertHeight(seed, x, z);
      case 4 -> HarvestRegionalTerrain.berrySnowfieldHeight(seed, x, z);
      case 5 -> pumpkinGorgeHeight(seed, x, z);
      case 6 -> melonJungleHeight(seed, x, z);
      case 7 -> HarvestRegionalTerrain.sacredMountainHeight(seed, x, z);
      default -> lakeBedHeight(seed, x, z);
    };
  }

  private static int fastFloor(
      double value
  ) {
    int integer = (int) value;
    return value < integer ? integer - 1 : integer;
  }

  private static double gradientDot(
      long seed,
      int x, int z,
      double dx, double dz
  ) {
    return switch ((int) (mix(seed, x, z) & 7L)) {
      case 0 -> dx;
      case 1 -> -dx;
      case 2 -> dz;
      case 3 -> -dz;
      case 4 -> (dx + dz) * 0.7071067811865476;
      case 5 -> (-dx + dz) * 0.7071067811865476;
      case 6 -> (dx - dz) * 0.7071067811865476;
      default -> (-dx - dz) * 0.7071067811865476;
    };
  }

  private static double wheatPlainHeight(
      long seed,
      int x, int z
  ) {
    double large = fbmPerlin(seed ^ 0x1A2B3C4D5E6F7890L, x, z, 0.0026, 3) * 5.2;
    double medium = fbmPerlin(seed ^ 0x9876543210FEDCBAL, x, z, 0.0100, 2) * 3.4;
    double micro = fbmPerlin(seed ^ 0xABCDEF0123456789L, x, z, 0.0340, 2) * 1.35;
    return Mth.clamp(WHEAT_BASE_HEIGHT + large + medium + micro,
        WHEAT_INTERIOR_MIN_HEIGHT, WHEAT_INTERIOR_MAX_HEIGHT);
  }

  private static double pumpkinGorgeHeight(
      long seed,
      int x, int z
  ) {
    double macro = Math.sin(x * 0.035) + Math.cos(z * 0.032);
    double ridges = Math.abs(Math.sin((x + z) * 0.08)) * 24.0;
    double spikes = Math.abs(Math.sin(x * 0.19) * Math.cos(z * 0.17)) * 14.0;
    double detail = (1.0 - Math.abs(fbmPerlin(seed ^ 0x3C6EF372FE94F82BL, x, z, 0.040, 4)));
    detail = detail * detail * 22.0 - 10.0;
    detail += fbmPerlin(seed ^ 0x510E527FADE682D1L, x, z, 0.085, 3) * 8.0;
    double height = PUMPKIN_GORGE_BASE_HEIGHT + macro * 6.0 + ridges + spikes + detail;
    return Mth.clamp(height, PUMPKIN_GORGE_INTERIOR_MIN_HEIGHT, PUMPKIN_GORGE_MAX_HEIGHT);
  }

  private static double melonJungleHeight(
      long seed,
      int x, int z
  ) {
    return HarvestMelonTerrain.height(seed, x, z) + SURFACE_Y_OFFSET;
  }

  private static double value(
      long seed,
      int x, int y, int z
  ) {
    long h = mix(mix(seed, x, z), y, x ^ z);
    return ((h >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
  }

}
