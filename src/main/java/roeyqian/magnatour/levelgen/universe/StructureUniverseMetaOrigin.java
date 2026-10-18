/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.levelgen.universe;

// Java Standard
import java.util.Optional;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.world.MagnaStructures;

public final class StructureUniverseMetaOrigin extends Structure {

  public static final MapCodec<StructureUniverseMetaOrigin> CODEC =
      simpleCodec(StructureUniverseMetaOrigin::new);

  public StructureUniverseMetaOrigin(
      StructureSettings settings
  ) {
    super(settings);
  }

  @Override @NonNull
  public Optional<GenerationStub> findGenerationPoint(
      @NonNull GenerationContext context
  ) {
    // The structure always occupies [-32, 31] on all axes, regardless of seed or terrain.
    if (context.chunkPos().x() != 0 || context.chunkPos().z() != 0) {
      return Optional.empty();
    }

    return Optional.of(new GenerationStub(
        BlockPos.ZERO,
        builder -> builder.addPiece(new StructureUniverseMetaOriginPiece(
            MagnaStructures.UNIVERSE_META_ORIGIN_PIECE
        ))
    ));
  }

  @Override @NonNull
  public StructureType<?> type() {
    return MagnaStructures.UNIVERSE_META_ORIGIN;
  }

}
