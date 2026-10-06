/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.output;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

// Minecraft
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.supreme.EmperorArmor;

@Environment(EnvType.CLIENT)
public final class RegItemTooltips {

  private RegItemTooltips() {}

  public static void init() {
    ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
      if (stack.getItem() instanceof EmperorArmor armor
          && !stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).hideTooltip()) {
        lines.add(Math.min(1, lines.size()), armor.getAttributesTooltip(stack));
      }
    });
    Magnatour.LOGGER.info("[Client] Initializing 'RegItemTooltips'");
  }

}
