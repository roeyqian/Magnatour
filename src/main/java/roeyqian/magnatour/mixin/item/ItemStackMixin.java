/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.item;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.item.ItemHelperForEnchantment;

@Mixin(value = ItemStack.class, priority = 3600000)
public class ItemStackMixin {

  /* Universe Equipment: Disallow direct enchant application
   */
  @Inject(
      method = "enchant(Lnet/minecraft/core/Holder;I)V",
      at = @At("HEAD"),
      cancellable = true
  )
  private void inEnchant(
      Holder<Enchantment> enchantment,
      int level,
      CallbackInfo ci
  ) {
    ItemHelperForEnchantment.handleEnchant((ItemStack) (Object) this, ci);
  }

  /* Universe Equipment: Disallow enchantment component writes
   */
  @Inject(
      method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
      at = @At("HEAD"),
      cancellable = true
  )
  private <T> void inSet(
      DataComponentType<T> componentType,
      T value,
      CallbackInfoReturnable<T> cir
  ) {
    ItemHelperForEnchantment.handleSet(
        (ItemStack) (Object) this,
        componentType,
        value,
        cir
    );
  }

  /* Universe Equipment: Disallow typed enchantment component writes
   */
  @Inject(
      method = "set(Lnet/minecraft/core/component/TypedDataComponent;)Ljava/lang/Object;",
      at = @At("HEAD"),
      cancellable = true
  )
  private <T> void inSetTyped(
      TypedDataComponent<T> component,
      CallbackInfoReturnable<T> cir
  ) {
    ItemHelperForEnchantment.handleSetTyped(
        (ItemStack) (Object) this,
        component,
        cir
    );
  }

}
