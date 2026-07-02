/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.render.entity;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;

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
import roeyqian.magnatour.entity.live.BellSoul;
import roeyqian.magnatour.render.entity.model.BellSoulModel;
import roeyqian.magnatour.render.entity.state.BellSoulRenderState;
import roeyqian.magnatour.utility.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class BellSoulRenderer extends MobRenderer<BellSoul, BellSoulRenderState, BellSoulModel> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/bell_soul/bell_soul.png"
  );

  public BellSoulRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new BellSoulModel(context.bakeLayer(RegEntityLayers.BELL_SOUL)), 0.3F);
  }

  @Override
  public BellSoulRenderState createRenderState() {
    return new BellSoulRenderState();
  }

  @Override
  public void extractRenderState(
      BellSoul entity,
      BellSoulRenderState state,
      float partialTick
  ) {
    super.extractRenderState(entity, state, partialTick);
    state.charging = entity.isChargingAttack();
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      BellSoulRenderState state
  ) {
    return TEXTURE;
  }

  @Override
  protected void scale(
      BellSoulRenderState state,
      PoseStack poseStack
  ) {
    super.scale(state, poseStack);
    // Align the visual model center with BellSoul's small hitbox.
    poseStack.translate(0.0F, -0.35F, 0.0F);
  }

}
