"""Generates reference docs from the Java sources so they never drift from the code.

    python3 tools/docs_gen.py

Writes docs/CONFIG.md (every config option with its default and range), docs/WEAPONS.md (roster and base stats)
and docs/ATTACHMENTS.md (attachments, fit rules and stat trade-offs).
"""
from __future__ import annotations

import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, ".."))
JAVA = os.path.join(ROOT, "1.21.1", "common", "src", "main", "java", "com", "nightbeam", "remnants", "ballistics")
DOCS = os.path.join(ROOT, "docs")

sys.path.insert(0, os.path.join(HERE, "assets"))
import data_gen  # noqa: E402
from lang import AMMO_ITEMS, ATTACHMENT_NAMES, MATERIAL_NAMES, STATS, WEAPON_NAMES  # noqa: E402


def read(rel):
    with open(os.path.join(JAVA, rel), encoding="utf-8") as f:
        return f.read()


def write(name, lines):
    os.makedirs(DOCS, exist_ok=True)
    with open(os.path.join(DOCS, name), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines).rstrip() + "\n")


# ------------------------------------------------------------------------------------------------------------------
# Config
# ------------------------------------------------------------------------------------------------------------------

def _java_string(expr):
    """Concatenated Java string literals -> text."""
    return "".join(re.findall(r'"((?:[^"\\]|\\.)*)"', expr)).replace('\\"', '"')


def _statements(code):
    """Splits Java code on semicolons that are outside string literals."""
    out, cur, in_str, esc = [], [], False, False
    for ch in code:
        if in_str:
            cur.append(ch)
            if esc:
                esc = False
            elif ch == "\\":
                esc = True
            elif ch == '"':
                in_str = False
        elif ch == '"':
            in_str = True
            cur.append(ch)
        elif ch == ";":
            out.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
    out.append("".join(cur))
    return out


def parse_config(rel):
    src = read(rel)
    body = src[src.index("static {"):]
    body = body[:body.index("SPEC = b.build()")]
    entries = []
    sections = []
    for stmt in _statements(body):
        stmt = " ".join(stmt.split())
        if "for (WeaponDefinition" in stmt:
            break
        if ".pop()" in stmt and "push(" not in stmt:
            if sections:
                sections.pop()
            continue
        comment = ""
        m = re.search(r'\.comment\((.*?)\)\s*\.(?:push|define)', stmt)
        if m:
            comment = _java_string(m.group(1))
        m = re.search(r'\.push\("([^"]+)"\)', stmt)
        if m:
            sections.append(m.group(1))
            entries.append(("section", ".".join(sections), comment, None, None))
            continue
        m = re.search(r'\.(define|defineInRange|defineEnum)\("([^"]+)",\s*(.*)\)\s*$', stmt)
        if not m:
            continue
        kind, name, args = m.groups()
        parts = [a.strip() for a in args.split(",")]
        if kind == "define":
            default, rng = parts[0], "true / false"
        elif kind == "defineEnum":
            enum_name = parts[0].split(".")[0]
            values = re.search(r'enum ' + enum_name + r' \{([^}]*)\}', src)
            default = parts[0].split(".")[-1]
            rng = " / ".join(v.strip() for v in values.group(1).split(",")) if values else enum_name
        else:
            default = parts[0]
            rng = f"{parts[1]} to {parts[2]}"
            if name == "crosshairColor":
                default, rng = "0xFFFFFF", "0x000000 to 0xFFFFFF"
        entries.append(("value", ".".join(sections + [name]), comment, default, rng))
    return entries


