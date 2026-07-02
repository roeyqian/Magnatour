/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.entity;

// Java Standard
import java.util.Objects;
import java.util.Optional;

// Minecraft
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootTable;

// Magnatour
import roeyqian.magnatour.Magnatour;

public final class EntityLootTableHelper {

  private static final String ENTITY_LOOT_TABLE_PREFIX = "entity/";

  private EntityLootTableHelper() {}

  public static void dropMagnatourEntityLoot(
      LivingEntity entity,
      ServerLevel world,
      DamageSource source,
      boolean causedByPlayer
  ) {
    entity.dropFromLootTable(
        world,
        source,
        causedByPlayer,
        entityLootTable(entity.getType())
    );
  }

  public static ResourceKey<LootTable> entityLootTable(
      String path
  ) {
    return ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(
            Magnatour.MOD_ID,
            ENTITY_LOOT_TABLE_PREFIX + path
        )
    );
  }

  public static ResourceKey<LootTable> entityLootTable(
      EntityType<?> entityType
  ) {
    Identifier entityId = Objects.requireNonNull(
        BuiltInRegistries.ENTITY_TYPE.getKey(entityType),
        "entityType"
    );
    return entityLootTable(entityId.getPath());
  }

  public static Optional<ResourceKey<LootTable>> resolveLootTable(
      LivingEntity entity
  ) {
    Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    if (entityId == null || !Magnatour.MOD_ID.equals(entityId.getNamespace())) {
      return Optional.empty();
    }

    return Optional.of(entityLootTable(entityId.getPath()));
  }

}
