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
import roeyqian.magnatour.renderstate.supreme.BellSoulRenderState;

@Environment(EnvType.CLIENT)
public final class BellSoulModel extends EntityModel<BellSoulRenderState> {

  // Each material has its own 64 x 64 region in the texture atlas.
  private static final int BRONZE_U = 4;
  private static final int BRONZE_V = 28;
  private static final int GOLD_U = 68;
  private static final int GOLD_V = 4;
  private static final int SPIRIT_U = 4;
  private static final int SPIRIT_V = 68;
  private static final int WING_U = 68;
  private static final int WING_V = 68;

  private static final float ATTACK_ARM_ROTATION = 210.0F * ((float) Math.PI / 180.0F);
  private static final float WING_ANGLE = 0.47F;

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
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();

    PartDefinition head = root.addOrReplaceChild(
        "head",
        CubeListBuilder.create().texOffs(4, 4).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 4.0F, 6.0F),
        PartPose.offset(0.0F, 18.0F, 0.0F)
    );
    head.addOrReplaceChild(
        "crown", CubeListBuilder.create().texOffs(GOLD_U, GOLD_V)
            .addBox(-1.0F, -8.0F, -1.0F, 2.0F, 1.0F, 2.0F)
            .texOffs(BRONZE_U, BRONZE_V).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 2.0F, 4.0F),
        PartPose.ZERO
    );
    head.addOrReplaceChild(
        "rim", CubeListBuilder.create().texOffs(GOLD_U, GOLD_V)
            .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F),
        PartPose.ZERO
    );
    // The dark inset makes the bell mouth visible around the spirit beneath it.
    head.addOrReplaceChild(
        "mouth", CubeListBuilder.create().texOffs(38, 28)
            .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 1.0F, 5.0F),
        PartPose.ZERO
    );

    PartDefinition body = root.addOrReplaceChild(
        "body", CubeListBuilder.create().texOffs(SPIRIT_U, SPIRIT_V)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
        PartPose.offset(0.0F, 19.0F, 0.0F)
    );
    body.addOrReplaceChild(
        "tail", CubeListBuilder.create().texOffs(SPIRIT_U + 20, SPIRIT_V)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
        PartPose.offset(0.0F, 5.0F, 0.0F)
    );

    root.addOrReplaceChild(
        "right_arm", CubeListBuilder.create().texOffs(SPIRIT_U, SPIRIT_V)
            .addBox(-1.25F, -0.5F, -0.75F, 2.0F, 4.0F, 1.5F),
        PartPose.offset(-2.5F, 20.0F, 0.0F)
    );
    root.addOrReplaceChild(
        "left_arm", CubeListBuilder.create().texOffs(SPIRIT_U, SPIRIT_V).mirror()
            .addBox(-0.75F, -0.5F, -0.75F, 2.0F, 4.0F, 1.5F),
        PartPose.offset(2.5F, 20.0F, 0.0F)
    );

    root.addOrReplaceChild(
        "right_wing", CubeListBuilder.create().texOffs(WING_U, WING_V)
            .addBox(-9.0F, -1.0F, 0.0F, 9.0F, 9.0F, 1.0F),
        PartPose.offsetAndRotation(-1.5F, 18.0F, 2.0F, 0.0F, WING_ANGLE, 0.0F)
    );
    root.addOrReplaceChild(
        "left_wing", CubeListBuilder.create().texOffs(WING_U, WING_V).mirror()
            .addBox(0.0F, -1.0F, 0.0F, 9.0F, 9.0F, 1.0F),
        PartPose.offsetAndRotation(1.5F, 18.0F, 2.0F, 0.0F, -WING_ANGLE, 0.0F)
    );

    return LayerDefinition.create(mesh, 128, 128);
  }

  @Override
  public void setupAnim(
      @NonNull BellSoulRenderState state
  ) {
    this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
    this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);

    float wingFlap = Mth.cos(state.ageInTicks * 0.8F) * ((float) Math.PI * 0.08F);
    this.rightWing.yRot = WING_ANGLE + wingFlap;
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
