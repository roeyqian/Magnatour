/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.active;

// Mojang
import com.mojang.serialization.MapCodec;

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

// Magnatour
import roeyqian.magnatour.block.active.entity.UniverseTeleportPointEntity;
import roeyqian.magnatour.menu.block.UniverseTeleportPointMenu;

public class UniverseTeleportPoint extends BaseEntityBlock {

  public static final MapCodec<UniverseTeleportPoint> CODEC = simpleCodec(UniverseTeleportPoint::new);

  public UniverseTeleportPoint(
      Properties settings
  ) {
    super(settings);
  }

  @Override
  public BlockEntity newBlockEntity(
      @NonNull BlockPos pos,
      @NonNull BlockState state
  ) {
    return new UniverseTeleportPointEntity(pos, state);
  }

  @Override @NonNull
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override @NonNull
  protected InteractionResult useWithoutItem(
      @NonNull BlockState state,
      Level world,
      @NonNull BlockPos pos,
      @NonNull Player player,
      @NonNull BlockHitResult hit
  ) {
    if (world.isClientSide()) return InteractionResult.SUCCESS;

    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof UniverseTeleportPointEntity teleportPoint) {
      player.openMenu(new ExtendedMenuProvider<UniverseTeleportPointMenu.OpeningData>() {

        @Override @NonNull
        public Component getDisplayName() {
          return teleportPoint.getDisplayName();
        }

        @Override
        public AbstractContainerMenu createMenu(
            int syncId,
            @NonNull Inventory inv,
            @NonNull Player player
        ) {
          return new UniverseTeleportPointMenu(
              syncId,
              teleportPoint.getDestinations(),
              pos,
              world.dimension()
          );
        }

        @Override
        public UniverseTeleportPointMenu.OpeningData getScreenOpeningData(
            @NonNull ServerPlayer player
        ) {
          return new UniverseTeleportPointMenu.OpeningData(
              pos,
              world.dimension(),
              teleportPoint.getDestinations()
          );
        }
      });
    }

    return InteractionResult.CONSUME;
  }

}
