/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
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
import roeyqian.magnatour.item.universe.UniverseBoots;
import roeyqian.magnatour.item.universe.UniverseChestplate;
import roeyqian.magnatour.item.universe.UniverseHelmet;
import roeyqian.magnatour.item.universe.UniverseLeggings;
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.item.universe.UniverseUltimaSword;

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

    if (itemStack.getItem() instanceof UniverseHelmet
        || itemStack.getItem() instanceof UniverseChestplate
        || itemStack.getItem() instanceof UniverseLeggings
        || itemStack.getItem() instanceof UniverseBoots
        || itemStack.getItem() instanceof UniverseUltimaSword
        || itemStack.getItem() instanceof UniverseOmniBlade) {
      ci.cancel();
    }
  }

}