def config_doc():
    lines = ["# Configuration", "",
             "Generated from the config specs by `python3 tools/docs_gen.py`. Edit the files in game with **Mods → Remnants: Ballistics → Config**, "
             "or by hand while the game is closed.", "",
             "| File | Scope | Location |", "|---|---|---|",
             "| `remnants_ballistics-client.toml` | Each player's own visuals, HUD, controls and audio | `config/` |",
             "| `remnants_ballistics-common.toml` | Recipe toggles and loot injection (applies on `/reload` or world load) | `config/` |",
             "| `remnants_ballistics-server.toml` | Balance, hit zones, block interaction, anti-cheat, per-weapon overrides. Synced to clients so their prediction matches the server | `<world>/serverconfig/` (copy to `defaultconfigs/` for new worlds) |",
             ""]
    for title, rel in (("Client", "config/ClientConfig.java"), ("Common", "config/CommonConfig.java"), ("Server", "config/ServerConfig.java")):
        lines += [f"## {title}", ""]
        current = None
        rows = []

        def flush():
            if current is not None and rows:
                key, comment = current
                lines.extend([f"### `[{key}]`" + (f": {comment}" if comment else ""), "",
                              "| Option | Default | Range | Description |", "|---|---|---|---|"] + rows + [""])

        for kind, key, comment, default, rng in parse_config(rel):
            if kind == "section":
                flush()
                current, rows = (key, comment), []
                continue
            rows.append(f"| `{key.split('.')[-1]}` | `{default}` | {rng} | {comment or '-'} |")
        flush()
    lines += ["### `[weapons.<weapon>]`: per-weapon overrides", "",
              "One section per weapon (`r9_sidearm`, `arx_16`, …). Values are absolute and default to the weapon's base stats listed in "
              "[WEAPONS.md](WEAPONS.md).", "",
              "| Option | Description |", "|---|---|",
              "| `enabled` | `false` stops the weapon from firing (it can still be held and traded) |",
              "| `damage` | Damage per bullet or pellet, in half-hearts |",
              "| `fireRate` | Rounds per minute |",
              "| `magazine` | Magazine capacity |",
              "| `reloadTicks` | Tactical reload ticks (single-load weapons: ticks to insert one round) |",
              "| `emptyReloadTicks` | Empty reload ticks (single-load weapons: ticks to open and close the action) |",
              "| `recoilMultiplier` | Recoil multiplier for this weapon |",
              "| `spreadMultiplier` | Spread multiplier for this weapon |", ""]
    write("CONFIG.md", lines)


# ------------------------------------------------------------------------------------------------------------------
# Weapons and attachments
# ------------------------------------------------------------------------------------------------------------------

def _num(block, method, idx=0):
    m = re.search(r'\.' + method + r'\(([^)]*)\)', block)
    if not m:
        return None
    return m.group(1).split(",")[idx].strip().rstrip("f")


def weapons():
    src = read("weapon/Weapons.java")
    out = []
    for m in re.finditer(r'builder\("([a-z0-9_]+)",\s*([A-Z]+),\s*AmmoType\.([A-Z0-9_]+)\)(.*?)\.build\(\)', src, re.S):
        name, category, ammo, block = m.groups()
        stat = lambda s: (re.search(r'stat\(Stat\.' + s + r',\s*([0-9.]+)f?\)', block) or [None, None])[1]  # noqa: E731
        modes = re.search(r'\.modes\(([^)]*)\)', block).group(1).replace(" ", "").split(",")
        slots = re.search(r'\.slots\(([^)]*)\)', block)
        default = re.search(r'defaultAttachment\([A-Z]+,\s*"([a-z_]+)"\)', block)
        action = re.search(r'\.action\(ActionType\.([A-Z]+)\)', block)
        out.append({
            "name": name, "category": category.lower(), "ammo": ammo, "damage": _num(block, "damage"),
            "pellets": stat("PELLETS"), "rpm": _num(block, "rpm"), "magazine": _num(block, "magazine"),
            "reload": _num(block, "reload", 0), "reload_empty": _num(block, "reload", 1), "single": ".singleLoad()" in block,
            "modes": [m.lower() for m in modes], "rarity": re.search(r'WeaponRarity\.([A-Z]+)', block).group(1).capitalize(),
            "tier": _num(block, "tier"), "durability": _num(block, "durability"),
            "slots": [s.strip().lower() for s in slots.group(1).split(",")] if slots else [],
            "default": default.group(1) if default else None, "action": action.group(1).lower() if action else None,
            "projectile": ".projectile()" in block, "charge": stat("CHARGE_TIME"), "headshot": stat("HEADSHOT"),
            "falloff": re.search(r'\.falloff\(([^)]*)\)', block).group(1).replace("f", "").replace(" ", "").split(","),
            "velocity": re.search(r'\.ballistics\(([^)]*)\)', block).group(1).replace("f", "").replace(" ", "").split(",")[0],
            "integrated": "integratedOptic" in block,
        })
    return out


