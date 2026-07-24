/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Minecraft
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.universe.UniverseBlock;
import roeyqian.magnatour.block.universe.UniverseLeavesBlock;
import roeyqian.magnatour.block.universe.UniverseLibrary;
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

  private UniverseBlocks() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'UniverseBlocks'");
  }

}
