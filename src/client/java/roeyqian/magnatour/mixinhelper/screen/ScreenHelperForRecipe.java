/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.screen;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.screen.recipe.UniverseCraftingBookComponent;

public final class ScreenHelperForRecipe {

  private ScreenHelperForRecipe() {}

  public static void handleExtractRenderState(
      RecipeBookComponent<?> recipeBookComponent,
      boolean visible,
      int xOffset,
      EditBox searchBox,
      CycleButton<Boolean> filterButton,
      RecipeBookPage recipeBookPage,
      List<RecipeBookTabButton> tabButtons,
      int width,
      int height,
      GuiGraphicsExtractor context,
      int mouseX,
      int mouseY,
      float delta,
      CallbackInfo ci
  ) {
    if (!(recipeBookComponent instanceof UniverseCraftingBookComponent) || !visible) return;

    int textureWidth = 147;
    int textureHeight = 194;
    int posX = (width - textureWidth) / 2 - xOffset;
    int posY = (height - textureHeight) / 2 + 14;

    context.blit(
        RenderPipelines.GUI_TEXTURED,
        Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "textures/gui/recipe_book.png"),
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

}
