/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * full license text available in the LICENSE file in the project root.
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
import net.minecraft.util.Mth;

// Magnatour
import roeyqian.magnatour.render.entity.state.SculkBehemothRenderState;

@Environment(EnvType.CLIENT)
public final class SculkBehemothModel extends EntityModel<SculkBehemothRenderState> {

  private final ModelPart head;
  private final ModelPart leftBackLeg;
  private final ModelPart leftFrontLeg;
  private final ModelPart rightBackLeg;
  private final ModelPart rightFrontLeg;

  public SculkBehemothModel(
      ModelPart root
  ) {
    super(root);
    this.head = root.getChild("head");
    this.leftFrontLeg = root.getChild("left_front_leg");
    this.rightFrontLeg = root.getChild("right_front_leg");
    this.leftBackLeg = root.getChild("left_back_leg");
    this.rightBackLeg = root.getChild("right_back_leg");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    partdefinition.addOrReplaceChild(
        "head",
        CubeListBuilder.create().texOffs(0, 180).addBox(
            -20.0F, -40.0F, -40.0F,
            40.0F, 80.0F, 40.0F,
            CubeDeformation.NONE
        ),
        PartPose.offset(0.0F, -90.0F, -60.0F)
    );

    partdefinition.addOrReplaceChild(
        "body",
        CubeListBuilder.create().texOffs(0, 0).addBox(
            -40.0F, -10.0F, -60.0F,
            80.0F, 60.0F, 120.0F,
            CubeDeformation.NONE
        ),
        PartPose.offset(0.0F, -80.0F, 0.0F)
    );

    float legWidth = 30.0F;
    float legHeight = 50.0F;
    float legDepth = 30.0F;
    float legPivotY = -50.0F;

    partdefinition.addOrReplaceChild(
        "left_front_leg",
        CubeListBuilder.create().texOffs(160, 210).addBox(
            -legWidth / 2, 20.0F, -legDepth / 2 - 5,
            legWidth, legHeight, legDepth,
            CubeDeformation.NONE
        ),
        PartPose.offset(25.0F, legPivotY, -40.0F)
    );

    partdefinition.addOrReplaceChild(
        "right_front_leg",
        CubeListBuilder.create().texOffs(160, 210).addBox(
            -legWidth / 2, 20.0F, -legDepth / 2 - 5,
            legWidth, legHeight, legDepth,
            CubeDeformation.NONE
        ),
        PartPose.offset(-25.0F, legPivotY, -40.0F)
    );

    partdefinition.addOrReplaceChild(
        "left_back_leg",
        CubeListBuilder.create().texOffs(160, 210).addBox(
            -legWidth / 2, 20.0F, -legDepth / 2 - 5,
            legWidth, legHeight, legDepth,
            CubeDeformation.NONE
        ),
        PartPose.offset(25.0F, legPivotY, 50.0F)
    );

    partdefinition.addOrReplaceChild(
        "right_back_leg",
        CubeListBuilder.create().texOffs(160, 210).addBox(
            -legWidth / 2, 20.0F, -legDepth / 2 - 5,
            legWidth, legHeight, legDepth,
            CubeDeformation.NONE
        ),
        PartPose.offset(-25.0F, legPivotY, 50.0F)
    );

    return LayerDefinition.create(meshdefinition, 512, 512);
  }

  @Override
  public void setupAnim(
      SculkBehemothRenderState state
  ) {
    super.setupAnim(state);

    this.head.xRot = 0;
    this.head.yRot = 0;
    this.leftFrontLeg.xRot = 0;
    this.rightFrontLeg.xRot = 0;
    this.leftBackLeg.xRot = 0;
    this.rightBackLeg.xRot = 0;

    this.head.yRot = state.yRot * ((float) Math.PI / 180F);
    this.head.xRot = state.xRot * ((float) Math.PI / 180F);

    animateByPhase(state);
  }

  private void animateByPhase(
      SculkBehemothRenderState state
  ) {
    switch (state.phaseType) {
      case 1 -> animateCharge(state);
      case 2 -> animateSonicBoom(state);
      case 3 -> animateSmash(state);
      default -> animateIdle(state);
    }
  }

  private void animateCharge(
      SculkBehemothRenderState state
  ) {
    float walkPhase = state.walkAnimationPos;
    float walkAmplitude = state.walkAnimationSpeed;

    if (walkAmplitude > 0.01F) {
      float swingAmount = 1.2F * Math.min(walkAmplitude, 1.0F);
      float angle = Mth.cos(walkPhase * 1.2F) * swingAmount;

      this.leftFrontLeg.xRot = angle;
      this.rightBackLeg.xRot = angle;
      this.rightFrontLeg.xRot = -angle;
      this.leftBackLeg.xRot = -angle;
    }

    this.head.xRot += 0.4F;
  }

  private void animateSonicBoom(
      SculkBehemothRenderState state
  ) {
    float walkPhase = state.walkAnimationPos;
    float walkAmplitude = state.walkAnimationSpeed;

    if (walkAmplitude > 0.01F) {
      float swingAmount = 0.5F * Math.min(walkAmplitude, 1.0F);
      setWalkAngle(walkPhase, swingAmount);
    }

    this.head.xRot -= 0.2F;
  }

  private void animateSmash(
      SculkBehemothRenderState state
  ) {
    if (state.inAir) {
      float spread = 0.8F;
      this.leftFrontLeg.xRot = -spread;
      this.rightFrontLeg.xRot = -spread;
      this.leftBackLeg.xRot = spread;
      this.rightBackLeg.xRot = spread;

      this.head.xRot += 0.3F;
    } else {
      getWalkPhaseState(state);
    }
  }

  private void animateIdle(
      SculkBehemothRenderState state
  ) {
    getWalkPhaseState(state);
  }

  private void setWalkAngle(
      float walkPhase,
      float swingAmount
  ) {
    float frontLeftAngle = Mth.cos(walkPhase * 0.6662F) * swingAmount;
    float frontRightAngle = Mth.cos(walkPhase * 0.6662F + (float) Math.PI) * swingAmount;

    this.leftFrontLeg.xRot = frontLeftAngle;
    this.rightBackLeg.xRot = frontLeftAngle;
    this.rightFrontLeg.xRot = frontRightAngle;
    this.leftBackLeg.xRot = frontRightAngle;
  }

  private void getWalkPhaseState(
      SculkBehemothRenderState state
  ) {
    float walkPhase = state.walkAnimationPos;
    float walkAmplitude = state.walkAnimationSpeed;

    if (walkAmplitude > 0.01F) {
      float clampedAmplitude = Math.min(walkAmplitude, 1.0F);
      float swingAmount = 0.6F * clampedAmplitude;

      setWalkAngle(walkPhase, swingAmount);
    }
  }

}
