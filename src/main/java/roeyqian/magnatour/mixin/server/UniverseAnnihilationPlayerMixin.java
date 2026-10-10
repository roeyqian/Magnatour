/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.server;

// Java Standard
import java.util.Set;

// Minecraft
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.level.UniverseAnnihilation;

@Mixin(ServerPlayer.class)
public abstract class UniverseAnnihilationPlayerMixin {

  // Commands and direct mod teleport calls may bypass the portal transition entry point.
  @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z",
      at = @At("HEAD"), cancellable = true)
  private void magnatour$blockDirectTeleport(
      ServerLevel destination,
      double x, double y, double z,
      Set<Relative> relatives,
      float yaw, float pitch,
      boolean resetCamera,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (UniverseAnnihilation.isLocked(destination)) {
      ((ServerPlayer) (Object) this).sendSystemMessage(Component.translatable("gui.magnatour.annihilator.locked"));
      cir.setReturnValue(false);
    }
  }

  // Portals, ender pearls and all TeleportTransition-based travel share this entry point.
  @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
      at = @At("HEAD"), cancellable = true)
  private void magnatour$blockTransition(
      TeleportTransition transition,
      CallbackInfoReturnable<ServerPlayer> cir
  ) {
    if (UniverseAnnihilation.isLocked(transition.newLevel())) {
      ((ServerPlayer) (Object) this).sendSystemMessage(Component.translatable("gui.magnatour.annihilator.locked"));
      cir.setReturnValue(null);
    }
  }

  // PlayerList respawn constructs a new player without calling either teleport method.
  @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"), cancellable = true)
  private void magnatour$redirectRespawn(
      boolean consumeSpawnBlock,
      TeleportTransition.PostTeleportTransition postTeleport,
      CallbackInfoReturnable<TeleportTransition> cir
  ) {
    TeleportTransition transition = cir.getReturnValue();
    if (transition != null && UniverseAnnihilation.isLocked(transition.newLevel())) {
      ServerPlayer player = (ServerPlayer) (Object) this;
      var landing = UniverseAnnihilation.evacuationLanding(player.level().getServer(), transition.newLevel().dimension());
      cir.setReturnValue(new TeleportTransition(landing.level(),
          Vec3.atBottomCenterOf(landing.position()), Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
    }
  }

}
