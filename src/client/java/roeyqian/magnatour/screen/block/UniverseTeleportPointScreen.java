/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.block;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.active.entity.UniverseTeleportPointEntity;
import roeyqian.magnatour.gen.network.UniverseTeleportPointPayload;
import roeyqian.magnatour.menu.block.UniverseTeleportPointMenu;

public class UniverseTeleportPointScreen extends AbstractContainerScreen<UniverseTeleportPointMenu> {

  private static final int BACKGROUND_HEIGHT = 228;
  private static final int BACKGROUND_WIDTH = 268;
  private static final int LIST_BUTTON_TOP = 20;
  private static final int ROW_BUTTON_HEIGHT = 20;
  private static final int ROW_BUTTON_WIDTH = 193;
  private static final int ROW_DELETE_BUTTON_WIDTH = 32;
  private static final int ROW_HEIGHT = 20;
  private static final int ROW_TEXTURE_HEIGHT = 20;
  private static final int ROW_TEXTURE_WIDTH = 189;
  private static final int ROW_X = 10;
  private static final int SCROLLBAR_X = 247;
  private static final int SCROLLER_HEIGHT = 15;
  private static final int SCROLLER_WIDTH = 12;
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final int VISIBLE_ROWS = 8;
  private static final int SCROLLBAR_HEIGHT = VISIBLE_ROWS * ROW_HEIGHT;

