/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.stuff;

// Fabric
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.blockentity.supreme.LogisticsFiberEntity;
import roeyqian.magnatour.blockentity.supreme.RedstoneTriggerEntity;
import roeyqian.magnatour.blockentity.supreme.SupremeBlockEntity;
import roeyqian.magnatour.blockentity.supreme.SupremeChestEntity;
import roeyqian.magnatour.blockentity.supreme.SupremeFurnaceEntity;
import roeyqian.magnatour.blockentity.universe.UniverseBlockEntity;
import roeyqian.magnatour.blockentity.universe.UniverseDotEntity;
import roeyqian.magnatour.blockentity.universe.UniverseForgeEntity;
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;
import roeyqian.magnatour.blockentity.universe.UniversePoolEntity;
import roeyqian.magnatour.blockentity.universe.UniverseRefineryEntity;

/*
 * Supreme and Universe Groups: Work, Store
 */
public final class MagnaBlockEntities {

  // Supreme Group
  // Work
  public static final BlockEntityType<ItemHubEntity> ITEM_HUB_ENTITY = register(
      "item_hub_entity",
      ItemHubEntity::new,
      MagnaBlocks.ITEM_HUB
  );

  public static final BlockEntityType<SupremeBlockEntity> SUPREME_BLOCK_ENTITY = register(
      "supreme_block_entity",
      SupremeBlockEntity::new,
      MagnaBlocks.SUPREME_BLOCK
  );

  public static final BlockEntityType<SupremeChestEntity> SUPREME_CHEST_ENTITY = register(
      "supreme_chest_entity",
      SupremeChestEntity::new,
      MagnaBlocks.SUPREME_CHEST
  );

  public static final BlockEntityType<LogisticsFiberEntity> LOGISTICS_FIBER_ENTITY = register(
      "logistics_fiber_entity",
      LogisticsFiberEntity::new,
      MagnaBlocks.LOGISTICS_FIBER
  );

  public static final BlockEntityType<RedstoneTriggerEntity> REDSTONE_TRIGGER_ENTITY = register(
      "redstone_trigger_entity",
      RedstoneTriggerEntity::new,
      MagnaBlocks.REDSTONE_TRIGGER
  );

  // Store
  public static final BlockEntityType<SupremeFurnaceEntity> SUPREME_FURNACE_ENTITY = register(
      "supreme_furnace_entity",
      SupremeFurnaceEntity::new,
      MagnaBlocks.SUPREME_FURNACE
  );

  // Universe Group
  // Work
  public static final BlockEntityType<UniverseForgeEntity> UNIVERSE_FORGE_ENTITY = register(
      "universe_forge_entity",
      UniverseForgeEntity::new,
      MagnaBlocks.UNIVERSE_FORGE
  );

  public static final BlockEntityType<UniverseBlockEntity> UNIVERSE_BLOCK_ENTITY = register(
      "universe_block_entity",
      UniverseBlockEntity::new,
      MagnaBlocks.UNIVERSE_BLOCK
  );

  public static final BlockEntityType<UniverseLibraryEntity> UNIVERSE_LIBRARY_ENTITY = register(
      "universe_library_entity",
      UniverseLibraryEntity::new,
      MagnaBlocks.UNIVERSE_LIBRARY
  );

  // Store
  public static final BlockEntityType<UniverseRefineryEntity> UNIVERSE_REFINERY_ENTITY = register(
      "universe_refinery_entity",
      UniverseRefineryEntity::new,
      MagnaBlocks.UNIVERSE_REFINERY
  );

  public static final BlockEntityType<UniversePoolEntity> UNIVERSE_POOL_ENTITY = register(
      "universe_pool_entity",
      UniversePoolEntity::new,
      MagnaBlocks.UNIVERSE_POOL
  );

  public static final BlockEntityType<UniverseDotEntity> UNIVERSE_DOT_ENTITY = register(
      "universe_dot_entity",
      UniverseDotEntity::new,
      MagnaBlocks.UNIVERSE_DOT
  );

  private MagnaBlockEntities() {}

  public static void init() {
    ((FabricBlockEntityType) BlockEntityTypes.SHELF).addValidBlock(MagnaBlocks.GOLDEN_SHELF);
    ((FabricBlockEntityType) BlockEntityTypes.SIGN).addValidBlock(MagnaBlocks.GOLDEN_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.SIGN).addValidBlock(MagnaBlocks.GOLDEN_WALL_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN).addValidBlock(MagnaBlocks.GOLDEN_HANGING_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN).addValidBlock(MagnaBlocks.GOLDEN_WALL_HANGING_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.SHELF).addValidBlock(MagnaBlocks.UNIVERSE_SHELF);
    ((FabricBlockEntityType) BlockEntityTypes.SIGN).addValidBlock(MagnaBlocks.UNIVERSE_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.SIGN).addValidBlock(MagnaBlocks.UNIVERSE_WALL_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN).addValidBlock(MagnaBlocks.UNIVERSE_HANGING_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN).addValidBlock(MagnaBlocks.UNIVERSE_WALL_HANGING_SIGN);
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaBlockEntities'");
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
