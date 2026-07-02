/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.item;

// Java Standard
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiFunction;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

public class SupremeMobileMenuAccess implements ContainerLevelAccess {

  private final BlockPos pos;

  private final Level world;

  public SupremeMobileMenuAccess(
      Player player
  ) {
    this.world = player.level();
    this.pos = player.blockPosition();
  }

  public static void injectContext(
      AbstractContainerMenu handler,
      ContainerLevelAccess newContext
  ) {
    if (handler == null) return;

    Class<?> clazz = handler.getClass();
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (ContainerLevelAccess.class.isAssignableFrom(field.getType())) {
          try {
            field.setAccessible(true);
            field.set(handler, newContext);
            return;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  @Override @NonNull
  public <T> Optional<T> evaluate(
      BiFunction<Level, BlockPos, T> getter
  ) {
    T result = getter.apply(world, pos);
    if (Boolean.FALSE.equals(result)) return Optional.empty();
    return Optional.ofNullable(result);
  }

}
