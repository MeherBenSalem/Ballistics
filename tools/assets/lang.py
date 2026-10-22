"""English (en_us) translations. Config keys are derived from the Java config sources so they never drift."""
from __future__ import annotations

import os
import re

NS = "remnants_ballistics"

WEAPON_NAMES = {
    "r9_sidearm": "R-9 Sidearm", "ironfang_50": "Ironfang .50", "warden_357": "Warden .357", "viper_9": "Viper-9",
    "spectre_pdw": "Spectre PDW", "arx_16": "ARX-16", "raven_ak": "Raven AK", "mantis_br": "Mantis BR",
    "breacher_12": "Breacher-12", "thunderpump": "Thunderpump", "ghostline_m24": "Ghostline M24", "longshot_50": "Longshot .50",
    "titan_56": "Titan-56", "bulwark_762": "Bulwark-762", "voidcaster": "Voidcaster", "eclipse_railgun": "Eclipse Railgun",
}

ATTACHMENT_NAMES = {
    "holo_reflex": "Holo Reflex Sight", "tripoint_holo": "Tripoint Holographic Sight", "ranger_scope": "Ranger 4x Scope",
    "longeye_scope": "Longeye 8x Scope", "suppressor": "Suppressor", "compensator": "Compensator", "muzzle_brake": "Muzzle Brake",
    "flash_hider": "Flash Hider", "shotgun_choke": "Shotgun Choke", "vertical_grip": "Vertical Grip", "angled_grip": "Angled Grip",
    "stubby_grip": "Stubby Grip", "extended_mag": "Extended Magazine", "drum_mag": "Drum Magazine", "quick_mag": "Quick-Swap Magazine",
    "tactical_stock": "Tactical Stock", "light_stock": "Light Stock", "heavy_stock": "Heavy Precision Stock",
    "skin_desert": "Desert Camo Kit", "skin_arctic": "Arctic Camo Kit", "skin_crimson": "Crimson Finish Kit",
    "skin_gilded": "Gilded Finish Kit", "skin_void": "Void Finish Kit",
}

AMMO_ITEMS = {
    "ammo_9mm": ("9mm Rounds", "pistol_9mm", "9mm"), "ammo_magnum": ("Magnum Rounds", "magnum", "Magnum"),
    "ammo_57": ("5.7mm Rounds", "pdw_57", "5.7mm"), "ammo_556": ("5.56mm Rounds", "rifle_556", "5.56mm"),
    "ammo_762": ("7.62mm Rounds", "rifle_762", "7.62mm"), "ammo_12g": ("12 Gauge Shells", "shotgun_12g", "12 Gauge"),
    "ammo_308": (".308 Rounds", "sniper_308", ".308"), "ammo_50bmg": (".50 BMG Rounds", "heavy_50", ".50 BMG"),
    "energy_cell": ("Energy Cell", "energy_cell", "Energy Cell"),
}

MATERIAL_NAMES = {
    "brass_ingot": "Brass Ingot", "steel_ingot": "Steel Ingot", "casing": "Brass Casing", "shotgun_hull": "Shotgun Hull",
    "polymer_sheet": "Polymer Sheet", "gun_parts": "Gun Parts", "precision_parts": "Precision Parts", "remnant_alloy": "Remnant Alloy",
    "void_core": "Void Core", "repair_kit": "Weapon Repair Kit", "blueprint_advanced": "Advanced Weapon Blueprint",
    "blueprint_prototype": "Prototype Weapon Blueprint",
}

STATS = {
    "damage": "Damage", "pellets": "Pellets", "fire_rate": "Fire Rate (RPM)", "magazine": "Magazine", "reload_time": "Reload Time",
    "empty_reload_time": "Empty Reload", "falloff_start": "Falloff Start", "falloff_end": "Falloff End", "min_damage": "Minimum Damage",
    "max_range": "Max Range", "hip_spread": "Hip Spread", "ads_spread": "Aimed Spread", "move_spread": "Moving Spread", "bloom": "Bloom",
    "max_bloom": "Max Bloom", "bloom_recovery": "Bloom Recovery", "recoil_vertical": "Vertical Recoil", "recoil_horizontal": "Horizontal Recoil",
    "recoil_recovery": "Recoil Recovery", "kick": "Kick", "ads_time": "Aim Time", "zoom": "Zoom", "move_speed": "Move Speed",
    "ads_move_speed": "Aimed Move Speed", "muzzle_velocity": "Muzzle Velocity", "gravity": "Bullet Drop", "penetration": "Penetration",
    "headshot": "Headshot Multiplier", "sway": "Sway", "loudness": "Loudness", "charge_time": "Charge Time",
}

