"""Mirrors the generated 1.21.1 resources into the other Minecraft workspaces, rewriting what changed format.

    python3 tools/assets/port_resources.py          # every target
    python3 tools/assets/port_resources.py 26.2     # one target

tools/assets/generate.py writes 1.21.1/common/src/main/resources and runs this afterwards. Assets and data are copied,
then rewritten for each target: item model definitions, weapon base models, recipe ingredients, advancement
backgrounds and entity predicates, the key-mapping category label and the pack format. Hand-written files in the
target workspace (META-INF, the access widener) are left alone. Standard library only, so CI can re-run it and diff
the result.
"""
from __future__ import annotations

import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
NS = "remnants_ballistics"
SOURCE = os.path.join(ROOT, "1.21.1", "common", "src", "main", "resources")
# Generated content; everything else in a workspace's resources is hand-written and kept.
GENERATED = ("assets", "data", "pack.mcmeta", "logo.png")

# Resource and data pack versions per target (version.json "pack_version" of the game). "typed_loot": loot
# conditions and functions are keyed by "type" and fields hold one condition (26.3).
TARGETS = {
    "26.2": {"resource": 88, "data": [107, 1], "typed_loot": False},
    "26.3": {"resource": 97, "data": [121, 0], "typed_loot": True},
}


def resources(game):
    return os.path.join(ROOT, game, "common", "src", "main", "resources")


def read_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ----------------------------------------------------------------------------------------------------------------
# 26.x (Minecraft 26.2 and 26.3, NeoForge 26.x + Fabric)
# ----------------------------------------------------------------------------------------------------------------

# Vanilla ids that were renamed by 26.x (the copper update split chains into iron and copper ones).
RENAMED_26 = {"minecraft:chain": "minecraft:iron_chain"}


def _renamed_26(node):
    if isinstance(node, dict):
        return {k: _renamed_26(v) for k, v in node.items()}
    if isinstance(node, list):
        return [_renamed_26(v) for v in node]
    if isinstance(node, str):
        prefix = "#" if node.startswith("#") else ""
        return prefix + RENAMED_26.get(node[len(prefix):], node[len(prefix):])
    return node


def _ingredient_26(ingredient):
    """Ingredients are an item id, "#tag" or a list of those (no more {"item": ...} / {"tag": ...} objects)."""
    if isinstance(ingredient, list):
        return [_ingredient_26(i) for i in ingredient]
    if isinstance(ingredient, dict):
        if "tag" in ingredient:
            return "#" + ingredient["tag"]
        if "item" in ingredient:
            return ingredient["item"]
    return ingredient


def recipe_26(recipe):
    recipe = dict(recipe)
    if "key" in recipe:
        recipe["key"] = {k: _ingredient_26(v) for k, v in recipe["key"].items()}
    if "ingredient" in recipe:
        recipe["ingredient"] = _ingredient_26(recipe["ingredient"])
    if "ingredients" in recipe:
        if recipe["type"] == f"{NS}:ammo_press":
            # Counted ingredients use NeoForge's sized-ingredient shape: {"ingredient": ..., "count": n}.
            recipe["ingredients"] = [
                {"ingredient": _ingredient_26({k: v for k, v in i.items() if k != "count"}),
                 **({"count": i["count"]} if i.get("count", 1) != 1 else {})}
                for i in recipe["ingredients"]]
        else:
            recipe["ingredients"] = [_ingredient_26(i) for i in recipe["ingredients"]]
    return recipe


def _entity_predicate_26(predicate):
    """Entity predicates are maps of namespaced sub-predicates ("minecraft:distance", "minecraft:entity_type", ...)."""
    renamed = {"type": "entity_type", "nbt": "nbt"}
    out = {}
    for key, value in predicate.items():
        key = renamed.get(key, key)
        out[key if ":" in key else "minecraft:" + key] = value
    return out


