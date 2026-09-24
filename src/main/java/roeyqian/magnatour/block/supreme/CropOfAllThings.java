/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Minecraft
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeItems;

public class CropOfAllThings extends CropBlock {

  public CropOfAllThings(
      Properties settings
  ) {
    super(settings);
  }

  @Override @NonNull
  protected ItemLike getBaseSeedId() {
    return SupremeItems.SEED_OF_ALL_THINGS;
  }

}
