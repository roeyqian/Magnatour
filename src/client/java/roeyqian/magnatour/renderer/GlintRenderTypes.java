/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.mixin.item.RenderTypeInvoker;

@Environment(EnvType.CLIENT)
public final class GlintRenderTypes {

  public static final Identifier RAINBOW_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/misc/universe_glint.png"
  );
  public static final Identifier SUPREME_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/misc/supreme_glint.png"
  );

  public static final RenderType ARMOR_ENTITY_GLINT = createArmorGlint("universe", RAINBOW_GLINT_TEXTURE);
  public static final RenderType SUPREME_ARMOR_ENTITY_GLINT = createArmorGlint("supreme", SUPREME_GLINT_TEXTURE);

  private static final RenderType[] SUPREME_ITEM_GLINT = createItemGlintSet("supreme", SUPREME_GLINT_TEXTURE);
  private static final RenderType[] UNIVERSE_ITEM_GLINT = createItemGlintSet("universe", RAINBOW_GLINT_TEXTURE);

  private GlintRenderTypes() {}

  public static RenderType itemGlint(
      RenderType vanillaType,
      boolean supreme
  ) {
    int variant = itemGlintVariant(vanillaType);
    return variant < 0 ? vanillaType : (supreme ? SUPREME_ITEM_GLINT : UNIVERSE_ITEM_GLINT)[variant];
  }

  private static RenderType createArmorGlint(
      String name,
      Identifier texture
  ) {
    return RenderTypeInvoker.magnatour$create(
        name + "_armor_entity_glint",
        RenderSetup.builder(RenderPipelines.GLINT)
            .withTexture("Sampler0", texture)
            .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    );
  }

  private static RenderType[] createItemGlintSet(
      String name,
      Identifier glintTexture
  ) {
    RenderType[] types = new RenderType[8];
    for (int i = 0; i < types.length; i++) {
      boolean blockAtlas = i < 4;
      boolean translucent = (i % 4) >= 2;
      boolean special = (i % 2) != 0;
      Identifier atlas = blockAtlas ? Sheets.BLOCKS_MAPPER.sheet() : Sheets.ITEMS_MAPPER.sheet();
      var pipeline = translucent
          ? special ? RenderPipelines.ITEM_TRANSLUCENT_GLINT_SPECIAL : RenderPipelines.ITEM_TRANSLUCENT_GLINT
          : special ? RenderPipelines.ITEM_CUTOUT_GLINT_SPECIAL : RenderPipelines.ITEM_CUTOUT_GLINT;
      RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline)
          .withTexture("Sampler0", atlas)
          .withTexture("GlintSampler", glintTexture)
          .setTextureTransform(TextureTransform.GLINT_TEXTURING)
          .useLightmap()
          .useOverlay()
          .affectsCrumbling()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
      if (translucent) {
        setup.setOitPipelines(special ? RenderPipelines.OIT_ITEM_GLINT_SPECIAL : RenderPipelines.OIT_ITEM_GLINT)
            .sortOnUpload();
      }
      types[i] = RenderTypeInvoker.magnatour$create(
          name + "_item_glint_" + i, setup.createRenderSetup()
      );
    }
    return types;
  }

  private static int itemGlintVariant(
      RenderType type
  ) {
    if (type == Sheets.cutoutBlockItemGlintSheet()) return 0;
    if (type == Sheets.cutoutBlockItemGlintSpecialSheet()) return 1;
    if (type == Sheets.translucentBlockItemGlintSheet()) return 2;
    if (type == Sheets.translucentBlockItemGlintSpecialSheet()) return 3;
    if (type == Sheets.cutoutItemGlintSheet()) return 4;
    if (type == Sheets.cutoutItemGlintSpecialSheet()) return 5;
    if (type == Sheets.translucentItemGlintSheet()) return 6;
    if (type == Sheets.translucentItemGlintSpecialSheet()) return 7;
    return -1;
  }

}
