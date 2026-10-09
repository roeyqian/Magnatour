/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.supreme;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.level.network.ItemHubPayload;
import roeyqian.magnatour.menu.supreme.ItemHubMenu;

/** A modal screen rendered by its parent without closing the container menu. */
public class ItemHubAnchorsScreen extends Screen {

  private static final int ROWS_PER_PAGE = 5;
  private static final int WINDOW_HEIGHT = 238;
  private static final int WINDOW_WIDTH = 256;

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      "magnatour", "textures/gui/container/item_hub_anchors.png"
  );

  private final ItemHubScreen parent;

  private final ItemHubMenu menu;

  private final List<Button> removeButtons = new ArrayList<>();

  private boolean error;

  private int left;
  private int page;
  private int top;

  private Button nextButton;
  private Button previousButton;

  private EditBox itemIdField;

  private String pendingItemId = "";

  private Component status = Component.empty();

  private List<String> anchors = List.of();

  public ItemHubAnchorsScreen(
      ItemHubScreen parent,
      ItemHubMenu menu
  ) {
    super(Component.translatable("gui.magnatour.item_hub.anchors_title"));
    this.parent = parent;
    this.menu = menu;
  }

  @Override
  public void extractRenderState(
      @NonNull GuiGraphicsExtractor graphics,
      int mouseX, int mouseY,
      float delta
  ) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.left, this.top,
        0.0F, 0.0F, WINDOW_WIDTH, WINDOW_HEIGHT, 256, 256);
    graphics.text(this.font, this.title, this.left + 10, this.top + 10, 0xFF303030, false);
    graphics.text(this.font, Component.translatable("gui.magnatour.item_hub.anchor_capacity",
        this.anchors.size(), ItemHubEntity.MAX_ANCHORED_ITEMS), this.left + 10, this.top + 21,
        0xFF505050, false);
    if (this.anchors.isEmpty()) {
      // Hide unused row dividers and center the wrapped hint within the list.
      graphics.fill(this.left + 11, this.top + 33, this.left + 245, this.top + 151, 0xFFB3B8B7);
      var lines = this.font.split(Component.translatable("gui.magnatour.item_hub.empty"),
          WINDOW_WIDTH - 36);
      int textY = this.top + 32 + (120 - lines.size() * this.font.lineHeight) / 2;
      for (var line : lines) {
        graphics.text(this.font, line, this.left + (WINDOW_WIDTH - this.font.width(line)) / 2,
            textY, 0xFF505050, false);
        textY += this.font.lineHeight;
      }
    }
    for (int row = 0; row < ROWS_PER_PAGE; row++) {
      int index = this.page * ROWS_PER_PAGE + row;
      if (index >= this.anchors.size()) break;
      int y = this.top + 32 + row * 24;
      String itemId = this.anchors.get(index);
      ItemStack stack = stackFor(itemId);
      graphics.fakeItem(stack, this.left + 13, y + 4);
      graphics.text(this.font, this.font.plainSubstrByWidth(stack.getHoverName().getString(), 163),
          this.left + 34, y + 3, 0xFF303030, false);
      graphics.text(this.font, this.font.plainSubstrByWidth(itemId, 163),
          this.left + 34, y + 13, 0xFF666666, false);
      if (mouseX >= this.left + 10 && mouseX < this.left + 200 && mouseY >= y && mouseY < y + 24) {
        graphics.setTooltipForNextFrame(this.font, List.of(stack.getHoverName(), Component.literal(itemId)),
            java.util.Optional.empty(), mouseX, mouseY);
      }
    }
    graphics.text(this.font, Component.translatable("gui.magnatour.item_hub.anchor_item_id"),
        this.left + 10, this.top + 160, 0xFF404040, false);
    graphics.text(this.font, this.font.plainSubstrByWidth(this.status.getString(), WINDOW_WIDTH - 20),
        this.left + 10, this.top + 195, this.error ? 0xFFAA2222 : 0xFF286448, false);
    if (mouseY >= this.top + 193 && mouseY < this.top + 205 && !this.status.getString().isEmpty()) {
      graphics.setTooltipForNextFrame(this.font, this.status, mouseX, mouseY);
    }
    graphics.text(this.font, Component.translatable("gui.magnatour.item_hub.page",
        this.page + 1, pageCount()), this.left + 82, this.top + 217, 0xFF404040, false);
    super.extractRenderState(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    if (event.key() == 257 || event.key() == 335) {
      addAnchor();
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontal, double vertical
  ) {
    if (vertical != 0 && mouseY >= this.top + 32 && mouseY < this.top + 152) {
      this.page = Math.clamp(this.page + (vertical > 0 ? -1 : 1), 0, pageCount() - 1);
      updateButtons();
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
  }

  @Override
  public void onClose() {
    this.parent.closeAnchors();
  }

  @Override
  public void tick() {
    this.anchors = this.menu.getAnchoredItemIds();
    this.page = Math.clamp(this.page, 0, pageCount() - 1);
    if (!this.pendingItemId.isEmpty() && this.anchors.contains(this.pendingItemId)) {
      this.status = Component.translatable("gui.magnatour.item_hub.anchored",
          stackFor(this.pendingItemId).getHoverName());
      this.pendingItemId = "";
      this.error = false;
    } else if (!this.pendingItemId.isEmpty() && this.anchors.size() >= ItemHubEntity.MAX_ANCHORED_ITEMS) {
      showError("full");
      this.pendingItemId = "";
    }
    updateButtons();
  }

  @Override
  protected void init() {
    String draft = this.itemIdField == null ? "" : this.itemIdField.getValue();
    this.left = (this.width - WINDOW_WIDTH) / 2;
    this.top = (this.height - WINDOW_HEIGHT) / 2;
    this.anchors = this.menu.getAnchoredItemIds();
    this.removeButtons.clear();
    for (int row = 0; row < ROWS_PER_PAGE; row++) {
      final int visibleRow = row;
      this.removeButtons.add(this.addRenderableWidget(Button.builder(
          Component.translatable("gui.magnatour.item_hub.remove_anchor"), _ -> removeAnchor(visibleRow)
      ).bounds(this.left + 202, this.top + 34 + row * 24, 42, 20).build()));
    }
    this.itemIdField = new EditBox(this.font, this.left + 10, this.top + 172, 180, 18,
        Component.translatable("gui.magnatour.item_hub.anchor_item_id"));
    this.itemIdField.setMaxLength(128);
    this.itemIdField.setValue(draft);
    this.addRenderableWidget(this.itemIdField);
    this.addRenderableWidget(Button.builder(Component.translatable("gui.magnatour.item_hub.add_anchor"),
        _ -> addAnchor()).bounds(this.left + 194, this.top + 171, 50, 20).build());
    this.previousButton = this.addRenderableWidget(Button.builder(Component.literal("<"), _ -> {
      this.page--;
      updateButtons();
    }).bounds(this.left + 10, this.top + 210, 28, 20).build());
    this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), _ -> {
      this.page++;
      updateButtons();
    }).bounds(this.left + 42, this.top + 210, 28, 20).build());
    this.addRenderableWidget(Button.builder(Component.translatable("gui.magnatour.item_hub.back"),
        _ -> onClose()).bounds(this.left + 180, this.top + 210, 64, 20).build());
    updateButtons();
    this.setInitialFocus(this.itemIdField);
  }

  private static ItemStack stackFor(
      String itemId
  ) {
    return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)));
  }

  private void showError(
      String key
  ) {
    this.status = Component.translatable("gui.magnatour.item_hub." + key);
    this.error = true;
  }

  private void sendAction(
      int action,
      String itemId
  ) {
    ClientPlayNetworking.send(new ItemHubPayload(this.menu.getBlockPos(), this.menu.getDimension(),
        action, itemId));
  }

  private int pageCount() {
    return Math.max(1, (this.anchors.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
  }

  private void removeAnchor(
      int row
  ) {
    int index = this.page * ROWS_PER_PAGE + row;
    if (index < this.anchors.size()) {
      sendAction(ItemHubPayload.REMOVE, this.anchors.get(index));
      this.status = Component.empty();
      this.error = false;
    }
  }

  private void addAnchor() {
    String itemId = ItemHubEntity.normalizeAnchorItemId(this.itemIdField.getValue());
    if (itemId == null) {
      showError("invalid_item");
      return;
    }
    if (this.menu.getAnchoredItemIds().contains(itemId)) {
      showError("duplicate");
      return;
    }
    if (this.menu.getAnchoredItemIds().size() >= ItemHubEntity.MAX_ANCHORED_ITEMS) {
      showError("full");
      return;
    }
    sendAction(ItemHubPayload.ADD, itemId);
    this.pendingItemId = itemId;
    this.status = Component.empty();
    this.error = false;
    this.itemIdField.setValue("");
  }

  private void updateButtons() {
    this.previousButton.active = this.page > 0;
    this.nextButton.active = this.page + 1 < pageCount();
    for (int row = 0; row < this.removeButtons.size(); row++) {
      this.removeButtons.get(row).visible = this.page * ROWS_PER_PAGE + row < this.anchors.size();
    }
  }

}
