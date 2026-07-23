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
import roeyqian.magnatour.registry.GenRegHelper;

/*
 * Supreme Group: Particle
 * Universe Group: Particle
 */
public final class RegParticles {

  // Universe Group: Particle
  public static final SimpleParticleType UNIVERSE_SONIC_BOOM =
      GenRegHelper.registerSimpleParticle("universe_sonic_boom");

  private RegParticles() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'RegParticles'");
  }

}
