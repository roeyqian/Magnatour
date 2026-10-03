/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer.supreme;

// Mojang
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
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
import roeyqian.magnatour.model.supreme.CustomGolemModel;
import roeyqian.magnatour.registry.output.RegEntityLayers;
import roeyqian.magnatour.renderstate.supreme.CustomGolemRenderState;

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
    super(context, new CustomGolemModel(context.bakeLayer(RegEntityLayers.NETHERITE_GOLEM)), 0.7F * NetheriteGolem.SIZE_SCALE);
    this.blockModelResolver = context.getBlockModelResolver();
  }

  @NonNull public CustomGolemRenderState createRenderState() {
    return new CustomGolemRenderState();
  }

  public void extractRenderState(
      final @NonNull NetheriteGolem entity,
      final @NonNull CustomGolemRenderState state,
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
      final @NonNull CustomGolemRenderState state
  ) {
    return GOLEM_LOCATION;
  }

  @Override
  protected void scale(
      final @NonNull CustomGolemRenderState state,
      final @NonNull PoseStack poseStack
  ) {
    super.scale(state, poseStack);
    poseStack.scale(NetheriteGolem.SIZE_SCALE, NetheriteGolem.SIZE_SCALE, NetheriteGolem.SIZE_SCALE);
  }

  protected void setupRotations(
      final @NonNull CustomGolemRenderState state,
      final @NonNull PoseStack poseStack,
      final float bodyRot,
      final float entityScale
  ) {
    super.setupRotations(state, poseStack, bodyRot, entityScale);
    if (!((double) state.walkAnimationSpeed < 0.01)) {
      float p = 13.0F;
      float wp = state.walkAnimationPos + 6.0F;
      float triangleWave = (Math.abs(wp % 13.0F - 6.5F) - 3.25F) / 3.25F;
      poseStack.rotateDegrees(Axis.ZP, 6.5F * triangleWave);
    }
  }

}
