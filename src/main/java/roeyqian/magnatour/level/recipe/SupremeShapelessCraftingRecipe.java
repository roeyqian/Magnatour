/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.recipe;

// Java Standard
import java.util.List;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.logic.CustomRecipes;

public class SupremeShapelessCraftingRecipe implements CraftingRecipe {

  public static final MapCodec<SupremeShapelessCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
          Codec.STRING
              .optionalFieldOf("group", "")
              .forGetter(recipe -> recipe.recipeGroup),

          CraftingBookCategory.CODEC
              .fieldOf("category")
              .orElse(CraftingBookCategory.MISC)
              .forGetter(recipe -> recipe.recipeCategory),

          Ingredient.CODEC.listOf(1, 16)
              .fieldOf("ingredients")
              .forGetter(recipe -> recipe.ingredients),

          ItemStackTemplate.CODEC
              .fieldOf("result")
              .forGetter(recipe -> recipe.resultStack),

          Codec.BOOL
              .optionalFieldOf("show_notification", true)
              .forGetter(recipe -> recipe.notification)
      ).apply(instance, SupremeShapelessCraftingRecipe::new)
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, SupremeShapelessCraftingRecipe> PACKET_CODEC =
      StreamCodec.of(SupremeShapelessCraftingRecipe::write, SupremeShapelessCraftingRecipe::read);

  public static final RecipeSerializer<SupremeShapelessCraftingRecipe> SERIALIZER =
      new RecipeSerializer<>(CODEC, PACKET_CODEC);

  final boolean notification;

  final String recipeGroup;

  final ItemStackTemplate resultStack;

  final CraftingBookCategory recipeCategory;

  final List<Ingredient> ingredients;

  public SupremeShapelessCraftingRecipe(
      String group,
      CraftingBookCategory category,
      List<Ingredient> ingredients,
      ItemStackTemplate result,
      boolean showNotification
  ) {
    if (ingredients.isEmpty() || ingredients.size() > 16) {
      throw new IllegalArgumentException("Supreme shapeless recipes require between 1 and 16 ingredients");
    }

    this.recipeGroup = group;
    this.recipeCategory = category;
    this.ingredients = List.copyOf(ingredients);
    this.resultStack = result;
    this.notification = showNotification;
  }

  @Override @NonNull
  public ItemStack assemble(
      @NonNull CraftingInput input
  ) {
    return CraftingResultHelper.createResultStack(this.resultStack, input);
  }

  @Override @NonNull
  public CraftingBookCategory category() {
    return this.recipeCategory;
  }

  @Override @NonNull
  public List<RecipeDisplay> display() {
    List<SlotDisplay> ingredientDisplays = this.ingredients.stream()
        .map(Ingredient::display)
        .toList();

    SlotDisplay resultDisplay = new SlotDisplay.ItemStackSlotDisplay(this.resultStack);
    SlotDisplay craftingStationDisplay = new SlotDisplay.ItemSlotDisplay(
        SupremeBlocks.SUPREME_CRAFTABLE.asItem()
    );

    return List.of(
        new ShapelessCraftingRecipeDisplay(
            ingredientDisplays,
            resultDisplay,
            craftingStationDisplay
        )
    );
  }

  public int getBaseResultCount() {
    return this.resultStack.count();
  }

  @Override @NonNull
  public RecipeSerializer<SupremeShapelessCraftingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override @NonNull
  public RecipeType<CraftingRecipe> getType() {
    return CustomRecipes.SUPREME_CRAFTING_TYPE;
  }

  @Override @NonNull
  public String group() {
    return this.recipeGroup;
  }

  @Override
  public boolean matches(
      @NonNull CraftingInput input,
      @NonNull Level world
  ) {
    return input.ingredientCount() == this.ingredients.size()
        && input.stackedContents().canCraft(this, null);
  }

  @Override @NonNull
  public PlacementInfo placementInfo() {
    return PlacementInfo.create(this.ingredients);
  }

  @Override @NonNull
  public RecipeBookCategory recipeBookCategory() {
    return CustomRecipes.SUPREME_CRAFTING;
  }

  @Override
  public boolean showNotification() {
    return this.notification;
  }

  private static SupremeShapelessCraftingRecipe read(
      RegistryFriendlyByteBuf buf
  ) {
    String group = buf.readUtf();
    CraftingBookCategory category = CraftingBookCategory.STREAM_CODEC.decode(buf);
    List<Ingredient> ingredients = Ingredient.CONTENTS_STREAM_CODEC
        .apply(ByteBufCodecs.list(16))
        .decode(buf);
    ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
    boolean showNotification = buf.readBoolean();

    return new SupremeShapelessCraftingRecipe(group, category, ingredients, result, showNotification);
  }

  private static void write(
      RegistryFriendlyByteBuf buf,
      SupremeShapelessCraftingRecipe recipe
  ) {
    buf.writeUtf(recipe.recipeGroup);
    CraftingBookCategory.STREAM_CODEC.encode(buf, recipe.recipeCategory);
    Ingredient.CONTENTS_STREAM_CODEC
        .apply(ByteBufCodecs.list(16))
        .encode(buf, recipe.ingredients);

    ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.resultStack);
    buf.writeBoolean(recipe.notification);
  }

}
