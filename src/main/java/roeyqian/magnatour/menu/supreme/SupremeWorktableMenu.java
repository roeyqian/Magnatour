/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.supreme;

// Java Standard
import java.util.List;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.block.CustomCraftingBlock;
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.registry.logic.CustomRecipes;
import roeyqian.magnatour.registry.content.SupremeMenus;

public class SupremeWorktableMenu extends AbstractCraftingMenu implements CustomCraftingBlock {

  public static final int RESULT_ID = 0;

  private static final int HOTBAR_END = 46;
  private static final int HOTBAR_START = 37;
  private static final int INPUT_END = 10;
  private static final int INPUT_START = 1;
  private static final int INVENTORY_END = 37;
  private static final int INVENTORY_START = 10;

  private boolean filling;

  private final Player player;

  private final ContainerLevelAccess context;

  public SupremeWorktableMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, ContainerLevelAccess.NULL);
  }

  public SupremeWorktableMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context
  ) {
    super(SupremeMenus.SUPREME_WORKTABLE_HANDLER, syncId, 3, 3);
    this.context = context;
    this.player = playerInventory.player;

    this.addResultSlot(this.player, 124, 35);
    this.addCraftingGridSlots(30, 17);
    this.addStandardInventorySlots(playerInventory, 8, 84);
  }

  @Override
  public void beginPlacingRecipe() {
    this.filling = true;
  }

  @Override
  public boolean canTakeItemForPickAll(
      @NonNull ItemStack stack,
      Slot slot
  ) {
    return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
  }

  @Override
  public ItemStack findCustomRecipeResult(
      CraftingInput input,
      ServerLevel world
  ) {
    var supremeMatch = world
        .recipeAccess()
        .getSynchronizedRecipes()
        .getFirstMatch(CustomRecipes.SUPREME_CRAFTING_TYPE, input, world);

    return supremeMatch.map(
        craftingRecipeRecipeHolder -> craftingRecipeRecipeHolder.value().assemble(input)
    ).orElse(ItemStack.EMPTY);
  }

  @Override
  public void finishPlacingRecipe(
      @NonNull ServerLevel world,
      @NonNull RecipeHolder<CraftingRecipe> recipe
  ) {
    this.filling = false;
    this.updateResult(
        this,
        world,
        this.player,
        this.craftSlots,
        this.resultSlots,
        recipe
    );
  }

  @Override
  public int getHotbarEnd() {
    return HOTBAR_END;
  }

  @Override
  public int getHotbarStart() {
    return HOTBAR_START;
  }

  @Override
  public int getInputEnd() {
    return INPUT_END;
  }

  @Override @NonNull
  public List<Slot> getInputGridSlots() {
    return this.slots.subList(1, 10);
  }

  @Override
  public int getInputStart() {
    return INPUT_START;
  }

  @Override
  public int getInventoryEnd() {
    return INVENTORY_END;
  }

  @Override
  public int getInventoryStart() {
    return INVENTORY_START;
  }

  @Override @NonNull
  public RecipeBookType getRecipeBookType() {
    return RecipeBookType.CRAFTING;
  }

  @Override
  public int getResultId() {
    return RESULT_ID;
  }

  @Override @NonNull
  public Slot getResultSlot() {
    return this.slots.getFirst();
  }

  @Override
  public List<Slot> getSlots() {
    return this.slots;
  }

  @Override
  public boolean noInsertItem(
      ItemStack stack,
      int start,
      int end,
      boolean fromPlayer
  ) {
    return !super.moveItemStackTo(stack, start, end, fromPlayer);
  }

  @Override @NonNull
  public ItemStack quickMoveStack(
      @NonNull Player player,
      int slot
  ) {
    return CustomCraftingBlock.super.quickMove(player, slot);
  }

  @Override
  public void removed(
      @NonNull Player player
  ) {
    super.removed(player);
    this.context.execute((_, _) -> this.clearContainer(player, this.craftSlots));
  }

  @Override
  public void slotsChanged(
      @NonNull Container inventory
  ) {
    if (this.filling) return;
    this.context.execute((world, _) -> {
      if (!(world instanceof ServerLevel serverWorld)) return;
      this.updateResult(
          this,
          serverWorld,
          this.player,
          this.craftSlots,
          this.resultSlots,
          null
      );
    });
  }

  @Override
  public boolean stillValid(
      @NonNull Player player
  ) {
    return stillValid(this.context, player, SupremeBlocks.SUPREME_WORKTABLE);
  }

  @Override @NonNull
  protected Player owner() {
    return this.player;
  }

}
