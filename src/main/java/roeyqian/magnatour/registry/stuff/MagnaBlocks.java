/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.stuff;

// Java Standard
import java.util.Optional;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.LootTable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.SummonStructureHelper;
import roeyqian.magnatour.block.supreme.ChunkTntBlock;
import roeyqian.magnatour.block.supreme.CropOfAllThings;
import roeyqian.magnatour.block.supreme.EverWaterFarmland;
import roeyqian.magnatour.block.supreme.EverWaterGrassBlock;
import roeyqian.magnatour.block.supreme.GoldenLeavesBlock;
import roeyqian.magnatour.block.supreme.HarvestBlock;
import roeyqian.magnatour.block.supreme.HarvestContinentPortal;
import roeyqian.magnatour.block.supreme.ItemHub;
import roeyqian.magnatour.block.supreme.LogisticsFiber;
import roeyqian.magnatour.block.supreme.OreBlock;
import roeyqian.magnatour.block.supreme.OreContinentPortal;
import roeyqian.magnatour.block.supreme.RedstoneTrigger;
import roeyqian.magnatour.block.supreme.SupremeBlock;
import roeyqian.magnatour.block.supreme.SupremeChest;
import roeyqian.magnatour.block.supreme.SupremeCraftable;
import roeyqian.magnatour.block.supreme.SupremeFurnace;
import roeyqian.magnatour.block.supreme.SupremePumpkinHead;
import roeyqian.magnatour.block.supreme.SupremeReserver;
import roeyqian.magnatour.block.universe.UniverseBlock;
import roeyqian.magnatour.block.universe.UniverseDot;
import roeyqian.magnatour.block.universe.UniverseForge;
import roeyqian.magnatour.block.universe.UniverseLeavesBlock;
import roeyqian.magnatour.block.universe.UniverseLibrary;
import roeyqian.magnatour.block.universe.UniverseMetaPortal;
import roeyqian.magnatour.block.universe.UniversePool;
import roeyqian.magnatour.block.universe.UniverseRefinery;
import roeyqian.magnatour.levelgen.tree.SaplingGenerators;
import roeyqian.magnatour.registry.StuffHelper;

/*
 * Supreme and Universe Groups: Active Blocks, Insert Blocks
 * Categories: Portal, Plant, Earth, Stone, Entity, Stateless, Stateful
 */
public final class MagnaBlocks {

  // Supreme Group
  private static final String supreme = "supreme";

  // Portal Blocks
  public static final Block HARVEST_CONTINENT_PORTAL = StuffHelper.registerPortal(
      "harvest_continent_portal",
      HarvestContinentPortal::new
  );
  public static final Block ORE_CONTINENT_PORTAL = StuffHelper.registerPortal(
      "ore_continent_portal",
      OreContinentPortal::new
  );

