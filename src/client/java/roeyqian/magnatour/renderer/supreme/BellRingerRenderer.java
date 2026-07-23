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
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.supreme.BellRinger;
import roeyqian.magnatour.model.supreme.BellRingerModel;
import roeyqian.magnatour.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class BellRingerRenderer extends HumanoidMobRenderer<BellRinger, HumanoidRenderState, BellRingerModel> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/bell_ringer/bell_ringer.png"
  );

  public BellRingerRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new BellRingerModel(context.bakeLayer(RegEntityLayers.BELL_RINGER)), 0.5F);
  }

  @Override
  public HumanoidRenderState createRenderState() {
    return new HumanoidRenderState();
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      HumanoidRenderState state
  ) {
    return TEXTURE;
  }

}