  private static final Identifier BUTTON_DELETE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/teleport/button_delete.png"
  );
  private static final Identifier BUTTON_DELETE_HIGHLIGHTED = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/teleport/button_delete_highlighted.png"
  );
  private static final Identifier BUTTON_HIGHLIGHTED = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/teleport/button_highlighted.png"
  );
  private static final Identifier BUTTON_NORMAL = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/teleport/button.png"
  );
  private static final Identifier SCROLLER = Identifier.withDefaultNamespace(
      "container/creative_inventory/scroller"
  );
  private static final Identifier SCROLLER_DISABLED = Identifier.withDefaultNamespace(
      "container/creative_inventory/scroller_disabled"
  );
  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/teleport/window.png"
  );

  private int scrollOffset = 0;

  private float scrollPosition = 0.0f;

  private boolean addMode = false;
  private boolean isScrolling = false;

  private String errorText = "";

  private Button addButton;
  private Button cancelButton;
  private Button currentButton;
  private Button saveButton;

  private EditBox dimensionField;
  private EditBox nameField;
  private EditBox xField;
  private EditBox yField;
  private EditBox zField;

  private final List<UniverseTeleportPointEntity.Destination> destinations = new ArrayList<>();

  public UniverseTeleportPointScreen(
      UniverseTeleportPointMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    this.destinations.addAll(handler.getDestinations());
  }

  @Override
  public void extractContents(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    int x = (this.width - BACKGROUND_WIDTH) / 2;
    int y = (this.height - BACKGROUND_HEIGHT) / 2;

    graphics.blit(
        RenderPipelines.GUI_TEXTURED, TEXTURE,
        x, y, 0.0F, 0.0F,
        BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
        BACKGROUND_WIDTH, BACKGROUND_HEIGHT
    );

    if (addMode) {
      drawAddForm(graphics);
    } else {
      drawScrollbar(graphics);
      drawDestinationList(graphics, mouseX, mouseY);
    }

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    if (hasFocusedTextField() && this.minecraft.options.keyInventory.matches(event)) {
      return true;
    }

    if (addMode && event.key() == 257) {
      saveDestination();
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean mouseClicked(
      @NonNull MouseButtonEvent event,
      boolean doubled
  ) {
    if (!addMode && event.button() == 0) {
      if (mouseOverScrollbar(event.x(), event.y()) && canScroll()) {
        this.isScrolling = true;
        updateScroll(event.y());
        return true;
      }

      int index = getHoveredIndex(event.x(), event.y());
      if (index >= 0) {
        this.minecraft.getSoundManager()
            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        sendAction(UniverseTeleportPointPayload.Action.TELEPORT, index, emptyDestination());
        return true;
      }

      int deleteIndex = getHoveredDeleteIndex(event.x(), event.y());
      if (deleteIndex >= 0) {
        this.minecraft.getSoundManager()
            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        sendAction(UniverseTeleportPointPayload.Action.DELETE, deleteIndex, emptyDestination());
        this.destinations.remove(deleteIndex);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, getMaxScrollOffset());
        syncScrollPosition();
        return true;
      }
    }
    return super.mouseClicked(event, doubled);
  }

  @Override
  public boolean mouseDragged(
      @NonNull MouseButtonEvent event,
      double offsetX,
      double offsetY
  ) {
    if (this.isScrolling && canScroll()) {
      updateScroll(event.y());
      return true;
    }
    return super.mouseDragged(event, offsetX, offsetY);
  }

  @Override
  public boolean mouseReleased(
      MouseButtonEvent event
  ) {
    if (event.button() == 0) {
      this.isScrolling = false;
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
    if (!addMode && canScroll()) {
      this.scrollOffset = Mth.clamp(
          this.scrollOffset - (int) Math.signum(verticalAmount),
          0,
          getMaxScrollOffset()
      );
      syncScrollPosition();
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  protected void extractLabels(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
  }

  @Override
  protected void init() {
    super.init();
    this.inventoryLabelY = 10000;
    this.scrollOffset = 0;
    this.scrollPosition = 0.0f;
    createButtons();
    createTextFields();
    setAddMode(false);
  }

  private void drawAddForm(
      GuiGraphicsExtractor graphics
  ) {
    int labelX = leftPos + 14;
    graphics.text(
        this.font, Component.translatable("gui.magnatour.universe_teleport_point.name"),
        labelX, topPos + 49,
        TEXT_COLOR, true
    );
    graphics.text(
        this.font, Component.translatable("gui.magnatour.universe_teleport_point.dimension"),
        labelX, topPos + 75,
        TEXT_COLOR, true
    );
    graphics.text(
        this.font, Component.literal("XYZ"),
        labelX, topPos + 101,
        TEXT_COLOR, true
    );

    if (!errorText.isEmpty()) {
      graphics.text(
          this.font, Component.literal(errorText),
          leftPos + 24, topPos + 132,
          0xFFFF7A7A, true
      );
    }
  }

  private void drawScrollbar(
      GuiGraphicsExtractor graphics
  ) {
    boolean enableScrollbar = canScroll();
    int thumbY;
    if (enableScrollbar) {
      int thumbRange = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT;
      thumbY = this.topPos + LIST_BUTTON_TOP + (int) (this.scrollPosition * thumbRange);
    } else {
      thumbY = this.topPos + LIST_BUTTON_TOP;
    }

    graphics.blitSprite(
        RenderPipelines.GUI_TEXTURED,
        enableScrollbar ? SCROLLER : SCROLLER_DISABLED,
        this.leftPos + SCROLLBAR_X,
        thumbY,
        SCROLLER_WIDTH,
        SCROLLER_HEIGHT
    );
  }

  private void drawDestinationList(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    if (destinations.isEmpty()) {
      Component empty = Component.translatable("gui.magnatour.universe_teleport_point.empty");
      graphics.centeredText(this.font, empty, leftPos + BACKGROUND_WIDTH / 2, topPos + 92, TEXT_COLOR);
      return;
    }

    int rows = Math.min(VISIBLE_ROWS, destinations.size() - scrollOffset);
    for (int i = 0; i < rows; i++) {
      int index = scrollOffset + i;
      UniverseTeleportPointEntity.Destination destination = destinations.get(index);
      int rowX = leftPos + ROW_X;
      int rowY = topPos + LIST_BUTTON_TOP + i * ROW_HEIGHT;
      int deleteX = rowX + ROW_BUTTON_WIDTH;

      Identifier rowTexture =
          mouseOverMain(mouseX, mouseY, rowX, rowY) ? BUTTON_HIGHLIGHTED : BUTTON_NORMAL;
      Identifier deleteTexture =
          mouseOverDelete(mouseX, mouseY, deleteX, rowY) ? BUTTON_DELETE_HIGHLIGHTED : BUTTON_DELETE;

      String coordinates = destination.x() + ", " + destination.y() + ", " + destination.z();
      int coordinatesX = rowX + ROW_BUTTON_WIDTH - this.font.width(coordinates) - 6;
      String name = truncateToWidth(destination.name(), coordinatesX - rowX - 12);

      graphics.blit(
          RenderPipelines.GUI_TEXTURED, rowTexture,
          rowX, rowY, 0.0F, 0.0F,
          ROW_BUTTON_WIDTH, ROW_BUTTON_HEIGHT,
          ROW_TEXTURE_WIDTH, ROW_TEXTURE_HEIGHT,
          ROW_TEXTURE_WIDTH, ROW_TEXTURE_HEIGHT
      );
      graphics.blit(
          RenderPipelines.GUI_TEXTURED, deleteTexture,
          deleteX, rowY, 0.0F, 0.0F,
          ROW_DELETE_BUTTON_WIDTH, ROW_BUTTON_HEIGHT,
          ROW_DELETE_BUTTON_WIDTH, ROW_BUTTON_HEIGHT
      );
      graphics.text(this.font, name, rowX + 6, rowY + 6, TEXT_COLOR, true);
      graphics.text(this.font, coordinates, coordinatesX, rowY + 6, TEXT_COLOR, true);
    }
  }

  private boolean hasFocusedTextField() {
    return this.nameField != null && this.nameField.isFocused()
        || this.dimensionField != null && this.dimensionField.isFocused()
        || this.xField != null && this.xField.isFocused()
        || this.yField != null && this.yField.isFocused()
        || this.zField != null && this.zField.isFocused();
  }

  private void saveDestination() {
    try {
      String name = nameField.getValue().trim();
      if (name.isEmpty()) {
        name = Component.translatable("gui.magnatour.universe_teleport_point.default_name").getString();
      }

      Identifier dimensionId = Identifier.parse(dimensionField.getValue().trim());
      ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
      UniverseTeleportPointEntity.Destination destination = new UniverseTeleportPointEntity.Destination(
          name,
          dimension,
          Integer.parseInt(xField.getValue().trim()),
          Integer.parseInt(yField.getValue().trim()),
          Integer.parseInt(zField.getValue().trim())
      );

      sendAction(UniverseTeleportPointPayload.Action.ADD, -1, destination);
      setAddMode(false);
      if (this.minecraft.player != null) {
        this.minecraft.player.closeContainer();
      }
    } catch (RuntimeException exception) {
      this.errorText = Component.translatable(
          "gui.magnatour.universe_teleport_point.invalid_input"
      ).getString();
    }
  }

  private boolean mouseOverScrollbar(
      double mouseX,
      double mouseY
  ) {
    int scrollbarX = this.leftPos + SCROLLBAR_X;
    int scrollbarY = this.topPos + LIST_BUTTON_TOP;
    return mouseX >= scrollbarX
        && mouseX < scrollbarX + SCROLLER_WIDTH
        && mouseY >= scrollbarY
        && mouseY < scrollbarY + SCROLLBAR_HEIGHT;
  }

  private boolean canScroll() {
    return this.destinations.size() > VISIBLE_ROWS;
  }

  private void updateScroll(
      double mouseY
  ) {
    int scrollbarY = this.topPos + LIST_BUTTON_TOP;
    int thumbRange = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT;
    float newPosition = ((float) mouseY - scrollbarY - SCROLLER_HEIGHT / 2.0f) / thumbRange;

    this.scrollPosition = Mth.clamp(newPosition, 0.0f, 1.0f);
    this.scrollOffset = Math.round(this.scrollPosition * getMaxScrollOffset());
  }

  private int getHoveredIndex(
      double mouseX,
      double mouseY
  ) {
    int rows = Math.min(VISIBLE_ROWS, destinations.size() - scrollOffset);
    for (int i = 0; i < rows; i++) {
      int index = scrollOffset + i;
      int rowX = leftPos + ROW_X;
      int rowY = topPos + 18 + i * ROW_HEIGHT;
      if (mouseOverMain((int) mouseX, (int) mouseY, rowX, rowY)) {
        return index;
      }
    }
    return -1;
  }

  private void sendAction(
      UniverseTeleportPointPayload.Action action,
      int index,
      UniverseTeleportPointEntity.Destination destination
  ) {
    ClientPlayNetworking.send(new UniverseTeleportPointPayload(
        action,
        this.menu.getBlockPos(),
        index,
        destination
    ));
  }

  private UniverseTeleportPointEntity.Destination emptyDestination() {
    return new UniverseTeleportPointEntity.Destination(
        "",
        Level.OVERWORLD,
        0,
        0,
        0
    );
  }

  private int getHoveredDeleteIndex(
      double mouseX,
      double mouseY
  ) {
    int rows = Math.min(VISIBLE_ROWS, destinations.size() - scrollOffset);
    for (int i = 0; i < rows; i++) {
      int index = scrollOffset + i;
      int rowX = leftPos + ROW_X;
      int rowY = topPos + 18 + i * ROW_HEIGHT;
      int deleteX = rowX + ROW_BUTTON_WIDTH;
      if (mouseOverDelete((int) mouseX, (int) mouseY, deleteX, rowY)) {
        return index;
      }
    }
    return -1;
  }

  private int getMaxScrollOffset() {
    return Math.max(0, this.destinations.size() - VISIBLE_ROWS);
  }

  private void syncScrollPosition() {
    int maxOffset = getMaxScrollOffset();
    this.scrollPosition = maxOffset > 0 ? (float) this.scrollOffset / (float) maxOffset : 0.0f;
  }

  private void createButtons() {
    this.addButton = Button.builder(
            Component.translatable("gui.magnatour.universe_teleport_point.add"),
            _ -> setAddMode(true)
        )
        .bounds(leftPos + 10, topPos + 195, 52, 20)
        .build();
    this.saveButton = Button.builder(
            Component.translatable("gui.magnatour.universe_teleport_point.save"),
            _ -> saveDestination()
        )
        .bounds(leftPos + 10, topPos + 195, 52, 20)
        .build();
    this.cancelButton = Button.builder(
            Component.translatable("gui.magnatour.universe_teleport_point.cancel"),
            _ -> setAddMode(false)
        )
        .bounds(leftPos + 67, topPos + 195, 52, 20)
        .build();
    this.currentButton = Button.builder(
            Component.translatable("gui.magnatour.universe_teleport_point.current"),
            _ -> fillCurrentPosition()
        )
        .bounds(leftPos + 124, topPos + 195, 92, 20)
        .build();

    this.addRenderableWidget(addButton);
    this.addRenderableWidget(currentButton);
    this.addRenderableWidget(saveButton);
    this.addRenderableWidget(cancelButton);
  }

  private void createTextFields() {
    this.nameField = new EditBox(
        this.font,
        leftPos + 46, topPos + 44, 168, 18,
        Component.translatable("gui.magnatour.universe_teleport_point.name")
    );
    this.dimensionField = new EditBox(
        this.font,
        leftPos + 46, topPos + 70, 168, 18,
        Component.translatable("gui.magnatour.universe_teleport_point.dimension")
    );
    this.xField = coordinateField(46, 96, "x");
    this.yField = coordinateField(106, 96, "y");
    this.zField = coordinateField(166, 96, "z");

    nameField.setMaxLength(32);
    dimensionField.setMaxLength(96);
    dimensionField.setValue("minecraft:overworld");

    this.addRenderableWidget(nameField);
    this.addRenderableWidget(dimensionField);
    this.addRenderableWidget(xField);
    this.addRenderableWidget(yField);
    this.addRenderableWidget(zField);
  }

  private void setAddMode(
      boolean addMode
  ) {
    this.addMode = addMode;
    this.errorText = "";

    this.addButton.visible = !addMode;
    this.currentButton.visible = addMode;
    this.saveButton.visible = addMode;
    this.cancelButton.visible = addMode;

    this.nameField.visible = addMode;
    this.dimensionField.visible = addMode;
    this.xField.visible = addMode;
    this.yField.visible = addMode;
    this.zField.visible = addMode;
  }

  private boolean mouseOverMain(
      int mouseX,
      int mouseY,
      int buttonX,
      int buttonY
  ) {
    return mouseX >= buttonX && mouseX < buttonX + ROW_BUTTON_WIDTH
        && mouseY >= buttonY && mouseY < buttonY + ROW_BUTTON_HEIGHT;
  }

  private boolean mouseOverDelete(
      int mouseX,
      int mouseY,
      int buttonX,
      int buttonY
  ) {
    return mouseX >= buttonX && mouseX < buttonX + ROW_DELETE_BUTTON_WIDTH
        && mouseY >= buttonY && mouseY < buttonY + ROW_BUTTON_HEIGHT;
  }

  private String truncateToWidth(
      String value,
      int maxWidth
  ) {
    if (maxWidth <= 0) return "";
    if (this.font.width(value) <= maxWidth) return value;

    int end = value.length();
    while (end > 0 && this.font.width(value.substring(0, end) + "...") > maxWidth) {
      end--;
    }
    return end > 0 ? value.substring(0, end) + "..." : "...";
  }

  private void fillCurrentPosition() {
    if (this.minecraft.player == null || this.minecraft.level == null) return;

    BlockPos playerPos = this.minecraft.player.blockPosition();
    Identifier dimension = this.minecraft.level.dimension().identifier();

    this.dimensionField.setValue(dimension.toString());
    this.xField.setValue(Integer.toString(playerPos.getX()));
    this.yField.setValue(Integer.toString(playerPos.getY()));
    this.zField.setValue(Integer.toString(playerPos.getZ()));
  }

  private EditBox coordinateField(
      int x,
      int y,
      String name
  ) {
    EditBox field = new EditBox(
        this.font,
        leftPos + x,
        topPos + y,
        48,
        18,
        Component.literal(name)
    );
    field.setMaxLength(8);
    return field;
  }

}
