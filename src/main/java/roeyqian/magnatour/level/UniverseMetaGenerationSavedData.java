/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Mojang
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

// Minecraft
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

// Magnatour
import roeyqian.magnatour.Magnatour;

/** Persistent generation status for the Universe Meta dimension's central cube. */
public class UniverseMetaGenerationSavedData extends SavedData {

  private static final int COMPLETED = Integer.MAX_VALUE;

  /**
   * The boolean branch preserves worlds created before generation became incremental. New saves
   * store the next block index, so an interrupted first generation resumes after a restart.
   */
  private static final Codec<UniverseMetaGenerationSavedData> CODEC = Codec.either(
      Codec.BOOL.fieldOf("generated").codec(),
      Codec.INT.fieldOf("next_block_index").codec()
  ).xmap(
      savedValue -> savedValue.map(
          generated -> new UniverseMetaGenerationSavedData(generated ? COMPLETED : 0),
          UniverseMetaGenerationSavedData::new
      ),
      savedData -> Either.right(savedData.nextBlockIndex)
  );

  private static final SavedDataType<UniverseMetaGenerationSavedData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_meta_generation"),
      UniverseMetaGenerationSavedData::new,
      CODEC,
      DataFixTypes.LEVEL
  );

  private int nextBlockIndex;

  public UniverseMetaGenerationSavedData() {
    this(0);
  }

  private UniverseMetaGenerationSavedData(
      int nextBlockIndex
  ) {
    this.nextBlockIndex = Math.max(0, nextBlockIndex);
  }

  public static UniverseMetaGenerationSavedData get(
      MinecraftServer server
  ) {
    return server.overworld().getDataStorage().computeIfAbsent(TYPE);
  }

  public boolean isGenerated() {
    return this.nextBlockIndex == COMPLETED;
  }

  public void markGenerated() {
    this.nextBlockIndex = COMPLETED;
    this.setDirty();
  }

  public int nextBlockIndex() {
    return this.nextBlockIndex;
  }

  public void setNextBlockIndex(
      int nextBlockIndex
  ) {
    if (this.isGenerated()) return;

    this.nextBlockIndex = Math.max(0, nextBlockIndex);
    this.setDirty();
  }

}
