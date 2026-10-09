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
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.universe.UniverseWorkstationMenu;
import roeyqian.magnatour.mixin.screen.AbstractContainerScreenAccessor;

public class UniverseWorkstationScreen extends AbstractRecipeBookScreen<UniverseWorkstationMenu> {

  private static final int BACKGROUND_HEIGHT = 202;
  private static final int BACKGROUND_WIDTH = 176;

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      "magnatour", "textures/gui/container/universe_workstation.png"
  );

  private final UniverseCraftingBookComponent craftingBook;

  public UniverseWorkstationScreen(
      UniverseWorkstationMenu handler,
      Inventory inventory,
      Component title
  ) {
    this(handler, new UniverseCraftingBookComponent(handler), inventory, title);
  }

  private UniverseWorkstationScreen(
      UniverseWorkstationMenu handler,
      UniverseCraftingBookComponent craftingBook,
      Inventory inventory,
      Component title
  ) {
    super(handler, craftingBook, inventory, title);
    this.craftingBook = craftingBook;
    AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
    accessor.magnatour$setImageWidth(BACKGROUND_WIDTH);
    accessor.magnatour$setImageHeight(BACKGROUND_HEIGHT);
  }

  @Override
  public void extractBackground(
      @NonNull GuiGraphicsExtractor graphics,
      int mouseX, int mouseY,
      float delta
  ) {
    super.extractBackground(graphics, mouseX, mouseY, delta);
    if (this.width < UniverseCraftingBookComponent.MIN_SIDE_BY_SIDE_WIDTH && this.craftingBook.isVisible()) return;
    int xo = this.leftPos;
    int yo = this.topPos;
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, TEXTURE,
        xo, yo,
        0.0F, 0.0F,
        this.imageWidth, this.imageHeight,
        256, 256
    );
  }

  @Override @NonNull
  protected ScreenPosition getRecipeBookButtonPosition() {
    return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = 29;
    this.inventoryLabelY = 108;
  }

}
