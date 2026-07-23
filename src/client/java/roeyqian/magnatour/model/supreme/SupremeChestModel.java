/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.model.supreme;

// Java Standard
import java.util.EnumSet;
import java.util.Set;

// Minecraft
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;

public final class SupremeChestModel {

  private SupremeChestModel() {}

  public static LayerDefinition createTripleMiddleBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    Set<Direction> visibleFaces = EnumSet.of(
        Direction.NORTH,
        Direction.SOUTH,
        Direction.UP,
        Direction.DOWN
    );

    root.addOrReplaceChild(
        "bottom",
        CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 16.0F, 10.0F, 14.0F, visibleFaces),
        PartPose.ZERO
    );
    root.addOrReplaceChild(
        "lid",
        CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 5.0F, 14.0F, visibleFaces),
        PartPose.offset(0.0F, 9.0F, 1.0F)
    );
    root.addOrReplaceChild(
        "lock",
        CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F, visibleFaces),
        PartPose.offset(0.0F, 9.0F, 1.0F)
    );
    return LayerDefinition.create(mesh, 64, 64);
  }

}
