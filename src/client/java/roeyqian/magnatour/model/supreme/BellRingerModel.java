/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.model.supreme;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

// JSpecify
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class BellRingerModel extends HumanoidModel<HumanoidRenderState> {

  public BellRingerModel(
      ModelPart root
  ) {
    super(root);
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
    return LayerDefinition.create(meshDefinition, 64, 64);
  }

  @Override
  public void setupAnim(
      @NonNull HumanoidRenderState state
  ) {
    super.setupAnim(state);
  }

}
