/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer.universe;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

// Magnatour
import roeyqian.magnatour.model.universe.UniverseGuardianModel;
import roeyqian.magnatour.renderer.GlintRenderTypes;
import roeyqian.magnatour.renderstate.universe.UniverseGuardianRenderState;

@Environment(EnvType.CLIENT)
public final class UniverseGuardianGlintLayer extends RenderLayer<UniverseGuardianRenderState, UniverseGuardianModel> {

  public UniverseGuardianGlintLayer(
      RenderLayerParent<UniverseGuardianRenderState, UniverseGuardianModel> parent
  ) {
    super(parent);
  }

  @Override
  public void submit(
      PoseStack poseStack,
      SubmitNodeCollector collector,
      int lightCoords,
      UniverseGuardianRenderState state,
      float yRot, float xRot
  ) {
    if (state.isInvisible) return;
    // The base model must populate depth before this EQUAL-depth overlay is drawn.
    collector.order(1).submitModel(
        getParentModel(), state, poseStack, GlintRenderTypes.UNIVERSE_GUARDIAN_GLINT,
        lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor
    );
  }

}
