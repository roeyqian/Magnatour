/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.durable;

// Java Standard
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.utility.registry.gen.RegComponentTypes;

public class UniverseOmniBlade extends Item {

  private static final Map<Block, TillBehavior> TILLING_ACTIONS = Map.ofEntries(
      Map.entry(Blocks.GRASS_BLOCK, tillToFarmland()),
      Map.entry(Blocks.DIRT_PATH, tillToFarmland()),
      Map.entry(Blocks.DIRT, tillToFarmland()),
      Map.entry(Blocks.COARSE_DIRT, tillToDirt()),
      Map.entry(Blocks.ROOTED_DIRT, tillToDirt()),
      Map.entry(RegInsertBlocks.EVER_WATER_GRASS_BLOCK, tillToEverWaterFarmland()),
      Map.entry(RegInsertBlocks.EVER_WATER_SOIL, tillToEverWaterFarmland())
  );

  public UniverseOmniBlade(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override
  public boolean canDestroyBlock(
      @NonNull ItemStack stack,
      @NonNull BlockState state,
      @NonNull Level world,
      @NonNull BlockPos pos,
      @NonNull LivingEntity user
  ) {
    return true;
  }

  @Override
  public boolean isCorrectToolForDrops(
      @NonNull ItemStack stack,
      @NonNull BlockState state
  ) {
    return true;
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    Level world = context.getLevel();
    BlockPos blockPos = context.getClickedPos();
    Player player = context.getPlayer();

    if (player == null) return InteractionResult.PASS;
    player.swing(player.getUsedItemHand());
    if (world.isClientSide()) return InteractionResult.PASS;

    int mode = context.getItemInHand().getOrDefault(RegComponentTypes.UNIVERSE_OMNI_BLADE_MODE, 0);
    return mode == 0 ? execTilling(context, world, blockPos) : execChainBreak(world, blockPos, player);
  }

  private static TillBehavior tillToFarmland() {
    return new TillBehavior(
        HoeItem::onlyIfAirAbove,
        HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())
    );
  }

  private static TillBehavior tillToDirt() {
    return new TillBehavior(
        HoeItem::onlyIfAirAbove,
        HoeItem.changeIntoState(Blocks.DIRT.defaultBlockState())
    );
  }

  private static TillBehavior tillToEverWaterFarmland() {
    return new TillBehavior(
        HoeItem::onlyIfAirAbove,
        HoeItem.changeIntoState(RegInsertBlocks.EVER_WATER_FARMLAND.defaultBlockState())
    );
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_omni_blade", 3)
        )
        .component(
            DataComponents.ATTRIBUTE_MODIFIERS,
            createAttributes()
        );
  }

  private static ItemAttributeModifiers createAttributes() {
    return ItemAttributeModifiers.builder()
        .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(
                Item.BASE_ATTACK_DAMAGE_ID,
                CustomToolMaterial.UNIVERSE_TOOL.attackDamageBonus() - 1,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HAND
        )
        .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(
                Item.BASE_ATTACK_SPEED_ID,
                CustomToolMaterial.UNIVERSE_TOOL.speed() - 4,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HAND
        )
        .add(
            Attributes.BLOCK_INTERACTION_RANGE,
            new AttributeModifier(
                Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_block_range"),
                1024.0F,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HAND
        )
        .build();
  }

  private InteractionResult execTilling(
      UseOnContext context,
      Level world,
      BlockPos blockPos
  ) {
    TillBehavior behavior = TILLING_ACTIONS.get(world.getBlockState(blockPos).getBlock());
    if (behavior == null) return InteractionResult.PASS;

    if (!behavior.predicate().test(context)) return InteractionResult.PASS;

    world.playSound(
        null,
        blockPos,
        SoundEvents.HOE_TILL,
        SoundSource.BLOCKS,
        1.0F,
        1.0F
    );
    behavior.consumer().accept(context);
    return InteractionResult.SUCCESS;
  }

  private InteractionResult execChainBreak(
      Level world,
      BlockPos startPos,
      Player player
  ) {
    BlockState targetState = world.getBlockState(startPos);
    Block targetBlock = targetState.getBlock();
    final int targetCount = 512;
    if (targetState.isAir()) return InteractionResult.PASS;

    List<BlockPos> toBreak = new ArrayList<>();
    Set<BlockPos> visited = new HashSet<>();
    visited.add(startPos);
    ArrayDeque<BlockPos> queue = new ArrayDeque<>();
    queue.add(startPos);

    while (!queue.isEmpty() && toBreak.size() < targetCount) {
      BlockPos current = queue.poll();
      toBreak.add(current);

      for (BlockPos neighbor : BlockPos.betweenClosed(
          current.offset(-1, -1, -1),
          current.offset(1, 1, 1)
      )) {
        BlockPos immutableNeighbor = neighbor.immutable();
        if (visited.add(immutableNeighbor)) {
          if (world.getBlockState(immutableNeighbor).is(targetBlock)) {
            queue.add(immutableNeighbor);
          }
        }
        if (toBreak.size() + queue.size() >= targetCount) break;
      }
    }

    for (BlockPos pos : toBreak) {
      if (world.getBlockState(pos).getDestroySpeed(world, pos) >= 0) {
        world.destroyBlock(pos, true, player, Block.UPDATE_ALL);
      }
    }
    return InteractionResult.SUCCESS;
  }

  private record TillBehavior(
      Predicate<UseOnContext> predicate,
      Consumer<UseOnContext> consumer
  ) {}

}
