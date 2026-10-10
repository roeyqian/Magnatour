/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity.universe;

// Java Standard
import java.util.List;

// Fabric
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;

// Minecraft
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

// Magnatour
import roeyqian.magnatour.level.UniverseAnnihilation;
import roeyqian.magnatour.menu.universe.UniverseAnnihilatorMenu;

public final class UniverseAnnihilator extends PathfinderMob {

  public UniverseAnnihilator(
      EntityType<? extends PathfinderMob> type,
      Level level
  ) {
    super(type, level);
    setPersistenceRequired();
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createMobAttributes().add(Attributes.MAX_HEALTH, 200)
        .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
  }

  @Override
  public InteractionResult mobInteract(
      Player player,
      InteractionHand hand
  ) {
    if (player instanceof ServerPlayer serverPlayer) {
      var task = UniverseAnnihilation.getTask(serverPlayer.level().getServer(), getUUID());
      boolean progressOnly = task != null && task.busy();
      if (!progressOnly && !UniverseAnnihilation.canUse(serverPlayer)) {
        serverPlayer.sendSystemMessage(Component.translatable("gui.magnatour.annihilator.denied"));
        return InteractionResult.CONSUME;
      }
      var data = new UniverseAnnihilatorMenu.OpeningData(getId(), progressOnly ? List.of(task.dimension) :
          serverPlayer.level().getServer().levelKeys().stream()
              .filter(key -> !key.equals(level().dimension()))
              .sorted(java.util.Comparator.comparing(key -> key.identifier().toString())).toList(), progressOnly);
      player.openMenu(new ExtendedMenuProvider<UniverseAnnihilatorMenu.OpeningData>() {
        @Override
        public Component getDisplayName() { return getName(); }
        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
          return new UniverseAnnihilatorMenu(id, data, UniverseAnnihilator.this);
        }
        @Override
        public UniverseAnnihilatorMenu.OpeningData getScreenOpeningData(ServerPlayer player) {
          return data;
        }
      });
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected void registerGoals() {
    goalSelector.addGoal(0, new FloatGoal(this));
    goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8));
  }

}
