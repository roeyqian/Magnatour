/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.render.block;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
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
import net.minecraft.world.phys.Vec3;

// JSpecify
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.universe.UniverseLibrary;
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;
import roeyqian.magnatour.render.block.state.UniverseLibraryRenderState;

@Environment(EnvType.CLIENT)
public final class UniverseLibraryRenderer implements BlockEntityRenderer<UniverseLibraryEntity, UniverseLibraryRenderState> {

  private static final SpriteId UNIVERSE_LIBRARY_SPRITE = Sheets.CHEST_MAPPER.apply(
      Identifier.fromNamespaceAndPath(Magnatour.MOD_ID, "universe")
  );

  private final ChestModel model;

  private final SpriteGetter sprites;

  public UniverseLibraryRenderer(
      BlockEntityRendererProvider.Context ctx
  ) {
    this.model = new ChestModel(ctx.bakeLayer(ModelLayers.CHEST));
    this.sprites = ctx.sprites();
  }

  @Override
  public UniverseLibraryRenderState createRenderState() {
    return new UniverseLibraryRenderState();
  }

  @Override
  public void extractRenderState(
      UniverseLibraryEntity blockEntity,
      UniverseLibraryRenderState state,
      float partialTicks,
      @NonNull Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
  ) {
    BlockEntityRenderer.super.extractRenderState(
        blockEntity, state, partialTicks, cameraPosition, breakProgress);

    BlockState blockState = blockEntity.getBlockState();
    if (blockState.hasProperty(UniverseLibrary.FACING)) {
      state.facing = blockState.getValue(UniverseLibrary.FACING);
    }

    state.lidProgress = blockEntity.getAnimationProgress(partialTicks);
  }

  @Override
  public void submit(
      UniverseLibraryRenderState state,
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
        this.model,
        open,
        poseStack,
        state.lightCoords,
        OverlayTexture.NO_OVERLAY,
        -1,
        UNIVERSE_LIBRARY_SPRITE,
        this.sprites,
        0,
        state.breakProgress
    );

    poseStack.popPose();
  }

}
