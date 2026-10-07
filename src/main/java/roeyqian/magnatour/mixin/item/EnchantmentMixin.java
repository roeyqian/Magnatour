/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.item;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import roeyqian.magnatour.registry.content.SupremeItems;

// Other
// Apache Commons Lang
import org.apache.commons.lang3.mutable.MutableFloat;

@Mixin(value = Enchantment.class, priority = 3600000)
public abstract class EnchantmentMixin {

  @Inject(method = "isPrimaryItem", at = @At("RETURN"), cancellable = true)
  private void inIsPrimaryItem(
      ItemStack stack,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (cir.getReturnValue() || !stack.is(SupremeItems.EMPEROR_AXE_PICKAXE)) return;
    Enchantment enchantment = (Enchantment) (Object) this;
    if (enchantment.isSupportedItem(stack)
        && ItemHelperForEnchantment.supportsAxeOrPickaxe(
            enchantment.definition().primaryItems().orElse(enchantment.getSupportedItems()))) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "isSupportedItem", at = @At("RETURN"), cancellable = true)
  private void inIsSupportedItem(
      ItemStack stack,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (cir.getReturnValue() || !stack.is(SupremeItems.EMPEROR_AXE_PICKAXE)) return;
    Enchantment enchantment = (Enchantment) (Object) this;
    if (ItemHelperForEnchantment.supportsAxeOrPickaxe(enchantment.getSupportedItems())) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "modifyDamage", at = @At("HEAD"), cancellable = true)
  private void inModifyDamage(
      ServerLevel world,
      int level,
      ItemStack stack,
      Entity target,
      DamageSource source,
      MutableFloat damage,
      CallbackInfo ci
  ) {
    if (!ItemHelperForEnchantment.isEmperorTool(stack)) return;
    ItemHelperForEnchantment.applyEmperorDamageEnchantments(
        (Enchantment) (Object) this, world, level, target, source, damage
    );
    ci.cancel();
  }

}
