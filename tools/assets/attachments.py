"""Attachment geometry. One function per attachment builds its voxel model.

Every attachment is modelled around its mount point at (8, 8, 8), which GunRenderer moves onto the weapon's mount:
optics sit on a rail whose top is y = 8 and put their sight line at 8 + GunRenderer.opticSightHeight; muzzle
devices start at z = 8 on the bore axis and extend toward -Z by GunRenderer.muzzleExtension; underbarrel grips hang
below y = 8; drum magazines hang below the magwell; stocks extend toward +Z from the receiver's rear face.
Extended and quick magazines are not drawn on weapons (the magazine part is scaled instead), and skins recolour the
weapon texture, so their models exist for inventory icons only.
"""
from __future__ import annotations

from kit import ring, stick_mag, tube
from voxel import VoxelModel


class Att:
    def __init__(self, name, slot):
        self.model = VoxelModel(name)
        self.name = name
        self.slot = slot

    def box(self, part, name, frm, to, mat, rot=None, hidden=(), decal=None):
        self.model.box(part, name, frm, to, mat, rot, hidden, decal)
        return self


def _octagon_y(a, name, x0, y0, z0, x1, y1, z1, mat):
    """Upright octagonal prism (a square box plus a copy turned 45 degrees about Y)."""
    a.box("body", name, (x0, y0, z0), (x1, y1, z1), mat)
    a.box("body", name + "_45", (x0, y0, z0), (x1, y1, z1), mat, rot=("y", 45, ((x0 + x1) / 2, y0, (z0 + z1) / 2)))


def _octagon_x(a, name, x0, y0, z0, x1, y1, z1, mat):
    """Octagonal prism along X (a square box plus a copy turned 45 degrees about X)."""
    a.box("body", name, (x0, y0, z0), (x1, y1, z1), mat)
    a.box("body", name + "_45", (x0, y0, z0), (x1, y1, z1), mat, rot=("x", 45, (x0, (y0 + y1) / 2, (z0 + z1) / 2)))


def _rail_clamp(a, z0, z1, mat="anodized", lever=True):
    a.box("body", "clamp_l", (6.45, 7.2, z0), (6.75, 8.5, z1), mat)
    a.box("body", "clamp_r", (9.25, 7.2, z0), (9.55, 8.5, z1), mat)
    zc = (z0 + z1) / 2
    if lever:
        a.box("body", "qd_lever", (9.55, 7.35, z0 + 0.3), (9.85, 8.15, z1 - 0.3), "steel")
        a.box("body", "qd_pivot", (9.85, 7.55, zc - 0.3), (10.05, 7.95, zc + 0.3), "steel_dark")
    else:
        a.box("body", "clamp_nut", (9.55, 7.45, zc - 0.4), (9.9, 8.05, zc + 0.4), "steel")


# ------------------------------------------------------------------------------------------------------------------
# Optics
# ------------------------------------------------------------------------------------------------------------------

def holo_reflex():
    """Compact open reflex sight; red dot at y = 10.5."""
    a = Att("holo_reflex", "optic")
    _rail_clamp(a, 6.8, 9.6, lever=False)
    a.box("body", "base", (6.45, 8.0, 6.6), (9.55, 8.7, 9.8), "anodized")
    a.box("body", "housing", (6.8, 8.7, 7.8), (9.2, 9.5, 9.8), "anodized")
    a.box("body", "emitter", (7.65, 9.5, 8.6), (8.35, 9.85, 9.2), "steel_dark")
    a.box("body", "hood_l", (6.8, 8.7, 6.8), (7.25, 12.0, 7.8), "anodized")
    a.box("body", "hood_r", (8.75, 8.7, 6.8), (9.2, 12.0, 7.8), "anodized")
    a.box("body", "hood_top", (6.8, 12.0, 6.8), (9.2, 12.4, 7.8), "anodized")
    a.box("body", "hood_top_bevel", (7.0, 12.4, 6.9), (9.0, 12.55, 7.7), "anodized")
    a.box("body", "sill", (7.25, 8.7, 6.8), (8.75, 9.0, 7.8), "anodized")
    a.box("body", "lens", (7.25, 9.0, 7.0), (8.75, 12.0, 7.1), "lens_clear")
    a.box("body", "dot", (7.9, 10.4, 6.94), (8.1, 10.6, 7.0), "glow_red")
    a.box("body", "button_up", (9.2, 8.85, 8.2), (9.4, 9.3, 8.7), "polymer")
    a.box("body", "button_down", (9.2, 8.85, 8.9), (9.4, 9.3, 9.4), "polymer")
    a.box("body", "battery_cap", (6.55, 8.8, 8.2), (6.8, 9.35, 9.4), "steel")
    return a


