"""Mirrors the generated 1.21.1 resources into the other Minecraft workspaces, rewriting data that changed format.

    python3 tools/assets/port_resources.py           # every target
    python3 tools/assets/port_resources.py 1.20.1    # one target

tools/assets/generate.py writes 1.21.1/common/src/main/resources and runs this afterwards. Assets are copied as they
are; data packs are rewritten for each target's format (folder names, recipe results, conditions, advancement
predicates, convention tags, pack format, structure data version). Hand-written files in the target workspace
(META-INF, the access widener) are left alone. Standard library only, so CI can re-run it and diff the result.
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


def resources(game):
    return os.path.join(ROOT, game, "common", "src", "main", "resources")


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


# ----------------------------------------------------------------------------------------------------------------
# 1.20.1 (pack format 15, Forge 47 + Fabric)
# ----------------------------------------------------------------------------------------------------------------

# Registry folders that were plural before 1.21.
FOLDERS_1_20 = {"recipe": "recipes", "loot_table": "loot_tables", "advancement": "advancements", "structure": "structures"}
TAG_FOLDERS_1_20 = {"item": "items", "block": "blocks", "entity_type": "entity_types", "fluid": "fluids", "game_event": "game_events"}

# 1.21's unified "c" tags had a Forge and a (different) Fabric name before. Recipes use a tag of the mod's own that
# lists the vanilla item plus both conventions as optional entries, so one data pack works on both loaders.
CONVENTION_1_20 = {
    "c:ingots/steel": {"own": f"{NS}:ingots/steel", "forge": "forge:ingots/steel", "fabric": "c:steel_ingots", "vanilla": []},
    "c:ingots/brass": {"own": f"{NS}:ingots/brass", "forge": "forge:ingots/brass", "fabric": "c:brass_ingots", "vanilla": []},
    "c:glass_blocks": {"own": f"{NS}:glass_blocks", "forge": "forge:glass", "fabric": "c:glass_blocks", "vanilla": ["minecraft:glass"]},
    "c:glass_panes": {"own": f"{NS}:glass_panes", "forge": "forge:glass_panes", "fabric": "c:glass_panes", "vanilla": ["minecraft:glass_pane"]},
    "c:storage_blocks/iron": {"own": f"{NS}:storage_blocks/iron", "forge": "forge:storage_blocks/iron", "fabric": "c:iron_blocks",
                              "vanilla": ["minecraft:iron_block"]},
}
# Tags the mod adds its own items to, under each loader's name ("c:ingots" has no Fabric equivalent).
CONTRIBUTED_1_20 = {"c:ingots": ["forge:ingots"], "c:ingots/steel": ["forge:ingots/steel", "c:steel_ingots"],
                    "c:ingots/brass": ["forge:ingots/brass", "c:brass_ingots"]}
STRUCTURE_DATA_VERSION_1_20_1 = 3465
COOKING = {"minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"}


def _optional(tag):
    return {"id": "#" + tag, "required": False}


def _item_predicate_1_20(predicate):
    """1.21 item predicates name items as a string, "#tag" or a list; 1.20 uses "items": [...] or "tag"."""
    items = predicate.get("items")
    out = {k: v for k, v in predicate.items() if k != "items"}
    if isinstance(items, str) and items.startswith("#"):
        out["tag"] = items[1:]
    elif isinstance(items, str):
        out["items"] = [items]
    elif items is not None:
        out["items"] = items
    return out


def _ingredient_1_20(ingredient):
    if isinstance(ingredient, list):
        return [_ingredient_1_20(i) for i in ingredient]
    if "tag" in ingredient and ingredient["tag"] in CONVENTION_1_20:
        return {**ingredient, "tag": CONVENTION_1_20[ingredient["tag"]]["own"]}
    return ingredient


def recipe_1_20(recipe):
    recipe = dict(recipe)
    if "neoforge:conditions" in recipe:
        conditions = recipe.pop("neoforge:conditions")
        # Forge reads "conditions" before the Fabric key; keep the order of later versions otherwise.
        recipe = {**{k: v for k, v in recipe.items() if k != "fabric:load_conditions"}, "conditions": conditions,
                  **({"fabric:load_conditions": recipe["fabric:load_conditions"]} if "fabric:load_conditions" in recipe else {})}
    result = recipe.get("result")
    if isinstance(result, dict) and "id" in result:
        if recipe["type"] in COOKING:
            recipe["result"] = result["id"]
        else:
            recipe["result"] = {"item": result["id"], **{k: v for k, v in result.items() if k != "id"}}
    if "key" in recipe:
        recipe["key"] = {k: _ingredient_1_20(v) for k, v in recipe["key"].items()}
    if "ingredient" in recipe:
        recipe["ingredient"] = _ingredient_1_20(recipe["ingredient"])
    if "ingredients" in recipe:
        recipe["ingredients"] = [
            {**i, "ingredient": _ingredient_1_20(i["ingredient"])} if isinstance(i, dict) and "ingredient" in i else _ingredient_1_20(i)
            for i in recipe["ingredients"]]
    return recipe


def advancement_1_20(advancement):
    advancement = json.loads(json.dumps(advancement))
    icon = advancement.get("display", {}).get("icon")
    if icon and "id" in icon:
        advancement["display"]["icon"] = {"item": icon["id"], **{k: v for k, v in icon.items() if k != "id"}}
    for criterion in advancement.get("criteria", {}).values():
        if criterion["trigger"] == "minecraft:inventory_changed":
            conditions = criterion.get("conditions", {})
            conditions["items"] = [_item_predicate_1_20(p) for p in conditions.get("items", [])]
    return advancement


def loot_table_1_20(table):
    """Nested loot table entries name their table with "value" in 1.21 and "name" before."""
    def walk(node):
        if isinstance(node, dict):
            if node.get("type") == "minecraft:loot_table" and "value" in node:
                node = {("name" if k == "value" else k): v for k, v in node.items()}
            return {k: walk(v) for k, v in node.items()}
        if isinstance(node, list):
            return [walk(v) for v in node]
        return node
    return walk(table)


def tag_values_1_20(values):
    out = []
    for value in values:
        tag = value["id"] if isinstance(value, dict) else value
        if tag.startswith("#") and tag[1:] in CONVENTION_1_20:
            convention = CONVENTION_1_20[tag[1:]]
            out += [_optional(convention["forge"]), _optional(convention["fabric"])]
        else:
            out.append(value)
    return out


def structure_1_20_1(data):
    """Rewrites the DataVersion int of a gzipped structure NBT file."""
    raw = gzip.decompress(data)
    key = b"\x03" + struct.pack(">H", len("DataVersion")) + b"DataVersion"
    at = raw.index(key) + len(key)
    raw = raw[:at] + struct.pack(">i", STRUCTURE_DATA_VERSION_1_20_1) + raw[at + 4:]
    return gzip.compress(raw, mtime=0)


def port_data_1_20_1(src_data, dst_data):
    for namespace in sorted(os.listdir(src_data)):
        src_ns = os.path.join(src_data, namespace)
        if namespace == "c":
            continue  # contributed convention tags are written below under each loader's name
        if not os.path.isdir(src_ns):
            shutil.copyfile(src_ns, os.path.join(dst_data, namespace))
            continue
        for dirpath, _, files in os.walk(src_ns):
            for name in sorted(files):
                src = os.path.join(dirpath, name)
                rel = os.path.relpath(src, src_ns).replace(os.sep, "/")
                parts = rel.split("/")
                kind = parts[0]
                if kind == "tags" and len(parts) > 2:
                    parts[1] = TAG_FOLDERS_1_20.get(parts[1], parts[1])
                else:
                    parts[0] = FOLDERS_1_20.get(kind, kind)
                dst = os.path.join(dst_data, namespace, *parts)
                os.makedirs(os.path.dirname(dst), exist_ok=True)
                if not name.endswith(".json"):
                    with open(src, "rb") as f:
                        data = f.read()
                    with open(dst, "wb") as f:
                        f.write(structure_1_20_1(data) if kind == "structure" else data)
                    continue
                with open(src, encoding="utf-8") as f:
                    obj = json.load(f)
                if kind == "recipe":
                    obj = recipe_1_20(obj)
                elif kind == "advancement":
                    obj = advancement_1_20(obj)
                elif kind == "loot_table":
                    obj = loot_table_1_20(obj)
                elif kind == "tags":
                    obj = {**obj, "values": tag_values_1_20(obj["values"])}
                write_json(dst, obj)
    # The mod's own copies of the convention tags its recipes use.
    for convention in CONVENTION_1_20.values():
        own_ns, own_path = convention["own"].split(":")
        values = list(convention["vanilla"])
        for contributed, names in CONTRIBUTED_1_20.items():
            if convention["forge"] in names:
                values += _contributed_items(src_data, contributed)
        values += [_optional(convention["forge"]), _optional(convention["fabric"])]
        write_json(os.path.join(dst_data, own_ns, "tags", "items", own_path + ".json"), {"replace": False, "values": values})
    for contributed, names in CONTRIBUTED_1_20.items():
        for tag in names:
            ns, path = tag.split(":")
            write_json(os.path.join(dst_data, ns, "tags", "items", path + ".json"),
                       {"replace": False, "values": _contributed_items(src_data, contributed)})


def _contributed_items(src_data, tag):
    ns, path = tag.split(":")
    with open(os.path.join(src_data, ns, "tags", "item", path + ".json"), encoding="utf-8") as f:
        return json.load(f)["values"]


def port_1_20_1(src, dst):
    shutil.copytree(os.path.join(src, "assets"), os.path.join(dst, "assets"))
    shutil.copyfile(os.path.join(src, "logo.png"), os.path.join(dst, "logo.png"))
    os.makedirs(os.path.join(dst, "data"))
    port_data_1_20_1(os.path.join(src, "data"), os.path.join(dst, "data"))
    with open(os.path.join(src, "pack.mcmeta"), encoding="utf-8") as f:
        pack = json.load(f)
    write_json(os.path.join(dst, "pack.mcmeta"), {"pack": {"description": pack["pack"]["description"], "pack_format": 15}})


TARGETS = {"1.20.1": port_1_20_1}


def port(game):
    dst = resources(game)
    for name in GENERATED:
        path = os.path.join(dst, name)
        if os.path.isdir(path):
            shutil.rmtree(path)
        elif os.path.exists(path):
            os.remove(path)
    os.makedirs(dst, exist_ok=True)
    TARGETS[game](SOURCE, dst)
    print(f"resources: ported to {game}")


def main():
    for game in sys.argv[1:] or TARGETS:
        port(game)


if __name__ == "__main__":
    main()
