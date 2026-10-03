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
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

// Magnatour
import roeyqian.magnatour.renderstate.supreme.CustomGolemRenderState;

@Environment(EnvType.CLIENT)
public final class CustomGolemModel extends EntityModel<CustomGolemRenderState> {

  private final ModelPart head;
  private final ModelPart leftArm;
  private final ModelPart leftLeg;
  private final ModelPart rightArm;
  private final ModelPart rightLeg;

  public CustomGolemModel(
      final ModelPart root
  ) {
    super(root);
    this.head = root.getChild("head");
    this.rightArm = root.getChild("right_arm");
    this.leftArm = root.getChild("left_arm");
    this.rightLeg = root.getChild("right_leg");
    this.leftLeg = root.getChild("left_leg");
  }

  public static LayerDefinition createNetheriteBodyLayer() {
    MeshDefinition mesh = createBaseMesh();
    PartDefinition root = mesh.getRoot();
    root.getChild("head").addOrReplaceChild("helmet_brow", CubeListBuilder.create()
        .texOffs(84, 34).addBox(-5.0F, -10.0F, -6.5F, 10.0F, 2.0F, 2.0F), PartPose.ZERO);
    root.getChild("head").addOrReplaceChild("block_helmet", CubeListBuilder.create()
        .texOffs(0, 96).addBox(-5.0F, -13.0F, -6.5F, 10.0F, 3.0F, 10.0F)
        .texOffs(44, 96).addBox(-5.0F, -8.0F, -6.5F, 2.0F, 6.0F, 3.0F)
        .texOffs(44, 96).addBox(3.0F, -8.0F, -6.5F, 2.0F, 6.0F, 3.0F), PartPose.ZERO);
    root.getChild("body").addOrReplaceChild("block_breastplate", CubeListBuilder.create()
        .texOffs(0, 112).addBox(-7.5F, -1.0F, -8.0F, 7.0F, 9.0F, 3.0F)
        .texOffs(0, 112).addBox(0.5F, -1.0F, -8.0F, 7.0F, 9.0F, 3.0F)
        .texOffs(44, 110).addBox(-5.5F, 10.0F, -4.0F, 11.0F, 3.0F, 8.0F), PartPose.ZERO);
    addNetheriteArmDetails(root.getChild("right_arm"), -11.0F);
    addNetheriteArmDetails(root.getChild("left_arm"), 11.0F);
    for (String leg : new String[] {"right_leg", "left_leg"}) {
      root.getChild(leg).addOrReplaceChild("greave", CubeListBuilder.create()
          .texOffs(84, 52).addBox(-4.0F, 3.0F, -4.0F, 7.0F, 7.0F, 2.0F), PartPose.ZERO);
    }
    return LayerDefinition.create(mesh, 128, 128);
  }

  public static LayerDefinition createObsidianBodyLayer() {
    MeshDefinition mesh = createBaseMesh();
    PartDefinition root = mesh.getRoot();
    root.getChild("head").addOrReplaceChild("crystal_brow", CubeListBuilder.create()
        .texOffs(84, 34).addBox(-5.0F, -9.0F, -6.5F, 10.0F, 2.0F, 2.0F), PartPose.ZERO);
    root.getChild("head").addOrReplaceChild("broken_crown", CubeListBuilder.create()
        .texOffs(84, 64).addBox(-3.5F, -14.5F, -2.0F, 3.0F, 4.0F, 3.0F)
        .texOffs(84, 64).addBox(1.0F, -13.0F, -1.0F, 3.0F, 4.0F, 3.0F), PartPose.ZERO);
    root.getChild("body").addOrReplaceChild("fractured_chest", CubeListBuilder.create()
        .texOffs(0, 96).addBox(-7.0F, -1.0F, -7.5F, 6.0F, 5.0F, 3.0F)
        .texOffs(20, 96).addBox(1.0F, 2.0F, -7.5F, 5.0F, 4.0F, 3.0F), PartPose.ZERO);
    root.getChild("body").addOrReplaceChild("back_crystals", CubeListBuilder.create()
        .texOffs(84, 64).addBox(-3.5F, -3.0F, 4.0F, 3.0F, 4.0F, 3.0F)
        .texOffs(84, 64).addBox(1.0F, 3.0F, 4.0F, 3.0F, 4.0F, 3.0F), PartPose.ZERO);
    addObsidianArmDetails(root.getChild("right_arm"), -11.0F);
    addObsidianArmDetails(root.getChild("left_arm"), 11.0F);
    return LayerDefinition.create(mesh, 128, 128);
  }

