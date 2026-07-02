/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.utility.registry.output;

// Java Standard
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

// Fabric
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

// Magnatour
import roeyqian.magnatour.Magnatour;
import roeyqian.magnatour.block.insert.portal.HarvestContinentPortal;
import roeyqian.magnatour.block.insert.portal.OreContinentPortal;
import roeyqian.magnatour.utility.registry.block.RegInsertBlocks;
import roeyqian.magnatour.block.CustomPortalBlock;

@Environment(EnvType.CLIENT)
public final class RegBlockLayers {

  public static final Identifier HARVEST_PORTAL_OVERLAY_ID = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "harvest_continent_portal_overlay"
  );
  public static final Identifier ORE_PORTAL_OVERLAY_ID = Identifier.fromNamespaceAndPath(
      Magnatour.MOD_ID, "ore_continent_portal_overlay"
  );

  private RegBlockLayers() {}

  public static void init() {
    registerPortalOverlay(
        ORE_PORTAL_OVERLAY_ID,
        RegInsertBlocks.ORE_CONTINENT_PORTAL,
        () -> OreContinentPortal.clientPortalTicks,
        ticks -> OreContinentPortal.clientPortalTicks = ticks
    );
    registerPortalOverlay(
        HARVEST_PORTAL_OVERLAY_ID,
        RegInsertBlocks.HARVEST_CONTINENT_PORTAL,
        () -> HarvestContinentPortal.clientPortalTicks,
        ticks -> HarvestContinentPortal.clientPortalTicks = ticks
    );

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (client.player == null) {
        OreContinentPortal.clientPortalTicks = 0;
        HarvestContinentPortal.clientPortalTicks = 0;
        return;
      }

      updatePortalTicks(
          () -> isPlayerInPortalType(client.player, OreContinentPortal.class),
          OreContinentPortal.clientPortalTicks,
          ticks -> OreContinentPortal.clientPortalTicks = ticks
      );
      updatePortalTicks(
          () -> isPlayerInPortalType(client.player, HarvestContinentPortal.class),
          HarvestContinentPortal.clientPortalTicks,
          ticks -> HarvestContinentPortal.clientPortalTicks = ticks
      );
    });

    Magnatour.LOGGER.info("[Client] Initializing 'RegBlockLayers'");
  }

  public static void renderPortalOverlay(
      GuiGraphicsExtractor context,
      float strength,
      Minecraft client,
      Block portalBlock
  ) {
    float alpha = strength;
    if (alpha < 1.0f) {
      alpha *= alpha;
      alpha *= alpha;
      alpha = alpha * 0.8f + 0.2f;
    }

    int width = context.guiWidth();
    int height = context.guiHeight();

    Material.Baked particleMaterial = client
        .getModelManager()
        .getBlockStateModelSet()
        .getParticleMaterial(portalBlock.defaultBlockState());
    TextureAtlasSprite sprite = particleMaterial.sprite();

    int color = ARGB.white(alpha);
    context.blitSprite(
        RenderPipelines.GUI_TEXTURED,
        sprite, 0, 0, width, height, color
    );
  }

  private static void registerPortalOverlay(
      Identifier overlayId,
      Block portalBlock,
      IntSupplier getTicks,
      IntConsumer setTicks
  ) {
    HudElementRegistry.attachElementAfter(
        VanillaHudElements.MISC_OVERLAYS,
        overlayId,
        (guiGraphics, _) -> {
          int ticks = getTicks.getAsInt();
          if (ticks <= 0) return;

          Minecraft client = Minecraft.getInstance();
          if (client.player == null) return;
          if (client.player.isCreative()) {
            setTicks.accept(0);
            return;
          }

          float progress = (float) ticks / CustomPortalBlock.TELEPORT_TICKS;
          renderPortalOverlay(guiGraphics, progress, client, portalBlock);
        }
    );
  }

  private static void updatePortalTicks(
      BooleanSupplier isInPortal,
      int currentTicks,
      IntConsumer setTicks
  ) {
    int maxTicks = CustomPortalBlock.TELEPORT_TICKS;
    if (isInPortal.getAsBoolean()) {
      if (currentTicks < maxTicks) setTicks.accept(currentTicks + 1);
    } else {
      if (currentTicks > 0) setTicks.accept(currentTicks - 1);
    }
  }

  private static boolean isPlayerInPortalType(
      LocalPlayer player,
      Class<? extends Block> portalClass
  ) {
    AABB box = player.getBoundingBox().inflate(0.1);
    Level level = player.level();

    int minX = Mth.floor(box.minX);
    int minY = Mth.floor(box.minY);
    int minZ = Mth.floor(box.minZ);
    int maxX = Mth.floor(box.maxX);
    int maxY = Mth.floor(box.maxY);
    int maxZ = Mth.floor(box.maxZ);

    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          pos.set(x, y, z);
          if (portalClass.isInstance(level.getBlockState(pos).getBlock())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
