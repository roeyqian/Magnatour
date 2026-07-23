/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.item;

// Java Standard
import java.util.List;
import java.util.ArrayList;

// Fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.level.network.UniverseConsoleBoundBlockPayload;
import roeyqian.magnatour.item.universe.UniverseConsole;
import roeyqian.magnatour.menu.universe.UniverseConsoleMenu;

public class UniverseConsoleScreen extends AbstractContainerScreen<UniverseConsoleMenu> {

  private static final int BACKGROUND_HEIGHT = 187;
  private static final int BACKGROUND_WIDTH = 252;
  private static final int BUTTON_HEIGHT = 20;
  private static final int BUTTON_MARGIN_LEFT = 9;
  private static final int BUTTON_MARGIN_TOP = 18;
  private static final int BUTTON_WIDTH = 189;
  private static final int DELETE_BUTTON_HEIGHT = 20;
  private static final int DELETE_BUTTON_WIDTH = 27;
  private static final int SCROLLBAR_HEIGHT = 160;
  private static final int SCROLLBAR_TOP = 18;
  private static final int SCROLLBAR_X = 231;
  private static final int SCROLLER_HEIGHT = 15;
  private static final int SCROLLER_WIDTH = 12;
  private static final int VISIBLE_BUTTONS = 8;

  private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/window.png"
  );
  private static final Identifier BUTTON_DELETE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/button_delete.png"
  );
  private static final Identifier BUTTON_DELETE_HIGHLIGHTED = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/button_delete_highlighted.png"
  );
  private static final Identifier BUTTON_HIGHLIGHTED = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/button_highlighted.png"
  );
  private static final Identifier BUTTON_NORMAL = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/button.png"
  );
  private static final Identifier SCROLLER = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/scroller.png"
  );
  private static final Identifier SCROLLER_DISABLED = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/gui/console/scroller_disabled.png"
  );

  private int scrollOffset = 0;

  private float scrollPosition = 0.0f;

  private boolean isScrolling = false;

  private List<UniverseConsole.BoundBlocks> boundBlocks;

  public UniverseConsoleScreen(
      UniverseConsoleMenu handler,
      Inventory inventory,
      Component title
  ) {
    super(handler, inventory, title, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
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
        RenderPipelines.GUI_TEXTURED, BACKGROUND,
        x, y, 0.0F, 0.0F,
        BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
        BACKGROUND_WIDTH, BACKGROUND_HEIGHT
    );

    int baseX = x + SCROLLBAR_X;
    int baseY = y + SCROLLBAR_TOP;

    boolean enableScrollbar = boundBlocks != null && boundBlocks.size() > VISIBLE_BUTTONS;

    int scrollbarY;
    if (enableScrollbar) {
      int maxScroll = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT;
      scrollbarY = baseY + (int) (scrollPosition * maxScroll);
    } else {
      scrollbarY = baseY;
    }

    Identifier scrollerTexture = enableScrollbar ? SCROLLER : SCROLLER_DISABLED;
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        scrollerTexture,
        baseX, scrollbarY, 0, 0,
        SCROLLER_WIDTH, SCROLLER_HEIGHT,
        SCROLLER_WIDTH, SCROLLER_HEIGHT
    );

    drawButtons(graphics, mouseX, mouseY);

    super.extractContents(graphics, mouseX, mouseY, delta);
  }

  @Override
  public void extractRenderState(
      @NonNull GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    super.extractRenderState(graphics, mouseX, mouseY, delta);
    drawTooltips(graphics, mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(
      MouseButtonEvent event,
      boolean doubled
  ) {
    double mouseX = event.x();
    double mouseY = event.y();
    int button = event.button();

    if (button == 0) {
      if (mouseOverScrollbar(mouseX, mouseY) && canScroll()) {
        this.isScrolling = true;
        updateScroll(mouseY);
        return true;
      }

      int[] clickInfo = getClickedButton(mouseX, mouseY);
      if (clickInfo[0] != 0) {
        this.minecraft.getSoundManager()
            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));

        if (clickInfo[0] == 2) removeBoundBlock(boundBlocks.get(clickInfo[1]), clickInfo[1]);
        else if (clickInfo[0] == 1) openBoundBlock(boundBlocks.get(clickInfo[1]));
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
    if (canScroll()) {
      int maxOffset = (boundBlocks == null) ? 0 : Math.max(0, boundBlocks.size() - VISIBLE_BUTTONS);

      this.scrollOffset = Mth.clamp(
          this.scrollOffset - (int) Math.signum(verticalAmount),
          0, maxOffset
      );

      if (maxOffset > 0) this.scrollPosition = (float) this.scrollOffset / (float) maxOffset;
      else this.scrollPosition = 0.0f;

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

    if (menu.getBoundBlocks() == null) this.boundBlocks = new ArrayList<>();
    else this.boundBlocks = new ArrayList<>(menu.getBoundBlocks().blocks());

    this.scrollPosition = 0.0f;
    this.scrollOffset = 0;
  }

  private void drawButtons(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    if (boundBlocks == null || boundBlocks.isEmpty()) {
      Component emptyText = Component.translatable("gui.magnatour.universe_console.no_bind");
      int textX = (this.width / 2) - (this.font.width(emptyText) / 2);
      int textY = (this.height / 2) - 4;
      graphics.text(this.font, emptyText, textX, textY, 0xFFFFFFFF, true);
      return;
    }

    int baseX = (this.width - BACKGROUND_WIDTH) / 2 + BUTTON_MARGIN_LEFT;
    int baseY = (this.height - BACKGROUND_HEIGHT) / 2 + BUTTON_MARGIN_TOP;

    int visibleCount = Math.min(VISIBLE_BUTTONS, boundBlocks.size() - scrollOffset);
    for (int i = 0; i < visibleCount; i++) {
      int index = scrollOffset + i;
      UniverseConsole.BoundBlocks block = boundBlocks.get(index);

      int buttonY = baseY + i * BUTTON_HEIGHT;
      int deleteX = baseX + BUTTON_WIDTH;

      drawMainButtons(graphics, mouseX, mouseY, baseX, buttonY, block);
      drawDeleteButtons(graphics, mouseX, mouseY, deleteX, buttonY);
    }
  }

  private void drawTooltips(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY
  ) {
    if (boundBlocks == null || boundBlocks.isEmpty()) return;

    int baseX = (this.width - BACKGROUND_WIDTH) / 2 + BUTTON_MARGIN_LEFT;
    int baseY = (this.height - BACKGROUND_HEIGHT) / 2 + BUTTON_MARGIN_TOP;

    for (int i = 0; i < Math.min(VISIBLE_BUTTONS, boundBlocks.size() - scrollOffset); i++) {
      int index = scrollOffset + i;
      if (index >= boundBlocks.size()) break;

      UniverseConsole.BoundBlocks block = boundBlocks.get(index);
      int buttonY = baseY + i * BUTTON_HEIGHT;
      int deleteX = baseX + BUTTON_WIDTH;

      if (mouseOverMain(mouseX, mouseY, baseX, buttonY)) {
        List<Component> tooltip = List.of(
            Component.literal(block.displayName()),
            Component.literal("§o" + block.pos().toShortString()),
            Component.translatable(
                "dimension." + block.dimension().identifier()
            )
        );
        graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        break;
      }

      if (mouseOverDelete(mouseX, mouseY, deleteX, buttonY)) {
        List<Component> tooltip = List.of(
            Component.translatable("gui.magnatour.universe_console.remove_btn")
        );
        graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        break;
      }
    }
  }

  private boolean mouseOverScrollbar(
      double mouseX,
      double mouseY
  ) {
    int baseX = (this.width - BACKGROUND_WIDTH) / 2 + SCROLLBAR_X;
    int baseY = (this.height - BACKGROUND_HEIGHT) / 2 + SCROLLBAR_TOP;

    return mouseX >= baseX && mouseX < baseX + SCROLLER_WIDTH &&
        mouseY >= baseY && mouseY < baseY + SCROLLBAR_HEIGHT;
  }

  private boolean canScroll() {
    return boundBlocks != null && boundBlocks.size() > VISIBLE_BUTTONS;
  }

  private void updateScroll(
      double mouseY
  ) {
    int baseY = (this.height - BACKGROUND_HEIGHT) / 2 + SCROLLBAR_TOP;
    int scrollableHeight = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT;

    float newPosition = ((float) mouseY - baseY - SCROLLER_HEIGHT / 2.0f) / scrollableHeight;
    this.scrollPosition = Mth.clamp(newPosition, 0.0f, 1.0f);

    int maxOffset = (boundBlocks == null) ? 0 : Math.max(0, boundBlocks.size() - VISIBLE_BUTTONS);
    this.scrollOffset = Math.round(this.scrollPosition * maxOffset);
  }

  private int[] getClickedButton(
      double mouseX,
      double mouseY
  ) {
    if (boundBlocks == null || boundBlocks.isEmpty()) {
      return new int[]{0, -1};
    }

    int baseX = (this.width - BACKGROUND_WIDTH) / 2 + BUTTON_MARGIN_LEFT;
    int baseY = (this.height - BACKGROUND_HEIGHT) / 2 + BUTTON_MARGIN_TOP;
    int visibleCount = Math.min(VISIBLE_BUTTONS, boundBlocks.size() - scrollOffset);

    for (int i = 0; i < visibleCount; i++) {
      int index = scrollOffset + i;
      if (index >= boundBlocks.size()) break;

      int buttonY = baseY + i * BUTTON_HEIGHT;
      int deleteX = baseX + BUTTON_WIDTH;

      if (mouseOverDelete((int) mouseX, (int) mouseY, deleteX, buttonY)) return new int[]{2, index};
      if (mouseOverMain((int) mouseX, (int) mouseY, baseX, buttonY)) return new int[]{1, index};
    }
    return new int[]{0, -1};
  }

  private void removeBoundBlock(
      UniverseConsole.BoundBlocks block,
      int index
  ) {
    ClientPlayNetworking.send(new UniverseConsoleBoundBlockPayload(
        UniverseConsoleBoundBlockPayload.Action.REMOVE, block.pos(), block.dimension()
    ));

    boundBlocks.remove(index);

    if (scrollOffset > 0 && scrollOffset >= boundBlocks.size()) {
      scrollOffset = Math.max(0, boundBlocks.size() - VISIBLE_BUTTONS);
    }

    int maxOffset = (boundBlocks == null) ? 0 : Math.max(0, boundBlocks.size() - VISIBLE_BUTTONS);
    if (maxOffset > 0) scrollPosition = (float) scrollOffset / (float) maxOffset;
    else scrollPosition = 0.0f;
  }

  private void openBoundBlock(
      UniverseConsole.BoundBlocks block
  ) {
    ClientPlayNetworking.send(new UniverseConsoleBoundBlockPayload(
        UniverseConsoleBoundBlockPayload.Action.OPEN, block.pos(), block.dimension()
    ));
  }

  private void drawMainButtons(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      int baseX,
      int buttonY,
      UniverseConsole.BoundBlocks block
  ) {
    Identifier mainTexture =
        mouseOverMain(mouseX, mouseY, baseX, buttonY) ? BUTTON_HIGHLIGHTED : BUTTON_NORMAL;
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, mainTexture,
        baseX, buttonY, 0, 0,
        BUTTON_WIDTH, BUTTON_HEIGHT,
        BUTTON_WIDTH, BUTTON_HEIGHT
    );

    String blockName = block.displayName();
    String blockPos = " [" + block.pos().toShortString() + "]";
    String fullText = blockName + blockPos;

    int maxTextWidth = BUTTON_WIDTH - 10;
    if (this.font.width(fullText) > maxTextWidth) {
      while (this.font.width(blockName + "..." + blockPos) > maxTextWidth) {
        if (blockName.length() > 1) {
          blockName = blockName.substring(0, blockName.length() - 1);
        }
      }
      fullText = blockName + "..." + blockPos;
    }

    int textX = baseX + (BUTTON_WIDTH - this.font.width(fullText)) / 2;
    int textY = buttonY + (BUTTON_HEIGHT - this.font.lineHeight) / 2;

    graphics.text(this.font, Component.literal(fullText), textX, textY, 0xFFFFFFFF, true);
  }

  private void drawDeleteButtons(
      GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      int deleteX,
      int buttonY
  ) {
    Identifier deleteTexture =
        mouseOverDelete(mouseX, mouseY, deleteX, buttonY) ? BUTTON_DELETE_HIGHLIGHTED : BUTTON_DELETE;
    graphics.blit(
        RenderPipelines.GUI_TEXTURED, deleteTexture,
        deleteX, buttonY, 0, 0,
        DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT,
        DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT
    );
  }

  private boolean mouseOverMain(
      int mouseX,
      int mouseY,
      int buttonX,
      int buttonY
  ) {
    return mouseX >= buttonX && mouseX < buttonX + BUTTON_WIDTH &&
        mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;
  }

  private boolean mouseOverDelete(
      int mouseX,
      int mouseY,
      int deleteX,
      int buttonY
  ) {
    return mouseX >= deleteX && mouseX < deleteX + DELETE_BUTTON_WIDTH &&
        mouseY >= buttonY && mouseY < buttonY + DELETE_BUTTON_HEIGHT;
  }

}