AMMO_LABEL = {"PISTOL_9MM": "9mm", "MAGNUM": "Magnum", "PDW_57": "5.7mm", "RIFLE_556": "5.56mm", "RIFLE_762": "7.62mm",
              "SHOTGUN_12G": "12 Gauge", "SNIPER_308": ".308", "HEAVY_50": ".50 BMG", "ENERGY_CELL": "Energy Cell"}
CATEGORY_LABEL = {"pistol": "Pistol", "smg": "SMG", "rifle": "Assault rifle", "shotgun": "Shotgun", "sniper": "Sniper rifle",
                  "lmg": "LMG", "experimental": "Experimental"}
CATEGORY_PLURAL = {"pistol": "pistols", "smg": "SMGs", "rifle": "assault rifles", "shotgun": "shotguns", "sniper": "sniper rifles",
                   "lmg": "LMGs", "experimental": "experimental weapons"}


def _ticks(t):
    return f"{int(t) / 20:.2f}s".replace(".00s", "s")


def weapons_doc(ws):
    lines = ["# Weapons", "", "Generated from `Weapons.java` by `python3 tools/docs_gen.py`. Damage is in half-hearts per bullet "
             "(per pellet for shotguns) before hit-zone multipliers; server owners can override every value in "
             "`remnants_ballistics-server.toml` (see [CONFIG.md](CONFIG.md)).", "",
             "| Weapon | Class | Ammo | Damage | RPM | Magazine | Reload (tactical / empty) | Fire modes | Rarity | Tier |",
             "|---|---|---|---:|---:|---:|---|---|---|---:|"]
    for w in ws:
        dmg = w["damage"] + (f" × {w['pellets']}" if w["pellets"] else "")
        if w["single"]:
            reload = f"{_ticks(w['reload'])} per round + {_ticks(w['reload_empty'])}"
        else:
            reload = f"{_ticks(w['reload'])} / {_ticks(w['reload_empty'])}"
        lines.append(f"| **{WEAPON_NAMES[w['name']]}** | {CATEGORY_LABEL[w['category']]} | {AMMO_LABEL[w['ammo']]} | {dmg} | {w['rpm']} | "
                     f"{w['magazine']} | {reload} | {', '.join(m.capitalize() for m in w['modes'])} | {w['rarity']} | {w['tier']} |")
    lines += ["", "## Details", ""]
    for w in ws:
        notes = []
        if w["action"] == "pump":
            notes.append("pump action: the pump is worked after every shot")
        elif w["action"] == "bolt":
            notes.append("bolt action: the bolt is cycled after every shot")
        elif w["action"] == "energy":
            notes.append("energy weapon")
        if w["projectile"]:
            notes.append("fires a slow void orb projectile that bursts on impact instead of a hitscan bullet")
        if w["charge"]:
            notes.append(f"must be charged for {_ticks(w['charge'])} before each shot")
        if w["single"]:
            notes.append("loads one round at a time; firing interrupts the reload")
        if w["default"]:
            notes.append(f"ships with the {ATTACHMENT_NAMES[w['default']]} fitted")
        if w["integrated"]:
            notes.append("integrated scope (no optic slot)")
        slots = ", ".join(s for s in w["slots"]) if w["slots"] else "none (cosmetic skins only)"
        lines += [f"### {WEAPON_NAMES[w['name']]} (`remnants_ballistics:{w['name']}`)", "",
                  f"- **Range:** full damage to {w['falloff'][0]} blocks, falling to {int(float(w['falloff'][2]) * 100)}% at {w['falloff'][1]} blocks; "
                  f"maximum range {w['falloff'][3]} blocks. Muzzle velocity {w['velocity']} blocks/s.",
                  f"- **Durability:** {w['durability']} shots. **Attachment slots:** {slots}."]
        if notes:
            lines.append("- " + "; ".join(n[0].upper() + n[1:] for n in notes) + ".")
        lines.append("")
    write("WEAPONS.md", lines)


