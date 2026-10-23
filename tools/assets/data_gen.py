"""Server data: recipes, ammunition press recipes, loot tables and loot modifiers, tags, damage types, advancements."""
from __future__ import annotations

NS = "remnants_ballistics"


def _id(name):
    return name if ":" in name else f"{NS}:{name}"


def _ing(spec):
    """'#tag', 'item' or 'ns:item' -> ingredient JSON."""
    if spec.startswith("#"):
        return {"tag": spec[1:]}
    return {"item": spec if ":" in spec else (f"minecraft:{spec}" if spec in VANILLA else _id(spec))}


VANILLA = {
    "iron_ingot", "iron_nugget", "gold_ingot", "gold_nugget", "copper_ingot", "redstone", "glass_pane", "diamond",
    "crafting_table", "piston", "gunpowder", "blaze_powder", "paper", "red_dye", "dried_kelp", "slime_ball", "quartz",
    "netherite_scrap", "amethyst_shard", "ender_pearl", "echo_shard", "leather", "string", "lapis_lazuli", "sand",
    "brown_dye", "yellow_dye", "snowball", "white_dye", "light_gray_dye", "black_dye", "purple_dye",
}

def conditions(option):
    """Load conditions on both loaders, backed by the same common-config option (NeoForge ignores Fabric's key and back)."""
    return {"neoforge:conditions": [{"type": f"{NS}:config", "option": option}],
            "fabric:load_conditions": [{"condition": f"{NS}:config", "option": option}]}


def shaped(result, pattern, key, count=1, category="equipment", option=None, group=None):
    r = {"type": "minecraft:crafting_shaped", "category": category}
    if group:
        r["group"] = group
    r["pattern"] = pattern
    r["key"] = {k: _ing(v) for k, v in key.items()}
    r["result"] = {"id": _id(result), "count": count}
    if option:
        r.update(conditions(option))
    return r


def shapeless(result, ingredients, count=1, category="misc", option=None, group=None):
    r = {"type": "minecraft:crafting_shapeless", "category": category}
    if group:
        r["group"] = group
    r["ingredients"] = [_ing(i) for i in ingredients]
    r["result"] = {"id": _id(result), "count": count}
    if option:
        r.update(conditions(option))
    return r


STEEL = "#c:ingots/steel"
BRASS = "#c:ingots/brass"

WEAPON_RECIPES = {
    "r9_sidearm": (["SSP", " YY"], {"S": STEEL, "P": "gun_parts", "Y": "polymer_sheet"}),
    "warden_357": (["SSS", " PW", "  W"], {"S": STEEL, "P": "gun_parts", "W": "#minecraft:planks"}),
    "ironfang_50": (["SSS", "XQW", "  W"], {"S": STEEL, "X": "blueprint_advanced", "Q": "precision_parts", "W": "#minecraft:planks"}),
    "viper_9": (["SSS", "YPY", " Y "], {"S": STEEL, "P": "gun_parts", "Y": "polymer_sheet"}),
    "spectre_pdw": (["YSS", "XQY", " Y "], {"S": STEEL, "X": "blueprint_advanced", "Q": "precision_parts", "Y": "polymer_sheet"}),
    "arx_16": (["SSS", "PPY", "Y Y"], {"S": STEEL, "P": "gun_parts", "Y": "polymer_sheet"}),
    "raven_ak": (["SSS", "WPS", "W S"], {"S": STEEL, "P": "gun_parts", "W": "#minecraft:planks"}),
    "mantis_br": (["SSY", "XQY", "Y Y"], {"S": STEEL, "X": "blueprint_advanced", "Q": "precision_parts", "Y": "polymer_sheet"}),
    "breacher_12": (["SSS", "XQY", "YSY"], {"S": STEEL, "X": "blueprint_advanced", "Q": "precision_parts", "Y": "polymer_sheet"}),
    "thunderpump": (["SSS", "WPS", "W  "], {"S": STEEL, "P": "gun_parts", "W": "#minecraft:planks"}),
    "ghostline_m24": (["SSS", "ZQS", "YY "], {"S": STEEL, "Z": "blueprint_prototype", "Q": "precision_parts", "Y": "polymer_sheet"}),
    "longshot_50": (["AAS", "ZQS", "YYS"], {"S": STEEL, "A": "remnant_alloy", "Z": "blueprint_prototype", "Q": "precision_parts", "Y": "polymer_sheet"}),
    "titan_56": (["SSS", "XQS", "YPY"], {"S": STEEL, "X": "blueprint_advanced", "Q": "precision_parts", "P": "gun_parts", "Y": "polymer_sheet"}),
    "bulwark_762": (["SSS", "ZQS", "WPW"], {"S": STEEL, "Z": "blueprint_prototype", "Q": "precision_parts", "P": "gun_parts", "W": "#minecraft:planks"}),
    "voidcaster": (["AVA", "ZQA", "Y  "], {"A": "remnant_alloy", "V": "void_core", "Z": "blueprint_prototype", "Q": "precision_parts", "Y": "polymer_sheet"}),
    "eclipse_railgun": (["AVA", "ZQV", "AAY"], {"A": "remnant_alloy", "V": "void_core", "Z": "blueprint_prototype", "Q": "precision_parts", "Y": "polymer_sheet"}),
}

