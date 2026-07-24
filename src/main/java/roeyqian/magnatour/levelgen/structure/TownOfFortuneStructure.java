/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.structure;

// Java Standard
import java.util.Optional;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.worldgen.CustomStructures;

public final class TownOfFortuneStructure extends Structure {

  public static final MapCodec<TownOfFortuneStructure> CODEC =
      simpleCodec(TownOfFortuneStructure::new);

  private static final int MAX_DEPTH = 24;

  private static final JigsawStructure.MaxDistance MAX_DISTANCE =
      new JigsawStructure.MaxDistance(116);

  private static final ResourceKey<StructureTemplatePool> START_POOL =
      ResourceKey.create(
          Registries.TEMPLATE_POOL,
          Identifier.fromNamespaceAndPath(
              Magnatour.MOD_ID,
              "town_of_fortune/plains/town_centers"
          )
      );

  public TownOfFortuneStructure(
      StructureSettings settings
  ) {
    super(settings);
  }

  @Override @NonNull
  public Optional<GenerationStub> findGenerationPoint(
      GenerationContext context
  ) {
    Registry<StructureTemplatePool> templatePools =
        context.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
    Optional<Holder.Reference<StructureTemplatePool>> startPool =
        templatePools.get(START_POOL);
    if (startPool.isEmpty()) {
      return Optional.empty();
    }

    ChunkPos chunkPos = context.chunkPos();
    BlockPos startPos = new BlockPos(
        chunkPos.getMinBlockX(),
        0,
        chunkPos.getMinBlockZ()
    );

    return JigsawPlacement.addPieces(
        context,
        startPool.get(),
        Optional.empty(),
        MAX_DEPTH,
        startPos,
        true,
        Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
        MAX_DISTANCE,
        PoolAliasLookup.EMPTY,
        DimensionPadding.ZERO,
        LiquidSettings.IGNORE_WATERLOGGING
    );
  }

  @Override @NonNull
  public StructureType<?> type() {
    return CustomStructures.TOWN_OF_FORTUNE;
  }

}
