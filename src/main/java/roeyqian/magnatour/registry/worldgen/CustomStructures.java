/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.worldgen;

// Minecraft
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.levelgen.structure.DiamondCityPiece;
import roeyqian.magnatour.levelgen.structure.DiamondCityStructure;
import roeyqian.magnatour.levelgen.structure.GoldBellTowerPiece;
import roeyqian.magnatour.levelgen.structure.GoldBellTowerStructure;
import roeyqian.magnatour.levelgen.structure.StructureMobSpawner;
import roeyqian.magnatour.levelgen.structure.TownOfFortuneStructure;
import roeyqian.magnatour.registry.WorldgenRegHelper;

/*
 * Supreme Group: Harvest Continent, Ore Continent
 * Universe Group: Structure
 */
public final class CustomStructures {

  // Supreme Group: Ore Continent
  public static final StructurePieceType DIAMOND_CITY_PIECE =
      registerDiamondCityPiece();
  // Supreme Group: Harvest Continent
  public static final StructurePieceType GOLD_BELL_TOWER_PIECE =
      registerGoldBellTowerPiece();

  public static final StructureType<DiamondCityStructure> DIAMOND_CITY =
      WorldgenRegHelper.registerStructureType(
          "diamond_city",
          () -> DiamondCityStructure.CODEC
      );

  public static final StructureType<GoldBellTowerStructure> GOLD_BELL_TOWER =
      WorldgenRegHelper.registerStructureType(
          "gold_bell_tower",
          () -> GoldBellTowerStructure.CODEC
      );

  public static final StructureType<TownOfFortuneStructure> TOWN_OF_FORTUNE =
      WorldgenRegHelper.registerStructureType(
          "town_of_fortune",
          () -> TownOfFortuneStructure.CODEC
      );

  private CustomStructures() {}

  public static void init() {
    StructureMobSpawner.registerTickEvent();
    Magnatour.LOGGER.info("[Server] Initializing 'CustomStructures'");
  }

  private static StructurePieceType registerDiamondCityPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = WorldgenRegHelper.registerStructurePiece(
        "diamond_city",
        (_, tag) -> new DiamondCityPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

  private static StructurePieceType registerGoldBellTowerPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = WorldgenRegHelper.registerStructurePiece(
        "gold_bell_tower",
        (_, tag) -> new GoldBellTowerPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

}
