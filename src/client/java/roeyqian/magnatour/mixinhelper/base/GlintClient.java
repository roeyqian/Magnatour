/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.base;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.mixin.renderer.ItemStackRenderStateAccessor;
import roeyqian.magnatour.mixin.renderer.RenderTypeInvoker;
import roeyqian.magnatour.registry.logic.MagnaComponents;

@Environment(EnvType.CLIENT)
public final class GlintClient {

  private GlintClient() {}

  public static void armFoilSubmit(
      ItemFeatureRenderer.Submit submit
  ) {
    UniverseGlintBridge.armFoil(((UniverseGlintHolder) (Object) submit).universeGlintType());
  }

  public static void armGlintBridge(
      Object layer
  ) {
    UniverseGlintBridge.arm(((UniverseGlintHolder) layer).universeGlintType());
  }

  public static void clearGlint(
      UniverseGlintHolder holder
  ) {
    holder.setUniverseGlint(GlintType.NONE);
  }

  public static GlintType currentFoilGlint() {
    return UniverseGlintBridge.currentFoil();
  }

  public static void disarmFoilSubmit() {
    UniverseGlintBridge.armFoil(GlintType.NONE);
  }

  public static void disarmGlintBridge() {
    UniverseGlintBridge.arm(GlintType.NONE);
  }

  public static RenderType glintRenderType(
      RenderType baseRenderType,
      GlintType glintType
  ) {
    return glintType == GlintType.NONE
        ? baseRenderType
        : RenderTypes.itemGlint(baseRenderType, glintType == GlintType.SUPREME);
  }

  public static void markGlint(
      ItemStackRenderState output,
      GlintType glintType
  ) {
    ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) output;
    int count = accessor.getActiveLayerCount();
    ItemStackRenderState.LayerRenderState[] layers = accessor.getLayers();

    for (int i = 0; i < count; i++) {
      ItemStackRenderState.LayerRenderState layer = layers[i];
      ((UniverseGlintHolder) layer).setUniverseGlint(glintType);
      layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
    }

    output.setAnimated();
  }

  public static void markGlint(
      ItemStackRenderState output,
      ItemStack itemStack
  ) {
    GlintType glintType = glintType(itemStack);
    if (glintType == GlintType.NONE) return;

    markGlint(output, glintType);
  }

  public static void markLastSubmit(
      ItemFeatureRenderer.Submit submit
  ) {
    GlintType glintType = UniverseGlintBridge.pending();
    if (glintType == GlintType.NONE || submit.foilType() == ItemStackRenderState.FoilType.NONE) {
      return;
    }
    ((UniverseGlintHolder) (Object) submit).setUniverseGlint(glintType);
  }

  public static <S> void submitArmorGlint(
      Model<? super S> model,
      S state,
      ItemStack itemStack,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int lightCoords, int outlineColor
  ) {
    GlintType glintType = glintType(itemStack);
    if (glintType == GlintType.NONE) {
      return;
    }

    submitNodeCollector.submitModel(
        model,
        state,
        poseStack,
        armorGlintRenderType(glintType),
        lightCoords,
        OverlayTexture.NO_OVERLAY,
        -1,
        null,
        outlineColor
    );
  }

  private static GlintType glintType(
      ItemStack itemStack
  ) {
    if (Boolean.TRUE.equals(itemStack.get(MagnaComponents.UNIVERSE_GLINT_OVERRIDE))) {
      return GlintType.UNIVERSE;
    }
    if (Boolean.TRUE.equals(itemStack.get(MagnaComponents.SUPREME_GLINT_OVERRIDE))) {
      return GlintType.SUPREME;
    }
    return GlintType.NONE;
  }

  private static RenderType armorGlintRenderType(
      GlintType glintType
  ) {
    if (glintType == GlintType.SUPREME) {
      return RenderTypes.SUPREME_ARMOR_ENTITY_GLINT;
    }
    return RenderTypes.ARMOR_ENTITY_GLINT;
  }

  public enum GlintType {
    NONE,
    UNIVERSE,
    SUPREME
  }

  @Environment(EnvType.CLIENT)
  public static final class RenderTypes {

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

    private RenderTypes() {}

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
      return net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(atlas);
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

  public static final class UniverseGlintBridge {

    private static GlintType currentFoil = GlintType.NONE;
    private static GlintType pending = GlintType.NONE;

    private UniverseGlintBridge() {}

    public static void arm(
        GlintType value
    ) {
      pending = value;
    }

    public static void armFoil(
        GlintType value
    ) {
      currentFoil = value;
    }

    public static GlintType currentFoil() {
      return currentFoil;
    }

    public static GlintType pending() {
      return pending;
    }

  }

  public interface UniverseGlintHolder {

    void setUniverseGlint(
        GlintType glintType
    );

    GlintType universeGlintType();

  }

}