CONFIG_LABELS = {
    "hud": "Weapon HUD", "controls": "Controls", "visuals": "Visual Effects", "audio": "Audio", "crafting": "Crafting", "loot": "Loot",
    "combat": "Combat", "handling": "Handling", "ammunition": "Ammunition", "blocks": "Block Interaction", "network": "Network",
    "weapons": "Weapon Overrides", "offsetX": "Offset X", "offsetY": "Offset Y", "dynamicCrosshair": "Dynamic Crosshair",
    "aimSensitivity": "Aim Sensitivity", "scaleSensitivityWithZoom": "Scale Sensitivity With Zoom", "viewmodelOffsetX": "Viewmodel Offset X",
    "viewmodelOffsetY": "Viewmodel Offset Y", "viewmodelOffsetZ": "Viewmodel Offset Z", "pvpDamageMultiplier": "PvP Damage Multiplier",
    "fxBroadcastRange": "Effects Broadcast Range", "maxAimDeviation": "Max Aim Deviation", "maxLagCompensationMs": "Max Lag Compensation (ms)",
    "fireRateToleranceTicks": "Fire Rate Tolerance (ticks)", "lootChanceMultiplier": "Loot Chance Multiplier", "reloadTicks": "Reload Ticks",
    "emptyReloadTicks": "Empty Reload Ticks",
}


def _humanize(key):
    if key in CONFIG_LABELS:
        return CONFIG_LABELS[key]
    words = re.sub(r"(?<!^)(?=[A-Z])", " ", key).split()
    return " ".join(w[:1].upper() + w[1:] for w in words)


def config_keys(java_config_dir):
    keys = {}
    pattern = re.compile(r'(?:define\w*|push)\("([A-Za-z0-9_]+)"')
    for fn in sorted(os.listdir(java_config_dir)):
        if not fn.endswith(".java"):
            continue
        with open(os.path.join(java_config_dir, fn), encoding="utf-8") as f:
            for name in pattern.findall(f.read()):
                keys[f"{NS}.configuration.{name}"] = _humanize(name)
    for w, label in WEAPON_NAMES.items():
        keys[f"{NS}.configuration.{w}"] = label
    keys[f"{NS}.configuration.title"] = "Remnants: Ballistics Configuration"
    keys[f"{NS}.configuration.section.{NS}.client.toml"] = "Client Settings"
    keys[f"{NS}.configuration.section.{NS}.client.toml.title"] = "Client Settings"
    keys[f"{NS}.configuration.section.{NS}.common.toml"] = "Common Settings"
    keys[f"{NS}.configuration.section.{NS}.common.toml.title"] = "Common Settings"
    keys[f"{NS}.configuration.section.{NS}.server.toml"] = "Server Settings"
    keys[f"{NS}.configuration.section.{NS}.server.toml.title"] = "Server Settings"
    return keys


