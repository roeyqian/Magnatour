/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.entity;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.RemoteAccessManager;
import roeyqian.magnatour.registry.content.SupremeItems;
import roeyqian.magnatour.registry.content.UniverseItems;
import roeyqian.magnatour.registry.logic.CustomComponents;

public final class EntityHelperForEquipment {

  private static final float DEFAULT_FLYING_SPEED = new Abilities().getFlyingSpeed();

  private EntityHelperForEquipment() {}

  public static void handleDefaultFlight(
      Player player,
      float defaultSpeed
  ) {
    player.getAbilities().setFlyingSpeed(defaultSpeed);
    player.onUpdateAbilities();

    if (!player.isCreative() && !player.isSpectator()) {
      player.getAbilities().mayfly = false;
      player.getAbilities().flying = false;
      player.onUpdateAbilities();
    }
  }

  public static void handleEntityIsInLava(
      Entity entity,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (entity instanceof Player player) {
      handleUniverseBootsFluidContact(player, cir);
    }
  }

  public static void handleEntityIsInWater(
      Entity entity,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (entity instanceof Player player) {
      handleUniverseBootsFluidContact(player, cir);
    }
  }

  public static void handleEntityTick(
      Entity entity
  ) {
    if (entity instanceof Player player) {
      handleUniverseBootsFluidWalking(player);
    }
  }

  public static int handleLivingBaseTick(
      LivingEntity living,
      int flightTicks
  ) {
    if (!(living instanceof Player player)) {
      return flightTicks;
    }

    if (player.getItemBySlot(EquipmentSlot.CHEST).is(UniverseItems.UNIVERSE_CHESTPLATE)) {
      return handleUniverseFlight(player, flightTicks);
    }

    if (player.getAbilities().getFlyingSpeed() != DEFAULT_FLYING_SPEED) {
      handleDefaultFlight(player, DEFAULT_FLYING_SPEED);
    }

    return flightTicks;
  }

  public static void handleLivingForceAddEffect(
      LivingEntity living,
      MobEffectInstance effect,
      CallbackInfo ci
  ) {
    if (living instanceof Player player) {
      handleUniverseHelmetImmunity(player, effect, ci);
    }
  }

  public static void handleRemoteBlockAccess(
      Player player,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (RemoteAccessManager.isRemoteAccessing(player)) {
      cir.setReturnValue(true);
    }
  }

  public static void handleSetRemainingFireTicks(
      Player player,
      CallbackInfo ci
  ) {
    if (player.getItemBySlot(EquipmentSlot.CHEST).is(UniverseItems.UNIVERSE_CHESTPLATE)) {
      ci.cancel();
    }
  }

