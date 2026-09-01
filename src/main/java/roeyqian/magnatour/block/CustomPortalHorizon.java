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
import java.util.Optional;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.VoxelShape;

// Magnatour
import roeyqian.magnatour.level.PortalLinkSavedData;

public interface CustomPortalHorizon {

  int TELEPORT_TICKS = 80;

  // Match the End Portal: a horizontal surface recessed below the frame top.
  VoxelShape HORIZONTAL_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

  EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

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
    // Check 5x5 grid - only outer edges EXCLUDING corners (12 blocks total)
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

        // Must be the frame block
        if (!state.is(frameBlock)) {
          return false;
        }

        // Must be lit (activated)
        if (!state.hasProperty(BlockStateProperties.LIT) || !state.getValue(BlockStateProperties.LIT)) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean hasClearArrivalSpace(
      ServerLevel world,
      BlockPos center
  ) {
    if (world.isEmptyBlock(center.below())) return false;

    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        BlockPos portalPos = center.offset(dx, 0, dz);
        if (!world.isEmptyBlock(portalPos.above())
            || !world.isEmptyBlock(portalPos.above(2))) {
          return false;
        }
      }
    }
    return true;
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

  static BlockPos findExistingPortal(
      ServerLevel world,
      BlockPos searchCenter,
      Block portalBlock
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (int x = -16; x <= 16; x++) {
      for (int z = -16; z <= 16; z++) {
        for (int y = world.getMinY(); y <= world.getMaxY(); y++) {
          mutable.set(searchCenter.getX() + x, y, searchCenter.getZ() + z);
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
    // Build a 5x5 portal centered on the supplied destination position.
    BlockPos corner = centerPos.offset(-2, 0, -2);

    // Place the 12 non-corner frame blocks and the inner 3x3 portal.
    for (int dx = 0; dx < 5; dx++) {
      for (int dz = 0; dz < 5; dz++) {
        BlockPos pos = corner.offset(dx, 0, dz);
        boolean isCorner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
        boolean isFrame = (dx == 0 || dx == 4 || dz == 0 || dz == 4) && !isCorner;
        boolean isPortal = dx >= 1 && dx <= 3 && dz >= 1 && dz <= 3;

        if (isFrame) {
          BlockState frameState = frameBlock.defaultBlockState();
          if (frameState.hasProperty(BlockStateProperties.LIT)) {
            frameState = frameState.setValue(BlockStateProperties.LIT, true);
          }
          world.setBlockAndUpdate(pos, frameState);
        } else if (isPortal) {
          world.setBlock(pos, portalBlock.defaultBlockState().setValue(AXIS, Direction.Axis.X), 18);
          world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
          world.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 18);
        }
      }
    }

    return corner.offset(2, 0, 2);
  }

  /**
   * Custom dimensions define their own arrival platform. For the Overworld, create the fallback
   * portal above local terrain instead of at a fixed Y level that may be inside stone.
   */
  static BlockPos findSafeFallbackPortalCenter(
      ServerLevel world,
      BlockPos fallbackPortalCenter
  ) {
    if (world.dimension() != Level.OVERWORLD) return fallbackPortalCenter;

    int fallbackX = fallbackPortalCenter.getX();
    int fallbackZ = fallbackPortalCenter.getZ();
    for (int radius = 0; radius <= 16; radius++) {
      for (int x = fallbackX - radius; x <= fallbackX + radius; x++) {
        for (int z = fallbackZ - radius; z <= fallbackZ + radius; z++) {
          if (radius > 0 && x != fallbackX - radius && x != fallbackX + radius
              && z != fallbackZ - radius && z != fallbackZ + radius) {
            continue;
          }

          BlockPos candidate = new BlockPos(
              x,
              world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z),
              z
          );
          if (hasClearArrivalSpace(world, candidate)) return candidate;
        }
      }
    }

    return new BlockPos(
        fallbackX,
        world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fallbackX, fallbackZ),
        fallbackZ
    );
  }

  static BlockPos getPortalCenter(
      Level world,
      BlockPos portalPos,
      Block frameBlock
  ) {
    BlockPos corner = findCompleteFrame(world, portalPos, frameBlock);
    return corner == null ? portalPos : corner.offset(2, 0, 2);
  }

  static boolean isValidPortal(
      LevelReader world,
      BlockPos pos,
      Block frameBlock,
      Block portalBlock
  ) {
    BlockPos corner = findCompleteFrame((Level)world, pos, frameBlock);
    if (corner == null) return false;

    // Match the ore-continent portal's complete-shape check: a sound frame is
    // not enough; all nine cells in the interior must still be portal blocks.
    for (int dx = 1; dx < 4; dx++) {
      for (int dz = 1; dz < 4; dz++) {
        if (!world.getBlockState(corner.offset(dx, 0, dz)).is(portalBlock)) {
          return false;
        }
      }
    }
    return true;
  }

  static BlockPos findOrCreatePortal(
      ServerLevel targetWorld,
      BlockPos fallbackPortalCenter,
      Block frameBlock,
      Block portalBlock
  ) {
    BlockPos existing = findExistingPortal(targetWorld, fallbackPortalCenter, portalBlock);
    if (existing != null) return existing;

    return buildPortalAt(
        targetWorld,
        findSafeFallbackPortalCenter(targetWorld, fallbackPortalCenter),
        frameBlock,
        portalBlock
    );
  }

  static void execTeleport(
      ServerPlayer player,
      BlockPos portalPos,
      Block frameBlock,
      Block portalBlock,
      ResourceKey<Level> sourceDim,
      ResourceKey<Level> targetDim,
      BlockPos fallbackPortalCenter
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

    PortalLinkSavedData linkData = PortalLinkSavedData.get(server);
    PortalLinkSavedData.Endpoint sourceEndpoint = new PortalLinkSavedData.Endpoint(
        currentWorld.dimension(), getPortalCenter(currentWorld, portalPos, frameBlock)
    );
    BlockPos targetPos = null;

    Optional<PortalLinkSavedData.Endpoint> linkedEndpoint = linkData.getDestination(sourceEndpoint);
    if (linkedEndpoint.isPresent()) {
      PortalLinkSavedData.Endpoint endpoint = linkedEndpoint.get();
      ServerLevel linkedWorld = server.getLevel(endpoint.dimension());
      if (linkedWorld != null && isValidPortal(linkedWorld, endpoint.pos(), frameBlock, portalBlock)) {
        targetWorld = linkedWorld;
        targetPos = endpoint.pos();
      } else {
        linkData.unlink(sourceEndpoint);
      }
    }

    if (targetPos == null) {
      targetPos = findOrCreatePortal(
          targetWorld, fallbackPortalCenter, frameBlock, portalBlock
      );
      BlockPos targetCenter = getPortalCenter(targetWorld, targetPos, frameBlock);
      linkData.link(
          sourceEndpoint,
          new PortalLinkSavedData.Endpoint(targetWorld.dimension(), targetCenter)
      );
    }

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

  static VoxelShape getOutlineShape(
      BlockState state
  ) {
    return HORIZONTAL_SHAPE;
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
      ResourceKey<Level> targetDim,
      BlockPos fallbackPortalCenter
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
      execTeleport(player, pos, frameBlock, portalBlock, sourceDim, targetDim, fallbackPortalCenter);
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
      execTeleport(player, pos, frameBlock, portalBlock, sourceDim, targetDim, fallbackPortalCenter);
    }
  }

  static boolean shouldBreakPortal(
      Block portalBlock,
      Block frameBlock,
      BlockState neighborState,
      BlockPos pos,
      LevelReader world
  ) {
    // Unlike a vertical Nether-style portal, every cell in this 3x3 portal
    // must react to frame changes on both horizontal axes. Once one cell is
    // removed, its neighbours receive the same invalid-frame check and the
    // entire portal is cleared.
    return !neighborState.is(portalBlock)
        && !isValidPortal(world, pos, frameBlock, portalBlock);
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
