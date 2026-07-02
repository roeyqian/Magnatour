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
import roeyqian.magnatour.entity.live.TheUnnameableThing;
import roeyqian.magnatour.render.entity.model.TheUnnameableThingModel;
import roeyqian.magnatour.render.entity.state.TheUnnameableThingRenderState;
import roeyqian.magnatour.utility.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class TheUnnameableThingRenderer extends MobRenderer<TheUnnameableThing, TheUnnameableThingRenderState, TheUnnameableThingModel> {

  private static final float MODEL_SCALE = 5.0F;

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/the_unnameable_thing/the_unnameable_thing.png"
  );

  public TheUnnameableThingRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new TheUnnameableThingModel(context.bakeLayer(RegEntityLayers.THE_UNNAMEABLE_THING)), 0.35F * MODEL_SCALE);
  }

  @Override
  public TheUnnameableThingRenderState createRenderState() {
    return new TheUnnameableThingRenderState();
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      TheUnnameableThingRenderState state
  ) {
    return TEXTURE;
  }

  @Override
  protected void scale(
      TheUnnameableThingRenderState state,
      PoseStack poseStack
  ) {
    super.scale(state, poseStack);
    poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
  }

}
