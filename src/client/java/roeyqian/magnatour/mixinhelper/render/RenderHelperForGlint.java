/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.render;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

// Magnatour
import roeyqian.magnatour.mixin.render.ItemStackRenderStateAccessor;
import roeyqian.magnatour.renderer.GlintRenderTypes;
import roeyqian.magnatour.registry.logic.RegComponentTypes;

@Environment(EnvType.CLIENT)
public final class RenderHelperForGlint {

  private RenderHelperForGlint() {}

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
    boolean transparent = Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency()
        && baseRenderType.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET;
    if (glintType == GlintType.SUPREME) {
      return transparent ? GlintRenderTypes.SUPREME_GLINT_TRANSLUCENT : GlintRenderTypes.SUPREME_GLINT;
    }
    return transparent ? GlintRenderTypes.GLINT_TRANSLUCENT : GlintRenderTypes.GLINT;
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
    GlintType glintType = UniverseGlintBridge.consume();
    if (glintType == GlintType.NONE) {
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
      int lightCoords,
      int outlineColor
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
        outlineColor,
        null
    );
  }

  private static GlintType glintType(
      ItemStack itemStack
  ) {
    if (Boolean.TRUE.equals(itemStack.get(RegComponentTypes.UNIVERSE_GLINT_OVERRIDE))) {
      return GlintType.UNIVERSE;
    }
    if (Boolean.TRUE.equals(itemStack.get(RegComponentTypes.SUPREME_GLINT_OVERRIDE))) {
      return GlintType.SUPREME;
    }
    return GlintType.NONE;
  }

  private static RenderType armorGlintRenderType(
      GlintType glintType
  ) {
    if (glintType == GlintType.SUPREME) {
      return GlintRenderTypes.SUPREME_ARMOR_ENTITY_GLINT;
    }
    return GlintRenderTypes.ARMOR_ENTITY_GLINT;
  }

  public enum GlintType {
    NONE,
    UNIVERSE,
    SUPREME
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

    public static GlintType consume() {
      GlintType value = pending;
      pending = GlintType.NONE;
      return value;
    }

    public static GlintType currentFoil() {
      return currentFoil;
    }

  }

  public interface UniverseGlintHolder {

    void setUniverseGlint(
        GlintType glintType
    );

    GlintType universeGlintType();

  }

}
