/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.item.universe.UniverseOmniBlade;
import roeyqian.magnatour.registry.logic.MagnaComponents;
import roeyqian.magnatour.registry.stuff.MagnaItems;

/** Universe Omni Blade mining, block drops and shovel mode. */
public final class UniverseOmniBladeMain {

  private static final float UNIVERSE_OMNI_BLADE_MODE_0_DESTROY_PROGRESS = 0.02F;
  private static final float UNIVERSE_OMNI_BLADE_MODE_1_DESTROY_PROGRESS = 1.0F;

  private UniverseOmniBladeMain() {}

  public static void handleBlockBreakAction(
      ServerLevel level,
      ServerPlayer player,
      BlockPos pos,
      ServerboundPlayerActionPacket.Action action,
      Direction direction,
      CallbackInfo ci
  ) {
    if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
      return;
    }

    ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
    if (stack.is(MagnaItems.UNIVERSE_OMNI_BLADE)
        && stack.getOrDefault(MagnaComponents.UNIVERSE_OMNI_BLADE_MODE, 0) == 0) {
      execShovelMode(level, player, direction, pos, ci);
    }
  }

  public static void handleDestroyProgress(
      Player player,
      CallbackInfoReturnable<Float> cir
  ) {
    ItemStack stack = player.getItemInHand(player.getUsedItemHand());
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

  private static void execShovelMode(
      ServerLevel level,
      ServerPlayer player,
      Direction direction,
      BlockPos pos,
      CallbackInfo ci
  ) {
    if (direction == Direction.DOWN || !level.getBlockState(pos.above()).isAir()) {
      return;
    }

    BlockState state = level.getBlockState(pos);
    BlockState newState = null;

    if (state.is(Blocks.GRASS_BLOCK)
        || state.is(Blocks.DIRT)
        || state.is(Blocks.COARSE_DIRT)
        || state.is(Blocks.ROOTED_DIRT)) {
      newState = Blocks.DIRT_PATH.defaultBlockState();
    }

    if (newState == null) {
      return;
    }

    player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
    level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
    level.setBlock(pos, newState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
    ci.cancel();
  }

}
