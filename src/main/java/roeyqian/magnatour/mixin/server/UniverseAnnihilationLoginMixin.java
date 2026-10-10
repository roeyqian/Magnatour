/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.server;

// Java Standard
import java.net.SocketAddress;

// Minecraft
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.level.UniverseAnnihilation;

@Mixin(PlayerList.class)
public abstract class UniverseAnnihilationLoginMixin {

  // Player data is loaded, but login packets and registration have not been sent.
  @Inject(method = "placeNewPlayer", at = @At("HEAD"))
  private void magnatour$blockPlacement(
      Connection connection,
      ServerPlayer player,
      CommonListenerCookie cookie,
      CallbackInfo ci
  ) {
    if (UniverseAnnihilation.isLocked(player.level())) {
      player.setServerLevel(((PlayerList) (Object) this).getServer().overworld());
      player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
      player.fallDistance = 0;
    }
  }

}
