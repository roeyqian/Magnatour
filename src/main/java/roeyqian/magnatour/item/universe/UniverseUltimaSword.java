/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.item.CustomToolMaterial;
import roeyqian.magnatour.entity.universe.UniverseFireball;
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.registry.logic.CustomComponents;

public class UniverseUltimaSword extends Item {

  public UniverseUltimaSword(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override
  public void hurtEnemy(
      @NonNull ItemStack stack,
      @NonNull LivingEntity target,
      @NonNull LivingEntity user
  ) {
    if (!(user instanceof Player player)) return;
    if (!(player.level() instanceof ServerLevel world)) return;

    int mode = stack.getOrDefault(CustomComponents.UNIVERSE_ULTIMA_SWORD_MODE, 0);
    target.igniteForTicks(100);
    if (mode == 1) execUniverseKill(world, target);
  }

  @Override
  public boolean isCorrectToolForDrops(
      @NonNull ItemStack stack,
      @NonNull BlockState state
  ) {
    return true;
  }

  @Override @NonNull
  public InteractionResult use(
      @NonNull Level world,
      Player player,
      @NonNull InteractionHand hand
  ) {
    int mode = player.getItemInHand(hand).getOrDefault(CustomComponents.UNIVERSE_ULTIMA_SWORD_MODE, 0);
    return mode == 1 ? execFireballMode(world, player) : InteractionResult.PASS;
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    int mode = context.getItemInHand().getOrDefault(CustomComponents.UNIVERSE_ULTIMA_SWORD_MODE, 1);
    return mode == 0 ? execFlintMode(context) : InteractionResult.PASS;
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_ultima_sword", 3)
        )
        .component(
            DataComponents.ATTRIBUTE_MODIFIERS,
            createAttributes()
        );
  }

  private static void execUniverseKill(
      ServerLevel world,
      LivingEntity target
  ) {
    EntityTypes.LIGHTNING_BOLT.spawn(world, target.blockPosition(), EntitySpawnReason.TRIGGERED);
    world.playSound(
        null,
        target.getX(), target.getY(), target.getZ(),
        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
        100.0F, 1.0F
    );
    target.kill(world);
    if (target.isAlive()) {
      target.discard();
    }
  }

  private static InteractionResult execFireballMode(
      Level world,
      Player player
  ) {
    if (player == null) return InteractionResult.PASS;
    player.swing(player.getUsedItemHand());
    if (!(world instanceof ServerLevel)) return InteractionResult.PASS;

    double speedMultiplier = 5.0F;
    Vec3 lookVec = player.getViewVector(1.0F);
    Vec3 velocity = lookVec.scale(speedMultiplier);
    UniverseFireball fireball = new UniverseFireball(world, player, velocity, 15);

    fireball.setDeltaMovement(velocity);
    fireball.setPos(
        player.getX() + lookVec.x * 1.5,
        player.getY() + player.getEyeHeight() + lookVec.y * 0.5,
        player.getZ() + lookVec.z * 1.5
    );

    world.addFreshEntity(fireball);
    world.playSound(
        null, player.getX(), player.getY(), player.getZ(),
        SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F
    );

    return InteractionResult.SUCCESS;
  }

  private static InteractionResult execFlintMode(
      UseOnContext context
  ) {
    Level world = context.getLevel();
    Player player = context.getPlayer();
    BlockPos blockPos = context.getClickedPos();
    BlockState blockState = world.getBlockState(blockPos);

    if (!CampfireBlock.canLight(blockState)
        && !CandleBlock.canLight(blockState)
        && !CandleCakeBlock.canLight(blockState)
    ) {
      BlockPos blockPos2 = blockPos.relative(context.getClickedFace());

      if (BaseFireBlock.canBePlacedAt(world, blockPos2, context.getHorizontalDirection())) {
        world.playSound(
            null,
            blockPos2,
            SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
            1.0F, 1.0F
        );

        BlockState blockState2 = BaseFireBlock.getState(world, blockPos2);
        world.setBlock(blockPos2, blockState2, Block.UPDATE_ALL_IMMEDIATE);
        world.gameEvent(player, GameEvent.BLOCK_PLACE, blockPos);
        return InteractionResult.SUCCESS;
      } else {
        return InteractionResult.FAIL;
      }
    } else {
      world.playSound(
          player, blockPos,
          SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
          1.0F, 1.0F
      );

      world.setBlock(
          blockPos, blockState.setValue(BlockStateProperties.LIT, true),
          Block.UPDATE_ALL_IMMEDIATE
      );
      world.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
      return InteractionResult.SUCCESS;
    }
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
            Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(
                Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe_entity_range"),
                1024.0F,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.HAND
        )
        .build();
  }

}
