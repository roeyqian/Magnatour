/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.block;

// Java Standard
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.VoxelShape;

// Magnatour
import roeyqian.magnatour.registry.content.SupremeBlocks;
import roeyqian.magnatour.level.PortalLinkSavedData;
import roeyqian.magnatour.registry.worldgen.CustomDimensions;

public interface CustomPortalVertex {

  int MAX_PORTAL_INNER_HEIGHT = 21;
  int MAX_PORTAL_INNER_WIDTH = 21;
  int MIN_PORTAL_FRAME_HEIGHT = 4;
  int MIN_PORTAL_FRAME_WIDTH = 3;
  int MIN_PORTAL_INNER_HEIGHT = MIN_PORTAL_FRAME_HEIGHT - 2;
  int MIN_PORTAL_INNER_WIDTH = MIN_PORTAL_FRAME_WIDTH - 2;
  int TELEPORT_TICKS = 80;

  VoxelShape X_SHAPE = Block.box(
      0.0, 0.0, 6.0, 16.0, 16.0, 10.0
  );
  VoxelShape Z_SHAPE = Block.box(
      6.0, 0.0, 0.0, 10.0, 16.0, 16.0
  );

  EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

  private static boolean isEmpty(
      BlockState state,
      Block portalBlock
  ) {
    return state.isAir() || state.is(BlockTags.FIRE) || state.is(portalBlock);
  }

  private static int getDistanceUntilEdgeAboveFrame(
      BlockGetter world,
      BlockPos pos,
      Direction direction,
      Block frameBlock,
      Block portalBlock
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (int width = 0; width <= MAX_PORTAL_INNER_WIDTH; width++) {
      mutable.set(pos).move(direction, width);
      BlockState state = world.getBlockState(mutable);
      if (!isEmpty(state, portalBlock)) {
        if (state.is(frameBlock)) return width;
        break;
      }

      BlockState belowState = world.getBlockState(mutable.move(Direction.DOWN));
      if (!belowState.is(frameBlock)) break;
    }

    return 0;
  }

  private static int getDistanceUntilTop(
      BlockGetter world,
      BlockPos bottomLeft,
      Direction rightDir,
      BlockPos.MutableBlockPos mutable,
      int width,
      int[] portalBlockCount,
      Block frameBlock,
      Block portalBlock
  ) {
    for (int height = 0; height < MAX_PORTAL_INNER_HEIGHT; height++) {
      mutable.set(bottomLeft).move(Direction.UP, height).move(rightDir, -1);
      if (!world.getBlockState(mutable).is(frameBlock)) return height;

      mutable.set(bottomLeft).move(Direction.UP, height).move(rightDir, width);
      if (!world.getBlockState(mutable).is(frameBlock)) return height;

      for (int i = 0; i < width; i++) {
        mutable.set(bottomLeft).move(Direction.UP, height).move(rightDir, i);
        BlockState state = world.getBlockState(mutable);
        if (!isEmpty(state, portalBlock)) return height;
        if (state.is(portalBlock)) portalBlockCount[0]++;
      }
    }

    return MAX_PORTAL_INNER_HEIGHT;
  }

  private static boolean hasTopFrame(
      BlockGetter world,
      BlockPos bottomLeft,
      Direction rightDir,
      BlockPos.MutableBlockPos mutable,
      int width,
      int height,
      Block frameBlock
  ) {
    for (int i = 0; i < width; i++) {
      mutable.set(bottomLeft).move(Direction.UP, height).move(rightDir, i);
      if (!world.getBlockState(mutable).is(frameBlock)) return false;
    }
    return true;
  }

  private static BlockPos calculateBottomLeft(
      BlockGetter world,
      Direction rightDir,
      BlockPos pos,
      Block frameBlock,
      Block portalBlock
  ) {
    int minY = Math.max(world.getMinY(), pos.getY() - MAX_PORTAL_INNER_HEIGHT);

    while (pos.getY() > minY && isEmpty(world.getBlockState(pos.below()), portalBlock)) {
      pos = pos.below();
    }

    Direction leftDir = rightDir.getOpposite();
    int edge = getDistanceUntilEdgeAboveFrame(
        world, pos, leftDir, frameBlock, portalBlock
    ) - 1;
    return edge < 0 ? null : pos.relative(leftDir, edge);
  }

  private static int calculateWidth(
      BlockGetter world,
      BlockPos bottomLeft,
      Direction rightDir,
      Block frameBlock,
      Block portalBlock
  ) {
    int width = getDistanceUntilEdgeAboveFrame(
        world, bottomLeft, rightDir, frameBlock, portalBlock
    );
    return width >= MIN_PORTAL_INNER_WIDTH && width <= MAX_PORTAL_INNER_WIDTH ? width : 0;
  }

