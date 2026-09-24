/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.client;

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
    if (packet.count() <= PARTICLE_PACKET_COUNT_CAP) {
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
    boolean alternative = packet.randomizationType().isAlternative();
    boolean randomSpeed = packet.randomizationType()
        == ClientboundLevelParticlesPacket.RandomizationType.ALTERNATIVE_WITH_SPEED;
    for (int i = 0; i < PARTICLE_PACKET_COUNT_CAP; i++) {
      double xVariance = (alternative ? random.nextDouble() : random.nextGaussian()) * packet.xDist();
      double yVariance = (alternative ? random.nextDouble() : random.nextGaussian()) * packet.yDist();
      double zVariance = (alternative ? random.nextDouble() : random.nextGaussian()) * packet.zDist();
      double xa = (alternative ? randomSpeed ? random.nextDouble() : 1.0 : random.nextGaussian())
          * packet.xMaxSpeed();
      double ya = (alternative ? randomSpeed ? random.nextDouble() : 1.0 : random.nextGaussian())
          * packet.yMaxSpeed();
      double za = (alternative ? randomSpeed ? random.nextDouble() : 1.0 : random.nextGaussian())
          * packet.zMaxSpeed();

      try {
        level.addParticle(
            packet.particle(),
            packet.overrideLimiter(),
            packet.alwaysShow(),
            packet.x() + xVariance,
            packet.y() + yVariance,
            packet.z() + zVariance,
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
