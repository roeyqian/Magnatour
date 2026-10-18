/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.world;

// Minecraft
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.MobStructureSpawner;
import roeyqian.magnatour.levelgen.supreme.StructureDiamondCity;
import roeyqian.magnatour.levelgen.supreme.StructureDiamondCityPiece;
import roeyqian.magnatour.levelgen.supreme.StructureGoldBellTower;
import roeyqian.magnatour.levelgen.supreme.StructureGoldBellTowerPiece;
import roeyqian.magnatour.levelgen.supreme.StructureTownOfFortune;
import roeyqian.magnatour.levelgen.universe.StructureUniverseMetaOrigin;
import roeyqian.magnatour.levelgen.universe.StructureUniverseMetaOriginPiece;
import roeyqian.magnatour.registry.WorldHelper;

/*
 * Supreme Group: Harvest Continent, Ore Continent
 * Universe Group: Structure
 */
public final class MagnaStructures {

  // Supreme Group: Ore Continent
  public static final StructurePieceType DIAMOND_CITY_PIECE =
      registerDiamondCityPiece();
  // Supreme Group: Harvest Continent
  public static final StructurePieceType GOLD_BELL_TOWER_PIECE =
      registerGoldBellTowerPiece();

  // Universe Group: Structure
  public static final StructurePieceType UNIVERSE_META_ORIGIN_PIECE =
      registerUniverseMetaOriginPiece();

  public static final StructureType<StructureDiamondCity> DIAMOND_CITY =
      WorldHelper.registerStructureType(
          "diamond_city",
          () -> StructureDiamondCity.CODEC
      );

  public static final StructureType<StructureGoldBellTower> GOLD_BELL_TOWER =
      WorldHelper.registerStructureType(
          "gold_bell_tower",
          () -> StructureGoldBellTower.CODEC
      );

  public static final StructureType<StructureTownOfFortune> TOWN_OF_FORTUNE =
      WorldHelper.registerStructureType(
          "town_of_fortune",
          () -> StructureTownOfFortune.CODEC
      );

  public static final StructureType<StructureUniverseMetaOrigin> UNIVERSE_META_ORIGIN =
      WorldHelper.registerStructureType(
          "universe_meta_origin",
          () -> StructureUniverseMetaOrigin.CODEC
      );

  private MagnaStructures() {}

  public static void init() {
    MobStructureSpawner.registerTickEvent();
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaStructures'");
  }

  private static StructurePieceType registerDiamondCityPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = WorldHelper.registerStructurePiece(
        "diamond_city",
        (_, tag) -> new StructureDiamondCityPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

  private static StructurePieceType registerGoldBellTowerPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = WorldHelper.registerStructurePiece(
        "gold_bell_tower",
        (_, tag) -> new StructureGoldBellTowerPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

  private static StructurePieceType registerUniverseMetaOriginPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = WorldHelper.registerStructurePiece(
        "universe_meta_origin",
        (_, tag) -> new StructureUniverseMetaOriginPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

}
