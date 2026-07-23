/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Minecraft
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;

// Magnatour
import roeyqian.magnatour.Magnatour;

@Environment(EnvType.CLIENT)
public final class GlintRenderTypes {

  public static final Identifier RAINBOW_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/misc/universe_glint.png"
  );
  public static final Identifier SUPREME_GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID,
      "textures/misc/supreme_glint.png"
  );

  // Armor glint (worn equipment layers) — mirrors RenderTypes.armorEntityGlint()
  public static final RenderType ARMOR_ENTITY_GLINT = RenderType.create(
      "universe_armor_entity_glint",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", RAINBOW_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
          .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
          .createRenderSetup()
  );
  // Entity glint (held / dropped items in world) — mirrors RenderTypes.entityGlint()
  public static final RenderType ENTITY_GLINT = RenderType.create(
      "universe_entity_glint",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", RAINBOW_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
          .createRenderSetup()
  );
  // Item glint (GUI / sheeted item rendering) — mirrors RenderTypes.glint()
  public static final RenderType GLINT = RenderType.create(
      "universe_glint",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", RAINBOW_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.GLINT_TEXTURING)
          .createRenderSetup()
  );
  // Translucent item glint (shader transparency, item-entity target) — mirrors RenderTypes.glintTranslucent()
  public static final RenderType GLINT_TRANSLUCENT = RenderType.create(
      "universe_glint_translucent",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", RAINBOW_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.GLINT_TEXTURING)
          .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
          .createRenderSetup()
  );
  public static final RenderType SUPREME_ARMOR_ENTITY_GLINT = RenderType.create(
      "supreme_armor_entity_glint",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", SUPREME_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
          .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
          .createRenderSetup()
  );
  public static final RenderType SUPREME_ENTITY_GLINT = RenderType.create(
      "supreme_entity_glint",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", SUPREME_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
          .createRenderSetup()
  );
  public static final RenderType SUPREME_GLINT = RenderType.create(
      "supreme_glint",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", SUPREME_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.GLINT_TEXTURING)
          .createRenderSetup()
  );
  public static final RenderType SUPREME_GLINT_TRANSLUCENT = RenderType.create(
      "supreme_glint_translucent",
      RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", SUPREME_GLINT_TEXTURE)
          .setTextureTransform(TextureTransform.GLINT_TEXTURING)
          .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
          .createRenderSetup()
  );

  public static final RenderType[] ITEM_GLINT_TYPES = {
      GLINT, GLINT_TRANSLUCENT, ENTITY_GLINT,
      SUPREME_GLINT, SUPREME_GLINT_TRANSLUCENT, SUPREME_ENTITY_GLINT
  };

  private GlintRenderTypes() {}

}
