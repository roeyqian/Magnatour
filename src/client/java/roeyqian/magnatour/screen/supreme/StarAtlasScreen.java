/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.screen.supreme;

// Java Standard
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

// Mojang
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;

// Fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.item.supreme.StarAtlas;

public class StarAtlasScreen extends Screen {

  private static final double WORLD_SPAN_MULTIPLIER = 4.0;

  private static final Identifier MAP_TEXTURE = Identifier.fromNamespaceAndPath("magnatour", "star_atlas_view");
  private static final Identifier PANEL_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
  private static final Identifier PLAYER_TEXTURE = Identifier.withDefaultNamespace("textures/map/decorations/player.png");

  private int left;
  private int mapHeight;
  private int mapWidth;
  private int top;
  private int windowHeight;
  private int windowWidth;

  private long nextMapRefresh;
  private long renderedRevision = -1;

  private double centerX;
  private double centerZ;
  private double zoom = 1.0;

  private boolean dirty = true;
  private boolean dragging;

  private final AtlasMap map;

  private NativeImage image;

  private final ClientLevel level;

  private DynamicTexture texture;

  private StarAtlasScreen(
      AtlasMap map,
      ClientLevel level
  ) {
    super(Component.translatable("gui.magnatour.star_atlas.title"));
    this.map = map;
    this.level = level;
    map.open();
    centerOnPlayer();
  }

  public static void initClient() { AtlasMap.init(); }

