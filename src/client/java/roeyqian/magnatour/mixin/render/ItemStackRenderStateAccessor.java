/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.item.ItemStackRenderState;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT) @Mixin(value = ItemStackRenderState.class, priority = 3600000)
public interface ItemStackRenderStateAccessor {

  @Accessor("activeLayerCount")
  int getActiveLayerCount();

  @Accessor("layers")
  ItemStackRenderState.LayerRenderState[] getLayers();

}