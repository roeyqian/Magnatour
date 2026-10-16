/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.world;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.item.universe.UniverseUltimaSword;
import roeyqian.magnatour.mixinhelper.supreme.EmperorArmorMain;
import roeyqian.magnatour.mixinhelper.supreme.EmperorEnchantMain;
import roeyqian.magnatour.mixinhelper.universe.UniverseRemoteAccessMain;
import roeyqian.magnatour.mixinhelper.universe.UniverseVoidArmorMain;
import roeyqian.magnatour.registry.logic.MagnaComponents;

// Other
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(value = Player.class, priority = 3600000)
public class PlayerMixin {

  /* Universe Void Chestplate & Universe Void Boots: Flight Through Walls
   */
  @Redirect(method = "tick", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z", ordinal = 0))
  private boolean allowUniverseVoidFlightThroughWalls(
      Player player
  ) {
    return player.isSpectator() || UniverseVoidArmorMain.canUniverseVoidFlyThroughWalls(player);
  }

  /* Universe Ultima Sword: Force Normal Death Without Applying Damage
   */
  @SuppressWarnings("deprecation")
  @WrapOperation(method = "attack", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
  private boolean executeUniverseSwordTarget(
      Entity target,
      DamageSource source,
      float amount,
      Operation<Boolean> original,
      @Local(ordinal = 2) boolean critical
  ) {
    Player player = (Player) (Object) this;
    ItemStack weapon = player.getWeaponItem();
    if (player.level() instanceof ServerLevel world
        && target instanceof LivingEntity living && living.isAlive()
        && weapon.getItem() instanceof UniverseUltimaSword sword
        && weapon.getOrDefault(MagnaComponents.UNIVERSE_ULTIMA_SWORD_MODE, 0) == 1) {
      // Trigger execution even when immunity would prevent a successful hit.
      // Use vanilla's already-computed critical flag, before any death effects.
      sword.executeUniverseAttack(world, living, player, critical);
      // Report a successful hit so vanilla can perform the sword's sweep attack.
      return true;
    }
    return original.call(target, source, amount);
  }

  /* Universe Void Armors: Absolute Defense
   */
  @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
  private void inHurtServer(
      ServerLevel world,
      DamageSource source,
      float amount,
      CallbackInfoReturnable<Boolean> cir
  ) {
    Player player = (Player) (Object) this;
    UniverseVoidArmorMain.handleUniverseVoidArmorDefense(player, cir);
  }

  /* Universe Void Chestplate: Fire and Freezing Immunity
   */
  @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
  private void inIsInvulnerableTo(
      ServerLevel world,
      DamageSource source,
      CallbackInfoReturnable<Boolean> cir
  ) {
    UniverseVoidArmorMain.handleUniverseVoidChestplateImmunity((Player) (Object) this, source, cir);
  }

  /* Universe Command Star: Remote Accessibility
   */
  @Inject(method = "isWithinBlockInteractionRange", at = @At("HEAD"), cancellable = true)
  private void inIsWithinBlockInteractionRange(
      BlockPos pos,
      double buffer,
      CallbackInfoReturnable<Boolean> cir
  ) {
    UniverseRemoteAccessMain.handleRemoteBlockAccess((Player) (Object) this, cir);
  }

  /* Universe Void Chestplate: Fire Immunity Optimization
   */
  @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
  private void inSetRemainingFireTicks(
      int ticks,
      CallbackInfo ci
  ) {
    UniverseVoidArmorMain.handleSetRemainingFireTicks((Player) (Object) this, ci);
  }

  /* Universe Void Chestplate & Universe Void Leggings: Smooth Movement (when flying or swimming)
   */
  @Inject(method = "travel", at = @At("TAIL"))
  private void inTravel(
      Vec3 movementInput,
      CallbackInfo ci
  ) {
    UniverseVoidArmorMain.handleTravelTail((Player) (Object) this, movementInput);
  }

  /* Supreme Armors: Damage Reduction
   */
  @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private float modvarHurtServer(
      float amount,
      ServerLevel world,
      DamageSource source
  ) {
    return EmperorArmorMain.handleSupremeArmorDefense((Player) (Object) this, amount);
  }

  /* Emperor Tools: Tenfold Efficiency bonus before vanilla mining multipliers.
   */
  @Redirect(
      method = "getDestroySpeed",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D",
          ordinal = 0
      )
  )
  private double redirectMiningEfficiency(
      Player player,
      Holder<Attribute> attribute
  ) {
    return EmperorEnchantMain.amplifyEmperorMiningEfficiency(
        player.getMainHandItem(), player.getAttributeValue(attribute)
    );
  }

}
