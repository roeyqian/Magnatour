/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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

  private static final RenderType SUPREME_ITEM_GLINT = createArmorGlint("supreme_item", SUPREME_GLINT_TEXTURE);
  private static final RenderType UNIVERSE_ITEM_GLINT = createArmorGlint("universe_item", UNIVERSE_GLINT_TEXTURE);

  private GlintRenderTypes() {}

  public static RenderType itemArmorGlint(
      boolean supreme
  ) {
    return supreme ? SUPREME_ITEM_GLINT : UNIVERSE_ITEM_GLINT;
  }

  public static RenderType itemGlint(
      RenderType vanillaType,
      boolean supreme
  ) {
    int variant = itemGlintVariant(vanillaType);
    if (variant < 0) return vanillaType;
    Identifier atlas = variant < 4 ? Sheets.BLOCKS_MAPPER.sheet() : Sheets.ITEMS_MAPPER.sheet();
    // Use the armor base pass as well as its glint pass: both apply the same
    // view offset, and the cutout surface supplies the depth mask for GLINT.
    return RenderTypes.armorCutoutNoCull(atlas);
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
