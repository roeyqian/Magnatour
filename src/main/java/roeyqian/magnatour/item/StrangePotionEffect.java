/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Universe Mod.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.item;

// Java Standard
import java.util.ArrayList;
import java.util.List;

// Minecraft
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class StrangePotionEffect {

  private static final int EFFECT_COUNT = 5;
  private static final int MAX_SECONDS = 1000;
  private static final int MIN_SECONDS = 10;

  private StrangePotionEffect() {}

  public static void applyRandomEffects(
      LivingEntity target,
      RandomSource random
  ) {
    List<Holder.Reference<MobEffect>> pool = new ArrayList<>(BuiltInRegistries.MOB_EFFECT.listElements().toList());
    int amount = Math.min(EFFECT_COUNT, pool.size());

    for (int i = 0; i < amount; i++) {
      Holder<MobEffect> effect = pool.remove(random.nextInt(pool.size()));
      int durationSeconds = MIN_SECONDS + random.nextInt(MAX_SECONDS - MIN_SECONDS + 1);
      target.addEffect(new MobEffectInstance(effect, durationSeconds * 20));
    }
  }

}
