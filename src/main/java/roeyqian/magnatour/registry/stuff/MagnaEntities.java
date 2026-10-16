/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.stuff;

// Fabric
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

// Minecraft
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.supreme.BellRinger;
import roeyqian.magnatour.entity.supreme.BellSoul;
import roeyqian.magnatour.entity.supreme.NetheriteGolem;
import roeyqian.magnatour.entity.supreme.ObsidianGolem;
import roeyqian.magnatour.entity.supreme.PaleLord;
import roeyqian.magnatour.entity.supreme.PaleLordClone;
import roeyqian.magnatour.entity.supreme.PrimedChunkTnt;
import roeyqian.magnatour.entity.supreme.SculkBehemoth;
import roeyqian.magnatour.entity.supreme.TheUnnameableThing;
import roeyqian.magnatour.entity.universe.UniverseAnnihilator;
import roeyqian.magnatour.entity.universe.UniverseGuardian;
import roeyqian.magnatour.registry.StuffHelper;

/*
 * Supreme and Universe Groups: Creature, Monster, Misc
 */
public final class MagnaEntities {

  // Supreme Group
  // Monster
  public static final ResourceKey<EntityType<?>> BELL_RINGER_KEY =
      StuffHelper.entityKey("bell_ringer");
  public static final ResourceKey<EntityType<?>> BELL_SOUL_KEY =
      StuffHelper.entityKey("bell_soul");
  public static final ResourceKey<EntityType<?>> PALE_LORD_KEY =
      StuffHelper.entityKey("pale_lord");
  public static final ResourceKey<EntityType<?>> PALE_LORD_CLONE_KEY =
      StuffHelper.entityKey("pale_lord_clone");
  public static final ResourceKey<EntityType<?>> SCULK_BEHEMOTH_KEY =
      StuffHelper.entityKey("sculk_behemoth");
  public static final ResourceKey<EntityType<?>> THE_UNNAMEABLE_THING_KEY =
      StuffHelper.entityKey("the_unnameable_thing");

  // Creature
  public static final ResourceKey<EntityType<?>> NETHERITE_GOLEM_KEY =
      StuffHelper.entityKey("netherite_golem");
  public static final ResourceKey<EntityType<?>> OBSIDIAN_GOLEM_KEY =
      StuffHelper.entityKey("obsidian_golem");

  // Misc
  public static final ResourceKey<EntityType<?>> PRIMED_CHUNK_TNT_KEY =
      StuffHelper.entityKey("primed_chunk_tnt");

  public static final EntityType<BellRinger> BELL_RINGER = StuffHelper.register(
      BELL_RINGER_KEY, BellRinger::new, MobCategory.MONSTER,
      0.6F, 1.95F
  );

  public static final EntityType<BellSoul> BELL_SOUL = StuffHelper.register(
      BELL_SOUL_KEY, BellSoul::new, MobCategory.MONSTER,
      0.4F, 0.8F
  );

  public static final EntityType<PaleLord> PALE_LORD = StuffHelper.register(
      PALE_LORD_KEY, PaleLord::new, MobCategory.MONSTER,
      0.6F, 1.95F
  );

  public static final EntityType<PaleLordClone> PALE_LORD_CLONE = StuffHelper.register(
      PALE_LORD_CLONE_KEY, PaleLordClone::new, MobCategory.MONSTER,
      0.6F, 1.95F
  );

  public static final EntityType<SculkBehemoth> SCULK_BEHEMOTH = StuffHelper.register(
      SCULK_BEHEMOTH_KEY, SculkBehemoth::new, MobCategory.MONSTER,
      8.0F, 10.0F
  );

  public static final EntityType<TheUnnameableThing> THE_UNNAMEABLE_THING = StuffHelper.register(
      THE_UNNAMEABLE_THING_KEY, TheUnnameableThing::new, MobCategory.MONSTER,
      1.2F, 1.2F
  );

  public static final EntityType<NetheriteGolem> NETHERITE_GOLEM = StuffHelper.register(
      NETHERITE_GOLEM_KEY, NetheriteGolem::new, MobCategory.CREATURE,
      1.4F * NetheriteGolem.SIZE_SCALE, 2.7F * NetheriteGolem.SIZE_SCALE
  );

  public static final EntityType<ObsidianGolem> OBSIDIAN_GOLEM = StuffHelper.register(
      OBSIDIAN_GOLEM_KEY, ObsidianGolem::new, MobCategory.CREATURE,
      1.4F * ObsidianGolem.SIZE_SCALE, 2.7F * ObsidianGolem.SIZE_SCALE
  );

  public static final EntityType<PrimedChunkTnt> PRIMED_CHUNK_TNT = StuffHelper.register(
      PRIMED_CHUNK_TNT_KEY, PrimedChunkTnt::new, MobCategory.MISC,
      0.98F, 0.98F
  );

  // Universe Group
  public static final EntityType<UniverseAnnihilator> UNIVERSE_ANNIHILATOR = StuffHelper.register(
      StuffHelper.entityKey("universe_annihilator"), UniverseAnnihilator::new,
      MobCategory.CREATURE, 0.6F, 1.8F);

  // Creature
  public static final ResourceKey<EntityType<?>> UNIVERSE_GUARDIAN_KEY =
      StuffHelper.entityKey("universe_guardian");

  public static final EntityType<UniverseGuardian> UNIVERSE_GUARDIAN = StuffHelper.register(
      UNIVERSE_GUARDIAN_KEY, UniverseGuardian::new, MobCategory.CREATURE,
      0.6F, 1.4F
  );

  private MagnaEntities() {}

  public static void init() {
    StuffHelper.registerAttributes(BELL_RINGER, BellRinger.createAttributes());
    StuffHelper.registerAttributes(BELL_SOUL, BellSoul.createAttributes());
    StuffHelper.registerAttributes(PALE_LORD, PaleLord.createAttributes());
    StuffHelper.registerAttributes(PALE_LORD_CLONE, PaleLordClone.createAttributes());
    StuffHelper.registerAttributes(SCULK_BEHEMOTH, SculkBehemoth.createAttributes());
    StuffHelper.registerAttributes(THE_UNNAMEABLE_THING, TheUnnameableThing.createAttributes());
    StuffHelper.registerAttributes(NETHERITE_GOLEM, NetheriteGolem.createAttributes());
    StuffHelper.registerAttributes(OBSIDIAN_GOLEM, ObsidianGolem.createAttributes());

    StuffHelper.registerAttributes(UNIVERSE_ANNIHILATOR, UniverseAnnihilator.createAttributes());
    StuffHelper.registerAttributes(UNIVERSE_GUARDIAN, UniverseGuardian.createAttributes());
    ServerLivingEntityEvents.AFTER_DAMAGE.register(UniverseGuardian::recordOwnerDamage);
    Magnatour.LOGGER.info("[Server] Initializing 'MagnaEntities'");
  }

}
