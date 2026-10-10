/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.server;

// Minecraft
import net.minecraft.server.MinecraftServer;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.level.UniverseAnnihilation;

@Mixin(MinecraftServer.class)
public abstract class UniverseAnnihilationMixin {

  @Inject(method = "stopServer", at = @At("HEAD"))
  private void magnatour$finishAfterClosingWorlds(
      CallbackInfo ci
  ) {
    UniverseAnnihilation.stop((MinecraftServer) (Object) this);
  }

  @Inject(method = "createLevels", at = @At("HEAD"))
  private void magnatour$finishBeforeLoading(
      CallbackInfo ci
  ) {
    UniverseAnnihilation.finishPending((MinecraftServer) (Object) this);
  }

}
