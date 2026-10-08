/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.universe;

// Java Standard
import java.util.Locale;

// Mojang
import com.mojang.blaze3d.platform.InputConstants;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.universe.UniverseLibraryContents;
import roeyqian.magnatour.menu.universe.UniverseLibraryMenu;

public class UniverseLibraryScreen extends AbstractContainerScreen<UniverseLibraryMenu> {

  private static final int scrollBarThumbHeight = 15;
  private static final int scrollBarTrackHeight = 106;
  private static final int scrollBarWidth = 12;
  private static final int scrollBarXOffset = 174;
  private static final int scrollBarYOffset = 18 + UniverseLibraryMenu.SEARCH_PANEL_HEIGHT;
  private static final int SEARCH_X = 8;
  private static final int SEARCH_Y = 20;
  private static final int SEARCH_WIDTH = 108;
  private static final int RESULT_X = 122;
  private static final int RESULT_Y = 20;

  private EditBox searchField;

  private boolean invalidSearch = false;
  private boolean extractingStorageSlot;

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
    super(handler, inventory, title, 195, 222 + UniverseLibraryMenu.SEARCH_PANEL_HEIGHT);
  }

  public static void drawCount(
      GuiGraphicsExtractor graphics,
      Font font,
      String text,
      float x,
      float y,
      float scale,
      int color,
      boolean shadow
  ) {
    graphics.pose().pushMatrix();
    try {
      graphics.pose().translate(x, y);
      graphics.pose().scale(scale, scale);
      graphics.text(font, text, 0, 0, color, shadow);
    } finally {
      graphics.pose().popMatrix();
    }
  }

  public static float getCountScale(
      String text
  ) {
    String visibleText = ChatFormatting.stripFormatting(text);
    int length = visibleText.codePointCount(0, visibleText.length());
    return length <= 3 ? 1.0F : 3.0F / length;
  }

  @Override
  public void extractContents(
      @NonNull GuiGraphicsExtractor graphics,
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
    this.drawSearchResult(graphics, mouseX, mouseY);
  }

  @Override
  public void extractRenderState(
      @NonNull GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    super.extractRenderState(graphics, mouseX, mouseY, delta);
    if (this.invalidSearch && this.searchField.isMouseOver(mouseX, mouseY)) {
      graphics.setTooltipForNextFrame(this.font,
          Component.translatable("gui.magnatour.universe_library.invalid"), mouseX, mouseY);
    }
  }

  public boolean isExtractingStorageSlot() {
    return this.extractingStorageSlot;
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    if (this.searchField != null && this.searchField.isFocused()) {
      if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
        this.submitSearch();
      } else if (event.key() == InputConstants.KEY_ESCAPE) {
        this.searchField.setFocused(false);
        this.setFocused(null);
      } else {
        this.searchField.keyPressed(event);
      }
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean mouseClicked(
      @NonNull MouseButtonEvent event,
      boolean doubled
  ) {
    double mouseX = event.x();
    double mouseY = event.y();
    int button = event.button();

    if (this.searchField.isMouseOver(mouseX, mouseY)) {
      this.setFocused(this.searchField);
      return this.searchField.mouseClicked(event, doubled);
    }
    this.searchField.setFocused(false);
    this.setFocused(null);

    if (button == InputConstants.MOUSE_BUTTON_LEFT && isPointInScrollbarArea(mouseX, mouseY) && canScroll()) {
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
      @NonNull MouseButtonEvent event
  ) {
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.isDragging) {
      this.isDragging = false;
      return true;
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
  protected void extractSlot(
      @NonNull GuiGraphicsExtractor graphics,
      @NonNull Slot slot,
      int mouseX,
      int mouseY
  ) {
    if (slot.index == UniverseLibraryMenu.SEARCH_RESULT_SLOT) return;
    this.extractingStorageSlot = slot.index >= 0 && slot.index < 54;
    try {
      super.extractSlot(graphics, slot, mouseX, mouseY);
    } finally {
      this.extractingStorageSlot = false;
    }
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = 8;
    this.titleLabelY = 6;
    this.inventoryLabelX = 8;
    this.inventoryLabelY = 129 + UniverseLibraryMenu.SEARCH_PANEL_HEIGHT;

    this.scrollBarX = this.leftPos + scrollBarXOffset;
    this.scrollBarY = this.topPos + scrollBarYOffset;

    String previousQuery = this.searchField == null ? "" : this.searchField.getValue();
    this.searchField = new EditBox(this.font,
        this.leftPos + SEARCH_X + 3, this.topPos + SEARCH_Y + 4,
        SEARCH_WIDTH - 6, 12,
        Component.translatable("gui.magnatour.universe_library.search"));
    this.searchField.setBordered(false);
    this.searchField.setMaxLength(256);
    this.searchField.setHint(Component.translatable("gui.magnatour.universe_library.search")
        .withStyle(ChatFormatting.WHITE));
    this.searchField.setValue(previousQuery);
    this.searchField.setTextColor(this.invalidSearch ? 0xFFFF5555 : 0xFFFFFFFF);
    this.searchField.setResponder(_ -> {
      this.invalidSearch = false;
      this.searchField.setTextColor(0xFFFFFFFF);
    });
    this.addRenderableWidget(this.searchField);
  }

  private int getMaxOffset() {
    int maxRows = (int) Math.ceil(this.menu.getInventorySize() / 9.0);
    return Math.max(0, maxRows - 6);
  }

  private boolean isOverResult(
      double mouseX,
      double mouseY
  ) {
    return mouseX >= this.leftPos + RESULT_X - 1 && mouseX < this.leftPos + RESULT_X + 17
        && mouseY >= this.topPos + RESULT_Y - 1 && mouseY < this.topPos + RESULT_Y + 17;
  }

  private boolean canScroll() {
    return getMaxOffset() > 0;
  }

  private void drawSearchResult(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    ItemStack icon = this.menu.getSearchIcon();
    if (icon.isEmpty()) return;
    if (this.isOverResult(mouseX, mouseY) && this.menu.getSearchCount() > 0) {
      graphics.fill(this.leftPos + RESULT_X, this.topPos + RESULT_Y,
          this.leftPos + RESULT_X + 16, this.topPos + RESULT_Y + 16, 0x80FFFFFF);
    }
    graphics.item(icon, this.leftPos + RESULT_X, this.topPos + RESULT_Y);
    String countText = "x" + UniverseLibraryContents.formatCount(this.menu.getSearchCount());
    drawCount(graphics, this.font, countText,
        this.leftPos + 144, this.topPos + RESULT_Y + 4, getCountScale(countText), 0xFF404040, false);
  }

  private void submitSearch() {
    String query = this.searchField.getValue().strip();
    if (query.isEmpty()) {
      this.sendMenuButton(UniverseLibraryMenu.CLEAR_SEARCH_BUTTON);
      return;
    }

    Identifier id = Identifier.tryParse(query.toLowerCase(Locale.ROOT));
    Item result = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    if (result == null) {
      for (Item item : BuiltInRegistries.ITEM) {
        if (item == Items.AIR) continue;
        if (new ItemStack(item).getHoverName().getString().equalsIgnoreCase(query)) {
          if (result != null) {
            this.rejectSearch();
            return;
          }
          result = item;
        }
      }
    }

    if (result == null || result == Items.AIR) {
      this.rejectSearch();
      return;
    }
    this.invalidSearch = false;
    this.searchField.setTextColor(0xFFFFFFFF);
    this.sendMenuButton(UniverseLibraryMenu.SEARCH_BUTTON_BASE + Item.getId(result));
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

  private void sendMenuButton(
      int id
  ) {
    if (this.minecraft.gameMode != null) {
      this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }
  }

  private void rejectSearch() {
    this.invalidSearch = true;
    this.searchField.setTextColor(0xFFFF5555);
    this.sendMenuButton(UniverseLibraryMenu.CLEAR_SEARCH_BUTTON);
  }

}
