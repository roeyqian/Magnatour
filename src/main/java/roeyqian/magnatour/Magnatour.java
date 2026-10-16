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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

// SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Magnatour
import roeyqian.magnatour.block.VirtualBlockLightManager;
import roeyqian.magnatour.entity.universe.UniverseAnnihilation;
import roeyqian.magnatour.mixinhelper.entity.UniverseInventoryHelper;
import roeyqian.magnatour.registry.logic.MagnaComponents;
import roeyqian.magnatour.registry.logic.MagnaNetworks;
import roeyqian.magnatour.registry.logic.MagnaParticles;
import roeyqian.magnatour.registry.logic.MagnaRecipes;
import roeyqian.magnatour.registry.stuff.MagnaBlockEntities;
import roeyqian.magnatour.registry.stuff.MagnaBlocks;
import roeyqian.magnatour.registry.stuff.MagnaEntities;
import roeyqian.magnatour.registry.stuff.MagnaItems;
import roeyqian.magnatour.registry.stuff.MagnaMenus;
import roeyqian.magnatour.registry.world.MagnaBiomeSources;
import roeyqian.magnatour.registry.world.MagnaChunkGenerators;
import roeyqian.magnatour.registry.world.MagnaDimensions;
import roeyqian.magnatour.registry.world.MagnaFeatures;
import roeyqian.magnatour.registry.world.MagnaStructures;

public class Magnatour implements ModInitializer {

