/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.tree;

// Minecraft
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;

// Magnatour
import roeyqian.magnatour.Magnatour;

public final class SaplingGenerators {

  public static final TreeGrower GOLDEN = new TreeGrower(
      "golden",
      WeightedList.of(),
      WeightedList.of(),
      WeightedList.of(),
      ResourceKey.create(
          Registries.FEATURE,
          Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "golden_tree")
      )
  );
  public static final TreeGrower UNIVERSE = new TreeGrower(
      "universe",
      WeightedList.of(),
      WeightedList.of(),
      WeightedList.of(),
      ResourceKey.create(
          Registries.FEATURE,
          Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_tree")
      )
  );

}
