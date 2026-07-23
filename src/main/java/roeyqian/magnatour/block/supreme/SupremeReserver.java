/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.supreme;

// Mojang
import com.mojang.serialization.MapCodec;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.supreme.SupremeReserverMenu;

public class SupremeReserver extends Block {

  public static final MapCodec<SupremeReserver> CODEC =
      simpleCodec(SupremeReserver::new);

  private static final Component TITLE =
      Component.translatable("block.magnatour.supreme_reserver");

  public SupremeReserver(
      Properties settings
  ) {
    super(settings);
  }

  @Override @NonNull
  protected MapCodec<? extends Block> codec() {
    return CODEC;
  }

  @Override
  protected MenuProvider getMenuProvider(
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos
  ) {
    return new SimpleMenuProvider(
        (syncId, playerInventory, _) -> new SupremeReserverMenu(
            syncId, playerInventory,
            ContainerLevelAccess.create(world, pos)
        ),
        TITLE
    );
  }

  @Override
  protected @NonNull InteractionResult useWithoutItem(
      @NonNull BlockState state,
      Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (!world.isClientSide()) player.openMenu(state.getMenuProvider(world, pos));
    return InteractionResult.SUCCESS;
  }

}
