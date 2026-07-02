/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.level;

// Minecraft
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.structure.DiamondCityPiece;
import roeyqian.magnatour.level.structure.DiamondCityStructure;
import roeyqian.magnatour.level.structure.GoldBellTowerPiece;
import roeyqian.magnatour.level.structure.GoldBellTowerStructure;
import roeyqian.magnatour.level.structure.StructureMobSpawner;

/*
 * Supreme Group: Harvest Continent, Ore Continent
 * Universe Group: Structure
 */
public final class RegStructures {

  // Supreme Group: Ore Continent
  public static final StructurePieceType DIAMOND_CITY_PIECE =
      registerDiamondCityPiece();
  public static final StructureType<DiamondCityStructure> DIAMOND_CITY =
      LevelRegHelper.registerStructureType(
          "diamond_city",
          () -> DiamondCityStructure.CODEC
      );

  // Supreme Group: Harvest Continent
  public static final StructurePieceType GOLD_BELL_TOWER_PIECE =
      registerGoldBellTowerPiece();
  public static final StructureType<GoldBellTowerStructure> GOLD_BELL_TOWER =
      LevelRegHelper.registerStructureType(
          "gold_bell_tower",
          () -> GoldBellTowerStructure.CODEC
      );

  private RegStructures() {}

  public static void init() {
    StructureMobSpawner.registerTickEvent();
    Magnatour.LOGGER.info("[Server] Initializing 'RegStructures'");
  }

  private static StructurePieceType registerDiamondCityPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = LevelRegHelper.registerStructurePiece(
        "diamond_city",
        (_, tag) -> new DiamondCityPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

  private static StructurePieceType registerGoldBellTowerPiece() {
    StructurePieceType[] pieceHolder = new StructurePieceType[1];
    pieceHolder[0] = LevelRegHelper.registerStructurePiece(
        "gold_bell_tower",
        (_, tag) -> new GoldBellTowerPiece(pieceHolder[0], tag)
    );
    return pieceHolder[0];
  }

}
