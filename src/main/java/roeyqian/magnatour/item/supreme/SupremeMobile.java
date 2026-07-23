/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.supreme;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.menu.supreme.SupremeMobileMenuAccess;
import roeyqian.magnatour.registry.logic.RegComponentTypes;

public class SupremeMobile extends Item {

  public SupremeMobile(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public InteractionResult use(
      Level world,
      Player user,
      @NonNull InteractionHand hand
  ) {
    ItemStack stack = user.getItemInHand(hand);

    user.swing(hand);
    if (world.isClientSide()) return InteractionResult.PASS;

    int mode = stack.getOrDefault(RegComponentTypes.SUPREME_MOBILE_MODE, 0);
    if (mode != 0) return InteractionResult.PASS;

    String blockId = stack.getOrDefault(RegComponentTypes.SUPREME_MOBILE_BLOCK_ID, "");

    if (blockId.isEmpty()) {
      user.sendOverlayMessage(
          Component.translatable("msg.magnatour.supreme_mobile.no_block_stored")
              .withStyle(ChatFormatting.YELLOW)
      );
      return InteractionResult.FAIL;
    }

    if (user instanceof ServerPlayer serverPlayer) {
      boolean success = openVirtualScreen(serverPlayer, blockId);
      return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    return InteractionResult.PASS;
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();
    ItemStack stack = context.getItemInHand();

    int mode = stack.getOrDefault(RegComponentTypes.SUPREME_MOBILE_MODE, 0);
    if (mode != 1) return InteractionResult.PASS;

    if (player == null) return InteractionResult.PASS;
    player.swing(player.getUsedItemHand());
    if (world.isClientSide()) return InteractionResult.PASS;

    BlockState blockState = world.getBlockState(pos);
    MenuConstructor factory = blockState.getMenuProvider(world, pos);
    if (factory == null) {
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.supreme_mobile.unfunctional_block")
              .withStyle(ChatFormatting.YELLOW)
      );
      return InteractionResult.FAIL;
    }

    if (world.getBlockEntity(pos) != null) {
      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.supreme_mobile.has_block_entity")
              .withStyle(ChatFormatting.YELLOW)
      );
      return InteractionResult.FAIL;
    }

    String blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
    stack.set(RegComponentTypes.SUPREME_MOBILE_BLOCK_ID, blockId);

    Component blockName = Component.translatable(blockState.getBlock().getDescriptionId());
    player.sendOverlayMessage(
        Component.translatable("msg.magnatour.supreme_mobile.identified", blockName)
            .withStyle(ChatFormatting.GREEN)
    );

    return InteractionResult.SUCCESS;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applySupremeDefaults(settings)
        .component(DataComponents.LORE, CustomItemSetting.supremeLore("supreme_mobile", 1))
        .component(RegComponentTypes.SUPREME_MOBILE_MODE, 0)
        .component(RegComponentTypes.SUPREME_MOBILE_BLOCK_ID, "");
  }

  private static MenuProvider getNamedScreenHandlerFactory(
      ServerPlayer player,
      Block block,
      MenuConstructor originalFactory
  ) {
    SupremeMobileMenuAccess virtualContext = new SupremeMobileMenuAccess(player);
    return new SimpleMenuProvider(
        (syncId, playerInventory, p) -> {
          AbstractContainerMenu handler = originalFactory.createMenu(syncId, playerInventory, p);
          if (handler != null) SupremeMobileMenuAccess.injectContext(handler, virtualContext);
          return handler;
        },
        Component.translatable(block.getDescriptionId())
    );
  }

  private boolean openVirtualScreen(
      ServerPlayer player,
      String blockId
  ) {
    Level world = player.level();
    BlockPos playerPos = player.blockPosition();

    Identifier blockIdentifier = Identifier.tryParse(blockId);
    if (blockIdentifier == null) {
      sendErrorMessage(player, "invalid_block_id");
      return false;
    }

    Block block = BuiltInRegistries.BLOCK.getValue(blockIdentifier);
    BlockState blockState = block.defaultBlockState();

    MenuConstructor originalFactory = blockState.getMenuProvider(world, playerPos);
    if (originalFactory == null) {
      sendErrorMessage(player, "no_factory");
      return false;
    }

    MenuProvider wrappedFactory = getNamedScreenHandlerFactory(
        player,
        block,
        originalFactory
    );
    player.openMenu(wrappedFactory);
    return true;
  }

  private void sendErrorMessage(
      ServerPlayer player,
      String key
  ) {
    player.sendOverlayMessage(
        Component.translatable("msg.magnatour.supreme_mobile.error" + key)
    );
  }

}
