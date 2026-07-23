/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.particle;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

// JSpecify
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class UniverseSonicBoomParticle extends SonicBoomParticle {

  private final Mode mode;

  private final float hueOffset;

  protected UniverseSonicBoomParticle(
      ClientLevel level,
      double x,
      double y,
      double z,
      double g,
      SpriteSet spriteSet,
      Mode mode
  ) {
    super(level, x, y, z, g, spriteSet);
    this.mode = mode;

    float h = (float) (x * 0.15 + y * 0.05 + z * 0.15);
    this.hueOffset = h - (float) Math.floor(h);

    this.applyColor();
  }

  @Override
  public void tick() {
    super.tick();
    this.applyColor();
  }

  private void applyColor() {
    if (this.mode == Mode.RAINBOW) {
      this.setColor(1.0F, 1.0F, 1.0F);
      return;
    }

    float hue = (this.hueOffset + this.age * 0.08F) % 1.0F;
    int rgb = Mth.hsvToArgb(hue, 1.0F, 1.0F, 1);

    float r = ((rgb >> 16) & 255) / 255.0F;
    float g = ((rgb >> 8) & 255) / 255.0F;
    float b = (rgb & 255) / 255.0F;

    this.setColor(r, g, b);
  }

  public enum Mode {
    RAINBOW
  }

  @Environment(EnvType.CLIENT)
  public static class RainbowFactory implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;

    public RainbowFactory(
        SpriteSet spriteSet
    ) {
      this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(
        SimpleParticleType type,
        @NonNull ClientLevel level,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ,
        @NonNull RandomSource random
    ) {
      return new UniverseSonicBoomParticle(
          level, x, y, z,
          velocityX,
          this.spriteSet,
          Mode.RAINBOW
      );
    }

  }

}
