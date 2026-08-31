/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.block.BlockHelperForNetherPortal;

@Mixin(value = NetherPortalBlock.class, priority = 3600000)
public abstract class NetherPortalBlockMixin {

  @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
  private void inGetPortalDestinationHead(
      ServerLevel level,
      Entity entity,
      BlockPos pos,
      CallbackInfoReturnable<TeleportTransition> cir
  ) {
    BlockHelperForNetherPortal.handleDestinationLookup(level, entity, pos, cir);
  }

  @Inject(method = "getPortalDestination", at = @At("RETURN"))
  private void inGetPortalDestinationReturn(
      ServerLevel level,
      Entity entity,
      BlockPos pos,
      CallbackInfoReturnable<TeleportTransition> cir
  ) {
    BlockHelperForNetherPortal.recordDestination(level, pos, cir.getReturnValue());
  }

}
