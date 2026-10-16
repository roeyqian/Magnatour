/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.universe;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// JSpecify
import org.jspecify.annotations.Nullable;

// Lightweight Java Game Library
import org.lwjgl.sdl.SDLMouse;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.screen.universe.UniverseForgeCraftingComponent;
import roeyqian.magnatour.screen.universe.UniverseForgeCraftingScreen;
import roeyqian.magnatour.screen.universe.UniverseForgeSmeltingComponent;
import roeyqian.magnatour.screen.universe.UniverseForgeSmeltingScreen;

public final class UniverseForgeClient {

  private static final long TIMEOUT_NANOS = 5_000_000_000L;

  private static boolean pending;
  private static boolean smelting;

  private static long deadline;

  private static double mouseX;
  private static double mouseY;

  private UniverseForgeClient() {}

  public static void afterSetScreen(
      Minecraft client,
      @Nullable Screen newScreen
  ) {
    if (!isPending() || newScreen == null) return;
    pending = false;
    if (!isTarget(newScreen)) return;
    SDLMouse.SDL_WarpMouseInWindow(client.getWindow().handle(), (float) mouseX, (float) mouseY);
    client.mouseHandler.resyncMousePosition();
  }

  public static void beforeSetScreen(
      Minecraft client,
      @Nullable Screen currentScreen, @Nullable Screen newScreen
  ) {
    if (!isPending()) return;
    // Capture the latest position before the close packet grabs and centers the mouse.
    boolean leavingSource = smelting
        ? currentScreen instanceof UniverseForgeCraftingScreen
        : currentScreen instanceof UniverseForgeSmeltingScreen;
    if (leavingSource && (newScreen == null || isTarget(newScreen))) {
      mouseX = client.mouseHandler.xpos();
      mouseY = client.mouseHandler.ypos();
    }
  }

  public static void begin(
      Minecraft client,
      boolean toSmelting
  ) {
    pending = true;
    smelting = toSmelting;
    deadline = System.nanoTime() + TIMEOUT_NANOS;
    mouseX = client.mouseHandler.xpos();
    mouseY = client.mouseHandler.ypos();
  }

  public static void handleExtractRenderState(
      RecipeBookComponent<?> recipeBookComponent,
      boolean visible,
      int xOffset,
      EditBox searchBox,
      CycleButton<Boolean> filterButton,
      RecipeBookPage recipeBookPage,
      List<RecipeBookTabButton> tabButtons,
      int width, int height,
      GuiGraphicsExtractor context,
      int mouseX, int mouseY,
      float delta,
      CallbackInfo ci
  ) {
    if (!visible) return;
    if (!(recipeBookComponent instanceof UniverseForgeCraftingComponent)
        && !(recipeBookComponent instanceof UniverseForgeSmeltingComponent)) return;

    int textureWidth = 147;
    int textureHeight = 194;
    int posX = (width - textureWidth) / 2 - xOffset;
    int posY = (height - textureHeight) / 2 + 14 - UniverseForgeCraftingComponent.VERTICAL_OFFSET;

    context.blit(
        RenderPipelines.GUI_TEXTURED,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "textures/gui/general/recipe_book.png"),
        posX, posY,
        1.0F, 1.0F,
        textureWidth, textureHeight,
        256, 256
    );

    searchBox.extractRenderState(context, mouseX, mouseY, delta);
    for (RecipeBookTabButton button : tabButtons) {
      button.extractRenderState(context, mouseX, mouseY, delta);
    }
    filterButton.extractRenderState(context, mouseX, mouseY, delta);
    recipeBookPage.extractRenderState(context, posX, posY, mouseX, mouseY, delta);

    ci.cancel();
  }

  private static boolean isPending() {
    if (pending && System.nanoTime() - deadline >= 0) pending = false;
    return pending;
  }

  private static boolean isTarget(
      Screen screen
  ) {
    return smelting
        ? screen instanceof UniverseForgeSmeltingScreen
        : screen instanceof UniverseForgeCraftingScreen;
  }

}
