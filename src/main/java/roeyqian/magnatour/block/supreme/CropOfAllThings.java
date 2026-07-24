/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeItems;

public class CropOfAllThings extends CropBlock {

  public static final MapCodec<CropOfAllThings> CODEC = simpleCodec(CropOfAllThings::new);

  public CropOfAllThings(
      Properties settings
  ) {
    super(settings);
  }

  @Override @NonNull
  public MapCodec<? extends CropBlock> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected ItemLike getBaseSeedId() {
    return SupremeItems.SEED_OF_ALL_THINGS;
  }

}