  @Override
  public void extractRenderState(
      @NonNull GuiGraphicsExtractor graphics,
      int mouseX,
      int mouseY,
      float delta
  ) {
    if (minecraft.level != level || minecraft.player == null || image == null) return;
    long now = System.nanoTime();
    if (dirty || (renderedRevision != map.revision() && now >= nextMapRefresh)) {
      map.copyTo(image, centerX, centerZ, zoom / WORLD_SPAN_MULTIPLIER);
      texture.upload();
      renderedRevision = map.revision();
      nextMapRefresh = now;
      dirty = false;
    }
    extractPanel(graphics);
    graphics.text(font, title, left + 8, top + 7, 0xFF404040, false);
    String dimension = level.dimension().identifier() + " · Y: " + Math.max(level.getMinY(), Math.min(level.getMaxY(), 64));
    graphics.text(font, font.plainSubstrByWidth(dimension, windowWidth - 80),
        left + 8, top + 19, 0xFF606060, false);
    graphics.text(font, "N ↑", left + windowWidth - 36, top + 7, 0xFF404040, false);
    // The same recessed light/dark edge used by vanilla inventory slots.
    graphics.fill(left + 7, top + 31, left + 9 + mapWidth, top + 33 + mapHeight, 0xFF373737);
    graphics.fill(left + 8, top + 32, left + 9 + mapWidth, top + 33 + mapHeight, 0xFFFFFFFF);
    graphics.blit(RenderPipelines.GUI_TEXTURED, MAP_TEXTURE, left + 8, top + 32,
        0, 0, mapWidth, mapHeight, mapWidth, mapHeight);
    int playerX = left + 8 + (int) Math.round(mapWidth / 2.0 + (minecraft.player.getX() - centerX) * zoom / WORLD_SPAN_MULTIPLIER);
    int playerZ = top + 32 + (int) Math.round(mapHeight / 2.0 + (minecraft.player.getZ() - centerZ) * zoom / WORLD_SPAN_MULTIPLIER);
    graphics.enableScissor(left + 8, top + 32, left + 8 + mapWidth, top + 32 + mapHeight);
    graphics.pose().pushMatrix();
    graphics.pose().translate(playerX, playerZ);
    graphics.pose().rotate((float) Math.toRadians(minecraft.player.getYRot() + 180.0F));
    graphics.blit(RenderPipelines.GUI_TEXTURED, PLAYER_TEXTURE, -4, -4, 0, 0, 8, 8, 8, 8);
    graphics.pose().popMatrix();
    graphics.disableScissor();
    int coordinateX = (int) Math.floor(overMap(mouseX, mouseY)
        ? centerX + (mouseX - left - 8 - mapWidth / 2.0) * WORLD_SPAN_MULTIPLIER / zoom : minecraft.player.getX());
    int coordinateZ = (int) Math.floor(overMap(mouseX, mouseY)
        ? centerZ + (mouseY - top - 32 - mapHeight / 2.0) * WORLD_SPAN_MULTIPLIER / zoom : minecraft.player.getZ());
    graphics.text(font, font.plainSubstrByWidth(map.biomeAt(coordinateX, coordinateZ).getString(), mapWidth),
        left + 8, top + windowHeight - 60, 0xFF404040, false);
    graphics.text(font, "X: " + coordinateX + "  Z: " + coordinateZ + "   " + zoom + "×",
        left + 8, top + windowHeight - 49, 0xFF404040, false);
    graphics.text(font, font.plainSubstrByWidth(Component.translatable("gui.magnatour.star_atlas.controls")
        .getString(), mapWidth), left + 8, top + windowHeight - 39, 0xFF606060, false);
    super.extractRenderState(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public boolean keyPressed(
      @NonNull KeyEvent event
  ) {
    if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_E
        || minecraft.options.keyInventory.matches(event)) {
      onClose();
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean mouseClicked(
      @NonNull MouseButtonEvent event,
      boolean doubled
  ) {
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overMap(event.x(), event.y())) {
      dragging = true;
      setDragging(true);
      setFocused(null);
      return true;
    }
    return super.mouseClicked(event, doubled);
  }

  @Override
  public boolean mouseDragged(
      @NonNull MouseButtonEvent event,
      double offsetX,
      double offsetY
  ) {
    if (dragging && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
      centerX -= offsetX * WORLD_SPAN_MULTIPLIER / zoom;
      centerZ -= offsetY * WORLD_SPAN_MULTIPLIER / zoom;
      dirty = true;
      return true;
    }
    return super.mouseDragged(event, offsetX, offsetY);
  }

  @Override
  public boolean mouseReleased(
      @NonNull MouseButtonEvent event
  ) {
    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && dragging) {
      dragging = false;
      setDragging(false);
      return true;
    }
    return super.mouseReleased(event);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX,
      double mouseY,
      double horizontal,
      double vertical
  ) {
    if (!overMap(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    double offsetX = mouseX - left - 8 - mapWidth / 2.0;
    double offsetZ = mouseY - top - 32 - mapHeight / 2.0;
    double oldZoom = zoom;
    setZoom(zoom * Math.pow(2, vertical));
    centerX += WORLD_SPAN_MULTIPLIER * (offsetX / oldZoom - offsetX / zoom);
    centerZ += WORLD_SPAN_MULTIPLIER * (offsetZ / oldZoom - offsetZ / zoom);
    return true;
  }

  @Override
  public void removed() {
    map.close();
    releaseTexture();
  }

  @Override
  public void tick() {
    if (minecraft.level != level || minecraft.player == null) {
      onClose();
      return;
    }
    map.requestView(centerX, centerZ, zoom / WORLD_SPAN_MULTIPLIER, mapWidth, mapHeight);
  }

  @Override
  protected void init() {
    releaseTexture();
    dragging = false;
    setDragging(false);
    windowWidth = Math.min(360, Math.max(120, width - 24));
    windowHeight = Math.min(260, Math.max(120, height - 24));
    left = (width - windowWidth) / 2;
    top = (height - windowHeight) / 2;
    mapWidth = windowWidth - 16;
    mapHeight = windowHeight - 98;
    image = new NativeImage(mapWidth, mapHeight, false);
    texture = new DynamicTexture(() -> "Magnatour star atlas", image);
    minecraft.getTextureManager().register(MAP_TEXTURE, texture);
    dirty = true;
    addRenderableWidget(Button.builder(Component.translatable("gui.magnatour.star_atlas.center"),
        button -> centerOnPlayer()).bounds(left + 8, top + windowHeight - 28, 92, 20).build());
    addRenderableWidget(Button.builder(Component.literal("−"), button -> setZoom(zoom / 2))
        .bounds(left + windowWidth - 56, top + windowHeight - 28, 22, 20).build());
    addRenderableWidget(Button.builder(Component.literal("+"), button -> setZoom(zoom * 2))
        .bounds(left + windowWidth - 30, top + windowHeight - 28, 22, 20).build());
  }

  private void centerOnPlayer() {
    var player = net.minecraft.client.Minecraft.getInstance().player;
    if (player != null) {
      centerX = player.getX();
      centerZ = player.getZ();
      dirty = true;
    }
  }

  private void panelPart(
      GuiGraphicsExtractor graphics,
      int x,
      int y,
      int partWidth,
      int partHeight,
      int u,
      int v,
      int sourceWidth,
      int sourceHeight
  ) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE, left + x, top + y, u, v,
        partWidth, partHeight, sourceWidth, sourceHeight, 256, 256);
  }

  private void extractPanel(
      GuiGraphicsExtractor graphics
  ) {
    // Reuse the unmodified vanilla chest frame, stretching only its flat edges.
    graphics.fill(left + 7, top + 7, left + windowWidth - 7, top + windowHeight - 7, 0xFFC6C6C6);
    panelPart(graphics, 0, 0, 7, 7, 0, 0, 7, 7);
    panelPart(graphics, 7, 0, windowWidth - 14, 7, 7, 0, 162, 7);
    panelPart(graphics, windowWidth - 7, 0, 7, 7, 169, 0, 7, 7);
    panelPart(graphics, 0, 7, 7, windowHeight - 14, 0, 7, 7, 1);
    panelPart(graphics, windowWidth - 7, 7, 7, windowHeight - 14, 169, 7, 7, 1);
    panelPart(graphics, 0, windowHeight - 7, 7, 7, 0, 215, 7, 7);
    panelPart(graphics, 7, windowHeight - 7, windowWidth - 14, 7, 7, 215, 162, 7);
    panelPart(graphics, windowWidth - 7, windowHeight - 7, 7, 7, 169, 215, 7, 7);
  }

  private boolean overMap(
      double x,
      double y
  ) {
    return x >= left + 8 && x < left + 8 + mapWidth && y >= top + 32 && y < top + 32 + mapHeight;
  }

  private void releaseTexture() {
    if (texture != null) {
      minecraft.getTextureManager().release(MAP_TEXTURE);
      texture = null;
      image = null;
    }
  }

  private void setZoom(
      double value
  ) {
    zoom = Math.max(0.125, Math.min(4.0, value));
    dirty = true;
  }

  private static final class AtlasMap {

    private static AtlasMap active;

    private long nextRequest;
    private long revision;

    private final Map<Long, int[]> visible = new HashMap<>();

    private final ClientLevel level;

    private StarAtlas.Payload lastView;

    private final Registry<Biome> biomes;

    private AtlasMap(
        ClientLevel level
    ) {
      this.level = level;
      biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
    }

    private static long key(
        int x,
        int z
    ) { return ((long) x << 32) | (z & 0xFFFFFFFFL); }

    private static void init() {
      ClientPlayNetworking.registerGlobalReceiver(StarAtlas.Payload.ID, (payload, context) ->
          context.client().execute(() -> {
            AtlasMap map = active;
            if (map == null || context.client().level != map.level
                || (payload.action() != StarAtlas.Payload.TILE && payload.action() != StarAtlas.Payload.BATCH)
                || !map.level.dimension().equals(payload.dimension()) || map.lastView == null) return;
            ByteBuffer bytes = ByteBuffer.wrap(payload.pixels());
            do {
              int x = payload.action() == StarAtlas.Payload.BATCH ? bytes.getInt() : payload.minX();
              int z = payload.action() == StarAtlas.Payload.BATCH ? bytes.getInt() : payload.minZ();
              if (x < map.lastView.minX() || x > map.lastView.maxX()
                  || z < map.lastView.minZ() || z > map.lastView.maxZ()) {
                bytes.position(bytes.position() + 64);
                continue;
              }
              int[] cells = new int[16];
              for (int i = 0; i < cells.length; i++) cells[i] = bytes.getInt();
              map.visible.put(key(x, z), cells);
            } while (bytes.hasRemaining());
            map.revision++;
          }));
      StarAtlas.setClientOpenHandler(() -> {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
          client.gui.setScreen(new StarAtlasScreen(new AtlasMap(client.level), client.level));
        }
      });
    }

    private int[] cellsAt(
        int x,
        int z
    ) {
      return visible.get(key(x, z));
    }

    private void close() {
      if (active == this) active = null;
      if (Minecraft.getInstance().getConnection() != null && ClientPlayNetworking.canSend(StarAtlas.Payload.ID)) {
        ClientPlayNetworking.send(StarAtlas.Payload.close(level.dimension()));
      }
      visible.clear();
    }

    private Component biomeAt(
        int x,
        int z
    ) {
      int[] cells = cellsAt(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
      Biome biome = cells == null ? null
          : biomes.byId(cells[(Math.floorMod(z, 16) >> 2) * 4 + (Math.floorMod(x, 16) >> 2)]);
      if (biome == null) return Component.translatable("gui.magnatour.star_atlas.unknown");
      Identifier id = biomes.getKey(biome);
      return Component.translatableWithFallback("biome." + id.getNamespace() + "." + id.getPath().replace('/', '.'), id.toString());
    }

    private void copyTo(
        NativeImage image,
        double centerX,
        double centerZ,
        double zoom
    ) {
      int width = image.getWidth();
      int height = image.getHeight();
      int[] chunkXs = new int[width];
      int[] localXs = new int[width];
      int[][] columns = new int[width][];
      for (int x = 0; x < width; x++) {
        int worldX = (int) Math.floor(centerX + (x - width / 2.0) / zoom);
        chunkXs[x] = Math.floorDiv(worldX, 16);
        localXs[x] = Math.floorMod(worldX, 16) >> 2;
      }
      int previousZ = Integer.MIN_VALUE;
      for (int y = 0; y < height; y++) {
        int worldZ = (int) Math.floor(centerZ + (y - height / 2.0) / zoom);
        int chunkZ = Math.floorDiv(worldZ, 16);
        int row = (Math.floorMod(worldZ, 16) >> 2) * 4;
        if (chunkZ != previousZ) {
          int previousX = Integer.MIN_VALUE;
          int[] colors = null;
          for (int x = 0; x < width; x++) {
            if (chunkXs[x] != previousX) {
              int[] cells = cellsAt(chunkXs[x], chunkZ);
              colors = cells == null ? null : new int[16];
              if (cells != null) {
                for (int i = 0; i < 16; i++) {
                  Biome biome = biomes.byId(cells[i]);
                  colors[i] = biome == null ? 0xFFD6CBA5 : StarAtlas.biomeColor(biomes.getKey(biome));
                }
              }
              previousX = chunkXs[x];
            }
            columns[x] = colors;
          }
          previousZ = chunkZ;
        }
        for (int x = 0; x < width; x++) image.setPixel(x, y,
            columns[x] == null ? 0xFFD6CBA5 : columns[x][row + localXs[x]]);
      }
    }

    private void open() { active = this; }

    private void requestView(
        double centerX,
        double centerZ,
        double zoom,
        int width,
        int height
    ) {
      if (!ClientPlayNetworking.canSend(StarAtlas.Payload.ID)) return;
      int minX = Math.max(-1875000, (int) Math.floor((centerX - width / (2.0 * zoom)) / 16));
      int minZ = Math.max(-1875000, (int) Math.floor((centerZ - height / (2.0 * zoom)) / 16));
      int maxX = Math.min(1875000, (int) Math.floor((centerX + width / (2.0 * zoom)) / 16));
      int maxZ = Math.min(1875000, (int) Math.floor((centerZ + height / (2.0 * zoom)) / 16));
      if (maxX < minX || maxZ < minZ) return;
      boolean changed = lastView == null || lastView.minX() != minX || lastView.minZ() != minZ
          || lastView.maxX() != maxX || lastView.maxZ() != maxZ;
      long now = System.nanoTime();
      if (now < nextRequest || (!changed && now - nextRequest < 4_900_000_000L)) return;
      lastView = StarAtlas.Payload.view(level.dimension(), minX, minZ, maxX, maxZ);
      visible.keySet().removeIf(key -> (int) (key >> 32) < minX || (int) (key >> 32) > maxX
          || (int) (long) key < minZ || (int) (long) key > maxZ);
      ClientPlayNetworking.send(lastView);
      nextRequest = now + 100_000_000L;
    }

    private long revision() { return revision; }

  }

}
