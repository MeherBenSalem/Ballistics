"""Mirrors the generated 1.21.1 resources into the other Minecraft workspaces, rewriting what changed format.

    python3 tools/assets/port_resources.py          # every target
    python3 tools/assets/port_resources.py 26.2     # one target

tools/assets/generate.py writes 1.21.1/common/src/main/resources and runs this afterwards. Assets and data are copied,
then rewritten for each target:

- 26.x: item model definitions, weapon base models, recipe ingredients, advancement backgrounds and entity predicates,
  the key-mapping category label and the pack format.
- 1.20.1: plural data folders, Forge recipe conditions and loot-table-id conditions, recipe results, advancement icons and
  item predicates, the pre-1.21 common ingot tags, vanilla ids that did not exist yet, the GameTest structure's data
  version and the pack format.

Hand-written files in the target workspace (META-INF, the access widener) are left alone. Standard library only, so CI
can re-run it and diff the result.
"""
from __future__ import annotations

import gzip
import json
import os
import shutil
import struct
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
# Pre-1.21 targets (Forge + Fabric): one pack format for resources and data, and the world data version that the
# GameTest structure is saved with.
LEGACY_TARGETS = {
    "1.20.1": {"pack": 15, "data_version": 3465},
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


# ----------------------------------------------------------------------------------------------------------------
# 1.20.1 (Minecraft 1.20.1, Forge 47 + Fabric)
# ----------------------------------------------------------------------------------------------------------------

# Registry folders were plural before 1.21 (tag folders too, for the registries that had them).
LEGACY_DIRS = {"recipe": "recipes", "advancement": "advancements", "loot_table": "loot_tables", "structure": "structures"}
LEGACY_TAG_DIRS = {"item": "items", "block": "blocks", "entity_type": "entity_types", "fluid": "fluids",
                   "game_event": "game_events"}
# Vanilla ids renamed after 1.20.1 (short grass was "grass" until 1.20.3).
RENAMED_1201 = {"minecraft:short_grass": "minecraft:grass"}
# Vanilla ids added after 1.20.1 (1.21's bogged and trial chambers): dropped from tags and loot targets.
ADDED_AFTER_1201 = {"minecraft:bogged", "minecraft:chests/trial_chambers/corridor"}
COOKING = {"minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"}


def _c_tag_1201(path):
    """1.21's common tags c:ingots/<metal> were c:<metal>_ingots on Fabric (and forge:ingots/<metal> on Forge)."""
    if path.startswith("ingots/"):
        return path[len("ingots/"):] + "_ingots"
    return path


def _ids_1201(node):
    """Renames vanilla ids, rewrites common tag references and drops ids that 1.20.1 does not have."""
    if isinstance(node, dict):
        if node.get("loot_table_id") in ADDED_AFTER_1201:
            return None
        out = {}
        for k, v in node.items():
            v = _ids_1201(v)
            if v is not None:
                out[k] = v
        return out
    if isinstance(node, list):
        return [v for v in (_ids_1201(v) for v in node) if v is not None]
    if isinstance(node, str):
        prefix = "#" if node.startswith("#") else ""
        value = node[len(prefix):]
        if value in ADDED_AFTER_1201:
            return None
        value = RENAMED_1201.get(value, value)
        if value.startswith("c:"):
            value = "c:" + _c_tag_1201(value[2:])
        return prefix + value
    return node


def _item_stack_1201(result):
    out = {"item": result["id"]}
    if result.get("count", 1) != 1:
        out["count"] = result["count"]
    return out


def recipe_1201(recipe):
    out = {}
    for key, value in recipe.items():
        if key == "neoforge:conditions":
            # Forge 1.20.1 reads top-level "conditions" with the same {"type": ...} entries.
            out["conditions"] = value
        elif key == "result" and isinstance(value, dict) and "id" in value:
            # Cooking recipes name their result item as a plain string.
            out[key] = value["id"] if recipe["type"] in COOKING else _item_stack_1201(value)
        else:
            out[key] = value
    return out


def _item_predicate_1201(predicate):
    """Item predicates list item ids in an array, and a tag goes in "tag"."""
    predicate = dict(predicate)
    items = predicate.get("items")
    if isinstance(items, str):
        if items.startswith("#"):
            del predicate["items"]
            predicate["tag"] = items[1:]
        else:
            predicate["items"] = [items]
    return predicate


def advancement_1201(advancement):
    advancement = json.loads(json.dumps(advancement))
    icon = advancement.get("display", {}).get("icon")
    if icon and "id" in icon:
        advancement["display"]["icon"] = {"item": icon["id"], **{k: v for k, v in icon.items() if k not in ("id", "count")}}
    for criterion in advancement.get("criteria", {}).values():
        conditions = criterion.get("conditions", {})
        if criterion.get("trigger") == "minecraft:inventory_changed" and "items" in conditions:
            conditions["items"] = [_item_predicate_1201(p) for p in conditions["items"]]
    return advancement


def loot_modifier_1201(modifier):
    """NeoForge's loot_table_id condition is Forge's on 1.20.1."""
    text = json.dumps(modifier).replace('"neoforge:loot_table_id"', '"forge:loot_table_id"')
    return json.loads(text)


def tag_values_1201(tag):
    tag = dict(tag)
    tag["values"] = _ids_1201(tag["values"])
    return tag


def structure_1201(path, data_version):
    """Re-stamps a GameTest structure with the target's data version (it would otherwise be "from the future")."""
    with gzip.open(path, "rb") as f:
        data = f.read()
    marker = b"\x03" + struct.pack(">H", len("DataVersion")) + b"DataVersion"
    at = data.index(marker) + len(marker)
    data = data[:at] + struct.pack(">i", data_version) + data[at + 4:]
    with gzip.GzipFile(path, "wb", mtime=0) as f:
        f.write(data)


def common_tags_1201(data):
    """data/c/tags/item/ingots/<metal>.json -> c:<metal>_ingots (Fabric) and forge:ingots/<metal> (Forge). The Fabric
    tag also pulls in Forge's, so recipes that use c:<metal>_ingots accept other mods' ingots on both loaders."""
    src = os.path.join(data, "c", "tags", "item")
    if not os.path.isdir(src):
        return
    for dirpath, _, files in os.walk(src):
        for name in sorted(files):
            rel = os.path.relpath(os.path.join(dirpath, name), src).replace(os.sep, "/")[:-len(".json")]
            tag = read_json(os.path.join(dirpath, name))
            write_json(os.path.join(data, "forge", "tags", "items", rel + ".json"), tag)
            if rel.startswith("ingots/"):
                fabric = dict(tag)
                fabric["values"] = list(tag["values"]) + [{"id": "#forge:" + rel, "required": False}]
                write_json(os.path.join(data, "c", "tags", "items", _c_tag_1201(rel) + ".json"), fabric)
    shutil.rmtree(src)


def port_1201(game):
    target = resources(game)
    formats = LEGACY_TARGETS[game]
    for name in GENERATED:
        path = os.path.join(target, name)
        if os.path.isdir(path):
            shutil.rmtree(path)
        elif os.path.exists(path):
            os.remove(path)
    for name in ("assets", "logo.png"):
        src = os.path.join(SOURCE, name)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(target, name))
        else:
            shutil.copy2(src, os.path.join(target, name))

    # Data: rename the registry folders while copying.
    src_data = os.path.join(SOURCE, "data")
    data = os.path.join(target, "data")
    for dirpath, _, files in os.walk(src_data):
        rel = os.path.relpath(dirpath, src_data).replace(os.sep, "/")
        parts = rel.split("/")
        if len(parts) >= 2:
            parts[1] = LEGACY_DIRS.get(parts[1], parts[1])
            if parts[1] == "tags" and len(parts) >= 3:
                parts[2] = LEGACY_TAG_DIRS.get(parts[2], parts[2])
        if parts[:3] == ["c", "tags", "items"]:
            parts[2] = "item"  # converted by common_tags_1201 below
        if parts[:1] == ["neoforge"]:
            parts[0] = "forge"  # global_loot_modifiers.json
        out_dir = os.path.join(data, *parts)
        for name in sorted(files):
            src = os.path.join(dirpath, name)
            dst = os.path.join(out_dir, name)
            os.makedirs(out_dir, exist_ok=True)
            if not name.endswith(".json"):
                shutil.copy2(src, dst)
                continue
            content = read_json(src)
            registry = parts[1:2]
            if registry == ["recipes"]:
                content = recipe_1201(content)
            elif registry == ["advancements"]:
                content = advancement_1201(content)
            elif registry == ["loot_modifiers"]:
                content = loot_modifier_1201(content)
            elif registry == ["tags"]:
                content = tag_values_1201(content)
            content = _ids_1201(content)
            write_json(dst, content)
    common_tags_1201(data)
    for dirpath, _, files in os.walk(data):
        for name in files:
            if name.endswith(".nbt"):
                structure_1201(os.path.join(dirpath, name), formats["data_version"])

    pack = read_json(os.path.join(SOURCE, "pack.mcmeta"))["pack"]
    write_json(os.path.join(target, "pack.mcmeta"), {"pack": {
        "description": pack["description"],
        "pack_format": formats["pack"],
    }})


def main(argv):
    every = sorted(TARGETS) + sorted(LEGACY_TARGETS)
    games = argv[1:] or every
    for game in games:
        if game not in every:
            raise SystemExit(f"Unknown target {game}; expected one of {', '.join(every)}")
        if not os.path.isdir(os.path.join(ROOT, game)):
            continue
        if game in LEGACY_TARGETS:
            port_1201(game)
        else:
            port_26(game)
        print(f"Ported resources to {game}")


if __name__ == "__main__":
    main(sys.argv)
