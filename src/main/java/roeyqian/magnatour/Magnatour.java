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
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;
import roeyqian.magnatour.utility.registry.block.RegActiveBlocks;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.entity.RegLiveEntities;
import roeyqian.magnatour.utility.registry.level.RegBiomeSources;
import roeyqian.magnatour.utility.registry.level.RegChunkGenerators;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;
import roeyqian.magnatour.utility.registry.gen.RegNetworks;
import roeyqian.magnatour.utility.registry.level.RegDimensions;
import roeyqian.magnatour.utility.registry.level.RegFeatures;
import roeyqian.magnatour.utility.registry.level.RegStructures;
import roeyqian.magnatour.utility.registry.gen.RegParticles;
import roeyqian.magnatour.utility.registry.gen.RegRecipes;
import roeyqian.magnatour.utility.registry.item.RegConsumableItems;
import roeyqian.magnatour.utility.registry.item.RegDurableItems;
import roeyqian.magnatour.utility.registry.menu.RegBlockMenus;
import roeyqian.magnatour.utility.registry.menu.RegItemMenus;

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
    RegConsumableItems.init();
    RegDurableItems.init();
    RegNetworks.init();

    RegInsertBlocks.init();
    RegActiveBlocks.init();
    RegBlockEntities.init();

    RegLiveEntities.init();

    RegItemMenus.init();
    RegBlockMenus.init();

    RegRecipes.init();
    RegParticles.init();
    RegComponentTypes.init();

    RegFeatures.init();
    RegDimensions.init();
    RegBiomeSources.init();
    RegChunkGenerators.init();
    RegStructures.init();

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
            .icon(() -> new ItemStack(RegConsumableItems.SUPREME_CORE))
            .displayItems((_, entries) -> {
              entries.accept(RegConsumableItems.SUPREME_CORE);
              entries.accept(RegConsumableItems.HARVEST_CORE);
              entries.accept(RegConsumableItems.ORE_CORE);
              entries.accept(RegConsumableItems.SEED_OF_ALL_THINGS);
              entries.accept(RegConsumableItems.FRUIT_OF_ALL_THINGS);
              entries.accept(RegConsumableItems.SUPREME_BANQUET);
              entries.accept(RegConsumableItems.SUPREME_CRYSTAL);
              entries.accept(RegConsumableItems.SUPREME_METAL);
              entries.accept(RegConsumableItems.RAINBOW_THING);
              entries.accept(RegDurableItems.SUPREME_SWORD);
              entries.accept(RegDurableItems.SUPREME_AXE);
              entries.accept(RegDurableItems.SUPREME_PICKAXE);
              entries.accept(RegDurableItems.SUPREME_SHOVEL);
              entries.accept(RegDurableItems.SUPREME_HOE);
              entries.accept(RegDurableItems.SUPREME_HELMET);
              entries.accept(RegDurableItems.SUPREME_CHESTPLATE);
              entries.accept(RegDurableItems.SUPREME_LEGGINGS);
              entries.accept(RegDurableItems.SUPREME_BOOTS);
              entries.accept(RegDurableItems.SUPREME_MOBILE);
              entries.accept(RegConsumableItems.STRANGE_MATTER);
              entries.accept(RegConsumableItems.STRANGE_POTION);
              entries.accept(RegConsumableItems.STRANGE_SPLASH_POTION);
              entries.accept(RegConsumableItems.STRANGE_LINGERING_POTION);
              entries.accept(RegConsumableItems.WITHER_SPAWN_EGG);
              entries.accept(RegConsumableItems.ENDER_DRAGON_SPAWN_EGG);
              entries.accept(RegConsumableItems.SCULK_BEHEMOTH_SPAWN_EGG);
              entries.accept(RegConsumableItems.PALE_LORD_SPAWN_EGG);
              entries.accept(RegConsumableItems.THE_UNNAMEABLE_EGG);
              entries.accept(RegConsumableItems.BELL_RINGER_SPAWN_EGG);
              entries.accept(RegConsumableItems.BELL_SOUL_SPAWN_EGG);
              entries.accept(RegConsumableItems.OBSIDIAN_GOLEM_SPAWN_EGG);
              entries.accept(RegConsumableItems.NETHERITE_GOLEM_SPAWN_EGG);
            })
            .build()
    );
  }

  private void registerSupremeBlockTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, SUPREME_BLOCK_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.supreme_block"))
            .icon(() -> new ItemStack(RegInsertBlocks.SUPREME_BLOCK))
            .displayItems((_, entries) -> {
              entries.accept(RegInsertBlocks.EVER_WATER_GRASS_BLOCK);
              entries.accept(RegInsertBlocks.GOLDEN_GRASS_BLOCK);
              entries.accept(RegInsertBlocks.EVER_WATER_SOIL);
              entries.accept(RegInsertBlocks.EVER_WATER_FARMLAND);
              entries.accept(RegInsertBlocks.GOLDEN_LOG);
              entries.accept(RegInsertBlocks.STRIPPED_GOLDEN_LOG);
              entries.accept(RegInsertBlocks.GOLDEN_WOOD);
              entries.accept(RegInsertBlocks.STRIPPED_GOLDEN_WOOD);
              entries.accept(RegInsertBlocks.GOLDEN_PLANKS);
              entries.accept(RegInsertBlocks.GOLDEN_LEAVES);
              entries.accept(RegInsertBlocks.GOLDEN_SAPLING);
              entries.accept(RegInsertBlocks.SUPREME_GEM_BLOCK);
              entries.accept(RegInsertBlocks.SUPREME_FODDER_BLOCK);
              entries.accept(RegInsertBlocks.CHUNK_TNT);
              entries.accept(RegInsertBlocks.SUPREME_PUMPKIN_HEAD);
              entries.accept(RegInsertBlocks.SUPREME_BLOCK);
              entries.accept(RegActiveBlocks.SUPREME_WORKTABLE);
              entries.accept(RegActiveBlocks.SUPREME_FURNACE);
              entries.accept(RegActiveBlocks.SUPREME_RESERVER);
              entries.accept(RegActiveBlocks.SUPREME_CHEST);
              entries.accept(RegActiveBlocks.REDSTONE_TRIGGER);
              entries.accept(RegActiveBlocks.ITEM_HUB);
              entries.accept(RegInsertBlocks.LOGISTICS_FIBER);
            })
            .build()
    );
  }

  private void registerUniverseItemTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, UNIVERSE_ITEM_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.universe_item"))
            .icon(() -> new ItemStack(RegConsumableItems.UNIVERSE_STAR))
            .displayItems((_, entries) -> {
              entries.accept(RegConsumableItems.UNIVERSE_GEMRED);
              entries.accept(RegConsumableItems.UNIVERSE_GEMBLUE);
              entries.accept(RegConsumableItems.UNIVERSE_GEMYELLOW);
              entries.accept(RegConsumableItems.UNIVERSE_GEMGREEN);
              entries.accept(RegConsumableItems.UNIVERSE_GEMBLACK);
              entries.accept(RegConsumableItems.UNIVERSE_GEMWHITE);
              entries.accept(RegConsumableItems.UNIVERSE_LIGHT);
              entries.accept(RegConsumableItems.UNIVERSE_DARK);
              entries.accept(RegConsumableItems.UNIVERSE_PRIMARY_FRAGMENT);
              entries.accept(RegConsumableItems.UNIVERSE_STAR);
              entries.accept(RegDurableItems.UNIVERSE_STICK);
              entries.accept(RegDurableItems.UNIVERSE_ULTIMA_SWORD);
              entries.accept(RegDurableItems.UNIVERSE_OMNI_BLADE);
              entries.accept(RegDurableItems.UNIVERSE_CONSOLE);
              entries.accept(RegDurableItems.UNIVERSE_HELMET);
              entries.accept(RegDurableItems.UNIVERSE_CHESTPLATE);
              entries.accept(RegDurableItems.UNIVERSE_LEGGINGS);
              entries.accept(RegDurableItems.UNIVERSE_BOOTS);
              entries.accept(RegDurableItems.UNIVERSE_BUCKET);
              entries.accept(RegConsumableItems.UNIVERSE_BANQUET);
              entries.accept(RegConsumableItems.UNIVERSE_GUARDIAN_SPAWN_EGG);
            })
            .build()
    );
  }

  private void registerUniverseBlockTab() {
    Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB, UNIVERSE_BLOCK_KEY, FabricCreativeModeTab
            .builder()
            .title(Component.translatable("group.magnatour.universe_block"))
            .icon(() -> new ItemStack(RegInsertBlocks.UNIVERSE_BLOCK))
            .displayItems((_, entries) -> {
              entries.accept(RegInsertBlocks.UNIVERSE_LIGHT_BLOCK);
              entries.accept(RegInsertBlocks.UNIVERSE_DARK_BLOCK);
              entries.accept(RegInsertBlocks.UNIVERSE_LIGHT_AIR);
              entries.accept(RegInsertBlocks.UNIVERSE_DARK_AIR);
              entries.accept(RegInsertBlocks.UNIVERSE_LOG);
              entries.accept(RegInsertBlocks.STRIPPED_UNIVERSE_LOG);
              entries.accept(RegInsertBlocks.UNIVERSE_WOOD);
              entries.accept(RegInsertBlocks.STRIPPED_UNIVERSE_WOOD);
              entries.accept(RegInsertBlocks.UNIVERSE_PLANKS);
              entries.accept(RegInsertBlocks.UNIVERSE_LEAVES);
              entries.accept(RegInsertBlocks.UNIVERSE_SAPLING);
              entries.accept(RegInsertBlocks.UNIVERSE_PRIMARY_BLOCK);
              entries.accept(RegInsertBlocks.UNIVERSE_BLOCK);
              entries.accept(RegActiveBlocks.UNIVERSE_WORKSTATION);
              entries.accept(RegActiveBlocks.UNIVERSE_REFINERY);
              entries.accept(RegActiveBlocks.UNIVERSE_VOID_POOL);
              entries.accept(RegActiveBlocks.UNIVERSE_LIBRARY);
              entries.accept(RegActiveBlocks.UNIVERSE_TELEPORT_POINT);
              entries.accept(Blocks.COMMAND_BLOCK);
              entries.accept(Blocks.STRUCTURE_BLOCK);
              entries.accept(Blocks.JIGSAW);
              entries.accept(Blocks.BARRIER);
            })
            .build()
    );
  }

}
