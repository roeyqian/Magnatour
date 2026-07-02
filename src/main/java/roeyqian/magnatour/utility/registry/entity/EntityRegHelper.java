/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.entity;

// Java Standard
import java.util.Objects;

// Fabric
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface EntityRegHelper {

  static <T extends Entity> EntityType<T> register(
      ResourceKey<EntityType<?>> key,
      EntityType.EntityFactory<T> factory,
      MobCategory category,
      float width,
      float height
  ) {
    return Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        key,
        EntityType.Builder.of(factory, category).sized(width, height).build(key)
    );
  }

  static Identifier id(
      String path
  ) {
    return Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path);
  }

  static ResourceKey<EntityType<?>> entityKey(
      String path
  ) {
    return ResourceKey.create(Registries.ENTITY_TYPE, id(path));
  }

  @SuppressWarnings("DataFlowIssue")
  static <T extends LivingEntity> void registerAttributes(
      EntityType<T> type,
      AttributeSupplier.Builder attributes
  ) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(attributes, "attributes");
    FabricDefaultAttributeRegistry.register(type, attributes);
  }

}
