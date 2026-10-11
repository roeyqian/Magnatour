/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.screen;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Magnatour
import roeyqian.magnatour.mixinhelper.entity.UniverseInventoryHelper;

@Mixin(value = AbstractContainerScreen.class, priority = 3600000)
public class AbstractContainerScreenMixin {

  @Redirect(method = {"extractSlot", "recalculateQuickCraftRemaining"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private int getPlayerDragStackLimit(
      ItemStack stack
  ) {
    var player = Minecraft.getInstance().player;
    return player == null ? stack.getMaxStackSize() : UniverseInventoryHelper.getStackLimit(player, stack);
  }

}