def tripoint_holo():
    """Holographic sight with a ring-and-dot reticle; sight line at y = 11.0."""
    a = Att("tripoint_holo", "optic")
    _rail_clamp(a, 7.4, 10.4)
    a.box("body", "base", (6.4, 8.0, 5.0), (9.6, 8.7, 11.0), "anodized")
    a.box("body", "housing", (6.6, 8.7, 8.4), (9.4, 9.9, 11.0), "anodized", decal="stamp")
    a.box("body", "button_l", (7.0, 9.0, 11.0), (7.6, 9.5, 11.15), "polymer")
    a.box("body", "button_r", (8.4, 9.0, 11.0), (9.0, 9.5, 11.15), "polymer")
    a.box("body", "side_button", (6.4, 9.0, 9.0), (6.6, 9.6, 10.4), "polymer")
    a.box("body", "wall_l", (6.4, 8.7, 5.2), (6.9, 13.25, 8.4), "anodized")
    a.box("body", "wall_r", (9.1, 8.7, 5.2), (9.6, 13.25, 8.4), "anodized")
    a.box("body", "roof", (6.4, 13.25, 5.2), (9.6, 13.75, 8.4), "anodized")
    a.box("body", "roof_ridge", (6.7, 13.75, 5.6), (9.3, 13.95, 8.0), "anodized")
    a.box("body", "sill", (6.9, 8.7, 5.2), (9.1, 8.75, 8.4), "anodized")
    a.box("body", "pane_front", (6.9, 8.75, 5.2), (9.1, 13.25, 5.3), "lens_clear")
    a.box("body", "pane_rear", (6.9, 8.75, 8.3), (9.1, 13.25, 8.4), "lens_clear")
    z0, z1 = 5.3, 5.36
    a.box("body", "ret_dot", (7.92, 10.92, z0), (8.08, 11.08, z1), "glow_red")
    a.box("body", "ret_top", (7.65, 11.6, z0), (8.35, 11.7, z1), "glow_red")
    a.box("body", "ret_bottom", (7.65, 10.3, z0), (8.35, 10.4, z1), "glow_red")
    a.box("body", "ret_left", (7.3, 10.65, z0), (7.4, 11.35, z1), "glow_red")
    a.box("body", "ret_right", (8.6, 10.65, z0), (8.7, 11.35, z1), "glow_red")
    return a