ATTACHMENT_RECIPES = {
    "holo_reflex": [STEEL, STEEL, "glass_pane", "redstone"],
    "tripoint_holo": [STEEL, STEEL, "glass_pane", "glass_pane", "redstone", "precision_parts"],
    "ranger_scope": [STEEL, STEEL, STEEL, "glass_pane", "glass_pane", "precision_parts"],
    "longeye_scope": [STEEL, STEEL, STEEL, "glass_pane", "glass_pane", "precision_parts", "diamond"],
    "suppressor": [STEEL, STEEL, STEEL, "#minecraft:wool", "polymer_sheet"],
    "compensator": [STEEL, STEEL, "gun_parts"],
    "muzzle_brake": [STEEL, STEEL, STEEL, "gun_parts"],
    "flash_hider": [STEEL, "gun_parts", "iron_nugget"],
    "shotgun_choke": [STEEL, "iron_nugget", "iron_nugget"],
    "vertical_grip": ["polymer_sheet", "polymer_sheet", "gun_parts"],
    "angled_grip": ["polymer_sheet", "polymer_sheet", "polymer_sheet", "gun_parts"],
    "stubby_grip": ["polymer_sheet", "gun_parts"],
    "extended_mag": [STEEL, STEEL, "polymer_sheet", "polymer_sheet", "gun_parts"],
    "drum_mag": [STEEL, STEEL, STEEL, "polymer_sheet", "polymer_sheet", "polymer_sheet", "precision_parts"],
    "quick_mag": [STEEL, STEEL, "polymer_sheet", "polymer_sheet", "string"],
    "tactical_stock": ["polymer_sheet", "polymer_sheet", "polymer_sheet", STEEL, STEEL, "gun_parts"],
    "light_stock": [STEEL, STEEL, "iron_ingot", "iron_ingot", "leather"],
    "heavy_stock": [STEEL, STEEL, STEEL, "polymer_sheet", "polymer_sheet", "precision_parts", "leather"],
    "skin_desert": ["polymer_sheet", "sand", "brown_dye", "yellow_dye"],
    "skin_arctic": ["polymer_sheet", "snowball", "white_dye", "light_gray_dye"],
    "skin_crimson": ["polymer_sheet", "red_dye", "red_dye", "black_dye"],
    "skin_gilded": ["polymer_sheet", "gold_ingot", "gold_ingot", "black_dye"],
    "skin_void": ["polymer_sheet", "ender_pearl", "purple_dye", "purple_dye"],
}

# Each press recipe uses a distinct set of item types so no recipe ever matches another's inputs.
PRESS_RECIPES = {
    "ammo_9mm": ([("casing", 4), ("gunpowder", 1), ("copper_ingot", 1)], 16),
    "ammo_magnum": ([("casing", 4), ("gunpowder", 2), ("iron_ingot", 1)], 12),
    "ammo_57": ([("casing", 4), ("gunpowder", 1), ("iron_nugget", 4)], 16),
    "ammo_556": ([("casing", 4), ("gunpowder", 2), (STEEL, 1)], 16),
    "ammo_762": ([("casing", 4), ("blaze_powder", 1), ("copper_ingot", 1)], 16),
    "ammo_308": ([("casing", 4), ("blaze_powder", 1), (STEEL, 1)], 8),
    "ammo_50bmg": ([("casing", 4), ("blaze_powder", 2), ("iron_ingot", 2)], 4),
    "ammo_12g": ([("shotgun_hull", 4), ("gunpowder", 1), ("iron_nugget", 4)], 8),
    "energy_cell": ([("redstone", 4), ("amethyst_shard", 2), ("gold_ingot", 1)], 4),
}


