/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.supreme;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.menu.supreme.ItemHubMenu;

public class ItemHubScreen extends AbstractContainerScreen<ItemHubMenu> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      "magnatour", "textures/gui/container/item_hub.png"
  );

  private ItemHubAnchorsScreen anchorsScreen;

  public ItemHubScreen(
      ItemHubMenu menu,
      Inventory inventory,
      Component title
  ) {
    super(menu, inventory, title, 176, 176);
  }

  @Override
  public boolean charTyped(
      @NonNull CharacterEvent event
  ) {
    return this.anchorsScreen == null ? super.charTyped(event) : this.anchorsScreen.charTyped(event);
  }

  public void closeAnchors() {
    this.anchorsScreen = null;
  }

  @Override
  public void extractContents(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos,
        0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    // Keep the same container open while the modal owns input and tooltips.
    super.extractRenderState(graphics, this.anchorsScreen == null ? mouseX : -1,
        this.anchorsScreen == null ? mouseY : -1, delta);
    if (this.anchorsScreen != null) {
      this.anchorsScreen.extractRenderState(graphics, mouseX, mouseY, delta);
    }
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    return this.anchorsScreen == null ? super.keyPressed(event) : this.anchorsScreen.keyPressed(event);
  }

  @Override
  public boolean mouseClicked(
      @NonNull MouseButtonEvent event,
      boolean doubleClick
  ) {
    return this.anchorsScreen == null ? super.mouseClicked(event, doubleClick)
        : this.anchorsScreen.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseDragged(
      @NonNull MouseButtonEvent event,
      double deltaX,
      double deltaY
  ) {
    return this.anchorsScreen == null ? super.mouseDragged(event, deltaX, deltaY)
        : this.anchorsScreen.mouseDragged(event, deltaX, deltaY);
  }

  @Override
  public boolean mouseReleased(
      @NonNull MouseButtonEvent event
  ) {
    return this.anchorsScreen == null ? super.mouseReleased(event)
        : this.anchorsScreen.mouseReleased(event);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX,
      double mouseY,
      double horizontal,
      double vertical
  ) {
    return this.anchorsScreen == null ? super.mouseScrolled(mouseX, mouseY, horizontal, vertical)
        : this.anchorsScreen.mouseScrolled(mouseX, mouseY, horizontal, vertical);
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (this.anchorsScreen != null) {
      this.anchorsScreen.tick();
    }
  }

  @Override
  protected void extractLabels(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    super.extractLabels(graphics, mouseX, mouseY);
    graphics.text(this.font, Component.translatable("gui.magnatour.item_hub.anchor_count",
        this.menu.getAnchoredItemIds().size()), 8, 136, 0xFF404040, false);
  }

  @Override
  protected void init() {
    super.init();
    this.inventoryLabelY = 39;
    this.addRenderableWidget(Button.builder(
        Component.translatable("gui.magnatour.item_hub.manage_anchors"), _ -> openAnchors()
    ).bounds(this.leftPos + 8, this.topPos + 148, 160, 20).build());
    if (this.anchorsScreen != null) {
      this.anchorsScreen.init(this.width, this.height);
    }
  }

  private void openAnchors() {
    this.anchorsScreen = new ItemHubAnchorsScreen(this, this.menu);
    this.anchorsScreen.init(this.width, this.height);
  }

}
