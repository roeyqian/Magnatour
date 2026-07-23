/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.universe;

// Minecraft
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.registry.content.RegItemMenus;

public class UniverseConsoleMenu extends AbstractContainerMenu {

  private final UniverseConsole.BoundBlockList boundBlocks;

  public UniverseConsoleMenu(
      int syncId,
      UniverseConsole.BoundBlockList boundBlocks
  ) {
    super(RegItemMenus.UNIVERSE_CONSOLE_HANDLER, syncId);
    this.boundBlocks = boundBlocks != null
        ? boundBlocks
        : UniverseConsole.BoundBlockList.EMPTY;
  }

  public UniverseConsole.BoundBlockList getBoundBlocks() {
    return boundBlocks;
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
