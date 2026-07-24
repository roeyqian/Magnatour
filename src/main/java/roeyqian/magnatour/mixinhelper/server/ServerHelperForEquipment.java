/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.server;

// Java Standard
import java.util.OptionalInt;

// Minecraft
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.registry.logic.CustomComponents;
import roeyqian.magnatour.registry.content.UniverseItems;

public final class ServerHelperForEquipment {

  private ServerHelperForEquipment() {}

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
    if (stack.is(UniverseItems.UNIVERSE_OMNI_BLADE)
        && stack.getOrDefault(CustomComponents.UNIVERSE_OMNI_BLADE_MODE, 0) == 0) {
      execShovelMode(level, player, direction, pos, ci);
    }
  }

  public static void handleBucketPickup(
      ServerPlayer player
  ) {
    ItemStack stack = player.getMainHandItem();
    if (!stack.is(UniverseItems.UNIVERSE_BUCKET)) return;

    BlockHitResult hitResult = getPlayerFluidSourceHitResult(player);
    if (hitResult.getType() != HitResult.Type.BLOCK) return;

    BlockPos pos = hitResult.getBlockPos();
    Direction direction = hitResult.getDirection();
    BlockPos directionOffsetPos = pos.relative(direction);
    if (!player.level().mayInteract(player, pos)
        || !player.mayUseItemAt(directionOffsetPos, direction, stack)) {
      return;
    }

    BlockState blockState = player.level().getBlockState(pos);
    OptionalInt nextMode = getBucketModeForFluid(blockState.getFluidState().getType());
    if (nextMode.isEmpty() || !(blockState.getBlock() instanceof BucketPickup bucketPickupBlock)) {
      return;
    }

    ItemStack taken = bucketPickupBlock.pickupBlock(player, player.level(), pos, blockState);
    if (taken.isEmpty()) return;

    stack.set(CustomComponents.UNIVERSE_BUCKET_MODE, nextMode.getAsInt());
    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    player.level().playSound(
        null,
        pos,
        nextMode.getAsInt() == 1 ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL,
        SoundSource.BLOCKS,
        1.0F,
        1.0F
    );
    player.level().gameEvent(player, GameEvent.FLUID_PICKUP, pos);
    CriteriaTriggers.FILLED_BUCKET.trigger(player, taken);
    player.swing(InteractionHand.MAIN_HAND, true);
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

    player.swing(InteractionHand.MAIN_HAND, true);
    level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0f, 1.0f);
    level.setBlock(pos, newState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
    ci.cancel();
  }

  private static BlockHitResult getPlayerFluidSourceHitResult(
      ServerPlayer player
  ) {
    Vec3 from = player.getEyePosition();
    Vec3 to = from.add(
        player.calculateViewVector(player.getXRot(), player.getYRot())
            .scale(player.blockInteractionRange())
    );
    return player.level().clip(
        new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player)
    );
  }

  private static OptionalInt getBucketModeForFluid(
      Fluid fluid
  ) {
    if (fluid == Fluids.WATER) return OptionalInt.of(0);
    if (fluid == Fluids.LAVA) return OptionalInt.of(1);
    return OptionalInt.empty();
  }

}
