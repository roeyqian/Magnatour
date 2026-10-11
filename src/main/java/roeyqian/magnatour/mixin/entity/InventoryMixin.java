/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Magnatour
import roeyqian.magnatour.mixinhelper.entity.UniverseInventoryHelper;

@Mixin(value = Inventory.class, priority = 3600000)
public abstract class InventoryMixin implements Container {

  @Shadow @Final public Player player;

  @Override
  public int getMaxStackSize() {
    return Math.max(64, UniverseInventoryHelper.getArmorStackLimit(this.player));
  }

  @Override
  public int getMaxStackSize(
      ItemStack stack
  ) {
    return UniverseInventoryHelper.getStackLimit(this.player, stack);
  }

  @Redirect(method = "hasRemainingSpaceForItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"))
  private boolean allowArmorStacking(
      ItemStack stack
  ) {
    return UniverseInventoryHelper.getArmorStackLimit(this.player) > 0 || stack.isStackable();
  }

  @Redirect(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z"))
  private boolean allowDamagedArmorStacking(
      ItemStack stack
  ) {
    return UniverseInventoryHelper.getArmorStackLimit(this.player) == 0 && stack.isDamaged();
  }

  @Redirect(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private int getReturnStackLimit(
      ItemStack stack
  ) {
    return this.getMaxStackSize(stack);
  }

}
