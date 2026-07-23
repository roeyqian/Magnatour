/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.block;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.menu.supreme.SupremeReserverMenu;

public class SupremeReserverScreen extends AbstractContainerScreen<SupremeReserverMenu> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/supreme_reserver.png"
  );

  private Button nextButton;
  private Button prevButton;

  public SupremeReserverScreen(
      SupremeReserverMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, 176, 166);
  }

  @Override
  public void extractContents(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, TEXTURE,
        this.leftPos, this.topPos,
        0.0F, 0.0F,
        this.imageWidth, this.imageHeight,
        256, 256
    );

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public void extractRenderState(
      @NonNull GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    updateButtonStates();
    super.extractRenderState(graphics, mouseX, mouseY, delta);
  }

  @Override
  protected void extractLabels(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    graphics.text(
        this.font, this.title, this.titleLabelX, this.titleLabelY,
        -12566464, false
    );
    graphics.text(
        this.font, this.playerInventoryTitle,
        this.inventoryLabelX, this.inventoryLabelY,
        -12566464, false
    );

    int recipeCount = this.menu.getRecipeCount();
    if (recipeCount > 1) {
      String recipeInfo = (this.menu.getCurrentRecipeIndex() + 1) + "/" + recipeCount;
      int textWidth = this.font.width(recipeInfo);
      graphics.text(this.font, Component.literal(recipeInfo),
          71 - textWidth / 2, 50, 0x404040, false);
    }
  }

  @Override
  protected void init() {
    super.init();

    int x = this.leftPos;
    int y = this.topPos;

    this.prevButton = this.addRenderableWidget(Button.builder(
            Component.literal("<"), _ -> {
              if (this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId, 0
                );
              }
            })
        .bounds(x + 57, y + 60, 14, 14)
        .build());

    this.nextButton = this.addRenderableWidget(Button.builder(
            Component.literal(">"), _ -> {
              if (this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId, 1
                );
              }
            })
        .bounds(x + 71, y + 60, 14, 14)
        .build());

    updateButtonStates();
  }

  private void updateButtonStates() {
    boolean hasMultiple = this.menu.hasMultipleRecipes();

    this.prevButton.visible = true;
    this.nextButton.visible = true;
    this.prevButton.active = hasMultiple;
    this.nextButton.active = hasMultiple;
  }

}
