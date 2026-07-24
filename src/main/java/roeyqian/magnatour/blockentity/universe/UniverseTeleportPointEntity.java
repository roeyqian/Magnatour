/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.blockentity.universe;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.universe.UniverseTeleportPointMenu;
import roeyqian.magnatour.registry.content.UniverseBlockEntities;

public class UniverseTeleportPointEntity extends BlockEntity implements MenuProvider {

  public static final int MAX_DESTINATIONS = 64;

  private static final String DESTINATIONS_KEY = "Destinations";

  private final List<Destination> destinations = new ArrayList<>();

  public UniverseTeleportPointEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(UniverseBlockEntities.UNIVERSE_TELEPORT_POINT_ENTITY, pos, state);
  }

  public boolean addDestination(
      Destination destination
  ) {
    if (destinations.size() >= MAX_DESTINATIONS) return false;

    destinations.removeIf(existing -> existing.name().equals(destination.name()));
    destinations.add(destination);
    syncChanged();
    return true;
  }

  @Override
  public AbstractContainerMenu createMenu(
      int syncId,
      @NonNull Inventory playerInventory,
      @NonNull Player player
  ) {
    return new UniverseTeleportPointMenu(
        syncId,
        getDestinations(),
        getBlockPos(),
        this.level != null ? this.level.dimension() : Level.OVERWORLD
    );
  }

  public boolean deleteDestination(
      int index
  ) {
    if (index < 0 || index >= destinations.size()) return false;

    destinations.remove(index);
    syncChanged();
    return true;
  }

  public List<Destination> getDestinations() {
    return List.copyOf(destinations);
  }

  public @NonNull Component getDisplayName() {
    return Component.translatable("block.magnatour.universe_teleport_point");
  }

  public boolean teleport(
      ServerPlayer player,
      int index
  ) {
    if (index < 0 || index >= destinations.size()) return false;

    Destination destination = destinations.get(index);
    if (player.level().getServer() == null) return false;
    ServerLevel targetLevel = player.level().getServer().getLevel(destination.dimension());
    if (targetLevel == null) return false;

    player.teleportTo(
        targetLevel,
        destination.x() + 0.5,
        destination.y(),
        destination.z() + 0.5,
        Relative.union(Relative.ROTATION, Relative.DELTA),
        player.getYRot(),
        player.getXRot(),
        false
    );
    return true;
  }

  @Override
  protected void loadAdditional(
      @NonNull ValueInput input
  ) {
    super.loadAdditional(input);
    destinations.clear();
    for (Destination destination : input.listOrEmpty(DESTINATIONS_KEY, Destination.CODEC)) {
      destinations.add(destination);
    }
  }

  @Override
  protected void saveAdditional(
      @NonNull ValueOutput output
  ) {
    super.saveAdditional(output);
    output.store(DESTINATIONS_KEY, Destination.CODEC.listOf(), destinations);
  }

  private void syncChanged() {
    this.setChanged();
    if (this.level != null && !this.level.isClientSide()) {
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }
  }

  public record Destination(
      String name,
      ResourceKey<Level> dimension,
      int x,
      int y,
      int z
  ) {

    public static final Codec<Destination> CODEC = RecordCodecBuilder.create((instance) -> instance
        .group(
            Codec.STRING.fieldOf("name").forGetter(Destination::name),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Destination::dimension),
            Codec.INT.fieldOf("x").forGetter(Destination::x),
            Codec.INT.fieldOf("y").forGetter(Destination::y),
            Codec.INT.fieldOf("z").forGetter(Destination::z)
        )
        .apply(instance, Destination::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Destination> PACKET_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            Destination::name,
            ResourceKey.streamCodec(Registries.DIMENSION),
            Destination::dimension,
            ByteBufCodecs.VAR_INT,
            Destination::x,
            ByteBufCodecs.VAR_INT,
            Destination::y,
            ByteBufCodecs.VAR_INT,
            Destination::z,
            Destination::new
        );

  }

}
