/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.render;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// JSpecify
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.utility.mixin.render.RenderHelperForGlint;

@Environment(EnvType.CLIENT) @Mixin(value = CuboidItemModelWrapper.class, priority = 3600000)
public class CuboidItemModelWrapperMixin {

  /* Universe Items: Custom Glint Marker
   */
  @Inject(method = "update", at = @At("TAIL"))
  private void inUpdate(
      ItemStackRenderState output,
      ItemStack item,
      ItemModelResolver resolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed,
      CallbackInfo ci
  ) {
    RenderHelperForGlint.markGlint(output, item);
  }

}
