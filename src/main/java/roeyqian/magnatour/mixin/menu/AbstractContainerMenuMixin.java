/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.menu;

// Minecraft
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.menu.MenuHelperForEquipment;

@Mixin(value = AbstractContainerMenu.class, priority = 3600000)
public class AbstractContainerMenuMixin {

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
