/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.blockentity.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Magnatour
import roeyqian.magnatour.block.universe.UniverseBlock;
import roeyqian.magnatour.block.VirtualBlockLightManager;
import roeyqian.magnatour.registry.content.UniverseBlockEntities;

public class UniverseBlockEntity extends BlockEntity {

  private boolean registered;

  public UniverseBlockEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(UniverseBlockEntities.UNIVERSE_BLOCK_ENTITY, pos, state);
  }

  public static void tick(
      Level world,
      BlockPos pos,
      BlockState state,
      UniverseBlockEntity blockEntity
  ) {
    boolean lit = state.getValue(UniverseBlock.LIT);
    blockEntity.setLightRegistered(lit);
  }

  public void setLightRegistered(
      boolean registered
  ) {
    if (this.registered == registered || this.level == null) return;

    this.registered = registered;
    VirtualBlockLightManager.setActive(this.level, this.worldPosition, registered);
  }

  @Override
  public void setRemoved() {
    setLightRegistered(false);
    super.setRemoved();
  }

}