  public static final String MOD_ID = "magnatour";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static final ResourceKey<CreativeModeTab> SUPREME_BLOCK_KEY = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "supreme_block")
  );
  private static final ResourceKey<CreativeModeTab> SUPREME_ITEM_KEY = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "supreme_item")
  );
  private static final ResourceKey<CreativeModeTab> UNIVERSE_BLOCK_KEY = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "universe_block")
  );
  private static final ResourceKey<CreativeModeTab> UNIVERSE_ITEM_KEY = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "universe_item")
  );

  @Override
  public void onInitialize() {
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      for (var player : server.getPlayerList().getPlayers()) {
        UniverseInventoryHelper.dropExcess(player);
      }
    });
    ServerTickEvents.END_SERVER_TICK.register(UniverseAnnihilation::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(UniverseAnnihilation::forget);
    VirtualBlockLightManager.init();
    ServerTickEvents.END_LEVEL_TICK.register(VirtualBlockLightManager::tick);
    ServerChunkEvents.CHUNK_LOAD.register((world, chunk, _) -> VirtualBlockLightManager.onChunkLoad(world, chunk));
    ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> VirtualBlockLightManager.onChunkUnload(world, chunk.getPos()));
    MagnaItems.init();
    MagnaNetworks.init();

    MagnaBlocks.init();
    MagnaBlockEntities.init();

    MagnaEntities.init();

    MagnaMenus.init();

    MagnaRecipes.init();
    MagnaParticles.init();
    MagnaComponents.init();

    MagnaFeatures.init();
    MagnaDimensions.init();
    MagnaBiomeSources.init();
    MagnaChunkGenerators.init();
    MagnaStructures.init();

    registerSupremeItemTab();
    registerSupremeBlockTab();
    registerUniverseItemTab();
    registerUniverseBlockTab();
  }

  private void registerSupremeItemTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, SUPREME_ITEM_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.supreme_item"))
            .icon(() -> new ItemStack(MagnaItems.SUPREME_CORE))
            .displayItems((_, entries) -> {
              entries.accept(MagnaItems.SUPREME_CORE);
              entries.accept(MagnaItems.HARVEST_CORE);
              entries.accept(MagnaItems.ORE_CORE);
              entries.accept(MagnaItems.SEED_OF_ALL_THINGS);
              entries.accept(MagnaItems.FRUIT_OF_ALL_THINGS);
              entries.accept(MagnaItems.GREAT_BANQUET);
              entries.accept(MagnaItems.FORTUNE_METAL);
              entries.accept(MagnaItems.FORTUNE_CRYSTAL);
              entries.accept(MagnaItems.RAINBOW_MIRROR);
              entries.accept(MagnaItems.WITHER_SPAWN_EGG);
              entries.accept(MagnaItems.ENDER_DRAGON_SPAWN_EGG);
              entries.accept(MagnaItems.SCULK_BEHEMOTH_SPAWN_EGG);
              entries.accept(MagnaItems.PALE_LORD_SPAWN_EGG);
              entries.accept(MagnaItems.THE_UNNAMEABLE_EGG);
              entries.accept(MagnaItems.BELL_RINGER_SPAWN_EGG);
              entries.accept(MagnaItems.BELL_SOUL_SPAWN_EGG);
              entries.accept(MagnaItems.OBSIDIAN_GOLEM_SPAWN_EGG);
              entries.accept(MagnaItems.NETHERITE_GOLEM_SPAWN_EGG);
              entries.accept(MagnaItems.EMPEROR_SWORD);
              entries.accept(MagnaItems.EMPEROR_BOW);
              entries.accept(MagnaItems.EMPEROR_AXE_PICKAXE);
              entries.accept(MagnaItems.EMPEROR_SHOVEL);
              entries.accept(MagnaItems.EMPEROR_HOE);
              entries.accept(MagnaItems.EMPEROR_HELMET);
              entries.accept(MagnaItems.EMPEROR_CHESTPLATE);
              entries.accept(MagnaItems.EMPEROR_LEGGINGS);
              entries.accept(MagnaItems.EMPEROR_BOOTS);
              entries.accept(MagnaItems.STRANGE_MATTER);
              entries.accept(MagnaItems.STRANGE_POTION);
              entries.accept(MagnaItems.STRANGE_SPLASH_POTION);
              entries.accept(MagnaItems.STRANGE_LINGERING_POTION);
              entries.accept(MagnaItems.MIRROR_MOBILE);
              entries.accept(MagnaItems.STAR_ATLAS);
            })
            .build()
    );
  }

  private void registerSupremeBlockTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, SUPREME_BLOCK_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.supreme_block"))
            .icon(() -> new ItemStack(MagnaBlocks.SUPREME_BLOCK))
            .displayItems((_, entries) -> {
              entries.accept(MagnaBlocks.GOLDEN_LOG);
              entries.accept(MagnaBlocks.STRIPPED_GOLDEN_LOG);
              entries.accept(MagnaBlocks.GOLDEN_WOOD);
              entries.accept(MagnaBlocks.STRIPPED_GOLDEN_WOOD);
              entries.accept(MagnaBlocks.GOLDEN_PLANKS);
              entries.accept(MagnaBlocks.GOLDEN_STAIRS);
              entries.accept(MagnaBlocks.GOLDEN_SLAB);
              entries.accept(MagnaBlocks.GOLDEN_FENCE);
              entries.accept(MagnaBlocks.GOLDEN_FENCE_GATE);
              entries.accept(MagnaBlocks.GOLDEN_DOOR);
              entries.accept(MagnaBlocks.GOLDEN_TRAPDOOR);
              entries.accept(MagnaBlocks.GOLDEN_PRESSURE_PLATE);
              entries.accept(MagnaBlocks.GOLDEN_BUTTON);
              entries.accept(MagnaBlocks.GOLDEN_SHELF);
              entries.accept(MagnaBlocks.GOLDEN_SIGN_ITEM);
              entries.accept(MagnaBlocks.GOLDEN_HANGING_SIGN_ITEM);
              entries.accept(MagnaBlocks.GOLDEN_LEAVES);
              entries.accept(MagnaBlocks.GOLDEN_SAPLING);
              entries.accept(MagnaBlocks.EVER_WATER_GRASS_BLOCK);
              entries.accept(MagnaBlocks.GOLDEN_GRASS_BLOCK);
              entries.accept(MagnaBlocks.EVER_WATER_SOIL);
              entries.accept(MagnaBlocks.EVER_WATER_FARMLAND);
              entries.accept(MagnaBlocks.ORE_BLOCK);
              entries.accept(MagnaBlocks.HARVEST_BLOCK);
              entries.accept(MagnaBlocks.CHUNK_TNT);
              entries.accept(MagnaBlocks.SUPREME_PUMPKIN_HEAD);
              entries.accept(MagnaBlocks.SUPREME_BLOCK);
              entries.accept(MagnaBlocks.SUPREME_CRAFTABLE);
              entries.accept(MagnaBlocks.SUPREME_FURNACE);
              entries.accept(MagnaBlocks.SUPREME_RESERVER);
              entries.accept(MagnaBlocks.SUPREME_CHEST);
              entries.accept(MagnaBlocks.REDSTONE_TRIGGER);
              entries.accept(MagnaBlocks.ITEM_HUB);
              entries.accept(MagnaBlocks.LOGISTICS_FIBER);
            })
            .build()
    );
  }

  private void registerUniverseItemTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, UNIVERSE_ITEM_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.universe_item"))
            .icon(() -> new ItemStack(MagnaItems.UNIVERSE_CORE))
            .displayItems((_, entries) -> {
              entries.accept(MagnaItems.UNIVERSE_GEMRED);
              entries.accept(MagnaItems.UNIVERSE_GEMBLUE);
              entries.accept(MagnaItems.UNIVERSE_GEMYELLOW);
              entries.accept(MagnaItems.UNIVERSE_GEMGREEN);
              entries.accept(MagnaItems.UNIVERSE_GEMBLACK);
              entries.accept(MagnaItems.UNIVERSE_GEMWHITE);
              entries.accept(MagnaItems.UNIVERSE_LIGHT);
              entries.accept(MagnaItems.UNIVERSE_DARK);
              entries.accept(MagnaItems.UNIVERSE_PRIMARY_FRAGMENT);
              entries.accept(MagnaItems.UNIVERSE_CORE);
              entries.accept(MagnaItems.UNIVERSE_STICK);
              entries.accept(MagnaItems.UNIVERSE_ULTIMA_SWORD);
              entries.accept(MagnaItems.UNIVERSE_OMNI_BLADE);
              entries.accept(MagnaItems.UNIVERSE_COMMAND_STAR);
              entries.accept(MagnaItems.UNIVERSE_VOID_HELMET);
              entries.accept(MagnaItems.UNIVERSE_VOID_CHESTPLATE);
              entries.accept(MagnaItems.UNIVERSE_VOID_LEGGINGS);
              entries.accept(MagnaItems.UNIVERSE_VOID_BOOTS);
              entries.accept(MagnaItems.UNIVERSE_BUCKET);
              entries.accept(MagnaItems.UNIVERSE_BANQUET);
              entries.accept(MagnaItems.UNIVERSE_POTION);
              entries.accept(MagnaItems.UNIVERSE_SPLASH_POTION);
              entries.accept(MagnaItems.UNIVERSE_LINGERING_POTION);
              entries.accept(MagnaItems.UNIVERSE_GUARDIAN_SPAWN_EGG);
              entries.accept(MagnaItems.UNIVERSE_ANNIHILATOR_SPAWN_EGG);
            })
            .build()
    );
  }

  private void registerUniverseBlockTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, UNIVERSE_BLOCK_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.universe_block"))
            .icon(() -> new ItemStack(MagnaBlocks.UNIVERSE_BLOCK))
            .displayItems((_, entries) -> {
              entries.accept(MagnaBlocks.UNIVERSE_LOG);
              entries.accept(MagnaBlocks.STRIPPED_UNIVERSE_LOG);
              entries.accept(MagnaBlocks.UNIVERSE_WOOD);
              entries.accept(MagnaBlocks.STRIPPED_UNIVERSE_WOOD);
              entries.accept(MagnaBlocks.UNIVERSE_PLANKS);
              entries.accept(MagnaBlocks.UNIVERSE_STAIRS);
              entries.accept(MagnaBlocks.UNIVERSE_SLAB);
              entries.accept(MagnaBlocks.UNIVERSE_FENCE);
              entries.accept(MagnaBlocks.UNIVERSE_FENCE_GATE);
              entries.accept(MagnaBlocks.UNIVERSE_DOOR);
              entries.accept(MagnaBlocks.UNIVERSE_TRAPDOOR);
              entries.accept(MagnaBlocks.UNIVERSE_PRESSURE_PLATE);
              entries.accept(MagnaBlocks.UNIVERSE_BUTTON);
              entries.accept(MagnaBlocks.UNIVERSE_SHELF);
              entries.accept(MagnaBlocks.UNIVERSE_SIGN_ITEM);
              entries.accept(MagnaBlocks.UNIVERSE_HANGING_SIGN_ITEM);
              entries.accept(MagnaBlocks.UNIVERSE_LEAVES);
              entries.accept(MagnaBlocks.UNIVERSE_SAPLING);
              entries.accept(MagnaBlocks.UNIVERSE_LIGHT_BLOCK);
              entries.accept(MagnaBlocks.UNIVERSE_DARK_BLOCK);
              entries.accept(MagnaBlocks.UNIVERSE_LIGHT_AIR);
              entries.accept(MagnaBlocks.UNIVERSE_DARK_AIR);
              entries.accept(MagnaBlocks.UNIVERSE_PRIMARY_BLOCK);
              entries.accept(Blocks.COMMAND_BLOCK);
              entries.accept(Blocks.STRUCTURE_BLOCK);
              entries.accept(Blocks.JIGSAW);
              entries.accept(Blocks.BARRIER);
              entries.accept(MagnaBlocks.UNIVERSE_BLOCK);
              entries.accept(MagnaBlocks.UNIVERSE_FORGE);
              entries.accept(MagnaBlocks.UNIVERSE_REFINERY);
              entries.accept(MagnaBlocks.UNIVERSE_POOL);
              entries.accept(MagnaBlocks.UNIVERSE_LIBRARY);
              entries.accept(MagnaBlocks.UNIVERSE_DOT);
            })
            .build()
    );
  }

}
