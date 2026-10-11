/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.menu;

// Minecraft
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Slot.class, priority = 3600000)
public class SlotMixin {

  @Shadow @Final public Container container;

  @Redirect(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private int getInventoryStackLimit(
      ItemStack stack
  ) {
    return this.container instanceof Inventory inventory ? inventory.getMaxStackSize(stack) : stack.getMaxStackSize();
  }

}