  public static float handleSupremeArmorDefense(
      Player player,
      float amount
  ) {
    int count = 0;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      ItemStack armorStack = player.getItemBySlot(slot);
      if (!slot.isArmor() || armorStack.isEmpty()) {
        continue;
      }
      if (armorStack.is(SupremeItems.SUPREME_HELMET)
          || armorStack.is(SupremeItems.SUPREME_CHESTPLATE)
          || armorStack.is(SupremeItems.SUPREME_LEGGINGS)
          || armorStack.is(SupremeItems.SUPREME_BOOTS)) {
        count++;
      }
    }
    return Math.max(0, count == 0 ? amount : amount / (count * 3));
  }

  public static void handleTravelTail(
      Player player,
      Vec3 movementInput
  ) {
    if (!player.getItemBySlot(EquipmentSlot.LEGS).is(UniverseItems.UNIVERSE_LEGGINGS)) return;
    if (player.getAbilities().flying || movementInput.lengthSqr() <= 0) return;

    if (player.isInWater()) {
      player.moveRelative(0.1F, movementInput);
    }
    if (player.isInLava()) {
      player.moveRelative(0.1F, movementInput);
      Vec3 vel = player.getDeltaMovement();
      player.setDeltaMovement(vel.x * 1.15F, vel.y * 1.05F, vel.z * 1.15F);
    }
  }

  public static void handleUniverseArmorDefense(
      Player player,
      CallbackInfoReturnable<Boolean> cir
  ) {
    int count = 0;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      ItemStack armorStack = player.getItemBySlot(slot);
      if (!slot.isArmor() || armorStack.isEmpty()) {
        continue;
      }
      if (armorStack.is(UniverseItems.UNIVERSE_CHESTPLATE)
          || armorStack.is(UniverseItems.UNIVERSE_HELMET)
          || armorStack.is(UniverseItems.UNIVERSE_LEGGINGS)
          || armorStack.is(UniverseItems.UNIVERSE_BOOTS)) {
        count++;
      }
    }

    if (count > 0 && player.getRandom().nextFloat() < (count * 0.333F)) {
      cir.setReturnValue(false);
    }
  }

  public static void handleUniverseBootsFluidContact(
      Player player,
      CallbackInfoReturnable<Boolean> cir
  ) {
    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    if (!boots.is(UniverseItems.UNIVERSE_BOOTS)) return;
    if (player.isShiftKeyDown()) return;

    Level level = player.level();
    double surfaceY = findFluidSurfaceY(level, player);
    if (surfaceY == Double.MIN_VALUE) return;

    double maxDepth = 0.5;
    if (surfaceY - player.getY() > maxDepth) return;

    cir.setReturnValue(false);
  }

  public static void handleUniverseBootsFluidWalking(
      Player player
  ) {
    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    if (!boots.is(UniverseItems.UNIVERSE_BOOTS)) return;
    if (player.isShiftKeyDown()) return;
    if (player.getAbilities().flying || player.isSpectator()) return;

    Level level = player.level();
    double playerY = player.getY();

    double surfaceY = findFluidSurfaceY(level, player);
    if (surfaceY == Double.MIN_VALUE) return;

    double maxDepth = 0.5;
    if (surfaceY - playerY > maxDepth) return;

    if (playerY <= surfaceY) {
      player.setPos(player.getX(), surfaceY, player.getZ());
      Vec3 vel = player.getDeltaMovement();
      if (vel.y < 0) {
        player.setDeltaMovement(vel.x, 0, vel.z);
      }
      player.setOnGround(true);
      player.resetFallDistance();
    } else if (playerY < surfaceY + 0.1 && player.getDeltaMovement().y < 0) {
      player.setPos(player.getX(), surfaceY, player.getZ());
      player.setDeltaMovement(
          player.getDeltaMovement().x,
          0,
          player.getDeltaMovement().z
      );
      player.setOnGround(true);
      player.resetFallDistance();
    }
  }

  public static void handleUniverseChestplateImmunity(
      Player player,
      DamageSource source,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (player.getItemBySlot(EquipmentSlot.CHEST).is(UniverseItems.UNIVERSE_CHESTPLATE)
        && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING))) {
      cir.setReturnValue(true);
    }
  }

  public static int handleUniverseFlight(
      Player player,
      int flightTicks
  ) {
    if (!player.getAbilities().mayfly) {
      player.getAbilities().mayfly = true;
      player.onUpdateAbilities();
    }

    float baseSpeed = player.isSprinting() ? 0.2F : 0.08F;
    float targetSpeed = baseSpeed;
    boolean allowSpeedUp = player.getItemBySlot(EquipmentSlot.LEGS).is(UniverseItems.UNIVERSE_LEGGINGS);

    if (allowSpeedUp && player.getAbilities().flying && player.isSprinting()) {
      flightTicks++;
      float acceleration = flightTicks * 0.01F;
      targetSpeed = baseSpeed + acceleration;
    } else {
      flightTicks = 0;
    }

    if (player.getAbilities().getFlyingSpeed() != targetSpeed) {
      player.getAbilities().setFlyingSpeed(targetSpeed);
      player.onUpdateAbilities();
    }

    if (player.getAbilities().flying) {
      if (player.zza == 0 && player.xxa == 0) {
        Vec3 delta = player.getDeltaMovement();

        double frictionFactor = 0.75;

        if (Math.abs(delta.x) > 0.01 || Math.abs(delta.z) > 0.01) {
          player.setDeltaMovement(delta.x * frictionFactor, delta.y, delta.z * frictionFactor);
        }
      }
    }

    return flightTicks;
  }

  public static void handleUniverseHelmetFixedHunger(
      Player player,
      FoodData foodData
  ) {
    ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
    if (!head.is(UniverseItems.UNIVERSE_HELMET)) {
      return;
    }

    if (foodData != null) {
      foodData.setFoodLevel(20);
      foodData.setSaturation(5.0F);
    }
    player.setAirSupply(player.getMaxAirSupply());
  }

  public static void handleUniverseHelmetImmunity(
      Player player,
      MobEffectInstance effect,
      CallbackInfo ci
  ) {
    ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
    if (!headStack.is(UniverseItems.UNIVERSE_HELMET)) {
      return;
    }

    Holder<MobEffect> effectType = effect.getEffect();
    if (effectType.equals(MobEffects.BLINDNESS)
        || effectType.equals(MobEffects.DARKNESS)
        || effectType.equals(MobEffects.NAUSEA)
        || effectType.equals(MobEffects.HUNGER)
    ) {
      ci.cancel();
    }
  }

  public static void handleUniverseHitEffect(
      LivingEntity target,
      boolean damaged,
      DamageSource source
  ) {
    if (!damaged) return;
    if (!(source.getEntity() instanceof Player player)) return;

    ItemStack weaponStack = player.getMainHandItem();
    if (!weaponStack.is(UniverseItems.UNIVERSE_ULTIMA_SWORD)) return;

    if (weaponStack.getOrDefault(CustomComponents.UNIVERSE_ULTIMA_SWORD_MODE, 0) == 1) {
      target.hurtTime = 0;
      target.invulnerableTime = 0;
    }
  }

  public static void handleUniverseLeggingsStepping(
      LivingEntity living
  ) {
    if (!(living instanceof Player player)) return;
    if (player.level().isClientSide()) return;

    ItemStack legStack = player.getItemBySlot(EquipmentSlot.LEGS);
    AttributeInstance stepAttr = player.getAttribute(Attributes.STEP_HEIGHT);
    AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
    Identifier stepId = Identifier.fromNamespaceAndPath(
        Magnatour.MOD_ID, "universe_leggings_step_height"
    );
    Identifier jumpId = Identifier.fromNamespaceAndPath(
        Magnatour.MOD_ID, "universe_leggings_jump_boost"
    );

    if (legStack.is(UniverseItems.UNIVERSE_LEGGINGS)) {
      if (stepAttr != null && !stepAttr.hasModifier(stepId)) {
        stepAttr.addTransientModifier(
            new AttributeModifier(stepId, 0.5, AttributeModifier.Operation.ADD_VALUE)
        );
      }
      if (jumpAttr != null && !jumpAttr.hasModifier(jumpId)) {
        jumpAttr.addTransientModifier(
            new AttributeModifier(jumpId, 0.15, AttributeModifier.Operation.ADD_VALUE)
        );
      }
    } else {
      if (stepAttr != null && stepAttr.hasModifier(stepId)) {
        stepAttr.removeModifier(stepId);
      }
      if (jumpAttr != null && jumpAttr.hasModifier(jumpId)) {
        jumpAttr.removeModifier(jumpId);
      }
    }
  }

  private static double findFluidSurfaceY(
      Level level,
      Player player
  ) {
    BlockPos.MutableBlockPos mutable = player.blockPosition().mutable();

    boolean foundFluid = false;
    for (int i = 0; i <= 3; i++) {
      FluidState fluidState = level.getFluidState(mutable);
      if (!fluidState.isEmpty()
          && (fluidState.is(FluidTags.WATER) || fluidState.is(FluidTags.LAVA))
      ) {
        foundFluid = true;
        break;
      }
      mutable.move(net.minecraft.core.Direction.DOWN);
    }

    if (!foundFluid) return Double.MIN_VALUE;

    while (true) {
      BlockPos above = mutable.above();
      FluidState aboveFluid = level.getFluidState(above);
      if (aboveFluid.isEmpty()) {
        FluidState topFluid = level.getFluidState(mutable);
        return mutable.getY() + topFluid.getHeight(level, mutable);
      }
      mutable.move(net.minecraft.core.Direction.UP);

      if (mutable.getY() > player.getY() + 10) return Double.MIN_VALUE;
    }
  }

}
