/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.block;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.active.entity.RedstoneTriggerEntity;
import roeyqian.magnatour.utility.registry.menu.RegBlockMenus;

public class RedstoneTriggerMenu extends AbstractContainerMenu {

  private static final StreamCodec<RegistryFriendlyByteBuf, Boolean> BOOL_CODEC =
      StreamCodec.ofMember(
          (value, buf) -> buf.writeBoolean(value),
          RegistryFriendlyByteBuf::readBoolean
      );

  private final int intervalTicks;

  private final boolean enabled;

  private final BlockPos blockPos;

  private final ResourceKey<Level> dimension;

  private final RedstoneTriggerEntity.TriggerMode mode;

  public RedstoneTriggerMenu(
      int syncId,
      OpeningData openingData
  ) {
    this(
        syncId,
        openingData.blockPos(),
        openingData.dimension(),
        openingData.mode(),
        openingData.enabled(),
        openingData.intervalTicks()
    );
  }

  public RedstoneTriggerMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(
        syncId,
        BlockPos.ZERO,
        Level.OVERWORLD,
        RedstoneTriggerEntity.TriggerMode.NORMAL,
        false,
        RedstoneTriggerEntity.DEFAULT_INTERVAL_TICKS
    );
  }

  public RedstoneTriggerMenu(
      int syncId,
      BlockPos blockPos,
      ResourceKey<Level> dimension,
      RedstoneTriggerEntity.TriggerMode mode,
      boolean enabled,
      int intervalTicks
  ) {
    super(RegBlockMenus.REDSTONE_TRIGGER_HANDLER, syncId);
    this.blockPos = blockPos;
    this.dimension = dimension;
    this.mode = mode;
    this.enabled = enabled;
    this.intervalTicks = Math.max(1, intervalTicks);
  }

  public BlockPos getBlockPos() {
    return blockPos;
  }

  public ResourceKey<Level> getDimension() {
    return dimension;
  }

  public int getIntervalTicks() {
    return intervalTicks;
  }

  public RedstoneTriggerEntity.TriggerMode getMode() {
    return mode;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override @NonNull
  public ItemStack quickMoveStack(
      @NonNull Player player,
      int index
  ) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return true;
  }

  public record OpeningData(
      BlockPos blockPos,
      ResourceKey<Level> dimension,
      RedstoneTriggerEntity.TriggerMode mode,
      boolean enabled,
      int intervalTicks
  ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> PACKET_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpeningData::blockPos,
            ResourceKey.streamCodec(Registries.DIMENSION),
            OpeningData::dimension,
            RedstoneTriggerEntity.TriggerMode.PACKET_CODEC,
            OpeningData::mode,
            BOOL_CODEC,
            OpeningData::enabled,
            ByteBufCodecs.VAR_INT,
            OpeningData::intervalTicks,
            OpeningData::new
        );

  }

}
