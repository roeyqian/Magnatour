/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.mixinhelper.entity.EntityHelperForEquipment;

@Mixin(value = Entity.class, priority = 3600000)
public abstract class EntityMixin {

  /* Universe Boots: Walking on lava
   */
  @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
  private void inIsInLava(
      CallbackInfoReturnable<Boolean> cir
  ) {
    EntityHelperForEquipment.handleEntityIsInLava((Entity) (Object) this, cir);
  }

  /* Universe Boots: Walking on water
   */
  @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
  private void inIsInWater(
      CallbackInfoReturnable<Boolean> cir
  ) {
    EntityHelperForEquipment.handleEntityIsInWater((Entity) (Object) this, cir);
  }

  /* Refresh flight collision before movement, including flight toggles within a tick.
   */
  @Inject(method = "move", at = @At("HEAD"))
  private void inMove(
      MoverType type,
      Vec3 movement,
      CallbackInfo ci
  ) {
    if ((Object) this instanceof Player player) {
      EntityHelperForEquipment.handleUniverseBootsFlightCollision(player);
    }
  }

  /* Universe Boots: Fluid Walking on Water and Lava
   */
  @Inject(method = "tick", at = @At("HEAD"))
  private void inTick(
      CallbackInfo ci
  ) {
    EntityHelperForEquipment.handleEntityTick((Entity) (Object) this);
  }

}
