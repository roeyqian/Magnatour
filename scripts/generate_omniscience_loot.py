"""Generate Minecraft 26.3 entity loot data using native Looting functions.

Pass the 26.3 common Minecraft JAR. --check verifies generated data without writing.
Only entity loot tables are changed; acquisition, block and chest loot stay untouched.
"""

import argparse
import copy
import json
from pathlib import Path
import zipfile


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data"
ENCHANTMENT = "magnatour:omniscience"
MAX_LEVEL = 10


def level_condition(level=None):
    levels = {"min": 1, "max": MAX_LEVEL} if level is None else {"min": level, "max": level}
    return {
        "type": "minecraft:entity_properties",
        "entity": "attacker",
        "predicate": {
            "minecraft:equipment": {
                "mainhand": {
                    "predicates": {
                        "minecraft:enchantments": [{"enchantments": ENCHANTMENT, "levels": levels}]
                    }
                }
            }
        },
    }


def absent_condition():
    return {"type": "minecraft:inverted", "term": level_condition()}


def all_of(*terms):
    return {"type": "minecraft:all_of", "terms": list(terms)}


def add_chance_value(value, bonus):
    """Add probability points while preserving the original native Looting bonus."""
    if isinstance(value, (int, float)):
        return value + bonus
    if value.get("type") == "minecraft:linear":
        result = copy.deepcopy(value)
        result["base"] += bonus
        return result
    raise ValueError(f"Unsupported native level value: {value}")


def boost_chance(condition):
    terms = [all_of(absent_condition(), copy.deepcopy(condition))]
    for level in range(1, MAX_LEVEL + 1):
        variant = copy.deepcopy(condition)
        bonus = 0.1 * level
        if variant["type"] == "minecraft:random_chance":
            if not isinstance(variant["chance"], (int, float)):
                raise ValueError(f"Unsupported probability provider: {variant}")
            variant["chance"] = min(1, variant["chance"] + bonus)
        else:
            variant["unenchanted_chance"] = min(1, variant["unenchanted_chance"] + bonus)
            variant["enchanted_chance"] = add_chance_value(variant["enchanted_chance"], bonus)
        terms.append(all_of(level_condition(level), variant))
    return {"type": "minecraft:any_of", "terms": terms}


def strip_generated(node):
    """Recover the vanilla branch so generation remains repeatable for mod tables."""
    if isinstance(node, list):
        return [stripped for child in node if (stripped := strip_generated(child)) is not None]
    if not isinstance(node, dict):
        return node
    if node.get("type") == "minecraft:enchanted_count_increase" and node.get("enchantment") == ENCHANTMENT:
        return None
    if node.get("type") == "minecraft:any_of":
        first = node.get("terms", [{}])[0]
        if first.get("type") == "minecraft:all_of" and first["terms"][0] == absent_condition():
            return strip_generated(first["terms"][1])
    if node.get("type") == "minecraft:sequence":
        first = node.get("functions", [{}])[0]
        condition = first.get("condition")
        if condition == absent_condition() or (
            isinstance(condition, dict) and condition.get("type") == "minecraft:all_of"
            and condition["terms"][0] == absent_condition()
        ):
            original = copy.deepcopy(first)
            if condition == absent_condition():
                original.pop("condition")
            else:
                original["condition"] = condition["terms"][1]
            return strip_generated(original)
    result = {key: strip_generated(value) for key, value in node.items()}
    if result.get("modifier") == []:
        result.pop("modifier")
    return result


def transform(node):
    if isinstance(node, list):
        return [transform(child) for child in node]
    if not isinstance(node, dict):
        return node
    kind = node.get("type")
    if kind in ("minecraft:random_chance", "minecraft:random_chance_with_enchanted_bonus"):
        return boost_chance(node)
    result = {key: transform(value) for key, value in node.items()}
    if kind in ("minecraft:item", "minecraft:tag"):
        modifiers = result.get("modifier", [])
        if isinstance(modifiers, dict):
            modifiers = [modifiers]
        # Reuse Looting's native round(level * random(0, 10)) calculation.
        # Apply after existing count functions and limits, adding up to 10 items per level.
        modifiers.append({
            "type": "minecraft:enchanted_count_increase",
            "enchantment": ENCHANTMENT,
            "count": {"type": "minecraft:uniform", "min": 0.0, "max": 10.0},
            "condition": level_condition(),
        })
        result["modifier"] = modifiers
    return result


def render(value):
    return json.dumps(value, ensure_ascii=False, indent=2) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("minecraft_jar", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    pending = {}
    with zipfile.ZipFile(args.minecraft_jar) as archive:
        for name in sorted(archive.namelist()):
            if name.startswith("data/minecraft/loot_table/entities/") and name.endswith(".json"):
                original = json.loads(archive.read(name))
                generated = transform(original)
                if generated != original:
                    pending[DATA.parent / name] = render(generated)
    for path in sorted((DATA / "magnatour/loot_table/entity").glob("*.json")):
        original = strip_generated(json.loads(path.read_text(encoding="utf-8")))
        pending[path] = render(transform(original))
    # Prepare every output before writing. Existing sources were inspected and snapshotted by Codex.
    if args.check:
        stale = [str(path.relative_to(ROOT)) for path, data in pending.items()
                 if not path.exists() or path.read_text(encoding="utf-8") != data]
        if stale:
            raise SystemExit("Stale Omniscience loot data: " + ", ".join(stale))
    else:
        for path, data in pending.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(data, encoding="utf-8")
    print(f"{'Verified' if args.check else 'Generated'} {len(pending)} entity loot tables.")


if __name__ == "__main__":
    main()
