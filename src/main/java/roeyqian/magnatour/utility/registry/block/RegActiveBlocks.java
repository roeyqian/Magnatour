/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.block;

// Minecraft
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.insert.LogisticsFiber;
import roeyqian.magnatour.block.active.ItemHub;
import roeyqian.magnatour.block.active.RedstoneTrigger;
import roeyqian.magnatour.block.active.SupremeChest;
import roeyqian.magnatour.block.active.SupremeFurnace;
import roeyqian.magnatour.block.active.SupremeReserver;
import roeyqian.magnatour.block.active.SupremeWorktable;
import roeyqian.magnatour.block.active.UniverseLibrary;
import roeyqian.magnatour.block.active.UniverseRefinery;
import roeyqian.magnatour.block.active.UniverseTeleportPoint;
import roeyqian.magnatour.block.active.UniverseVoidPool;
import roeyqian.magnatour.block.active.UniverseWorkstation;

/*
 * Supreme Group: Stateless, Stateful
 * Universe Group: Stateless, Stateful
 */
public final class RegActiveBlocks {

  private static final String supreme = "supreme";
  private static final String universe = "universe";

  // Supreme Group: Stateless
  public static final Block SUPREME_WORKTABLE = BlockRegHelper.registerBase(
      "supreme_worktable", supreme,
      SupremeWorktable::new, BlockBehaviour.Properties.of()
  );

  // Supreme Group: Stateful
  public static final Block SUPREME_FURNACE = BlockRegHelper.registerBase(
      "supreme_furnace", supreme,
      SupremeFurnace::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_RESERVER = BlockRegHelper.registerBase(
      "supreme_reserver", supreme,
      SupremeReserver::new, BlockBehaviour.Properties.of()
  );
  public static final Block SUPREME_CHEST = BlockRegHelper.registerBase(
      "supreme_chest", supreme,
      SupremeChest::new, BlockBehaviour.Properties.of()
  );
  public static final Block REDSTONE_TRIGGER = BlockRegHelper.registerBase(
      "redstone_trigger", supreme,
      RedstoneTrigger::new, BlockBehaviour.Properties.of()
  );
  public static final Block ITEM_HUB = BlockRegHelper.registerBase(
      "item_hub", supreme,
      ItemHub::new, BlockBehaviour.Properties.of().noOcclusion()
  );

  // Universe Group: Stateful
  public static final Block UNIVERSE_REFINERY = BlockRegHelper.registerBase(
      "universe_refinery", universe,
      UniverseRefinery::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_VOID_POOL = BlockRegHelper.registerBase(
      "universe_void_pool", universe,
      UniverseVoidPool::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_LIBRARY = BlockRegHelper.registerBase(
      "universe_library", universe,
      UniverseLibrary::new, BlockBehaviour.Properties.of()
  );
  public static final Block UNIVERSE_TELEPORT_POINT = BlockRegHelper.registerBase(
      "universe_teleport_point", universe,
      UniverseTeleportPoint::new, BlockBehaviour.Properties.of()
  );

  // Universe Group: Stateless
  public static final Block UNIVERSE_WORKSTATION = BlockRegHelper.registerBase(
      "universe_workstation", universe,
      UniverseWorkstation::new, BlockBehaviour.Properties.of()
  );

  private RegActiveBlocks() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegActiveBlocks'");
  }

}
