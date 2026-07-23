/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.block;

// Fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.blockentity.supreme.RedstoneTriggerEntity;
import roeyqian.magnatour.level.network.RedstoneTriggerPayload;
import roeyqian.magnatour.menu.supreme.RedstoneTriggerMenu;

public class RedstoneTriggerScreen extends AbstractContainerScreen<RedstoneTriggerMenu> {

  private static final int ERROR_COLOR = 0xFFFF7A7A;
  private static final int TEXT_COLOR = -12566464;

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/redstone_trigger.png"
  );

  private int intervalTicks;

  private boolean enabled;

  private String errorText = "";

  private Button applyButton;
  private Button modeButton;
  private Button powerButton;

  private EditBox intervalField;

  private RedstoneTriggerEntity.TriggerMode mode;

  public RedstoneTriggerScreen(
      RedstoneTriggerMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, 192, 155);
    this.mode = handler.getMode();
    this.enabled = handler.isEnabled();
    this.intervalTicks = handler.getIntervalTicks();
  }

  @Override
  public void extractContents(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        TEXTURE,
        this.leftPos,
        this.topPos,
        0.0F,
        0.0F,
        this.imageWidth,
        this.imageHeight,
        this.imageWidth,
        this.imageHeight
    );

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    if (this.intervalField != null
        && this.intervalField.isFocused()
        && this.minecraft.options.keyInventory.matches(event)
    ) {
      return true;
    }

    if (event.key() == 257) {
      applyInterval();
      return true;
    }

    return super.keyPressed(event);
  }

  @Override
  protected void extractLabels(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    graphics.text(this.font, this.title, 8, 6, TEXT_COLOR, false);
    graphics.text(
        this.font,
        Component.translatable("gui.magnatour.redstone_trigger.frequency"),
        12,
        78,
        TEXT_COLOR,
        false
    );
    graphics.text(this.font, currentStatusText(), 12, 112, TEXT_COLOR, false);

    if (!this.errorText.isEmpty()) {
      graphics.text(this.font, this.errorText, 12, 130, ERROR_COLOR, true);
    }
  }

  @Override
  protected void init() {
    super.init();
    this.inventoryLabelY = 10000;
    this.titleLabelX = 8;
    this.titleLabelY = 6;

    this.modeButton = this.addRenderableWidget(Button.builder(
            modeButtonText(),
            _ -> toggleMode()
        )
        .bounds(this.leftPos + 12, this.topPos + 28, 152, 20)
        .build());

    this.powerButton = this.addRenderableWidget(Button.builder(
            powerButtonText(),
            _ -> toggleEnabled()
        )
        .bounds(this.leftPos + 12, this.topPos + 52, 152, 20)
        .build());

    this.intervalField = new EditBox(
        this.font,
        this.leftPos + 12,
        this.topPos + 90,
        74,
        18,
        Component.translatable("gui.magnatour.redstone_trigger.frequency")
    );
    this.intervalField.setMaxLength(5);
    this.intervalField.setValue(Integer.toString(this.intervalTicks));
    this.addRenderableWidget(this.intervalField);

    this.applyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("gui.magnatour.redstone_trigger.apply"),
            _ -> applyInterval()
        )
        .bounds(this.leftPos + 92, this.topPos + 90, 72, 20)
        .build());

    updateWidgetState();
  }

  private void applyInterval() {
    String value = this.intervalField.getValue().trim();

    try {
      int interval = Integer.parseInt(value);
      if (interval < 1 || interval > RedstoneTriggerEntity.MAX_INTERVAL_TICKS) {
        throw new NumberFormatException(value);
      }

      this.intervalTicks = interval;
      this.intervalField.setValue(Integer.toString(interval));
      this.errorText = "";
      pushSettings();
    } catch (NumberFormatException exception) {
      this.errorText = Component.translatable(
          "gui.magnatour.redstone_trigger.invalid_tick"
      ).getString();
    }
  }

  private Component currentStatusText() {
    if (!this.enabled) {
      return Component.translatable("gui.magnatour.redstone_trigger.status_idle");
    }
    if (this.mode == RedstoneTriggerEntity.TriggerMode.NORMAL) {
      return Component.translatable("gui.magnatour.redstone_trigger.status_normal");
    }
    return Component.translatable("gui.magnatour.redstone_trigger.status_pulse", this.intervalTicks);
  }

  private Component modeButtonText() {
    return Component.translatable(
        "gui.magnatour.redstone_trigger.mode_button",
        this.mode == RedstoneTriggerEntity.TriggerMode.NORMAL
            ? Component.translatable("gui.magnatour.redstone_trigger.mode_normal")
            : Component.translatable("gui.magnatour.redstone_trigger.mode_pulse")
    );
  }

  private void toggleMode() {
    this.mode = this.mode == RedstoneTriggerEntity.TriggerMode.NORMAL
        ? RedstoneTriggerEntity.TriggerMode.PULSE
        : RedstoneTriggerEntity.TriggerMode.NORMAL;
    this.intervalTicks = parseIntervalOrCurrent();
    pushSettings();
  }

  private Component powerButtonText() {
    return Component.translatable(
        this.enabled
            ? "gui.magnatour.redstone_trigger.disable"
            : "gui.magnatour.redstone_trigger.enable"
    );
  }

  private void toggleEnabled() {
    this.enabled = !this.enabled;
    this.intervalTicks = parseIntervalOrCurrent();
    pushSettings();
  }

  private void updateWidgetState() {
    this.modeButton.setMessage(modeButtonText());
    this.powerButton.setMessage(powerButtonText());
    this.intervalField.setEditable(this.mode == RedstoneTriggerEntity.TriggerMode.PULSE);
    this.intervalField.active = this.mode == RedstoneTriggerEntity.TriggerMode.PULSE;
    this.applyButton.active = this.mode == RedstoneTriggerEntity.TriggerMode.PULSE;
  }

  private void pushSettings() {
    this.errorText = "";
    ClientPlayNetworking.send(new RedstoneTriggerPayload(
        this.menu.getBlockPos(),
        this.menu.getDimension(),
        this.mode,
        this.enabled,
        this.intervalTicks
    ));
    updateWidgetState();
  }

  private int parseIntervalOrCurrent() {
    try {
      int parsed = Integer.parseInt(this.intervalField.getValue().trim());
      if (parsed >= 1 && parsed <= RedstoneTriggerEntity.MAX_INTERVAL_TICKS) {
        return parsed;
      }
    } catch (NumberFormatException ignored) {}
    return this.intervalTicks;
  }

}
