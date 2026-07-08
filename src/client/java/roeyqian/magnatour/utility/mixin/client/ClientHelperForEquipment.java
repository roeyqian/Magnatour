/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.mixin.client;

// Java Standard
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
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

// Lightweight Java Game Library
import org.lwjgl.glfw.GLFW;

// Magnatour
import roeyqian.magnatour.gen.network.UniverseBucketPickupPayload;
import roeyqian.magnatour.item.durable.UniverseBucket;
import roeyqian.magnatour.mixin.screen.WindowAccessor;
import roeyqian.magnatour.screen.item.UniverseConsoleScreen;

@Environment(EnvType.CLIENT)
public final class ClientHelperForEquipment {

  private static final Map<Minecraft, MouseRestoreState> SAVED_MOUSE_POSITIONS = Collections.synchronizedMap(
      new WeakHashMap<>()
  );

  private ClientHelperForEquipment() {}

  public static void handleAfterSetScreen(
      Minecraft client,
      Screen newScreen
  ) {
    if (newScreen == null) return;

    MouseRestoreState state;
    synchronized (SAVED_MOUSE_POSITIONS) {
      state = SAVED_MOUSE_POSITIONS.remove(client);
    }
    if (state == null) return;

    long windowHandle = ((WindowAccessor) (Object) client.getWindow()).getHandle();
    GLFW.glfwSetCursorPos(windowHandle, state.mouseX, state.mouseY);
  }

  public static void handleBeforeSetScreen(
      Minecraft client,
      MouseHandler mouseHandler,
      Screen currentScreen,
      Screen newScreen
  ) {
    if (newScreen != null || !(currentScreen instanceof UniverseConsoleScreen)) return;

    synchronized (SAVED_MOUSE_POSITIONS) {
      SAVED_MOUSE_POSITIONS.put(client, new MouseRestoreState(mouseHandler.xpos(), mouseHandler.ypos()));
    }
  }

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

  private static final class MouseRestoreState {

    private final double mouseX;
    private final double mouseY;

    private MouseRestoreState(
        double mouseX,
        double mouseY
    ) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
    }

  }

}
