/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Minecraft
import net.minecraft.world.entity.player.Player;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.item.RemoteAccessManager;

public final class UniverseRemoteAccessMain {

  private UniverseRemoteAccessMain() {}

  public static void handleRemoteBlockAccess(
      Player player,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (RemoteAccessManager.isRemoteAccessing(player)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleRemoved(
      Player player
  ) {
    RemoteAccessManager.endRemoteAccess(player);
  }

}
