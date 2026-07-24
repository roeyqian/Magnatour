/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

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
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.blockentity.supreme.LogisticsFiberEntity;
import roeyqian.magnatour.blockentity.supreme.RedstoneTriggerEntity;
import roeyqian.magnatour.blockentity.supreme.SupremeBlockEntity;
import roeyqian.magnatour.blockentity.supreme.SupremeChestEntity;
import roeyqian.magnatour.blockentity.supreme.SupremeFurnaceEntity;

/*
 * Supreme Group: Work, Store
 */
public final class SupremeBlockEntities {

  // Work
  public static final BlockEntityType<ItemHubEntity> ITEM_HUB_ENTITY = register(
      "item_hub_entity",
      ItemHubEntity::new,
      SupremeBlocks.ITEM_HUB
  );

  public static final BlockEntityType<SupremeBlockEntity> SUPREME_BLOCK_ENTITY = register(
      "supreme_block_entity",
      SupremeBlockEntity::new,
      SupremeBlocks.SUPREME_BLOCK
  );

  public static final BlockEntityType<SupremeChestEntity> SUPREME_CHEST_ENTITY = register(
      "supreme_chest_entity",
      SupremeChestEntity::new,
      SupremeBlocks.SUPREME_CHEST
  );

  public static final BlockEntityType<LogisticsFiberEntity> LOGISTICS_FIBER_ENTITY = register(
      "logistics_fiber_entity",
      LogisticsFiberEntity::new,
      SupremeBlocks.LOGISTICS_FIBER
  );

  public static final BlockEntityType<RedstoneTriggerEntity> REDSTONE_TRIGGER_ENTITY = register(
      "redstone_trigger_entity",
      RedstoneTriggerEntity::new,
      SupremeBlocks.REDSTONE_TRIGGER
  );

  // Store
  public static final BlockEntityType<SupremeFurnaceEntity> SUPREME_FURNACE_ENTITY = register(
      "supreme_furnace_entity",
      SupremeFurnaceEntity::new,
      SupremeBlocks.SUPREME_FURNACE
  );

  private SupremeBlockEntities() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'SupremeBlockEntities'");
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
