/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.menu;

// Minecraft
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.entity.UniverseInventoryHelper;
import roeyqian.magnatour.mixinhelper.menu.MenuHelperForEquipment;

@Mixin(value = AbstractContainerMenu.class, priority = 3600000)
public class AbstractContainerMenuMixin {

  @Redirect(method = "getQuickCraftPlaceCount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private static int getCreativeDragStackLimit(
      ItemStack stack
  ) {
    // The caller clamps this creative-only amount to each destination slot's limit.
    return stack.getMaxStackSize() <= 1 ? stack.getMaxStackSize() : Math.max(512, stack.getMaxStackSize());
  }

  @Redirect(method = "canItemQuickReplace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private static int getQuickReplaceStackLimit(
      ItemStack stack,
      Slot slot,
      ItemStack incoming,
      boolean allowOverflow
  ) {
    return slot.container instanceof Inventory inventory ? inventory.getMaxStackSize(stack) : stack.getMaxStackSize();
  }

  @Redirect(method = "moveItemStackTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"))
  private boolean allowPlayerQuickMoveStacking(
      ItemStack stack
  ) {
    return stack.isStackable()
        || (stack.getMaxStackSize() > 1 && UniverseInventoryHelper.hasArmor((AbstractContainerMenu) (Object) this));
  }

  @Inject(method = "clicked", at = @At("TAIL"))
  private void dropExcessAfterClick(
      int slotIndex, int button,
      ContainerInput input,
      Player player,
      CallbackInfo ci
  ) {
    UniverseInventoryHelper.dropExcess(player);
  }

  @Redirect(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private int getPlayerClickStackLimit(
      ItemStack stack,
      int slotIndex, int button,
      ContainerInput input,
      Player player
  ) {
    return UniverseInventoryHelper.getStackLimit(player, stack);
  }

  /* Universe Console: Clear Remote Access State
   */
  @Inject(method = "removed", at = @At("TAIL"))
  private void inRemoved(
      Player player,
      CallbackInfo ci
  ) {
    MenuHelperForEquipment.handleRemoved(player);
  }

}
