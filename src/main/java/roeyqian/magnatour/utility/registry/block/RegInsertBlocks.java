/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.block;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.insert.ChunkTntBlock;
import roeyqian.magnatour.block.insert.CropOfAllThings;
import roeyqian.magnatour.block.insert.EverWaterFarmland;
import roeyqian.magnatour.block.insert.EverWaterGrassBlock;
import roeyqian.magnatour.block.insert.GoldenLeavesBlock;
import roeyqian.magnatour.block.insert.LogisticsFiber;
import roeyqian.magnatour.block.insert.UniverseLeavesBlock;
import roeyqian.magnatour.block.insert.SupremeBlock;
import roeyqian.magnatour.block.insert.SupremeFodderBlock;
import roeyqian.magnatour.block.insert.SupremeGemBlock;
import roeyqian.magnatour.block.insert.SupremePumpkinHead;
import roeyqian.magnatour.block.insert.UniverseBlock;
import roeyqian.magnatour.block.insert.portal.HarvestContinentPortal;
import roeyqian.magnatour.block.insert.portal.OreContinentPortal;
import roeyqian.magnatour.block.SummonStructureHelper;
import roeyqian.magnatour.level.tree.SaplingGenerators;

/*
 * Supreme Group: Plant, Earth, Stone, Portal, Entity
 * Universe Group: Plant, Stone, Entity
 */
public final class RegInsertBlocks {

  private static final String universe = "universe";
  private static final String supreme = "supreme";

