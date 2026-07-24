/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Minecraft
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * End Portal-style activation logic:
 * - 12 frame blocks in a 3x3 hollow square
 * - All frame blocks must be "lit" (activated)
 * - When complete, 3x3 portal blocks fill the center
 */
public interface UniverseMetaPortalBlock {

  int TELEPORT_TICKS = 80;

  VoxelShape X_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
  VoxelShape Z_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

  EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

  static BlockPos findExistingPortal(
      ServerLevel world,
      Block portalBlock
  ) {
    BlockPos center = new BlockPos(0, 0, 0);
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (int x = -16; x <= 16; x++) {
      for (int z = -16; z <= 16; z++) {
        for (int y = world.getMinY(); y <= world.getMaxY(); y++) {
          mutable.set(center.getX() + x, y, center.getZ() + z);
          if (world.getBlockState(mutable).getBlock() == portalBlock) {
            return mutable.immutable();
          }
        }
      }
    }
    return null;
  }

  static BlockPos buildPortalAt(
      ServerLevel world,
      BlockPos centerPos,
      Block frameBlock,
      Block portalBlock
  ) {
    // Build at y=32 above the cube center
    BlockPos corner = centerPos.offset(-2, 32, -2);

    // Place 5x5: frame on outer edge, portal in inner 3x3
    for (int dx = 0; dx < 5; dx++) {
      for (int dz = 0; dz < 5; dz++) {
        BlockPos pos = corner.offset(dx, 0, dz);
        boolean isEdge = (dx == 0 || dx == 4 || dz == 0 || dz == 4);

        if (isEdge) {
          // Place frame block on edges
          world.setBlockAndUpdate(pos, frameBlock.defaultBlockState());
        } else {
          // Inner 3x3 gets portal blocks
          world.setBlock(pos, portalBlock.defaultBlockState().setValue(AXIS, Direction.Axis.X), 18);
        }
      }
    }

    return corner.offset(2, 0, 2);
  }

  static BlockPos findOrCreatePortal(
      ServerLevel targetWorld,
      BlockPos sourcePos,
      Block frameBlock,
      Block portalBlock
  ) {
    BlockPos existing = findExistingPortal(targetWorld, portalBlock);
    if (existing != null) return existing;

    return buildPortalAt(targetWorld, new BlockPos(0, 0, 0), frameBlock, portalBlock);
  }

  /**
   * Check if a 5x5 area starting at corner forms a valid activated portal frame.
   * The 12 edge positions (outer ring, EXCLUDING corners) must all be the frame block and all must be lit.
   * The inner 3x3 area and 4 corners can be anything (will be filled with portal).
   */
  static boolean isCompleteFrame(
      Level world,
      BlockPos corner,
      Block frameBlock
  ) {
    System.out.println("[Portal] Checking 5x5 frame at corner: " + corner);
    // Check 5x5 grid - only outer edges EXCLUDING corners (12 blocks total)
    int validCount = 0;
    for (int dx = 0; dx < 5; dx++) {
      for (int dz = 0; dz < 5; dz++) {
        // Skip corners: (0,0), (0,4), (4,0), (4,4)
        boolean isCorner = (dx == 0 && dz == 0) || (dx == 0 && dz == 4) || (dx == 4 && dz == 0) || (dx == 4 && dz == 4);

        // Only check outer edge, excluding corners
        boolean isEdge = (dx == 0 || dx == 4 || dz == 0 || dz == 4) && !isCorner;

        if (!isEdge) {
          // Inner 3x3 + 4 corners - skip, can be anything
          continue;
        }

        BlockPos pos = corner.offset(dx, 0, dz);
        BlockState state = world.getBlockState(pos);

        System.out.println("[Portal]   Checking pos " + pos + " (offset " + dx + "," + dz + "): block=" + state.getBlock().getDescriptionId() + ", isFrame=" + state.is(frameBlock) + ", hasLIT=" + state.hasProperty(BlockStateProperties.LIT) + ", lit=" + (state.hasProperty(BlockStateProperties.LIT) ? state.getValue(BlockStateProperties.LIT) : "N/A"));

        // Must be the frame block
        if (!state.is(frameBlock)) {
          System.out.println("[Portal] FAILED: Not a frame block at " + pos);
          return false;
        }

        // Must be lit (activated)
        if (!state.hasProperty(BlockStateProperties.LIT) || !state.getValue(BlockStateProperties.LIT)) {
          System.out.println("[Portal] FAILED: Not lit at " + pos);
          return false;
        }
        validCount++;
      }
    }
    System.out.println("[Portal] SUCCESS: Frame complete with " + validCount + " valid blocks!");
    return true;
  }

