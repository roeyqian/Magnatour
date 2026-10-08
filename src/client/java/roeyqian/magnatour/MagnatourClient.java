/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Magnatour - Copyright (C) 2026 Roey Qian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Magnatour
import roeyqian.magnatour.block.VirtualBlockLightManager;
import roeyqian.magnatour.registry.input.RegKeyBindings;
import roeyqian.magnatour.registry.input.RegUniverseBootsFlashing;
import roeyqian.magnatour.registry.output.RegBlockLayers;
import roeyqian.magnatour.registry.output.RegEntityLayers;
import roeyqian.magnatour.registry.output.RegItemTooltips;
import roeyqian.magnatour.registry.output.RegParticles;
import roeyqian.magnatour.registry.output.RegScreens;
import roeyqian.magnatour.screen.supreme.StarAtlasScreen;

@Environment(EnvType.CLIENT)
public class MagnatourClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    ClientPlayNetworking.registerGlobalReceiver(VirtualBlockLightManager.UniverseLightPayload.ID, (payload, context) -> {
      if (context.client().level != null) VirtualBlockLightManager.receiveUniverseState(context.client().level, payload);
    });
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.level != null) {
        VirtualBlockLightManager.tick(client.level);
      }
    });
    ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> VirtualBlockLightManager.onChunkLoad(world, chunk.getPos()));
    ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> VirtualBlockLightManager.onChunkUnload(world, chunk.getPos()));
    RegScreens.init();
    StarAtlasScreen.initClient();
    RegParticles.init();
    RegEntityLayers.init();
    RegBlockLayers.init();
    RegItemTooltips.init();
    RegKeyBindings.init();
    RegUniverseBootsFlashing.init();
  }

}
