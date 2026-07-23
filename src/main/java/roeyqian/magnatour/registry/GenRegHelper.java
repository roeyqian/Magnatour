/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry;

// Java Standard
import java.util.function.UnaryOperator;

// Fabric
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface GenRegHelper {

  static Identifier id(
      String path
  ) {
    return Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path);
  }

  static <T> DataComponentType<T> registerDataComponentType(
      String path,
      UnaryOperator<DataComponentType.Builder<T>> builderOperator
  ) {
    return Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id(path),
        builderOperator.apply(DataComponentType.builder()).build()
    );
  }

  static RecipeBookCategory registerRecipeBookCategory(
      String path
  ) {
    return Registry.register(
        BuiltInRegistries.RECIPE_BOOK_CATEGORY,
        id(path),
        new RecipeBookCategory() {
        }
    );
  }

  static <T extends Recipe<?>> RecipeSerializer<T> registerRecipeSerializer(
      String path,
      RecipeSerializer<T> serializer
  ) {
    return Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        id(path),
        serializer
    );
  }

  static <T extends Recipe<?>> RecipeType<T> registerRecipeType(
      String path
  ) {
    Identifier id = id(path);
    return Registry.register(
        BuiltInRegistries.RECIPE_TYPE,
        id,
        new RecipeType<T>() {
          @Override
          public String toString() {
            return id.toString();
          }
        }
    );
  }

  static SimpleParticleType registerSimpleParticle(
      String path
  ) {
    return Registry.register(
        BuiltInRegistries.PARTICLE_TYPE,
        id(path),
        FabricParticleTypes.simple()
    );
  }

}