def recipes(weapons, attachments):
    out = {}
    for w in weapons:
        pattern, key = WEAPON_RECIPES[w]
        out[w] = shaped(w, pattern, key, option="weapon_crafting", group="remnants_ballistics_weapons")
    for a in attachments:
        out[a] = shapeless(a, ATTACHMENT_RECIPES[a], category="equipment", option="attachment_crafting",
                           group="remnants_ballistics_attachments")
    for name, (ingredients, count) in PRESS_RECIPES.items():
        ings = []
        for item, n in ingredients:
            ing = _ing(item)
            ing["count"] = n
            ings.append(ing)
        out[f"ammo_press/{name}"] = {"type": f"{NS}:ammo_press", "ingredients": ings, "result": {"id": _id(name), "count": count},
                                     **conditions("ammo_crafting")}
    out["steel_ingot_from_blasting"] = {"type": "minecraft:blasting", "category": "misc", "ingredient": {"item": "minecraft:iron_ingot"},
                                        "result": {"id": _id("steel_ingot")}, "experience": 0.7, "cookingtime": 100}
    out["steel_ingot_from_smelting"] = {"type": "minecraft:smelting", "category": "misc", "ingredient": {"item": "minecraft:iron_ingot"},
                                        "result": {"id": _id("steel_ingot")}, "experience": 0.7, "cookingtime": 300}
    out["brass_ingot"] = shapeless("brass_ingot", ["copper_ingot", "copper_ingot", "copper_ingot", "gold_nugget"], count=3)
    out["casing"] = shapeless("casing", [BRASS], count=8)
    out["shotgun_hull"] = shapeless("shotgun_hull", ["paper", "paper", "red_dye", BRASS], count=6)
    out["polymer_sheet"] = shapeless("polymer_sheet", ["dried_kelp", "dried_kelp", "#minecraft:coals", "slime_ball"], count=4)
    out["gun_parts"] = shaped("gun_parts", [" S ", "SNS", " S "], {"S": STEEL, "N": "iron_nugget"}, count=2, category="misc")
    out["precision_parts"] = shaped("precision_parts", ["QRQ", "GPG", "QRQ"], {"Q": "quartz", "R": "redstone", "G": "gold_ingot", "P": "gun_parts"},
                                    count=2, category="misc")
    out["remnant_alloy"] = shapeless("remnant_alloy", ["netherite_scrap", STEEL, STEEL, BRASS, BRASS, "precision_parts"], count=2)
    out["void_core"] = shaped("void_core", ["AEA", "EXE", "AEA"], {"A": "amethyst_shard", "E": "ender_pearl", "X": "echo_shard"}, category="misc")
    out["repair_kit"] = shapeless("repair_kit", ["gun_parts", STEEL, "iron_ingot", "leather"], count=2, category="equipment")
    out["blueprint_advanced_copy"] = shapeless("blueprint_advanced", ["blueprint_advanced", "paper", "lapis_lazuli"])
    out["blueprint_prototype_copy"] = shapeless("blueprint_prototype", ["blueprint_prototype", "paper", "amethyst_shard", "ender_pearl"])
    out["gunsmith_workbench"] = shaped("gunsmith_workbench", ["SSS", "ICI", "I I"], {"S": STEEL, "I": "iron_ingot", "C": "crafting_table"},
                                       category="misc")
    out["ammo_press"] = shaped("ammo_press", ["SPS", "IRI", "CCC"], {"S": STEEL, "P": "piston", "I": "iron_ingot", "R": "redstone",
                                                                     "C": "#minecraft:stone_crafting_materials"}, category="misc")
    return out


