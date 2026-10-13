/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.server;

// Minecraft
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 3600000)
public class ServerGamePacketListenerImplMixin {

  @Redirect(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
  private int acceptExpandedInventoryStack(
      ItemStack stack,
      ServerboundSetCreativeModeSlotPacket packet
  ) {
    int slot = packet.slotNum();
    // Creative inventory edits arrive as multiple packets. Accept the previous
    // armor limit until END_SERVER_TICK enforces the final equipment state.
    if (slot >= 9 && slot <= 45 && stack.getMaxStackSize() > 1) return Math.max(512, stack.getMaxStackSize());
    return stack.getMaxStackSize();
  }

}
