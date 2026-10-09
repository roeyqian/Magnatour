/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item.universe;

// Java Standard
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

// Mojang
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Minecraft
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

// JSpecify
import org.jspecify.annotations.NonNull;

// Magnatour
import roeyqian.magnatour.blockentity.universe.UniverseLibraryEntity;

public record UniverseLibraryContents(
    List<StoredSlot> slots
) implements TooltipProvider {

  public static final Codec<UniverseLibraryContents> CODEC = StoredSlot.CODEC
      .listOf(0, UniverseLibraryEntity.CONTAINER_SIZE)
      .xmap(UniverseLibraryContents::new, UniverseLibraryContents::slots);

  public static final UniverseLibraryContents EMPTY = new UniverseLibraryContents(List.of());

  public UniverseLibraryContents {
    slots = List.copyOf(slots);
  }

  public static String formatCount(
      long count
  ) {
    if (count >= 1_000_000_000L) return formatCountUnit(count, 1_000_000_000L, "G");
    if (count >= 1_000_000L) return formatCountUnit(count, 1_000_000L, "M");
    if (count >= 1_000L) return formatCountUnit(count, 1_000L, "K");
    return Long.toString(count);
  }

  public static UniverseLibraryContents fromItems(
      List<ItemStack> items
  ) {
    List<StoredSlot> slots = new ArrayList<>();
    for (int slot = 0; slot < items.size(); slot++) {
      ItemStack stack = items.get(slot);
      if (stack.isEmpty()) continue;
      // Vanilla item codecs cap counts at 99; store the actual quantity separately.
      slots.add(new StoredSlot(slot, ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1)), stack.getCount()));
    }
    return new UniverseLibraryContents(slots);
  }

  @Override
  public void addToTooltip(
      Item.@NonNull TooltipContext context,
      @NonNull Consumer<Component> lines,
      @NonNull TooltipFlag flag,
      @NonNull DataComponentGetter components
  ) {
    List<StoredSlot> preview = this.slots.stream()
        .sorted(Comparator.comparingInt(StoredSlot::slot))
        .limit(5)
        .toList();
    for (StoredSlot slot : preview) {
      // Stored templates have a count of one; display the separate storage quantity directly.
      ItemStack item = slot.item().create();
      lines.accept(Component.translatable("item.container.item_count", formatCount(slot.count()), item.getHoverName()));
    }

    int remaining = this.slots.size() - preview.size();
    if (remaining > 0) {
      lines.accept(Component.translatable("item.container.more_items", remaining).withStyle(ChatFormatting.ITALIC));
    }
  }

  public void copyInto(
      NonNullList<ItemStack> items
  ) {
    items.clear();
    for (StoredSlot slot : this.slots) {
      if (slot.slot() < items.size()) {
        items.set(slot.slot(), slot.item().create().copyWithCount(slot.count()));
      }
    }
  }

  private static String formatCountUnit(
      long count, long unit,
      String suffix
  ) {
    return BigDecimal.valueOf(count).divide(BigDecimal.valueOf(unit), 2, RoundingMode.DOWN)
        .stripTrailingZeros().toPlainString() + suffix;
  }

  public record StoredSlot(
      int slot,
      ItemStackTemplate item,
      int count
  ) {

    public static final Codec<StoredSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.intRange(0, UniverseLibraryEntity.CONTAINER_SIZE - 1).fieldOf("slot").forGetter(StoredSlot::slot),
        ItemStackTemplate.CODEC.fieldOf("item").forGetter(StoredSlot::item),
        ExtraCodecs.intRange(1, UniverseLibraryEntity.STORAGE_STACK_LIMIT).fieldOf("count").forGetter(StoredSlot::count)
    ).apply(instance, StoredSlot::new));

  }

}
