/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.model.supreme;

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
import net.minecraft.util.Mth;

// Magnatour
import roeyqian.magnatour.renderstate.supreme.BellSoulRenderState;

@Environment(EnvType.CLIENT)
public final class BellSoulModel extends EntityModel<BellSoulRenderState> {

  private static final float ATTACK_ARM_ROTATION = 210.0F * ((float) Math.PI / 180.0F);

  private final ModelPart head;
  private final ModelPart leftArm;
  private final ModelPart leftWing;
  private final ModelPart rightArm;
  private final ModelPart rightWing;

  public BellSoulModel(
      ModelPart root
  ) {
    super(root);
    this.head = root.getChild("head");
    this.rightArm = root.getChild("right_arm");
    this.leftArm = root.getChild("left_arm");
    this.rightWing = root.getChild("right_wing");
    this.leftWing = root.getChild("left_wing");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshDefinition = new MeshDefinition();
    PartDefinition root = meshDefinition.getRoot();

    root.addOrReplaceChild(
        "head",
        CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
        PartPose.offset(0.0F, 20.0F, 0.0F)
    );

    root.addOrReplaceChild(
        "body",
        CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, CubeDeformation.NONE).texOffs(0, 16).addBox(-1.5F, 4.0F, -1.0F, 3.0F, 5.0F, 2.0F, CubeDeformation.NONE),
        PartPose.offset(0.0F, 20.0F, 0.0F)
    );

    root.addOrReplaceChild(
        "right_arm",
        CubeListBuilder.create().texOffs(23, 0).addBox(-1.25F, -0.5F, -0.75F, 2.0F, 4.0F, 1.5F, CubeDeformation.NONE),
        PartPose.offset(-1.75F, 21.5F, 0.0F)
    );

    root.addOrReplaceChild(
        "left_arm",
        CubeListBuilder.create().texOffs(23, 0).mirror().addBox(-0.75F, -0.5F, -0.75F, 2.0F, 4.0F, 1.5F, CubeDeformation.NONE),
        PartPose.offset(1.75F, 21.5F, 0.0F)
    );

    root.addOrReplaceChild(
        "right_wing",
        CubeListBuilder.create().texOffs(16, 14).addBox(-10.0F, 0.0F, 0.0F, 10.0F, 12.0F, 1.0F, CubeDeformation.NONE),
        PartPose.offsetAndRotation(-0.5F, 21.0F, 1.0F, 0.0F, 0.47123894F, 0.0F)
    );

    root.addOrReplaceChild(
        "left_wing",
        CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 1.0F, CubeDeformation.NONE),
        PartPose.offsetAndRotation(0.5F, 21.0F, 1.0F, 0.0F, -0.47123894F, 0.0F)
    );

    return LayerDefinition.create(meshDefinition, 32, 32);
  }

  @Override
  public void setupAnim(
      BellSoulRenderState state
  ) {
    this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
    this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);

    float wingBase = 0.47123894F;
    float wingFlap = Mth.cos(state.ageInTicks * 0.8F) * ((float) Math.PI * 0.08F);
    this.rightWing.yRot = wingBase + wingFlap;
    this.leftWing.yRot = -this.rightWing.yRot;

    if (state.charging) {
      this.rightArm.xRot = ATTACK_ARM_ROTATION;
      this.leftArm.xRot = ATTACK_ARM_ROTATION;
    } else {
      float armSwing = Mth.cos(state.ageInTicks * 0.6F) * 0.1F;
      this.rightArm.xRot = armSwing;
      this.leftArm.xRot = armSwing;
    }
  }

}
