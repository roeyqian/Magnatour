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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.menu.supreme.SupremeChestMenu;

public class SupremeChestScreen extends AbstractContainerScreen<SupremeChestMenu> {

  private static final Identifier DOUBLE_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/supreme_chest_double.png"
  );
  private static final Identifier SINGLE_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/supreme_chest_single.png"
  );
  private static final Identifier TRIPLE_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/supreme_chest_triple.png"
  );

  private final int rows;

  public SupremeChestScreen(
      SupremeChestMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, 256, 114 + handler.getRowCount() * 18);
    this.rows = handler.getRowCount();
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void extractContents(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, getTexture(),
        this.leftPos, this.topPos,
        0.0F, 0.0F,
        this.imageWidth, this.imageHeight,
        256, 276
    );

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = 8;
    this.titleLabelY = 6;
    this.inventoryLabelX = SupremeChestMenu.PLAYER_INVENTORY_X;
    this.inventoryLabelY = 18 + this.rows * 18 + 2;
  }

  private Identifier getTexture() {
    return switch (this.rows) {
      case SupremeChestMenu.DOUBLE_ROWS -> DOUBLE_TEXTURE;
      case SupremeChestMenu.TRIPLE_ROWS -> TRIPLE_TEXTURE;
      default -> SINGLE_TEXTURE;
    };
  }

}
