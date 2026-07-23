/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.supreme;

// Fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.supreme.ItemHubEntity;
import roeyqian.magnatour.level.network.ItemHubPayload;
import roeyqian.magnatour.menu.supreme.ItemHubMenu;

public class ItemHubScreen extends AbstractContainerScreen<ItemHubMenu> {

  private static final int ERROR_COLOR = 0xFFFF7A7A;
  private static final int HINT_COLOR = 0xFF6A6A6A;
  private static final int PANEL_COLOR = 0xFFC6C6C6;
  private static final int PANEL_OUTLINE_COLOR = 0xFF8B8B8B;
  private static final int PANEL_TOP = 133;
  private static final int TEXT_COLOR = -12566464;

  private static final Identifier TEXTURE = Identifier.withDefaultNamespace(
      "textures/gui/container/hopper.png"
  );

  private String errorText = "";
  private String filterItemId;

  private EditBox filterField;

  public ItemHubScreen(
      ItemHubMenu menu,
      Inventory inventory,
      Component title
  ) {
    super(menu, inventory, title, 176, 186);
    this.filterItemId = menu.getFilterItemId();
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
        this.leftPos,
        this.topPos,
        0.0F,
        0.0F,
        176,
        PANEL_TOP,
        256,
        256
    );
    graphics.fill(
        this.leftPos + 1,
        this.topPos + PANEL_TOP + 1,
        this.leftPos + this.imageWidth - 1,
        this.topPos + this.imageHeight - 1,
        PANEL_COLOR
    );
    graphics.outline(
        this.leftPos,
        this.topPos + PANEL_TOP,
        this.imageWidth,
        this.imageHeight - PANEL_TOP,
        PANEL_OUTLINE_COLOR
    );

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    if (this.filterField != null
        && this.filterField.isFocused()
        && this.minecraft.options.keyInventory.matches(event)
    ) {
      return true;
    }

    if (event.key() == 257 || event.key() == 335) {
      applyFilter();
      return true;
    }

    return super.keyPressed(event);
  }

  @Override
  protected void extractLabels(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    super.extractLabels(graphics, mouseX, mouseY);
    graphics.text(
        this.font,
        Component.translatable("gui.magnatour.item_hub.filter"),
        8,
        138,
        TEXT_COLOR,
        false
    );

    if (this.errorText.isEmpty()) {
      graphics.text(
          this.font,
          Component.translatable("gui.magnatour.item_hub.empty"),
          8,
          168,
          HINT_COLOR,
          false
      );
    } else {
      graphics.text(this.font, this.errorText, 8, 168, ERROR_COLOR, false);
    }
  }

  @Override
  protected void init() {
    super.init();
    this.inventoryLabelY = 39;

    this.filterField = new EditBox(
        this.font,
        this.leftPos + 8,
        this.topPos + 147,
        110,
        18,
        Component.translatable("gui.magnatour.item_hub.filter")
    );
    this.filterField.setMaxLength(128);
    this.filterField.setValue(this.filterItemId);
    this.addRenderableWidget(this.filterField);

    this.addRenderableWidget(Button.builder(
            Component.translatable("gui.magnatour.item_hub.apply"),
            _ -> applyFilter()
        )
        .bounds(this.leftPos + 122, this.topPos + 147, 46, 20)
        .build());
  }

  private void applyFilter() {
    String normalizedFilterItemId = ItemHubEntity.normalizeFilterItemId(
        this.filterField.getValue()
    );
    if (normalizedFilterItemId == null) {
      this.errorText = Component.translatable(
          "gui.magnatour.item_hub.invalid_item"
      ).getString();
      return;
    }

    this.errorText = "";
    this.filterItemId = normalizedFilterItemId;
    this.filterField.setValue(normalizedFilterItemId);
    if (!normalizedFilterItemId.equals(this.menu.getFilterItemId())) {
      this.menu.setFilterItemId(normalizedFilterItemId);
      ClientPlayNetworking.send(new ItemHubPayload(
          this.menu.getBlockPos(),
          this.menu.getDimension(),
          normalizedFilterItemId
      ));
    }
  }

}
