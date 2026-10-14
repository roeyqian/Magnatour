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
import roeyqian.magnatour.menu.universe.UniverseForgeSmeltingMenu;
import roeyqian.magnatour.mixin.screen.AbstractContainerScreenAccessor;
import roeyqian.magnatour.mixinhelper.client.ForgeScreenTransition;

public class UniverseForgeSmeltingScreen extends AbstractRecipeBookScreen<UniverseForgeSmeltingMenu> {

  private static final int BACKGROUND_HEIGHT = 202;
  private static final int BACKGROUND_WIDTH = 176;

  private static final Identifier BURN_PROGRESS_TEXTURE = Identifier.withDefaultNamespace(
      "container/furnace/burn_progress"
  );
  private static final Identifier LIT_PROGRESS_TEXTURE = Identifier.withDefaultNamespace(
      "container/furnace/lit_progress"
  );
  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      "magnatour", "textures/gui/universe_forge/universe_forge_smelting.png"
  );

  private final UniverseForgeSmeltingComponent cookingBook;

  private Button switchButton;

  public UniverseForgeSmeltingScreen(
      UniverseForgeSmeltingMenu handler,
      Inventory inventory,
      Component title
  ) {
    this(handler, new UniverseForgeSmeltingComponent(handler), inventory, title);
  }

  private UniverseForgeSmeltingScreen(
      UniverseForgeSmeltingMenu handler,
      UniverseForgeSmeltingComponent cookingBook,
      Inventory inventory,
      Component title
  ) {
    super(handler, cookingBook, inventory, title);
    this.cookingBook = cookingBook;
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
    this.switchButton.visible = this.width >= UniverseForgeCraftingComponent.MIN_SIDE_BY_SIDE_WIDTH
        || !this.cookingBook.isVisible();
    if (!this.switchButton.visible) return;
    int x = this.leftPos;
    int y = this.topPos;

    graphics.blit(
        RenderPipelines.GUI_TEXTURED, TEXTURE,
        x, y,
        0.0F, 0.0F,
        this.imageWidth, this.imageHeight,
        256, 256
    );

    if (this.menu.isBurning()) {
      int burnProgress = (int) (this.menu.getFuelProgress() * 13.0F) + 1;
      graphics.blitSprite(
          RenderPipelines.GUI_TEXTURED,
          LIT_PROGRESS_TEXTURE,
          14, 14,
          0, 14 - burnProgress,
          x + 56, y + 55 + 14 - burnProgress,
          14, burnProgress
      );
    }

    int cookProgress = (int) (this.menu.getCookProgress() * 24.0F);
    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED,
        BURN_PROGRESS_TEXTURE,
        24, 16,
        0, 0,
        x + 79, y + 53,
        cookProgress + 1, 16
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
        ForgeScreenTransition.begin(this.minecraft, false);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
      }
    }).bounds(this.leftPos + 151, this.topPos + 4, 20, 12)
        .tooltip(Tooltip.create(Component.translatable("container.crafting"))).build());
  }

}
