/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.block;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Minecraft
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.item.durable.UniverseOmniBlade;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;
import roeyqian.magnatour.utility.registry.item.RegDurableItems;

public final class BlockHelperForEquipment {

  private BlockHelperForEquipment() {}

  public static void handleCanEntityWalkOnPowderSnow(
      Entity entity,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (!(entity instanceof Player player)) {
      return;
    }

    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    if (boots.is(RegDurableItems.UNIVERSE_BOOTS)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleDestroyProgress(
      Player player,
      CallbackInfoReturnable<Float> cir
  ) {
    ItemStack stack = player.getItemInHand(player.getUsedItemHand());
    if (stack.is(RegDurableItems.UNIVERSE_ULTIMA_SWORD)) {
      cir.setReturnValue(0.5F);
      return;
    }

    if (stack.is(RegDurableItems.UNIVERSE_OMNI_BLADE)
        && stack.getOrDefault(RegComponentTypes.UNIVERSE_OMNI_BLADE_MODE, 0) == 1) {
      cir.setReturnValue(1.0F);
    }
  }

  public static void handleOmniBladeDrops(
      BlockBehaviour.BlockStateBase state,
      LootParams.Builder builder,
      CallbackInfoReturnable<List<ItemStack>> cir
  ) {
    ItemStack tool = (ItemStack) builder.getOptionalParameter(LootContextParams.TOOL);
    if (tool == null || !(tool.getItem() instanceof UniverseOmniBlade)) {
      return;
    }

    List<ItemStack> drops = new ArrayList<>();
    drops.add(new ItemStack(state.getBlock().asItem()));
    cir.setReturnValue(drops);
  }

}
