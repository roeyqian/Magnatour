/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.block;

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
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.registry.logic.MagnaComponents;
import roeyqian.magnatour.registry.stuff.MagnaItems;

public final class BlockHelperForEquipment {

  private static final float UNIVERSE_OMNI_BLADE_MODE_0_DESTROY_PROGRESS = 0.02F;
  private static final float UNIVERSE_OMNI_BLADE_MODE_1_DESTROY_PROGRESS = 1.0F;
  private static final float UNIVERSE_ULTIMA_SWORD_DESTROY_PROGRESS = 0.5F;

  private BlockHelperForEquipment() {}

  public static void handleCanEntityWalkOnPowderSnow(
      Entity entity,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (!(entity instanceof Player player)) {
      return;
    }

    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    if (boots.is(MagnaItems.UNIVERSE_VOID_BOOTS)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleDestroyProgress(
      Player player,
      CallbackInfoReturnable<Float> cir
  ) {
    ItemStack stack = player.getItemInHand(player.getUsedItemHand());
    if (stack.is(MagnaItems.UNIVERSE_ULTIMA_SWORD)) {
      cir.setReturnValue(UNIVERSE_ULTIMA_SWORD_DESTROY_PROGRESS);
      return;
    }

    if (!stack.is(MagnaItems.UNIVERSE_OMNI_BLADE)) {
      return;
    }

    int mode = stack.getOrDefault(MagnaComponents.UNIVERSE_OMNI_BLADE_MODE, 0);
    if (mode == 0) {
      cir.setReturnValue(UNIVERSE_OMNI_BLADE_MODE_0_DESTROY_PROGRESS);
      return;
    }

    if (mode == 1) {
      cir.setReturnValue(UNIVERSE_OMNI_BLADE_MODE_1_DESTROY_PROGRESS);
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

    // Mode 0 uses normal block loot, including Omniscience's Fortune bonuses.
    if (tool.getOrDefault(MagnaComponents.UNIVERSE_OMNI_BLADE_MODE, 0) == 0) {
      return;
    }

    List<ItemStack> drops = new ArrayList<>();
    drops.add(new ItemStack(state.getBlock().asItem()));
    cir.setReturnValue(drops);
  }

}
