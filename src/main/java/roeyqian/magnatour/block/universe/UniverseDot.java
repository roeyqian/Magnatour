/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.universe;

// Fabric
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseDotEntity;
import roeyqian.magnatour.menu.universe.UniverseDotMenu;

public class UniverseDot extends BaseEntityBlock {

  public UniverseDot(
      Properties settings
  ) {
    super(settings);
  }

  @Nullable @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseDotEntity(pos, state);
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (world.isClientSide()) return InteractionResult.SUCCESS;

    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof UniverseDotEntity dot) {
      player.openMenu(new ExtendedMenuProvider<UniverseDotMenu.OpeningData>() {

        @Override @NonNull
        public Component getDisplayName() {
          return dot.getDisplayName();
        }

        @Nullable @Override
        public AbstractContainerMenu createMenu(
            int syncId,
            @NonNull Inventory inv,
            @NonNull Player player
        ) {
          return new UniverseDotMenu(
              syncId,
              dot.getDestinations(),
              pos,
              world.dimension()
          );
        }

        @Override
        public UniverseDotMenu.@NonNull OpeningData getScreenOpeningData(
            @NonNull ServerPlayer player
        ) {
          return new UniverseDotMenu.OpeningData(
              pos,
              world.dimension(),
              dot.getDestinations()
          );
        }
      });
    }

    return InteractionResult.CONSUME;
  }

}
