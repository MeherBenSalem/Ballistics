"""Voxel models for ammunition, crafting materials and utility items (rendered to 32x32 inventory icons)."""
from __future__ import annotations

from attachments import Att, _octagon_x, _octagon_y


def _cartridge(a, name, x, z, case_h, case_w, bullet_h, bullet_mat="copper", case_mat="brass", y=4.0, rim=True):
    hw = case_w / 2
    _octagon_y(a, name + "_case", x - hw, y, z - hw, x + hw, y + case_h, z + hw, case_mat)
    if rim:
        _octagon_y(a, name + "_rim", x - hw - 0.1, y - 0.3, z - hw - 0.1, x + hw + 0.1, y, z + hw + 0.1, case_mat)
    bw = hw * 0.8
    _octagon_y(a, name + "_bullet", x - bw, y + case_h, z - bw, x + bw, y + case_h + bullet_h * 0.65, z + bw, bullet_mat)
    tw = bw * 0.55
    _octagon_y(a, name + "_tip", x - tw, y + case_h + bullet_h * 0.65, z - tw, x + tw, y + case_h + bullet_h, z + tw, bullet_mat)


def _rounds(name, case_h, case_w, bullet_h, bullet_mat="copper", count=3, case_mat="brass"):
    a = Att(name, "item")
    spacing = case_w + 0.5
    start = 8 - spacing * (count - 1) / 2
    for i in range(count):
        _cartridge(a, f"r{i}", start + spacing * i, 8 + (0.6 if i % 2 else -0.6), case_h, case_w, bullet_h, bullet_mat, case_mat)
    return a


def ammo_9mm():
    return _rounds("ammo_9mm", 2.4, 1.6, 1.4, count=3)


def ammo_magnum():
    return _rounds("ammo_magnum", 3.4, 1.9, 1.8, "steel", count=3)


def ammo_57():
    return _rounds("ammo_57", 4.0, 1.4, 2.6, "dot", count=3)


def ammo_556():
    a = _rounds("ammo_556", 5.0, 1.5, 2.6, count=3)
    return a


def ammo_762():
    return _rounds("ammo_762", 5.0, 1.8, 2.8, "steel_dark", count=3, case_mat="steel")


def ammo_12g():
    a = Att("ammo_12g", "item")
    for i, x in enumerate((6.2, 9.8)):
        z = 8 + (0.6 if i else -0.6)
        _octagon_y(a, f"hull_{i}", x - 1.3, 5.0, z - 1.3, x + 1.3, 11.0, z + 1.3, "shell_red")
        _octagon_y(a, f"head_{i}", x - 1.35, 3.4, z - 1.35, x + 1.35, 5.0, z + 1.35, "brass")
        _octagon_y(a, f"crimp_{i}", x - 1.0, 11.0, z - 1.0, x + 1.0, 11.4, z + 1.0, "shell_red")
    return a


def ammo_308():
    return _rounds("ammo_308", 6.0, 1.9, 3.0, count=2)


def ammo_50bmg():
    a = Att("ammo_50bmg", "item")
    _cartridge(a, "r0", 8, 8, 8.0, 2.6, 3.8, "copper", y=2.0)
    a.box("body", "tip_mark", (7.45, 13.0, 6.9), (8.55, 13.5, 9.1), "red")
    return a


def energy_cell():
    a = Att("energy_cell", "item")
    a.box("body", "shell", (6.2, 3.0, 6.6), (9.8, 12.0, 9.4), "white")
    a.box("body", "cap_top", (6.0, 12.0, 6.4), (10.0, 12.8, 9.6), "gunmetal")
    a.box("body", "cap_bottom", (6.0, 2.2, 6.4), (10.0, 3.0, 9.6), "gunmetal")
    a.box("body", "window_s", (6.8, 4.2, 9.4), (9.2, 10.8, 9.5), "glow_cyan")
    a.box("body", "window_e", (9.8, 4.2, 7.2), (9.9, 10.8, 8.8), "glow_cyan")
    a.box("body", "contact", (7.4, 12.8, 7.4), (8.6, 13.4, 8.6), "brass")
    return a