# ------------------------------------------------------------------------------------------------------------------
# Loot
# ------------------------------------------------------------------------------------------------------------------

def _item(name, weight, count=None, damaged=False):
    e = {"type": "minecraft:item", "name": _id(name), "weight": weight}
    fns = []
    if count:
        fns.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": count[0], "max": count[1]}})
    if damaged:
        fns.append({"function": "minecraft:set_damage", "damage": {"type": "minecraft:uniform", "min": 0.35, "max": 0.85}})
    if fns:
        e["functions"] = fns
    return e


def _table(rolls, entries, empty=0):
    pool = {"rolls": {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]} if isinstance(rolls, tuple) else rolls,
            "bonus_rolls": 0.0, "entries": entries}
    if empty:
        pool["entries"] = entries + [{"type": "minecraft:empty", "weight": empty}]
    return {"type": "minecraft:chest", "pools": [pool]}


def loot_tables():
    t = {}
    t["inject/outpost"] = _table((1, 3), [
        _item("ammo_9mm", 10, (6, 20)), _item("ammo_556", 8, (6, 20)), _item("ammo_12g", 6, (3, 10)), _item("gun_parts", 8, (1, 3)),
        _item("casing", 6, (4, 16)), _item("blueprint_advanced", 2), _item("r9_sidearm", 3, damaged=True), _item("viper_9", 2, damaged=True),
        _item("thunderpump", 2, damaged=True), _item("raven_ak", 1, damaged=True), _item("holo_reflex", 2), _item("compensator", 2),
        _item("vertical_grip", 2)])
    t["inject/village_weaponsmith"] = _table((1, 2), [
        _item("ammo_9mm", 10, (4, 12)), _item("steel_ingot", 8, (1, 4)), _item("gun_parts", 6, (1, 2)), _item("r9_sidearm", 2, damaged=True),
        _item("repair_kit", 3)], empty=10)
    t["inject/dungeon"] = _table((1, 2), [
        _item("ammo_9mm", 10, (4, 16)), _item("ammo_762", 6, (4, 12)), _item("casing", 8, (4, 16)), _item("gun_parts", 6, (1, 3)),
        _item("warden_357", 2, damaged=True), _item("raven_ak", 1, damaged=True), _item("ammo_magnum", 4, (3, 8)),
        _item("flash_hider", 2), _item("stubby_grip", 2)], empty=6)
    t["inject/stronghold"] = _table((1, 2), [
        _item("blueprint_advanced", 4), _item("precision_parts", 6, (1, 2)), _item("ranger_scope", 3), _item("tripoint_holo", 3),
        _item("spectre_pdw", 1, damaged=True), _item("mantis_br", 1, damaged=True), _item("ammo_57", 6, (8, 24)),
        _item("extended_mag", 3), _item("tactical_stock", 3)], empty=6)
    t["inject/desert"] = _table((1, 2), [
        _item("skin_desert", 4), _item("ammo_308", 6, (3, 8)), _item("ammo_556", 8, (6, 16)), _item("brass_ingot", 6, (2, 5)),
        _item("ghostline_m24", 1, damaged=True), _item("longeye_scope", 1), _item("muzzle_brake", 3)], empty=4)
    t["inject/jungle"] = _table((1, 2), [
        _item("ammo_9mm", 8, (6, 16)), _item("quick_mag", 3), _item("angled_grip", 3), _item("suppressor", 2),
        _item("viper_9", 2, damaged=True)], empty=6)
    t["inject/igloo"] = _table(1, [_item("skin_arctic", 1)])
    t["inject/shipwreck"] = _table((1, 2), [
        _item("ammo_12g", 10, (4, 12)), _item("thunderpump", 2, damaged=True), _item("shotgun_choke", 3), _item("shotgun_hull", 6, (4, 10))],
        empty=8)
    t["inject/nether_fortress"] = _table((1, 2), [
        _item("skin_crimson", 3), _item("ammo_magnum", 8, (4, 12)), _item("ironfang_50", 1, damaged=True), _item("breacher_12", 1, damaged=True),
        _item("remnant_alloy", 2), _item("blueprint_advanced", 3)], empty=6)
    t["inject/bastion"] = _table((1, 3), [
        _item("skin_gilded", 3), _item("remnant_alloy", 6, (1, 2)), _item("blueprint_prototype", 3), _item("ironfang_50", 2, damaged=True),
        _item("titan_56", 1, damaged=True), _item("bulwark_762", 1, damaged=True), _item("ammo_762", 8, (12, 32)), _item("drum_mag", 3),
        _item("heavy_stock", 2)])
    t["inject/ancient_city"] = _table((1, 2), [
        _item("void_core", 4), _item("blueprint_prototype", 4), _item("voidcaster", 1, damaged=True), _item("energy_cell", 8, (2, 6)),
        _item("longshot_50", 1, damaged=True), _item("ammo_50bmg", 4, (2, 6))], empty=4)
    t["inject/end_city"] = _table((1, 2), [
        _item("skin_void", 3), _item("eclipse_railgun", 1, damaged=True), _item("voidcaster", 2, damaged=True), _item("energy_cell", 8, (2, 8)),
        _item("void_core", 4), _item("remnant_alloy", 5, (1, 3)), _item("blueprint_prototype", 4)], empty=4)
    mob = _table(1, [_item("ammo_9mm", 6, (2, 6)), _item("ammo_556", 4, (2, 6)), _item("casing", 6, (1, 4)), _item("gun_parts", 2)], empty=10)
    mob["type"] = "minecraft:entity"
    t["inject/armed_mob"] = mob
    return t


