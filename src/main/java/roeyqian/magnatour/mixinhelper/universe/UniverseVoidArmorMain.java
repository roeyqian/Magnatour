/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Prediction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.registry.stuff.MagnaItems;

/** Universe Void armor abilities, including flight, movement, defense and inventory limits. */
public final class UniverseVoidArmorMain {

  private static final float DEFAULT_FLYING_SPEED = new Abilities().getFlyingSpeed();

  private UniverseVoidArmorMain() {}

  public static boolean canUniverseVoidFlyThroughWalls(
      Player player
  ) {
    return player.getAbilities().flying
        && player.getItemBySlot(EquipmentSlot.CHEST).is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)
        && player.getItemBySlot(EquipmentSlot.FEET).is(MagnaItems.UNIVERSE_VOID_BOOTS);
  }

  public static void dropExcess(
      Player player
  ) {
    if (player.level().isClientSide()) return;
    Inventory inventory = player.getInventory();
    boolean changed = false;
    for (ItemStack stack : inventory.getNonEquipmentItems()) {
      changed |= dropExcess(player, stack);
    }
    changed |= dropExcess(player, player.getItemBySlot(EquipmentSlot.OFFHAND));
    changed |= dropExcess(player, player.containerMenu.getCarried());
    if (changed) {
      inventory.setChanged();
      player.containerMenu.broadcastChanges();
    }
  }

  public static int getArmorStackLimit(
      Player player
  ) {
    int pieces = 0;
    if (player.getItemBySlot(EquipmentSlot.HEAD).is(MagnaItems.UNIVERSE_VOID_HELMET)) pieces++;
    if (player.getItemBySlot(EquipmentSlot.CHEST).is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)) pieces++;
    if (player.getItemBySlot(EquipmentSlot.LEGS).is(MagnaItems.UNIVERSE_VOID_LEGGINGS)) pieces++;
    if (player.getItemBySlot(EquipmentSlot.FEET).is(MagnaItems.UNIVERSE_VOID_BOOTS)) pieces++;
    return pieces == 0 ? 0 : 64 << (pieces - 1);
  }

  public static int getStackLimit(
      Player player,
      ItemStack stack
  ) {
    if (stack.getMaxStackSize() <= 1) return stack.getMaxStackSize();
    int limit = getArmorStackLimit(player);
    return limit == 0 ? stack.getMaxStackSize() : limit;
  }

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
      handleUniverseVoidBootsFluidContact(player, cir);
    }
  }

  public static void handleEntityIsInWater(
      Entity entity,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (entity instanceof Player player) {
      handleUniverseVoidBootsFluidContact(player, cir);
    }
  }

  public static void handleEntityTick(
      Entity entity
  ) {
    if (entity instanceof Player player) {
      handleUniverseVoidBootsFluidWalking(player);
    }
  }

  public static int handleLivingBaseTick(
      LivingEntity living,
      int flightTicks
  ) {
    if (!(living instanceof Player player)) {
      return flightTicks;
    }

    if (player.getItemBySlot(EquipmentSlot.CHEST).is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)) {
      return handleUniverseVoidFlight(player, flightTicks);
    }

    if (player.getAbilities().getFlyingSpeed() != DEFAULT_FLYING_SPEED) {
      handleDefaultFlight(player, DEFAULT_FLYING_SPEED);
    }

    return flightTicks;
  }

  public static void handleSetRemainingFireTicks(
      Player player,
      CallbackInfo ci
  ) {
    if (player.getItemBySlot(EquipmentSlot.CHEST).is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)) {
      ci.cancel();
    }
  }

  public static void handleTravelTail(
      Player player,
      Vec3 movementInput
  ) {
    if (!player.getItemBySlot(EquipmentSlot.LEGS).is(MagnaItems.UNIVERSE_VOID_LEGGINGS)) return;
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

  public static void handleUniverseVoidArmorDefense(
      Player player,
      CallbackInfoReturnable<Boolean> cir
  ) {
    int count = 0;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      ItemStack armorStack = player.getItemBySlot(slot);
      if (!slot.isArmor() || armorStack.isEmpty()) {
        continue;
      }
      if (armorStack.is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)
          || armorStack.is(MagnaItems.UNIVERSE_VOID_HELMET)
          || armorStack.is(MagnaItems.UNIVERSE_VOID_LEGGINGS)
          || armorStack.is(MagnaItems.UNIVERSE_VOID_BOOTS)) {
        count++;
      }
    }

    if (count > 0 && player.getRandom().nextFloat() < (count * 0.333F)) {
      cir.setReturnValue(false);
    }
  }

  public static void handleUniverseVoidBootsFlightCollision(
      Player player
  ) {
    player.noPhysics = player.isSpectator() || canUniverseVoidFlyThroughWalls(player);
    if (player.noPhysics) {
      player.setOnGround(false);
      player.resetFallDistance();
    }
  }

  public static void handleUniverseVoidBootsFluidContact(
      Player player,
      CallbackInfoReturnable<Boolean> cir
  ) {
    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    if (!boots.is(MagnaItems.UNIVERSE_VOID_BOOTS)) return;
    if (player.isShiftKeyDown()) return;

    Level level = player.level();
    double surfaceY = findFluidSurfaceY(level, player);
    if (surfaceY == Double.MIN_VALUE) return;

    double maxDepth = 0.5;
    if (surfaceY - player.getY() > maxDepth) return;

    cir.setReturnValue(false);
  }

  public static void handleUniverseVoidBootsFluidWalking(
      Player player
  ) {
    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    if (!boots.is(MagnaItems.UNIVERSE_VOID_BOOTS)) return;
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

  public static void handleUniverseVoidChestplateImmunity(
      Player player,
      DamageSource source,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (player.getItemBySlot(EquipmentSlot.CHEST).is(MagnaItems.UNIVERSE_VOID_CHESTPLATE)
        && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING))) {
      cir.setReturnValue(true);
    }
  }

  public static int handleUniverseVoidFlight(
      Player player,
      int flightTicks
  ) {
    if (!player.getAbilities().mayfly) {
      player.getAbilities().mayfly = true;
      player.onUpdateAbilities();
    }

    float baseSpeed = player.isSprinting() ? 0.2F : 0.08F;
    float targetSpeed = baseSpeed;
    boolean allowSpeedUp = player.getItemBySlot(EquipmentSlot.LEGS).is(MagnaItems.UNIVERSE_VOID_LEGGINGS);

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

  public static void handleUniverseVoidLeggingsStepping(
      LivingEntity living
  ) {
    if (!(living instanceof Player player)) return;
    if (player.level().isClientSide()) return;

    ItemStack legStack = player.getItemBySlot(EquipmentSlot.LEGS);
    AttributeInstance stepAttr = player.getAttribute(Attributes.STEP_HEIGHT);
    AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
    Identifier stepId = Identifier.fromNamespaceAndPath(
        Magnatour.MOD_ID, "universe_void_leggings_step_height"
    );
    Identifier jumpId = Identifier.fromNamespaceAndPath(
        Magnatour.MOD_ID, "universe_void_leggings_jump_boost"
    );

    if (legStack.is(MagnaItems.UNIVERSE_VOID_LEGGINGS)) {
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

  public static boolean hasArmor(
      AbstractContainerMenu menu
  ) {
    for (Slot slot : menu.slots) {
      if (slot.container instanceof Inventory inventory) {
        return getArmorStackLimit(inventory.player) > 0;
      }
    }
    return false;
  }

  private static boolean dropExcess(
      Player player,
      ItemStack stack
  ) {
    if (stack.isEmpty()) return false;
    int limit = getStackLimit(player, stack);
    if (stack.getCount() <= limit) return false;
    // Ground stacks retain vanilla limits, including tools with a limit of one.
    int dropLimit = stack.getMaxStackSize();
    while (stack.getCount() > limit) {
      player.drop(stack.split(Math.min(dropLimit, stack.getCount() - limit)), false, Prediction.SERVER_ONLY);
    }
    return true;
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
