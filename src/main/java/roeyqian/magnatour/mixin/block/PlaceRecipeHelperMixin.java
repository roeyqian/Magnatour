/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.item.crafting.Recipe;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.block.BlockHelperForFunction;

@Mixin(value = PlaceRecipeHelper.class, priority = 3600000)
public interface PlaceRecipeHelperMixin {

  /* Supreme & Universe Crafting Recipe: Recipe Alignment Optimization
   */
  @Inject(method = "placeRecipe(IILnet/minecraft/world/item/crafting/Recipe;"
      + "Ljava/lang/Iterable;"
      + "Lnet/minecraft/recipebook/PlaceRecipeHelper$Output;)V",
      at = @At("HEAD"),
      cancellable = true)
  private static <T> void inPlaceRecipe(
      int width,
      int height,
      Recipe<?> recipe,
      Iterable<T> slots,
      PlaceRecipeHelper.Output<T> filler,
      CallbackInfo ci
  ) {
    if (BlockHelperForFunction.handlePlaceRecipe(width, height, recipe, slots, filler)) {
      ci.cancel();
    }
  }

}
