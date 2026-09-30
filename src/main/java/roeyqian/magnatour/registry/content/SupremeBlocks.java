/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Java Standard
import java.util.Optional;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.supreme.ChunkTntBlock;
import roeyqian.magnatour.block.supreme.CropOfAllThings;
import roeyqian.magnatour.block.supreme.EverWaterFarmland;
import roeyqian.magnatour.block.supreme.EverWaterGrassBlock;
import roeyqian.magnatour.block.supreme.GoldenLeavesBlock;
import roeyqian.magnatour.block.supreme.HarvestContinentPortal;
import roeyqian.magnatour.block.supreme.ItemHub;
import roeyqian.magnatour.block.supreme.LogisticsFiber;
import roeyqian.magnatour.block.supreme.OreContinentPortal;
import roeyqian.magnatour.block.supreme.RedstoneTrigger;
import roeyqian.magnatour.block.supreme.SupremeBlock;
import roeyqian.magnatour.block.supreme.SupremeChest;
import roeyqian.magnatour.block.supreme.HarvestBlock;
import roeyqian.magnatour.block.supreme.SupremeFurnace;
import roeyqian.magnatour.block.supreme.OreBlock;
import roeyqian.magnatour.block.supreme.SupremePumpkinHead;
import roeyqian.magnatour.block.supreme.SupremeReserver;
import roeyqian.magnatour.block.supreme.SupremeWorktable;
import roeyqian.magnatour.block.SummonStructureHelper;
import roeyqian.magnatour.levelgen.tree.SaplingGenerators;
import roeyqian.magnatour.registry.BlockRegHelper;

/*
 * Supreme Group: Active Blocks, Insert Blocks
 * Categories: Portal, Plant, Earth, Stone, Entity, Stateless, Stateful
 */
public final class SupremeBlocks {

  private static final String supreme = "supreme";

  // Portal Blocks
  public static final Block HARVEST_CONTINENT_PORTAL = BlockRegHelper.registerPortal(
      "harvest_continent_portal",
      HarvestContinentPortal::new
  );
  public static final Block ORE_CONTINENT_PORTAL = BlockRegHelper.registerPortal(
      "ore_continent_portal",
      OreContinentPortal::new
  );

