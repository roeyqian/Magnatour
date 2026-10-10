/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

  @Inject(method = "isFullContainer", at = @At("HEAD"), cancellable = true)
  private static void checkLibraryCapacity(
      Container container,
      Direction direction,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (container instanceof UniverseLibraryEntity library) {
      cir.setReturnValue(library.isStorageFull());
    }
  }

  @Inject(
      method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;"
          + "Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;",
      at = @At("HEAD"), cancellable = true
  )
  private static void insertIntoLibrary(
      Container source, Container destination,
      ItemStack stack,
      Direction direction,
      CallbackInfoReturnable<ItemStack> cir
  ) {
    if (destination instanceof UniverseLibraryEntity library) {
      cir.setReturnValue(library.insertItems(stack));
    }
  }

}