def advancement_26(advancement):
    advancement = json.loads(json.dumps(advancement))
    display = advancement.get("display", {})
    background = display.get("background")
    if background:
        # Backgrounds name a texture asset without the textures/ prefix and .png suffix.
        ns, path = background.split(":", 1) if ":" in background else ("minecraft", background)
        if path.startswith("textures/") and path.endswith(".png"):
            display["background"] = f"{ns}:{path[len('textures/'):-len('.png')]}"
    for criterion in advancement.get("criteria", {}).values():
        conditions = criterion.get("conditions", {})
        for condition in conditions.get("entity", []) if isinstance(conditions.get("entity"), list) else []:
            if condition.get("condition") == "minecraft:entity_properties" and "predicate" in condition:
                condition["predicate"] = _entity_predicate_26(condition["predicate"])
    return advancement


# 26.3: loot conditions and item modifiers are keyed by "type"; a "condition" field holds one condition (several are
# wrapped in minecraft:all_of), "functions" became "modifier", and rolls and item counts are integer providers.

def _condition_263(condition):
    out = {"type": condition["condition"]}
    for key, value in condition.items():
        if key == "condition":
            continue
        if key == "terms":
            value = [_condition_263(t) for t in value]
        elif key == "term":
            value = _condition_263(value)
        out[key] = value
    return out


def _conditions_263(conditions):
    if len(conditions) == 1:
        return _condition_263(conditions[0])
    return {"type": "minecraft:all_of", "terms": [_condition_263(c) for c in conditions]}


def _int_provider_263(value):
    """Integer number providers: whole floats become ints (2.0 -> 2), uniform bounds likewise."""
    if isinstance(value, float) and value.is_integer():
        return int(value)
    if isinstance(value, dict):
        return {k: _int_provider_263(v) if k in ("min", "max", "value") else v for k, v in value.items()}
    return value


def _function_263(function):
    out = {"type": function["function"]}
    for key, value in function.items():
        if key == "function":
            continue
        if key == "conditions":
            out["condition"] = _conditions_263(value)
            continue
        if function["function"] == "minecraft:set_count" and key == "count":
            value = _int_provider_263(value)
        out[key] = value
    return out


def _loot_node_263(node):
    """A loot table, pool or entry: conditions, functions and nested entries."""
    out = {}
    for key, value in node.items():
        if key == "conditions":
            out["condition"] = _conditions_263(value)
        elif key == "functions":
            out["modifier"] = [_function_263(f) for f in value]
        elif key in ("pools", "entries", "children"):
            out[key] = [_loot_node_263(n) for n in value]
        elif key == "rolls":
            out[key] = _int_provider_263(value)
        else:
            out[key] = value
    return out


def loot_modifier_263(modifier):
    modifier = dict(modifier)
    if "conditions" in modifier:
        modifier["condition"] = _conditions_263(modifier.pop("conditions"))
    return modifier


def advancement_263(advancement):
    for criterion in advancement.get("criteria", {}).values():
        conditions = criterion.get("conditions", {})
        for key, value in list(conditions.items()):
            # Entity fields (player, entity, ...) hold one loot condition instead of a list.
            if isinstance(value, list) and value and isinstance(value[0], dict) and "condition" in value[0]:
                conditions[key] = _conditions_263(value)
        # Damage type tags are written with their "#".
        for tag in conditions.get("killing_blow", {}).get("tags", []):
            if not tag["id"].startswith("#"):
                tag["id"] = "#" + tag["id"]
    return advancement


def weapon_names():
    """Weapons are the items whose 1.21.1 item model is drawn by a renderer (parent builtin/entity)."""
    models = os.path.join(SOURCE, "assets", NS, "models", "item")
    names = []
    for name in sorted(os.listdir(models)):
        path = os.path.join(models, name)
        if name.endswith(".json") and read_json(path).get("parent") == "minecraft:builtin/entity":
            names.append(name[:-len(".json")])
    return names


