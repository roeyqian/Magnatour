/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.render.universe;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.universe.UniverseGuardian;
import roeyqian.magnatour.model.universe.UniverseGuardianModel;
import roeyqian.magnatour.registry.output.MagnaEntityLayers;
import roeyqian.magnatour.render.universe.UniverseGuardianRenderer.UniverseGuardianRenderState;

@Environment(EnvType.CLIENT)
public final class UniverseGuardianRenderer extends MobRenderer<UniverseGuardian, UniverseGuardianRenderState, UniverseGuardianModel> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/entity/universe_guardian/universe_guardian.png"
  );

  public UniverseGuardianRenderer(
      EntityRendererProvider.Context context
  ) {
    super(context, new UniverseGuardianModel(context.bakeLayer(MagnaEntityLayers.UNIVERSE_GUARDIAN)), 0.5F);
    Minecraft.getInstance().getTextureManager().registerAndLoad(TEXTURE, new UniverseGuardianTexture(TEXTURE));
    addLayer(new UniverseGuardianGlintLayer(this));
  }

  @NonNull @Override
  public UniverseGuardianRenderState createRenderState() {
    return new UniverseGuardianRenderState();
  }

  @Override
  public void extractRenderState(
      @NonNull UniverseGuardian entity,
      @NonNull UniverseGuardianRenderState state,
      float partialTick
  ) {
    super.extractRenderState(entity, state, partialTick);
  }

  @Override @NonNull
  public Identifier getTextureLocation(
      @NonNull UniverseGuardianRenderState state
  ) {
    return TEXTURE;
  }

  @Environment(EnvType.CLIENT)
  public static final class UniverseGuardianRenderState extends LivingEntityRenderState {}

}