def build(java_config_dir, subtitles):
    t = {}
    for w, n in WEAPON_NAMES.items():
        t[f"item.{NS}.{w}"] = n
    for a, n in ATTACHMENT_NAMES.items():
        t[f"item.{NS}.{a}"] = n
    for item, (name, ammo_type, short) in AMMO_ITEMS.items():
        t[f"item.{NS}.{item}"] = name
        t[f"ammo_type.{NS}.{ammo_type}"] = short
    for m, n in MATERIAL_NAMES.items():
        t[f"item.{NS}.{m}"] = n
    t[f"block.{NS}.gunsmith_workbench"] = "Gunsmith Workbench"
    t[f"block.{NS}.ammo_press"] = "Ammunition Press"
    t[f"entity.{NS}.void_orb"] = "Void Orb"
    t[f"itemGroup.{NS}.weapons"] = "Ballistics: Weapons"
    t[f"itemGroup.{NS}.ammunition"] = "Ballistics: Ammunition & Materials"
    t[f"itemGroup.{NS}.attachments"] = "Ballistics: Attachments"
    t[f"container.{NS}.gunsmith"] = "Gunsmith Workbench"
    t[f"container.{NS}.ammo_press"] = "Ammunition Press"
    t[f"gui.{NS}.insert_weapon"] = "Place a weapon in the centre slot to fit attachments and see its stats."
    t[f"gui.{NS}.repair"] = "Repair"
    t[f"gui.{NS}.ammo_press_hint"] = "Casings, propellant and projectiles"
    t[f"hud.{NS}.reloading"] = "RELOADING"
    t[f"hud.{NS}.broken"] = "BROKEN"
    t[f"hud.{NS}.press_reload"] = "Press %s to reload"
    t[f"hud.{NS}.no_ammo"] = "NO AMMO"
    tooltips = {
        "ammo": "Ammo: %s / %s", "fire_mode": "Fire mode: %s (%s)", "rarity": "Rarity: %s", "broken": "Broken - repair it at a Gunsmith Workbench",
        "attachments": "Attachments:", "tier": "Tier %s", "hold_shift": "Hold Shift for full stats", "repair_kit": "Restores %s%% durability at a Gunsmith Workbench",
        "ammo_used_by": "Used by: %s", "energy_cell": "Powers experimental energy weapons", "zoom": "%sx magnification",
        "suppressed": "Suppresses gunshots and hides the muzzle flash", "hides_flash": "Hides the muzzle flash", "cosmetic": "Cosmetic finish",
        "fits": "Fits: %s", "magazine_fed_only": "Magazine-fed weapons only", "blueprint": "Unlocks tier %s weapon recipes",
        "blueprint_reusable": "Returned after crafting - never consumed",
    }
    for k, v in tooltips.items():
        t[f"tooltip.{NS}.{k}"] = v
    for k, v in STATS.items():
        t[f"stat.{NS}.{k}"] = v
    for k, v in (("semi", "Semi-Auto"), ("burst", "Burst"), ("auto", "Full Auto")):
        t[f"fire_mode.{NS}.{k}"] = v
    for k in ("common", "uncommon", "rare", "epic", "legendary"):
        t[f"rarity.{NS}.{k}"] = k.capitalize()
    for k, v in (("pistol", "Pistol"), ("smg", "Submachine Gun"), ("rifle", "Assault Rifle"), ("shotgun", "Shotgun"),
                 ("sniper", "Sniper Rifle"), ("lmg", "Light Machine Gun"), ("experimental", "Experimental")):
        t[f"weapon_category.{NS}.{k}"] = v
    for k in ("optic", "muzzle", "underbarrel", "magazine", "stock", "skin"):
        t[f"attachment_slot.{NS}.{k}"] = k.capitalize()
    t[f"key.categories.{NS}"] = "Remnants: Ballistics"
    t[f"key.{NS}.reload"] = "Reload"
    t[f"key.{NS}.fire_mode"] = "Switch Fire Mode"
    t[f"key.{NS}.inspect"] = "Inspect Weapon"
    deaths = {
        "bullet": ("%1$s was shot", "%1$s was shot by %2$s", "%1$s was shot by %2$s using %3$s"),
        "headshot": ("%1$s took one to the head", "%1$s was headshot by %2$s", "%1$s was headshot by %2$s using %3$s"),
        "energy": ("%1$s was unmade by void energy", "%1$s was vaporised by %2$s", "%1$s was vaporised by %2$s using %3$s"),
    }
    for k, (plain, player, item) in deaths.items():
        t[f"death.attack.{NS}.{k}"] = plain
        t[f"death.attack.{NS}.{k}.player"] = player
        t[f"death.attack.{NS}.{k}.item"] = item
    adv = {
        "root": ("Remnants: Ballistics", "Forge steel in a furnace - the first step toward a working firearm"),
        "gunsmith": ("Bench Rest", "Build a Gunsmith Workbench"),
        "first_weapon": ("Locked and Loaded", "Obtain your first firearm"),
        "ammo_press": ("Rolling Your Own", "Build an Ammunition Press"),
        "attachment": ("Tactical Upgrade", "Obtain a weapon attachment"),
        "blueprint": ("Classified Schematics", "Find an Advanced Weapon Blueprint"),
        "prototype": ("Prototype Program", "Find a Prototype Weapon Blueprint"),
        "void": ("Stare Into the Void", "Obtain the Voidcaster"),
        "legendary": ("Legendary Arsenal", "Obtain a legendary weapon"),
        "first_blood": ("First Blood", "Defeat a creature with a firearm"),
        "headshot": ("Between the Eyes", "Finish a creature with a headshot"),
        "long_shot": ("Ghost in the Line", "Defeat a creature from 100 or more blocks away"),
    }
    for k, (title, desc) in adv.items():
        t[f"advancements.{NS}.{k}.title"] = title
        t[f"advancements.{NS}.{k}.description"] = desc
    t.update(subtitles)
    t.update(config_keys(java_config_dir))
    return dict(sorted(t.items()))
