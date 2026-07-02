/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.block;

// Fabric
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.active.entity.LogisticsFiberEntity;
import roeyqian.magnatour.block.active.entity.ItemHubEntity;
import roeyqian.magnatour.block.active.entity.RedstoneTriggerEntity;
import roeyqian.magnatour.block.active.entity.SupremeChestEntity;
import roeyqian.magnatour.block.active.entity.SupremeFurnaceEntity;
import roeyqian.magnatour.block.active.entity.UniverseLibraryEntity;
import roeyqian.magnatour.block.active.entity.UniverseRefineryEntity;
import roeyqian.magnatour.block.active.entity.UniverseTeleportPointEntity;
import roeyqian.magnatour.block.active.entity.UniverseVoidPoolEntity;
import roeyqian.magnatour.block.insert.entity.SupremeBlockEntity;
import roeyqian.magnatour.block.insert.entity.UniverseBlockEntity;

/*
 * Supreme Group: Work, Store
 * Universe Group: Work, Store
 */
public final class RegBlockEntities {

  // Supreme Group: Work
  public static final BlockEntityType<SupremeBlockEntity> SUPREME_BLOCK_ENTITY = register(
      "supreme_block_entity",
      SupremeBlockEntity::new,
      RegInsertBlocks.SUPREME_BLOCK
  );
  public static final BlockEntityType<LogisticsFiberEntity> LOGISTICS_FIBER_ENTITY = register(
      "logistics_fiber_entity",
      LogisticsFiberEntity::new,
      RegInsertBlocks.LOGISTICS_FIBER
  );

  // Supreme Group: Store
  public static final BlockEntityType<SupremeFurnaceEntity> SUPREME_FURNACE_ENTITY = register(
      "supreme_furnace_entity",
      SupremeFurnaceEntity::new,
      RegActiveBlocks.SUPREME_FURNACE
  );
  public static final BlockEntityType<SupremeChestEntity> SUPREME_CHEST_ENTITY = register(
      "supreme_chest_entity",
      SupremeChestEntity::new,
      RegActiveBlocks.SUPREME_CHEST
  );
  public static final BlockEntityType<RedstoneTriggerEntity> REDSTONE_TRIGGER_ENTITY = register(
      "redstone_trigger_entity",
      RedstoneTriggerEntity::new,
      RegActiveBlocks.REDSTONE_TRIGGER
  );
  public static final BlockEntityType<ItemHubEntity> ITEM_HUB_ENTITY = register(
      "item_hub_entity",
      ItemHubEntity::new,
      RegActiveBlocks.ITEM_HUB
  );

  // Universe Group: Work
  public static final BlockEntityType<UniverseBlockEntity> UNIVERSE_BLOCK_ENTITY = register(
      "universe_block_entity",
      UniverseBlockEntity::new,
      RegInsertBlocks.UNIVERSE_BLOCK
  );

  // Universe Group: Store
  public static final BlockEntityType<UniverseRefineryEntity> UNIVERSE_REFINERY_ENTITY = register(
      "universe_refinery_entity",
      UniverseRefineryEntity::new,
      RegActiveBlocks.UNIVERSE_REFINERY
  );
  public static final BlockEntityType<UniverseLibraryEntity> UNIVERSE_LIBRARY_ENTITY = register(
      "universe_library_entity",
      UniverseLibraryEntity::new,
      RegActiveBlocks.UNIVERSE_LIBRARY
  );
  public static final BlockEntityType<UniverseVoidPoolEntity> UNIVERSE_VOID_POOL_ENTITY = register(
      "universe_void_pool_entity",
      UniverseVoidPoolEntity::new,
      RegActiveBlocks.UNIVERSE_VOID_POOL
  );
  public static final BlockEntityType<UniverseTeleportPointEntity> UNIVERSE_TELEPORT_POINT_ENTITY = register(
      "universe_teleport_point_entity",
      UniverseTeleportPointEntity::new,
      RegActiveBlocks.UNIVERSE_TELEPORT_POINT
  );

  private RegBlockEntities() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegActiveBlockEntities'");
  }

  private static <T extends BlockEntity> BlockEntityType<T> register(
      String path,
      FabricBlockEntityTypeBuilder.Factory<T> factory,
      Block block
  ) {
    return Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path),
        FabricBlockEntityTypeBuilder.create(factory, block).build()
    );
  }

}