  static void execTeleport(
      ServerPlayer player,
      BlockPos portalPos,
      Block frameBlock,
      Block portalBlock,
      ResourceKey<Level> sourceDim,
      ResourceKey<Level> targetDim
  ) {
    MinecraftServer server = player.level().getServer();
    ServerLevel currentWorld = player.level();
    ServerLevel targetWorld;

    if (currentWorld.dimension() == sourceDim) {
      targetWorld = server.getLevel(targetDim);
    } else {
      targetWorld = server.getLevel(sourceDim);
    }

    if (targetWorld == null) return;

    BlockPos targetPos = findOrCreatePortal(
        targetWorld,
        portalPos,
        frameBlock,
        portalBlock
    );

    player.teleportTo(
        targetWorld,
        targetPos.getX() + 0.5,
        targetPos.getY(),
        targetPos.getZ() + 0.5,
        Set.of(),
        player.getYRot(),
        player.getXRot(),
        false
    );
    player.setPortalCooldown(80);
    targetWorld.playSound(
        null, player.blockPosition(),
        SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.2F, 1.0F
    );
  }

  /**
   * Search around the clicked position to find a valid 5x5 frame.
   * Returns the corner position if found, null otherwise.
   */
  static BlockPos findCompleteFrame(
      Level world,
      BlockPos clickedPos,
      Block frameBlock
  ) {
    // The clicked position could be any of the 12 frame blocks
    // Try all possible corner positions where this block could be part of a 5x5 frame
    for (int dx = -4; dx <= 0; dx++) {
      for (int dz = -4; dz <= 0; dz++) {
        BlockPos corner = clickedPos.offset(dx, 0, dz);
        if (isCompleteFrame(world, corner, frameBlock)) {
          return corner;
        }
      }
    }
    return null;
  }

  static VoxelShape getOutlineShape(
      BlockState state
  ) {
    return state.getValue(AXIS) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
  }

  static void handleEntityCollision(
      Level world,
      BlockPos pos,
      Entity entity,
      Map<UUID, Integer> portalTicks,
      Set<UUID> inPortalThisTick,
      boolean[] clientInPortalFlag,
      Block frameBlock,
      Block portalBlock,
      ResourceKey<Level> sourceDim,
      ResourceKey<Level> targetDim
  ) {
    if (world.isClientSide()) {
      if (entity instanceof Player && clientInPortalFlag != null) clientInPortalFlag[0] = true;
      return;
    }

    if (!(entity instanceof ServerPlayer player)) return;
    if (player.isPassenger() || player.isVehicle()) return;
    if (!player.canUsePortal(false)) return;

    UUID uuid = player.getUUID();

    if (player.isOnPortalCooldown()) {
      player.setPortalCooldown(20);
      portalTicks.remove(uuid);
      return;
    }

    if (player.isCreative()) {
      player.setPortalCooldown(80);
      execTeleport(player, pos, frameBlock, portalBlock, sourceDim, targetDim);
      return;
    }

    if (!inPortalThisTick.add(uuid)) return;

    int ticks = portalTicks.merge(uuid, 1, Integer::sum);
    if (ticks == 1) {
      world.playSound(
          null,
          pos,
          SoundEvents.PORTAL_TRIGGER,
          SoundSource.PLAYERS,
          0.2F,
          1.0F
      );
    } else if (ticks >= TELEPORT_TICKS) {
      portalTicks.remove(uuid);
      inPortalThisTick.remove(uuid);
      player.setPortalCooldown(60);
      execTeleport(player, pos, frameBlock, portalBlock, sourceDim, targetDim);
    }
  }

  static boolean isValidPortal(
      LevelReader world,
      BlockPos pos,
      Block frameBlock,
      Block portalBlock
  ) {
    return findCompleteFrame((Level)world, pos, frameBlock) != null;
  }

  static boolean shouldBreakPortal(
      Block portalBlock,
      Block frameBlock,
      BlockState state,
      BlockState neighborState,
      BlockPos pos,
      Direction direction,
      LevelReader world
  ) {
    Direction.Axis currentAxis = state.getValue(AXIS);
    Direction.Axis neighborAxis = direction.getAxis();

    return !(currentAxis != neighborAxis && neighborAxis.isHorizontal())
        && !neighborState.is(portalBlock)
        && findCompleteFrame((Level)world, pos, frameBlock) == null;
  }

  /**
   * Try to activate the portal. Called when a frame block is activated (lit).
   * Based on End Portal Frame logic.
   */
  static boolean tryActivatePortal(
      Level world,
      BlockPos pos,
      Block frameBlock,
      Block portalBlock
  ) {
    if (world.isClientSide()) return false;

    // Find if there's a complete 5x5 frame
    BlockPos corner = findCompleteFrame(world, pos, frameBlock);
    if (corner == null) {
      return false;
    }

    // Fill inner 3x3 with portal blocks (like End Portal spawning)
    for (int dx = 1; dx < 4; dx++) {
      for (int dz = 1; dz < 4; dz++) {
        BlockPos portalPos = corner.offset(dx, 0, dz);
        world.setBlock(portalPos, portalBlock.defaultBlockState().setValue(AXIS, Direction.Axis.X), 18);
      }
    }

    // Play End Portal spawn sound
    world.playSound(
        null,
        corner.offset(2, 0, 2),
        SoundEvents.END_PORTAL_SPAWN,
        SoundSource.BLOCKS,
        1.0F,
        1.0F
    );

    return true;
  }

}
