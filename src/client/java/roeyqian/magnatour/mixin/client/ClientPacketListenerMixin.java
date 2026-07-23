/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.util.RandomSource;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.client.ClientHelperForParticle;

@Mixin(value = ClientPacketListener.class, priority = 3600000)
public class ClientPacketListenerMixin {

  @Shadow @Final
  private RandomSource random;

  @Shadow
  private ClientLevel level;

  /* Universe Particles: Large Particle Packet Count Cap
   */
  @Inject(method = "handleParticleEvent", at = @At(
      value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
      shift = At.Shift.AFTER), cancellable = true)
  private void inHandleParticleEvent(
      ClientboundLevelParticlesPacket packet,
      CallbackInfo ci
  ) {
    ClientHelperForParticle.handleParticleEvent(this.level, this.random, packet, ci);
  }

}
