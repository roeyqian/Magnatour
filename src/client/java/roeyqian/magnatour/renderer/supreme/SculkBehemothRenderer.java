/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer.supreme;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.supreme.SculkBehemoth;
import roeyqian.magnatour.model.supreme.SculkBehemothModel;
import roeyqian.magnatour.renderstate.supreme.SculkBehemothRenderState;
import roeyqian.magnatour.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class SculkBehemothRenderer extends MobRenderer<SculkBehemoth, SculkBehemothRenderState, SculkBehemothModel> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/sculk_behemoth/sculk_behemoth.png"
  );

  public SculkBehemothRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new SculkBehemothModel(context.bakeLayer(RegEntityLayers.SCULK_BEHEMOTH)), 2.0F);
  }

  @Override
  public SculkBehemothRenderState createRenderState() {
    return new SculkBehemothRenderState();
  }

  @Override
  public void extractRenderState(
      SculkBehemoth entity,
      SculkBehemothRenderState state,
      float partialTick
  ) {
    super.extractRenderState(entity, state, partialTick);
    state.phaseType = entity.getPhaseType();
    state.inAir = !entity.onGround();
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      SculkBehemothRenderState state
  ) {
    return TEXTURE;
  }

}
