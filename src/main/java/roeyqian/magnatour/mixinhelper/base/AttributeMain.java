/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Roey Qian
 *
 * This file is part of Magnatour.
 * Full license text available in the LICENSE file in the project root.
 */
package roeyqian.magnatour.mixinhelper.base;

public final class AttributeMain {

  private AttributeMain() {}

  public static double extendAttributeLimit(
      String descriptionId,
      double currentMax
  ) {
    if ("attribute.name.max_health".equals(descriptionId)
        || "attribute.name.attack_damage".equals(descriptionId)
        || "attribute.name.attack_speed".equals(descriptionId)
        || "attribute.name.sweeping_damage_ratio".equals(descriptionId)
        || "attribute.name.entity_interaction_range".equals(descriptionId)
        || "attribute.name.block_interaction_range".equals(descriptionId)
        || "attribute.name.armor".equals(descriptionId)
        || "attribute.name.armor_toughness".equals(descriptionId)) {
      return Integer.MAX_VALUE;
    }
    return currentMax;
  }

}
