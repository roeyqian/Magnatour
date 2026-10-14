/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.universe;

// Java Standard
import java.util.List;

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
import roeyqian.magnatour.blockentity.universe.UniverseDotEntity;
import roeyqian.magnatour.registry.content.UniverseMenus;

public class UniverseDotMenu extends AbstractContainerMenu {

  private final BlockPos blockPos;

  private final ResourceKey<Level> dimension;

  private final List<UniverseDotEntity.Destination> destinations;

  public UniverseDotMenu(
      int syncId,
      OpeningData openingData
  ) {
    this(syncId, openingData.destinations(), openingData.blockPos(), openingData.dimension());
  }

  public UniverseDotMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, List.of(), BlockPos.ZERO, Level.OVERWORLD);
  }

  public UniverseDotMenu(
      int syncId,
      List<UniverseDotEntity.Destination> destinations,
      BlockPos blockPos,
      ResourceKey<Level> dimension
  ) {
    super(UniverseMenus.UNIVERSE_DOT_HANDLER, syncId);
    this.destinations = List.copyOf(destinations);
    this.blockPos = blockPos;
    this.dimension = dimension;
  }

  public BlockPos getBlockPos() {
    return blockPos;
  }

  public List<UniverseDotEntity.Destination> getDestinations() {
    return destinations;
  }

  public ResourceKey<Level> getDimension() {
    return dimension;
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
      List<UniverseDotEntity.Destination> destinations
  ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> PACKET_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpeningData::blockPos,
            ResourceKey.streamCodec(Registries.DIMENSION),
            OpeningData::dimension,
            UniverseDotEntity.Destination.PACKET_CODEC.apply(ByteBufCodecs.list()),
            OpeningData::destinations,
            OpeningData::new
        );

  }

}