  private static int calculateHeight(
      BlockGetter world,
      BlockPos bottomLeft,
      Direction rightDir,
      int width,
      int[] portalBlockCount,
      Block frameBlock,
      Block portalBlock
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    int height = getDistanceUntilTop(
        world, bottomLeft, rightDir, mutable, width, portalBlockCount, frameBlock, portalBlock
    );
    return height >= MIN_PORTAL_INNER_HEIGHT && height <= MAX_PORTAL_INNER_HEIGHT
        && hasTopFrame(world, bottomLeft, rightDir, mutable, width, height, frameBlock)
        ? height
        : 0;
  }

  private static CustomPortalShape findAnyShape(
      BlockGetter world,
      BlockPos pos,
      Direction.Axis axis,
      Block frameBlock,
      Block portalBlock
  ) {
    Direction rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
    BlockPos bottomLeft = calculateBottomLeft(world, rightDir, pos, frameBlock, portalBlock);
    if (bottomLeft == null) {
      return new CustomPortalShape(axis, rightDir, pos, 0, 0, 0, portalBlock);
    }

    int width = calculateWidth(world, bottomLeft, rightDir, frameBlock, portalBlock);
    if (width == 0) {
      return new CustomPortalShape(axis, rightDir, bottomLeft, 0, 0, 0, portalBlock);
    }

    int[] portalBlockCount = new int[]{0};
    int height = calculateHeight(
        world, bottomLeft, rightDir, width, portalBlockCount, frameBlock, portalBlock
    );
    return new CustomPortalShape(
        axis, rightDir, bottomLeft, width, height, portalBlockCount[0], portalBlock
    );
  }

  private static boolean hasAdjacentFrame(
      BlockGetter world,
      BlockPos pos,
      Block frameBlock
  ) {
    BlockPos.MutableBlockPos mutable = pos.mutable();
    for (Direction direction : Direction.values()) {
      if (world.getBlockState(mutable.set(pos).move(direction)).is(frameBlock)) return true;
    }
    return false;
  }

  private static Optional<CustomPortalShape> findPortalShape(
      BlockGetter world,
      BlockPos pos,
      java.util.function.Predicate<CustomPortalShape> isValid,
      Direction.Axis preferredAxis,
      Block frameBlock,
      Block portalBlock
  ) {
    Optional<CustomPortalShape> firstAxis = Optional.of(
        findAnyShape(world, pos, preferredAxis, frameBlock, portalBlock)
    ).filter(isValid);
    if (firstAxis.isPresent()) return firstAxis;

    Direction.Axis otherAxis = preferredAxis == Direction.Axis.X
        ? Direction.Axis.Z
        : Direction.Axis.X;
    return Optional.of(
        findAnyShape(world, pos, otherAxis, frameBlock, portalBlock)
    ).filter(isValid);
  }

  private static Direction.Axis portalAxis(
      LevelReader world,
      BlockPos portalPos
  ) {
    BlockState state = world.getBlockState(portalPos);
    return state.hasProperty(AXIS) ? state.getValue(AXIS) : Direction.Axis.X;
  }

  private static BlockPos findExistingPortal(
      ServerLevel world,
      int centerX,
      int centerZ,
      Block portalBlock
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    for (int x = -16; x <= 16; x++) {
      for (int z = -16; z <= 16; z++) {
        for (int y = world.getMinY(); y <= world.getMaxY(); y++) {
          mutable.set(centerX + x, y, centerZ + z);
          if (world.getBlockState(mutable).getBlock() == portalBlock) return mutable.immutable();
        }
      }
    }
    return null;
  }

