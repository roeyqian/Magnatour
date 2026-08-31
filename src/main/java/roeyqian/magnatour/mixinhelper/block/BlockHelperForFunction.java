/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.block;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.CustomPortalVertex;
import roeyqian.magnatour.block.SummonStructureHelper;
import roeyqian.magnatour.block.supreme.ChunkTntBlock;
import roeyqian.magnatour.level.recipe.CraftingResultHelper;
import roeyqian.magnatour.level.recipe.SupremeCraftingRecipe;
import roeyqian.magnatour.level.recipe.UniverseCraftingRecipe;
import roeyqian.magnatour.registry.logic.CustomRecipes;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public final class BlockHelperForFunction {

  private BlockHelperForFunction() {}

  public static void handleBaseFireCanBePlacedAt(
      Level level,
      BlockPos pos,
      Direction forwardDirection,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (cir.getReturnValue()) return;

    cir.setReturnValue(
        CustomPortalVertex.canBePlacedAt(level, pos, forwardDirection)
            || canCreateNetherPortal(level, pos, forwardDirection)
    );
  }

  public static void handleBaseFireOnPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState,
      CallbackInfo ci
  ) {
    if (oldState.is(state.getBlock())) return;
    if (CustomPortalVertex.tryCreatePortalFromFire(level, pos)) {
      ci.cancel();
      return;
    }
    if (tryCreateNetherPortal(level, pos)) ci.cancel();
  }

  public static void handleFireTick(
      ServerLevel level,
      BlockPos pos
  ) {
    for (Direction direction : Direction.values()) {
      BlockPos neighbor = pos.relative(direction);
      if (!(level.getBlockState(neighbor).getBlock() instanceof ChunkTntBlock)) continue;

      ChunkTntBlock.prime(level, neighbor, null);
      level.removeBlock(neighbor, false);
    }
  }

  public static void handleOnTake(
      Player player,
      CraftingContainer craftSlots,
      ItemStack stack,
      int removeCount,
      AchievementConsumer achievementConsumer,
      CallbackInfo ci
  ) {
    RecipeHolder<? extends CraftingRecipe> recipeHolder = getActiveCustomRecipe(player, craftSlots);
    if (recipeHolder == null) return;

    CraftingRecipe recipe = recipeHolder.value();
    int baseResultCount = getBaseResultCount(recipe);
    int removedItemCount = removeCount > 0 ? removeCount : Math.max(stack.getCount(), baseResultCount);
    int craftsToConsume = CraftingResultHelper.getConsumedCraftCount(
        removedItemCount,
        baseResultCount
    );
    ItemStack achievementStack = stack.isEmpty()
        ? recipe.assemble(craftSlots.asCraftInput()).copyWithCount(baseResultCount)
        : stack;

    achievementConsumer.accept(achievementStack);

    for (int i = 0; i < craftsToConsume; i++) {
      consumeSingleCraft(player, craftSlots, recipe);
    }

    ci.cancel();
  }

  public static <T> boolean handlePlaceRecipe(
      int width,
      int height,
      Recipe<?> recipe,
      Iterable<T> slots,
      PlaceRecipeHelper.Output<T> filler
  ) {
    if (recipe instanceof SupremeCraftingRecipe supremeRecipe) {
      PlaceRecipeHelper.placeRecipe(
          width, height,
          supremeRecipe.getWidth(), supremeRecipe.getHeight(),
          slots, filler
      );
      return true;
    }

    if (recipe instanceof UniverseCraftingRecipe universeRecipe) {
      PlaceRecipeHelper.placeRecipe(
          width, height,
          universeRecipe.getWidth(), universeRecipe.getHeight(),
          slots, filler
      );
      return true;
    }

    return false;
  }

  public static int handleQuickCraft(
      Player player,
      CraftingContainer craftSlots,
      int removeCount,
      int amount,
      CallbackInfo ci
  ) {
    if (!shouldHandleCustomRecipe(player, craftSlots)) {
      return removeCount;
    }

    ci.cancel();
    return removeCount + amount;
  }

  public static void handleVanillaSummonTriggerOnPlace(
      BlockState state,
      Level level,
      BlockPos pos,
      BlockState oldState
  ) {
    SummonStructureHelper.execBlockTrigger(state, level, pos, oldState);
  }

  public static boolean shouldHandleCustomRecipe(
      Player player,
      CraftingContainer craftSlots
  ) {
    return getActiveCustomRecipe(player, craftSlots) != null;
  }

  /**
   * Vanilla only considers the Overworld and Nether valid Nether-portal dimensions. Universe
   * Meta is intentionally a portal hub, so let its obsidian frames use the standard shape.
   */
  private static boolean canCreateNetherPortal(
      Level level,
      BlockPos pos,
      Direction forwardDirection
  ) {
    if (level.dimension() != CustomDimensions.UNIVERSE_META) return false;

    Direction.Axis axis = forwardDirection.getAxis().isHorizontal()
        ? forwardDirection.getCounterClockWise().getAxis()
        : Direction.Plane.HORIZONTAL.getRandomAxis(level.getRandom());
    return PortalShape.findEmptyPortalShape(level, pos, axis).isPresent();
  }

  private static boolean tryCreateNetherPortal(
      Level level,
      BlockPos pos
  ) {
    if (level.dimension() != CustomDimensions.UNIVERSE_META) return false;

    var portalShape = PortalShape.findEmptyPortalShape(level, pos, Direction.Axis.X);
    if (portalShape.isEmpty()) return false;

    portalShape.get().createPortalBlocks(level);
    return true;
  }

  private static RecipeHolder<? extends CraftingRecipe> getActiveCustomRecipe(
      Player player,
      CraftingContainer craftSlots
  ) {
    if (player.level().isClientSide()) return null;

    Level world = player.level();
    CraftingInput recipeInput = craftSlots.asCraftInput();
    var recipeManager = world.recipeAccess().getSynchronizedRecipes();

    if (recipeManager.getFirstMatch(RecipeType.CRAFTING, recipeInput, world).isPresent()) {
      return null;
    }

    var supremeMatch = recipeManager.getFirstMatch(CustomRecipes.SUPREME_CRAFTING_TYPE, recipeInput, world);
    if (supremeMatch.isPresent()) return supremeMatch.get();

    var universeMatch = recipeManager.getFirstMatch(CustomRecipes.UNIVERSE_CRAFTING_TYPE, recipeInput, world);
    return universeMatch.orElse(null);
  }

  private static int getBaseResultCount(
      CraftingRecipe recipe
  ) {
    if (recipe instanceof SupremeCraftingRecipe supremeRecipe) {
      return supremeRecipe.getBaseResultCount();
    }
    if (recipe instanceof UniverseCraftingRecipe universeRecipe) {
      return universeRecipe.getBaseResultCount();
    }

    return 1;
  }

  private static void consumeSingleCraft(
      Player player,
      CraftingContainer craftSlots,
      CraftingRecipe recipe
  ) {
    CraftingInput.Positioned positionedInput = craftSlots.asPositionedCraftInput();
    CraftingInput currentInput = positionedInput.input();
    NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(currentInput);
    int left = positionedInput.left();
    int top = positionedInput.top();

    for (int y = 0; y < currentInput.height(); y++) {
      for (int x = 0; x < currentInput.width(); x++) {
        int slotIndex = x + left + (y + top) * craftSlots.getWidth();
        ItemStack inputStack = craftSlots.getItem(slotIndex);
        ItemStack remainingStack = remainingItems.get(x + y * currentInput.width());

        if (!inputStack.isEmpty()) {
          craftSlots.removeItem(slotIndex, 1);
          inputStack = craftSlots.getItem(slotIndex);
        }

        if (remainingStack.isEmpty()) continue;

        if (inputStack.isEmpty()) {
          craftSlots.setItem(slotIndex, remainingStack);
        } else if (ItemStack.isSameItemSameComponents(inputStack, remainingStack)) {
          remainingStack.grow(inputStack.getCount());
          craftSlots.setItem(slotIndex, remainingStack);
        } else if (!player.getInventory().add(remainingStack)) {
          player.drop(remainingStack, false);
        }
      }
    }
  }

  @FunctionalInterface
  public interface AchievementConsumer {

    void accept(
        ItemStack stack
    );

  }

}
