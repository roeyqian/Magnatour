/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.universe;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// Magnatour
import roeyqian.magnatour.level.UniverseAnnihilation;
import roeyqian.magnatour.menu.universe.UniverseAnnihilatorMenu;

public final class UniverseAnnihilatorScreen extends AbstractContainerScreen<UniverseAnnihilatorMenu> {

  private boolean showingProgress;
  private boolean submitted;

  private int page;

  private int selected = -1;

  public UniverseAnnihilatorScreen(
      UniverseAnnihilatorMenu menu,
      Inventory inventory,
      Component title
  ) {
    super(menu, inventory, title, 300, 240);
  }

  @Override
  public void extractContents(
      GuiGraphicsExtractor graphics,
      int mouseX, int mouseY,
      float delta
  ) {
    graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF161021);
    graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xFF62E6DD);
    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (showingProgress != progressMode()) createButtons();
  }

  @Override
  protected void extractLabels(
      GuiGraphicsExtractor graphics,
      int mouseX, int mouseY
  ) {
    graphics.text(font, title, 12, 12, 0xFFAAEFFF, false);
    if (progressMode()) {
      int stage = menu.getStage();
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.progress"), 12, 43, -1, false);
      int index = menu.getTaskDimensionIndex();
      if (index >= 0 && index < menu.getOpeningData().dimensions().size()) {
        graphics.text(font, Component.literal(menu.getOpeningData().dimensions().get(index).identifier().toString()),
            12, 62, 0xFFAAEFFF, false);
      }
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.stage." + stage), 12, 89, -1, false);
      int progress = Math.clamp(menu.getProgress(), 0, 10000);
      graphics.fill(12, 112, 288, 130, 0xFF30233E);
      graphics.fill(13, 113, 13 + 274 * progress / 10000, 129,
          stage == UniverseAnnihilation.FAILED ? 0xFFE45A79 : 0xFF62E6DD);
      graphics.text(font, Component.literal(String.format(java.util.Locale.ROOT, "%.1f%%", progress / 100.0)),
          126, 117, 0xFFFFFFFF, false);
      graphics.text(font, Component.translatable(stage == UniverseAnnihilation.FAILED
          ? "gui.magnatour.annihilator.failure_hint" : "gui.magnatour.annihilator.progress_hint"),
          12, 146, 0xFFFFC1DD, false);
    } else if (selected < 0) {
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.select"), 12, 29, -1, false);
    } else {
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.target"), 12, 43, 0xFFFF8080, false);
      graphics.text(font, Component.literal(menu.getOpeningData().dimensions().get(selected).identifier().toString()),
          12, 60, -1, false);
      for (int i = 0; i < 4; i++) {
        graphics.text(font, Component.translatable("gui.magnatour.annihilator.warning." + i),
            12, 88 + i * 20, 0xFFFFC1DD, false);
      }
    }
  }

  @Override
  protected void init() {
    super.init();
    createButtons();
  }

  private boolean progressMode() {
    return submitted || menu.getOpeningData().progressOnly() || menu.getStage() != UniverseAnnihilation.IDLE;
  }

  private void createButtons() {
    clearWidgets();
    showingProgress = progressMode();
    if (showingProgress) {
      addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
          .bounds(leftPos + 80, topPos + 198, 140, 20).build());
      return;
    }
    if (selected >= 0) {
      addRenderableWidget(Button.builder(Component.translatable("gui.magnatour.annihilator.confirm"), button -> {
        submitted = true;
        send(UniverseAnnihilatorMenu.CONFIRM);
        createButtons();
      }).bounds(leftPos + 12, topPos + 198, 132, 20).build());
      addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> {
        send(UniverseAnnihilatorMenu.CANCEL);
        selected = -1;
        createButtons();
      }).bounds(leftPos + 156, topPos + 198, 132, 20).build());
      return;
    }
    var dimensions = menu.getOpeningData().dimensions();
    for (int row = 0; row < 6 && page * 6 + row < dimensions.size(); row++) {
      int index = page * 6 + row;
      addRenderableWidget(Button.builder(Component.literal(dimensions.get(index).identifier().toString()), button -> {
        selected = index;
        send(index);
        createButtons();
      }).bounds(leftPos + 12, topPos + 45 + row * 24, 276, 20).build());
    }
    Button previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
      page--;
      createButtons();
    }).bounds(leftPos + 12, topPos + 198, 40, 20).build());
    previous.active = page > 0;
    Button next = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
      page++;
      createButtons();
    }).bounds(leftPos + 248, topPos + 198, 40, 20).build());
    next.active = (page + 1) * 6 < dimensions.size();
    addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
        .bounds(leftPos + 80, topPos + 198, 140, 20).build());
  }

  private void send(
      int button
  ) {
    if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
  }

}
