/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.model.supreme;

// Java Standard
import java.util.Set;

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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

// Magnatour
import roeyqian.magnatour.renderstate.supreme.SculkBehemothRenderState;

@Environment(EnvType.CLIENT)
public final class SculkBehemothModel extends EntityModel<SculkBehemothRenderState> {

  private final ModelPart body;
  private final ModelPart head;
  private final ModelPart leftBackLeg;
  private final ModelPart leftFrontLeg;
  private final ModelPart leftSensor;
  private final ModelPart rightBackLeg;
  private final ModelPart rightFrontLeg;
  private final ModelPart rightSensor;

  public SculkBehemothModel(
      ModelPart root
  ) {
    super(root);
    this.body = root.getChild("body");
    this.head = root.getChild("head");
    this.leftSensor = this.head.getChild("left_sensor");
    this.rightSensor = this.head.getChild("right_sensor");
    this.leftFrontLeg = this.body.getChild("left_front_leg");
    this.rightFrontLeg = this.body.getChild("right_front_leg");
    this.leftBackLeg = this.body.getChild("left_back_leg");
    this.rightBackLeg = this.body.getChild("right_back_leg");
  }

  public static LayerDefinition createBodyLayer() {
    return LayerDefinition.create(createMesh(), 512, 512);
  }

  public static LayerDefinition createHeartLayer() {
    MeshDefinition mesh = createMesh();
    // Keep the animation hierarchy, but render only the flat heart decals.
    mesh.getRoot().retainExactParts(Set.of());
    PartDefinition body = mesh.getRoot().getChild("body");
    // Enlarge the existing motif by 1.5 on both axes, keeping its UVs unchanged.
    // Only the two flanks and underside carry glow; every plane faces outward.
    addHeartDecal(body, "left_heart", 40.075F, -3, 0, 0, -Mth.HALF_PI, 5.4F, 4.2F);
    addHeartDecal(body, "right_heart", -40.075F, -3, 0, 0, Mth.HALF_PI, 5.4F, 4.2F);
    addHeartDecal(body, "bottom_heart", 0, 50.075F, -21, Mth.HALF_PI, 0, 5.4F, 4.2F);
    // Sample the heart pixels directly from Minecraft's 128-square Warden atlas.
    return LayerDefinition.create(mesh, 128, 128);
  }

  @Override
  public void setupAnim(
      SculkBehemothRenderState state
  ) {
    super.setupAnim(state);
    this.head.yRot = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
    this.head.xRot = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;

    float breath = Mth.sin(state.ageInTicks * 0.09F);
    this.body.y += breath * 0.45F;
    this.head.y += breath * 0.3F;
    float listening = Mth.sin(state.ageInTicks * 0.12F) * 0.025F;
    this.leftSensor.zRot += listening;
    this.rightSensor.zRot -= listening;

    float amplitude = Math.min(state.walkAnimationSpeed, 1.0F);
    float speed = state.phaseType == 1 ? 1.05F : 0.6662F;
    float strength = state.phaseType == 1 ? 0.65F : 0.4F;
    float stride = Mth.cos(state.walkAnimationPos * speed) * amplitude * strength;
    this.leftFrontLeg.xRot = stride;
    this.rightBackLeg.xRot = stride;
    this.rightFrontLeg.xRot = -stride;
    this.leftBackLeg.xRot = -stride;
    this.body.zRot = Mth.sin(state.walkAnimationPos * speed) * amplitude * 0.015F;

    switch (state.phaseType) {
      case 1 -> {
        this.head.xRot += 0.3F;
        this.leftSensor.zRot -= 0.08F;
        this.rightSensor.zRot += 0.08F;
      }
      case 2 -> {
        // The original fixed mouth stays intact; head and sensors signal the roar.
        this.head.xRot -= 0.2F;
        this.head.y += Mth.sin(state.ageInTicks * 0.7F) * 0.3F;
        this.leftSensor.zRot += 0.1F;
        this.rightSensor.zRot -= 0.1F;
      }
      case 3 -> {
        if (state.inAir) {
          this.leftFrontLeg.xRot = -0.65F;
          this.rightFrontLeg.xRot = -0.65F;
          this.leftBackLeg.xRot = 0.6F;
          this.rightBackLeg.xRot = 0.6F;
          this.head.xRot += 0.28F;
        }
      }
      default -> { }
    }

  }

  private static MeshDefinition createMesh() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();

    // Keep the original head proportions and face UVs; detail belongs in the skin.
    PartDefinition head = root.addOrReplaceChild("head",
        CubeListBuilder.create().texOffs(0, 180).addBox(-20, -40, -40, 40, 80, 40),
        PartPose.offset(0, -90, -60));
    PartDefinition body = root.addOrReplaceChild("body",
        CubeListBuilder.create().texOffs(0, 0).addBox(-40, -10, -60, 80, 60, 120),
        PartPose.offset(0, -80, 0));
    // A single low shoulder rise gives weight without a separate armor silhouette.
    body.addOrReplaceChild("shoulders",
        CubeListBuilder.create().texOffs(240, 320).addBox(-36, -26, -48, 72, 18, 64),
        PartPose.ZERO);

    createSensor(head, "left_sensor", 1);
    createSensor(head, "right_sensor", -1);
    createLeg(body, "left_front_leg", 25, -43);
    createLeg(body, "right_front_leg", -25, -43);
    createLeg(body, "left_back_leg", 25, 45);
    createLeg(body, "right_back_leg", -25, 45);
    return mesh;
  }

  private static void addHeartDecal(
      PartDefinition body,
      String name,
      float x,
      float y,
      float z,
      float xRot,
      float yRot,
      float xScale,
      float yScale
  ) {
    body.addOrReplaceChild(name,
        CubeListBuilder.create().texOffs(14, 17)
            .addBox(-5, 0, 0, 10, 10, 0, Set.of(Direction.NORTH)),
        new PartPose(x, y, z, xRot, yRot, 0, xScale, yScale, 1.0F));
  }

  private static void createSensor(
      PartDefinition head,
      String name,
      int side
  ) {
    // Two cuboids per sensor: a short sculk stalk and an outward listening tip.
    PartDefinition sensor = head.addOrReplaceChild(name,
        CubeListBuilder.create().texOffs(280, 210).addBox(-3, -24, -3, 6, 24, 6),
        PartPose.offsetAndRotation(side * 21, -13, -18, 0, 0, side * 0.55F));
    sensor.addOrReplaceChild("tip",
        CubeListBuilder.create().texOffs(304, 210).addBox(side > 0 ? 0 : -12, -25, -3, 12, 6, 6),
        PartPose.ZERO);
  }

  private static void createLeg(
      PartDefinition body,
      String name,
      float x,
      float z
  ) {
    // Attach at the belly's Y=50 surface, rather than burying the upper 20
    // units in the torso. 54-unit legs still put the resting foot at model Y=24.
    body.addOrReplaceChild(name,
        CubeListBuilder.create().texOffs(160, 210).addBox(-15, 0, -15, 30, 54, 30),
        PartPose.offset(x, 50, z));
  }

}
