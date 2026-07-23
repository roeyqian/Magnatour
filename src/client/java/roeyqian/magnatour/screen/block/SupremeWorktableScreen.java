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
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// Magnatour
import roeyqian.magnatour.menu.supreme.SupremeWorktableMenu;
import roeyqian.magnatour.screen.recipe.SupremeCraftingBookComponent;

public class SupremeWorktableScreen extends AbstractRecipeBookScreen<SupremeWorktableMenu> {

  private static final Identifier TEXTURE = Identifier.withDefaultNamespace(
      "textures/gui/container/crafting_table.png"
  );

  public SupremeWorktableScreen(
      SupremeWorktableMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, new SupremeCraftingBookComponent(handler), inventory, title);
  }

  @Override
  public void extractBackground(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    super.extractBackground(graphics, mouseX, mouseY, delta);
    int xo = this.leftPos;
    int yo = (this.height - this.imageHeight) / 2;
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, TEXTURE,
        xo, yo,
        0.0F, 0.0F,
        this.imageWidth, this.imageHeight,
        256, 256
    );
  }

  @Override
  protected ScreenPosition getRecipeBookButtonPosition() {
    return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = 29;
  }

}