def attachments_doc():
    src = read("attachment/Attachments.java")
    lines = ["# Attachments", "", "Generated from `Attachments.java` by `python3 tools/docs_gen.py`. Fit attachments at the **Gunsmith Workbench**: "
             "put the weapon in the centre slot and drop attachments into the slots around it. The stat panel previews every change "
             "before you take the weapon out. Percentages are relative to the weapon's stats.", "",
             "| Attachment | Slot | Fits | Effects | Rarity |", "|---|---|---|---|---|"]
    for m in re.finditer(r'builder\("([a-z_]+)",\s*AttachmentSlot\.([A-Z]+)\)(.*?)\.build\(\)', src, re.S):
        name, slot, block = m.groups()
        effects = []
        optic = re.search(r'\.optic\(OpticType\.([A-Z]+),\s*([0-9.]+)f?\)', block)
        if optic:
            effects.append(f"{optic.group(2).rstrip('0').rstrip('.')}× {'scope' if optic.group(1) == 'SCOPE' else 'reflex'} zoom")
        if ".suppressed()" in block:
            effects.append("suppressed: quiet report, no muzzle flash, no far-field echo")
        if ".hidesFlash()" in block:
            effects.append("hides the muzzle flash")
        for stat, pct in re.findall(r'percent\(([A-Z_]+),\s*(-?[0-9.]+)f?\)', block):
            label = STATS[stat.lower()]
            value = float(pct) * 100
            effects.append(f"{label} {'+' if value > 0 else '−'}{abs(value):g}%")
        skin = re.search(r'\.skin\("([a-z]+)"\)', block)
        if skin:
            effects.append(f"cosmetic {skin.group(1)} finish")
        only = re.search(r'\.only\(([^)]*)\)', block)
        exc = re.search(r'\.except\(([^)]*)\)', block)
        if only:
            fits = ", ".join(CATEGORY_PLURAL[c.strip().lower()] for c in only.group(1).split(","))
            fits = fits[0].upper() + fits[1:]
        elif exc:
            fits = "All except " + ", ".join(CATEGORY_PLURAL[c.strip().lower()] for c in exc.group(1).split(","))
        else:
            fits = "Every weapon with the slot" if slot != "SKIN" else "Every weapon"
        if ".magazineFed()" in block:
            fits += " (magazine-fed only)"
        rarity = re.search(r'WeaponRarity\.([A-Z]+)', block).group(1).capitalize()
        lines.append(f"| **{ATTACHMENT_NAMES[name]}** | {slot.capitalize()} | {fits} | {'; '.join(effects)} | {rarity} |")
    lines += ["", "Each weapon only offers the slots listed for it in [WEAPONS.md](WEAPONS.md); the workbench greys out the rest. "
              "Skins fit every weapon, including the experimental ones.", ""]
    write("ATTACHMENTS.md", lines)


def _label(item):
    if not item.startswith("#"):
        item = item.split(":", 1)[-1]
    if item.startswith("#"):
        return {"#c:ingots/steel": "Steel Ingot", "#c:ingots/brass": "Brass Ingot", "#minecraft:planks": "any Planks",
                "#minecraft:wool": "any Wool", "#minecraft:coals": "Coal or Charcoal",
                "#minecraft:stone_crafting_materials": "Cobblestone (any stone crafting material)"}.get(item, item)
    for table in (WEAPON_NAMES, ATTACHMENT_NAMES, MATERIAL_NAMES):
        if item in table:
            return table[item]
    if item in AMMO_ITEMS:
        return AMMO_ITEMS[item][0]
    if item in ("gunsmith_workbench", "ammo_press"):
        return {"gunsmith_workbench": "Gunsmith Workbench", "ammo_press": "Ammunition Press"}[item]
    return item.replace("_", " ").title()


def _counted(items):
    counts = {}
    for i in items:
        counts[_label(i)] = counts.get(_label(i), 0) + 1
    return ", ".join(f"{n}× {k}" if n > 1 else k for k, n in counts.items())


