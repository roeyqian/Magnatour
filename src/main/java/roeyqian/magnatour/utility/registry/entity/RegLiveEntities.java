/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.entity;

// Minecraft
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.dead.PrimedChunkTnt;
import roeyqian.magnatour.entity.live.BellRinger;
import roeyqian.magnatour.entity.live.BellSoul;
import roeyqian.magnatour.entity.live.NetheriteGolem;
import roeyqian.magnatour.entity.live.ObsidianGolem;
import roeyqian.magnatour.entity.live.PaleLord;
import roeyqian.magnatour.entity.live.PaleLordClone;
import roeyqian.magnatour.entity.live.SculkBehemoth;
import roeyqian.magnatour.entity.live.TheUnnameableThing;
import roeyqian.magnatour.entity.live.UniverseGuardian;

/*
 * Supreme Group: Creature, Monster
 * Universe Group: Creature, Monster
 */
public final class RegLiveEntities {

  // Supreme Group: Creature
  public static final ResourceKey<EntityType<?>> THE_UNNAMEABLE_THING_KEY =
      EntityRegHelper.entityKey("the_unnameable_thing");
  public static final EntityType<TheUnnameableThing> THE_UNNAMEABLE_THING = EntityRegHelper.register(
      THE_UNNAMEABLE_THING_KEY, TheUnnameableThing::new, MobCategory.MONSTER,
      1.2F, 1.2F
  );
  public static final ResourceKey<EntityType<?>> OBSIDIAN_GOLEM_KEY =
      EntityRegHelper.entityKey("obsidian_golem");
  public static final EntityType<ObsidianGolem> OBSIDIAN_GOLEM = EntityRegHelper.register(
      OBSIDIAN_GOLEM_KEY, ObsidianGolem::new, MobCategory.CREATURE,
      1.4F, 2.7F
  );
  public static final ResourceKey<EntityType<?>> NETHERITE_GOLEM_KEY =
      EntityRegHelper.entityKey("netherite_golem");
  public static final EntityType<NetheriteGolem> NETHERITE_GOLEM = EntityRegHelper.register(
      NETHERITE_GOLEM_KEY, NetheriteGolem::new, MobCategory.CREATURE,
      1.4F, 2.7F
  );

  // Supreme Group: Monster
  public static final ResourceKey<EntityType<?>> BELL_SOUL_KEY =
      EntityRegHelper.entityKey("bell_soul");
  public static final EntityType<BellSoul> BELL_SOUL = EntityRegHelper.register(
      BELL_SOUL_KEY, BellSoul::new, MobCategory.MONSTER,
      0.4F, 0.8F
  );
  public static final ResourceKey<EntityType<?>> BELL_RINGER_KEY =
      EntityRegHelper.entityKey("bell_ringer");
  public static final EntityType<BellRinger> BELL_RINGER = EntityRegHelper.register(
      BELL_RINGER_KEY, BellRinger::new, MobCategory.MONSTER,
      0.6F, 1.95F
  );
  public static final ResourceKey<EntityType<?>> SCULK_BEHEMOTH_KEY =
      EntityRegHelper.entityKey("sculk_behemoth");
  public static final EntityType<SculkBehemoth> SCULK_BEHEMOTH = EntityRegHelper.register(
      SCULK_BEHEMOTH_KEY, SculkBehemoth::new, MobCategory.MONSTER,
      8.0F, 10.0F
  );
  public static final ResourceKey<EntityType<?>> PALE_LORD_KEY =
      EntityRegHelper.entityKey("pale_lord");
  public static final EntityType<PaleLord> PALE_LORD = EntityRegHelper.register(
      PALE_LORD_KEY, PaleLord::new, MobCategory.MONSTER,
      0.6F, 1.95F
  );
  public static final ResourceKey<EntityType<?>> PALE_LORD_CLONE_KEY =
      EntityRegHelper.entityKey("pale_lord_clone");
  public static final EntityType<PaleLordClone> PALE_LORD_CLONE = EntityRegHelper.register(
      PALE_LORD_CLONE_KEY, PaleLordClone::new, MobCategory.MONSTER,
      0.6F, 1.95F
  );

  // Supreme Group: Creature
  public static final ResourceKey<EntityType<?>> UNIVERSE_GUARDIAN_KEY =
      EntityRegHelper.entityKey("universe_guardian");
  public static final EntityType<UniverseGuardian> UNIVERSE_GUARDIAN = EntityRegHelper.register(
      UNIVERSE_GUARDIAN_KEY, UniverseGuardian::new, MobCategory.CREATURE,
      0.6F, 1.4F
  );

  // Dead Entity
  public static final ResourceKey<EntityType<?>> PRIMED_CHUNK_TNT_KEY =
      EntityRegHelper.entityKey("primed_chunk_tnt");
  public static final EntityType<PrimedChunkTnt> PRIMED_CHUNK_TNT = EntityRegHelper.register(
      PRIMED_CHUNK_TNT_KEY, PrimedChunkTnt::new, MobCategory.MISC,
      0.98F, 0.98F
  );

  private RegLiveEntities() {}

  public static void init() {
    EntityRegHelper.registerAttributes(THE_UNNAMEABLE_THING, TheUnnameableThing.createAttributes());
    EntityRegHelper.registerAttributes(OBSIDIAN_GOLEM, ObsidianGolem.createAttributes());
    EntityRegHelper.registerAttributes(NETHERITE_GOLEM, NetheriteGolem.createAttributes());
    EntityRegHelper.registerAttributes(BELL_SOUL, BellSoul.createAttributes());
    EntityRegHelper.registerAttributes(BELL_RINGER, BellRinger.createAttributes());
    EntityRegHelper.registerAttributes(SCULK_BEHEMOTH, SculkBehemoth.createAttributes());
    EntityRegHelper.registerAttributes(PALE_LORD, PaleLord.createAttributes());
    EntityRegHelper.registerAttributes(PALE_LORD_CLONE, PaleLordClone.createAttributes());
    EntityRegHelper.registerAttributes(UNIVERSE_GUARDIAN, UniverseGuardian.createAttributes());

    Magnatour.LOGGER.info("[Server] Initializing 'RegLiveEntities'");
  }

}
