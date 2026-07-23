/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderstate.supreme;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.ChestType;

@Environment(EnvType.CLIENT)
public final class SupremeChestRenderState extends BlockEntityRenderState {

  public int connectedChestCount = 1;
  public int connectedChestIndex = 0;

  public float lidProgress;

  public Direction facing = Direction.SOUTH;

  public ChestType type = ChestType.SINGLE;

}