  // Active Blocks - Stateless
  public static final Block ITEM_HUB = StuffHelper.registerBase(
      "item_hub", supreme,
      ItemHub::new, BlockBehaviour.Properties.of().noOcclusion()
  );
  public static final Block REDSTONE_TRIGGER = StuffHelper.registerBase(
      "redstone_trigger", supreme,
      RedstoneTrigger::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_CHEST = StuffHelper.registerBase(
      "supreme_chest", supreme,
      SupremeChest::new, BlockBehaviour.Properties.of()
  );

  // Active Blocks - Stateful
  public static final Block SUPREME_FURNACE = StuffHelper.registerBase(
      "supreme_furnace", supreme,
      SupremeFurnace::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_RESERVER = StuffHelper.registerBase(
      "supreme_reserver", supreme,
      SupremeReserver::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_CRAFTABLE = StuffHelper.registerBase(
      "supreme_craftable", supreme,
      SupremeCraftable::new, BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Entity
  public static final Block CHUNK_TNT = StuffHelper.registerBase(
      "chunk_tnt", supreme, ChunkTntBlock::new,
      BlockBehaviour.Properties.of().ignitedByLava()
  );

  // Insert Blocks - Plant
  public static final Block CROP_OF_ALL_THINGS = StuffHelper.registerCrop(
      "crop_of_all_things", supreme,
      CropOfAllThings::new, BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_LEAVES = StuffHelper.registerLeaves(
      "golden_leaves", supreme,
      GoldenLeavesBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_LOG = StuffHelper.registerWood(
      "golden_log", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_PLANKS = StuffHelper.registerWood(
      "golden_planks", supreme, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_STAIRS = StuffHelper.registerWood(
      "golden_stairs", supreme,
      properties -> new StairBlock(GOLDEN_PLANKS.defaultBlockState(), properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
  );
  public static final Block GOLDEN_SLAB = StuffHelper.registerWood(
      "golden_slab", supreme, SlabBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
  );
  public static final Block GOLDEN_FENCE = StuffHelper.registerWood(
      "golden_fence", supreme, FenceBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
  );
  public static final Block GOLDEN_FENCE_GATE = StuffHelper.registerWood(
      "golden_fence_gate", supreme,
      properties -> new FenceGateBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)
  );
  public static final Block GOLDEN_DOOR = StuffHelper.registerWoodDoor(
      "golden_door", supreme,
      properties -> new DoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
  );
  public static final Block GOLDEN_TRAPDOOR = StuffHelper.registerWood(
      "golden_trapdoor", supreme,
      properties -> new TrapDoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)
  );
  public static final Block GOLDEN_PRESSURE_PLATE = StuffHelper.registerWood(
      "golden_pressure_plate", supreme,
      properties -> new PressurePlateBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)
  );
  public static final Block GOLDEN_BUTTON = StuffHelper.registerWood(
      "golden_button", supreme,
      properties -> new ButtonBlock(BlockSetType.OAK, 30, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)
  );
  public static final Block GOLDEN_SHELF = StuffHelper.registerWood(
      "golden_shelf", supreme, ShelfBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SHELF)
  );
  public static final Block GOLDEN_SIGN = StuffHelper.registerWoodBlockOnly(
      "golden_sign", supreme, properties -> new StandingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)
  );
  public static final Block GOLDEN_WALL_SIGN = StuffHelper.registerWoodBlockOnly(
      "golden_wall_sign", supreme, properties -> new WallSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)
          .overrideLootTable(signLootTable("golden_sign"))
  );
  public static final Item GOLDEN_SIGN_ITEM = StuffHelper.registerWoodSignItem(
      "golden_sign", supreme, GOLDEN_SIGN, GOLDEN_WALL_SIGN, false
  );
  public static final Block GOLDEN_HANGING_SIGN = StuffHelper.registerWoodBlockOnly(
      "golden_hanging_sign", supreme, properties -> new CeilingHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)
  );
  public static final Block GOLDEN_WALL_HANGING_SIGN = StuffHelper.registerWoodBlockOnly(
      "golden_wall_hanging_sign", supreme, properties -> new WallHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)
          .overrideLootTable(signLootTable("golden_hanging_sign"))
  );
  public static final Item GOLDEN_HANGING_SIGN_ITEM = StuffHelper.registerWoodSignItem(
      "golden_hanging_sign", supreme, GOLDEN_HANGING_SIGN, GOLDEN_WALL_HANGING_SIGN, true
  );
  public static final Block GOLDEN_SAPLING = StuffHelper.registerSapling(
      "golden_sapling", supreme, setting -> new SaplingBlock(SaplingGenerators.GOLDEN, setting),
      BlockBehaviour.Properties.of()
  );
  public static final Block GOLDEN_WOOD = StuffHelper.registerWood(
      "golden_wood", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_GOLDEN_LOG = StuffHelper.registerWood(
      "stripped_golden_log", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_GOLDEN_WOOD = StuffHelper.registerWood(
      "stripped_golden_wood", supreme, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Earth
  public static final Block EVER_WATER_FARMLAND = StuffHelper.registerGravel(
      "ever_water_farmland", supreme,
      EverWaterFarmland::new,
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );
  public static final Block EVER_WATER_SOIL = StuffHelper.registerGravel(
      "ever_water_soil", supreme, Block::new,
      BlockBehaviour.Properties.of().strength(0.6F)
  );
  public static final Block GOLDEN_GRASS_BLOCK = StuffHelper.registerGrass(
      "golden_grass_block", supreme, Block::new,
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );

  private static final ResourceKey<Block> EVER_WATER_SOIL_KEY = ResourceKey.create(
      Registries.BLOCK,
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "ever_water_soil")
  );

  public static final Block EVER_WATER_GRASS_BLOCK = StuffHelper.registerGrass(
      "ever_water_grass_block", supreme,
      properties -> new EverWaterGrassBlock(properties, EVER_WATER_SOIL_KEY),
      BlockBehaviour.Properties.of()
          .strength(0.6F)
          .isValidSpawn((state, level, pos, type) -> type.getCategory().isFriendly())
  );

  // Insert Blocks - Stone
  public static final Block LOGISTICS_FIBER = StuffHelper.registerBase(
      "logistics_fiber", supreme,
      LogisticsFiber::new,
      BlockBehaviour.Properties.of()
          .noOcclusion()
          .isRedstoneConductor(Blocks::never)
  );
  public static final Block SUPREME_BLOCK = StuffHelper.registerBase(
      "supreme_block", supreme, SupremeBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block HARVEST_BLOCK = StuffHelper.registerGrass(
      "harvest_block", supreme, HarvestBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block ORE_BLOCK = StuffHelper.registerBase(
      "ore_block", supreme, OreBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_PUMPKIN_HEAD = StuffHelper.registerWood(
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

  // Universe Group
  private static final String universe = "universe";

  // Active Blocks - Stateless
  public static final Block UNIVERSE_LIBRARY = StuffHelper.registerPortableContainer(
      "universe_library", universe,
      UniverseLibrary::new, BlockBehaviour.Properties.of(), BlockItem::new
  );
  public static final Block UNIVERSE_FORGE = StuffHelper.registerBase(
      "universe_forge", universe,
      UniverseForge::new, BlockBehaviour.Properties.of()
  );

  // Active Blocks - Stateful
  public static final Block UNIVERSE_REFINERY = StuffHelper.registerBase(
      "universe_refinery", universe,
      UniverseRefinery::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_DOT = StuffHelper.registerBase(
      "universe_dot", universe,
      UniverseDot::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_POOL = StuffHelper.registerBase(
      "universe_pool", universe,
      UniversePool::new, BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Plant
  public static final Block UNIVERSE_LEAVES = StuffHelper.registerLeaves(
      "universe_leaves", universe,
      UniverseLeavesBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LOG = StuffHelper.registerWood(
      "universe_log", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_PLANKS = StuffHelper.registerWood(
      "universe_planks", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_STAIRS = StuffHelper.registerWood(
      "universe_stairs", universe,
      properties -> new StairBlock(UNIVERSE_PLANKS.defaultBlockState(), properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
  );
  public static final Block UNIVERSE_SLAB = StuffHelper.registerWood(
      "universe_slab", universe, SlabBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
  );
  public static final Block UNIVERSE_FENCE = StuffHelper.registerWood(
      "universe_fence", universe, FenceBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
  );
  public static final Block UNIVERSE_FENCE_GATE = StuffHelper.registerWood(
      "universe_fence_gate", universe,
      properties -> new FenceGateBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)
  );
  public static final Block UNIVERSE_DOOR = StuffHelper.registerWoodDoor(
      "universe_door", universe,
      properties -> new DoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
  );
  public static final Block UNIVERSE_TRAPDOOR = StuffHelper.registerWood(
      "universe_trapdoor", universe,
      properties -> new TrapDoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)
  );
  public static final Block UNIVERSE_PRESSURE_PLATE = StuffHelper.registerWood(
      "universe_pressure_plate", universe,
      properties -> new PressurePlateBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)
  );
  public static final Block UNIVERSE_BUTTON = StuffHelper.registerWood(
      "universe_button", universe,
      properties -> new ButtonBlock(BlockSetType.OAK, 30, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)
  );
  public static final Block UNIVERSE_SHELF = StuffHelper.registerWood(
      "universe_shelf", universe, ShelfBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SHELF)
  );
  public static final Block UNIVERSE_SIGN = StuffHelper.registerWoodBlockOnly(
      "universe_sign", universe, properties -> new StandingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)
  );
  public static final Block UNIVERSE_WALL_SIGN = StuffHelper.registerWoodBlockOnly(
      "universe_wall_sign", universe, properties -> new WallSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)
          .overrideLootTable(signLootTable("universe_sign"))
  );
  public static final Item UNIVERSE_SIGN_ITEM = StuffHelper.registerWoodSignItem(
      "universe_sign", universe, UNIVERSE_SIGN, UNIVERSE_WALL_SIGN, false
  );
  public static final Block UNIVERSE_HANGING_SIGN = StuffHelper.registerWoodBlockOnly(
      "universe_hanging_sign", universe, properties -> new CeilingHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)
  );
  public static final Block UNIVERSE_WALL_HANGING_SIGN = StuffHelper.registerWoodBlockOnly(
      "universe_wall_hanging_sign", universe, properties -> new WallHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)
          .overrideLootTable(signLootTable("universe_hanging_sign"))
  );
  public static final Item UNIVERSE_HANGING_SIGN_ITEM = StuffHelper.registerWoodSignItem(
      "universe_hanging_sign", universe, UNIVERSE_HANGING_SIGN, UNIVERSE_WALL_HANGING_SIGN, true
  );
  public static final Block UNIVERSE_SAPLING = StuffHelper.registerSapling(
      "universe_sapling", universe, setting -> new SaplingBlock(SaplingGenerators.UNIVERSE, setting),
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_WOOD = StuffHelper.registerWood(
      "universe_wood", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_UNIVERSE_LOG = StuffHelper.registerWood(
      "stripped_universe_log", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_UNIVERSE_WOOD = StuffHelper.registerWood(
      "stripped_universe_wood", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Stone
  public static final Block UNIVERSE_DARK_BLOCK = StuffHelper.registerBase(
      "universe_dark_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LIGHT_BLOCK = StuffHelper.registerBase(
      "universe_light_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_PRIMARY_BLOCK = StuffHelper.registerBase(
      "universe_primary_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Entity
  public static final Block UNIVERSE_BLOCK = StuffHelper.registerBase(
      "universe_block", universe,
      UniverseBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_DARK_AIR = StuffHelper.registerBase(
      "universe_dark_air", universe, TransparentBlock::new,
      BlockBehaviour.Properties.of().noOcclusion()
  );
  public static final Block UNIVERSE_LIGHT_AIR = StuffHelper.registerBase(
      "universe_light_air", universe, TransparentBlock::new,
      BlockBehaviour.Properties.of().noOcclusion()
  );

  // Portal Blocks
  public static final Block UNIVERSE_META_PORTAL = StuffHelper.registerPortal(
      "universe_meta_portal",
      roeyqian.magnatour.block.universe.UniverseMetaPortal::new
  );

  private static Optional<ResourceKey<LootTable>> signLootTable(String name) {
    return Optional.of(ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "blocks/" + name)
    ));
  }

  private MagnaBlocks() {}

  public static void init() {
    OreContinentPortal.registerTickEvent();
    HarvestContinentPortal.registerTickEvent();
    SummonStructureHelper.registerTickEvent();
    UniverseMetaPortal.registerTickEvent();

    Magnatour.LOGGER.info("[Server] Initializing 'MagnaBlocks'");
  }

}
