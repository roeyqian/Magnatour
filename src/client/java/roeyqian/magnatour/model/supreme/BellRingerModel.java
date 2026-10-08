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
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

// JSpecify
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class BellRingerModel extends HumanoidModel<HumanoidRenderState> {

  private static final int BRASS = 7;
  // Each material occupies a 64px tile in the 256px atlas. A 4px inset keeps UVs
  // away from tile boundaries; the largest cube net is only 40px wide.
  private static final int BRONZE = 0;
  private static final int CHEST_ARMOR = 6;
  private static final int DARK_CLOTH = 2;
  private static final int EMBROIDERY = 10;
  private static final int ENGRAVED_BRONZE = 8;
  private static final int GOLD = 4;
  private static final int IRON = 1;
  private static final int LEATHER = 11;
  private static final int LEG_CLOTHING = 13;
  private static final int PATINA = 9;
  private static final int TEAL_CLOTH = 3;
  private static final int VISOR = 5;

  private final ModelPart backBell;
  private final ModelPart bellClapper;

  public BellRingerModel(
      ModelPart root
  ) {
    super(root);
    this.backBell = this.body.getChild("back_bell");
    this.bellClapper = this.backBell.getChild("clapper");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    addHead(root);
    addBody(root);
    addArm(root, "right_arm", -6.0F, true);
    addArm(root, "left_arm", 6.0F, false);
    addLeg(root, "right_leg", -2.1F);
    addLeg(root, "left_leg", 2.1F);
    return LayerDefinition.create(mesh, 256, 256);
  }

  @Override
  public void setupAnim(
      @NonNull HumanoidRenderState state
  ) {
    super.setupAnim(state);
    float stride = state.walkAnimationPos * 0.6662F;
    float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
    float idle = Mth.sin(state.ageInTicks * 0.08F);
    float swing = Mth.sin(stride) * speed;

    // Keep vanilla head tracking, riding and melee poses. Only the accessories
    // receive secondary motion, so this remains a readable humanoid fighter.
    this.backBell.xRot = 0.08F + idle * 0.025F + swing * 0.09F;
    this.backBell.zRot = swing * 0.06F;
    this.bellClapper.xRot = -idle * 0.08F - swing * 0.22F;
  }

  private static CubeListBuilder material(
      int tile
  ) {
    return CubeListBuilder.create().texOffs((tile % 4) * 64 + 4, (tile / 4) * 64 + 4);
  }

  private static void addBellShell(
      PartDefinition bell
  ) {
    bell.addOrReplaceChild(
        "cap", material(BRONZE).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.ZERO
    );
    bell.addOrReplaceChild(
        "shell", material(BRONZE).addBox(-3.0F, 2.0F, -3.0F, 6.0F, 2.0F, 1.0F)
            .addBox(-3.0F, 2.0F, 2.0F, 6.0F, 2.0F, 1.0F)
            .addBox(-3.0F, 2.0F, -2.0F, 1.0F, 2.0F, 4.0F)
            .addBox(2.0F, 2.0F, -2.0F, 1.0F, 2.0F, 4.0F), PartPose.ZERO
    );
    // Four walls instead of a solid cube leave the mouth and clapper visible.
    bell.addOrReplaceChild(
        "mouth", material(ENGRAVED_BRONZE).addBox(-4.0F, 4.0F, -4.0F, 8.0F, 3.0F, 1.0F)
            .addBox(-4.0F, 4.0F, 3.0F, 8.0F, 3.0F, 1.0F)
            .addBox(-4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F)
            .addBox(3.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F), PartPose.ZERO
    );
    bell.addOrReplaceChild(
        "lip", material(GOLD).addBox(-4.5F, 7.0F, -4.5F, 9.0F, 1.0F, 1.0F)
            .addBox(-4.5F, 7.0F, 3.5F, 9.0F, 1.0F, 1.0F)
            .addBox(-4.5F, 7.0F, -3.5F, 1.0F, 1.0F, 7.0F)
            .addBox(3.5F, 7.0F, -3.5F, 1.0F, 1.0F, 7.0F), PartPose.ZERO
    );
  }

  private static void addHead(
      PartDefinition root
  ) {
    PartDefinition head = root.addOrReplaceChild(
        "head", material(BRONZE).addBox(-4.0F, -9.0F, -3.5F, 8.0F, 5.0F, 7.0F), PartPose.ZERO
    );
    // HumanoidModel resolves hat through head.getChild("hat"), not the root.
    head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
    head.addOrReplaceChild(
        "bell_rim", material(ENGRAVED_BRONZE).addBox(-5.0F, -4.0F, -4.5F, 10.0F, 1.0F, 9.0F), PartPose.ZERO
    );
    head.addOrReplaceChild(
        "crown", material(GOLD).addBox(-2.5F, -10.0F, -2.0F, 5.0F, 1.0F, 4.0F), PartPose.ZERO
    );
    head.addOrReplaceChild(
        "face", material(VISOR).addBox(-3.5F, -3.0F, -3.8F, 7.0F, 3.0F, 6.0F), PartPose.ZERO
    );
    head.addOrReplaceChild(
        "face_guard", material(BRASS).addBox(-0.5F, -3.0F, -4.5F, 1.0F, 3.0F, 1.0F), PartPose.ZERO
    );
  }

  private static void addBody(
      PartDefinition root
  ) {
    PartDefinition body = root.addOrReplaceChild(
        "body", material(DARK_CLOTH).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 12.0F, 5.0F), PartPose.ZERO
    );
    body.addOrReplaceChild(
        "cuirass", material(CHEST_ARMOR).addBox(-4.0F, 1.75F, -3.0F, 8.0F, 5.25F, 1.0F), PartPose.ZERO
    );
    body.addOrReplaceChild(
        "collar", material(PATINA).addBox(-4.6F, 0.5F, -3.0F, 9.2F, 1.0F, 6.0F), PartPose.ZERO
    );
    body.addOrReplaceChild(
        "belt", material(LEATHER).addBox(-4.75F, 9.0F, -2.75F, 9.5F, 2.0F, 5.5F), PartPose.ZERO
    );
    body.addOrReplaceChild(
        "belt_fittings", material(GOLD).addBox(-1.5F, 9.0F, -3.25F, 3.0F, 2.0F, 1.0F), PartPose.ZERO
    );
    body.addOrReplaceChild(
        "bell_harness", material(IRON).addBox(-3.5F, 0.0F, 2.5F, 1.0F, 9.0F, 1.5F)
            .addBox(2.5F, 0.0F, 2.5F, 1.0F, 9.0F, 1.5F)
            .addBox(-3.5F, 1.0F, 3.5F, 7.0F, 2.0F, 5.5F), PartPose.ZERO
    );
    PartDefinition bell = body.addOrReplaceChild(
        "back_bell", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 8.8F)
    );
    addBellShell(bell);
    bell.addOrReplaceChild(
        "clapper", material(IRON).addBox(-0.3F, 0.0F, -0.3F, 0.6F, 6.0F, 0.6F)
            .addBox(-1.0F, 6.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO
    );
  }

  private static void addArm(
      PartDefinition root,
      String name,
      float x,
      boolean armored
  ) {
    // Keep the humanoid arm layout, with clearance for the collar and cuffs.
    float minX = armored ? -3.0F : -1.0F;
    PartDefinition arm = root.addOrReplaceChild(
        name, material(TEAL_CLOTH).addBox(minX, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F),
        PartPose.offset(x, 2.0F, 0.0F)
    );
    float centerX = armored ? -1.0F : 1.0F;
    arm.addOrReplaceChild(
        "gauntlet", material(IRON).addBox(centerX - 2.2F, 5.0F, -2.2F, 4.4F, 5.0F, 4.4F), PartPose.ZERO
    );
    arm.addOrReplaceChild(
        "cuff", material(GOLD).addBox(centerX - 2.4F, 5.0F, -2.4F, 4.8F, 1.0F, 4.8F), PartPose.ZERO
    );
    if (armored) {
      arm.addOrReplaceChild(
          "bell_pauldron", material(BRONZE).addBox(-3.5F, -3.0F, -2.5F, 4.25F, 2.0F, 5.0F)
              .addBox(-4.0F, -1.0F, -3.0F, 4.75F, 3.0F, 6.0F), PartPose.ZERO
      );
      arm.addOrReplaceChild(
          "pauldron_rim", material(GOLD).addBox(-4.5F, 2.0F, -3.5F, 5.25F, 1.0F, 7.0F), PartPose.ZERO
      );
    } else {
      arm.addOrReplaceChild(
          "shoulder_mantle", material(EMBROIDERY).addBox(-0.75F, -2.5F, -2.5F, 4.25F, 4.0F, 5.0F), PartPose.ZERO
      );
    }
  }

  private static void addLeg(
      PartDefinition root,
      String name,
      float x
  ) {
    // One cube per leg; cloth, hem, greave and boot details share its texture.
    root.addOrReplaceChild(
        name, material(LEG_CLOTHING).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
        PartPose.offset(x, 12.0F, 0.0F)
    );
  }

}
