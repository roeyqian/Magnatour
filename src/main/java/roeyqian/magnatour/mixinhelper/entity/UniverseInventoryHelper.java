/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.entity;

// Minecraft
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// Magnatour
import roeyqian.magnatour.registry.content.UniverseItems;

public final class UniverseInventoryHelper {

  private UniverseInventoryHelper() {}

  public static void dropExcess(
      Player player
  ) {
    if (player.level().isClientSide()) return;
    Inventory inventory = player.getInventory();
    boolean changed = false;
    for (ItemStack stack : inventory.getNonEquipmentItems()) {
      changed |= dropExcess(player, stack);
    }
    changed |= dropExcess(player, player.getItemBySlot(EquipmentSlot.OFFHAND));
    changed |= dropExcess(player, player.containerMenu.getCarried());
    if (changed) {
      inventory.setChanged();
      player.containerMenu.broadcastChanges();
    }
  }

  public static int getArmorStackLimit(
      Player player
  ) {
    int pieces = 0;
    if (player.getItemBySlot(EquipmentSlot.HEAD).is(UniverseItems.UNIVERSE_HELMET)) pieces++;
    if (player.getItemBySlot(EquipmentSlot.CHEST).is(UniverseItems.UNIVERSE_CHESTPLATE)) pieces++;
    if (player.getItemBySlot(EquipmentSlot.LEGS).is(UniverseItems.UNIVERSE_LEGGINGS)) pieces++;
    if (player.getItemBySlot(EquipmentSlot.FEET).is(UniverseItems.UNIVERSE_BOOTS)) pieces++;
    return pieces == 0 ? 0 : 64 << (pieces - 1);
  }

  public static int getStackLimit(
      Player player,
      ItemStack stack
  ) {
    int limit = getArmorStackLimit(player);
    return limit == 0 ? stack.getMaxStackSize() : limit;
  }

  public static boolean hasArmor(
      AbstractContainerMenu menu
  ) {
    for (Slot slot : menu.slots) {
      if (slot.container instanceof Inventory inventory) {
        return getArmorStackLimit(inventory.player) > 0;
      }
    }
    return false;
  }

  private static boolean dropExcess(
      Player player,
      ItemStack stack
  ) {
    if (stack.isEmpty()) return false;
    int limit = getStackLimit(player, stack);
    if (stack.getCount() <= limit) return false;
    // Ground stacks retain vanilla limits, including tools with a limit of one.
    int dropLimit = stack.getMaxStackSize();
    while (stack.getCount() > limit) {
      player.drop(stack.split(Math.min(dropLimit, stack.getCount() - limit)), false, Prediction.SERVER_ONLY);
    }
    return true;
  }

}
