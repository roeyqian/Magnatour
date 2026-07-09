/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.menu;

// Minecraft
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AnvilMenu.class, priority = 3600000)
public interface AnvilMenuAccessor {

  @Accessor("cost")
  DataSlot getCost();

}
