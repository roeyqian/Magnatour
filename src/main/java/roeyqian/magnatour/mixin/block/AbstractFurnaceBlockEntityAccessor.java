/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.block;

// Minecraft
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractFurnaceBlockEntity.class, priority = 3600000)
public interface AbstractFurnaceBlockEntityAccessor {

  @Accessor("cookingTimer")
  int getCookingTimeSpent();

  @Accessor("cookingTotalTime")
  int getCookingTotalTime();

  @Accessor("litTimeRemaining")
  int getLitTimeRemaining();

  @Accessor("quickCheck")
  RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> getMatchGetter();

  @Accessor("cookingTimer")
  void setCookingTimeSpent(
      int value
  );

  @Accessor("cookingTotalTime")
  void setCookingTotalTime(
      int value
  );

  @Accessor("litTimeRemaining")
  void setLitTimeRemaining(
      int value
  );

  @Accessor("litTotalTime")
  void setLitTotalTime(
      int value
  );

}
