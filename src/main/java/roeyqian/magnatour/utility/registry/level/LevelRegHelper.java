/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.level;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface LevelRegHelper {

  static Identifier id(
      String path
  ) {
    return Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  static void registerBiomeSource(
      String path,
      MapCodec<? extends BiomeSource> codec
  ) {
    Registry.register(
        (Registry) BuiltInRegistries.BIOME_SOURCE,
        id(path),
        codec
    );
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  static void registerChunkGenerator(
      String path,
      MapCodec<? extends ChunkGenerator> codec
  ) {
    Registry.register(
        (Registry) BuiltInRegistries.CHUNK_GENERATOR,
        id(path),
        codec
    );
  }

  static <FC extends FeatureConfiguration> Feature<FC> registerFeature(
      String path,
      Feature<FC> feature
  ) {
    return Registry.register(
        BuiltInRegistries.FEATURE,
        id(path),
        feature
    );
  }

  static StructurePieceType registerStructurePiece(
      String path,
      StructurePieceType pieceType
  ) {
    return Registry.register(
        BuiltInRegistries.STRUCTURE_PIECE,
        id(path),
        pieceType
    );
  }

  static <S extends Structure> StructureType<S> registerStructureType(
      String path,
      StructureType<S> structureType
  ) {
    return Registry.register(
        BuiltInRegistries.STRUCTURE_TYPE,
        id(path),
        structureType
    );
  }

}
