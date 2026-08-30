/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level;

// Mojang
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

  private static final Codec<UniverseMetaGenerationSavedData> CODEC = Codec.BOOL
      .fieldOf("generated")
      .codec()
      .xmap(UniverseMetaGenerationSavedData::new, UniverseMetaGenerationSavedData::isGenerated);

  private static final SavedDataType<UniverseMetaGenerationSavedData> TYPE = new SavedDataType<>(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_meta_generation"),
      UniverseMetaGenerationSavedData::new,
      CODEC,
      DataFixTypes.LEVEL
  );

  private boolean generated;

  public UniverseMetaGenerationSavedData() {
    this(false);
  }

  private UniverseMetaGenerationSavedData(
      boolean generated
  ) {
    this.generated = generated;
  }

  public static UniverseMetaGenerationSavedData get(
      MinecraftServer server
  ) {
    return server.overworld().getDataStorage().computeIfAbsent(TYPE);
  }

  public boolean isGenerated() {
    return this.generated;
  }

  public void markGenerated() {
    this.generated = true;
    this.setDirty();
  }

}