def _ingot(name, mat, accent=None):
    a = Att(name, "item")
    a.box("body", "base", (3.0, 5.0, 5.4), (13.0, 7.4, 10.6), mat)
    a.box("body", "top", (3.8, 7.4, 6.0), (12.2, 8.6, 10.0), mat)
    if accent:
        a.box("body", "seam", (4.4, 8.6, 7.7), (11.6, 8.7, 8.3), accent)
        a.box("body", "seam_side", (4.4, 5.6, 10.6), (11.6, 6.0, 10.7), accent)
    return a


def brass_ingot():
    return _ingot("brass_ingot", "brass")


def steel_ingot():
    return _ingot("steel_ingot", "steel_light")


def remnant_alloy():
    return _ingot("remnant_alloy", "void_plate", accent="glow_cyan")


def casing():
    a = Att("casing", "item")
    _octagon_y(a, "case", 6.6, 3.0, 6.6, 9.4, 11.0, 9.4, "brass")
    _octagon_y(a, "rim", 6.4, 2.4, 6.4, 9.6, 3.0, 9.6, "brass")
    _octagon_y(a, "neck", 7.0, 11.0, 7.0, 9.0, 12.4, 9.0, "brass")
    a.box("body", "mouth", (7.5, 12.4, 7.5), (8.5, 12.45, 8.5), "recess")
    return a


def shotgun_hull():
    a = Att("shotgun_hull", "item")
    _octagon_y(a, "hull", 6.4, 5.0, 6.4, 9.6, 12.0, 9.6, "shell_red")
    _octagon_y(a, "head", 6.3, 3.0, 6.3, 9.7, 5.0, 9.7, "brass")
    a.box("body", "mouth", (7.2, 12.0, 7.2), (8.8, 12.05, 8.8), "recess")
    return a


def polymer_sheet():
    a = Att("polymer_sheet", "item")
    for i in range(3):
        y = 5.0 + i * 1.1
        dx = (i - 1) * 0.6
        a.box("body", f"sheet_{i}", (3.0 + dx, y, 4.0 - dx), (13.0 + dx, y + 0.9, 12.0 - dx), "polymer" if i != 1 else "polymer_grey")
    return a


def gun_parts():
    a = Att("gun_parts", "item")
    a.box("body", "bolt", (3.0, 5.0, 6.8), (10.0, 6.6, 8.4), "steel")
    a.box("body", "bolt_head", (2.2, 5.2, 7.0), (3.0, 6.4, 8.2), "steel_dark")
    a.box("body", "bolt_lug", (8.0, 6.6, 7.2), (9.0, 7.6, 8.0), "steel")
    for i in range(5):
        a.box("body", f"spring_{i}", (10.4 + i * 0.7, 4.6, 6.6), (10.8 + i * 0.7, 7.0, 8.6), "steel_light")
    a.box("body", "pin", (4.0, 3.0, 9.4), (11.0, 3.6, 10.0), "steel_light")
    a.box("body", "plate", (4.0, 8.4, 5.0), (8.0, 9.0, 9.0), "steel_dark")
    a.box("body", "screw", (5.4, 9.0, 6.4), (6.6, 9.4, 7.6), "steel")
    return a


def precision_parts():
    a = Att("precision_parts", "item")
    _octagon_y(a, "gear", 3.4, 4.0, 4.4, 9.4, 5.4, 10.4, "brass")
    _octagon_y(a, "gear_hub", 5.6, 5.4, 6.6, 7.2, 6.0, 8.2, "steel_light")
    for i, (dx, dz) in enumerate(((0, -3.4), (0, 3.4), (3.4, 0), (-3.4, 0))):
        a.box("body", f"tooth_{i}", (6.4 + dx - 0.5, 4.0, 7.4 + dz - 0.5), (6.4 + dx + 0.5, 5.4, 7.4 + dz + 0.5), "brass")
    _octagon_x(a, "barrel_blank", 8.0, 7.0, 7.6, 13.6, 8.8, 9.4, "steel")
    a.box("body", "bore", (13.6, 7.6, 8.2), (13.65, 8.2, 8.8), "recess")
    a.box("body", "spring", (9.0, 4.0, 10.4), (12.6, 4.6, 11.0), "steel_light")
    return a


