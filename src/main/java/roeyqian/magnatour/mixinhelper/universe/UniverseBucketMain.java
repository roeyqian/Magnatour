/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Java Standard
import java.util.OptionalInt;

// Minecraft
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// Magnatour
import roeyqian.magnatour.registry.stuff.MagnaItems;

/** Universe Bucket fluid pickup. */
public final class UniverseBucketMain {

  private UniverseBucketMain() {}

  public static void handleBucketPickup(
      ServerPlayer player
  ) {
    ItemStack stack = player.getMainHandItem();
    if (!stack.is(MagnaItems.UNIVERSE_BUCKET)) return;

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
    OptionalInt fluidMode = getBucketModeForFluid(blockState.getFluidState().getType());
    if (fluidMode.isEmpty() || !(blockState.getBlock() instanceof BucketPickup bucketPickupBlock)) {
      return;
    }

    ItemStack taken = bucketPickupBlock.pickupBlock(player, player.level(), pos, blockState);
    if (taken.isEmpty()) return;

    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    player.level().playSound(
        null,
        pos,
        fluidMode.getAsInt() == 1 ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL,
        SoundSource.BLOCKS,
        1.0F,
        1.0F
    );
    player.level().gameEvent(player, GameEvent.FLUID_PICKUP, pos);
    CriteriaTriggers.FILLED_BUCKET.trigger(player, taken);
    player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
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
