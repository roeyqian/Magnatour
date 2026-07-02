/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.List;
import java.util.Optional;

// Minecraft
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public interface CustomCraftingBlock {

  List<Slot> getSlots();

  int getResultId();

  boolean noInsertItem(
      ItemStack stack,
      int start,
      int end,
      boolean fromPlayer
  );

  int getInventoryStart();

  int getHotbarEnd();

  int getInputStart();

  int getInputEnd();

  int getHotbarStart();

  int getInventoryEnd();

  ItemStack findCustomRecipeResult(
      CraftingInput input,
      ServerLevel world
  );

  default ItemStack quickMove(
      Player player,
      int slot
  ) {
    ItemStack movedStack = ItemStack.EMPTY;
    Slot currentSlot = getSlots().get(slot);
    if (currentSlot == null || !currentSlot.hasItem()) return ItemStack.EMPTY;

    ItemStack originalStack = currentSlot.getItem();
    movedStack = originalStack.copy();

    if (slot == getResultId()) {
      originalStack.getItem().onCraftedBy(originalStack, player);
      if (noInsertItem(originalStack, getInventoryStart(), getHotbarEnd(), true)) return ItemStack.EMPTY;

      currentSlot.onQuickCraft(originalStack, movedStack);
    } else if (slot >= getInventoryStart() && slot < getHotbarEnd()) {
      if (noInsertItem(originalStack, getInputStart(), getInputEnd(), false)) {
        boolean isInMainInventory = slot < getHotbarStart();
        int fallbackStart = isInMainInventory ? getHotbarStart() : getInventoryStart();
        int fallbackEnd = isInMainInventory ? getHotbarEnd() : getInventoryEnd();

        if (noInsertItem(originalStack, fallbackStart, fallbackEnd, false)) {
          return ItemStack.EMPTY;
        }
      }
    } else if (noInsertItem(originalStack, getInventoryStart(), getHotbarEnd(), false)) {
      return ItemStack.EMPTY;
    }

    if (originalStack.isEmpty()) currentSlot.setByPlayer(ItemStack.EMPTY);
    else currentSlot.setChanged();

    if (originalStack.getCount() == movedStack.getCount()) return ItemStack.EMPTY;
    currentSlot.onTake(player, originalStack);

    if (slot == getResultId()) player.drop(originalStack, false);
    return movedStack;
  }

  default void updateResult(
      AbstractContainerMenu handler,
      ServerLevel world,
      Player player,
      CraftingContainer inputInv,
      ResultContainer resultInv,
      RecipeHolder<CraftingRecipe> recipe
  ) {
    CraftingInput craftingRecipeInput = inputInv.asCraftInput();
    ServerPlayer serverPlayerEntity = (ServerPlayer) player;
    ItemStack itemStack = ItemStack.EMPTY;

    Optional<RecipeHolder<CraftingRecipe>> optional = world
        .getServer()
        .getRecipeManager()
        .getRecipeFor(RecipeType.CRAFTING, craftingRecipeInput, world, recipe);

    if (optional.isPresent()) {
      RecipeHolder<CraftingRecipe> recipeEntry = optional.get();
      CraftingRecipe craftingRecipe = recipeEntry.value();

      if (resultInv.setRecipeUsed(serverPlayerEntity, recipeEntry)) {
        ItemStack itemStack2 = craftingRecipe.assemble(craftingRecipeInput);
        if (itemStack2.isItemEnabled(world.enabledFeatures())) itemStack = itemStack2;
      }
    }

    if (itemStack.isEmpty()) itemStack = findCustomRecipeResult(craftingRecipeInput, world);

    resultInv.setItem(0, itemStack);
    handler.setRemoteSlot(0, itemStack);

    serverPlayerEntity.connection.send(
        new ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), 0, itemStack)
    );
  }

}