  public ModelPart getFlowerHoldingArm() {
    return this.rightArm;
  }

  public void setupAnim(
      final CustomGolemRenderState state
  ) {
    super.setupAnim(state);
    float attackTick = state.attackTicksRemaining;
    float animationSpeed = state.walkAnimationSpeed;
    float animationPos = state.walkAnimationPos;
    if (attackTick > 0.0F) {
      this.rightArm.xRot = -2.0F + 1.5F * Mth.triangleWave(attackTick, 10.0F);
      this.leftArm.xRot = -2.0F + 1.5F * Mth.triangleWave(attackTick, 10.0F);
    } else {
      int offerFlowerTick = state.offerFlowerTick;
      if (offerFlowerTick > 0) {
        this.rightArm.xRot = -0.8F + 0.025F * Mth.triangleWave((float) offerFlowerTick, 70.0F);
        this.leftArm.xRot = 0.0F;
      } else {
        this.rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(animationPos, 13.0F)) * animationSpeed;
        this.leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(animationPos, 13.0F)) * animationSpeed;
      }
    }

    this.head.yRot = state.yRot * ((float) Math.PI / 180F);
    this.head.xRot = state.xRot * ((float) Math.PI / 180F);
    this.rightLeg.xRot = -1.5F * Mth.triangleWave(animationPos, 13.0F) * animationSpeed;
    this.leftLeg.xRot = 1.5F * Mth.triangleWave(animationPos, 13.0F) * animationSpeed;
    this.rightLeg.yRot = 0.0F;
    this.leftLeg.yRot = 0.0F;
  }

  private static MeshDefinition createBaseMesh() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -7.0F, -2.0F));
    root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F).texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -7.0F, 0.0F));
    root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
    root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
    root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(-4.0F, 11.0F, 0.0F));
    root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(5.0F, 11.0F, 0.0F));
    return mesh;
  }

  private static void addNetheriteArmDetails(
      final PartDefinition arm,
      final float centerX
  ) {
    arm.addOrReplaceChild("pauldron", CubeListBuilder.create()
        .texOffs(84, 0).addBox(centerX - 4.0F, -3.5F, -4.0F, 8.0F, 7.0F, 8.0F), PartPose.ZERO);
    arm.addOrReplaceChild("bracer", CubeListBuilder.create()
        .texOffs(84, 16).addBox(centerX - 3.0F, 17.5F, -4.0F, 6.0F, 8.0F, 8.0F), PartPose.ZERO);
  }

  private static void addObsidianArmDetails(
      final PartDefinition arm,
      final float centerX
  ) {
    arm.addOrReplaceChild("crystal_shoulder", CubeListBuilder.create()
        .texOffs(84, 0).addBox(centerX - 3.5F, -4.0F, -4.0F, 7.0F, 6.0F, 8.0F), PartPose.ZERO);
    float side = Math.signum(centerX);
    arm.addOrReplaceChild("shoulder_shard", CubeListBuilder.create()
        .texOffs(84, 64).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F),
        PartPose.offsetAndRotation(centerX + side * 2.0F, -3.0F, 0.0F, 0.0F, 0.0F, side * 0.4F));
    arm.addOrReplaceChild("crystal_fist", CubeListBuilder.create()
        .texOffs(84, 16).addBox(centerX - 3.0F, 19.5F, -4.0F, 6.0F, 8.0F, 8.0F)
        .texOffs(84, 64).addBox(centerX + side * 3.0F - 1.5F, 21.0F, -2.0F, 3.0F, 4.0F, 3.0F), PartPose.ZERO);
  }

}
