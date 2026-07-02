/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.insert.entity;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Magnatour
import roeyqian.magnatour.block.VirtualBlockLightManager;
import roeyqian.magnatour.block.insert.SupremeBlock;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class SupremeBlockEntity extends BlockEntity {

  private boolean registered;

  public SupremeBlockEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.SUPREME_BLOCK_ENTITY, pos, state);
  }

  public static void tick(
      Level world,
      BlockPos pos,
      BlockState state,
      SupremeBlockEntity blockEntity
  ) {
    boolean lit = state.getValue(SupremeBlock.LIT);
    blockEntity.setLightRegistered(lit);
  }

  public void setLightRegistered(
      boolean registered
  ) {
    if (this.registered == registered || this.level == null) return;

    this.registered = registered;
    VirtualBlockLightManager.setActive(this.level, this.worldPosition, SupremeBlock.LIGHT_LEVEL, registered);
  }

  @Override
  public void setRemoved() {
    setLightRegistered(false);
    super.setRemoved();
  }

}
