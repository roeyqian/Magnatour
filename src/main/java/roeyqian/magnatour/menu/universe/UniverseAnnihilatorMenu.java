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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Magnatour
import roeyqian.magnatour.entity.universe.UniverseAnnihilator;
import roeyqian.magnatour.level.UniverseAnnihilation;
import roeyqian.magnatour.registry.content.UniverseMenus;

public final class UniverseAnnihilatorMenu extends AbstractContainerMenu {

  public static final int CANCEL = Integer.MAX_VALUE - 1;
  public static final int CONFIRM = Integer.MAX_VALUE;
  public static final int IMAGE_OFF = Integer.MAX_VALUE - 2;
  public static final int IMAGE_ON = Integer.MAX_VALUE - 3;

  private final ContainerData progress;

  private final OpeningData data;

  private boolean rejected;

  private boolean createImage = true;

  private int selected = -1;

  public UniverseAnnihilatorMenu(
      int id,
      OpeningData data
  ) {
    this(id, data, null);
  }

  public UniverseAnnihilatorMenu(
      int id,
      OpeningData data,
      UniverseAnnihilator entity
  ) {
    super(UniverseMenus.UNIVERSE_ANNIHILATOR_HANDLER, id);
    this.data = data;
    var initialTask = entity == null ? null : UniverseAnnihilation.getTask(
        ((net.minecraft.server.level.ServerLevel) entity.level()).getServer(), entity.getUUID());
    this.progress = entity == null ? new SimpleContainerData(3) : new ContainerData() {
      @Override public int get(int index) {
        var task = UniverseAnnihilation.getTask(((net.minecraft.server.level.ServerLevel) entity.level()).getServer(), entity.getUUID());
        if (rejected) return index == 0 ? UniverseAnnihilation.FAILED : 0;
        if (task == null || task == initialTask && !data.progressOnly()
            && task.stage() == UniverseAnnihilation.COMPLETE) return 0;
        return switch (index) {
          case 0 -> task.stage();
          case 1 -> task.progress();
          case 2 -> data.dimensions().indexOf(task.dimension);
          default -> 0;
        };
      }
      @Override public void set(int index, int value) {}
      @Override public int getCount() { return 3; }
    };
    addDataSlots(progress);
  }

  @Override
  public boolean clickMenuButton(
      Player player,
      int button
  ) {
    if (data.progressOnly() || getStage() != UniverseAnnihilation.IDLE
        || !(player instanceof ServerPlayer serverPlayer) || !stillValid(player)
        || !UniverseAnnihilation.canUse(serverPlayer)) return false;
    if (button == IMAGE_ON || button == IMAGE_OFF) {
      if (selected < 0) return false;
      createImage = button == IMAGE_ON;
      return true;
    }
    if (button == CANCEL) { selected = -1; return true; }
    if (button >= 0 && button < data.dimensions().size()) {
      selected = button;
      return true;
    }
    if (button != CONFIRM || selected < 0) return false;
    var target = data.dimensions().get(selected);
    selected = -1;
    // Recheck the live entity's dimension, never rely on the client's opening list.
    var entity = player.level().getEntity(data.entityId());
    if (entity == null || target.equals(entity.level().dimension())) return false;
    boolean accepted = UniverseAnnihilation.request(serverPlayer, (UniverseAnnihilator) entity, target, createImage);
    if (!accepted) {
      rejected = true;
      serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable("gui.magnatour.annihilator.rejected"));
    }
    return accepted;
  }

  public OpeningData getOpeningData() { return data; }

  public int getProgress() { return progress.get(1); }

  public int getStage() { return progress.get(0); }

  public int getTaskDimensionIndex() { return progress.get(2); }

  @Override
  public ItemStack quickMoveStack(
      Player player,
      int index
  ) { return ItemStack.EMPTY; }

  @Override
  public boolean stillValid(
      Player player
  ) {
    var entity = player.level().getEntity(data.entityId());
    return entity instanceof UniverseAnnihilator && entity.isAlive()
        && player.distanceToSqr(entity) <= 64;
  }

  public record OpeningData(
      int entityId,
      List<ResourceKey<Level>> dimensions,
      boolean progressOnly
  ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpeningData> PACKET_CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, OpeningData::entityId,
            ResourceKey.<Level>streamCodec(Registries.DIMENSION).apply(ByteBufCodecs.list()),
            OpeningData::dimensions,
            ByteBufCodecs.BOOL, OpeningData::progressOnly, OpeningData::new);

    public OpeningData { dimensions = List.copyOf(dimensions); }

  }

}
