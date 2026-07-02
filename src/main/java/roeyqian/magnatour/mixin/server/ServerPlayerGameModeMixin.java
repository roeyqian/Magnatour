/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.server;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.utility.mixin.server.ServerHelperForEquipment;

@Mixin(value = ServerPlayerGameMode.class, priority = 3600000)
public class ServerPlayerGameModeMixin {

  @Shadow
  protected ServerLevel level;

  @Shadow @Final
  protected ServerPlayer player;

  /* Universe Omni Blade: Shoveling Earth
   */
  @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
  private void inHandleBlockBreakAction(
      BlockPos pos,
      ServerboundPlayerActionPacket.Action action,
      Direction direction,
      int worldHeight,
      int sequence,
      CallbackInfo ci
  ) {
    ServerHelperForEquipment.handleBlockBreakAction(
        this.level, this.player,
        pos, action, direction, ci
    );
  }

}
