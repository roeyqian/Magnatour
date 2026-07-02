/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.render.entity.model;

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
import roeyqian.magnatour.render.entity.state.TheUnnameableThingRenderState;

@Environment(EnvType.CLIENT)
public final class TheUnnameableThingModel extends EntityModel<TheUnnameableThingRenderState> {

  public TheUnnameableThingModel(
      ModelPart root
  ) {
    super(root);
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshDefinition = new MeshDefinition();
    PartDefinition partDefinition = meshDefinition.getRoot();

    partDefinition.addOrReplaceChild(
        "bone",
        CubeListBuilder.create()
            .texOffs(8, 0)
            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE)
            .texOffs(0, 8)
            .addBox(-2.0F, -3.0F, 0.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE)
            .texOffs(0, 0)
            .addBox(0.0F, -3.0F, 2.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE)
            .texOffs(0, 4)
            .addBox(-3.0F, -3.0F, 1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE)
            .texOffs(8, 4)
            .addBox(-1.0F, -4.0F, 1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE)
            .texOffs(8, 8)
            .addBox(-1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
        PartPose.offset(0.0F, 24.0F, -2.0F)
    );

    return LayerDefinition.create(meshDefinition, 16, 16);
  }

}
