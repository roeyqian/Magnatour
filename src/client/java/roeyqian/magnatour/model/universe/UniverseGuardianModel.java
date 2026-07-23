/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.model.universe;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

// Magnatour
import roeyqian.magnatour.renderstate.universe.UniverseGuardianRenderState;

@Environment(EnvType.CLIENT)
public final class UniverseGuardianModel extends EntityModel<UniverseGuardianRenderState> {

  public UniverseGuardianModel(
      ModelPart root
  ) {
    super(root);
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshDefinition = new MeshDefinition();
    PartDefinition partDefinition = meshDefinition.getRoot();

    partDefinition.addOrReplaceChild(
        "head",
        CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, 2.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
        PartPose.offset(0.0F, 0.0F, 0.0F)
    );

    partDefinition.addOrReplaceChild(
        "body",
        CubeListBuilder.create()
            .texOffs(16, 16)
            .addBox(-4.0F, 10.0F, -2.0F, 8.0F, 12.0F, 4.0F, CubeDeformation.NONE),
        PartPose.offset(0.0F, 0.0F, 0.0F)
    );

    return LayerDefinition.create(meshDefinition, 64, 64);
  }

}