  // Active Blocks - Stateless
  public static final Block ITEM_HUB = BlockRegHelper.registerBase(
      "item_hub", supreme,
      ItemHub::new, BlockBehaviour.Properties.of().noOcclusion()
  );
  public static final Block REDSTONE_TRIGGER = BlockRegHelper.registerBase(
      "redstone_trigger", supreme,
      RedstoneTrigger::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_CHEST = BlockRegHelper.registerBase(
      "supreme_chest", supreme,
      SupremeChest::new, BlockBehaviour.Properties.of()
  );

  // Active Blocks - Stateful
  public static final Block SUPREME_FURNACE = BlockRegHelper.registerBase(
      "supreme_furnace", supreme,
      SupremeFurnace::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_RESERVER = BlockRegHelper.registerBase(
      "supreme_reserver", supreme,
      SupremeReserver::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_WORKTABLE = BlockRegHelper.registerBase(
      "supreme_worktable", supreme,
      SupremeWorktable::new, BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Entity
  public static final Block CHUNK_TNT = BlockRegHelper.registerBase(
      "chunk_tnt", supreme, ChunkTntBlock::new,
      BlockBehaviour.Properties.of().ignitedByLava()
  );

  // Insert Blocks - Plant
  public static final Block CROP_OF_ALL_THINGS = BlockRegHelper.registerCrop(
      "crop_of_all_things", supreme,
      CropOfAllThings::new, BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_LEAVES = BlockRegHelper.registerLeaves(
      "golden_leaves", supreme,
      GoldenLeavesBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_LOG = BlockRegHelper.registerWood(
      "golden_log", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_PLANKS = BlockRegHelper.registerWood(
      "golden_planks", supreme, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_STAIRS = BlockRegHelper.registerWood(
      "golden_stairs", supreme,
      properties -> new StairBlock(GOLDEN_PLANKS.defaultBlockState(), properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
  );
  public static final Block GOLDEN_SLAB = BlockRegHelper.registerWood(
      "golden_slab", supreme, SlabBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
  );
  public static final Block GOLDEN_FENCE = BlockRegHelper.registerWood(
      "golden_fence", supreme, FenceBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
  );
  public static final Block GOLDEN_FENCE_GATE = BlockRegHelper.registerWood(
      "golden_fence_gate", supreme,
      properties -> new FenceGateBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)
  );
  public static final Block GOLDEN_DOOR = BlockRegHelper.registerWoodDoor(
      "golden_door", supreme,
      properties -> new DoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
  );
  public static final Block GOLDEN_TRAPDOOR = BlockRegHelper.registerWood(
      "golden_trapdoor", supreme,
      properties -> new TrapDoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)
  );
  public static final Block GOLDEN_PRESSURE_PLATE = BlockRegHelper.registerWood(
      "golden_pressure_plate", supreme,
      properties -> new PressurePlateBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)
  );
  public static final Block GOLDEN_BUTTON = BlockRegHelper.registerWood(
      "golden_button", supreme,
      properties -> new ButtonBlock(BlockSetType.OAK, 30, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)
  );
  public static final Block GOLDEN_SHELF = BlockRegHelper.registerWood(
      "golden_shelf", supreme, ShelfBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SHELF)
  );
  public static final Block GOLDEN_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "golden_sign", supreme, properties -> new StandingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)
  );
  public static final Block GOLDEN_WALL_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "golden_wall_sign", supreme, properties -> new WallSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)
          .overrideLootTable(signLootTable("golden_sign"))
  );
  public static final Item GOLDEN_SIGN_ITEM = BlockRegHelper.registerWoodSignItem(
      "golden_sign", supreme, GOLDEN_SIGN, GOLDEN_WALL_SIGN, false
  );
  public static final Block GOLDEN_HANGING_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "golden_hanging_sign", supreme, properties -> new CeilingHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)
  );
  public static final Block GOLDEN_WALL_HANGING_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "golden_wall_hanging_sign", supreme, properties -> new WallHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)
          .overrideLootTable(signLootTable("golden_hanging_sign"))
  );
  public static final Item GOLDEN_HANGING_SIGN_ITEM = BlockRegHelper.registerWoodSignItem(
      "golden_hanging_sign", supreme, GOLDEN_HANGING_SIGN, GOLDEN_WALL_HANGING_SIGN, true
  );
  public static final Block GOLDEN_SAPLING = BlockRegHelper.registerSapling(
      "golden_sapling", supreme, setting -> new SaplingBlock(SaplingGenerators.GOLDEN, setting),
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_WOOD = BlockRegHelper.registerWood(
      "golden_wood", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_GOLDEN_LOG = BlockRegHelper.registerWood(
      "stripped_golden_log", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_GOLDEN_WOOD = BlockRegHelper.registerWood(
      "stripped_golden_wood", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Earth
  public static final Block EVER_WATER_FARMLAND = BlockRegHelper.registerGravel(
      "ever_water_farmland", supreme,
      EverWaterFarmland::new,
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );
  public static final Block EVER_WATER_SOIL = BlockRegHelper.registerGravel(
      "ever_water_soil", supreme, Block::new,
      BlockBehaviour.Properties.of().strength(0.6F)
  );
  public static final Block GOLDEN_GRASS_BLOCK = BlockRegHelper.registerGrass(
      "golden_grass_block", supreme, Block::new,
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );

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

  // Insert Blocks - Stone
  public static final Block LOGISTICS_FIBER = BlockRegHelper.registerBase(
      "logistics_fiber", supreme,
      LogisticsFiber::new,
      BlockBehaviour.Properties.of()
          .noOcclusion()
          .isRedstoneConductor(Blocks::never)
  );
  public static final Block SUPREME_BLOCK = BlockRegHelper.registerBase(
      "supreme_block", supreme, SupremeBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block HARVEST_BLOCK = BlockRegHelper.registerGrass(
      "harvest_block", supreme, HarvestBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block ORE_BLOCK = BlockRegHelper.registerBase(
      "ore_block", supreme, OreBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_PUMPKIN_HEAD = BlockRegHelper.registerWood(
      "supreme_pumpkin_head", supreme, SupremePumpkinHead::new,
      BlockBehaviour.Properties.of()
  );

  // Visual Block for TNT
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

  private static Optional<ResourceKey<LootTable>> signLootTable(String name) {
    return Optional.of(ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "blocks/" + name)
    ));
  }

  private SupremeBlocks() {}

  public static void init() {
    OreContinentPortal.registerTickEvent();
    HarvestContinentPortal.registerTickEvent();
    SummonStructureHelper.registerTickEvent();

    Magnatour.LOGGER.info("[Server] Initializing 'SupremeBlocks'");
  }

}
