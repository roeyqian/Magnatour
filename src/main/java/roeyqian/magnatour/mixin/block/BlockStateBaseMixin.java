/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootParams;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.block.BlockHelperForEquipment;

@Mixin(value = BlockBehaviour.BlockStateBase.class, priority = 3600000)
public abstract class BlockStateBaseMixin {

  /* Universe Ultima Sword & Universe Omni Blade: Mining of Fixed Speed
   */
  @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
  public void inGetDestroyProgress(
      Player player,
      BlockGetter world,
      BlockPos pos,
      CallbackInfoReturnable<Float> cir
  ) {
    BlockHelperForEquipment.handleDestroyProgress(player, cir);
  }

  /* Universe Omni Blade: Capability of Mining Everything
   */
  @Inject(method = "getDrops", at = @At("HEAD"), cancellable = true)
  private void inGetDrops(
      LootParams.Builder builder,
      CallbackInfoReturnable<List<ItemStack>> cir
  ) {
    BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase) (Object) this;
    BlockHelperForEquipment.handleOmniBladeDrops(state, builder, cir);
  }

}