def ranger_scope():
    """4x prismatic combat scope with a fibre-optic illuminator; sight line at y = 11.0."""
    a = Att("ranger_scope", "optic")
    a.box("body", "base", (6.6, 8.0, 5.6), (9.4, 8.6, 10.4), "anodized")
    a.box("body", "riser", (7.0, 8.6, 6.0), (9.0, 9.2, 10.0), "anodized")
    a.box("body", "clamp_l", (6.45, 7.2, 5.8), (6.6, 8.6, 10.2), "anodized")
    a.box("body", "nut_1", (6.15, 7.4, 6.2), (6.45, 8.3, 7.1), "steel")
    a.box("body", "nut_2", (6.15, 7.4, 8.9), (6.45, 8.3, 9.8), "steel")
    a.box("body", "housing", (6.8, 9.2, 5.4), (9.2, 11.9, 10.6), "anodized", decal="stamp")
    a.box("body", "housing_bevel", (7.0, 11.9, 5.6), (9.0, 12.1, 10.4), "anodized")
    tube(a, "body", "objective", 11.0, 1.15, 3.6, 5.4, "anodized")
    ring(a, "body", "sunshade_lip", 11.0, 1.22, 3.6, 0.35, "anodized")
    a.box("body", "lens_front", (7.0, 10.0, 3.52), (9.0, 12.0, 3.6), "glass")
    tube(a, "body", "ocular", 11.0, 1.0, 10.6, 12.6, "anodized")
    ring(a, "body", "eyepiece", 11.0, 1.06, 12.0, 0.6, "rubber")
    a.box("body", "lens_rear", (7.15, 10.15, 12.6), (8.85, 11.85, 12.66), "glass")
    a.box("body", "fibre_housing", (7.4, 12.1, 6.0), (8.6, 12.5, 10.0), "anodized")
    a.box("body", "fibre", (7.7, 12.5, 6.2), (8.3, 12.6, 9.8), "glow_red")
    a.box("body", "turret_r", (9.2, 10.3, 7.4), (9.8, 11.5, 8.6), "steel_dark")
    a.box("body", "turret_l", (6.2, 10.3, 7.4), (6.8, 11.5, 8.6), "steel_dark")
    a.box("body", "turret_r_cap", (9.8, 10.5, 7.6), (9.95, 11.3, 8.4), "steel")
    a.box("body", "turret_l_cap", (6.05, 10.5, 7.6), (6.2, 11.3, 8.4), "steel")
    return a


def longeye_scope():
    """8x precision scope: sunshade, objective bell, turrets with zero marks, power ring; sight line at y = 11.0."""
    a = Att("longeye_scope", "optic")
    for i, z in enumerate((4.6, 10.6)):
        a.box("body", f"mount_base_{i}", (6.7, 8.0, z), (9.3, 8.7, z + 1.1), "anodized")
        a.box("body", f"mount_post_{i}", (7.1, 8.7, z), (8.9, 10.2, z + 1.1), "anodized")
        a.box("body", f"ring_{i}", (6.9, 10.0, z + 0.05), (9.1, 12.05, z + 1.05), "anodized")
        a.box("body", f"ring_cap_screw_{i}", (6.75, 11.6, z + 0.35), (6.9, 11.9, z + 0.75), "steel")
        a.box("body", f"mount_nut_{i}", (6.4, 7.4, z + 0.25), (6.7, 8.4, z + 0.85), "steel")
    tube(a, "body", "tube", 11.0, 0.85, 2.6, 12.6, "anodized")
    tube(a, "body", "bell_neck", 11.0, 1.1, 1.6, 2.6, "anodized")
    tube(a, "body", "bell", 11.0, 1.4, -1.0, 1.6, "anodized")
    tube(a, "body", "sunshade", 11.0, 1.45, -2.6, -1.0, "anodized")
    a.box("body", "lens_front", (6.9, 9.9, -2.66), (9.1, 12.1, -2.6), "glass")
    tube(a, "body", "saddle", 11.0, 1.0, 6.6, 9.0, "anodized")
    _octagon_y(a, "turret_elev", 7.25, 11.85, 7.05, 8.75, 12.9, 8.55, "steel_dark")
    _octagon_y(a, "turret_elev_cap", 7.4, 12.9, 7.2, 8.6, 13.2, 8.4, "grip")
    a.box("body", "elev_zero", (7.96, 12.3, 6.97), (8.04, 12.85, 7.05), "dot")
    _octagon_x(a, "turret_wind", 8.85, 10.25, 7.05, 9.9, 11.75, 8.55, "steel_dark")
    a.box("body", "wind_zero", (9.4, 11.7, 7.76), (9.85, 11.78, 7.84), "dot")
    _octagon_x(a, "turret_parallax", 6.25, 10.4, 7.2, 7.15, 11.6, 8.4, "steel_dark")
    ring(a, "body", "power_ring", 11.0, 1.0, 12.6, 1.0, "grip")
    a.box("body", "power_lever", (7.8, 11.95, 12.9), (8.2, 12.45, 13.3), "steel")
    tube(a, "body", "ocular", 11.0, 1.2, 13.6, 15.6, "anodized")
    ring(a, "body", "eyepiece", 11.0, 1.26, 15.0, 0.6, "rubber")
    a.box("body", "lens_rear", (7.1, 10.1, 15.6), (8.9, 11.9, 15.66), "glass")
    return a


