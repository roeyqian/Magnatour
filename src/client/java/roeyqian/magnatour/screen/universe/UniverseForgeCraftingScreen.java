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
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.universe.UniverseForgeCraftingMenu;
import roeyqian.magnatour.mixin.screen.AbstractContainerScreenAccessor;
import roeyqian.magnatour.mixinhelper.client.ForgeScreenTransition;

public class UniverseForgeCraftingScreen extends AbstractRecipeBookScreen<UniverseForgeCraftingMenu> {

  private static final int BACKGROUND_HEIGHT = 202;
  private static final int BACKGROUND_WIDTH = 176;

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      "magnatour", "textures/gui/container/universe_forge_crafting.png"
  );

  private final UniverseCraftingBookComponent craftingBook;

  private Button switchButton;

  public UniverseForgeCraftingScreen(
      UniverseForgeCraftingMenu handler,
      Inventory inventory,
      Component title
  ) {
    this(handler, new UniverseCraftingBookComponent(handler), inventory, title);
  }

  private UniverseForgeCraftingScreen(
      UniverseForgeCraftingMenu handler,
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
    this.switchButton.setPosition(this.leftPos + 151, this.topPos + 4);
    this.switchButton.visible = this.width >= UniverseCraftingBookComponent.MIN_SIDE_BY_SIDE_WIDTH
        || !this.craftingBook.isVisible();
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
    this.switchButton = this.addRenderableWidget(Button.builder(Component.literal("⇄"), button -> {
      if (this.minecraft.gameMode != null) {
        ForgeScreenTransition.begin(this.minecraft, true);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
      }
    }).bounds(this.leftPos + 151, this.topPos + 4, 20, 12)
        .tooltip(Tooltip.create(Component.translatable("container.furnace"))).build());
  }

}
