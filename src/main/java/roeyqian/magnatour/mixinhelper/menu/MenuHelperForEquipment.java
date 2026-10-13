/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.menu;

// Minecraft
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.item.RemoteAccessManager;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.item.universe.UniverseVoidBoots;
import roeyqian.magnatour.item.universe.UniverseVoidChestplate;
import roeyqian.magnatour.item.universe.UniverseVoidHelmet;
import roeyqian.magnatour.item.universe.UniverseVoidLeggings;

public final class MenuHelperForEquipment {

  private MenuHelperForEquipment() {}

  public static void handleRemoved(
      Player player
  ) {
    RemoteAccessManager.endRemoteAccess(player);
  }

  public static void handleSlotsChanged(
      Container inventory,
      CallbackInfo ci
  ) {
    ItemStack itemStack = inventory.getItem(0);

    if (itemStack.getItem() instanceof UniverseVoidHelmet
        || itemStack.getItem() instanceof UniverseVoidChestplate
        || itemStack.getItem() instanceof UniverseVoidLeggings
        || itemStack.getItem() instanceof UniverseVoidBoots
        || itemStack.getItem() instanceof UniverseUltimaSword
        || itemStack.getItem() instanceof UniverseOmniBlade) {
      ci.cancel();
    }
  }

}