def _any_table(*ids):
    terms = [{"condition": "neoforge:loot_table_id", "loot_table_id": i} for i in ids]
    return [terms[0]] if len(terms) == 1 else [{"condition": "minecraft:any_of", "terms": terms}]


def loot_modifiers():
    mods = {
        "outpost": (["minecraft:chests/pillager_outpost", "minecraft:chests/woodland_mansion"], "inject/outpost", "structure", 0.75),
        "village_weaponsmith": (["minecraft:chests/village/village_weaponsmith", "minecraft:chests/village/village_toolsmith"],
                                "inject/village_weaponsmith", "structure", 0.6),
        "dungeon": (["minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft", "minecraft:chests/trial_chambers/corridor"],
                    "inject/dungeon", "structure", 0.5),
        "stronghold": (["minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing", "minecraft:chests/stronghold_library"],
                       "inject/stronghold", "structure", 0.6),
        "desert": (["minecraft:chests/desert_pyramid"], "inject/desert", "structure", 0.6),
        "jungle": (["minecraft:chests/jungle_temple"], "inject/jungle", "structure", 0.6),
        "igloo": (["minecraft:chests/igloo_chest"], "inject/igloo", "structure", 0.35),
        "shipwreck": (["minecraft:chests/shipwreck_supply", "minecraft:chests/buried_treasure"], "inject/shipwreck", "structure", 0.5),
        "nether_fortress": (["minecraft:chests/nether_bridge"], "inject/nether_fortress", "structure", 0.5),
        "bastion": (["minecraft:chests/bastion_treasure", "minecraft:chests/bastion_other", "minecraft:chests/bastion_bridge"],
                    "inject/bastion", "structure", 0.55),
        "ancient_city": (["minecraft:chests/ancient_city", "minecraft:chests/ancient_city_ice_box"], "inject/ancient_city", "structure", 0.5),
        "end_city": (["minecraft:chests/end_city_treasure"], "inject/end_city", "structure", 0.5),
        "armed_mobs": (["minecraft:entities/pillager", "minecraft:entities/vindicator", "minecraft:entities/piglin_brute"],
                       "inject/armed_mob", "mob", 0.25),
    }
    out = {}
    for name, (targets, table, category, chance) in mods.items():
        cond = _any_table(*targets)
        if category == "mob":
            cond = cond + [{"condition": "minecraft:killed_by_player"}]
        out[name] = {"type": f"{NS}:configurable_table", "conditions": cond, "table": _id(table), "category": category, "chance": chance}
    return out


def block_loot(block):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                                                  "entries": [{"type": "minecraft:item", "name": _id(block)}],
                                                  "conditions": [{"condition": "minecraft:survives_explosion"}]}],
            "random_sequence": f"{NS}:blocks/{block}"}


