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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.universe.UniverseBlock;
import roeyqian.magnatour.block.universe.UniverseLeavesBlock;
import roeyqian.magnatour.block.universe.UniverseLibrary;
import roeyqian.magnatour.block.universe.UniverseMetaPortal;
import roeyqian.magnatour.block.universe.UniverseRefinery;
import roeyqian.magnatour.block.universe.UniverseTeleportPoint;
import roeyqian.magnatour.block.universe.UniverseVoidPool;
import roeyqian.magnatour.block.universe.UniverseWorkstation;
import roeyqian.magnatour.levelgen.tree.SaplingGenerators;
import roeyqian.magnatour.registry.BlockRegHelper;

/*
 * Universe Group: Active Blocks, Insert Blocks
 * Categories: Plant, Stone, Entity, Stateless, Stateful
 */
public final class UniverseBlocks {

  private static final String universe = "universe";

  // Active Blocks - Stateless
  public static final Block UNIVERSE_LIBRARY = BlockRegHelper.registerBase(
      "universe_library", universe,
      UniverseLibrary::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_WORKSTATION = BlockRegHelper.registerBase(
      "universe_workstation", universe,
      UniverseWorkstation::new, BlockBehaviour.Properties.of()
  );

  // Active Blocks - Stateful
  public static final Block UNIVERSE_REFINERY = BlockRegHelper.registerBase(
      "universe_refinery", universe,
      UniverseRefinery::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_TELEPORT_POINT = BlockRegHelper.registerBase(
      "universe_teleport_point", universe,
      UniverseTeleportPoint::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_VOID_POOL = BlockRegHelper.registerBase(
      "universe_void_pool", universe,
      UniverseVoidPool::new, BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Plant
  public static final Block UNIVERSE_LEAVES = BlockRegHelper.registerLeaves(
      "universe_leaves", universe,
      UniverseLeavesBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LOG = BlockRegHelper.registerWood(
      "universe_log", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_PLANKS = BlockRegHelper.registerWood(
      "universe_planks", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_STAIRS = BlockRegHelper.registerWood(
      "universe_stairs", universe,
      properties -> new StairBlock(UNIVERSE_PLANKS.defaultBlockState(), properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)
  );
  public static final Block UNIVERSE_SLAB = BlockRegHelper.registerWood(
      "universe_slab", universe, SlabBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
  );
  public static final Block UNIVERSE_FENCE = BlockRegHelper.registerWood(
      "universe_fence", universe, FenceBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
  );
  public static final Block UNIVERSE_FENCE_GATE = BlockRegHelper.registerWood(
      "universe_fence_gate", universe,
      properties -> new FenceGateBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)
  );
  public static final Block UNIVERSE_DOOR = BlockRegHelper.registerWoodDoor(
      "universe_door", universe,
      properties -> new DoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)
  );
  public static final Block UNIVERSE_TRAPDOOR = BlockRegHelper.registerWood(
      "universe_trapdoor", universe,
      properties -> new TrapDoorBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)
  );
  public static final Block UNIVERSE_PRESSURE_PLATE = BlockRegHelper.registerWood(
      "universe_pressure_plate", universe,
      properties -> new PressurePlateBlock(BlockSetType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)
  );
  public static final Block UNIVERSE_BUTTON = BlockRegHelper.registerWood(
      "universe_button", universe,
      properties -> new ButtonBlock(BlockSetType.OAK, 30, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)
  );
  public static final Block UNIVERSE_SHELF = BlockRegHelper.registerWood(
      "universe_shelf", universe, ShelfBlock::new,
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SHELF)
  );
  public static final Block UNIVERSE_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "universe_sign", universe, properties -> new StandingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)
  );
  public static final Block UNIVERSE_WALL_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "universe_wall_sign", universe, properties -> new WallSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)
          .overrideLootTable(signLootTable("universe_sign"))
  );
  public static final Item UNIVERSE_SIGN_ITEM = BlockRegHelper.registerWoodSignItem(
      "universe_sign", universe, UNIVERSE_SIGN, UNIVERSE_WALL_SIGN, false
  );
  public static final Block UNIVERSE_HANGING_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "universe_hanging_sign", universe, properties -> new CeilingHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)
  );
  public static final Block UNIVERSE_WALL_HANGING_SIGN = BlockRegHelper.registerWoodBlockOnly(
      "universe_wall_hanging_sign", universe, properties -> new WallHangingSignBlock(WoodType.OAK, properties),
      BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)
          .overrideLootTable(signLootTable("universe_hanging_sign"))
  );
  public static final Item UNIVERSE_HANGING_SIGN_ITEM = BlockRegHelper.registerWoodSignItem(
      "universe_hanging_sign", universe, UNIVERSE_HANGING_SIGN, UNIVERSE_WALL_HANGING_SIGN, true
  );
  public static final Block UNIVERSE_SAPLING = BlockRegHelper.registerSapling(
      "universe_sapling", universe, setting -> new SaplingBlock(SaplingGenerators.UNIVERSE, setting),
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_WOOD = BlockRegHelper.registerWood(
      "universe_wood", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_UNIVERSE_LOG = BlockRegHelper.registerWood(
      "stripped_universe_log", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block STRIPPED_UNIVERSE_WOOD = BlockRegHelper.registerWood(
      "stripped_universe_wood", universe, RotatedPillarBlock::new,
      BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Stone
  public static final Block UNIVERSE_DARK_BLOCK = BlockRegHelper.registerBase(
      "universe_dark_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LIGHT_BLOCK = BlockRegHelper.registerBase(
      "universe_light_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_PRIMARY_BLOCK = BlockRegHelper.registerBase(
      "universe_primary_block", universe, Block::new,
      BlockBehaviour.Properties.of()
  );

  // Insert Blocks - Entity
  public static final Block UNIVERSE_BLOCK = BlockRegHelper.registerBase(
      "universe_block", universe,
      UniverseBlock::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_DARK_AIR = BlockRegHelper.registerBase(
      "universe_dark_air", universe, TransparentBlock::new,
      BlockBehaviour.Properties.of().noOcclusion()
  );
  public static final Block UNIVERSE_LIGHT_AIR = BlockRegHelper.registerBase(
      "universe_light_air", universe, TransparentBlock::new,
      BlockBehaviour.Properties.of().noOcclusion()
  );

  // Portal Blocks
  public static final Block UNIVERSE_META_PORTAL = BlockRegHelper.registerPortal(
      "universe_meta_portal",
      roeyqian.magnatour.block.universe.UniverseMetaPortal::new
  );

  private static Optional<ResourceKey<LootTable>> signLootTable(String name) {
    return Optional.of(ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "blocks/" + name)
    ));
  }

  private UniverseBlocks() {}

  public static void init() {
    UniverseMetaPortal.registerTickEvent();
    Magnatour.LOGGER.info("[Server] Initializing 'UniverseBlocks'");
  }

}