def void_core():
    a = Att("void_core", "item")
    a.box("body", "core", (6.0, 6.0, 6.0), (10.0, 10.0, 10.0), "glow_violet")
    for i, (x0, x1) in enumerate(((5.0, 6.0), (10.0, 11.0))):
        a.box("body", f"cage_x_{i}", (x0, 5.0, 5.0), (x1, 11.0, 6.0), "void_plate")
        a.box("body", f"cage_x2_{i}", (x0, 5.0, 10.0), (x1, 11.0, 11.0), "void_plate")
    a.box("body", "cage_top", (5.0, 10.0, 6.0), (11.0, 11.0, 10.0), "void")
    a.box("body", "cage_bottom", (5.0, 5.0, 6.0), (11.0, 6.0, 10.0), "void")
    a.box("body", "top_glow", (7.0, 11.0, 7.0), (9.0, 11.2, 9.0), "glow_violet")
    return a


def repair_kit():
    a = Att("repair_kit", "item")
    a.box("body", "case", (3.0, 3.0, 5.0), (13.0, 9.0, 11.0), "olive")
    a.box("body", "lid", (2.8, 9.0, 4.8), (13.2, 10.2, 11.2), "olive")
    a.box("body", "band", (2.9, 7.0, 4.9), (13.1, 7.6, 11.1), "steel_dark")
    a.box("body", "latch_l", (5.0, 7.6, 11.1), (6.2, 9.4, 11.4), "steel")
    a.box("body", "latch_r", (9.8, 7.6, 11.1), (11.0, 9.4, 11.4), "steel")
    a.box("body", "handle_l", (6.0, 10.2, 7.4), (6.6, 11.6, 8.6), "steel_dark")
    a.box("body", "handle_r", (9.4, 10.2, 7.4), (10.0, 11.6, 8.6), "steel_dark")
    a.box("body", "handle", (6.0, 11.6, 7.4), (10.0, 12.2, 8.6), "rubber")
    a.box("body", "cross_v", (7.6, 4.0, 11.0), (8.4, 6.6, 11.1), "white")
    a.box("body", "cross_h", (6.7, 4.9, 11.0), (9.3, 5.7, 11.1), "white")
    return a


def _blueprint(name, mat):
    a = Att(name, "item")
    _octagon_x(a, "roll", 2.4, 6.0, 6.2, 13.6, 9.6, 9.8, mat)
    _octagon_x(a, "end_l", 1.8, 6.6, 6.8, 2.4, 9.0, 9.2, "white")
    _octagon_x(a, "end_r", 13.6, 6.6, 6.8, 14.2, 9.0, 9.2, "white")
    a.box("body", "sheet", (4.0, 4.4, 9.4), (12.0, 6.6, 9.6), mat)
    for i, x in enumerate((5.0, 7.4, 9.8)):
        a.box("body", f"line_{i}", (x, 4.8, 9.6), (x + 1.6, 5.0, 9.65), "white")
        a.box("body", f"line_b_{i}", (x + 0.4, 5.6, 9.6), (x + 1.2, 5.8, 9.65), "white")
    a.box("body", "ribbon", (7.4, 5.8, 5.9), (8.6, 9.9, 10.1), "red")
    return a


def blueprint_advanced():
    return _blueprint("blueprint_advanced", "paper_blue")


def blueprint_prototype():
    return _blueprint("blueprint_prototype", "kit_void")


AMMO = [ammo_9mm, ammo_magnum, ammo_57, ammo_556, ammo_762, ammo_12g, ammo_308, ammo_50bmg, energy_cell]
MATERIALS = [brass_ingot, steel_ingot, casing, shotgun_hull, polymer_sheet, gun_parts, precision_parts, remnant_alloy,
             void_core, repair_kit, blueprint_advanced, blueprint_prototype]
ALL = AMMO + MATERIALS
