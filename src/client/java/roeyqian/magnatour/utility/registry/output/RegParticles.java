/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.output;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.render.particle.UniverseSonicBoomParticle;

@Environment(EnvType.CLIENT)
public final class RegParticles {

  private RegParticles() {}

  public static void init() {
    ParticleProviderRegistry.getInstance().register(
        roeyqian.magnatour.utility.registry.gen.RegParticles.UNIVERSE_SONIC_BOOM,
        UniverseSonicBoomParticle.RainbowFactory::new
    );

    Magnatour.LOGGER.info("[Client] Initializing 'RegParticles'");
  }

}