def item_definitions_26(target_assets):
    """assets/<ns>/items/<id>.json for every item. Weapons show their flat icon in GUIs and the animated 3D model
    (item model type remnants_ballistics:gun, client.render.GunItemModel) everywhere else."""
    models = os.path.join(SOURCE, "assets", NS, "models", "item")
    weapons = set(weapon_names())
    for name in sorted(os.listdir(models)):
        if not name.endswith(".json"):
            continue
        item = name[:-len(".json")]
        if item in weapons:
            model = {
                "type": "minecraft:select",
                "property": "minecraft:display_context",
                "cases": [{"when": "gui", "model": {"type": "minecraft:model", "model": f"{NS}:item/icon/{item}"}}],
                "fallback": {"type": f"{NS}:gun", "base": f"{NS}:item/{item}"},
            }
        else:
            model = {"type": "minecraft:model", "model": f"{NS}:item/{item}"}
        write_json(os.path.join(target_assets, "items", item + ".json"), {"model": model})


def port_26(game):
    target = resources(game)
    formats = TARGETS[game]
    for name in GENERATED:
        path = os.path.join(target, name)
        if os.path.isdir(path):
            shutil.rmtree(path)
        elif os.path.exists(path):
            os.remove(path)
    for name in ("assets", "data", "logo.png"):
        src = os.path.join(SOURCE, name)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(target, name))
        else:
            shutil.copy2(src, os.path.join(target, name))

    data = os.path.join(target, "data")
    # NeoForge 26.x loads every data/<ns>/loot_modifiers/*.json; the global list file is gone.
    os.remove(os.path.join(data, "neoforge", "loot_modifiers", "global_loot_modifiers.json"))
    os.rmdir(os.path.join(data, "neoforge", "loot_modifiers"))
    os.rmdir(os.path.join(data, "neoforge"))
    for dirpath, _, files in os.walk(data):
        rel = os.path.relpath(dirpath, data).replace(os.sep, "/")
        for name in files:
            if not name.endswith(".json"):
                continue
            path = os.path.join(dirpath, name)
            content = _renamed_26(read_json(path))
            # rel is <namespace>/<registry>[/...]
            registry = rel.split("/")[1:2]
            if registry == ["recipe"]:
                content = recipe_26(content)
            elif registry == ["advancement"]:
                content = advancement_26(content)
                if formats["typed_loot"]:
                    content = advancement_263(content)
            elif registry == ["loot_table"] and formats["typed_loot"]:
                content = _loot_node_263(content)
            elif registry == ["loot_modifiers"] and formats["typed_loot"]:
                content = loot_modifier_263(content)
            write_json(path, content)

    assets = os.path.join(target, "assets", NS)
    item_definitions_26(assets)
    # The weapon base models only carry display transforms now; builtin/entity no longer exists.
    for item in weapon_names():
        path = os.path.join(assets, "models", "item", item + ".json")
        model = read_json(path)
        model.pop("parent", None)
        write_json(path, model)
    # Key-mapping categories are identifiers: key.category.<namespace>.<path>.
    lang_path = os.path.join(assets, "lang", "en_us.json")
    lang = read_json(lang_path)
    lang[f"key.category.{NS}.weapons"] = lang.pop(f"key.categories.{NS}")
    write_json(lang_path, dict(sorted(lang.items())))

    pack = read_json(os.path.join(SOURCE, "pack.mcmeta"))["pack"]
    # One pack.mcmeta serves the mod's resource pack and data pack, so the range spans both versions.
    write_json(os.path.join(target, "pack.mcmeta"), {"pack": {
        "description": pack["description"],
        "min_format": formats["resource"],
        "max_format": formats["data"][0],
    }})


def main(argv):
    games = argv[1:] or sorted(TARGETS)
    for game in games:
        if game not in TARGETS:
            raise SystemExit(f"Unknown target {game}; expected one of {', '.join(sorted(TARGETS))}")
        if not os.path.isdir(os.path.join(ROOT, game)):
            continue
        port_26(game)
        print(f"Ported resources to {game}")


if __name__ == "__main__":
    main(sys.argv)
