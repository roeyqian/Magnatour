/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.render;

// Java Standard
import java.util.List;
import java.util.SequencedMap;

// Mojang
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemStack;

// Magnatour
import roeyqian.magnatour.mixin.render.BufferSourceAccessor;
import roeyqian.magnatour.mixin.render.ItemStackRenderStateAccessor;
import roeyqian.magnatour.render.type.GlintRenderTypes;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;

@Environment(EnvType.CLIENT)
public final class RenderHelperForGlint {

  private RenderHelperForGlint() {}

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

  public static void disarmGlintBridge() {
    UniverseGlintBridge.arm(GlintType.NONE);
  }

  public static void markGlint(
      ItemStackRenderState output,
      GlintType glintType
  ) {
    ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) output;
    int count = accessor.getActiveLayerCount();
    ItemStackRenderState.LayerRenderState[] layers = accessor.getLayers();

    for (int i = 0; i < count; i++) {
      ((UniverseGlintHolder) layers[i]).setUniverseGlint(glintType);
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
      List<SubmitNodeStorage.ItemSubmit> itemSubmits
  ) {
    GlintType glintType = UniverseGlintBridge.consume();
    if (glintType == GlintType.NONE || itemSubmits.isEmpty()) {
      return;
    }

    SubmitNodeStorage.ItemSubmit submit = itemSubmits.getLast();
    ((UniverseGlintHolder) (Object) submit).setUniverseGlint(glintType);
  }

  public static void registerGlintBuffers(
      RenderBuffers buffers
  ) {
    MultiBufferSource.BufferSource bufferSource = buffers.bufferSource();
    SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers =
        ((BufferSourceAccessor) bufferSource).getFixedBuffers();

    for (RenderType glintType : GlintRenderTypes.ITEM_GLINT_TYPES) {
      fixedBuffers.putIfAbsent(glintType, new ByteBufferBuilder(glintType.bufferSize()));
    }
  }

  public static void renderRainbowGlint(
      QuadInstance quadInstance,
      MultiBufferSource.BufferSource bufferSource,
      SubmitNodeStorage.ItemSubmit submit
  ) {
    GlintType submitGlintType = ((UniverseGlintHolder) (Object) submit).universeGlintType();
    if (submitGlintType == GlintType.NONE) {
      return;
    }

    quadInstance.setLightCoords(submit.lightCoords());
    quadInstance.setOverlayCoords(submit.overlayCoords());

    for (BakedQuad quad : submit.quads()) {
      BakedQuad.MaterialInfo material = quad.materialInfo();
      RenderType baseRenderType = material.itemRenderType();
      quadInstance.setColor(layerColor(submit.tintLayers(), material));

      RenderType glintType = glintRenderType(baseRenderType, submitGlintType);
      VertexConsumer glintBuffer = bufferSource.getBuffer(glintType);
      glintBuffer.putBakedQuad(submit.pose(), quad, quadInstance);
    }
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

  private static int layerColor(
      int[] tintLayers,
      BakedQuad.MaterialInfo material
  ) {
    if (!material.isTinted()) {
      return -1;
    }
    int idx = material.tintIndex();
    return idx >= 0 && idx < tintLayers.length ? tintLayers[idx] : -1;
  }

  private static RenderType glintRenderType(
      RenderType baseRenderType,
      GlintType glintType
  ) {
    boolean transparent = Minecraft.useShaderTransparency()
        && baseRenderType.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET;
    if (glintType == GlintType.SUPREME) {
      return transparent ? GlintRenderTypes.SUPREME_GLINT_TRANSLUCENT : GlintRenderTypes.SUPREME_GLINT;
    }
    return transparent ? GlintRenderTypes.GLINT_TRANSLUCENT : GlintRenderTypes.GLINT;
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

    private static GlintType pending = GlintType.NONE;

    private UniverseGlintBridge() {}

    public static void arm(
        GlintType value
    ) {
      pending = value;
    }

    public static GlintType consume() {
      GlintType value = pending;
      pending = GlintType.NONE;
      return value;
    }

  }

  public interface UniverseGlintHolder {

    void setUniverseGlint(
        GlintType glintType
    );

    GlintType universeGlintType();

  }

}
