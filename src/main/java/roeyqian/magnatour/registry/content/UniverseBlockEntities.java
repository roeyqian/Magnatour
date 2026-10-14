/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

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
import roeyqian.magnatour.blockentity.universe.UniverseBlockEntity;
import roeyqian.magnatour.blockentity.universe.UniverseDotEntity;
import roeyqian.magnatour.blockentity.universe.UniverseForgeEntity;
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;
import roeyqian.magnatour.blockentity.universe.UniversePoolEntity;
import roeyqian.magnatour.blockentity.universe.UniverseRefineryEntity;

/*
 * Universe Group: Work, Store
 */
public final class UniverseBlockEntities {

  // Work
  public static final BlockEntityType<UniverseForgeEntity> UNIVERSE_FORGE_ENTITY = register(
      "universe_forge_entity",
      UniverseForgeEntity::new,
      UniverseBlocks.UNIVERSE_FORGE
  );

  public static final BlockEntityType<UniverseBlockEntity> UNIVERSE_BLOCK_ENTITY = register(
      "universe_block_entity",
      UniverseBlockEntity::new,
      UniverseBlocks.UNIVERSE_BLOCK
  );

  public static final BlockEntityType<UniverseLibraryEntity> UNIVERSE_LIBRARY_ENTITY = register(
      "universe_library_entity",
      UniverseLibraryEntity::new,
      UniverseBlocks.UNIVERSE_LIBRARY
  );

  // Store
  public static final BlockEntityType<UniverseRefineryEntity> UNIVERSE_REFINERY_ENTITY = register(
      "universe_refinery_entity",
      UniverseRefineryEntity::new,
      UniverseBlocks.UNIVERSE_REFINERY
  );

  public static final BlockEntityType<UniversePoolEntity> UNIVERSE_POOL_ENTITY = register(
      "universe_pool_entity",
      UniversePoolEntity::new,
      UniverseBlocks.UNIVERSE_POOL
  );

  public static final BlockEntityType<UniverseDotEntity> UNIVERSE_DOT_ENTITY = register(
      "universe_dot_entity",
      UniverseDotEntity::new,
      UniverseBlocks.UNIVERSE_DOT
  );

  private UniverseBlockEntities() {}

  public static void init() {
    ((FabricBlockEntityType) BlockEntityTypes.SHELF).addValidBlock(UniverseBlocks.UNIVERSE_SHELF);
    ((FabricBlockEntityType) BlockEntityTypes.SIGN).addValidBlock(UniverseBlocks.UNIVERSE_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.SIGN).addValidBlock(UniverseBlocks.UNIVERSE_WALL_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN).addValidBlock(UniverseBlocks.UNIVERSE_HANGING_SIGN);
    ((FabricBlockEntityType) BlockEntityTypes.HANGING_SIGN).addValidBlock(UniverseBlocks.UNIVERSE_WALL_HANGING_SIGN);
    Magnatour.LOGGER.info("[Server] Initializing 'UniverseBlockEntities'");
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
