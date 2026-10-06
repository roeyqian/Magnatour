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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.renderstate.supreme.PaleLordRenderState;

@Environment(EnvType.CLIENT)
public final class PaleLordModel extends EntityModel<PaleLordRenderState> {

  private final ModelPart head;
  private final ModelPart leftArm;
  private final ModelPart leftForearm;
  private final ModelPart leftLeg;
  private final ModelPart rightArm;
  private final ModelPart rightForearm;
  private final ModelPart rightLeg;
  private final ModelPart upperBody;

  public PaleLordModel(
      ModelPart roots
  ) {
    super(roots);
    ModelPart root = roots.getChild("root");
    this.upperBody = root.getChild("upper_body");
    this.head = this.upperBody.getChild("head");
    this.rightArm = this.upperBody.getChild("right_arm");
    this.leftArm = this.upperBody.getChild("left_arm");
    this.rightForearm = this.rightArm.getChild("forearm");
    this.leftForearm = this.leftArm.getChild("forearm");
    this.rightLeg = root.getChild("right_leg");
    this.leftLeg = root.getChild("left_leg");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot().addOrReplaceChild(
        "root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F)
    );
    PartDefinition upperBody = root.addOrReplaceChild(
        "upper_body", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, 0.0F)
    );
    addHead(upperBody);
    addBody(upperBody);
    addArm(upperBody, "right_arm", -7.0F, 10.0F, 40);
    addArm(upperBody, "left_arm", 7.0F, 9.0F, 60);
    addLeg(root, "right_leg", -2.5F, 40, 40);
    addLeg(root, "left_leg", 2.5F, 60, 72);
    return LayerDefinition.create(mesh, 128, 128);
  }

  @Override
  public void setupAnim(
      @NonNull PaleLordRenderState state
  ) {
    super.setupAnim(state);
    this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
    this.head.yRot = state.yRot * Mth.DEG_TO_RAD;

    float stride = state.walkAnimationPos * 0.6662F;
    float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
    float rightSwing = Mth.cos(stride) * speed;
    float leftSwing = Mth.cos(stride + Mth.PI) * speed;
    float sway = Mth.sin(state.ageInTicks * 0.06F);

    this.rightLeg.xRot = rightSwing * 0.8F;
    this.leftLeg.xRot = leftSwing * 0.8F;
    this.rightArm.xRot = leftSwing * 0.55F - 0.08F;
    this.leftArm.xRot = rightSwing * 0.55F - 0.08F;
    this.rightArm.zRot = 0.09F + sway * 0.025F;
    this.leftArm.zRot = -0.09F - sway * 0.025F;
    this.rightForearm.xRot = -0.12F - Math.max(0.0F, leftSwing) * 0.2F;
    this.leftForearm.xRot = -0.12F - Math.max(0.0F, rightSwing) * 0.2F;
    this.upperBody.xRot = 0.025F + sway * 0.012F;
    this.upperBody.zRot = Mth.sin(stride) * speed * 0.025F;

    if (state.attackProgress > 0.0F) {
      float attack = Mth.sin(state.attackProgress * Mth.PI);
      float strike = Mth.sin(Mth.sqrt(state.attackProgress) * Mth.PI);
      this.upperBody.xRot += attack * 0.25F;
      this.upperBody.yRot = -strike * 0.3F;
      this.rightArm.xRot = -strike * 2.0F - attack * 0.4F;
      this.rightArm.yRot = -attack * 0.35F;
      this.rightArm.zRot = 0.09F + attack * 0.2F;
      this.rightForearm.xRot = -0.12F - attack * 0.65F;
      this.leftArm.xRot -= attack * 0.5F;
      this.leftForearm.xRot -= attack * 0.25F;
    }
  }

  private static void addHead(
      PartDefinition upperBody
  ) {
    PartDefinition head = upperBody.addOrReplaceChild(
        "head",
        CubeListBuilder.create().texOffs(0, 0)
            .addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F),
        PartPose.offset(-0.5F, -12.0F, 0.0F)
    );
    head.addOrReplaceChild(
        "crown_center",
        CubeListBuilder.create().texOffs(84, 0)
            .addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F),
        PartPose.offsetAndRotation(0.0F, -9.0F, 1.0F, -0.12F, 0.0F, 0.0F)
    );
    addBranch(head, "right_branch", -3.0F, 0.55F);
    addBranch(head, "left_branch", 3.0F, -0.7F);
  }

  private static void addBody(
      PartDefinition upperBody
  ) {
    PartDefinition body = upperBody.addOrReplaceChild(
        "body",
        CubeListBuilder.create().texOffs(0, 24)
            .addBox(-5.0F, 0.0F, -3.0F, 10.0F, 14.0F, 6.0F),
        PartPose.offset(0.0F, -12.0F, 0.0F)
    );
    body.addOrReplaceChild(
        "resin_heart",
        CubeListBuilder.create().texOffs(0, 64)
            .addBox(-2.0F, -3.0F, -1.0F, 4.0F, 6.0F, 1.0F),
        PartPose.offset(0.0F, 6.0F, -3.0F)
    );
  }

  private static void addArm(
      PartDefinition upperBody,
      String name,
      float x,
      float length,
      int textureU
  ) {
    PartDefinition arm = upperBody.addOrReplaceChild(
        name,
        CubeListBuilder.create().texOffs(textureU, 0)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, length, 4.0F),
        PartPose.offset(x, -11.0F, 0.0F)
    );
    arm.addOrReplaceChild(
        "shoulder",
        CubeListBuilder.create().texOffs(0, 50)
            .addBox(-3.0F, -1.0F, -3.0F, 6.0F, 4.0F, 6.0F),
        PartPose.ZERO
    );
    PartDefinition forearm = arm.addOrReplaceChild(
        "forearm",
        CubeListBuilder.create().texOffs(textureU, 0)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, length, 4.0F),
        PartPose.offset(0.0F, length, 0.0F)
    );
    forearm.addOrReplaceChild(
        "root_claws",
        CubeListBuilder.create().texOffs(84, 28)
            .addBox(-2.0F, 0.0F, -2.0F, 1.0F, 5.0F, 1.0F)
            .addBox(1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 1.0F)
            .addBox(-0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 1.0F),
        PartPose.offset(0.0F, length, 0.0F)
    );
  }

  private static void addLeg(
      PartDefinition root,
      String name,
      float x,
      int textureU,
      int footU
  ) {
    PartDefinition leg = root.addOrReplaceChild(
        name,
        CubeListBuilder.create().texOffs(textureU, 28)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 18.0F, 4.0F),
        PartPose.offset(x, -18.0F, 0.0F)
    );
    leg.addOrReplaceChild(
        "root_foot",
        CubeListBuilder.create().texOffs(footU, 54)
            .addBox(-2.5F, -3.0F, -5.0F, 5.0F, 3.0F, 8.0F),
        PartPose.offset(0.0F, 18.0F, 0.0F)
    );
  }

  private static void addBranch(
      PartDefinition head,
      String name,
      float x,
      float tilt
  ) {
    PartDefinition branch = head.addOrReplaceChild(
        name,
        CubeListBuilder.create().texOffs(84, 12)
            .addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F),
        PartPose.offsetAndRotation(x, -6.0F, 1.0F, 0.12F, 0.0F, tilt)
    );
    branch.addOrReplaceChild(
        "fork",
        CubeListBuilder.create().texOffs(84, 0)
            .addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F),
        PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -0.2F, 0.0F, -tilt * 0.8F)
    );
  }

}
