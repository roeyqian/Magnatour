/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer;

// Mojang
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.oit.OitPipelineSet;
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

  public static final Identifier SUPREME_ARMOR_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/misc/supreme_glint_armor.png"
  );
  public static final Identifier SUPREME_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/misc/supreme_glint.png"
  );
  public static final Identifier UNIVERSE_ARMOR_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/misc/universe_glint_armor.png"
  );
  public static final Identifier UNIVERSE_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/misc/universe_glint.png"
  );

  public static final RenderType ARMOR_ENTITY_GLINT = createArmorGlint("universe", UNIVERSE_ARMOR_GLINT_TEXTURE);
  public static final RenderType SUPREME_ARMOR_ENTITY_GLINT = createArmorGlint("supreme", SUPREME_ARMOR_GLINT_TEXTURE);
  public static final RenderType UNIVERSE_GUARDIAN_GLINT = createGuardianGlint();

  private static final RenderType[] SUPREME_ITEM_GLINT = createItemGlintSet("supreme", SUPREME_GLINT_TEXTURE);
  private static final RenderType[] UNIVERSE_ITEM_GLINT = createItemGlintSet("universe", UNIVERSE_GLINT_TEXTURE);

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
    // Share pipelines between the block and item atlases. Custom items need
    // their own pipeline identity, not a new RenderType around a vanilla pipeline.
    RenderPipeline[] itemPipelines = new RenderPipeline[4];
    OitPipelineSet[] itemOitPipelines = new OitPipelineSet[2];
    for (int i = 0; i < types.length; i++) {
      boolean blockAtlas = i < 4;
      boolean translucent = (i % 4) >= 2;
      boolean special = (i % 2) != 0;
      Identifier atlas = blockAtlas ? Sheets.BLOCKS_MAPPER.sheet() : Sheets.ITEMS_MAPPER.sheet();
      int variant = i % 4;
      if (itemPipelines[variant] == null) {
        itemPipelines[variant] = createCustomItemPipeline(name, translucent, special);
      }
      RenderPipeline pipeline = itemPipelines[variant];
      RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline)
          .withTexture("Sampler0", atlas)
          .withTexture("GlintSampler", glintTexture)
          .setTextureTransform(TextureTransform.GLINT_TEXTURING)
          .useLightmap()
          .useOverlay()
          .affectsCrumbling()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
      if (translucent) {
        int oitVariant = special ? 1 : 0;
        if (itemOitPipelines[oitVariant] == null) {
          itemOitPipelines[oitVariant] = RenderPipelines.register(OitPipelineSet.builder(
              "magnatour_" + name + "_item_glint" + (special ? "_special" : ""),
              customItemPipelineBuilder(special)
          ).build());
        }
        setup.setOitPipelines(itemOitPipelines[oitVariant]).sortOnUpload();
      }
      types[i] = RenderTypeInvoker.magnatour$create(
          name + "_item_glint_" + i, setup.createRenderSetup()
      );
    }
    return types;
  }

  private static RenderType createGuardianGlint() {
    return RenderTypeInvoker.magnatour$create(
        "universe_guardian_glint",
        RenderSetup.builder(RenderPipelines.GLINT)
            .withTexture("Sampler0", UNIVERSE_ARMOR_GLINT_TEXTURE)
            .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
            // GLINT uses EQUAL depth testing. Match the unshifted entity base pass.
            .withForcedSolidModelPhase()
            .createRenderSetup()
    );
  }

  private static RenderPipeline.Builder customItemPipelineBuilder(
      boolean special
  ) {
    Identifier shader = Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "core/universe_item_glint");
    return RenderPipeline.builder(
        RenderPipelines.ITEM_SNIPPET,
        special ? RenderPipelines.GLINT_SPECIAL_SNIPPET : RenderPipelines.GLINT_SNIPPET
    )
        .withVertexShader(shader)
        .withFragmentShader(shader)
        .withVertexBinding(0, special ? UniverseItemVertexFormats.ITEM_SPECIAL : UniverseItemVertexFormats.ITEM)
        .withShaderDefine("ALPHA_CUTOUT", 0.1F);
  }

  private static RenderPipeline createCustomItemPipeline(
      String name,
      boolean translucent, boolean special
  ) {
    String variant = (translucent ? "translucent" : "cutout") + (special ? "_special" : "");
    return RenderPipelines.register(customItemPipelineBuilder(special)
        .withLocation(Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "pipeline/" + name + "_item_glint_" + variant))
        .withColorTargetState(translucent ? new ColorTargetState(BlendFunction.TRANSLUCENT) : ColorTargetState.DEFAULT)
        .build());
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
