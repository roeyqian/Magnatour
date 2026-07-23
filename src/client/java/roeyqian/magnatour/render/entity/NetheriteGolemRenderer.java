/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.render.entity;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.entity.supreme.NetheriteGolem;
import roeyqian.magnatour.render.entity.model.CustomGolemModel;
import roeyqian.magnatour.render.entity.state.CustomGolemRenderState;

@Environment(EnvType.CLIENT)
public final class NetheriteGolemRenderer extends MobRenderer<NetheriteGolem, CustomGolemRenderState, CustomGolemModel> {

  public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

  private static final Identifier GOLEM_LOCATION = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "textures/entity/custom_golem/netherite.png"
  );

  private final BlockModelResolver blockModelResolver;

  public NetheriteGolemRenderer(
      final EntityRendererProvider.Context context
  ) {
    super(context, new CustomGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
    this.blockModelResolver = context.getBlockModelResolver();
  }

  public CustomGolemRenderState createRenderState() {
    return new CustomGolemRenderState();
  }

  public void extractRenderState(
      final NetheriteGolem entity,
      final CustomGolemRenderState state,
      final float partialTicks
  ) {
    super.extractRenderState(entity, state, partialTicks);
    state.attackTicksRemaining = (float) entity.getAttackAnimationTick() > 0.0F
        ? (float) entity.getAttackAnimationTick() - partialTicks
        : 0.0F;
    state.offerFlowerTick = entity.getOfferFlowerTick();
    if (state.offerFlowerTick > 0) {
      this.blockModelResolver.update(state.flowerBlock, Blocks.POPPY.defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
    } else {
      state.flowerBlock.clear();
    }

    state.crackiness = entity.getCrackiness();
  }

  @NonNull
  public Identifier getTextureLocation(
      final CustomGolemRenderState state
  ) {
    return GOLEM_LOCATION;
  }

  protected void setupRotations(
      final CustomGolemRenderState state,
      final @NonNull PoseStack poseStack,
      final float bodyRot,
      final float entityScale
  ) {
    super.setupRotations(state, poseStack, bodyRot, entityScale);
    if (!((double) state.walkAnimationSpeed < 0.01)) {
      float p = 13.0F;
      float wp = state.walkAnimationPos + 6.0F;
      float triangleWave = (Math.abs(wp % 13.0F - 6.5F) - 3.25F) / 3.25F;
      poseStack.mulPose(Axis.ZP.rotationDegrees(6.5F * triangleWave));
    }
  }

}
