/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.output;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.TooltipDisplay;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.supreme.EmperorArmor;
import roeyqian.magnatour.registry.logic.CustomComponents;

@Environment(EnvType.CLIENT)
public final class RegItemTooltips {

  private RegItemTooltips() {}

  public static void init() {
    ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
      TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
      if (display.hideTooltip()) return;

      if (stack.get(CustomComponents.UNIVERSE_LIBRARY_CONTENTS) != null) {
        List<Component> contents = new ArrayList<>();
        stack.addToTooltip(CustomComponents.UNIVERSE_LIBRARY_CONTENTS, context, display, contents::add, flag);
        lines.addAll(Math.min(1, lines.size()), contents);
      }

      if (stack.getItem() instanceof EmperorArmor armor) {
        lines.add(Math.min(1, lines.size()), armor.getAttributesTooltip(stack));
      }
    });
    Magnatour.LOGGER.info("[Client] Initializing 'RegItemTooltips'");
  }

}
