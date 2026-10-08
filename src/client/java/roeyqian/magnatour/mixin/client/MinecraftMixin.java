/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixin.client;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.SectionPos;

// SpongePowered Mixin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Magnatour
import roeyqian.magnatour.block.VirtualBlockLightManager;
import roeyqian.magnatour.mixinhelper.client.ClientHelperForEquipment;

@Mixin(value = Minecraft.class, priority = 3600000)
public class MinecraftMixin {

  @Inject(method = "runTick", at = @At("HEAD"))
  private void inRunTick(
      boolean renderLevel,
      CallbackInfo ci
  ) {
    Minecraft client = (Minecraft) (Object) this;
    if (client.level == null) return;
    var world = client.level;
    var viewArea = client.levelRenderer.viewArea();
    VirtualBlockLightManager.finishUniverseBuilds(world, (key, started) -> {
      var section = viewArea == null ? null : viewArea.getRenderSectionAt(SectionPos.of(key).center());
      if (section == null) return true;
      var mesh = section.getSectionMesh();
      // Meshes become current only after upload. Ignore results from older compile tasks.
      return mesh == CompiledSectionMesh.UNCOMPILED || mesh == CompiledSectionMesh.EMPTY
          || mesh.getCompileTaskStartTime() >= started;
    });
    var regions = new RenderRegionCache();
    VirtualBlockLightManager.submitUniverseRefreshes(world, section -> {
      var terrain = viewArea == null ? null : viewArea.getRenderSectionAt(section.center());
      if (terrain == null) return;
      var mesh = terrain.getSectionMesh();
      // Preserve vanilla initial terrain loading. Empty terrain has no old geometry to refresh.
      if (mesh == CompiledSectionMesh.UNCOMPILED || mesh == CompiledSectionMesh.EMPTY) return;
      VirtualBlockLightManager.beginUniverseBuild(world, section.asLong());
      terrain.compileAsync(regions.createRegion(world, section.asLong()));
    });
  }

  @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
  private void inStartAttack(
      CallbackInfoReturnable<Boolean> cir
  ) {
    ClientHelperForEquipment.handleStartAttack((Minecraft) (Object) this, cir);
  }

}
