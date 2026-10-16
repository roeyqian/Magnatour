/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;

// Magnatour
import roeyqian.magnatour.entity.supreme.NetheriteGolem;
import roeyqian.magnatour.entity.supreme.ObsidianGolem;
import roeyqian.magnatour.registry.world.MagnaStructures;

/** Makes golems inside a Diamond City hostile while preserving player-created golem protection. */
public final class DiamondCityGolemTargetGoal extends NearestAttackableTargetGoal<Player> {

  private final AbstractGolem golem;

  public DiamondCityGolemTargetGoal(
      AbstractGolem golem
  ) {
    super(golem, Player.class, 10, true, false, (player, _) ->
        player.isAlive() && !player.isSpectator() && !((Player) player).isCreative());
    this.golem = golem;
  }

  public static boolean isHostileToPlayer(
      AbstractGolem golem,
      LivingEntity target
  ) {
    return target instanceof Player player && player.isAlive()
        && !player.isCreative() && !player.isSpectator()
        && !isPlayerCreated(golem) && isInsideDiamondCity(golem);
  }

  public static boolean isInsideDiamondCity(
      AbstractGolem golem
  ) {
    return golem.level() instanceof ServerLevel level
        && level.structureManager().getStructureWithPieceAt(
            golem.blockPosition(),
            holder -> holder.value().type() == MagnaStructures.DIAMOND_CITY
        ).isValid();
  }

  @Override
  public boolean canContinueToUse() {
    return !isPlayerCreated(this.golem) && isInsideDiamondCity(this.golem) && super.canContinueToUse();
  }

  @Override
  public boolean canUse() {
    return !isPlayerCreated(this.golem) && isInsideDiamondCity(this.golem) && super.canUse();
  }

  private static boolean isPlayerCreated(
      AbstractGolem golem
  ) {
    return golem instanceof IronGolem ironGolem && ironGolem.isPlayerCreated()
        || golem instanceof ObsidianGolem obsidianGolem && obsidianGolem.isPlayerCreated()
        || golem instanceof NetheriteGolem netheriteGolem && !netheriteGolem.isNotPlayerCreated();
  }

}