# ------------------------------------------------------------------------------------------------------------------
# Tags, damage types, advancements
# ------------------------------------------------------------------------------------------------------------------

def _opt(tag):
    return {"id": tag, "required": False}


def tags(weapons, attachments, ammo, legendary):
    t = {
        "block/bullet_breakable": ["#minecraft:impermeable", "minecraft:glass_pane", _opt("#c:glass_panes"), _opt("#c:glass_blocks"),
                                   "#minecraft:flower_pots", "minecraft:sea_pickle"],
        "block/bullet_penetrable": ["#minecraft:leaves", "#minecraft:wool_carpets", "#minecraft:wooden_doors", "#minecraft:wooden_trapdoors",
                                    "#minecraft:wooden_fences", "minecraft:iron_bars", "minecraft:glass_pane", _opt("#c:glass_panes"),
                                    "minecraft:cobweb", "#minecraft:crops", "#minecraft:saplings", "minecraft:short_grass", "minecraft:tall_grass",
                                    "minecraft:fern", "minecraft:large_fern", "minecraft:sugar_cane", "minecraft:bamboo", "minecraft:scaffolding"],
        "block/impact/metal": ["#minecraft:anvil", "minecraft:iron_block", "minecraft:iron_bars", "minecraft:iron_door", "minecraft:iron_trapdoor",
                               "minecraft:chain", "minecraft:hopper", "minecraft:cauldron", "minecraft:gold_block", "minecraft:netherite_block",
                               "minecraft:lantern", "minecraft:soul_lantern", "minecraft:bell", "minecraft:copper_block", "minecraft:cut_copper",
                               "minecraft:exposed_copper", "minecraft:weathered_copper", "minecraft:oxidized_copper", "minecraft:heavy_weighted_pressure_plate",
                               "minecraft:light_weighted_pressure_plate", "minecraft:rail", "minecraft:powered_rail", "minecraft:detector_rail",
                               "minecraft:activator_rail", "minecraft:blast_furnace", "minecraft:smithing_table", "minecraft:lodestone",
                               _id("gunsmith_workbench"), _id("ammo_press"), _opt("#c:storage_blocks/iron")],
        "block/impact/wood": ["#minecraft:logs", "#minecraft:planks", "#minecraft:wooden_slabs", "#minecraft:wooden_stairs",
                              "#minecraft:wooden_fences", "#minecraft:fence_gates", "#minecraft:wooden_doors", "#minecraft:wooden_trapdoors",
                              "minecraft:chest", "minecraft:trapped_chest", "minecraft:barrel", "minecraft:crafting_table", "minecraft:bookshelf",
                              "minecraft:chiseled_bookshelf", "minecraft:lectern", "minecraft:composter", "minecraft:note_block", "minecraft:jukebox"],
        "block/impact/glass": ["#minecraft:impermeable", "minecraft:glass_pane", _opt("#c:glass_panes"), _opt("#c:glass_blocks"),
                               "minecraft:ice", "minecraft:packed_ice", "minecraft:blue_ice", "minecraft:sea_lantern", "minecraft:beacon"],
        "block/impact/soft": ["#minecraft:dirt", "#minecraft:sand", "minecraft:gravel", "minecraft:clay", "#minecraft:wool", "minecraft:snow_block",
                              "minecraft:snow", "#minecraft:leaves", "minecraft:soul_sand", "minecraft:soul_soil", "minecraft:mud",
                              "minecraft:moss_block", "minecraft:hay_block", "minecraft:sponge", "minecraft:wet_sponge", "minecraft:slime_block",
                              "minecraft:honey_block", "minecraft:farmland", "minecraft:dirt_path"],
        "entity_type/humanoid_hit_zones": ["minecraft:player", "minecraft:zombie", "minecraft:husk", "minecraft:drowned", "minecraft:zombie_villager",
                                           "minecraft:skeleton", "minecraft:stray", "minecraft:wither_skeleton", "minecraft:bogged",
                                           "minecraft:villager", "minecraft:wandering_trader", "minecraft:pillager", "minecraft:vindicator",
                                           "minecraft:evoker", "minecraft:illusioner", "minecraft:witch", "minecraft:piglin",
                                           "minecraft:piglin_brute", "minecraft:zombified_piglin", "minecraft:enderman", "minecraft:creeper"],
        "item/weapons": [_id(w) for w in weapons],
        "item/attachments": [_id(a) for a in attachments],
        "item/ammo": [_id(a) for a in ammo],
        "item/legendary_weapons": [_id(w) for w in legendary],
        "damage_type/bullets": [_id("bullet"), _id("bullet_headshot"), _id("energy")],
        "damage_type/headshot": [_id("bullet_headshot")],
    }
    vanilla = {
        "minecraft/tags/damage_type/is_projectile": [_id("bullet"), _id("bullet_headshot"), _id("energy")],
        "minecraft/tags/block/mineable/pickaxe": [_id("gunsmith_workbench"), _id("ammo_press")],
        "c/tags/item/ingots/steel": [_id("steel_ingot")],
        "c/tags/item/ingots/brass": [_id("brass_ingot")],
        "c/tags/item/ingots": [_id("steel_ingot"), _id("brass_ingot")],
    }
    return t, vanilla


