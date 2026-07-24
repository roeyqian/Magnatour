/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Minecraft
import net.minecraft.core.particles.SimpleParticleType;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.LogicRegHelper;

/*
 * Supreme Group: Particle
 * Universe Group: Particle
 */
public final class CustomParticles {

  // Universe Group: Particle
  public static final SimpleParticleType UNIVERSE_SONIC_BOOM =
      LogicRegHelper.registerSimpleParticle("universe_sonic_boom");

  private CustomParticles() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'CustomParticles'");
  }

}
