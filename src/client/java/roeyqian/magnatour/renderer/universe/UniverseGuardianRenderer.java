/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer.universe;

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
import roeyqian.magnatour.entity.universe.UniverseGuardian;
import roeyqian.magnatour.model.universe.UniverseGuardianModel;
import roeyqian.magnatour.renderstate.universe.UniverseGuardianRenderState;
import roeyqian.magnatour.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class UniverseGuardianRenderer extends MobRenderer<UniverseGuardian, UniverseGuardianRenderState, UniverseGuardianModel> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/universe_guardian/universe_guardian.png"
  );

  public UniverseGuardianRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new UniverseGuardianModel(context.bakeLayer(RegEntityLayers.UNIVERSE_GUARDIAN)), 0.5F);
  }

  @Override
  public UniverseGuardianRenderState createRenderState() {
    return new UniverseGuardianRenderState();
  }

  @Override
  public void extractRenderState(
      UniverseGuardian entity,
      UniverseGuardianRenderState state,
      float partialTick
  ) {
    super.extractRenderState(entity, state, partialTick);
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      UniverseGuardianRenderState state
  ) {
    return TEXTURE;
  }

}
