/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.level.recipe;

// Java Standard
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;

/** A shaped recipe pattern sized for the Universe Workstation's 5x5 grid. */
public final class UniverseCraftingPattern {

  private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(
      symbol -> symbol.length() == 1
          ? DataResult.success(symbol.charAt(0))
          : DataResult.error(() -> "Recipe symbols must be one character"),
      String::valueOf
  );

  private static final MapCodec<Data> DATA_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
          Codec.unboundedMap(SYMBOL_CODEC, Ingredient.CODEC)
              .fieldOf("key")
              .forGetter(Data::key),
          Codec.STRING.listOf()
              .fieldOf("pattern")
              .forGetter(Data::pattern)
      ).apply(instance, Data::new)
  );

  public static final MapCodec<UniverseCraftingPattern> MAP_CODEC = DATA_CODEC.flatXmap(
      UniverseCraftingPattern::decode,
      pattern -> DataResult.success(pattern.data)
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, UniverseCraftingPattern> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          UniverseCraftingPattern::width,
          ByteBufCodecs.VAR_INT,
          UniverseCraftingPattern::height,
          Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(25)),
          UniverseCraftingPattern::ingredients,
          UniverseCraftingPattern::new
      );

  private final int width;
  private final int height;

  private final List<Optional<Ingredient>> ingredients;

  private Data data;

  private UniverseCraftingPattern(
      int width,
      int height,
      List<Optional<Ingredient>> ingredients
  ) {
    if (width < 1 || width > 5 || height < 1 || height > 5 || ingredients.size() != width * height) {
      throw new IllegalArgumentException("Universe crafting patterns must fit within a 5x5 grid");
    }

    this.width = width;
    this.height = height;
    this.ingredients = List.copyOf(ingredients);
    this.data = null;
  }

  private UniverseCraftingPattern(
      int width,
      int height,
      List<Optional<Ingredient>> ingredients,
      Data data
  ) {
    this(width, height, ingredients);
    this.data = data;
  }

  public int height() {
    return this.height;
  }

  public List<Optional<Ingredient>> ingredients() {
    return this.ingredients;
  }

  public boolean matches(
      CraftingInput input
  ) {
    if (this.width > input.width() || this.height > input.height()) return false;

    for (int offsetY = 0; offsetY <= input.height() - this.height; offsetY++) {
      for (int offsetX = 0; offsetX <= input.width() - this.width; offsetX++) {
        if (matchesAt(input, offsetX, offsetY, false)
            || matchesAt(input, offsetX, offsetY, true)) return true;
      }
    }
    return false;
  }

  public int width() {
    return this.width;
  }

  private static List<String> trim(
      List<String> source
  ) {
    int top = 0;
    int bottom = source.size();
    while (top < bottom && source.get(top).isBlank()) top++;
    while (bottom > top && source.get(bottom - 1).isBlank()) bottom--;
    if (top == bottom) return List.of();

    int left = Integer.MAX_VALUE;
    int right = -1;
    for (int y = top; y < bottom; y++) {
      String row = source.get(y);
      for (int x = 0; x < row.length(); x++) {
        if (row.charAt(x) != ' ') {
          left = Math.min(left, x);
          right = Math.max(right, x);
        }
      }
    }
    if (right < left) return List.of();

    int width = right - left + 1;
    List<String> trimmed = new ArrayList<>(bottom - top);
    for (int y = top; y < bottom; y++) {
      String row = source.get(y);
      if (row.length() < right + 1) row = row + " ".repeat(right + 1 - row.length());
      trimmed.add(row.substring(left, left + width));
    }
    return trimmed;
  }

  private static DataResult<UniverseCraftingPattern> decode(
      Data data
  ) {
    List<String> rows = trim(data.pattern());
    if (rows.isEmpty()) return DataResult.error(() -> "Recipe pattern cannot be empty");

    int height = rows.size();
    int width = rows.getFirst().length();
    if (width < 1 || width > 5 || height > 5) {
      return DataResult.error(() -> "Universe crafting pattern must fit within 5x5");
    }

    List<Optional<Ingredient>> ingredients = new ArrayList<>(width * height);
    Set<Character> usedSymbols = new HashSet<>();
    for (String row : rows) {
      if (row.length() != width) return DataResult.error(() -> "Recipe pattern rows must be the same width");
      for (int i = 0; i < row.length(); i++) {
        char symbol = row.charAt(i);
        if (symbol == ' ') {
          ingredients.add(Optional.empty());
        } else {
          Ingredient ingredient = data.key().get(symbol);
          if (ingredient == null) return DataResult.error(() -> "Recipe pattern uses undefined symbol '" + symbol + "'");
          ingredients.add(Optional.of(ingredient));
          usedSymbols.add(symbol);
        }
      }
    }

    for (Character symbol : data.key().keySet()) {
      if (!usedSymbols.contains(symbol)) {
        return DataResult.error(() -> "Recipe key contains unused symbol '" + symbol + "'");
      }
    }

    return DataResult.success(new UniverseCraftingPattern(width, height, ingredients, data));
  }

  private boolean matchesAt(
      CraftingInput input,
      int offsetX,
      int offsetY,
      boolean mirrored
  ) {
    for (int y = 0; y < input.height(); y++) {
      for (int x = 0; x < input.width(); x++) {
        int patternX = x - offsetX;
        int patternY = y - offsetY;
        Optional<Ingredient> expected = Optional.empty();

        if (patternX >= 0 && patternY >= 0 && patternX < this.width && patternY < this.height) {
          if (mirrored) patternX = this.width - patternX - 1;
          expected = this.ingredients.get(patternX + patternY * this.width);
        }

        if (!Ingredient.testOptionalIngredient(expected, input.getItem(x, y))) return false;
      }
    }
    return true;
  }

  private record Data(
      Map<Character, Ingredient> key,
      List<String> pattern
  ) {}

}
