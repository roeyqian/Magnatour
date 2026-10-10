/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer.universe;

// Minecraft
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

// Magnatour
import roeyqian.magnatour.entity.universe.UniverseAnnihilator;

public final class UniverseAnnihilatorRenderer extends HumanoidMobRenderer<UniverseAnnihilator, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("magnatour",
      "textures/entity/universe_annihilator/universe_annihilator.png");

  public UniverseAnnihilatorRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    addLayer(new UniverseAnnihilatorGlintLayer(this));
  }

  @Override
  public HumanoidRenderState createRenderState() { return new HumanoidRenderState(); }

  @Override
  public Identifier getTextureLocation(
      HumanoidRenderState state
  ) { return TEXTURE; }

}
