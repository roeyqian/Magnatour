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
import net.minecraft.world.entity.monster.Monster;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.model.supreme.PaleLordModel;
import roeyqian.magnatour.renderstate.supreme.PaleLordRenderState;
import roeyqian.magnatour.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class PaleLordRenderer extends MobRenderer<Monster, PaleLordRenderState, PaleLordModel> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/pale_lord/pale_lord.png"
  );

  public PaleLordRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new PaleLordModel(context.bakeLayer(RegEntityLayers.PALE_LORD)), 0.6F);
  }

  @Override
  public PaleLordRenderState createRenderState() {
    return new PaleLordRenderState();
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      PaleLordRenderState state
  ) {
    return TEXTURE;
  }

}
