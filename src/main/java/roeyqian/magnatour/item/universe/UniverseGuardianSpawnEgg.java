/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Java Standard
import java.util.Objects;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.item.CustomItemSetting;
import roeyqian.magnatour.registry.content.RegLiveEntities;

public class UniverseGuardianSpawnEgg extends SpawnEggItem {

  public UniverseGuardianSpawnEgg(
      Properties settings
  ) {
    super(applySettings(settings));
  }

  @Override @NonNull
  public InteractionResult useOn(
      UseOnContext context
  ) {
    if (context.getPlayer() != null && context.getPlayer().getRandom().nextInt(0, 9) != 0) {
      Level level = context.getLevel();
      if (!(level instanceof ServerLevel serverLevel)) {
        return InteractionResult.SUCCESS;
      } else {
        ItemStack itemStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockState blockState = level.getBlockState(pos);

        if (level.getBlockEntity(pos) instanceof Spawner spawnerHolder) {
          EntityType<?> type = getType(itemStack);
          if (type == null) {
            return InteractionResult.FAIL;
          } else if (!serverLevel.isSpawnerBlockEnabled()) {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
              serverPlayer.sendSystemMessage(Component.translatable("advMode.notEnabled.spawner"));
            }

            return InteractionResult.FAIL;
          } else {
            spawnerHolder.setEntityId(type, level.getRandom());
            level.sendBlockUpdated(pos, blockState, blockState, 3);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.SUCCESS;
          }
        } else {
          BlockPos spawnPos;
          if (blockState.getCollisionShape(level, pos).isEmpty()) {
            spawnPos = pos;
          } else {
            spawnPos = pos.relative(clickedFace);
          }

          return spawnMob(context.getPlayer(), itemStack, level, spawnPos,
              !Objects.equals(pos, spawnPos) && clickedFace == Direction.UP);
        }
      }
    } else {
      return super.useOn(context);
    }
  }

  private static Properties applySettings(
      Properties settings
  ) {
    return CustomItemSetting.applyUniverseDefaults(settings)
        .spawnEgg(RegLiveEntities.UNIVERSE_GUARDIAN)
        .component(
            DataComponents.LORE,
            CustomItemSetting.universeLore("universe_guardian_spawn_egg", 2)
        );
  }

  private static InteractionResult spawnMob(
      @Nullable final LivingEntity user,
      final ItemStack itemStack,
      final Level level,
      final BlockPos spawnPos,
      final boolean movedUp
  ) {
    EntityType<?> type = getType(itemStack);
    if (type == null) {
      return InteractionResult.FAIL;
    } else if (!type.isAllowedInPeaceful() && level.getDifficulty() == Difficulty.PEACEFUL) {
      return InteractionResult.FAIL;
    } else {
      if (type.spawn((ServerLevel)level, itemStack, user, spawnPos,
          EntitySpawnReason.SPAWN_ITEM_USE, true, movedUp) != null) {
        level.gameEvent(user, GameEvent.ENTITY_PLACE, spawnPos);
      }

      return InteractionResult.SUCCESS;
    }
  }

}
