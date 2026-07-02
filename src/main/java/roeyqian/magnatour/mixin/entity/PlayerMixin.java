/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.entity;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.utility.mixin.entity.EntityHelperForEquipment;

@Mixin(value = Player.class, priority = 3600000)
public class PlayerMixin {

  @Shadow
  protected FoodData foodData;

  /* Universe Helmet: Fixed Hunger
   */
  @Inject(method = "tick", at = @At("TAIL"))
  public void inTick(
      CallbackInfo ci
  ) {
    EntityHelperForEquipment.handleUniverseHelmetFixedHunger((Player) (Object) this, this.foodData);
  }

  /* Universe Armors: Absolute Defense
   */
  @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
  private void inHurtServer(
      ServerLevel world,
      DamageSource source,
      float amount,
      CallbackInfoReturnable<Boolean> cir
  ) {
    Player player = (Player) (Object) this;
    EntityHelperForEquipment.handleUniverseArmorDefense(player, cir);
  }

  /* Universe Chestplate: Fire and Freezing Immunity
   */
  @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
  private void inIsInvulnerableTo(
      ServerLevel world,
      DamageSource source,
      CallbackInfoReturnable<Boolean> cir
  ) {
    EntityHelperForEquipment.handleUniverseChestplateImmunity((Player) (Object) this, source, cir);
  }

  /* Universe Console: Remote Accessibility
   */
  @Inject(method = "isWithinBlockInteractionRange", at = @At("HEAD"), cancellable = true)
  private void inIsWithinBlockInteractionRange(
      BlockPos pos,
      double buffer,
      CallbackInfoReturnable<Boolean> cir
  ) {
    EntityHelperForEquipment.handleRemoteBlockAccess((Player) (Object) this, cir);
  }

  /* Universe Chestplate: Fire Immunity Optimization
   */
  @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
  private void inSetRemainingFireTicks(
      int ticks,
      CallbackInfo ci
  ) {
    EntityHelperForEquipment.handleSetRemainingFireTicks((Player) (Object) this, ci);
  }

  /* Universe Chestplate & Leggings: Smooth Movement (when flying or swimming)
   */
  @Inject(method = "travel", at = @At("TAIL"))
  private void inTravel(
      Vec3 movementInput,
      CallbackInfo ci
  ) {
    EntityHelperForEquipment.handleTravelTail((Player) (Object) this, movementInput);
  }

  /* Supreme Armors: Damage Reduction
   */
  @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private float modvarHurtServer(
      float amount,
      ServerLevel world,
      DamageSource source
  ) {
    return EntityHelperForEquipment.handleSupremeArmorDefense((Player) (Object) this, amount);
  }

}
