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
import roeyqian.magnatour.blockentity.universe.UniverseBlockEntity;
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;
import roeyqian.magnatour.blockentity.universe.UniverseRefineryEntity;
import roeyqian.magnatour.blockentity.universe.UniverseTeleportPointEntity;
import roeyqian.magnatour.blockentity.universe.UniverseVoidPoolEntity;

/*
 * Universe Group: Work, Store
 */
public final class UniverseBlockEntities {

  // Work
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

  public static final BlockEntityType<UniverseVoidPoolEntity> UNIVERSE_VOID_POOL_ENTITY = register(
      "universe_void_pool_entity",
      UniverseVoidPoolEntity::new,
      UniverseBlocks.UNIVERSE_VOID_POOL
  );

  public static final BlockEntityType<UniverseTeleportPointEntity> UNIVERSE_TELEPORT_POINT_ENTITY = register(
      "universe_teleport_point_entity",
      UniverseTeleportPointEntity::new,
      UniverseBlocks.UNIVERSE_TELEPORT_POINT
  );

  private UniverseBlockEntities() {}

  public static void init() {
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
