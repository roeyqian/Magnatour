/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.client;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.gen.network.UniverseBucketPickupPayload;
import roeyqian.magnatour.item.durable.UniverseBucket;

@Environment(EnvType.CLIENT)
public final class ClientHelperForEquipment {

  private ClientHelperForEquipment() {}

  public static void handleStartAttack(
      Minecraft client,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (!canTryBucketPickup(client)) return;

    BlockHitResult hitResult = getPlayerFluidSourceHitResult(client);
    if (hitResult.getType() != HitResult.Type.BLOCK || !canPickupFluidAt(client, hitResult)) return;

    ClientPlayNetworking.send(new UniverseBucketPickupPayload());
    client.player.swing(InteractionHand.MAIN_HAND);
    cir.setReturnValue(true);
  }

  private static boolean canTryBucketPickup(
      Minecraft client
  ) {
    if (client.player == null || client.level == null || client.gameMode == null) return false;
    if (client.gameMode.isSpectator() || client.player.isHandsBusy()) return false;

    ItemStack stack = client.player.getMainHandItem();
    return stack.getItem() instanceof UniverseBucket
        && stack.isItemEnabled(client.level.enabledFeatures());
  }

  private static BlockHitResult getPlayerFluidSourceHitResult(
      Minecraft client
  ) {
    Vec3 from = client.player.getEyePosition();
    Vec3 to = from.add(
        client.player.calculateViewVector(client.player.getXRot(), client.player.getYRot())
            .scale(client.player.blockInteractionRange())
    );
    return client.level.clip(
        new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, client.player)
    );
  }

  private static boolean canPickupFluidAt(
      Minecraft client,
      BlockHitResult hitResult
  ) {
    BlockState state = client.level.getBlockState(hitResult.getBlockPos());
    Fluid fluid = state.getFluidState().getType();
    return state.getBlock() instanceof BucketPickup
        && (fluid == Fluids.WATER || fluid == Fluids.LAVA);
  }

}
