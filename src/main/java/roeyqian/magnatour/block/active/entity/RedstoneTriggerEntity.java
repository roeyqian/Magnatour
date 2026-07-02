/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block.active.entity;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.active.RedstoneTrigger;
import roeyqian.magnatour.utility.registry.block.RegBlockEntities;

public class RedstoneTriggerEntity extends BlockEntity {

  public static final int DEFAULT_INTERVAL_TICKS = 20;
  public static final int MAX_INTERVAL_TICKS = 72000;

  private static final String ENABLED_KEY = "Enabled";
  private static final String INTERVAL_KEY = "IntervalTicks";
  private static final String MODE_KEY = "Mode";
  private static final String PROGRESS_KEY = "PulseProgress";

  private int intervalTicks = DEFAULT_INTERVAL_TICKS;
  private int pulseProgress = 0;

  private boolean enabled = false;

  private TriggerMode mode = TriggerMode.NORMAL;

  public RedstoneTriggerEntity(
      BlockPos pos,
      BlockState state
  ) {
    super(RegBlockEntities.REDSTONE_TRIGGER_ENTITY, pos, state);
  }

  public static void tick(
      Level world,
      BlockPos pos,
      BlockState state,
      RedstoneTriggerEntity blockEntity
  ) {
    if (world.isClientSide()) return;

    BlockState currentState = blockEntity.getBlockState();

    if (!blockEntity.enabled) {
      blockEntity.pulseProgress = 0;
      blockEntity.setPowered(false);
      return;
    }

    if (blockEntity.mode == TriggerMode.NORMAL) {
      blockEntity.pulseProgress = 0;
      if (!currentState.getValue(RedstoneTrigger.POWERED)) {
        blockEntity.setPowered(true);
      }
      return;
    }

    blockEntity.pulseProgress++;
    if (blockEntity.pulseProgress >= blockEntity.intervalTicks) {
      blockEntity.pulseProgress = 0;
      blockEntity.setPowered(!blockEntity.getBlockState().getValue(RedstoneTrigger.POWERED));
    }
  }

  public void applySettings(
      TriggerMode mode,
      boolean enabled,
      int intervalTicks
  ) {
    TriggerMode nextMode = mode == null ? TriggerMode.NORMAL : mode;
    int nextInterval = Mth.clamp(intervalTicks, 1, MAX_INTERVAL_TICKS);

    boolean modeChanged = this.mode != nextMode;
    boolean enabledChanged = this.enabled != enabled;
    boolean intervalChanged = this.intervalTicks != nextInterval;

    this.mode = nextMode;
    this.enabled = enabled;
    this.intervalTicks = nextInterval;

    if (!enabled) {
      this.pulseProgress = 0;
      this.setPowered(false);
    } else if (nextMode == TriggerMode.NORMAL) {
      this.pulseProgress = 0;
      this.setPowered(true);
    } else if (modeChanged || enabledChanged || intervalChanged) {
      this.pulseProgress = 0;
      this.setPowered(true);
    }

    if (modeChanged || enabledChanged || intervalChanged) {
      this.syncChanged();
    }
  }

  public @NonNull Component getDisplayName() {
    return Component.translatable("block.magnatour.redstone_trigger");
  }

  public int getIntervalTicks() {
    return intervalTicks;
  }

  public TriggerMode getMode() {
    return mode;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  protected void loadAdditional(
      @NonNull ValueInput input
  ) {
    super.loadAdditional(input);
    this.enabled = input.getBooleanOr(ENABLED_KEY, false);
    this.intervalTicks = Mth.clamp(
        input.getIntOr(INTERVAL_KEY, DEFAULT_INTERVAL_TICKS),
        1,
        MAX_INTERVAL_TICKS
    );
    this.mode = TriggerMode.fromId(input.getIntOr(MODE_KEY, TriggerMode.NORMAL.getId()));
    this.pulseProgress = Mth.clamp(input.getIntOr(PROGRESS_KEY, 0), 0, this.intervalTicks);
  }

  @Override
  protected void saveAdditional(
      @NonNull ValueOutput output
  ) {
    super.saveAdditional(output);
    output.putBoolean(ENABLED_KEY, this.enabled);
    output.putInt(INTERVAL_KEY, this.intervalTicks);
    output.putInt(MODE_KEY, this.mode.getId());
    output.putInt(PROGRESS_KEY, this.pulseProgress);
  }

  private void setPowered(
      boolean powered
  ) {
    if (this.level == null) return;

    BlockState state = this.getBlockState();
    if (!state.hasProperty(RedstoneTrigger.POWERED)
        || state.getValue(RedstoneTrigger.POWERED) == powered
    ) {
      return;
    }

    this.level.setBlockAndUpdate(
        this.worldPosition,
        state.setValue(RedstoneTrigger.POWERED, powered)
    );
    this.setChanged();
    RedstoneTrigger.updateSignalNeighbours(this.level, this.worldPosition, state.getBlock());
  }

  private void syncChanged() {
    this.setChanged();
    if (this.level != null && !this.level.isClientSide()) {
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }
  }

public enum TriggerMode {

    NORMAL(0),
    PULSE(1);

    public static final StreamCodec<RegistryFriendlyByteBuf, TriggerMode> PACKET_CODEC =
        StreamCodec.ofMember(
            (mode, buf) -> buf.writeEnum(mode),
            buf -> buf.readEnum(TriggerMode.class)
        );

    private final int id;

    TriggerMode(
        int id
    ) {
      this.id = id;
    }

    public int getId() {
      return id;
    }

    public static TriggerMode fromId(
        int id
    ) {
      for (TriggerMode mode : values()) {
        if (mode.id == id) return mode;
      }
      return NORMAL;
    }

  }

}
