/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer.universe;

// Java Standard
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

// Mojang
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

// Minecraft
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public final class UniverseGuardianTexture extends ReloadableTexture implements TickableTexture {

  private int elapsed;
  private int frame;

  private Animation animation;
  private volatile Animation pendingAnimation;

  public UniverseGuardianTexture(
      Identifier resourceId
  ) {
    super(resourceId);
  }

  @Override
  public void apply(
      TextureContents contents
  ) {
    Animation prepared = pendingAnimation;
    pendingAnimation = null;
    try {
      super.apply(contents);
    } catch (RuntimeException exception) {
      if (prepared != null) prepared.close();
      throw exception;
    }
    animation = prepared;
    frame = 0;
    elapsed = 0;
  }

  @Override
  public void close() {
    if (animation != null) {
      animation.close();
      animation = null;
    }
    super.close();
  }

  @Override
  public TextureContents loadContents(
      ResourceManager resourceManager
  ) throws IOException {
    Resource resource = resourceManager.getResourceOrThrow(resourceId());
    AnimationMetadataSection metadata = resource.metadata().getSection(AnimationMetadataSection.TYPE).orElse(null);
    TextureMetadataSection textureMetadata = resource.metadata().getSection(TextureMetadataSection.TYPE).orElse(null);
    if (metadata == null) {
      pendingAnimation = null;
      return TextureContents.load(resourceManager, resourceId());
    }
    NativeImage sheet;
    try (var input = resource.open()) {
      sheet = NativeImage.read(input);
    }
    FrameSize size = metadata.calculateFrameSize(sheet.getWidth(), sheet.getHeight());
    int columns = sheet.getWidth() / size.width();
    int rows = sheet.getHeight() / size.height();
    List<AnimationFrame> frames = metadata.frames().orElseGet(() ->
        IntStream.range(0, columns * rows).mapToObj(AnimationFrame::new).toList());
    if (sheet.getWidth() % size.width() != 0 || sheet.getHeight() % size.height() != 0
        || frames.isEmpty() || frames.stream().anyMatch(entry -> entry.index() < 0 || entry.index() >= columns * rows)) {
      sheet.close();
      throw new IOException("Invalid universe guardian animation frames");
    }
    NativeImage pixels = new NativeImage(size.width(), size.height(), false);
    Animation prepared = new Animation(sheet, pixels, frames, columns, metadata.defaultFrameTime());
    prepared.copyFrame(0);
    pendingAnimation = prepared;
    // ReloadableTexture consumes this copy; retain the frame buffer for later ticks.
    return new TextureContents(pixels.mappedCopy(color -> color), textureMetadata);
  }

  @Override
  public void tick() {
    if (animation == null || animation.frames().size() < 2 || texture == null) return;
    if (++elapsed < animation.frames().get(frame).timeOr(animation.frameTime())) return;
    elapsed = 0;
    frame = (frame + 1) % animation.frames().size();
    animation.copyFrame(frame);
    RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, animation.pixels());
  }

  private record Animation(
      NativeImage sheet, NativeImage pixels,
      List<AnimationFrame> frames,
      int columns, int frameTime
  ) {

    private void close() {
      pixels.close();
      sheet.close();
    }

    private void copyFrame(
        int frame
    ) {
      int index = frames.get(frame).index();
      int startX = (index % columns) * pixels.getWidth();
      int startY = (index / columns) * pixels.getHeight();
      for (int y = 0; y < pixels.getHeight(); y++) {
        for (int x = 0; x < pixels.getWidth(); x++) {
          pixels.setPixel(x, y, sheet.getPixel(startX + x, startY + y));
        }
      }
    }

  }

}
