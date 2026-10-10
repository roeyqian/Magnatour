/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.content;

// Fabric
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

// Minecraft
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.universe.UniverseAnnihilator;
import roeyqian.magnatour.entity.universe.UniverseGuardian;
import roeyqian.magnatour.registry.EntityRegHelper;

/*
 * Universe Group: Creature
 */
public final class UniverseLiveEntities {

  public static final EntityType<UniverseAnnihilator> UNIVERSE_ANNIHILATOR = EntityRegHelper.register(
      EntityRegHelper.entityKey("universe_annihilator"), UniverseAnnihilator::new,
      MobCategory.CREATURE, 0.6F, 1.8F);

  // Creature
  public static final ResourceKey<EntityType<?>> UNIVERSE_GUARDIAN_KEY =
      EntityRegHelper.entityKey("universe_guardian");

  public static final EntityType<UniverseGuardian> UNIVERSE_GUARDIAN = EntityRegHelper.register(
      UNIVERSE_GUARDIAN_KEY, UniverseGuardian::new, MobCategory.CREATURE,
      0.6F, 1.4F
  );

  private UniverseLiveEntities() {}

  public static void init() {
    EntityRegHelper.registerAttributes(UNIVERSE_ANNIHILATOR, UniverseAnnihilator.createAttributes());
    EntityRegHelper.registerAttributes(UNIVERSE_GUARDIAN, UniverseGuardian.createAttributes());
    ServerLivingEntityEvents.AFTER_DAMAGE.register(UniverseGuardian::recordOwnerDamage);

    Magnatour.LOGGER.info("[Server] Initializing 'UniverseLiveEntities'");
  }

}
