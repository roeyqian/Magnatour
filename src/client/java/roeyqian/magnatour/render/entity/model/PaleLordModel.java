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
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

// Magnatour
import roeyqian.magnatour.render.entity.state.PaleLordRenderState;

@Environment(EnvType.CLIENT)
public final class PaleLordModel extends EntityModel<PaleLordRenderState> {

  private final ModelPart body;
  private final ModelPart head;
  private final ModelPart leftArm;
  private final ModelPart leftLeg;
  private final ModelPart rightArm;
  private final ModelPart rightLeg;

  public PaleLordModel(
      ModelPart roots
  ) {
    super(roots);
    ModelPart root = roots.getChild("root");
    ModelPart upperBody = root.getChild("upper_body");
    this.head = upperBody.getChild("head");
    this.body = upperBody.getChild("body");
    this.rightArm = upperBody.getChild("right_arm");
    this.leftArm = upperBody.getChild("left_arm");
    this.rightLeg = root.getChild("right_leg");
    this.leftLeg = root.getChild("left_leg");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = createMesh();
    return LayerDefinition.create(mesh, 64, 64);
  }

  @Override
  public void setupAnim(
      PaleLordRenderState state
  ) {
    super.setupAnim(state);

    this.head.xRot = state.xRot * ((float) Math.PI / 180F);
    this.head.yRot = state.yRot * ((float) Math.PI / 180F);

    float walkPos = state.walkAnimationPos;
    float walkSpeed = state.walkAnimationSpeed;

    if (walkSpeed > 0.01F) {
      float swingAmount = Math.min(walkSpeed, 1.0F);
      this.rightArm.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * swingAmount;
      this.leftArm.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * swingAmount;
      this.rightLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * swingAmount;
      this.leftLeg.xRot = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.4F * swingAmount;
    }
  }

  private static MeshDefinition createMesh() {
    MeshDefinition meshDefinition = new MeshDefinition();
    PartDefinition partDefinition = meshDefinition.getRoot();
    PartDefinition root = partDefinition.addOrReplaceChild(
        "root",
        CubeListBuilder.create(),
        PartPose.offset(0.0F, 24.0F, 0.0F)
    );
    PartDefinition upperBody = root.addOrReplaceChild(
        "upper_body",
        CubeListBuilder.create(),
        PartPose.offset(-1.0F, -19.0F, 0.0F)
    );
    upperBody.addOrReplaceChild(
        "head",
        CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F)
            .texOffs(28, 31)
            .addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 6.0F)
            .texOffs(12, 40)
            .addBox(3.0F, -13.0F, 0.0F, 9.0F, 14.0F, 0.0F)
            .texOffs(34, 12)
            .addBox(-12.0F, -14.0F, 0.0F, 9.0F, 14.0F, 0.0F),
        PartPose.offset(-3.0F, -11.0F, 0.0F)
    );
    upperBody.addOrReplaceChild(
        "body",
        CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(0.0F, -3.0F, -3.0F, 6.0F, 13.0F, 5.0F)
            .texOffs(24, 0)
            .addBox(-6.0F, -4.0F, -3.0F, 6.0F, 7.0F, 5.0F),
        PartPose.offset(0.0F, -7.0F, 1.0F)
    );
    upperBody.addOrReplaceChild(
        "right_arm",
        CubeListBuilder.create()
            .texOffs(22, 13)
            .addBox(-2.0F, -1.5F, -1.5F, 3.0F, 21.0F, 3.0F)
            .texOffs(46, 0)
            .addBox(-2.0F, 19.5F, -1.5F, 3.0F, 4.0F, 3.0F),
        PartPose.offset(-7.0F, -9.5F, 1.5F)
    );
    upperBody.addOrReplaceChild(
        "left_arm",
        CubeListBuilder.create()
            .texOffs(30, 40)
            .addBox(0.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
            .texOffs(52, 12)
            .addBox(0.0F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F)
            .texOffs(52, 19)
            .addBox(0.0F, 15.0F, -1.5F, 3.0F, 4.0F, 3.0F),
        PartPose.offset(6.0F, -9.0F, 0.5F)
    );
    root.addOrReplaceChild(
        "left_leg",
        CubeListBuilder.create()
            .texOffs(42, 40)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F)
            .texOffs(45, 55)
            .addBox(-1.5F, 15.7F, -4.5F, 5.0F, 0.0F, 9.0F),
        PartPose.offset(1.5F, -16.0F, 0.5F)
    );
    root.addOrReplaceChild(
        "right_leg",
        CubeListBuilder.create()
            .texOffs(0, 34)
            .addBox(-3.0F, -1.5F, -1.5F, 3.0F, 19.0F, 3.0F)
            .texOffs(45, 46)
            .addBox(-5.0F, 17.2F, -4.5F, 5.0F, 0.0F, 9.0F)
            .texOffs(12, 34)
            .addBox(-3.0F, -4.5F, -1.5F, 3.0F, 3.0F, 3.0F),
        PartPose.offset(-1.0F, -17.5F, 0.5F)
    );
    return meshDefinition;
  }

}
