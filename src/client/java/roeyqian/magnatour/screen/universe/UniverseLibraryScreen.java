/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.universe;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;

public class UniverseLibraryScreen extends AbstractContainerScreen<UniverseLibraryMenu> {

  private static final int scrollBarThumbHeight = 15;
  private static final int scrollBarTrackHeight = 106;
  private static final int scrollBarWidth = 12;
  private static final int scrollBarXOffset = 174;
  private static final int scrollBarYOffset = 18;

  private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace(
      "container/creative_inventory/scroller"
  );
  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/container/universe_library.png"
  );

  private int scrollBarX;
  private int scrollBarY;

  private float scrollPosition = 0.0f;

  private boolean isDragging = false;

  public UniverseLibraryScreen(
      UniverseLibraryMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, 195, 222);
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

    if (canScroll()) {
      int movableRange = scrollBarTrackHeight - scrollBarThumbHeight;
      int thumbY = (int) (movableRange * Mth.clamp(this.scrollPosition, 0.0f, 1.0f));

      graphics.blitSprite(
          RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE,
          scrollBarX, scrollBarY + thumbY,
          scrollBarWidth, scrollBarThumbHeight
      );
    }

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseClicked(
      MouseButtonEvent event,
      boolean doubled
  ) {
    double mouseX = event.x();
    double mouseY = event.y();
    int button = event.button();

    if (button == 0 && isPointInScrollbarArea(mouseX, mouseY) && canScroll()) {
      this.isDragging = true;
      updateScrollFromMouseY(mouseY);
      return true;
    }

    return super.mouseClicked(event, doubled);
  }

  @Override
  public boolean mouseDragged(
      @NonNull MouseButtonEvent event,
      double offsetX,
      double offsetY
  ) {
    if (this.isDragging && canScroll()) {
      updateScrollFromMouseY(event.y());
      return true;
    }
    return super.mouseDragged(event, offsetX, offsetY);
  }

  @Override
  public boolean mouseReleased(
      MouseButtonEvent event
  ) {
    if (event.button() == 0) {
      this.isDragging = false;
    }
    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX,
      double mouseY,
      double horizontalAmount,
      double verticalAmount
  ) {
    int maxOffset = getMaxOffset();
    if (maxOffset > 0) {
      int currentOffset = this.menu.scrollOffset.get();
      int direction = verticalAmount > 0 ? -1 : 1;
      int newOffset = Mth.clamp(currentOffset + direction, 0, maxOffset);

      if (newOffset != currentOffset) {
        this.scrollPosition = (float) newOffset / (float) maxOffset;
        if (this.minecraft.gameMode != null) {
          this.minecraft.gameMode.handleInventoryButtonClick(
              this.menu.containerId, newOffset
          );
        }
      }
      return true;
    }
    return false;
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = 8;
    this.titleLabelY = 6;
    this.inventoryLabelX = 8;
    this.inventoryLabelY = 129;

    this.scrollBarX = this.leftPos + scrollBarXOffset;
    this.scrollBarY = this.topPos + scrollBarYOffset;
  }

  private boolean canScroll() {
    return getMaxOffset() > 0;
  }

  private boolean isPointInScrollbarArea(
      double mouseX,
      double mouseY
  ) {
    return mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth
        && mouseY >= scrollBarY && mouseY < scrollBarY + scrollBarTrackHeight;
  }

  private void updateScrollFromMouseY(
      double mouseY
  ) {
    int movableRange = scrollBarTrackHeight - scrollBarThumbHeight;

    double minY = scrollBarY;
    double maxY = scrollBarY + scrollBarTrackHeight;
    double clampedY = Mth.clamp(mouseY, minY, maxY);
    double relativeY = clampedY - minY;

    float newScrollPos = (float) ((relativeY - scrollBarThumbHeight / 2.0) / movableRange);
    this.scrollPosition = Mth.clamp(newScrollPos, 0.0f, 1.0f);

    int maxOffset = getMaxOffset();
    if (maxOffset <= 0) return;

    int newOffset = Math.round(this.scrollPosition * maxOffset);
    newOffset = Mth.clamp(newOffset, 0, maxOffset);

    int currentOffset = this.menu.scrollOffset.get();
    if (newOffset != currentOffset) {
      if (this.minecraft.gameMode != null) {
        this.minecraft.gameMode.handleInventoryButtonClick(
            this.menu.containerId, newOffset
        );
      }
    }
  }

  private int getMaxOffset() {
    int maxRows = (int) Math.ceil(this.menu.getInventorySize() / 9.0);
    return Math.max(0, maxRows - 6);
  }

}
