/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Minecraft
import net.minecraft.core.particles.SimpleParticleType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.LogicHelper;

/*
 * Supreme Group: Particle
 * Universe Group: Particle
 */
public final class MagnaParticles {

  // Universe Group: Particle
  public static final SimpleParticleType UNIVERSE_SONIC_BOOM =
      LogicHelper.registerSimpleParticle("universe_sonic_boom");

  private MagnaParticles() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaParticles'");
  }

}
