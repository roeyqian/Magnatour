/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.registry.logic;

// Java Standard
import java.util.function.UnaryOperator;

// Mojang
import com.mojang.serialization.Codec;

// Minecraft
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.registry.LogicRegHelper;

/*
 * Supreme Group: Item, Render
 * Universe Group: Item, Render
 */
public final class CustomComponents {

  public static final DataComponentType<String> SUPREME_MOBILE_BLOCK_ID = register(
      "supreme_mobile_block_id",
      builder -> builder
          .persistent(Codec.STRING)
          .networkSynchronized(ByteBufCodecs.STRING_UTF8)
  );

  // Supreme Group: Render
  public static final DataComponentType<Boolean> SUPREME_GLINT_OVERRIDE =
      register(
          "supreme_glint_override",
          builder -> builder
              .persistent(Codec.BOOL)
              .networkSynchronized(ByteBufCodecs.BOOL)
      );
  // Universe Group: Render
  public static final DataComponentType<Boolean> UNIVERSE_GLINT_OVERRIDE =
      register(
          "universe_glint_override",
          builder -> builder
              .persistent(Codec.BOOL)
              .networkSynchronized(ByteBufCodecs.BOOL)
      );

  // Supreme Group: Item
  public static final DataComponentType<Integer> SUPREME_MOBILE_MODE = register(
      "supreme_mobile_mode",
      builder -> builder
          .persistent(Codec.INT)
          .networkSynchronized(ByteBufCodecs.VAR_INT)
  );
  public static final DataComponentType<Integer> UNIVERSE_BUCKET_MODE = register(
      "universe_bucket_mode",
      builder -> builder
          .persistent(Codec.INT)
          .networkSynchronized(ByteBufCodecs.VAR_INT)
  );
  public static final DataComponentType<Integer> UNIVERSE_CONSOLE_MODE = register(
      "universe_console_mode",
      builder -> builder
          .persistent(Codec.INT)
          .networkSynchronized(ByteBufCodecs.VAR_INT)
  );
  public static final DataComponentType<Integer> UNIVERSE_OMNI_BLADE_MODE = register(
      "universe_omni_blade_mode",
      builder -> builder
          .persistent(Codec.INT)
          .networkSynchronized(ByteBufCodecs.VAR_INT)
  );
  // Universe Group: Item
  public static final DataComponentType<Integer> UNIVERSE_ULTIMA_SWORD_MODE = register(
      "universe_ultima_sword_mode",
      builder -> builder
          .persistent(Codec.INT)
          .networkSynchronized(ByteBufCodecs.VAR_INT)
  );

  public static final DataComponentType<UniverseConsole.BoundBlockList> UNIVERSE_CONSOLE_BOUND_LIST = register(
      "universe_console_bound_list",
      builder -> builder
          .persistent(UniverseConsole.BoundBlockList.CODEC)
          .networkSynchronized(UniverseConsole.BoundBlockList.PACKET_CODEC)
  );

  private CustomComponents() {}

  public static void init() {
    Magnatour.LOGGER.info("[Server] Initializing 'CustomComponents'");
  }

  private static <T> DataComponentType<T> register(
      String id,
      UnaryOperator<DataComponentType.Builder<T>> builderOperator
  ) {
    return LogicRegHelper.registerDataComponentType(id, builderOperator);
  }

}
