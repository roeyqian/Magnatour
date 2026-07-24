/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
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
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.registry.content.UniverseBlocks;
import roeyqian.magnatour.registry.logic.CustomRecipes;

public class UniverseCraftingRecipe implements CraftingRecipe {

  public static final MapCodec<UniverseCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
          Codec.STRING
              .optionalFieldOf("group", "")
              .forGetter(recipe -> recipe.recipeGroup),

          CraftingBookCategory.CODEC
              .fieldOf("category")
              .orElse(CraftingBookCategory.MISC)
              .forGetter(recipe -> recipe.recipeCategory),

          ShapedRecipePattern.MAP_CODEC
              .forGetter(recipe -> recipe.rawContents),

          ItemStackTemplate.CODEC
              .fieldOf("result")
              .forGetter(recipe -> recipe.resultStack),

          Codec.BOOL
              .optionalFieldOf("show_notification", true)
              .forGetter(recipe -> recipe.notification)
      ).apply(instance, UniverseCraftingRecipe::new)
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseCraftingRecipe> PACKET_CODEC =
      StreamCodec.of(UniverseCraftingRecipe::write, UniverseCraftingRecipe::read);

  public static final RecipeSerializer<UniverseCraftingRecipe> SERIALIZER =
      new RecipeSerializer<>(CODEC, PACKET_CODEC);

  final boolean notification;

  final String recipeGroup;

  final ItemStackTemplate resultStack;

  final ShapedRecipePattern rawContents;

  final CraftingBookCategory recipeCategory;

  public UniverseCraftingRecipe(
      String group,
      CraftingBookCategory category,
      ShapedRecipePattern raw,
      ItemStackTemplate result,
      boolean showNotification
  ) {
    this.recipeGroup = group;
    this.recipeCategory = category;
    this.rawContents = raw;
    this.resultStack = result;
    this.notification = showNotification;
  }

  @Override @NonNull
  public ItemStack assemble(
      CraftingInput input
  ) {
    return CraftingResultHelper.createResultStack(this.resultStack, input);
  }

  @Override @NonNull
  public CraftingBookCategory category() {
    return this.recipeCategory;
  }

  @Override @NonNull
  public List<RecipeDisplay> display() {
    List<SlotDisplay> ingredientDisplays = this.rawContents.ingredients().stream()
        .map((ingredient) -> ingredient
            .map(Ingredient::display)
            .orElse(SlotDisplay.Empty.INSTANCE)
        )
        .toList();

    SlotDisplay resultDisplay = new SlotDisplay.ItemStackSlotDisplay(this.resultStack);
    SlotDisplay craftingStationDisplay = new SlotDisplay.ItemSlotDisplay(
        UniverseBlocks.UNIVERSE_WORKSTATION.asItem()
    );

    return List.of(
        new ShapedCraftingRecipeDisplay(
            getWidth(),
            getHeight(),
            ingredientDisplays,
            resultDisplay,
            craftingStationDisplay
        )
    );
  }

  public int getBaseResultCount() {
    return this.resultStack.count();
  }

  public int getHeight() {
    return this.rawContents.height();
  }

  @Override @NonNull
  public RecipeSerializer<UniverseCraftingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override @NonNull
  public RecipeType<CraftingRecipe> getType() {
    return CustomRecipes.UNIVERSE_CRAFTING_TYPE;
  }

  public int getWidth() {
    return this.rawContents.width();
  }

  @Override @NonNull
  public String group() {
    return "";
  }

  @Override
  public boolean matches(
      CraftingInput input,
      @NonNull Level world
  ) {
    return this.rawContents.matches(input);
  }

  @Override @NonNull
  public PlacementInfo placementInfo() {
    return PlacementInfo.createFromOptionals(this.rawContents.ingredients());
  }

  @Override @NonNull
  public RecipeBookCategory recipeBookCategory() {
    return CustomRecipes.UNIVERSE_CRAFTING;
  }

  @Override
  public boolean showNotification() {
    return this.notification;
  }

  private static UniverseCraftingRecipe read(
      RegistryFriendlyByteBuf buf
  ) {
    String group = buf.readUtf();
    CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
    ShapedRecipePattern raw = ShapedRecipePattern.STREAM_CODEC.decode(buf);
    ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
    boolean showNotification = buf.readBoolean();

    return new UniverseCraftingRecipe(group, category, raw, result, showNotification);
  }

  private static void write(
      RegistryFriendlyByteBuf buf,
      UniverseCraftingRecipe recipe
  ) {
    buf.writeUtf(recipe.recipeGroup);
    buf.writeEnum(recipe.recipeCategory);
    ShapedRecipePattern.STREAM_CODEC.encode(buf, recipe.rawContents);

    ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.resultStack);
    buf.writeBoolean(recipe.notification);
  }

}