def progression_doc(ws):
    lines = ["# Crafting and progression", "",
             "Generated from the recipe and loot definitions in `tools/assets/data_gen.py` by `python3 tools/docs_gen.py`. "
             "Recipes show up in the recipe book and in JEI/EMI. Every recipe group can be switched off in "
             "`remnants_ballistics-common.toml` (see [CONFIG.md](CONFIG.md)).", "",
             "## Materials", "",
             "| Item | How to get it |", "|---|---|",
             "| Steel Ingot | Smelt an Iron Ingot (blast furnace: twice as fast). Also accepts other mods' `#c:ingots/steel` |",
             "| Brass Ingot | 3 Copper Ingots + 1 Gold Nugget → 3 (shapeless). Also accepts `#c:ingots/brass` |",
             "| Brass Casing | 1 Brass Ingot → 8 |",
             "| Shotgun Hull | 2 Paper + Red Dye + Brass Ingot → 6 |",
             "| Polymer Sheet | 2 Dried Kelp + Coal or Charcoal + Slime Ball → 4 |",
             "| Gun Parts | 4 Steel Ingots around an Iron Nugget → 2 |",
             "| Precision Parts | Gun Parts, 2 Gold Ingots, 2 Redstone and 4 Nether Quartz → 2 |",
             "| Remnant Alloy | Netherite Scrap + 2 Steel + 2 Brass + Precision Parts → 2. Also found in the Nether, ancient cities and end cities |",
             "| Void Core | An Echo Shard surrounded by 4 Ender Pearls and 4 Amethyst Shards. Also found in ancient cities and end cities |",
             "| Weapon Repair Kit | Gun Parts + Steel Ingot + Iron Ingot + Leather → 2. Restores durability at the Gunsmith Workbench (Gun Parts also repair weapons in an anvil) |",
             "| Advanced / Prototype Weapon Blueprint | Found in structures (see below). Copy one with Paper and Lapis Lazuli (advanced) or Paper, an Amethyst Shard and an Ender Pearl (prototype) |",
             "",
             "## Stations", "",
             "| Station | Recipe | Use |", "|---|---|---|",
             "| **Gunsmith Workbench** | 3 Steel Ingots, 4 Iron Ingots and a Crafting Table | Fit and remove attachments with a live stat preview; repair weapons with repair kits |",
             "| **Ammunition Press** | 2 Steel Ingots, a Piston, 2 Iron Ingots, Redstone and 3 Cobblestone | Presses ammunition from casings, propellant and projectiles |",
             "",
             "## Weapons by tier", "",
             "Blueprints are returned after crafting, so one blueprint unlocks a tier for good.", "",
             "| Tier | Weapon | Ingredients |", "|---:|---|---|"]
    for w in sorted(ws, key=lambda w: (int(w["tier"]), w["name"])):
        pattern, key = data_gen.WEAPON_RECIPES[w["name"]]
        items = [key[c] for row in pattern for c in row if c != " "]
        lines.append(f"| {w['tier']} | {WEAPON_NAMES[w['name']]} | {_counted(items)} |")
    lines += ["", "## Ammunition press", "", "Put the ingredients in any of the three input slots; the arrow lights up when they match.", "",
              "| Output | Ingredients |", "|---|---|"]
    for name, (ings, count) in data_gen.PRESS_RECIPES.items():
        lines.append(f"| {count}× {_label(name)} | " + ", ".join(f"{n}× {_label(i)}" for i, n in ings) + " |")
    lines += ["", "## Attachments", "", "Shapeless crafting-table recipes.", "", "| Attachment | Ingredients |", "|---|---|"]
    for name, ings in data_gen.ATTACHMENT_RECIPES.items():
        lines.append(f"| {ATTACHMENT_NAMES[name]} | {_counted(ings)} |")
    lines += ["", "## Where to find gear", "",
              "Loot is added to vanilla tables when they load (on every loader). Each source rolls its table with the listed chance "
              "(scaled by `lootChanceMultiplier`). Found weapons are partly worn.", "",
              "| Source | Chance | Highlights |", "|---|---:|---|"]
    tables = data_gen.loot_tables()
    for name, mod in data_gen.loot_injections().items():
        targets = mod["targets"]
        table = tables[mod["table"].split(":", 1)[1]]
        entries = [e for p in table["pools"] for e in p["entries"] if e["type"] == "minecraft:item"]
        top = ", ".join(_label(e["name"]) for e in entries)
        where = ", ".join(t.split(":", 1)[1].replace("chests/", "").replace("entities/", "").replace("_", " ") for t in targets)
        lines.append(f"| {where} | {int(mod['chance'] * 100)}% | {top} |")
    lines += ["", "Armed mobs only drop loot when killed by a player."]
    write("PROGRESSION.md", lines)


def main():
    config_doc()
    ws = weapons()
    weapons_doc(ws)
    attachments_doc()
    progression_doc(ws)
    print(f"docs: CONFIG.md, WEAPONS.md ({len(ws)} weapons), ATTACHMENTS.md, PROGRESSION.md")


if __name__ == "__main__":
    main()
