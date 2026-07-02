/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.screen;

// Minecraft
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GhostSlots.class, priority = 3600000)
public interface GhostSlotsInvoker {

  @Invoker("setInput")
  void invokeSetInput(
      Slot slot,
      ContextMap context,
      SlotDisplay display
  );

  @Invoker("setResult")
  void invokeSetResult(
      Slot slot,
      ContextMap context,
      SlotDisplay display
  );

}