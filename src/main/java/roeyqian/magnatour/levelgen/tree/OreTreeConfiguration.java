/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.tree;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record OreTreeConfiguration(
    BlockState trunk,
    BlockState foliage,
    int minHeight,
    int maxHeight,
    int foliageRadius
) implements FeatureConfiguration {

  public static final Codec<OreTreeConfiguration> CODEC =
      RecordCodecBuilder.create((instance) -> instance.group(
              BlockState.CODEC.fieldOf("trunk")
                  .forGetter(OreTreeConfiguration::trunk),
              BlockState.CODEC.fieldOf("foliage")
                  .forGetter(OreTreeConfiguration::foliage),
              Codec.INT.optionalFieldOf("min_height", 4)
                  .forGetter(OreTreeConfiguration::minHeight),
              Codec.INT.optionalFieldOf("max_height", 7)
                  .forGetter(OreTreeConfiguration::maxHeight),
              Codec.INT.optionalFieldOf("foliage_radius", 2)
                  .forGetter(OreTreeConfiguration::foliageRadius)
          )
          .apply(instance, OreTreeConfiguration::new)
      );

  public OreTreeConfiguration {
    minHeight = Mth.clamp(minHeight, 1, 32);
    maxHeight = Mth.clamp(Math.max(maxHeight, minHeight), 1, 32);
    foliageRadius = Mth.clamp(foliageRadius, 1, 5);
  }

}
