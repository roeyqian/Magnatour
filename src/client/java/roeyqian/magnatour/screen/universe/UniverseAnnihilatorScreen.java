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
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.UniverseAnnihilation;
import roeyqian.magnatour.menu.universe.UniverseAnnihilatorMenu;

public final class UniverseAnnihilatorScreen extends AbstractContainerScreen<UniverseAnnihilatorMenu> {

  private static final int TEXT_COLOR = 0xFF404040;
  private static final int WARNING_COLOR = 0xFFAA0000;

  private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/general/universe_annihilator.png"
  );

  private boolean showingProgress;
  private boolean submitted;

  private int page;

  private boolean createImage = true;

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
    graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
        leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);
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
    graphics.text(font, title, 8, 6, TEXT_COLOR, false);
    if (progressMode()) {
      int stage = menu.getStage();
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.progress"), 12, 43, TEXT_COLOR, false);
      int index = menu.getTaskDimensionIndex();
      if (index >= 0 && index < menu.getOpeningData().dimensions().size()) {
        graphics.text(font, UniverseAnnihilation.dimensionName(menu.getOpeningData().dimensions().get(index)),
            12, 62, TEXT_COLOR, false);
      }
      drawWrappedText(graphics, Component.translatable("gui.magnatour.annihilator.stage." + stage),
          89, stage == UniverseAnnihilation.FAILED ? WARNING_COLOR : TEXT_COLOR);
      int progress = Math.clamp(menu.getProgress(), 0, 10000);
      graphics.fill(12, 112, 288, 130, 0xFF373737);
      graphics.fill(13, 113, 288, 130, 0xFFFFFFFF);
      graphics.fill(13, 113, 287, 129, 0xFF8B8B8B);
      int progressRight = 13 + 274 * progress / 10000;
      if (progressRight > 13) {
        graphics.fill(13, 113, progressRight, 129,
            stage == UniverseAnnihilation.FAILED ? WARNING_COLOR : 0xFF00AA00);
        graphics.fill(13, 113, progressRight, 114,
            stage == UniverseAnnihilation.FAILED ? 0xFFFF5555 : 0xFF55FF55);
      }
      Component percentage = Component.literal(String.format(java.util.Locale.ROOT, "%.1f%%", progress / 100.0));
      graphics.text(font, percentage, (imageWidth - font.width(percentage)) / 2, 117, 0xFFFFFFFF, true);
      drawWrappedText(graphics, Component.translatable(stage == UniverseAnnihilation.FAILED
          ? "gui.magnatour.annihilator.failure_hint" : "gui.magnatour.annihilator.progress_hint"),
          146, stage == UniverseAnnihilation.FAILED ? WARNING_COLOR : TEXT_COLOR);
    } else if (selected < 0) {
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.select"), 12, 29, TEXT_COLOR, false);
    } else {
      graphics.text(font, Component.translatable("gui.magnatour.annihilator.target"), 12, 43, WARNING_COLOR, false);
      graphics.text(font, UniverseAnnihilation.dimensionName(menu.getOpeningData().dimensions().get(selected)),
          12, 60, TEXT_COLOR, false);
      for (int i = 0; i < 4; i++) {
        String key = i == 1 && !createImage ? "gui.magnatour.annihilator.warning.no_image"
            : "gui.magnatour.annihilator.warning." + i;
        drawWrappedText(graphics, Component.translatable(key),
            88 + i * 20, i == 1 && !createImage ? WARNING_COLOR : TEXT_COLOR);
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
      addRenderableWidget(Checkbox.builder(Component.translatable("gui.magnatour.annihilator.create_image")
              .withStyle(style -> style.withColor(TEXT_COLOR & 0xFFFFFF).withoutShadow()), font)
          .pos(leftPos + 12, topPos + 174).maxWidth(276).selected(createImage)
          .onValueChange((checkbox, checked) -> {
            createImage = checked;
            send(checked ? UniverseAnnihilatorMenu.IMAGE_ON : UniverseAnnihilatorMenu.IMAGE_OFF);
          }).build());
      addRenderableWidget(Button.builder(Component.translatable("gui.magnatour.annihilator.confirm"), button -> {
        submitted = true;
        send(createImage ? UniverseAnnihilatorMenu.IMAGE_ON : UniverseAnnihilatorMenu.IMAGE_OFF);
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
      addRenderableWidget(Button.builder(UniverseAnnihilation.dimensionName(dimensions.get(index)), button -> {
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

  private void drawWrappedText(
      GuiGraphicsExtractor graphics,
      Component text,
      int y, int color
  ) {
    for (var line : font.split(text, imageWidth - 24)) {
      graphics.text(font, line, 12, y, color, false);
      y += font.lineHeight;
    }
  }

}