def damage_types():
    return {
        "bullet": {"message_id": f"{NS}.bullet", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"},
        "bullet_headshot": {"message_id": f"{NS}.headshot", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"},
        "energy": {"message_id": f"{NS}.energy", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player", "effects": "burning"},
    }


def _has(item_or_tag):
    if item_or_tag.startswith("#"):
        return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": item_or_tag}]}}
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": _id(item_or_tag)}]}}


def _adv(parent, icon, key, criteria, frame="task", hidden=False, background=None, xp=None):
    display = {"icon": {"id": _id(icon)}, "title": {"translate": f"advancements.{NS}.{key}.title"},
               "description": {"translate": f"advancements.{NS}.{key}.description"}, "frame": frame,
               "show_toast": True, "announce_to_chat": True, "hidden": hidden}
    if background:
        display["background"] = background
    a = {"display": display, "criteria": criteria, "requirements": [list(criteria.keys())]}
    if parent:
        a["parent"] = _id(parent)
    if xp:
        a["rewards"] = {"experience": xp}
    return a


def advancements():
    kill_with = lambda tag, extra=None: {"trigger": "minecraft:player_killed_entity", "conditions": dict(  # noqa: E731
        {"killing_blow": {"tags": [{"id": f"{NS}:{tag}", "expected": True}]}}, **(extra or {}))}
    a = {
        "root": _adv(None, "steel_ingot", "root", {"steel": _has("steel_ingot")}, background="minecraft:textures/block/polished_blackstone.png"),
        "gunsmith": _adv("root", "gunsmith_workbench", "gunsmith", {"bench": _has("gunsmith_workbench")}),
        "first_weapon": _adv("root", "r9_sidearm", "first_weapon", {"weapon": _has(f"#{NS}:weapons")}),
        "ammo_press": _adv("first_weapon", "ammo_press", "ammo_press", {"press": _has("ammo_press")}),
        "attachment": _adv("gunsmith", "holo_reflex", "attachment", {"attachment": _has(f"#{NS}:attachments")}),
        "blueprint": _adv("first_weapon", "blueprint_advanced", "blueprint", {"blueprint": _has("blueprint_advanced")}, frame="goal"),
        "prototype": _adv("blueprint", "blueprint_prototype", "prototype", {"blueprint": _has("blueprint_prototype")}, frame="goal"),
        "void": _adv("prototype", "voidcaster", "void", {"void": _has("voidcaster")}, frame="challenge", xp=100),
        "legendary": _adv("prototype", "eclipse_railgun", "legendary", {"legendary": _has(f"#{NS}:legendary_weapons")}, frame="challenge", xp=200),
        "first_blood": _adv("first_weapon", "ammo_9mm", "first_blood", {"kill": kill_with("bullets")}),
        "headshot": _adv("first_blood", "ghostline_m24", "headshot", {"kill": kill_with("headshot")}, frame="goal"),
        "long_shot": _adv("headshot", "longshot_50", "long_shot",
                          {"kill": kill_with("bullets", {"entity": [{"condition": "minecraft:entity_properties", "entity": "this",
                                                                    "predicate": {"distance": {"absolute": {"min": 100}}}}]})},
                          frame="challenge", xp=150),
    }
    return a