# ------------------------------------------------------------------------------------------------------------------
# Muzzle devices
# ------------------------------------------------------------------------------------------------------------------

def suppressor():
    a = Att("suppressor", "muzzle")
    tube(a, "body", "collar", 8.0, 0.95, 6.6, 8.0, "steel_dark")
    a.box("body", "flat_l", (7.0, 7.6, 6.8), (7.08, 8.4, 7.8), "steel")
    a.box("body", "flat_r", (8.92, 7.6, 6.8), (9.0, 8.4, 7.8), "steel")
    tube(a, "body", "can", 8.0, 1.15, 1.6, 6.6, "anodized")
    for i, z in enumerate((2.2, 4.0, 5.8)):
        ring(a, "body", f"band_{i}", 8.0, 1.2, z, 0.3, "steel_dark")
    a.box("body", "serial", (9.13, 7.7, 2.8), (9.17, 8.3, 3.6), "steel")
    tube(a, "body", "end_cap", 8.0, 1.05, 1.0, 1.6, "steel_dark")
    for i, (dx, dy) in enumerate(((0, 0.75), (0, -0.75), (0.75, 0), (-0.75, 0))):
        a.box("body", f"cap_port_{i}", (8 + dx - 0.15, 8 + dy - 0.15, 0.94), (8 + dx + 0.15, 8 + dy + 0.15, 1.0), "recess")
    a.box("body", "bore", (7.7, 7.7, 0.94), (8.3, 8.3, 1.0), "recess")
    return a


def compensator():
    a = Att("compensator", "muzzle")
    tube(a, "body", "body", 8.0, 0.75, 5.0, 8.0, "steel_dark")
    ring(a, "body", "jam_nut", 8.0, 0.83, 7.55, 0.45, "steel")
    for i, z in enumerate((5.4, 6.1, 6.8)):
        a.box("body", f"port_top_{i}", (7.5, 8.69, z), (8.5, 8.77, z + 0.45), "recess")
    a.box("body", "port_l", (7.19, 7.7, 5.4), (7.27, 8.5, 6.4), "recess")
    a.box("body", "port_r", (8.73, 7.7, 5.4), (8.81, 8.5, 6.4), "recess")
    a.box("body", "face", (7.35, 7.35, 4.8), (8.65, 8.65, 5.0), "steel_dark")
    a.box("body", "bore", (7.7, 7.7, 4.75), (8.3, 8.3, 4.8), "recess")
    return a


def muzzle_brake():
    a = Att("muzzle_brake", "muzzle")
    a.box("body", "body", (7.05, 7.2, 5.0), (8.95, 8.8, 7.6), "steel_dark")
    tube(a, "body", "collar", 8.0, 0.75, 7.6, 8.0, "steel")
    for side, x0, x1 in (("l", 7.0, 7.05), ("r", 8.95, 9.0)):
        a.box("body", f"window_{side}_1", (x0, 7.5, 5.4), (x1, 8.5, 6.1), "recess")
        a.box("body", f"window_{side}_2", (x0, 7.5, 6.5), (x1, 8.5, 7.2), "recess")
    a.box("body", "top_slot", (7.6, 8.8, 5.6), (8.4, 8.85, 7.0), "recess")
    a.box("body", "face", (7.05, 7.2, 4.85), (8.95, 8.8, 5.0), "steel")
    a.box("body", "bore", (7.65, 7.65, 4.8), (8.35, 8.35, 4.85), "recess")
    return a


def flash_hider():
    a = Att("flash_hider", "muzzle")
    tube(a, "body", "body", 8.0, 0.78, 5.0, 8.0, "steel_dark")
    ring(a, "body", "collar", 8.0, 0.86, 7.4, 0.6, "steel")
    a.box("body", "slot_l", (7.18, 7.75, 5.1), (7.24, 8.25, 6.8), "recess")
    a.box("body", "slot_r", (8.76, 7.75, 5.1), (8.82, 8.25, 6.8), "recess")
    a.box("body", "slot_t", (7.75, 8.76, 5.1), (8.25, 8.82, 6.8), "recess")
    a.box("body", "face", (7.3, 7.3, 4.8), (8.7, 8.7, 5.0), "steel_dark")
    a.box("body", "bore", (7.6, 7.6, 4.75), (8.4, 8.4, 4.8), "recess")
    return a


