/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.renderer;

// Mojang
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;

// Fabric
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public final class UniverseItemVertexFormats {

  public static final VertexFormat ITEM = padded(DefaultVertexFormat.ENTITY);
  public static final VertexFormat ITEM_SPECIAL = padded(DefaultVertexFormat.ENTITY_GLINT_SPECIAL);

  private UniverseItemVertexFormats() {}

  private static VertexFormat padded(
      VertexFormat source
  ) {
    // Iris extends formats by value equality, not just by object identity.
    // Preserve every semantic and offset, but reserve one extra alignment word
    // so this custom shader's layout cannot be mistaken for a vanilla format.
    // BufferBuilder writes standard semantics at the offsets declared here;
    // the trailing padding is not a shader input and needs no vertex value.
    VertexFormat.Builder builder = VertexFormat.builder(source.getStepRate());
    var elements = source.getElements();
    for (int i = 0; i < elements.size(); i++) {
      var element = elements.get(i);
      int nextOffset = i + 1 < elements.size()
          ? elements.get(i + 1).offset() : source.getVertexSize() + Integer.BYTES;
      builder.addAttribute(element.name(), nextOffset - element.offset(), element.format());
    }
    return builder.build();
  }

}
