"""Generate Minecraft 26.3 Fortune-compatible loot data for Omniscience.

Native uniform_bonus_count adds 0..10 items per level. Fortune-dependent drop
probabilities gain 10 percentage points per level, capped at 100 percent.
Silk Touch, maturity, explosion and native Fortune conditions remain in place.
Pass the common Minecraft JAR; --check verifies data without writing it.
"""

import argparse
import copy
import json
from pathlib import Path
import zipfile

from generate_omniscience_loot import DATA, ENCHANTMENT, MAX_LEVEL, ROOT, all_of, render


FORTUNE = "minecraft:fortune"


def tool_condition(level=None):
    levels = {"min": 1, "max": MAX_LEVEL} if level is None else {"min": level, "max": level}
    return {
        "type": "minecraft:match_tool",
        "predicate": {"predicates": {"minecraft:enchantments": [
            {"enchantments": ENCHANTMENT, "levels": levels}
        ]}},
    }


def absent_tool_condition():
    return {"type": "minecraft:inverted", "term": tool_condition()}


def contains_fortune(node):
    if isinstance(node, list):
        return any(contains_fortune(child) for child in node)
    if not isinstance(node, dict):
        return False
    return (
        node.get("type") in ("minecraft:apply_bonus", "minecraft:table_bonus")
        and node.get("enchantment") == FORTUNE
    ) or any(contains_fortune(value) for value in node.values())


def contains_random_chance(node):
    if isinstance(node, list):
        return any(contains_random_chance(child) for child in node)
    if not isinstance(node, dict):
        return False
    return node.get("type") == "minecraft:random_chance" or any(
        contains_random_chance(value) for value in node.values()
    )


def boost_chance(condition):
    terms = [all_of(absent_tool_condition(), copy.deepcopy(condition))]
    for level in range(1, MAX_LEVEL + 1):
        variant = copy.deepcopy(condition)
        if variant["type"] == "minecraft:table_bonus":
            variant["chances"] = [min(1.0, chance + 0.1 * level) for chance in variant["chances"]]
        else:
            variant["chance"] = min(1.0, variant["chance"] + 0.1 * level)
        terms.append(all_of(tool_condition(level), variant))
    return {"type": "minecraft:any_of", "terms": terms}


def strip_generated(node):
    if isinstance(node, list):
        return [value for child in node if (value := strip_generated(child)) is not None]
    if not isinstance(node, dict):
        return node
    if node.get("type") == "minecraft:apply_bonus" and node.get("enchantment") == ENCHANTMENT:
        return None
    if node.get("type") == "minecraft:any_of":
        first = node.get("terms", [{}])[0]
        if isinstance(first, dict) and first.get("type") == "minecraft:all_of" and first["terms"][0] == absent_tool_condition():
            return strip_generated(first["terms"][1])
    result = {key: strip_generated(value) for key, value in node.items()}
    if result.get("modifier") == []:
        result.pop("modifier")
    return result


def transform(node, probability_drop=False, mod_leaves=False):
    if isinstance(node, list):
        return [transform(child, probability_drop, mod_leaves) for child in node]
    if not isinstance(node, dict):
        return node
    kind = node.get("type")
    if (kind == "minecraft:table_bonus" and node.get("enchantment") == FORTUNE) or (
        mod_leaves and kind == "minecraft:random_chance"
    ):
        return boost_chance(node)
    current_condition = node.get("condition")
    probability_drop = probability_drop or contains_fortune(current_condition) or (
        mod_leaves and contains_random_chance(current_condition)
    )
    result = {key: transform(value, probability_drop, mod_leaves) for key, value in node.items()}
    if kind in ("minecraft:item", "minecraft:tag"):
        original_modifiers = node.get("modifier", [])
        if isinstance(original_modifiers, dict):
            original_modifiers = [original_modifiers]
        fortune_functions = [function for function in original_modifiers
                             if function.get("type") == "minecraft:apply_bonus"
                             and function.get("enchantment") == FORTUNE]
        if probability_drop or fortune_functions:
            modifiers = result.get("modifier", [])
            if isinstance(modifiers, dict):
                modifiers = [modifiers]
            condition = tool_condition()
            # Conditional Fortune on crops must still obey its original maturity condition.
            if not probability_drop and all("condition" in function for function in fortune_functions):
                guards = [copy.deepcopy(function["condition"]) for function in fortune_functions]
                guard = guards[0] if len(guards) == 1 else {"type": "minecraft:any_of", "terms": guards}
                condition = all_of(condition, guard)
            bonus = {
                "type": "minecraft:apply_bonus",
                "enchantment": ENCHANTMENT,
                "formula": "minecraft:uniform_bonus_count",
                "parameters": {"bonusMultiplier": 10},
                "condition": condition,
            }
            # Add after native count caps, but before explosion decay removes individual items.
            insertion = next((index for index, function in enumerate(modifiers)
                              if function.get("type") == "minecraft:explosion_decay"), len(modifiers))
            modifiers.insert(insertion, bonus)
            result["modifier"] = modifiers
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("minecraft_jar", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    pending = {}
    with zipfile.ZipFile(args.minecraft_jar) as archive:
        for name in sorted(archive.namelist()):
            if name.startswith("data/minecraft/loot_table/blocks/") and name.endswith(".json"):
                original = json.loads(archive.read(name))
                if contains_fortune(original):
                    pending[DATA.parent / name] = render(transform(original))
    for path in sorted((DATA / "magnatour/loot_table/blocks").glob("*.json")):
        original = strip_generated(json.loads(path.read_text(encoding="utf-8")))
        mod_leaves = path.stem in ("golden_leaves", "universe_leaves")
        if contains_fortune(original) or mod_leaves:
            pending[path] = render(transform(original, mod_leaves=mod_leaves))
    if args.check:
        stale = [str(path.relative_to(ROOT)) for path, text in pending.items()
                 if not path.exists() or path.read_text(encoding="utf-8") != text]
        if stale:
            raise SystemExit("Stale Omniscience block loot data: " + ", ".join(stale))
    else:
        for path, text in pending.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8")
    print(f"{'Verified' if args.check else 'Generated'} {len(pending)} block loot tables.")


if __name__ == "__main__":
    main()