def shotgun_choke():
    a = Att("shotgun_choke", "muzzle")
    tube(a, "body", "body", 8.0, 0.95, 6.0, 8.0, "steel")
    ring(a, "body", "knurl", 8.0, 1.02, 6.9, 0.7, "grip")
    a.box("body", "port_1", (7.6, 8.93, 6.15), (8.4, 9.0, 6.55), "recess")
    a.box("body", "port_2", (7.6, 7.0, 6.15), (8.4, 7.07, 6.55), "recess")
    a.box("body", "face", (7.25, 7.25, 5.85), (8.75, 8.75, 6.0), "steel_dark")
    a.box("body", "bore", (7.5, 7.5, 5.8), (8.5, 8.5, 5.85), "recess")
    return a


# ------------------------------------------------------------------------------------------------------------------
# Underbarrel
# ------------------------------------------------------------------------------------------------------------------

def _grip_clamp(a, z0, z1):
    a.box("body", "clamp", (6.8, 7.3, z0), (9.2, 8.0, z1), "polymer")
    a.box("body", "clamp_lip_l", (6.8, 8.0, z0), (7.1, 8.3, z1), "polymer")
    a.box("body", "clamp_lip_r", (8.9, 8.0, z0), (9.2, 8.3, z1), "polymer")
    a.box("body", "clamp_screw", (9.2, 7.45, (z0 + z1) / 2 - 0.4), (9.5, 7.95, (z0 + z1) / 2 + 0.4), "steel")


def vertical_grip():
    a = Att("vertical_grip", "underbarrel")
    _grip_clamp(a, 6.4, 9.6)
    a.box("body", "body", (7.05, 3.4, 7.0), (8.95, 7.3, 9.0), "grip")
    for i, y in enumerate((4.2, 5.2, 6.2)):
        a.box("body", f"finger_{i}", (7.2, y, 6.85), (8.8, y + 0.45, 7.0), "polymer")
    a.box("body", "switch_pad", (7.4, 6.0, 9.0), (8.6, 7.0, 9.15), "rubber")
    a.box("body", "cap", (6.95, 2.9, 6.9), (9.05, 3.4, 9.1), "polymer")
    a.box("body", "cap_seam", (7.4, 2.85, 7.4), (8.6, 2.9, 8.6), "recess")
    return a


def angled_grip():
    a = Att("angled_grip", "underbarrel")
    _grip_clamp(a, 5.0, 11.0)
    a.box("body", "ramp", (7.05, 6.4, 5.2), (8.95, 7.3, 8.4), "grip", rot=("x", 45, (8, 7.3, 5.2)))
    a.box("body", "block", (7.05, 5.0, 7.3), (8.95, 7.3, 10.6), "polymer")
    a.box("body", "stop", (7.05, 4.5, 9.6), (8.95, 5.0, 10.8), "polymer")
    a.box("body", "texture_l", (6.98, 5.4, 8.0), (7.05, 6.9, 10.2), "stipple")
    a.box("body", "texture_r", (8.95, 5.4, 8.0), (9.02, 6.9, 10.2), "stipple")
    return a


def stubby_grip():
    a = Att("stubby_grip", "underbarrel")
    _grip_clamp(a, 6.6, 9.4)
    a.box("body", "body", (7.05, 5.3, 7.1), (8.95, 7.3, 8.9), "grip")
    a.box("body", "cap", (6.95, 4.85, 7.0), (9.05, 5.3, 9.0), "polymer")
    a.box("body", "rib", (7.2, 6.2, 6.95), (8.8, 6.6, 7.1), "polymer")
    return a


# ------------------------------------------------------------------------------------------------------------------
# Magazines
# ------------------------------------------------------------------------------------------------------------------

