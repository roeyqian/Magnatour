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
import roeyqian.magnatour.menu.block.UniverseVoidPoolMenu;

public class UniverseVoidPoolScreen extends AbstractContainerScreen<UniverseVoidPoolMenu> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/universe_void_pool.png"
  );

  public UniverseVoidPoolScreen(
      UniverseVoidPoolMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, 176, 166);
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
        RenderPipelines.GUI_TEXTURED,
        TEXTURE,
        this.leftPos, this.topPos,
        0.0F, 0.0F,
        this.imageWidth, this.imageHeight,
        256, 256
    );

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

}