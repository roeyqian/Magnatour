/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.output;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

// Minecraft
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.resources.Identifier;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.model.supreme.BellRingerModel;
import roeyqian.magnatour.model.supreme.BellSoulModel;
import roeyqian.magnatour.model.supreme.CustomGolemModel;
import roeyqian.magnatour.model.supreme.PaleLordModel;
import roeyqian.magnatour.model.supreme.SupremeChestModel;
import roeyqian.magnatour.model.supreme.SculkBehemothModel;
import roeyqian.magnatour.model.supreme.TheUnnameableThingModel;
import roeyqian.magnatour.model.universe.UniverseGuardianModel;
import roeyqian.magnatour.registry.content.SupremeEntities;
import roeyqian.magnatour.renderer.supreme.BellRingerRenderer;
import roeyqian.magnatour.renderer.supreme.BellSoulRenderer;
import roeyqian.magnatour.renderer.supreme.NetheriteGolemRenderer;
import roeyqian.magnatour.renderer.supreme.PaleLordRenderer;
import roeyqian.magnatour.renderer.supreme.SculkBehemothRenderer;
import roeyqian.magnatour.renderer.supreme.TheUnnameableThingRenderer;
import roeyqian.magnatour.renderer.universe.UniverseGuardianRenderer;
import roeyqian.magnatour.renderer.supreme.ObsidianGolemRenderer;
import roeyqian.magnatour.registry.content.UniverseLiveEntities;

@Environment(EnvType.CLIENT)
public final class RegEntityLayers {

  public static final ModelLayerLocation BELL_RINGER = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "bell_ringer"), "main"
  );
  public static final ModelLayerLocation BELL_SOUL = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "bell_soul"), "main"
  );
  public static final ModelLayerLocation NETHERITE_GOLEM = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "netherite_golem"), "main"
  );
  public static final ModelLayerLocation OBSIDIAN_GOLEM = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "obsidian_golem"), "main"
  );
  public static final ModelLayerLocation PALE_LORD = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "pale_lord"), "main"
  );
  public static final ModelLayerLocation SCULK_BEHEMOTH = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "sculk_behemoth"), "main"
  );
  public static final ModelLayerLocation SUPREME_CHEST_TRIPLE_MIDDLE = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_chest_triple_middle"), "main"
  );
  public static final ModelLayerLocation THE_UNNAMEABLE_THING = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "the_unnameable_thing"), "main"
  );
  public static final ModelLayerLocation UNIVERSE_GUARDIAN = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_guardian"), "main"
  );

  private RegEntityLayers() {}

  public static void init() {
    ModelLayerRegistry.registerModelLayer(SCULK_BEHEMOTH, SculkBehemothModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(BELL_RINGER, BellRingerModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(UNIVERSE_GUARDIAN, UniverseGuardianModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(THE_UNNAMEABLE_THING, TheUnnameableThingModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(PALE_LORD, PaleLordModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(BELL_SOUL, BellSoulModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(OBSIDIAN_GOLEM, CustomGolemModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(NETHERITE_GOLEM, CustomGolemModel::createBodyLayer);
    ModelLayerRegistry.registerModelLayer(SUPREME_CHEST_TRIPLE_MIDDLE, SupremeChestModel::createTripleMiddleBodyLayer);

    EntityRenderers.register(SupremeEntities.SCULK_BEHEMOTH, SculkBehemothRenderer::new);
    EntityRenderers.register(SupremeEntities.BELL_RINGER, BellRingerRenderer::new);
    EntityRenderers.register(UniverseLiveEntities.UNIVERSE_GUARDIAN, UniverseGuardianRenderer::new);
    EntityRenderers.register(SupremeEntities.THE_UNNAMEABLE_THING, TheUnnameableThingRenderer::new);
    EntityRenderers.register(SupremeEntities.PALE_LORD, PaleLordRenderer::new);
    EntityRenderers.register(SupremeEntities.PALE_LORD_CLONE, PaleLordRenderer::new);
    EntityRenderers.register(SupremeEntities.BELL_SOUL, BellSoulRenderer::new);
    EntityRenderers.register(SupremeEntities.OBSIDIAN_GOLEM, ObsidianGolemRenderer::new);
    EntityRenderers.register(SupremeEntities.NETHERITE_GOLEM, NetheriteGolemRenderer::new);

    registerTntLike(SupremeEntities.PRIMED_CHUNK_TNT);

    Magnatour.LOGGER.info("[Client] Initializing 'RegEntityLayers'");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void registerTntLike(
      net.minecraft.world.entity.EntityType type
  ) {
    EntityRenderers.register(type, ctx -> new TntRenderer(ctx));
  }

}