def drum_mag():
    a = Att("drum_mag", "magazine")
    a.box("body", "tower", (7.05, 4.4, 6.6), (8.95, 8.0, 9.0), "steel_dark")
    a.box("body", "tower_rib", (6.95, 4.6, 7.4), (9.05, 7.2, 8.2), "steel_dark")
    _octagon_x(a, "drum", 6.6, 0.0, 5.6, 9.4, 4.4, 10.0, "polymer")
    _octagon_x(a, "cover_l", 6.35, 0.4, 6.0, 6.6, 4.0, 9.6, "polymer_grey")
    _octagon_x(a, "cover_r", 9.4, 0.4, 6.0, 9.65, 4.0, 9.6, "polymer_grey")
    a.box("body", "hub", (9.65, 1.7, 7.3), (9.95, 2.7, 8.3), "steel")
    a.box("body", "key", (9.95, 2.0, 6.6), (10.15, 2.4, 9.0), "steel")
    a.box("body", "window", (6.3, 1.8, 6.6), (6.35, 2.6, 9.0), "recess")
    for i, z in enumerate((6.8, 7.4, 8.0, 8.6)):
        a.box("body", f"round_{i}", (6.28, 1.9, z), (6.32, 2.5, z + 0.4), "brass")
    return a


def extended_mag():
    a = Att("extended_mag", "magazine")
    stick_mag(a, "body", 8.0, 6.8, 9.4, straight=7.6, curved=2.4, mat="ribs", base_mat="accent")
    a.box("body", "feed_lips", (7.2, 8.0, 7.0), (8.8, 8.4, 8.6), "steel_dark")
    a.box("body", "round", (7.5, 8.0, 7.2), (8.5, 8.5, 8.4), "brass")
    return a


def quick_mag():
    a = Att("quick_mag", "magazine")
    for i, cx in enumerate((6.9, 9.1)):
        a.box("body", f"mag_{i}", (cx - 0.9, 1.4, 6.8), (cx + 0.9, 8.0, 9.2), "ribs")
        a.box("body", f"base_{i}", (cx - 1.05, 0.8, 6.6), (cx + 1.05, 1.4, 9.5), "polymer")
        a.box("body", f"pull_tab_{i}", (cx - 0.35, 0.0, 7.6), (cx + 0.35, 0.8, 8.6), "accent")
        a.box("body", f"round_{i}", (cx - 0.5, 8.0, 7.2), (cx + 0.5, 8.4, 8.6), "brass")
    a.box("body", "coupler_top", (5.85, 5.6, 6.6), (10.15, 6.4, 9.4), "anodized")
    a.box("body", "coupler_bottom", (5.85, 2.6, 6.6), (10.15, 3.4, 9.4), "anodized")
    a.box("body", "coupler_screw", (10.15, 5.75, 7.6), (10.35, 6.25, 8.4), "steel")
    return a


# ------------------------------------------------------------------------------------------------------------------
# Stocks
# ------------------------------------------------------------------------------------------------------------------

def tactical_stock():
    a = Att("tactical_stock", "stock")
    ring(a, "body", "castle_nut", 8.0, 0.95, 8.0, 0.45, "steel_dark")
    tube(a, "body", "buffer_tube", 8.0, 0.8, 8.45, 15.0, "anodized")
    a.box("body", "body", (6.65, 4.8, 11.8), (9.35, 9.2, 18.4), "polymer")
    a.box("body", "cheek", (6.85, 9.2, 12.6), (9.15, 9.9, 17.6), "polymer")
    a.box("body", "toe", (6.85, 3.6, 15.2), (9.15, 4.8, 18.4), "polymer")
    a.box("body", "lever", (7.6, 4.4, 12.0), (8.4, 4.8, 14.6), "polymer_grey")
    a.box("body", "qd_cup", (9.35, 6.4, 16.6), (9.6, 7.2, 17.4), "steel_dark")
    a.box("body", "pad", (6.6, 3.6, 18.4), (9.4, 9.9, 19.2), "rubber")
    return a