  static BlockPos buildPortalAt(
      ServerLevel world,
      BlockPos groundPos,
      Direction.Axis axis,
      Block frameBlock,
      Block portalBlock
  ) {
    int frameWidth = MIN_PORTAL_FRAME_WIDTH;
    int frameHeight = MIN_PORTAL_FRAME_HEIGHT;
    int innerWidth = MIN_PORTAL_INNER_WIDTH;
    int innerHeight = MIN_PORTAL_INNER_HEIGHT;

    // Auto-created portals use the smallest legal frame size.
    for (int y = 0; y < frameHeight; y++) {
      for (int i = -1; i <= innerWidth; i++) {
        BlockPos current = (axis == Direction.Axis.X)
            ? groundPos.offset(i, y, 0)
            : groundPos.offset(0, y, i);
        if (y == 0 || y == frameHeight - 1 || i == -1 || i == innerWidth) {
          world.setBlockAndUpdate(current, frameBlock.defaultBlockState());
        } else {
          world.setBlockAndUpdate(current, Blocks.AIR.defaultBlockState());
        }
      }
    }

    List<BlockPos> portalPositions = new ArrayList<>();
    for (int y = 1; y <= innerHeight; y++) {
      for (int i = 0; i < innerWidth; i++) {
        BlockPos pos = (axis == Direction.Axis.X)
            ? groundPos.offset(i, y, 0)
            : groundPos.offset(0, y, i);
        portalPositions.add(pos);
        world.setBlock(pos, portalBlock.defaultBlockState().setValue(AXIS, axis), 18);
      }
    }

    for (int y = 0; y < frameHeight; y++) {
      for (int i = 0; i < innerWidth; i++) {
        BlockPos front = (axis == Direction.Axis.X)
            ? groundPos.offset(i, y, 1)
            : groundPos.offset(1, y, i);
        BlockPos back = (axis == Direction.Axis.X)
            ? groundPos.offset(i, y, -1)
            : groundPos.offset(-1, y, i);
        if (!world.getBlockState(front).isAir())
          world.setBlockAndUpdate(front, Blocks.AIR.defaultBlockState());
        if (!world.getBlockState(back).isAir())
          world.setBlockAndUpdate(back, Blocks.AIR.defaultBlockState());
      }
    }

    for (BlockPos pos : portalPositions) world.updateNeighborsAt(pos, portalBlock);
    return groundPos.above(1);
  }

  private static List<PortalSpec> portalSpecs() {
    return List.of(
        new PortalSpec(
            SupremeBlocks.SUPREME_GEM_BLOCK,
            SupremeBlocks.ORE_CONTINENT_PORTAL,
            CustomDimensions.ORE_CONTINENT
        ),
        new PortalSpec(
            SupremeBlocks.SUPREME_FODDER_BLOCK,
            SupremeBlocks.HARVEST_CONTINENT_PORTAL,
            CustomDimensions.HARVEST_CONTINENT
        )
    );
  }

