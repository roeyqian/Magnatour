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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

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
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.content.SupremeBlockEntities;
import roeyqian.magnatour.registry.content.UniverseBlockEntities;
import roeyqian.magnatour.registry.content.SupremeItems;
import roeyqian.magnatour.registry.content.UniverseItems;
import roeyqian.magnatour.registry.content.SupremeMenus;
import roeyqian.magnatour.registry.content.UniverseMenus;
import roeyqian.magnatour.registry.content.SupremeEntities;
import roeyqian.magnatour.registry.content.UniverseLiveEntities;
import roeyqian.magnatour.registry.logic.CustomComponents;
import roeyqian.magnatour.registry.logic.CustomNetworks;
import roeyqian.magnatour.registry.logic.CustomRecipes;
import roeyqian.magnatour.registry.worldgen.CustomBiomeSources;
import roeyqian.magnatour.registry.worldgen.CustomChunkGenerators;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;
import roeyqian.magnatour.registry.worldgen.CustomFeatures;
import roeyqian.magnatour.registry.worldgen.CustomStructures;
import roeyqian.magnatour.registry.logic.CustomParticles;

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
    SupremeItems.init();
    UniverseItems.init();
    CustomNetworks.init();

    SupremeBlocks.init();
    UniverseBlocks.init();
    SupremeBlockEntities.init();
    UniverseBlockEntities.init();

    SupremeEntities.init();
    UniverseLiveEntities.init();

    SupremeMenus.init();
    UniverseMenus.init();

    CustomRecipes.init();
    CustomParticles.init();
    CustomComponents.init();

    CustomFeatures.init();
    CustomDimensions.init();
    CustomBiomeSources.init();
    CustomChunkGenerators.init();
    CustomStructures.init();

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
            .icon(() -> new ItemStack(SupremeItems.SUPREME_CORE))
            .displayItems((_, entries) -> {
              entries.accept(SupremeItems.SUPREME_CORE);
              entries.accept(SupremeItems.HARVEST_CORE);
              entries.accept(SupremeItems.ORE_CORE);
              entries.accept(SupremeItems.SEED_OF_ALL_THINGS);
              entries.accept(SupremeItems.FRUIT_OF_ALL_THINGS);
              entries.accept(SupremeItems.SUPREME_BANQUET);
              entries.accept(SupremeItems.SUPREME_CRYSTAL);
              entries.accept(SupremeItems.SUPREME_METAL);
              entries.accept(SupremeItems.RAINBOW_THING);
              entries.accept(SupremeItems.SUPREME_SWORD);
              entries.accept(SupremeItems.SUPREME_AXE);
              entries.accept(SupremeItems.SUPREME_PICKAXE);
              entries.accept(SupremeItems.SUPREME_SHOVEL);
              entries.accept(SupremeItems.SUPREME_HOE);
              entries.accept(SupremeItems.SUPREME_HELMET);
              entries.accept(SupremeItems.SUPREME_CHESTPLATE);
              entries.accept(SupremeItems.SUPREME_LEGGINGS);
              entries.accept(SupremeItems.SUPREME_BOOTS);
              entries.accept(SupremeItems.SUPREME_MOBILE);
              entries.accept(SupremeItems.STRANGE_MATTER);
              entries.accept(SupremeItems.STRANGE_POTION);
              entries.accept(SupremeItems.STRANGE_SPLASH_POTION);
              entries.accept(SupremeItems.STRANGE_LINGERING_POTION);
              entries.accept(SupremeItems.WITHER_SPAWN_EGG);
              entries.accept(SupremeItems.ENDER_DRAGON_SPAWN_EGG);
              entries.accept(SupremeItems.SCULK_BEHEMOTH_SPAWN_EGG);
              entries.accept(SupremeItems.PALE_LORD_SPAWN_EGG);
              entries.accept(SupremeItems.THE_UNNAMEABLE_EGG);
              entries.accept(SupremeItems.BELL_RINGER_SPAWN_EGG);
              entries.accept(SupremeItems.BELL_SOUL_SPAWN_EGG);
              entries.accept(SupremeItems.OBSIDIAN_GOLEM_SPAWN_EGG);
              entries.accept(SupremeItems.NETHERITE_GOLEM_SPAWN_EGG);
            })
            .build()
    );
  }

  private void registerSupremeBlockTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, SUPREME_BLOCK_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.supreme_block"))
            .icon(() -> new ItemStack(SupremeBlocks.SUPREME_BLOCK))
            .displayItems((_, entries) -> {
              entries.accept(SupremeBlocks.EVER_WATER_GRASS_BLOCK);
              entries.accept(SupremeBlocks.GOLDEN_GRASS_BLOCK);
              entries.accept(SupremeBlocks.EVER_WATER_SOIL);
              entries.accept(SupremeBlocks.EVER_WATER_FARMLAND);
              entries.accept(SupremeBlocks.GOLDEN_LOG);
              entries.accept(SupremeBlocks.STRIPPED_GOLDEN_LOG);
              entries.accept(SupremeBlocks.GOLDEN_WOOD);
              entries.accept(SupremeBlocks.STRIPPED_GOLDEN_WOOD);
              entries.accept(SupremeBlocks.GOLDEN_PLANKS);
              entries.accept(SupremeBlocks.GOLDEN_LEAVES);
              entries.accept(SupremeBlocks.GOLDEN_SAPLING);
              entries.accept(SupremeBlocks.SUPREME_GEM_BLOCK);
              entries.accept(SupremeBlocks.SUPREME_FODDER_BLOCK);
              entries.accept(SupremeBlocks.CHUNK_TNT);
              entries.accept(SupremeBlocks.SUPREME_PUMPKIN_HEAD);
              entries.accept(SupremeBlocks.SUPREME_BLOCK);
              entries.accept(SupremeBlocks.SUPREME_WORKTABLE);
              entries.accept(SupremeBlocks.SUPREME_FURNACE);
              entries.accept(SupremeBlocks.SUPREME_RESERVER);
              entries.accept(SupremeBlocks.SUPREME_CHEST);
              entries.accept(SupremeBlocks.REDSTONE_TRIGGER);
              entries.accept(SupremeBlocks.ITEM_HUB);
              entries.accept(SupremeBlocks.LOGISTICS_FIBER);
            })
            .build()
    );
  }

  private void registerUniverseItemTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, UNIVERSE_ITEM_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.universe_item"))
            .icon(() -> new ItemStack(UniverseItems.UNIVERSE_STAR))
            .displayItems((_, entries) -> {
              entries.accept(UniverseItems.UNIVERSE_GEMRED);
              entries.accept(UniverseItems.UNIVERSE_GEMBLUE);
              entries.accept(UniverseItems.UNIVERSE_GEMYELLOW);
              entries.accept(UniverseItems.UNIVERSE_GEMGREEN);
              entries.accept(UniverseItems.UNIVERSE_GEMBLACK);
              entries.accept(UniverseItems.UNIVERSE_GEMWHITE);
              entries.accept(UniverseItems.UNIVERSE_LIGHT);
              entries.accept(UniverseItems.UNIVERSE_DARK);
              entries.accept(UniverseItems.UNIVERSE_PRIMARY_FRAGMENT);
              entries.accept(UniverseItems.UNIVERSE_STAR);
              entries.accept(UniverseItems.UNIVERSE_STICK);
              entries.accept(UniverseItems.UNIVERSE_ULTIMA_SWORD);
              entries.accept(UniverseItems.UNIVERSE_OMNI_BLADE);
              entries.accept(UniverseItems.UNIVERSE_CONSOLE);
              entries.accept(UniverseItems.UNIVERSE_HELMET);
              entries.accept(UniverseItems.UNIVERSE_CHESTPLATE);
              entries.accept(UniverseItems.UNIVERSE_LEGGINGS);
              entries.accept(UniverseItems.UNIVERSE_BOOTS);
              entries.accept(UniverseItems.UNIVERSE_BUCKET);
              entries.accept(UniverseItems.UNIVERSE_BANQUET);
              entries.accept(UniverseItems.UNIVERSE_GUARDIAN_SPAWN_EGG);
            })
            .build()
    );
  }

  private void registerUniverseBlockTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, UNIVERSE_BLOCK_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.universe_block"))
            .icon(() -> new ItemStack(UniverseBlocks.UNIVERSE_BLOCK))
            .displayItems((_, entries) -> {
              entries.accept(UniverseBlocks.UNIVERSE_LIGHT_BLOCK);
              entries.accept(UniverseBlocks.UNIVERSE_DARK_BLOCK);
              entries.accept(UniverseBlocks.UNIVERSE_LIGHT_AIR);
              entries.accept(UniverseBlocks.UNIVERSE_DARK_AIR);
              entries.accept(UniverseBlocks.UNIVERSE_LOG);
              entries.accept(UniverseBlocks.STRIPPED_UNIVERSE_LOG);
              entries.accept(UniverseBlocks.UNIVERSE_WOOD);
              entries.accept(UniverseBlocks.STRIPPED_UNIVERSE_WOOD);
              entries.accept(UniverseBlocks.UNIVERSE_PLANKS);
              entries.accept(UniverseBlocks.UNIVERSE_LEAVES);
              entries.accept(UniverseBlocks.UNIVERSE_SAPLING);
              entries.accept(UniverseBlocks.UNIVERSE_PRIMARY_BLOCK);
              entries.accept(UniverseBlocks.UNIVERSE_BLOCK);
              entries.accept(UniverseBlocks.UNIVERSE_WORKSTATION);
              entries.accept(UniverseBlocks.UNIVERSE_REFINERY);
              entries.accept(UniverseBlocks.UNIVERSE_VOID_POOL);
              entries.accept(UniverseBlocks.UNIVERSE_LIBRARY);
              entries.accept(UniverseBlocks.UNIVERSE_TELEPORT_POINT);
              entries.accept(Blocks.COMMAND_BLOCK);
              entries.accept(Blocks.STRUCTURE_BLOCK);
              entries.accept(Blocks.JIGSAW);
              entries.accept(Blocks.BARRIER);
            })
            .build()
    );
  }

}
