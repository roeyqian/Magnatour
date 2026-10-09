/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.screen;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Magnatour
import roeyqian.magnatour.mixinhelper.screen.ScreenHelperForRecipe;
import roeyqian.magnatour.screen.universe.UniverseCraftingBookComponent;

@Mixin(value = RecipeBookComponent.class, priority = 3600000)
public class RecipeBookComponentMixin {

  @Shadow
  protected Minecraft minecraft;

  @Shadow
  protected CycleButton<Boolean> filterButton;

  @Shadow
  private boolean visible;

  @Shadow
  private int height;
  @Shadow
  private int width;
  @Shadow
  private int xOffset;

  @Shadow
  private float time;

  @Shadow @Final
  private List<RecipeBookTabButton> tabButtons;

  @Shadow
  private EditBox searchBox;

  @Shadow @Final
  private RecipeBookPage recipeBookPage;

  /* Supreme & Universe Crafting Recipe: Custom Recipe Book Render State
   */
  @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
  private void inExtractRenderState(
      GuiGraphicsExtractor context,
      int mouseX, int mouseY,
      float delta,
      CallbackInfo ci
  ) {
    if ((Object) this instanceof UniverseCraftingBookComponent && this.visible && !this.minecraft.hasControlDown()) {
      this.time += delta;
    }

    ScreenHelperForRecipe.handleExtractRenderState(
        (RecipeBookComponent<?>) (Object) this,
        this.visible,
        this.xOffset,
        this.searchBox,
        this.filterButton,
        this.recipeBookPage,
        this.tabButtons,
        this.width, this.height,
        context,
        mouseX, mouseY,
        delta, ci
    );
  }

  @ModifyConstant(method = {"getYOrigin", "updateTabs"}, constant = @Constant(intValue = 166))
  private int magnatour$alignUniverseRecipeBook(
      int original
  ) {
    return (Object) this instanceof UniverseCraftingBookComponent
        ? original + 2 * UniverseCraftingBookComponent.VERTICAL_OFFSET : original;
  }

}
