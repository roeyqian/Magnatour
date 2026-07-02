/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.screen;

// Mojang
import com.mojang.blaze3d.platform.Window;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT) @Mixin(value = Window.class, priority = 3600000)
public interface WindowAccessor {

  @Accessor("handle")
  long getHandle();

}
