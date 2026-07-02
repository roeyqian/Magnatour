/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Java Standard
import java.util.SequencedMap;

// Mojang
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT) @Mixin(value = MultiBufferSource.BufferSource.class, priority = 3600000)
public interface BufferSourceAccessor {

  @Accessor("fixedBuffers")
  SequencedMap<RenderType, ByteBufferBuilder> getFixedBuffers();

}