  // Supreme Group: Plant
  public static final Block GOLDEN_SAPLING = BlockRegHelper.registerSapling(
      "golden_sapling", supreme, setting -> new SaplingBlock(SaplingGenerators.GOLDEN, setting),
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_LOG = BlockRegHelper.registerWood(
      "golden_log", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_GOLDEN_LOG = BlockRegHelper.registerWood(
      "stripped_golden_log", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_WOOD = BlockRegHelper.registerWood(
      "golden_wood", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_GOLDEN_WOOD = BlockRegHelper.registerWood(
      "stripped_golden_wood", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_PLANKS = BlockRegHelper.registerWood(
      "golden_planks", supreme, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_LEAVES = BlockRegHelper.registerLeaves(
      "golden_leaves", supreme,
      GoldenLeavesBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block CROP_OF_ALL_THINGS = BlockRegHelper.registerCrop(
      "crop_of_all_things", supreme,
      CropOfAllThings::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_PUMPKIN_HEAD = BlockRegHelper.registerWood(
      "supreme_pumpkin_head", supreme, SupremePumpkinHead::new,
      BlockBehaviour.Properties.of()
  );

  // Supreme Group: Earth
  private static final ResourceKey<Block> EVER_WATER_SOIL_KEY = ResourceKey.create(
      Registries.BLOCK,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "ever_water_soil")
  );
  public static final Block EVER_WATER_GRASS_BLOCK = BlockRegHelper.registerGrass(
      "ever_water_grass_block", supreme,
      properties -> new EverWaterGrassBlock(properties, EVER_WATER_SOIL_KEY),
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );
  public static final Block GOLDEN_GRASS_BLOCK = BlockRegHelper.registerGrass(
      "golden_grass_block", supreme, Block::new,
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );
  public static final Block EVER_WATER_SOIL = BlockRegHelper.registerGravel(
      "ever_water_soil", supreme, Block::new,
      BlockBehaviour.Properties.of().strength(0.6F)
  );
  public static final Block EVER_WATER_FARMLAND = BlockRegHelper.registerGravel(
      "ever_water_farmland", supreme,
      EverWaterFarmland::new,
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );
  public static final Block SUPREME_FODDER_BLOCK = BlockRegHelper.registerGrass(
      "supreme_fodder_block", supreme, SupremeFodderBlock::new,
      BlockBehaviour.Properties.of()
  );

  // Supreme Group: Stone
  public static final Block SUPREME_GEM_BLOCK = BlockRegHelper.registerBase(
      "supreme_gem_block", supreme, SupremeGemBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_BLOCK = BlockRegHelper.registerBase(
      "supreme_block", supreme, SupremeBlock::new,
      BlockBehaviour.Properties.of()
  );
  private static final ResourceKey<Block> PRIMED_CHUNK_TNT_VISUAL_KEY = ResourceKey.create(
      Registries.BLOCK,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "primed_chunk_tnt_visual")
  );
  public static final Block PRIMED_CHUNK_TNT_VISUAL = Blocks.register(
      PRIMED_CHUNK_TNT_VISUAL_KEY,
      BlockBehaviour.Properties.of()
          .requiresCorrectToolForDrops()
          .instabreak()
          .noLootTable()
          .noOcclusion()
  );
  public static final Block CHUNK_TNT = BlockRegHelper.registerBase(
      "chunk_tnt", supreme, ChunkTntBlock::new,
      BlockBehaviour.Properties.of().ignitedByLava()
  );
  public static final Block LOGISTICS_FIBER = BlockRegHelper.registerBase(
      "logistics_fiber", supreme,
      LogisticsFiber::new,
      BlockBehaviour.Properties.of()
          .noOcclusion()
          .isRedstoneConductor(Blocks::never)
  );

  // Supreme Group: Portal
  public static final Block ORE_CONTINENT_PORTAL = BlockRegHelper.registerPortal(
      "ore_continent_portal",
      OreContinentPortal::new
  );
  public static final Block HARVEST_CONTINENT_PORTAL = BlockRegHelper.registerPortal(
      "harvest_continent_portal",
      HarvestContinentPortal::new
  );

  // Universe Group: Plant
  public static final Block UNIVERSE_SAPLING = BlockRegHelper.registerSapling(
      "universe_sapling", universe, setting -> new SaplingBlock(SaplingGenerators.UNIVERSE, setting),
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LOG = BlockRegHelper.registerWood(
      "universe_log", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_UNIVERSE_LOG = BlockRegHelper.registerWood(
      "stripped_universe_log", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_WOOD = BlockRegHelper.registerWood(
      "universe_wood", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_UNIVERSE_WOOD = BlockRegHelper.registerWood(
      "stripped_universe_wood", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_PLANKS = BlockRegHelper.registerWood(
      "universe_planks", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LEAVES = BlockRegHelper.registerLeaves(
      "universe_leaves", universe,
      UniverseLeavesBlock::new, BlockBehaviour.Properties.of()
  );

  // Universe Group: Stone
  public static final Block UNIVERSE_LIGHT_BLOCK = BlockRegHelper.registerBase(
      "universe_light_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_DARK_BLOCK = BlockRegHelper.registerBase(
      "universe_dark_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LIGHT_AIR = BlockRegHelper.registerBase(
      "universe_light_air", universe, TransparentBlock::new,
      BlockBehaviour.Properties.of().noOcclusion()
  );
  public static final Block UNIVERSE_DARK_AIR = BlockRegHelper.registerBase(
      "universe_dark_air", universe, TransparentBlock::new,
      BlockBehaviour.Properties.of().noOcclusion()
  );
  public static final Block UNIVERSE_PRIMARY_BLOCK = BlockRegHelper.registerBase(
      "universe_primary_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );

  // Universe Group: Entity
  public static final Block UNIVERSE_BLOCK = BlockRegHelper.registerBase(
      "universe_block", universe,
      UniverseBlock::new, BlockBehaviour.Properties.of()
  );

  private RegInsertBlocks() {}

  public static void init() {
    OreContinentPortal.registerTickEvent();
    HarvestContinentPortal.registerTickEvent();

    SummonStructureHelper.registerTickEvent();

    Magnatour.LOGGER.info("[Server] Initializing 'RegInsertBlocks'");
  }

}
