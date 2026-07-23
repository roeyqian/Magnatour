/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.block.BlockHelperForFunction;

@Mixin(value = ResultSlot.class, priority = 3600000)
public abstract class ResultSlotMixin extends Slot {

  @Shadow
  private int removeCount;

  @Shadow @Final
  private Player player;

  @Shadow @Final
  private CraftingContainer craftSlots;

  public ResultSlotMixin(
      Container inventory,
      int index,
      int x,
      int y
  ) {
    super(inventory, index, x, y);
  }

  @Shadow
  protected abstract void checkTakeAchievements(
      ItemStack stack
  );

  @Inject(method = "onQuickCraft(Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"), cancellable = true)
  private void inOnQuickCraft(
      ItemStack stack,
      int amount,
      CallbackInfo ci
  ) {
    this.removeCount = BlockHelperForFunction.handleQuickCraft(
        this.player,
        this.craftSlots,
        this.removeCount,
        amount,
        ci
    );
  }

  /* Universe Workstation & Supreme Worktable: Item Deduction
   */
  @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
  private void inOnTake(
      Player player,
      ItemStack stack,
      CallbackInfo ci
  ) {
    BlockHelperForFunction.handleOnTake(
        player,
        this.craftSlots,
        stack,
        this.removeCount,
        this::checkTakeAchievements,
        ci
    );
  }

}