  private static Optional<CustomPortalShape> findNearbyEmptyPortalShape(
      LevelAccessor world,
      BlockPos origin,
      Direction.Axis preferredAxis,
      PortalSpec spec
  ) {
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    // One-block-tall interiors such as a 4x3 frame are sensitive to which
    // interior cell the fire happens to occupy, so probe a tight neighborhood.
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        for (int z = -1; z <= 1; z++) {
          mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
          if (!isEmpty(world.getBlockState(mutable), spec.portalBlock())) continue;
          if (!hasAdjacentFrame(world, mutable, spec.frameBlock())) continue;

          Optional<CustomPortalShape> shape = findPortalShape(
              world,
              mutable.immutable(),
              customPortalShape -> customPortalShape.isValid()
                  && customPortalShape.numPortalBlocks() == 0,
              preferredAxis,
              spec.frameBlock(),
              spec.portalBlock()
          );
          if (shape.isPresent()) return shape;
        }
      }
    }

    return Optional.empty();
  }

  private static BlockPos getPortalOrigin(
      LevelReader world,
      BlockPos portalPos,
      Block frameBlock,
      Block portalBlock
  ) {
    CustomPortalShape shape = findAnyShape(
        world, portalPos, portalAxis(world, portalPos), frameBlock, portalBlock
    );
    return shape.isComplete() ? shape.bottomLeft() : portalPos;
  }

  static boolean isValidPortal(
      LevelReader world,
      BlockPos pos,
      Direction.Axis axis,
      Block frameBlock,
      Block portalBlock
  ) {
    return findAnyShape(world, pos, axis, frameBlock, portalBlock).isComplete();
  }

  static BlockPos findOrCreatePortal(
      ServerLevel targetWorld,
      BlockPos sourcePos,
      Direction.Axis axis,
      Block frameBlock,
      Block portalBlock
  ) {
    BlockPos existing = findExistingPortal(
        targetWorld,
        sourcePos.getX(),
        sourcePos.getZ(),
        portalBlock
    );
    if (existing != null) return existing;

    int surfaceY = targetWorld.getHeight(
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        sourcePos.getX(), sourcePos.getZ()
    );
    BlockPos buildPos = new BlockPos(sourcePos.getX(), surfaceY, sourcePos.getZ());
    return buildPortalAt(targetWorld, buildPos, axis, frameBlock, portalBlock);
  }

  private static Optional<CustomPortalShape> findEmptyPortalShape(
      LevelAccessor world,
      BlockPos pos,
      Direction.Axis preferredAxis
  ) {
    for (PortalSpec spec : portalSpecs()) {
      if (!spec.canIgniteIn(world)) continue;
      Optional<CustomPortalShape> shape = findNearbyEmptyPortalShape(
          world,
          pos,
          preferredAxis,
          spec
      );
      if (shape.isPresent()) return shape;
    }
    return Optional.empty();
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
    PortalLinkSavedData linkData = PortalLinkSavedData.get(server);
    PortalLinkSavedData.Endpoint sourceEndpoint = new PortalLinkSavedData.Endpoint(
        currentWorld.dimension(), getPortalOrigin(currentWorld, portalPos, frameBlock, portalBlock)
    );
    ServerLevel targetWorld = null;
    BlockPos targetPos = null;

    if (currentWorld.dimension() == sourceDim) {
      Optional<PortalLinkSavedData.Endpoint> linkedEndpoint = linkData.getDestination(sourceEndpoint);
      if (linkedEndpoint.isPresent()) {
        PortalLinkSavedData.Endpoint endpoint = linkedEndpoint.get();
        ServerLevel linkedWorld = server.getLevel(endpoint.dimension());
        if (linkedWorld != null && endpoint.dimension() == CustomDimensions.UNIVERSE_META
            && isValidPortal(linkedWorld, endpoint.pos(), portalAxis(linkedWorld, endpoint.pos()), frameBlock, portalBlock)) {
          targetWorld = linkedWorld;
          targetPos = endpoint.pos();
        } else {
          linkData.unlink(sourceEndpoint);
        }
      }
    }

    if (targetWorld == null) {
      targetWorld = currentWorld.dimension() == sourceDim
          ? server.getLevel(targetDim)
          : server.getLevel(sourceDim);
    }
    if (targetWorld == null) return;

    if (targetPos == null) {
      targetPos = findOrCreatePortal(
          targetWorld,
          portalPos,
          portalAxis(currentWorld, portalPos),
          frameBlock,
          portalBlock
      );

      if (currentWorld.dimension() == CustomDimensions.UNIVERSE_META) {
        PortalLinkSavedData.Endpoint targetEndpoint = new PortalLinkSavedData.Endpoint(
            targetWorld.dimension(), getPortalOrigin(targetWorld, targetPos, frameBlock, portalBlock)
        );
        linkData.link(sourceEndpoint, targetEndpoint);
      }
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

  static boolean canBePlacedAt(
      Level world,
      BlockPos pos,
      Direction forwardDirection
  ) {
    BlockState state = world.getBlockState(pos);
    if (!state.isAir()) return false;
    Direction.Axis preferredAxis = forwardDirection.getAxis().isHorizontal()
        ? forwardDirection.getCounterClockWise().getAxis()
        : Direction.Plane.HORIZONTAL.getRandomAxis(world.getRandom());
    return findEmptyPortalShape(world, pos, preferredAxis).isPresent();
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

    return !(currentAxis != neighborAxis
        && neighborAxis.isHorizontal())
        && !neighborState.is(portalBlock)
        && !isValidPortal(world, pos, currentAxis, frameBlock, portalBlock);
  }

  static boolean tryCreatePortalFromFire(
      LevelAccessor world,
      BlockPos pos
  ) {
    Optional<CustomPortalShape> optionalShape = findEmptyPortalShape(
        world, pos, Direction.Axis.X
    );
    if (optionalShape.isEmpty()) return false;

    optionalShape.get().createPortalBlocks(world);
    return true;
  }

  record CustomPortalShape(
      Direction.Axis axis,
      Direction rightDir,
      BlockPos bottomLeft,
      int width,
      int height,
      int numPortalBlocks,
      Block portalBlock
  ) {

    boolean isValid() {
      return this.width >= MIN_PORTAL_INNER_WIDTH && this.width <= MAX_PORTAL_INNER_WIDTH
          && this.height >= MIN_PORTAL_INNER_HEIGHT && this.height <= MAX_PORTAL_INNER_HEIGHT;
    }

    void createPortalBlocks(
        LevelAccessor world
    ) {
      BlockState portalState = this.portalBlock.defaultBlockState().setValue(AXIS, this.axis);
      BlockPos.betweenClosed(
          this.bottomLeft,
          this.bottomLeft.relative(Direction.UP, this.height - 1)
              .relative(this.rightDir, this.width - 1)
      ).forEach(pos -> world.setBlock(pos, portalState, 18));
    }

    boolean isComplete() {
      return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

  }

  record PortalSpec(
      Block frameBlock,
      Block portalBlock,
      ResourceKey<Level> portalDimension
  ) {

    boolean canIgniteIn(
        LevelAccessor world
    ) {
      if (!(world instanceof Level level)) return false;
      return level.dimension() == Level.OVERWORLD
          || level.dimension() == CustomDimensions.UNIVERSE_META
          || level.dimension() == this.portalDimension;
    }

  }

}
