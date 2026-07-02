/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.menu;

// Fabric
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;

// Minecraft
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

// Magnatour
import roeyqian.magnatour.Magnatour;

public interface MenuRegHelper {

  static <T extends AbstractContainerMenu> MenuType<T> register(
      String path,
      MenuType.MenuSupplier<T> factory
  ) {
    return Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path),
        new MenuType<>(factory, FeatureFlags.VANILLA_SET)
    );
  }

  static <T extends AbstractContainerMenu, D> MenuType<T> registerExtended(
      String path,
      ExtendedMenuType.ExtendedFactory<T, D> factory,
      StreamCodec<? super RegistryFriendlyByteBuf, D> codec
  ) {
    return Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, path),
        new ExtendedMenuType<>(factory, codec)
    );
  }

}
