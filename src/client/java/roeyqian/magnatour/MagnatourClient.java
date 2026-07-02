/*
 * Magnatour - Copyright (C) 2026 Roey Qian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package roeyqian.magnatour;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ClientModInitializer;

// Magnatour
import roeyqian.magnatour.utility.registry.input.RegKeyBindings;
import roeyqian.magnatour.utility.registry.output.RegEntityLayers;
import roeyqian.magnatour.utility.registry.output.RegBlockLayers;
import roeyqian.magnatour.utility.registry.output.RegParticles;
import roeyqian.magnatour.utility.registry.output.RegScreens;
import roeyqian.magnatour.utility.registry.input.RegUniverseBootsFlashing;

@Environment(EnvType.CLIENT)
public class MagnatourClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    RegScreens.init();
    RegParticles.init();
    RegEntityLayers.init();
    RegBlockLayers.init();
    RegKeyBindings.init();
    RegUniverseBootsFlashing.init();
  }

}