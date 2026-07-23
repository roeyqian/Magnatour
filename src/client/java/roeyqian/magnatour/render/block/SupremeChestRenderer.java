/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.render.block;

// Java Standard
import java.util.List;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.supreme.SupremeChest;
import roeyqian.magnatour.blockentity.supreme.SupremeChestEntity;
import roeyqian.magnatour.render.block.state.SupremeChestRenderState;
import roeyqian.magnatour.registry.output.RegEntityLayers;

@Environment(EnvType.CLIENT)
public final class SupremeChestRenderer implements BlockEntityRenderer<SupremeChestEntity, SupremeChestRenderState> {

  private static final SpriteId SUPREME_CHEST_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme")
  );
  private static final SpriteId SUPREME_CHEST_S_LEFT_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_s_left")
  );
  private static final SpriteId SUPREME_CHEST_S_RIGHT_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_s_right")
  );
  private static final SpriteId SUPREME_CHEST_TI_LEFT_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_ti_left")
  );
  private static final SpriteId SUPREME_CHEST_TI_MIDDLE_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_ti_middle")
  );
  private static final SpriteId SUPREME_CHEST_TI_RIGHT_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "supreme_ti_right")
  );

  private final ChestModel tripleMiddleModel;

  private final SpriteGetter sprites;

  private final MultiblockChestResources<ChestModel> models;

  public SupremeChestRenderer(
      BlockEntityRendererProvider.Context ctx
  ) {
    this.models = new MultiblockChestResources<>(
        new ChestModel(ctx.bakeLayer(ModelLayers.CHEST)),
        new ChestModel(ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT)),
        new ChestModel(ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT))
    );
    this.tripleMiddleModel = new ChestModel(ctx.bakeLayer(RegEntityLayers.SUPREME_CHEST_TRIPLE_MIDDLE));
    this.sprites = ctx.sprites();
  }

  @Override
  public SupremeChestRenderState createRenderState() {
    return new SupremeChestRenderState();
  }

  @Override
  public void extractRenderState(
      SupremeChestEntity blockEntity,
      SupremeChestRenderState state,
      float partialTicks,
      @NonNull Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
  ) {
    BlockEntityRenderer.super.extractRenderState(
        blockEntity, state, partialTicks, cameraPosition, breakProgress
    );

    BlockState blockState = blockEntity.getBlockState();
    if (blockState.hasProperty(SupremeChest.FACING)) {
      state.facing = blockState.getValue(SupremeChest.FACING);
    }
    if (blockState.hasProperty(SupremeChest.TYPE)) {
      state.type = blockState.getValue(SupremeChest.TYPE);
    }

    state.lidProgress = blockEntity.getLevel() == null
        ? blockEntity.getAnimationProgress(partialTicks)
        : SupremeChest.getOpenNess(blockEntity.getLevel(), blockEntity.getBlockPos(), partialTicks);

    state.connectedChestCount = 1;
    state.connectedChestIndex = 0;
    if (blockEntity.getLevel() != null) {
      List<SupremeChestEntity> connectedChests = SupremeChest.getConnectedChestsForRender(
          blockEntity.getLevel(),
          blockEntity.getBlockPos()
      );
      state.connectedChestCount = connectedChests.size();
      state.connectedChestIndex = connectedChests.indexOf(blockEntity);
      if (state.connectedChestIndex < 0) {
        state.connectedChestIndex = 0;
      }
    }
  }

  @Override
  public void submit(
      SupremeChestRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      @NonNull CameraRenderState camera
  ) {
    poseStack.pushPose();

    Transformation transformation = ChestRenderer.modelTransformation(state.facing);
    poseStack.mulPose(transformation);

    float open = state.lidProgress;
    open = 1.0F - open;
    open = 1.0F - open * open * open;

    collector.submitModel(
        selectModel(state),
        open,
        poseStack,
        state.lightCoords,
        OverlayTexture.NO_OVERLAY,
        -1,
        getSprite(state),
        this.sprites,
        0,
        state.breakProgress
    );

    poseStack.popPose();
  }

  private static SpriteId getSprite(
      SupremeChestRenderState state
  ) {
    if (state.connectedChestCount == 3) {
      return switch (state.connectedChestIndex) {
        case 0 -> SUPREME_CHEST_TI_LEFT_SPRITE;
        case 1 -> SUPREME_CHEST_TI_MIDDLE_SPRITE;
        case 2 -> SUPREME_CHEST_TI_RIGHT_SPRITE;
        default -> SUPREME_CHEST_SPRITE;
      };
    }

    ChestType type = state.type;
    return switch (type) {
      case LEFT -> SUPREME_CHEST_S_LEFT_SPRITE;
      case RIGHT -> SUPREME_CHEST_S_RIGHT_SPRITE;
      case SINGLE -> SUPREME_CHEST_SPRITE;
    };
  }

  private ChestModel selectModel(
      SupremeChestRenderState state
  ) {
    if (state.connectedChestCount == 3 && state.connectedChestIndex == 1) {
      return this.tripleMiddleModel;
    }

    return this.models.select(state.type);
  }

}
