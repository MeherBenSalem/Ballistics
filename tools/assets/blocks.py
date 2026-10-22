"""Voxel block models for the crafting stations (facing north; blockstates rotate them)."""
from __future__ import annotations

from attachments import Att, _octagon_x, _octagon_y


def gunsmith_workbench():
    a = Att("gunsmith_workbench", "block")
    # Steel-edged walnut top.
    a.box("body", "top", (0, 12, 0), (16, 15.2, 16), "wood")
    a.box("body", "edge_n", (0, 15.2, 0), (16, 16, 0.8), "steel_dark")
    a.box("body", "edge_s", (0, 15.2, 15.2), (16, 16, 16), "steel_dark")
    a.box("body", "edge_w", (0, 15.2, 0.8), (0.8, 16, 15.2), "steel_dark")
    a.box("body", "edge_e", (15.2, 15.2, 0.8), (16, 16, 15.2), "steel_dark")
    a.box("body", "mat", (0.8, 15.2, 0.8), (15.2, 15.6, 15.2), "rubber")
    # Legs, stretchers and lower shelf.
    for name, x, z in (("leg_nw", 1, 1), ("leg_ne", 12, 1), ("leg_sw", 1, 12), ("leg_se", 12, 12)):
        a.box("body", name, (x, 0, z), (x + 3, 12, z + 3), "steel_dark")
        a.box("body", name + "_foot", (x - 0.2, 0, z - 0.2), (x + 3.2, 0.6, z + 3.2), "rubber")
    a.box("body", "shelf", (2, 4, 2), (14, 6, 14), "steel")
    a.box("body", "apron_n", (4, 10.6, 1.4), (12, 12, 2.2), "steel_dark")
    a.box("body", "drawer", (4.4, 8.0, 1.2), (11.6, 10.6, 2.0), "steel")
    a.box("body", "drawer_handle", (6.8, 9.0, 0.8), (9.2, 9.6, 1.2), "steel_light")
    # Shelf contents: ammo can and a stack of parts.
    a.box("body", "ammo_can", (3.0, 6.0, 4.0), (8.0, 9.0, 7.0), "olive")
    a.box("body", "ammo_can_lid", (2.9, 9.0, 3.9), (8.1, 9.4, 7.1), "olive")
    a.box("body", "ammo_can_handle", (4.8, 9.4, 5.2), (6.2, 9.8, 5.8), "steel_dark")
    a.box("body", "parts_box", (9.4, 6.0, 8.0), (13.0, 7.6, 12.6), "polymer")
    # Bench vise (back right) and a rifle receiver on the mat.
    a.box("body", "vise_base", (10.6, 15.6, 10.0), (14.6, 16.6, 14.6), "steel_dark")
    a.box("body", "vise_jaw_a", (11.0, 16.6, 10.4), (14.2, 18.6, 11.6), "steel")
    a.box("body", "vise_jaw_b", (11.0, 16.6, 13.0), (14.2, 18.6, 14.2), "steel")
    a.box("body", "vise_screw", (12.3, 17.2, 14.2), (12.9, 17.8, 16.6), "steel_light")
    a.box("body", "vise_handle", (11.0, 17.3, 16.2), (14.2, 17.7, 16.6), "steel_light")
    a.box("body", "receiver", (2.0, 15.6, 4.4), (9.6, 17.0, 6.0), "anodized")
    a.box("body", "receiver_rail", (2.4, 17.0, 4.8), (9.2, 17.4, 5.6), "rail")
    a.box("body", "barrel", (9.6, 16.0, 4.9), (14.4, 16.6, 5.5), "steel_dark")
    a.box("body", "grip", (3.0, 15.6, 6.0), (4.4, 16.4, 8.0), "grip")
    a.box("body", "mag", (5.6, 15.6, 6.0), (6.8, 16.2, 8.6), "ribs")
    for i, x in enumerate((2.4, 3.6, 4.8)):
        a.box("body", f"round_{i}", (x, 15.6, 10.0), (x + 0.6, 16.6, 10.6), "brass")
    a.box("body", "screwdriver", (2.0, 15.6, 12.4), (7.0, 16.0, 12.8), "steel_light")
    a.box("body", "screwdriver_handle", (7.0, 15.6, 12.2), (9.0, 16.2, 13.0), "red")
    return a


def ammo_press():
    a = Att("ammo_press", "block")
    a.box("body", "base", (0, 0, 0), (16, 5.2, 16), "steel_dark")
    a.box("body", "base_top", (0.6, 5.2, 0.6), (15.4, 6.0, 15.4), "steel")
    a.box("body", "base_trim", (0, 2.2, -0.05), (16, 2.8, 0.0), "accent")
    a.box("body", "plate", (3, 6, 3), (13, 9, 13), "anodized")
    a.box("body", "die_ring", (5.6, 9.0, 5.6), (10.4, 9.6, 10.4), "steel_light")
    _octagon_y(a, "ram", 6.0, 9.6, 6.0, 10.0, 16.0, 10.0, "steel")
    _octagon_y(a, "ram_collar", 5.6, 13.2, 5.6, 10.4, 14.0, 10.4, "steel_dark")
    a.box("body", "column_back", (6.4, 6.0, 12.4), (9.6, 15.6, 15.0), "steel_dark")
    a.box("body", "head", (5.4, 14.6, 6.0), (10.6, 16.0, 15.0), "steel_dark")
    # Operating lever on the east side.
    a.box("body", "lever_pivot", (10.6, 14.8, 9.4), (11.6, 15.8, 10.4), "steel_light")
    a.box("body", "lever", (11.2, 14.9, 9.6), (15.6, 15.5, 10.2), "steel", rot=("z", -22.5, (11.2, 15.2, 9.9)))
    a.box("body", "lever_grip", (14.4, 12.4, 9.4), (16.0, 13.6, 10.4), "red")
    # Casing tray and primer tube.
    a.box("body", "tray", (1.0, 6.0, 1.0), (5.0, 6.8, 5.4), "steel")
    for i, (x, z) in enumerate(((1.6, 1.6), (2.8, 1.6), (4.0, 1.6), (1.6, 3.0), (2.8, 3.0), (4.0, 3.0), (1.6, 4.2))):
        a.box("body", f"case_{i}", (x, 6.8, z), (x + 0.8, 8.0, z + 0.8), "brass")
    _octagon_x(a, "primer_tube", 11.0, 6.4, 1.6, 15.4, 7.6, 2.8, "steel_light")
    a.box("body", "output_chute", (12.0, 6.0, 4.0), (15.2, 7.0, 8.0), "steel")
    for i, z in enumerate((4.4, 5.6, 6.8)):
        a.box("body", f"round_{i}", (12.6, 7.0, z), (14.6, 7.6, z + 0.6), "brass")
    return a


ALL = [gunsmith_workbench, ammo_press]
