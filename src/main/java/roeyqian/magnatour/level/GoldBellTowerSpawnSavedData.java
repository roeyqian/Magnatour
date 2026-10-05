/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Java Standard
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Mojang
import com.mojang.serialization.Codec;

// Minecraft
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

// Magnatour
import roeyqian.magnatour.Magnatour;

/** Disabled tower instances, keyed by their start chunk and persisted per dimension. */
public class GoldBellTowerSpawnSavedData extends SavedData {

  private static final Codec<GoldBellTowerSpawnSavedData> CODEC = Codec.LONG.listOf()
      .fieldOf("disabled_structures").codec()
      .xmap(GoldBellTowerSpawnSavedData::new, data -> List.copyOf(data.disabledStructures));

  private static final SavedDataType<GoldBellTowerSpawnSavedData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "gold_bell_tower_spawning"),
      GoldBellTowerSpawnSavedData::new,
      CODEC,
      DataFixTypes.LEVEL
  );

  private final Set<Long> disabledStructures = new HashSet<>();

  public GoldBellTowerSpawnSavedData() {}

  private GoldBellTowerSpawnSavedData(
      List<Long> disabledStructures
  ) {
    this.disabledStructures.addAll(disabledStructures);
  }

  public static GoldBellTowerSpawnSavedData get(
      ServerLevel level
  ) {
    return level.getDataStorage().computeIfAbsent(TYPE);
  }

  public boolean isDisabled(
      long startChunk
  ) {
    return this.disabledStructures.contains(startChunk);
  }

  public void setDisabled(
      long startChunk,
      boolean disabled
  ) {
    boolean changed = disabled
        ? this.disabledStructures.add(startChunk)
        : this.disabledStructures.remove(startChunk);
    if (changed) this.setDirty();
  }

}
