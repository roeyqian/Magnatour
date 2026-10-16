/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.universe;

// Minecraft
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.universe.UniverseCommandStar;
import roeyqian.magnatour.registry.stuff.MagnaMenus;

public class UniverseCommandStarMenu extends AbstractContainerMenu {

  private final InteractionHand commandStarHand;

  private final UniverseCommandStar.BoundBlockList boundBlocks;

  public UniverseCommandStarMenu(
      int syncId,
      UniverseCommandStar.BoundBlockList boundBlocks
  ) {
    this(syncId, boundBlocks, InteractionHand.MAIN_HAND);
  }

  public UniverseCommandStarMenu(
      int syncId,
      UniverseCommandStar.BoundBlockList boundBlocks,
      InteractionHand commandStarHand
  ) {
    super(MagnaMenus.UNIVERSE_COMMAND_STAR_HANDLER, syncId);
    this.boundBlocks = boundBlocks != null
        ? boundBlocks
        : UniverseCommandStar.BoundBlockList.EMPTY;
    this.commandStarHand = commandStarHand;
  }

  public UniverseCommandStar.BoundBlockList getBoundBlocks() {
    return boundBlocks;
  }

  public InteractionHand getCommandStarHand() {
    return commandStarHand;
  }

  @Override @NonNull
  public ItemStack quickMoveStack(
      @NonNull Player player,
      int slot
  ) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return true;
  }

}