def light_stock():
    a = Att("light_stock", "stock")
    a.box("body", "mount", (6.8, 7.0, 8.0), (9.2, 8.8, 9.2), "anodized")
    a.box("body", "rod_l", (6.8, 7.6, 9.2), (7.3, 8.1, 16.4), "steel")
    a.box("body", "rod_r", (8.7, 7.6, 9.2), (9.2, 8.1, 16.4), "steel")
    a.box("body", "rod_low", (7.75, 4.4, 11.8), (8.25, 4.9, 16.4), "steel")
    a.box("body", "rod_diag", (7.75, 6.75, 9.2), (8.25, 7.25, 12.8), "steel", rot=("x", 45, (8, 7.0, 9.2)))
    a.box("body", "plate", (6.6, 4.0, 16.4), (9.4, 9.0, 17.0), "steel_dark")
    a.box("body", "pad", (6.7, 4.1, 17.0), (9.3, 8.9, 17.5), "rubber")
    a.box("body", "release", (9.2, 7.3, 8.2), (9.5, 8.0, 9.0), "steel")
    return a


def heavy_stock():
    a = Att("heavy_stock", "stock")
    a.box("body", "mount", (6.7, 5.6, 8.0), (9.3, 9.6, 10.0), "anodized")
    a.box("body", "body", (6.7, 4.0, 10.0), (9.3, 8.4, 19.0), "anodized", decal="stamp")
    a.box("body", "cut_l", (6.64, 4.6, 11.0), (6.7, 6.2, 14.2), "recess")
    a.box("body", "cut_r", (9.3, 4.6, 11.0), (9.36, 6.2, 14.2), "recess")
    a.box("body", "riser", (6.9, 8.4, 11.6), (9.1, 9.6, 17.6), "polymer")
    a.box("body", "riser_gap", (6.95, 8.4, 11.8), (9.05, 8.55, 17.4), "recess")
    a.box("body", "riser_knob", (9.3, 7.4, 13.8), (9.8, 8.1, 14.6), "steel")
    a.box("body", "length_knob", (9.3, 6.0, 10.4), (9.8, 6.8, 11.2), "steel")
    a.box("body", "plate", (6.6, 3.0, 19.0), (9.4, 9.6, 19.6), "steel_dark")
    a.box("body", "pad", (6.7, 3.1, 19.6), (9.3, 9.5, 20.4), "rubber")
    a.box("body", "monopod", (7.6, 1.4, 16.4), (8.4, 4.0, 17.2), "steel")
    a.box("body", "monopod_foot", (7.2, 1.0, 16.0), (8.8, 1.4, 17.6), "rubber")
    return a


# ------------------------------------------------------------------------------------------------------------------
# Skin kits (inventory icons only)
# ------------------------------------------------------------------------------------------------------------------

def _skin_kit(skin):
    a = Att("skin_" + skin, "skin")
    mat = "kit_" + skin
    _octagon_y(a, "can", 6.6, 2.6, 6.6, 9.4, 9.0, 9.4, "steel_light")
    _octagon_y(a, "label", 6.5, 4.0, 6.5, 9.5, 7.8, 9.5, mat)
    _octagon_y(a, "base_ring", 6.5, 2.4, 6.5, 9.5, 2.8, 9.5, "steel")
    _octagon_y(a, "shoulder", 6.9, 9.0, 6.9, 9.1, 9.6, 9.1, "steel")
    _octagon_y(a, "cap", 7.4, 9.6, 7.4, 8.6, 10.6, 8.6, mat)
    a.box("body", "nozzle", (7.85, 10.0, 7.1), (8.15, 10.3, 7.4), "polymer")
    return a


def skin_desert():
    return _skin_kit("desert")


def skin_arctic():
    return _skin_kit("arctic")


def skin_crimson():
    return _skin_kit("crimson")


def skin_gilded():
    return _skin_kit("gilded")


def skin_void():
    return _skin_kit("void")


ALL = [holo_reflex, tripoint_holo, ranger_scope, longeye_scope, suppressor, compensator, muzzle_brake, flash_hider,
       shotgun_choke, vertical_grip, angled_grip, stubby_grip, extended_mag, drum_mag, quick_mag, tactical_stock,
       light_stock, heavy_stock, skin_desert, skin_arctic, skin_crimson, skin_gilded, skin_void]
