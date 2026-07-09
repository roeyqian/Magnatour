/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.durable;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Fabric
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.menu.item.UniverseConsoleMenu;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;

public class UniverseConsole extends Item {

  public UniverseConsole(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public InteractionResult use(
      @NonNull Level world,
      Player player,
      @NonNull InteractionHand hand
  ) {
    ItemStack stack = player.getItemInHand(hand);
    int mode = stack.getOrDefault(RegComponentTypes.UNIVERSE_CONSOLE_MODE, 0);

    if (mode == 0) {
      return execWorkingManager(world, player, stack);
    }

    return InteractionResult.PASS;
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    Player player = context.getPlayer();
    if (player == null) return InteractionResult.PASS;

    ItemStack stack = context.getItemInHand();
    int mode = stack.getOrDefault(RegComponentTypes.UNIVERSE_CONSOLE_MODE, 0);

    if (mode == 1) return bindBlock(context);
    return InteractionResult.PASS;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_console", 3)
        )
        .component(
            DataComponents.ATTRIBUTE_MODIFIERS,
            createAttributes()
        )
        .component(RegComponentTypes.UNIVERSE_CONSOLE_MODE, 0)
        .component(RegComponentTypes.UNIVERSE_CONSOLE_BOUND_LIST, BoundBlockList.EMPTY);
  }

  private static ItemAttributeModifiers createAttributes() {
    return ItemAttributeModifiers.builder()
        .add(
            Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(
                Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_entity_range"),
                1024.0F,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.ANY
        )
        .add(
            Attributes.BLOCK_INTERACTION_RANGE,
            new AttributeModifier(
                Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_block_range"),
                1024.0F,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.ANY
        )
        .build();
  }

  private InteractionResult execWorkingManager(
      Level world,
      Player player,
      ItemStack stack
  ) {
    if (world.isClientSide()) return InteractionResult.SUCCESS;

    BoundBlockList boundList = stack.getOrDefault(
        RegComponentTypes.UNIVERSE_CONSOLE_BOUND_LIST, BoundBlockList.EMPTY
    );

    player.openMenu(new ExtendedMenuProvider<BoundBlockList>() {

      @Override @NonNull
      public Component getDisplayName() {
        return Component.translatable("item.magnatour.universe_console");
      }

      @Override
      public AbstractContainerMenu createMenu(
          int syncId,
          @NonNull Inventory inv,
          @NonNull Player player
      ) {
        return new UniverseConsoleMenu(syncId, boundList);
      }

      @Override
      public BoundBlockList getScreenOpeningData(
          @NonNull ServerPlayer player
      ) {
        return boundList;
      }
    });

    return InteractionResult.CONSUME;
  }

  private InteractionResult bindBlock(
      UseOnContext context
  ) {
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockState state = world.getBlockState(pos);
    Player player = context.getPlayer();
    ItemStack stack = context.getItemInHand();

    if (player == null) return InteractionResult.PASS;

    if (state.getMenuProvider(world, pos) == null) {
      if (!world.isClientSide()) {
        player.sendOverlayMessage(
            Component.translatable("msg.magnatour.universe_console.unable_bind")
                .withStyle(ChatFormatting.YELLOW)
        );
      }
      return InteractionResult.FAIL;
    }

    if (!world.isClientSide()) {
      BoundBlockList currentList = stack.getOrDefault(
          RegComponentTypes.UNIVERSE_CONSOLE_BOUND_LIST,
          BoundBlockList.EMPTY
      );

      String blockName = state.getBlock().getName().getString();
      BoundBlocks newBlock = new BoundBlocks(pos, world.dimension(), blockName);

      BoundBlockList newList = currentList.withAdded(newBlock);
      stack.set(RegComponentTypes.UNIVERSE_CONSOLE_BOUND_LIST, newList);

      player.sendOverlayMessage(
          Component.translatable("msg.magnatour.universe_console.bind")
              .withStyle(ChatFormatting.GREEN)
      );
    }

    return InteractionResult.SUCCESS;
  }

public record BoundBlockList(
    List<BoundBlocks> blocks
) {

    public static final BoundBlockList EMPTY = new BoundBlockList(List.of());

    public static final Codec<BoundBlockList> CODEC =
        BoundBlocks.CODEC.listOf().xmap(BoundBlockList::new, BoundBlockList::blocks);

    public static final StreamCodec<RegistryFriendlyByteBuf, BoundBlockList> PACKET_CODEC =
        BoundBlocks.PACKET_CODEC.apply(ByteBufCodecs.list()).map(BoundBlockList::new, BoundBlockList::blocks);

    public BoundBlockList withAdded(
        BoundBlocks block
    ) {
      List<BoundBlocks> newList = new ArrayList<>(blocks);
      newList.removeIf(b -> b.pos().equals(block.pos()) && b.dimension().equals(block.dimension()));
      newList.add(block);
      return new BoundBlockList(newList);
    }

    public BoundBlockList withRemoved(
        BlockPos pos,
        ResourceKey<Level> dimension
    ) {
      List<BoundBlocks> newList = new ArrayList<>(blocks);
      newList.removeIf(b -> b.pos().equals(pos) && b.dimension().equals(dimension));
      return new BoundBlockList(newList);
    }

  }

public record BoundBlocks(
    BlockPos pos,
    ResourceKey<Level> dimension,
    String displayName
) {

    public static final Codec<BoundBlocks> CODEC = RecordCodecBuilder.create((instance) -> instance
        .group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BoundBlocks::pos),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(BoundBlocks::dimension),
            Codec.STRING.fieldOf("name").forGetter(BoundBlocks::displayName)
        )
        .apply(instance, BoundBlocks::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BoundBlocks> PACKET_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BoundBlocks::pos,
            ResourceKey.streamCodec(Registries.DIMENSION),
            BoundBlocks::dimension,
            ByteBufCodecs.STRING_UTF8,
            BoundBlocks::displayName,
            BoundBlocks::new
        );

  }

}
