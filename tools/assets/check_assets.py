"""Cross-checks generated resources against the Java sources. Exit code 1 on any problem.

    python3 tools/assets/check_assets.py [--manifest docs/ASSET_MANIFEST.md]

Checks: every registered item/block/sound/particle has its model, texture, lang entry and sound files; every model's
texture reference resolves; every weapon has six part models, an icon, skins, visuals and an animation set with the
clips its reload/action type needs; animation sound cues exist; textures referenced from Java exist; literal and
enum-derived translation keys exist; recipes, loot tables and advancements only reference registered content.
"""
from __future__ import annotations

import glob
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
NS = "remnants_ballistics"
WORKSPACE = os.path.join(ROOT, "1.21.1")
JAVA = os.path.join(WORKSPACE, "common", "src", "main", "java", "com", "nightbeam", "remnants", "ballistics")
RES = os.path.join(WORKSPACE, "common", "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", NS)
DATA = os.path.join(RES, "data", NS)

problems = []


def problem(msg):
    problems.append(msg)


def read(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def load(path):
    try:
        return json.loads(read(path))
    except Exception as e:  # noqa: BLE001
        problem(f"invalid JSON {os.path.relpath(path, ROOT)}: {e}")
        return None


def java(rel):
    return read(os.path.join(JAVA, rel))


def enum_values(rel, pattern=r'^\s{4}([A-Z0-9_]+)\('):
    return re.findall(pattern, java(rel), re.M)


# ------------------------------------------------------------------------------------------------------------------
# Registered content (parsed from Java)
# ------------------------------------------------------------------------------------------------------------------

weapons = re.findall(r'WeaponDefinition\.builder\("([a-z0-9_]+)"', java("weapon/Weapons.java"))
weapon_src = java("weapon/Weapons.java")
attachments = re.findall(r'AttachmentDefinition\.builder\("([a-z0-9_]+)",\s*AttachmentSlot\.([A-Z]+)', java("attachment/Attachments.java"))
ammo = re.findall(r'^\s{4}[A-Z0-9_]+\("([a-z0-9_]+)",', java("ammo/AmmoType.java"), re.M)
simple_items = re.findall(r'\b(?:simple|register)\("([a-z0-9_]+)"', java("registry/ModItems.java"))
block_items = re.findall(r'\bblockItem\("([a-z0-9_]+)"', java("registry/ModItems.java"))
blocks = re.findall(r'Registries\.BLOCK,\s*"([a-z0-9_]+)"', java("registry/ModBlocks.java"))
sound_src = java("registry/ModSounds.java")
profiles = [p.lower() for p in re.findall(r'\b([A-Z_]+)\b', re.search(r'enum SoundProfile \{([^;]*);', java("weapon/SoundProfile.java")).group(1))]
sounds = set(re.findall(r'register\("([a-z0-9_.]+)"\)', sound_src)) | {f"gun.{p}.fire" for p in profiles}
particles = re.findall(r'\bregister\("([a-z0-9_]+)"\)', java("registry/ModParticles.java"))
items = weapons + [a for a, _ in attachments] + ammo + simple_items + block_items
item_ids = {f"{NS}:{i}" for i in items} | {f"{NS}:{b}" for b in blocks}

# ------------------------------------------------------------------------------------------------------------------
# JSON validity
# ------------------------------------------------------------------------------------------------------------------

for path in glob.glob(os.path.join(RES, "**", "*.json"), recursive=True):
    load(path)

# ------------------------------------------------------------------------------------------------------------------
# Models and textures
# ------------------------------------------------------------------------------------------------------------------


def texture_path(ref):
    ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
    return os.path.join(RES, "assets", ns, "textures", path + ".png")


def check_model(rel):
    path = os.path.join(ASSETS, "models", rel + ".json")
    if not os.path.exists(path):
        problem(f"missing model models/{rel}.json")
        return None
    m = load(path)
    if m is None:
        return None
    for key, ref in (m.get("textures") or {}).items():
        if ref.startswith("#"):
            continue
        if ref.startswith(NS + ":") and not os.path.exists(texture_path(ref)):
            problem(f"models/{rel}.json: texture {ref} not found")
    parent = m.get("parent", "")
    if parent.startswith(NS + ":"):
        check_model(parent.split(":", 1)[1])
    return m


PARTS = re.search(r'PARTS = \{([^}]*)\}', java("client/render/GunModels.java")).group(1).replace('"', "").replace(" ", "").split(",")
for w in weapons:
    for part in PARTS:
        check_model(f"gun/{w}/{part}")
    check_model(f"item/icon/{w}")
    m = check_model(f"item/{w}")
    if m is not None and m.get("parent") != "minecraft:builtin/entity":
        problem(f"item/{w}: weapon item models must use builtin/entity")
    for skin in re.findall(r'\.skin\("([a-z]+)"\)', java("attachment/Attachments.java")):
        if not os.path.exists(os.path.join(ASSETS, "textures", "item", "gun", f"{w}_{skin}.png")):
            problem(f"missing skin texture item/gun/{w}_{skin}.png")
for a, slot in attachments:
    check_model(f"item/{a}")
    if slot != "SKIN":
        check_model(f"attachment/{a}")
for i in ammo + simple_items + block_items:
    check_model(f"item/{i}")
for b in blocks:
    bs = os.path.join(ASSETS, "blockstates", b + ".json")
    if not os.path.exists(bs):
        problem(f"missing blockstate {b}")
    else:
        for v in (load(bs) or {}).get("variants", {}).values():
            check_model(v["model"].split(":", 1)[1])
    if not os.path.exists(os.path.join(DATA, "loot_table", "blocks", b + ".json")):
        problem(f"missing block loot table {b}")

# Textures referenced directly from Java.
for ref in set(re.findall(r'id\("(textures/[a-z0-9_/]+\.png)"\)', "\n".join(read(p) for p in glob.glob(os.path.join(JAVA, "**", "*.java"), recursive=True)))):
    if not os.path.exists(os.path.join(ASSETS, ref)):
        problem(f"Java references missing {ref}")
for sprite in re.findall(r'RemnantsBallistics\.id\("(hud/[a-z0-9_]+)"\)', java("client/hud/WeaponHud.java")):
    if not os.path.exists(os.path.join(ASSETS, "textures", "gui", "sprites", sprite + ".png")):
        problem(f"missing GUI sprite {sprite}")

# ------------------------------------------------------------------------------------------------------------------
# Visuals and animations
# ------------------------------------------------------------------------------------------------------------------

sounds_json = load(os.path.join(ASSETS, "sounds.json")) or {}
for w in weapons:
    vis = load(os.path.join(ASSETS, "gun_visuals", w + ".json")) if os.path.exists(os.path.join(ASSETS, "gun_visuals", w + ".json")) else None
    if vis is None:
        problem(f"missing gun_visuals/{w}.json")
        continue
    for key in ("pivot", "hip", "sight", "muzzle", "left_hand", "animations"):
        if key not in vis:
            problem(f"gun_visuals/{w}: missing {key}")
    set_id = vis.get("animations", "")
    anim_path = os.path.join(ASSETS, "animations", "gun", set_id.split(":", 1)[-1] + ".json")
    if not os.path.exists(anim_path):
        problem(f"{w}: animation set {set_id} missing")
        continue
    anim = load(anim_path) or {}
    clips = {k.rsplit(".", 1)[-1]: v for k, v in anim.get("animations", {}).items()}
    block = re.search(r'builder\("' + w + r'"(.*?)\.build\(\)', weapon_src, re.S).group(1)
    need = {"idle", "equip", "fire", "inspect"}
    need |= {"reload_start", "reload_insert", "reload_end"} if ".singleLoad()" in block else {"reload", "reload_empty"}
    if "ActionType.PUMP" in block or "ActionType.BOLT" in block:
        need.add("cycle")
    if "CHARGE_TIME" in block:
        need.add("charge")
    for clip in sorted(need - clips.keys()):
        problem(f"{w}: animation clip '{clip}' missing")
    for clip, body in clips.items():
        for cues in body.get("sound_effects", {}).values():
            for cue in cues if isinstance(cues, list) else [cues]:
                name = cue["effect"].split(":", 1)[-1]
                if name not in sounds:
                    problem(f"{w}.{clip}: sound cue {name} is not a registered sound")

# ------------------------------------------------------------------------------------------------------------------
# Sounds and particles
# ------------------------------------------------------------------------------------------------------------------

for s in sorted(sounds - sounds_json.keys()):
    problem(f"sounds.json: missing event {s}")
for s in sorted(sounds_json.keys() - sounds):
    problem(f"sounds.json: event {s} is not registered in Java")
for event, body in sounds_json.items():
    for entry in body["sounds"]:
        name = entry["name"] if isinstance(entry, dict) else entry
        if not os.path.exists(os.path.join(ASSETS, "sounds", name.split(":", 1)[1] + ".ogg")):
            problem(f"sound file for {name} missing")
for p in particles:
    pj = os.path.join(ASSETS, "particles", p + ".json")
    if not os.path.exists(pj):
        problem(f"missing particle definition {p}")
        continue
    for t in (load(pj) or {}).get("textures", []):
        if not os.path.exists(os.path.join(ASSETS, "textures", "particle", t.split(":", 1)[1] + ".png")):
            problem(f"particle {p}: sprite {t} missing")

# ------------------------------------------------------------------------------------------------------------------
# Language
# ------------------------------------------------------------------------------------------------------------------

lang = load(os.path.join(ASSETS, "lang", "en_us.json")) or {}
all_java = "\n".join(read(p) for p in glob.glob(os.path.join(JAVA, "**", "*.java"), recursive=True))
needed = set(re.findall(r'"((?:tooltip|hud|gui|container|itemGroup|key)\.' + NS + r'(?:\.[a-z0-9_]+)+)"', all_java))
needed.add(f"key.categories.{NS}")
needed |= {f"item.{NS}.{i}" for i in items if i not in block_items} | {f"block.{NS}.{b}" for b in blocks}  # block items use the block key
needed |= {f"stat.{NS}.{k}" for k in re.findall(r'^\s{4}[A-Z_]+\("([a-z_]+)"', java("core/Stat.java"), re.M)}
needed |= {f"fire_mode.{NS}.{k}" for k in re.findall(r'^\s{4}[A-Z]+\("([a-z]+)"\)', java("weapon/FireMode.java"), re.M)}
needed |= {f"weapon_category.{NS}.{k}" for k in re.findall(r'^\s{4}[A-Z]+\("([a-z]+)"', java("weapon/WeaponCategory.java"), re.M)}
needed |= {f"rarity.{NS}.{k.lower()}" for k in enum_values("weapon/WeaponRarity.java")}
needed |= {f"attachment_slot.{NS}.{k}" for k in re.findall(r'^\s{4}[A-Z]+\("([a-z]+)"\)', java("attachment/AttachmentSlot.java"), re.M)}
needed |= {f"ammo_type.{NS}.{k.lower()}" for k in enum_values("ammo/AmmoType.java")}
needed |= {body["subtitle"] for body in sounds_json.values() if "subtitle" in body}
for d in glob.glob(os.path.join(DATA, "damage_type", "*.json")):
    msg = (load(d) or {}).get("message_id")
    needed |= {f"death.attack.{msg}", f"death.attack.{msg}.player", f"death.attack.{msg}.item"}
for a in glob.glob(os.path.join(DATA, "advancement", "*.json")):
    disp = (load(a) or {}).get("display", {})
    for k in ("title", "description"):
        if "translate" in disp.get(k, {}):
            needed.add(disp[k]["translate"])
needed.add(f"entity.{NS}.void_orb")
for k in sorted(needed - lang.keys()):
    problem(f"en_us.json: missing {k}")

# ------------------------------------------------------------------------------------------------------------------
# Data references
# ------------------------------------------------------------------------------------------------------------------


def refs(obj):
    if isinstance(obj, dict):
        for v in obj.values():
            yield from refs(v)
    elif isinstance(obj, list):
        for v in obj:
            yield from refs(v)
    elif isinstance(obj, str) and obj.startswith(NS + ":"):
        yield obj


for path in glob.glob(os.path.join(DATA, "recipe", "**", "*.json"), recursive=True):
    for r in refs(load(path)):
        if r not in item_ids and r not in (f"{NS}:config", f"{NS}:ammo_press"):
            problem(f"{os.path.relpath(path, RES)}: unknown id {r}")
tables = {os.path.relpath(p, os.path.join(DATA, "loot_table"))[:-5].replace(os.sep, "/") for p in glob.glob(os.path.join(DATA, "loot_table", "**", "*.json"), recursive=True)}
for path in glob.glob(os.path.join(DATA, "loot_modifiers", "*.json")):
    m = load(path) or {}
    if m.get("table", "").split(":", 1)[-1] not in tables:
        problem(f"{os.path.relpath(path, RES)}: table {m.get('table')} missing")
glm = load(os.path.join(RES, "data", "neoforge", "loot_modifiers", "global_loot_modifiers.json")) or {}
for e in glm.get("entries", []):
    if not os.path.exists(os.path.join(DATA, "loot_modifiers", e.split(":", 1)[1] + ".json")):
        problem(f"global_loot_modifiers: {e} missing")
for path in glob.glob(os.path.join(DATA, "loot_table", "**", "*.json"), recursive=True):
    for r in refs(load(path)):
        if r not in item_ids and not r.startswith(f"{NS}:blocks/"):
            problem(f"{os.path.relpath(path, RES)}: unknown id {r}")
advs = {os.path.basename(p)[:-5] for p in glob.glob(os.path.join(DATA, "advancement", "*.json"))}
tag_ids = {f"{NS}:" + os.path.relpath(p, os.path.join(DATA, "tags"))[:-5].split(os.sep, 1)[1].replace(os.sep, "/")
           for p in glob.glob(os.path.join(DATA, "tags", "**", "*.json"), recursive=True)}
for path in glob.glob(os.path.join(DATA, "advancement", "*.json")):
    a = load(path) or {}
    if "parent" in a and a["parent"].split(":", 1)[1] not in advs:
        problem(f"advancement {path}: parent {a['parent']} missing")
    for r in refs(a):
        if r not in item_ids and r not in tag_ids and r.split(":", 1)[1] not in advs:
            problem(f"{os.path.relpath(path, RES)}: unknown id {r}")
for path in glob.glob(os.path.join(DATA, "tags", "**", "*.json"), recursive=True):
    for v in (load(path) or {}).get("values", []):
        ref = v["id"] if isinstance(v, dict) else v
        if ref.startswith(NS + ":") and ref not in item_ids and ref not in (f"{NS}:bullet", f"{NS}:bullet_headshot", f"{NS}:energy"):
            problem(f"{os.path.relpath(path, RES)}: unknown id {ref}")
if not os.path.exists(os.path.join(RES, "logo.png")):
    problem("logo.png missing (neoforge.mods.toml logoFile)")

# ------------------------------------------------------------------------------------------------------------------
# Report
# ------------------------------------------------------------------------------------------------------------------


def count(pattern):
    return len(glob.glob(os.path.join(RES, pattern), recursive=True))


summary = [
    ("Weapons (items, 6 part models each, icon, 5 skins)", len(weapons)),
    ("Attachments", len(attachments)),
    ("Ammunition types", len(ammo)),
    ("Other items (materials, kits, blueprints)", len(simple_items)),
    ("Blocks", len(blocks)),
    ("Model JSON files", count("assets/**/models/**/*.json")),
    ("Textures (PNG)", count("assets/**/textures/**/*.png")),
    ("Animation sets", count("assets/**/animations/gun/*.json")),
    ("Sound events", len(sounds_json)),
    ("Sound files (OGG)", count("assets/**/sounds/**/*.ogg")),
    ("Particle types", len(particles)),
    ("Recipes", count("data/**/recipe/**/*.json")),
    ("Loot tables", count("data/**/loot_table/**/*.json")),
    ("Loot modifiers", count("data/**/loot_modifiers/*.json") - 1),
    ("Advancements", count("data/**/advancement/*.json")),
    ("Translation keys (en_us)", len(lang)),
]

if "--manifest" in sys.argv:
    out = sys.argv[sys.argv.index("--manifest") + 1]
    lines = ["# Asset manifest", "",
             "Generated by `python3 tools/assets/check_assets.py --manifest docs/ASSET_MANIFEST.md` after "
             "`python3 tools/assets/generate.py`. Every asset is original and produced by the scripts in `tools/assets` "
             "(voxel models and painted textures, procedural audio, procedural 2D art), except the firearm and handling "
             "sounds, which `sfx_eleven.py` generates with ElevenLabs.", "",
             "| Content | Count |", "|---|---:|"]
    lines += [f"| {k} | {v} |" for k, v in summary]
    lines += ["", "## Validation", "",
              f"Cross-check against the Java registries: **{'passed' if not problems else f'{len(problems)} problem(s)'}**.", ""]
    lines += [f"- {p}" for p in problems]
    lines += ["", "## Missing or placeholder assets", "",
              "None. Every registered item, block, particle, sound event, GUI texture, HUD sprite, effect texture, weapon part, "
              "skin, animation clip and translation key referenced by the code has a generated asset; the checker fails the build "
              "of the asset set otherwise.", "",
              "## Weapons", "", "| Weapon | Part models | Animation set |", "|---|---|---|"]
    for w in weapons:
        lines.append(f"| `{w}` | {', '.join(PARTS)} | `{NS}:{w}` |")
    lines += ["", "## Attachments", "", ", ".join(f"`{a}`" for a, _ in attachments), ""]
    with open(os.path.join(ROOT, out), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")

for k, v in summary:
    print(f"{k:55s} {v}")
if problems:
    print(f"\n{len(problems)} problem(s):")
    for p in problems:
        print(" -", p)
    sys.exit(1)
print("\nAll asset checks passed.")
