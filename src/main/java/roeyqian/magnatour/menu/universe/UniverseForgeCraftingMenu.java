/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.menu.universe;

// Java Standard
import java.util.List;
import java.util.Optional;

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
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseForgeEntity;
import roeyqian.magnatour.menu.CustomCraftingBlock;
import roeyqian.magnatour.mixin.menu.AbstractCraftingMenuAccessor;
import roeyqian.magnatour.registry.logic.MagnaRecipes;
import roeyqian.magnatour.registry.stuff.MagnaBlocks;
import roeyqian.magnatour.registry.stuff.MagnaMenus;

public class UniverseForgeCraftingMenu extends AbstractCraftingMenu implements CustomCraftingBlock {

  public static final int RESULT_ID = 0;

  private static final int HOTBAR_END = 62;
  private static final int HOTBAR_START = 53;
  private static final int INPUT_END = 26;
  private static final int INPUT_START = 1;
  private static final int INVENTORY_END = 53;
  private static final int INVENTORY_START = 26;

  private final Player player;

  private final ContainerLevelAccess context;

  private final @Nullable UniverseForgeEntity forge;

  private boolean filling;

  public UniverseForgeCraftingMenu(
      int syncId,
      Inventory playerInventory
  ) {
    this(syncId, playerInventory, ContainerLevelAccess.NULL, null);
  }

  public UniverseForgeCraftingMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context
  ) {
    this(syncId, playerInventory, context, null);
  }

  public UniverseForgeCraftingMenu(
      int syncId,
      Inventory playerInventory,
      ContainerLevelAccess context,
      @Nullable UniverseForgeEntity forge
  ) {
    super(MagnaMenus.UNIVERSE_FORGE_HANDLER, syncId, 5, 5);
    this.context = context;
    this.player = playerInventory.player;
    this.forge = forge;
    if (forge != null) {
      ((AbstractCraftingMenuAccessor) this).magnatour$setCraftSlots(forge.getCraftingInventory());
    }

    this.addResultSlot(this.player, 149, 54);
    this.addCraftingGridSlots(30, 17);
    this.addStandardInventorySlots(playerInventory, 8, 120);
    this.slotsChanged(this.craftSlots);
  }

  @Override
  public void beginPlacingRecipe() {
    this.filling = true;
  }

  @Override
  public boolean canTakeItemForPickAll(
      @NonNull ItemStack stack,
      @NonNull Slot slot
  ) {
    return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
  }

  @Override
  public boolean clickMenuButton(
      @NonNull Player player,
      int id
  ) {
    return id == 0 && this.forge != null && this.stillValid(player)
        && this.forge.switchMenu(player, true);
  }

  @Override
  public Optional<RecipeHolder<CraftingRecipe>> findCustomRecipe(
      CraftingInput input,
      ServerLevel world
  ) {
    var recipeManager = world.recipeAccess().getSynchronizedRecipes();
    var supremeMatch = recipeManager.getFirstMatch(MagnaRecipes.SUPREME_CRAFTING_TYPE, input, world);

    return supremeMatch.or(
        () -> recipeManager.getFirstMatch(MagnaRecipes.UNIVERSE_CRAFTING_TYPE, input, world)
    );
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
    return this.slots.subList(INPUT_START, INPUT_END);
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

  public boolean isFor(
      UniverseForgeEntity entity
  ) {
    return this.forge == entity;
  }

  @Override
  public boolean noInsertItem(
      ItemStack stack,
      int start, int end,
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
    this.resultSlots.clearContent();
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
    return stillValid(this.context, player, MagnaBlocks.UNIVERSE_FORGE);
  }

  @Override @NonNull
  protected Player owner() {
    return this.player;
  }

}
