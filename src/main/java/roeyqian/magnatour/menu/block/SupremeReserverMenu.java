/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.block;

// Java Standard
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.mixin.world.IngredientAccessor;
import roeyqian.magnatour.mixin.world.ShapelessRecipeAccessor;
import roeyqian.magnatour.utility.registry.block.RegActiveBlocks;
import roeyqian.magnatour.utility.registry.menu.RegBlockMenus;

public class SupremeReserverMenu extends AbstractContainerMenu {

  public static final int HOTBAR_END = 46;
  public static final int HOTBAR_START = 37;
  public static final int INPUT_SLOT = 0;
  public static final int INVENTORY_START = 10;
  public static final int OUTPUT_END = 10;
  public static final int OUTPUT_START = 1;

  private final SimpleContainer inputInventory;
  private final SimpleContainer outputInventory;

  private final Player player;

  private final ContainerData propertyDelegate;

  private final ContainerLevelAccess context;

  private final List<RecipeHolder<CraftingRecipe>> matchingRecipes = new ArrayList<>();

  public SupremeReserverMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(
        syncId,
        playerInventory,
        ContainerLevelAccess.NULL,
        new SimpleContainerData(2)
    );
  }

  public SupremeReserverMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context
  ) {
    this(
        syncId,
        playerInventory,
        context,
        new SimpleContainerData(2)
    );
  }

  public SupremeReserverMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context,
      ContainerData propertyDelegate
  ) {
    super(RegBlockMenus.SUPREME_RESERVER_HANDLER, syncId);
    this.context = context;
    this.player = playerInventory.player;
    this.propertyDelegate = propertyDelegate;

    this.addDataSlots(propertyDelegate);

    this.inputInventory = new SimpleContainer(1) {
      @Override
      public void setChanged() {
        super.setChanged();
        SupremeReserverMenu.this.onInputChanged();
      }
    };

    this.outputInventory = new SimpleContainer(9);

    this.addSlot(new Slot(inputInventory, 0, 26, 35));

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        this.addSlot(
            new DisplaySlot(
                outputInventory,
                col + row * 3,
                98 + col * 18,
                17 + row * 18
            )
        );
      }
    }

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(
            new Slot(
                playerInventory,
                col + row * 9 + 9,
                8 + col * 18,
                84 + row * 18
            )
        );
      }
    }

    for (int col = 0; col < 9; col++) {
      this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
    }
  }

  @Override
  public boolean clickMenuButton(
      @NonNull Player player,
      int id
  ) {
    switch (id) {
      case 0 -> {
        previousRecipe();
        return true;
      }
      case 1 -> {
        nextRecipe();
        return true;
      }
      case 2 -> {
        return performReverse();
      }
    }
    return super.clickMenuButton(player, id);
  }

  @Override
  public void clicked(
      int slotIndex,
      int buttonNum,
      @NonNull ContainerInput input,
      @NonNull Player player
  ) {
    if (slotIndex >= OUTPUT_START && slotIndex < OUTPUT_END) {
      if (!player.level().isClientSide()) {
        performReverse();
      }
      return;
    }

    super.clicked(slotIndex, buttonNum, input, player);
  }

  public int getCurrentRecipeIndex() {
    return this.propertyDelegate.get(1);
  }

  public int getRecipeCount() {
    return this.propertyDelegate.get(0);
  }

  public boolean hasMultipleRecipes() {
    return getRecipeCount() > 1;
  }

  public void nextRecipe() {
    int count = getRecipeCount();
    if (count > 1) {
      int newIndex = (getCurrentRecipeIndex() + 1) % count;
      setCurrentRecipeIndex(newIndex);
      context.execute((world, _) -> {
        if (world instanceof ServerLevel) {
          updateOutputSlots();
          broadcastChanges();
        }
      });
    }
  }

  public boolean performReverse() {
    Level world = player.level();
    if (world.isClientSide()) return false;

    int index = getCurrentRecipeIndex();
    if (matchingRecipes.isEmpty()
        || index >= matchingRecipes.size()
        || inputInventory.getItem(0).isEmpty()
    ) {
      return false;
    }

    RecipeHolder<CraftingRecipe> recipeEntry = matchingRecipes.get(index);
    CraftingRecipe recipe = recipeEntry.value();

    ItemStack result = getRecipeResult(recipe);
    ItemStack inputStack = inputInventory.getItem(0);

    if (result.isEmpty() || inputStack.getCount() < result.getCount()) return false;

    List<ItemStack> outputs = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      ItemStack outputStack = outputInventory.getItem(i);
      if (!outputStack.isEmpty()) outputs.add(outputStack.copy());
    }

    inputStack.shrink(result.getCount());
    if (inputStack.isEmpty()) inputInventory.setItem(0, ItemStack.EMPTY);
    else inputInventory.setItem(0, inputStack);

    for (ItemStack output : outputs) {
      if (!player.getInventory().add(output.copy())) player.drop(output, false);
    }

    broadcastChanges();
    return true;
  }

  public void previousRecipe() {
    int count = getRecipeCount();
    if (count > 1) {
      int newIndex = (getCurrentRecipeIndex() - 1 + count) % count;
      setCurrentRecipeIndex(newIndex);
      context.execute((world, _) -> {
        if (world instanceof ServerLevel) {
          updateOutputSlots();
          broadcastChanges();
        }
      });
    }
  }

  @Override @NonNull
  public ItemStack quickMoveStack(
      @NonNull Player player,
      int slotIndex
  ) {
    ItemStack itemStack = ItemStack.EMPTY;
    Slot slot = this.slots.get(slotIndex);

    if (slot.hasItem()) {
      ItemStack slotStack = slot.getItem();
      itemStack = slotStack.copy();

      if (slotIndex >= OUTPUT_START && slotIndex < OUTPUT_END) {
        return ItemStack.EMPTY;
      } else if (slotIndex >= INVENTORY_START && slotIndex < HOTBAR_END) {
        if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
          if (slotIndex < HOTBAR_START) {
            if (!this.moveItemStackTo(slotStack, HOTBAR_START, HOTBAR_END, false)) {
              return ItemStack.EMPTY;
            }
          } else {
            if (!this.moveItemStackTo(slotStack, INVENTORY_START, HOTBAR_START, false)) {
              return ItemStack.EMPTY;
            }
          }
        }
      } else if (slotIndex == INPUT_SLOT) {
        if (!this.moveItemStackTo(slotStack, INVENTORY_START, HOTBAR_END, true)) {
          return ItemStack.EMPTY;
        }
      }

      if (slotStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
      else slot.setChanged();
    }

    return itemStack;
  }

  @Override
  public void removed(
      @NonNull Player player
  ) {
    super.removed(player);
    this.context.execute((_, _) -> this.clearContainer(player, inputInventory));
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return stillValid(this.context, player, RegActiveBlocks.SUPREME_RESERVER);
  }

  private void setCurrentRecipeIndex(
      int index
  ) {
    this.propertyDelegate.set(1, index);
  }

  private void updateOutputSlots() {
    clearOutputSlots();

    int variantIndex = getCurrentRecipeIndex();
    if (matchingRecipes.isEmpty() || variantIndex >= matchingRecipes.size()) {
      return;
    }

    RecipeHolder<CraftingRecipe> recipeEntry = matchingRecipes.get(variantIndex);
    CraftingRecipe recipe = recipeEntry.value();

    if (recipe instanceof ShapedRecipe shapedRecipe) {
      int width = shapedRecipe.getWidth();
      int height = shapedRecipe.getHeight();
      List<Optional<Ingredient>> ingredients = shapedRecipe.getIngredients();

      for (int row = 0; row < height; row++) {
        for (int col = 0; col < width; col++) {
          int ingredientIndex = col + row * width;
          int slotIndex = col + row * 3;

          if (ingredientIndex < ingredients.size()) {
            Optional<Ingredient> ingredientOpt = ingredients.get(ingredientIndex);
            ingredientOpt.ifPresent((ingredient) -> setSlotFromIngredient(
                slotIndex, ingredient, variantIndex)
            );
          }
        }
      }
    } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
      List<Ingredient> ingredients = ((ShapelessRecipeAccessor) shapelessRecipe).getIngredients();

      for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
        Ingredient ingredient = ingredients.get(i);
        setSlotFromIngredient(i, ingredient, variantIndex);
      }
    }
  }

  private ItemStack getRecipeResult(
      CraftingRecipe recipe
  ) {
    try {
      CraftingInput emptyInput = CraftingInput.of(1, 1, List.of(ItemStack.EMPTY));
      return recipe.assemble(emptyInput);
    } catch (Exception e) {
      return ItemStack.EMPTY;
    }
  }

  private void clearOutputSlots() {
    for (int i = 0; i < 9; i++) {
      outputInventory.setItem(i, ItemStack.EMPTY);
    }
  }

  private void setSlotFromIngredient(
      int slotIndex,
      Ingredient ingredient,
      int variantIndex
  ) {
    if (ingredient == null || ingredient.isEmpty()) {
      return;
    }

    List<Holder<Item>> matchingItems =
        ((IngredientAccessor) (Object) ingredient).getEntries().stream().toList();

    if (!matchingItems.isEmpty()) {
      int itemIndex = variantIndex % matchingItems.size();
      Holder<Item> selectedItem = matchingItems.get(itemIndex);
      ItemStack stack = new ItemStack(selectedItem.value());
      outputInventory.setItem(slotIndex, stack);
    }
  }

  private List<Ingredient> getIngredientsList(
      CraftingRecipe recipe
  ) {
    List<Ingredient> result = new ArrayList<>();

    if (recipe instanceof ShapedRecipe shapedRecipe) {
      List<Optional<Ingredient>> ingredients = shapedRecipe.getIngredients();
      for (Optional<Ingredient> opt : ingredients) {
        opt.ifPresent(result::add);
      }
    } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
      result.addAll(
          ((ShapelessRecipeAccessor) shapelessRecipe).getIngredients()
      );
    }

    return result;
  }

  private int getMaxIngredientVariants(
      List<Ingredient> ingredients
  ) {
    int max = 1;
    for (Ingredient ingredient : ingredients) {
      if (ingredient != null && !ingredient.isEmpty()) {
        int count = (int) ((IngredientAccessor) (Object) ingredient)
            .getEntries()
            .stream()
            .count();
        if (count > max) {
          max = count;
        }
      }
    }
    return max;
  }

  private void setRecipeCount(
      int count
  ) {
    this.propertyDelegate.set(0, count);
  }

  private void findMatchingRecipes(
      ServerLevel world,
      ItemStack inputStack
  ) {
    RecipeManager recipeManager = world.recipeAccess();

    for (RecipeHolder<?> entry : recipeManager.getRecipes()) {
      if (entry.value() instanceof CraftingRecipe craftingRecipe) {
        ItemStack result = getRecipeResult(craftingRecipe);

        if (!result.isEmpty() && ItemStack.isSameItem(result, inputStack)) {
          @SuppressWarnings("unchecked")
          RecipeHolder<CraftingRecipe> craftingEntry =
              (RecipeHolder<CraftingRecipe>) entry;

          List<Ingredient> ingredients = getIngredientsList(craftingRecipe);
          int maxVariants = getMaxIngredientVariants(ingredients);

          if (maxVariants > 1) {
            int variantsToAdd = Math.min(maxVariants, 16);
            for (int i = 0; i < variantsToAdd; i++) {
              matchingRecipes.add(craftingEntry);
            }
          } else {
            matchingRecipes.add(craftingEntry);
          }
        }
      }
    }
  }

  private void onInputChanged() {
    ItemStack inputStack = inputInventory.getItem(0);
    matchingRecipes.clear();
    setCurrentRecipeIndex(0);
    setRecipeCount(0);

    if (!inputStack.isEmpty()) {
      context.execute((world, _) -> {
        if (world instanceof ServerLevel serverWorld) {
          findMatchingRecipes(serverWorld, inputStack);

          setRecipeCount(matchingRecipes.size());
          setCurrentRecipeIndex(0);
          updateOutputSlots();
          broadcastChanges();
        }
      });
    } else {
      clearOutputSlots();
      broadcastChanges();
    }
  }

private static class DisplaySlot extends Slot {

    public DisplaySlot(
        Container inventory,
        int index,
        int x,
        int y
    ) {
      super(inventory, index, x, y);
    }

    @Override
    public int getMaxStackSize() {
      return 0;
    }

    @Override
    public boolean mayPickup(
        @NonNull Player player
    ) {
      return false;
    }

    @Override
    public boolean mayPlace(
        @NonNull ItemStack stack
    ) {
      return false;
    }

  }

}
