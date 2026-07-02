/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.client;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.util.RandomSource;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
public final class ClientHelperForParticle {

  private static final int PARTICLE_PACKET_COUNT_CAP = 10240;

  private ClientHelperForParticle() {}

  /* Universe Particles: Large Particle Packet Count Cap
   *
   * Caps the particle count from a single packet so an oversized burst cannot stall the
   * client; when over the cap, spawns up to the cap manually and cancels vanilla handling.
   */
  public static void handleParticleEvent(
      ClientLevel level,
      RandomSource random,
      ClientboundLevelParticlesPacket packet,
      CallbackInfo ci
  ) {
    if (packet.getCount() <= PARTICLE_PACKET_COUNT_CAP) {
      return;
    }

    spawnCappedParticles(level, random, packet);
    ci.cancel();
  }

  private static void spawnCappedParticles(
      ClientLevel level,
      RandomSource random,
      ClientboundLevelParticlesPacket packet
  ) {
    for (int i = 0; i < PARTICLE_PACKET_COUNT_CAP; i++) {
      double xVariance = random.nextGaussian() * packet.getXDist();
      double yVariance = random.nextGaussian() * packet.getYDist();
      double zVariance = random.nextGaussian() * packet.getZDist();
      double xa = random.nextGaussian() * packet.getMaxSpeed();
      double ya = random.nextGaussian() * packet.getMaxSpeed();
      double za = random.nextGaussian() * packet.getMaxSpeed();

      try {
        level.addParticle(
            packet.getParticle(),
            packet.isOverrideLimiter(),
            packet.alwaysShow(),
            packet.getX() + xVariance,
            packet.getY() + yVariance,
            packet.getZ() + zVariance,
            xa,
            ya,
            za
        );
      } catch (Throwable ignored) {
        return;
      }
    }
  }

}
