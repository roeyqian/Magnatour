/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.menu;

// Minecraft
import net.minecraft.world.inventory.AnvilMenu;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.menu.MenuHelperForEnchantment;

@Mixin(value = AnvilMenu.class, priority = 3600000)
public class AnvilMenuMixin {

  /* Universe Equipment: No anvil enchanting
   */
  @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
  private void inCreateResult(
      CallbackInfo ci
  ) {
    MenuHelperForEnchantment.handleCreateResult((AnvilMenu) (Object) this, ci);
  }

}
