"""Weapon geometry and presentation data. One function per weapon builds its voxel model and visuals.

Coordinates are Java model pixels: barrel toward -Z (north), +Y up, weapon centred on x = 8.
Parts: body (static), action (slide/bolt/pump/cylinder/charging handle), magazine, stock, sights and muzzle
(the built-in muzzle device, hidden when a muzzle attachment is fitted).
`visuals` mirrors the gun_visuals JSON read by GunVisuals.java.
"""
from __future__ import annotations

from kit import (CX, GRIP_TILT, aperture_rear_sight, bottom_rail, butt_pad, crown, folding_front_sight, notch_rear_sight,
                 picatinny, pistol_grip, post_front_sight, ring, rot_point, screw, selector, serrations, sling_loop, stick_mag,
                 trigger, trigger_guard, tube)
from voxel import VoxelModel


class Gun:
    def __init__(self, name, animations, scale=0.55):
        self.model = VoxelModel(name)
        self.name = name
        self.visuals = {"animations": "remnants_ballistics:" + animations, "scale": scale}

    def box(self, part, name, frm, to, mat, rot=None, hidden=(), decal=None):
        self.model.box(part, name, frm, to, mat, rot, hidden, decal)
        return self

    def set(self, **kwargs):
        self.visuals.update(kwargs)
        return self


PISTOL_SPRINT = {"position": [0.0, -0.12, 0.08], "rotation": [-28, 20, 12]}
RIFLE_SPRINT = {"position": [0.04, -0.1, 0.06], "rotation": [-18, 42, 14]}


def long_gun(g, pivot, sight, muzzle, left_hand, mounts, pivots, hip=None, ads=0.26, shell="casing", flash=1.0, hip_rot=None,
             eject=None):
    g.set(pivot=pivot, hip=hip or [0.30, -0.35, -0.54], hip_rotation=hip_rot or [2, -7, 0], ads_distance=ads, sight=sight,
          muzzle=muzzle, eject=eject or [9.8, 9.4, 9], left_hand=left_hand, mounts=mounts, pivots=pivots, sprint=RIFLE_SPRINT,
          shell=shell, flash_size=flash)


# ==================================================================================================================
# R-9 Sidearm: striker-fired polymer service pistol
# ==================================================================================================================

def r9_sidearm():
    g = Gun("r9_sidearm", "pistol", scale=0.6)
    # Slide (action): main block, chamfered top, nose bevel, front/rear serrations, ejection port, extractor.
    g.box("action", "slide", (6.6, 8.7, 0.3), (9.4, 11.2, 15.2), "anodized", decal="stamp")
    g.box("action", "slide_top", (6.95, 11.2, 0.3), (9.05, 11.6, 15.2), "anodized")
    g.box("action", "slide_nose", (6.6, 9.6, -0.1), (9.4, 11.2, 0.9), "anodized", rot=("x", -22.5, (8, 11.2, 0.3)))
    serrations(g, "action", "serr_rear_l", 6.6, 9.0, 11.0, 11.8, 14.8, 7)
    serrations(g, "action", "serr_rear_r", 9.4, 9.0, 11.0, 11.8, 14.8, 7)
    serrations(g, "action", "serr_front_l", 6.6, 9.2, 10.9, 1.2, 3.4, 5)
    serrations(g, "action", "serr_front_r", 9.4, 9.2, 10.9, 1.2, 3.4, 5)
    g.box("action", "port_front", (8.6, 10.2, 4.85), (9.44, 11.64, 5.05), "recess")
    g.box("action", "port_rear", (8.6, 10.2, 8.55), (9.44, 11.64, 8.75), "recess")
    g.box("action", "barrel_hood", (7.4, 10.25, 5.05), (9.42, 11.66, 8.55), "steel")
    g.box("action", "extractor", (9.4, 10.0, 8.7), (9.55, 10.5, 10.6), "steel")
    g.box("action", "striker_plate", (7.0, 8.9, 15.2), (9.0, 11.2, 15.4), "steel_dark")
    g.box("action", "striker_dot", (7.8, 9.8, 15.4), (8.2, 10.2, 15.45), "accent")
    # Barrel muzzle peeking from the slide.
    ring(g, "body", "barrel_muzzle", 10.0, 0.65, -0.35, 0.4, "steel")
    crown(g, "body", "barrel_crown", 10.0, 0.65, -0.35)
    # Frame (polymer): dust cover with accessory rail, trigger guard, controls.
    g.box("body", "dust_cover", (6.8, 7.1, 0.9), (9.2, 8.7, 9.2), "polymer")
    bottom_rail(g, "body", "acc_rail", 7.1, 1.3, 5.0, width=2.0, mat="polymer")
    g.box("body", "frame", (6.8, 6.3, 8.6), (9.2, 8.7, 15.1), "polymer", decal="crest")
    g.box("body", "frame_tail", (6.95, 7.6, 15.1), (9.05, 8.7, 15.7), "polymer")
    trigger_guard(g, "body", 5.4, 10.6, 4.2, 7.1, "polymer", width=1.5, thick=0.6)
    trigger(g, "body", 8.6, 7.0, 4.9, "steel_dark", blade=True)
    g.box("body", "slide_stop_l", (6.62, 8.35, 7.4), (6.8, 8.85, 9.8), "steel_dark")
    g.box("body", "takedown_l", (6.62, 7.7, 6.3), (6.8, 8.15, 7.3), "steel_dark")
    g.box("body", "takedown_r", (9.2, 7.7, 6.3), (9.38, 8.15, 7.3), "steel_dark")
    g.box("body", "mag_release", (9.2, 6.6, 10.2), (9.42, 7.4, 10.9), "polymer")
    pivot = pistol_grip(g, "body", 10.2, 6.6, length=4.4, depth=7.6, width=2.5, mat="stipple", finger_grooves=True)
    g.box("body", "beavertail", (7.0, 6.4, 14.4), (9.0, 7.6, 16.0), "polymer", rot=("x", -22.5, (8, 7.0, 15.0)))
    # Magazine (follows the grip angle).
    g_origin = (8, 6.6, 12.4)
    rot = ("x", GRIP_TILT, g_origin)
    g.box("magazine", "mag", (7.1, -1.6, 10.6), (8.9, 6.4, 14.1), "steel_dark", rot=rot)
    g.box("magazine", "mag_base", (6.85, -2.45, 10.3), (9.15, -1.4, 14.6), "polymer", rot=rot)
    g.box("magazine", "mag_base_tab", (7.3, -2.6, 14.6), (8.7, -1.6, 15.0), "polymer", rot=rot)
    # Sights: three-dot.
    sight_y = notch_rear_sight(g, "sights", 11.6, 13.4, height=1.0)
    post_front_sight(g, "sights", 11.6, 0.9, height=0.65)
    g.set(pivot=pivot, hip=[0.27, -0.29, -0.56], hip_rotation=[0, -4, 0], ads_distance=0.46,
          sight=[8, sight_y, 14], muzzle=[8, 10.0, -0.55], eject=[9.6, 10.9, 6.8], left_hand=[7.4, 1.4, 12.0],
          mounts={"optic": [8, 11.6, 9.5], "muzzle": [8, 10.0, -0.55], "magazine": [8, 1.5, 12.2]},
          pivots={"action": [8, 10, 8], "magazine": [8, 1.5, 12.2]},
          sprint=PISTOL_SPRINT, shell="casing", flash_size=0.8)
    return g


# ==================================================================================================================
# ARX-16: modern direct-impingement carbine with free-float M-LOK handguard
# ==================================================================================================================

def arx_16():
    g = Gun("arx_16", "rifle", scale=0.5)
    # Upper receiver
    g.box("body", "upper", (6.5, 8.2, 3.6), (9.5, 11.0, 14.4), "anodized")
    g.box("body", "upper_brow", (9.5, 10.2, 3.6), (9.75, 11.0, 6.8), "anodized")
    rail_top = picatinny(g, "body", "upper_rail", 11.0, 3.7, 14.3)
    g.box("body", "port_recess", (9.45, 8.6, 7.0), (9.56, 10.15, 10.6), "recess")
    g.box("body", "dust_cover", (9.5, 7.9, 7.0), (9.72, 8.6, 10.6), "anodized")
    g.box("body", "brass_deflector", (9.5, 9.7, 10.75), (10.1, 10.8, 11.35), "anodized")
    g.box("body", "forward_assist", (9.5, 9.3, 11.4), (10.35, 10.3, 12.7), "anodized")
    g.box("body", "forward_assist_btn", (10.35, 9.5, 11.6), (10.55, 10.1, 12.5), "steel")
    g.box("action", "bolt_carrier", (9.48, 8.85, 7.6), (9.58, 9.9, 10.0), "steel")
    g.box("action", "ch_shaft", (7.3, 10.55, 14.4), (8.7, 10.95, 15.3), "anodized")
    g.box("action", "ch_tee", (6.6, 10.45, 15.0), (9.4, 11.05, 15.7), "anodized")
    g.box("action", "ch_latch", (6.35, 10.55, 14.95), (6.7, 10.95, 15.6), "steel")
    # Free-float handguard with M-LOK slots, top rail and short bottom rail.
    g.box("body", "hg_core", (6.6, 7.4, -9.0), (9.4, 11.0, 3.6), "anodized")
    g.box("body", "hg_side_l", (6.3, 7.9, -8.8), (6.6, 10.5, 3.4), "mlok")
    g.box("body", "hg_side_r", (9.4, 7.9, -8.8), (9.7, 10.5, 3.4), "mlok")
    g.box("body", "hg_bottom", (6.9, 7.1, -8.8), (9.1, 7.4, 3.4), "mlok")
    g.box("body", "hg_cap", (6.5, 7.5, -9.35), (9.5, 10.9, -9.0), "steel_dark")
    picatinny(g, "body", "hg_rail", 11.0, -8.9, 3.5)
    bottom_rail(g, "body", "hg_bottom_rail", 7.1, -7.6, -3.0)
    g.box("body", "barrel_nut", (6.7, 7.6, 3.4), (9.3, 10.8, 3.8), "steel_dark")
    sling_loop(g, "body", "qd_front", 9.7, 8.6, -7.5)
    # Barrel and built-in A2-style flash hider (muzzle part).
    tube(g, "body", "barrel", 9.4, 0.62, -14.4, -9.35, "steel_dark")
    tube(g, "body", "gas_block", 9.6, 0.9, -10.0, -9.4, "steel_dark")
    tube(g, "muzzle", "flash_hider", 9.4, 0.85, -15.7, -14.3, "steel_dark")
    g.box("muzzle", "fh_slot_l", (7.1, 9.1, -15.5), (7.16, 9.7, -14.6), "recess")
    g.box("muzzle", "fh_slot_r", (8.84, 9.1, -15.5), (8.9, 9.7, -14.6), "recess")
    g.box("muzzle", "fh_slot_t", (7.7, 10.24, -15.5), (8.3, 10.3, -14.6), "recess")
    crown(g, "muzzle", "fh_crown", 9.4, 0.7, -15.7)
    # Lower receiver: magwell, fire controls, stamped markings.
    g.box("body", "lower", (6.6, 5.6, 4.0), (9.4, 8.2, 13.6), "anodized", decal="stamp")
    g.box("body", "magwell", (6.65, 4.0, 4.6), (9.35, 5.6, 8.6), "anodized")
    g.box("body", "magwell_flare", (6.45, 3.6, 4.4), (9.55, 4.2, 8.8), "anodized")
    trigger_guard(g, "body", 8.8, 12.2, 3.9, 5.6, "anodized", width=1.5, thick=0.5)
    trigger(g, "body", 9.9, 5.6, 4.4, "steel")
    g.box("body", "mag_release", (9.4, 6.3, 8.7), (9.75, 7.0, 9.4), "steel")
    g.box("body", "bolt_catch", (6.3, 7.1, 8.9), (6.6, 8.1, 10.2), "anodized")
    selector(g, "body", 6.6, 7.0, 12.0)
    selector(g, "body", 9.4, 7.0, 12.0)
    screw(g, "body", "pivot_pin", 9.4, 7.5, 4.9)
    screw(g, "body", "takedown_pin", 9.4, 7.5, 13.1)
    pivot = pistol_grip(g, "body", 11.6, 5.7, length=3.0, depth=7.4, width=2.3, mat="grip")
    # Magazine: polymer, ribbed, curved, with baseplate lip.
    stick_mag(g, "magazine", 4.2, 4.9, 8.3, straight=5.0, curved=4.4, mat="ribs", base_mat="polymer")
    # Stock: buffer tube with castle nut, adjustable stock with cheek weld and QD sling point.
    ring(g, "stock", "castle_nut", 9.4, 1.0, 14.4, 0.5, "steel_dark")
    tube(g, "stock", "buffer_tube", 9.4, 0.82, 14.9, 21.5, "anodized")
    g.box("stock", "stock_body", (6.65, 6.2, 18.6), (9.35, 10.6, 26.4), "polymer")
    g.box("stock", "stock_cheek", (6.85, 10.6, 19.4), (9.15, 11.3, 25.6), "polymer")
    g.box("stock", "stock_toe", (6.85, 5.0, 22.2), (9.15, 6.2, 26.4), "polymer")
    g.box("stock", "stock_lever", (7.6, 7.7, 18.8), (8.4, 8.1, 21.6), "polymer_grey")
    sling_loop(g, "stock", "qd_rear", 9.35, 8.2, 24.5)
    butt_pad(g, "stock", 26.4, 4.8, 11.3, width=2.9)
    # Folding iron sights.
    rear_center = 13.25
    aperture_rear_sight(g, "sights", rail_top, 12.6, rear_center)
    folding_front_sight(g, "sights", rail_top, -7.4)
    long_gun(g, pivot, sight=[8, rear_center, 12.6], muzzle=[8, 9.4, -15.9], left_hand=[8, 6.4, -4.0],
             mounts={"optic": [8, rail_top, 9.5], "muzzle": [8, 9.4, -14.3], "underbarrel": [8, 6.3, -4.5],
                     "stock": [8, 9.4, 18.0], "magazine": [8, 4.2, 6.6]},
             pivots={"action": [8, 10.8, 15.0], "magazine": [8, 4.2, 6.6]}, eject=[9.7, 9.4, 8.8])
    return g


# ==================================================================================================================
# Ironfang .50: gas-operated magnum hand cannon
# ==================================================================================================================

def ironfang_50():
    g = Gun("ironfang_50", "pistol", scale=0.56)
    # Slide (action): heavy brushed steel with a ventilated top rib.
    g.box("action", "slide", (6.4, 8.5, 0.4), (9.6, 11.0, 15.6), "steel", decal="stamp")
    g.box("action", "slide_shoulder", (6.7, 11.0, 0.4), (9.3, 11.5, 15.6), "steel")
    serrations(g, "action", "serr_l", 6.4, 8.8, 10.8, 11.6, 15.2, 8, mat="steel")
    serrations(g, "action", "serr_r", 9.6, 8.8, 10.8, 11.6, 15.2, 8, mat="steel")
    g.box("action", "safety_l", (6.15, 9.6, 13.6), (6.4, 10.2, 15.0), "steel_dark")
    g.box("action", "safety_r", (9.6, 9.6, 13.6), (9.85, 10.2, 15.0), "steel_dark")
    g.box("action", "port_frame", (9.55, 9.4, 6.0), (9.62, 10.8, 9.8), "recess")
    g.box("action", "port_case", (9.58, 9.75, 7.0), (9.66, 10.45, 8.8), "brass")
    # Fixed barrel above the frame with a polygonal top and a full-length rail.
    g.box("body", "barrel_block", (6.9, 11.5, -2.4), (9.1, 12.6, 15.2), "steel_dark")
    g.box("body", "barrel_ridge", (7.25, 11.85, -2.4), (8.75, 13.35, 15.2), "steel_dark", rot=("z", 45, (8, 12.6, 6.4)))
    picatinny(g, "body", "top_rail", 12.6, -1.6, 13.4, width=1.6, mat="rail")
    g.box("body", "barrel_lower", (6.9, 8.5, -2.4), (9.1, 11.5, 0.4), "steel_dark")
    g.box("body", "muzzle_face", (6.8, 8.4, -2.6), (9.2, 12.65, -2.4), "steel")
    g.box("body", "bore", (7.45, 10.05, -2.66), (8.55, 11.15, -2.6), "recess")
    g.box("body", "gas_port", (7.5, 8.9, -2.66), (8.5, 9.5, -2.6), "recess")
    # Frame and grip with wood panels.
    g.box("body", "frame", (6.6, 6.0, 0.4), (9.4, 8.5, 15.2), "steel_dark", decal="crest")
    trigger_guard(g, "body", 4.6, 10.8, 3.6, 6.0, "steel_dark", width=1.6, thick=0.6)
    g.box("body", "guard_hook", (7.2, 2.8, 4.6), (8.8, 3.6, 5.3), "steel_dark")
    trigger(g, "body", 8.4, 6.0, 4.4, "brass")
    g.box("body", "slide_stop", (6.4, 7.9, 7.6), (6.6, 8.5, 10.4), "steel")
    g_origin = (8, 6.6, 12.6)
    rot = ("x", GRIP_TILT, g_origin)
    g.box("body", "grip_frame", (6.9, -2.2, 10.4), (9.1, 6.6, 14.8), "steel_dark", rot=rot)
    g.box("body", "grip_panel_l", (6.6, -1.4, 10.8), (6.9, 6.0, 14.4), "wood_dark", rot=rot)
    g.box("body", "grip_panel_r", (9.1, -1.4, 10.8), (9.4, 6.0, 14.4), "wood_dark", rot=rot)
    g.box("body", "grip_screw_l", (6.52, 2.0, 12.3), (6.6, 2.5, 12.8), "brass", rot=rot)
    g.box("body", "grip_screw_r", (9.4, 2.0, 12.3), (9.48, 2.5, 12.8), "brass", rot=rot)
    g.box("body", "hammer", (7.5, 9.4, 15.6), (8.5, 10.8, 16.4), "steel_dark")
    g.box("body", "hammer_spur", (7.6, 10.8, 15.8), (8.4, 11.4, 16.9), "steel_dark")
    g.box("body", "beavertail", (7.0, 6.4, 15.0), (9.0, 7.6, 16.6), "steel_dark", rot=("x", -22.5, (8, 7.0, 15.6)))
    g.box("magazine", "mag", (7.15, -2.0, 10.7), (8.85, 6.4, 14.5), "steel", rot=rot)
    g.box("magazine", "mag_base", (6.8, -2.95, 10.4), (9.2, -2.0, 15.0), "steel_dark", rot=rot)
    pivot = rot_point([8, 2.2, 12.6], "x", GRIP_TILT, g_origin)
    sight_y = notch_rear_sight(g, "sights", 13.35, 13.6, height=0.9)
    post_front_sight(g, "sights", 13.35, -1.8, height=0.6)
    g.set(pivot=pivot, hip=[0.27, -0.30, -0.58], hip_rotation=[0, -4, 0], ads_distance=0.48,
          sight=[8, sight_y, 14.2], muzzle=[8, 10.6, -2.6], eject=[9.8, 10.2, 7.8], left_hand=[7.3, 1.2, 12.4],
          mounts={"optic": [8, 13.45, 9.5], "muzzle": [8, 10.6, -2.6], "magazine": [8, 1.5, 12.6]},
          pivots={"action": [8, 10, 8], "magazine": [8, 1.5, 12.6]},
          sprint=PISTOL_SPRINT, shell="casing", flash_size=1.25)
    return g


# ==================================================================================================================
# Warden .357: double-action revolver with full underlug
# ==================================================================================================================

def warden_357():
    g = Gun("warden_357", "revolver", scale=0.58)
    tube(g, "body", "barrel", 9.6, 0.85, -6.4, 4.8, "steel")
    g.box("body", "underlug", (7.35, 7.2, -6.4), (8.65, 9.0, 3.2), "steel")
    g.box("body", "ejector_rod", (7.65, 7.6, 3.2), (8.35, 8.2, 5.0), "steel_light")
    g.box("body", "vent_rib", (7.45, 10.45, -6.4), (8.55, 10.85, 4.8), "steel_dark")
    for i, z in enumerate((-5.6, -3.6, -1.6, 0.4, 2.4)):
        g.box("body", f"rib_vent_{i}", (7.47, 10.5, z), (8.53, 10.83, z + 1.0), "recess")
    g.box("body", "front_ramp", (7.65, 10.85, -6.2), (8.35, 11.9, -4.8), "steel_dark")
    g.box("body", "front_insert", (7.62, 11.3, -5.4), (8.38, 11.75, -4.9), "red")
    crown(g, "body", "crown", 9.6, 0.7, -6.4)
    g.box("body", "frame_rear", (6.9, 5.8, 10.4), (9.1, 10.6, 13.8), "steel", decal="crest")
    g.box("body", "frame_bottom", (6.9, 5.8, 4.4), (9.1, 6.85, 10.4), "steel")
    g.box("body", "frame_front", (6.9, 5.8, 4.4), (9.1, 10.6, 5.0), "steel")
    g.box("body", "top_strap", (7.1, 10.6, 4.4), (8.9, 11.3, 11.6), "steel")
    g.box("body", "rear_notch_l", (7.1, 11.3, 10.8), (7.7, 12.0, 11.6), "steel_dark")
    g.box("body", "rear_notch_r", (8.3, 11.3, 10.8), (8.9, 12.0, 11.6), "steel_dark")
    g.box("body", "side_plate_l", (6.85, 6.4, 10.4), (6.9, 9.8, 13.4), "steel_dark")
    screw(g, "body", "plate_screw_1", 6.85, 7.0, 11.0)
    screw(g, "body", "plate_screw_2", 6.85, 9.2, 12.8)
    g.box("body", "cylinder_release", (6.65, 9.0, 10.6), (6.9, 9.7, 12.0), "steel_dark")
    g.box("body", "hammer", (7.5, 10.2, 13.6), (8.5, 11.8, 14.6), "steel_dark")
    g.box("body", "hammer_spur", (7.4, 11.8, 14.2), (8.6, 12.4, 15.6), "steel_dark", rot=("x", -22.5, (8, 12.0, 14.6)))
    trigger_guard(g, "body", 6.2, 11.2, 3.4, 5.8, "steel", width=1.4, thick=0.55)
    trigger(g, "body", 8.6, 5.8, 4.0, "steel_dark")
    g_origin = (8, 6.4, 13.4)
    rot = ("x", GRIP_TILT, g_origin)
    g.box("body", "grip_frame", (7.2, 0.0, 11.4), (8.8, 6.4, 15.4), "steel", rot=rot)
    g.box("body", "grip_wood", (6.6, -1.2, 11.2), (9.4, 6.0, 15.6), "wood", rot=rot)
    g.box("body", "medallion_l", (6.52, 2.4, 12.8), (6.6, 3.4, 13.8), "brass", rot=rot)
    g.box("body", "medallion_r", (9.4, 2.4, 12.8), (9.48, 3.4, 13.8), "brass", rot=rot)
    pivot = rot_point([8, 2.4, 13.4], "x", GRIP_TILT, g_origin)
    # Cylinder (action): fluted, with visible cartridge bases at the rear face.
    tube(g, "action", "cylinder", 8.7, 1.85, 5.0, 10.4, "gunmetal")
    for i, (dx, dy) in enumerate(((0, 1), (0, -1), (1, 0), (-1, 0))):
        hx, hy = (0.22, 0.04) if dx == 0 else (0.04, 0.22)
        x, y = 8 + dx * 1.84, 8.7 + dy * 1.84
        g.box("action", f"flute_{i}", (x - hx, y - hy, 6.1), (x + hx, y + hy, 9.3), "steel_dark")
    ring(g, "action", "cylinder_band", 8.7, 1.9, 9.7, 0.25, "steel_dark")
    for i, (dx, dy) in enumerate(((0, 1.1), (0.95, 0.55), (0.95, -0.55), (0, -1.1), (-0.95, -0.55), (-0.95, 0.55))):
        g.box("action", f"case_{i}", (8 + dx - 0.4, 8.7 + dy - 0.4, 10.4), (8 + dx + 0.4, 8.7 + dy + 0.4, 10.55), "brass")
    g.box("action", "crane", (7.3, 6.9, 4.6), (8.7, 7.4, 5.2), "steel")
    g.set(pivot=pivot, hip=[0.27, -0.30, -0.58], hip_rotation=[0, -4, 0], ads_distance=0.5,
          sight=[8, 11.7, 11.2], muzzle=[8, 9.6, -6.6], eject=[9.7, 9.5, 8], left_hand=[7.3, 1.4, 13.4],
          mounts={"optic": [8, 11.3, 8.0], "muzzle": [8, 9.6, -6.6]},
          pivots={"action": [8, 7.15, 7.7], "magazine": [8, 8.7, 10.5]},
          sprint=PISTOL_SPRINT, shell="none", flash_size=1.1)
    return g


# ==================================================================================================================
# Viper-9: roller-delayed 9 mm submachine gun
# ==================================================================================================================

def viper_9():
    g = Gun("viper_9", "smg", scale=0.54)
    g.box("body", "receiver", (6.6, 7.8, -1.5), (9.4, 10.6, 11.6), "anodized", decal="stamp")
    g.box("body", "receiver_top", (6.9, 10.6, -1.5), (9.1, 11.0, 11.6), "anodized")
    rail_top = picatinny(g, "body", "top_rail", 11.0, 1.0, 10.4)
    tube(g, "body", "cocking_tube", 10.1, 0.9, -7.6, -1.5, "anodized")
    g.box("action", "cocking_handle", (5.4, 9.8, -6.4), (6.6, 10.4, -5.6), "steel")
    g.box("action", "cocking_knob", (5.1, 9.6, -6.6), (5.5, 10.6, -5.4), "polymer")
    g.box("body", "cocking_slot", (6.55, 9.8, -6.6), (6.6, 10.4, -2.2), "recess")
    g.box("body", "handguard", (6.7, 7.2, -7.4), (9.3, 9.6, -1.5), "polymer")
    g.box("body", "hg_grip_l", (6.6, 7.4, -6.8), (6.7, 9.2, -2.0), "ribs")
    g.box("body", "hg_grip_r", (9.3, 7.4, -6.8), (9.4, 9.2, -2.0), "ribs")
    tube(g, "body", "barrel", 9.0, 0.55, -9.6, -7.4, "steel")
    g.box("body", "hood_base", (7.0, 10.6, -7.9), (9.0, 11.2, -6.7), "anodized")
    g.box("body", "hood_post_l", (7.0, 11.2, -7.8), (7.4, 13.6, -6.8), "anodized")
    g.box("body", "hood_post_r", (8.6, 11.2, -7.8), (9.0, 13.6, -6.8), "anodized")
    g.box("body", "hood_top", (7.0, 13.6, -7.8), (9.0, 13.9, -6.8), "anodized")
    g.box("body", "front_post", (7.85, 11.2, -7.5), (8.15, 12.9, -7.1), "steel_light")
    tube(g, "muzzle", "tri_lug", 9.0, 0.75, -10.4, -9.4, "steel_dark")
    crown(g, "muzzle", "crown", 9.0, 0.6, -10.4)
    g.box("body", "port", (9.36, 8.6, 3.0), (9.45, 10.0, 6.6), "recess")
    g.box("action", "bolt_face", (9.38, 8.8, 3.6), (9.46, 9.8, 6.0), "steel")
    g.box("body", "lower", (6.75, 6.0, 0.4), (9.25, 7.8, 11.2), "polymer")
    g.box("body", "magwell", (6.8, 4.6, 0.8), (9.2, 6.0, 4.2), "polymer")
    trigger_guard(g, "body", 4.8, 8.6, 3.6, 6.0, "polymer", width=1.5, thick=0.55)
    trigger(g, "body", 5.8, 6.0, 4.2, "steel")
    selector(g, "body", 6.75, 7.0, 8.6)
    selector(g, "body", 9.25, 7.0, 8.6)
    g.box("body", "paddle_release", (7.4, 4.0, 4.2), (8.6, 4.6, 4.8), "polymer")
    pivot = pistol_grip(g, "body", 7.9, 6.0, length=2.8, depth=7.0, width=2.3, mat="grip")
    stick_mag(g, "magazine", 4.8, 1.0, 3.9, straight=4.6, curved=4.4, mat="steel_dark", base_mat="polymer", width=1.7)
    g.box("stock", "stock_block", (6.8, 7.8, 11.6), (9.2, 10.4, 13.0), "anodized")
    g.box("stock", "stock_rail_l", (6.75, 8.6, 13.0), (7.35, 9.4, 21.4), "steel_dark")
    g.box("stock", "stock_rail_r", (8.65, 8.6, 13.0), (9.25, 9.4, 21.4), "steel_dark")
    g.box("stock", "stock_latch", (7.4, 9.4, 12.4), (8.6, 10.0, 13.6), "polymer")
    butt_pad(g, "stock", 21.4, 5.4, 10.6, width=3.0)
    g.box("stock", "butt_frame", (6.6, 5.6, 21.0), (9.4, 10.4, 21.4), "steel_dark")
    sling_loop(g, "body", "sling_front", 9.4, 8.8, -1.0)
    rear_center = 12.9
    aperture_rear_sight(g, "sights", rail_top, 10.1, rear_center)
    long_gun(g, pivot, sight=[8, rear_center, 10.1], muzzle=[8, 9.0, -10.5], left_hand=[8, 7.0, -4.2],
             mounts={"optic": [8, rail_top, 6.0], "muzzle": [8, 9.0, -9.4], "underbarrel": [8, 7.2, -4.6],
                     "stock": [8, 9.0, 11.6], "magazine": [8, 4.8, 2.4]},
             pivots={"action": [8, 10.1, -6.0], "magazine": [8, 4.8, 2.4]}, hip=[0.29, -0.33, -0.56], ads=0.27,
             flash=0.8, eject=[9.6, 9.3, 4.8])
    return g


# ==================================================================================================================
# Spectre PDW: bullpup personal defence weapon
# ==================================================================================================================

def spectre_pdw():
    g = Gun("spectre_pdw", "bullpup", scale=0.54)
    g.box("body", "shell", (6.3, 6.4, -4.6), (9.7, 10.9, 15.2), "olive", decal="stamp")
    g.box("body", "shell_top", (6.6, 10.9, -4.6), (9.4, 11.2, 15.2), "olive")
    g.box("body", "nose", (6.6, 7.2, -8.6), (9.4, 10.6, -4.6), "polymer")
    g.box("body", "nose_bevel", (6.6, 9.8, -8.9), (9.4, 10.6, -7.9), "polymer", rot=("x", -22.5, (8, 10.6, -8.6)))
    tube(g, "body", "barrel", 8.8, 0.55, -10.6, -8.6, "steel")
    tube(g, "muzzle", "comp", 8.8, 0.75, -12.0, -10.4, "steel_dark")
    g.box("muzzle", "comp_port", (7.7, 9.5, -11.6), (8.3, 9.56, -10.8), "recess")
    crown(g, "muzzle", "crown", 8.8, 0.6, -12.0)
    rail_top = picatinny(g, "body", "top_rail", 11.2, -4.0, 9.4)
    g.box("body", "vfg", (7.0, 2.2, -7.6), (9.0, 7.2, -5.6), "grip", rot=("x", 22.5, (8, 7.2, -6.6)))
    g.box("body", "vfg_stop", (6.8, 6.6, -8.4), (9.2, 7.3, -7.4), "polymer")
    g.box("body", "accent_stripe", (9.7, 10.1, -3.0), (9.78, 10.45, 14.4), "accent")
    g.box("body", "port", (9.66, 8.4, 9.0), (9.74, 9.8, 12.2), "recess")
    g.box("action", "bolt_face", (9.68, 8.6, 9.4), (9.76, 9.6, 11.6), "steel")
    g.box("action", "ch_l", (5.8, 9.6, 1.6), (6.3, 10.4, 3.2), "steel")
    g.box("action", "ch_r", (9.7, 9.6, 1.6), (10.2, 10.4, 3.2), "steel")
    trigger_guard(g, "body", -2.8, 1.0, 3.6, 6.4, "polymer", width=1.5, thick=0.55)
    trigger(g, "body", -1.6, 6.4, 4.4, "steel")
    pivot = pistol_grip(g, "body", 1.0, 6.4, length=2.8, depth=7.0, width=2.3, mat="grip")
    g.box("body", "magwell", (6.6, 5.6, 5.4), (9.4, 6.4, 10.6), "olive")
    g.box("body", "butt", (6.4, 3.6, 12.4), (9.6, 6.4, 15.2), "olive")
    g.box("body", "butt_cheek", (6.5, 10.0, 12.0), (9.5, 11.2, 15.2), "polymer")
    butt_pad(g, "body", 15.2, 3.4, 11.2, width=3.4)
    sling_loop(g, "body", "sling_rear", 9.7, 5.2, 13.6)
    g.box("magazine", "mag", (6.95, 0.8, 6.2), (9.05, 5.6, 10.0), "polymer_grey")
    g.box("magazine", "mag_window", (9.05, 2.0, 6.8), (9.1, 4.8, 7.6), "glass")
    g.box("magazine", "mag_base", (6.75, 0.1, 6.0), (9.25, 0.8, 10.3), "polymer")
    rear_center = rail_top + 1.3
    aperture_rear_sight(g, "sights", rail_top, 8.4, rear_center)
    folding_front_sight(g, "sights", rail_top, -3.4)
    long_gun(g, pivot, sight=[8, rear_center, 8.4], muzzle=[8, 8.8, -12.1], left_hand=[8, 3.2, -6.6],
             mounts={"optic": [8, rail_top, 3.0], "muzzle": [8, 8.8, -10.4], "underbarrel": [8, 7.2, -6.6],
                     "stock": [8, 6.4, 16.0], "magazine": [8, 5.6, 8.1]},
             pivots={"action": [8, 10, 2.4], "magazine": [8, 5.6, 8.1]}, hip=[0.28, -0.33, -0.55], ads=0.27, flash=0.8,
             eject=[9.9, 9.1, 10.6])
    return g


# ==================================================================================================================
# Raven AK: stamped-receiver 7.62 rifle with wood furniture
# ==================================================================================================================

def raven_ak():
    g = Gun("raven_ak", "rifle", scale=0.5)
    # Stamped receiver with trunnion rivets and a ribbed dust cover.
    g.box("body", "receiver", (6.65, 6.9, 1.6), (9.35, 10.0, 14.2), "steel_dark", decal="stamp")
    g.box("body", "dust_cover", (6.8, 10.0, 3.0), (9.2, 10.7, 14.6), "steel_dark")
    g.box("body", "dust_cover_crown", (7.3, 10.7, 3.0), (8.7, 10.95, 14.6), "steel_dark")
    for i, z in enumerate((6.0, 9.0, 12.0)):
        g.box("body", f"dust_rib_{i}", (6.95, 10.65, z), (9.05, 10.9, z + 0.6), "steel")
    for side, x0, x1 in (("l", 6.58, 6.65), ("r", 9.35, 9.42)):
        for i, (y, z) in enumerate(((7.6, 2.4), (9.3, 2.4), (7.6, 3.6), (7.6, 12.8), (9.3, 13.6))):
            g.box("body", f"rivet_{side}_{i}", (x0, y - 0.22, z - 0.22), (x1, y + 0.22, z + 0.22), "steel_light")
    g.box("body", "mag_dimple_l", (6.58, 7.4, 4.6), (6.65, 8.6, 6.4), "steel")
    g.box("body", "port", (9.35, 8.0, 5.6), (9.42, 8.9, 9.6), "recess")
    # Right-side safety lever and the reciprocating bolt carrier with its charging handle.
    g.box("body", "safety_lever", (9.42, 9.05, 5.8), (9.6, 9.6, 12.4), "steel")
    g.box("body", "safety_paddle", (9.42, 8.6, 5.2), (9.75, 9.7, 5.9), "steel")
    g.box("action", "carrier", (9.36, 8.1, 6.4), (9.44, 8.8, 9.2), "steel")
    g.box("action", "charging_handle", (9.4, 8.2, 6.4), (10.6, 8.75, 7.0), "steel_light")
    # Front trunnion, gas tube and two-piece wood handguard.
    g.box("body", "front_trunnion", (6.75, 7.3, 0.8), (9.25, 10.6, 1.8), "steel_dark")
    g.box("body", "rear_sight_base", (7.0, 10.6, -0.2), (9.0, 11.2, 2.8), "steel_dark")
    g.box("body", "gas_tube", (7.25, 10.2, -8.6), (8.75, 11.0, -0.2), "steel_dark")
    g.box("body", "hg_upper", (7.0, 10.0, -6.4), (9.0, 11.3, -0.4), "wood")
    for i, z in enumerate((-5.4, -4.0, -2.6)):
        g.box("body", f"hg_upper_vent_{i}", (8.98, 10.35, z), (9.04, 10.9, z + 0.7), "recess")
        g.box("body", f"hg_upper_vent_l_{i}", (6.96, 10.35, z), (7.02, 10.9, z + 0.7), "recess")
    g.box("body", "hg_lower", (6.55, 6.6, -7.2), (9.45, 9.9, 0.6), "wood")
    g.box("body", "hg_lower_swell_l", (6.4, 7.0, -6.6), (6.55, 9.4, -0.4), "wood")
    g.box("body", "hg_lower_swell_r", (9.45, 7.0, -6.6), (9.6, 9.4, -0.4), "wood")
    g.box("body", "hg_ferrule", (6.5, 6.5, -7.6), (9.5, 10.0, -7.2), "steel_dark")
    g.box("body", "hg_retainer", (6.5, 6.5, 0.6), (9.5, 10.1, 1.1), "steel_dark")
    g.box("body", "retainer_lever", (9.5, 7.6, 0.6), (9.7, 9.2, 1.0), "steel")
    # Barrel, gas block with cleaning rod, front sight block.
    tube(g, "body", "barrel", 8.7, 0.58, -13.8, -7.2, "steel_dark")
    g.box("body", "gas_block", (7.1, 7.9, -9.6), (8.9, 11.0, -8.2), "steel_dark")
    g.box("body", "cleaning_rod", (7.7, 7.2, -12.4), (8.3, 7.6, -7.2), "steel_light")
    g.box("body", "fsb_base", (7.15, 7.8, -12.8), (8.85, 9.9, -11.2), "steel_dark")
    g.box("body", "fsb_ear_l", (7.0, 9.9, -12.6), (7.45, 12.95, -11.4), "steel_dark")
    g.box("body", "fsb_ear_r", (8.55, 9.9, -12.6), (9.0, 12.95, -11.4), "steel_dark")
    g.box("body", "fsb_post", (7.85, 9.9, -12.2), (8.15, 12.55, -11.8), "steel_light")
    g.box("body", "bayonet_lug", (7.6, 7.2, -12.6), (8.4, 7.8, -11.4), "steel_dark")
    # Slant muzzle brake (muzzle part).
    tube(g, "muzzle", "brake", 8.7, 0.78, -15.6, -13.6, "steel_dark")
    g.box("muzzle", "brake_slant", (7.25, 9.15, -15.75), (8.75, 9.75, -14.9), "steel_dark", rot=("x", 22.5, (8, 9.45, -15.3)))
    g.box("muzzle", "brake_port", (7.75, 9.45, -14.6), (8.25, 9.52, -13.9), "recess")
    crown(g, "muzzle", "crown", 8.7, 0.6, -15.6)
    # Rear tangent sight.
    g.box("sights", "leaf", (7.25, 11.2, 0.0), (8.75, 12.1, 2.6), "steel_dark")
    g.box("sights", "notch_l", (7.25, 12.1, 0.2), (7.8, 12.8, 0.65), "steel_dark")
    g.box("sights", "notch_r", (8.2, 12.1, 0.2), (8.75, 12.8, 0.65), "steel_dark")
    g.box("sights", "slider", (7.15, 11.75, 1.4), (8.85, 12.25, 2.0), "steel")
    # Fire controls, bakelite grip and the iconic curved magazine.
    trigger_guard(g, "body", 7.4, 11.0, 4.9, 6.9, "steel_dark", width=1.4, thick=0.45)
    trigger(g, "body", 8.6, 6.9, 5.3, "steel")
    g.box("body", "mag_release", (7.45, 5.9, 6.8), (8.55, 6.9, 7.3), "steel")
    pivot = pistol_grip(g, "body", 10.8, 6.9, length=2.6, depth=6.8, width=2.2, mat="bakelite", finger_grooves=False,
                        cap_mat="bakelite")
    stick_mag(g, "magazine", 6.9, 3.0, 6.6, straight=3.2, curved=5.8, width=1.9, mat="bakelite", base_mat="bakelite")
    g.box("magazine", "mag_rib", (6.9, 4.0, 4.1), (9.1, 6.4, 5.2), "bakelite")
    g.box("magazine", "mag_lug", (7.4, 6.4, 2.8), (8.6, 6.9, 3.2), "steel_dark")
    # Wood stock with a sloping belly and a steel butt plate.
    g.box("stock", "wrist", (6.85, 6.7, 14.2), (9.15, 9.9, 17.8), "wood")
    g.box("stock", "belly", (6.85, 5.3, 14.6), (9.15, 7.3, 19.8), "wood", rot=("x", 22.5, (8, 7.3, 14.6)))
    g.box("stock", "butt", (6.8, 4.4, 17.8), (9.2, 9.8, 25.4), "wood")
    g.box("stock", "tang", (7.4, 9.9, 13.6), (8.6, 10.2, 16.4), "steel_dark")
    g.box("stock", "butt_plate", (6.7, 4.3, 25.4), (9.3, 9.9, 26.0), "steel_dark")
    g.box("stock", "trap_door", (7.4, 6.0, 25.98), (8.6, 8.4, 26.06), "steel")
    sling_loop(g, "stock", "sling_rear", 9.2, 5.4, 22.6)
    sling_loop(g, "body", "sling_front", 9.5, 9.1, -7.4)
    long_gun(g, pivot, sight=[8, 12.55, 0.4], muzzle=[8, 8.7, -15.8], left_hand=[8, 6.4, -3.6],
             mounts={"optic": [8, 10.95, 9.0], "muzzle": [8, 8.7, -13.6], "underbarrel": [8, 6.6, -4.0],
                     "stock": [8, 8.6, 14.2], "magazine": [8, 6.9, 4.8]},
             pivots={"action": [8, 8.5, 7.8], "magazine": [8, 6.9, 4.8]}, ads=0.34, eject=[9.7, 8.6, 7.6])
    return g


# ==================================================================================================================
# Mantis BR: tan bullpup battle rifle
# ==================================================================================================================

def mantis_br():
    g = Gun("mantis_br", "bullpup", scale=0.5)
    g.box("body", "chassis", (6.3, 6.2, -6.2), (9.7, 10.4, 16.8), "tan", decal="stamp")
    g.box("body", "upper", (6.6, 10.4, -9.6), (9.4, 11.2, 16.4), "anodized")
    rail_top = picatinny(g, "body", "top_rail", 11.2, -9.2, 15.8)
    g.box("body", "cheek_l", (6.15, 8.6, 11.6), (6.3, 10.2, 16.4), "polymer")
    g.box("body", "cheek_r", (9.7, 8.6, 11.6), (9.85, 10.2, 16.4), "polymer")
    g.box("body", "seam_l", (6.24, 7.9, -5.8), (6.3, 8.2, 16.4), "anodized")
    g.box("body", "seam_r", (9.7, 7.9, -5.8), (9.76, 8.2, 16.4), "anodized")
    # Fore-end: forward shroud with heat vents and side M-LOK.
    g.box("body", "shroud", (6.5, 6.8, -12.4), (9.5, 10.4, -6.2), "tan")
    g.box("body", "shroud_nose", (6.7, 7.2, -13.0), (9.3, 10.2, -12.4), "anodized")
    g.box("body", "shroud_side_l", (6.35, 7.4, -11.8), (6.5, 9.8, -6.8), "mlok")
    g.box("body", "shroud_side_r", (9.5, 7.4, -11.8), (9.65, 9.8, -6.8), "mlok")
    g.box("body", "shroud_lip", (6.5, 6.2, -7.2), (9.5, 6.8, -6.2), "tan")
    bottom_rail(g, "body", "hg_bottom_rail", 6.8, -11.6, -8.2)
    tube(g, "body", "barrel", 8.6, 0.6, -14.6, -13.0, "steel_dark")
    tube(g, "muzzle", "brake", 8.6, 0.85, -15.7, -14.4, "steel_dark")
    g.box("muzzle", "brake_port_l", (7.1, 8.25, -15.4), (7.16, 8.95, -14.7), "recess")
    g.box("muzzle", "brake_port_r", (8.84, 8.25, -15.4), (8.9, 8.95, -14.7), "recess")
    crown(g, "muzzle", "crown", 8.6, 0.7, -15.7)
    # Ambidextrous charging handle (action) and forward ejection chute.
    g.box("action", "ch_l", (5.7, 9.5, -4.6), (6.3, 10.2, -3.4), "steel")
    g.box("action", "ch_r", (9.7, 9.5, -4.6), (10.3, 10.2, -3.4), "steel")
    g.box("body", "ch_slot_l", (6.25, 9.7, -5.4), (6.3, 10.0, 2.4), "recess")
    g.box("body", "ch_slot_r", (9.7, 9.7, -5.4), (9.75, 10.0, 2.4), "recess")
    g.box("body", "eject_chute", (9.7, 7.0, -4.2), (9.95, 8.0, 0.6), "anodized")
    g.box("body", "eject_mouth", (9.9, 7.2, -4.4), (9.96, 7.8, -3.4), "recess")
    # Grip ahead of the magazine, trigger linkage housing.
    trigger_guard(g, "body", -3.2, 1.0, 3.2, 6.2, "tan", width=1.5, thick=0.5)
    trigger(g, "body", -2.0, 6.2, 4.2, "steel")
    pivot = pistol_grip(g, "body", 1.0, 6.2, length=2.9, depth=7.0, width=2.3, mat="tan_grip", cap_mat="tan")
    selector(g, "body", 6.3, 7.2, 2.8)
    selector(g, "body", 9.7, 7.2, 2.8)
    g.box("body", "magwell", (6.6, 4.8, 6.0), (9.4, 6.2, 10.8), "tan")
    g.box("body", "mag_release", (7.3, 4.4, 10.8), (8.7, 5.4, 11.4), "anodized")
    stick_mag(g, "magazine", 4.8, 6.6, 10.0, straight=4.8, curved=0.0, width=2.0, mat="ribs", base_mat="polymer")
    g.box("body", "butt_stock", (6.4, 3.4, 12.2), (9.6, 6.2, 16.8), "tan")
    g.box("body", "butt_cell", (6.3, 3.8, 13.0), (6.4, 5.8, 16.0), "anodized")
    butt_pad(g, "body", 16.8, 3.4, 11.2, width=3.4)
    sling_loop(g, "body", "sling_rear", 9.7, 4.8, 15.0)
    sling_loop(g, "body", "sling_front", 9.5, 8.0, -11.2)
    rear_center = rail_top + 1.3
    aperture_rear_sight(g, "sights", rail_top, 13.6, rear_center)
    folding_front_sight(g, "sights", rail_top, -8.6)
    long_gun(g, pivot, sight=[8, rear_center, 13.6], muzzle=[8, 8.6, -16.1], left_hand=[8, 6.4, -9.6],
             mounts={"optic": [8, rail_top, 6.0], "muzzle": [8, 8.6, -14.4], "underbarrel": [8, 6.8, -9.6],
                     "stock": [8, 6.2, 17.6], "magazine": [8, 4.8, 8.3]},
             pivots={"action": [8, 9.8, -4.0], "magazine": [8, 4.8, 8.3]}, hip=[0.29, -0.34, -0.55], ads=0.26,
             eject=[10.0, 7.5, -3.9])
    return g


# ==================================================================================================================
# Breacher-12: magazine-fed semi-automatic combat shotgun
# ==================================================================================================================

def breacher_12():
    g = Gun("breacher_12", "shotgun", scale=0.5)
    g.box("body", "receiver", (6.5, 6.8, 2.0), (9.5, 10.4, 14.2), "anodized", decal="stamp")
    g.box("body", "receiver_cover", (6.7, 10.4, 3.0), (9.3, 10.9, 14.4), "anodized")
    rail_top = picatinny(g, "body", "top_rail", 10.9, 3.4, 14.0)
    g.box("body", "port", (9.45, 8.0, 5.4), (9.55, 9.6, 10.0), "recess")
    g.box("action", "carrier", (9.47, 8.3, 6.0), (9.56, 9.3, 9.4), "steel")
    g.box("action", "charging_handle", (9.5, 8.45, 6.0), (10.7, 9.15, 6.7), "steel")
    g.box("action", "ch_knob", (10.5, 8.25, 5.9), (10.9, 9.35, 6.8), "polymer")
    g.box("body", "safety_lever", (9.5, 9.6, 5.4), (9.68, 10.1, 12.4), "steel_dark")
    # Quad-rail handguard.
    g.box("body", "hg_core", (6.6, 6.8, -7.8), (9.4, 10.6, 2.0), "anodized")
    picatinny(g, "body", "hg_rail", 10.6, -7.6, 1.6)
    bottom_rail(g, "body", "hg_bottom_rail", 6.8, -7.4, -1.0)
    for side, x0, x1 in (("l", 6.2, 6.6), ("r", 9.4, 9.8)):
        g.box("body", f"hg_side_{side}", (x0, 7.6, -7.4), (x1, 9.8, 1.6), "rail")
        for i in range(9):
            z = -7.2 + i * 1.0
            g.box("body", f"hg_side_{side}_slot_{i}", (x0 - 0.01 if side == "l" else x1 - 0.06, 8.2, z), (x0 + 0.06 if side == "l" else x1 + 0.01, 9.2, z + 0.5), "recess")
    # Heavy barrel, gas block and cylindrical ported brake.
    tube(g, "body", "barrel", 8.7, 0.8, -12.6, -7.8, "steel_dark")
    g.box("body", "gas_block", (7.0, 7.6, -9.6), (9.0, 10.6, -8.0), "steel_dark")
    g.box("body", "front_sight", (7.7, 10.6, -9.4), (8.3, 11.6, -8.6), "steel_dark")
    g.box("body", "front_bead", (7.85, 11.6, -9.2), (8.15, 11.9, -8.9), "dot")
    tube(g, "muzzle", "brake", 8.7, 1.15, -15.6, -12.4, "steel_dark")
    for i, z in enumerate((-15.2, -14.2, -13.2)):
        g.box("muzzle", f"brake_port_l_{i}", (6.88, 8.1, z), (6.94, 9.3, z + 0.6), "recess")
        g.box("muzzle", f"brake_port_r_{i}", (9.06, 8.1, z), (9.12, 9.3, z + 0.6), "recess")
        g.box("muzzle", f"brake_port_t_{i}", (7.4, 9.86, z), (8.6, 9.92, z + 0.6), "recess")
    crown(g, "muzzle", "crown", 8.7, 0.95, -15.6)
    # Big box magazine with a shell window showing red hulls.
    trigger_guard(g, "body", 7.6, 11.2, 4.6, 6.8, "polymer", width=1.5, thick=0.5)
    trigger(g, "body", 8.8, 6.8, 5.0, "steel")
    g.box("body", "mag_release", (7.4, 5.8, 6.4), (8.6, 6.8, 7.0), "steel")
    pivot = pistol_grip(g, "body", 11.0, 6.8, length=2.8, depth=7.0, width=2.3, mat="grip")
    g.box("magazine", "mag_body", (6.6, 0.6, 1.8), (9.4, 6.8, 6.4), "polymer")
    g.box("magazine", "mag_window", (9.4, 1.4, 2.4), (9.46, 6.0, 5.8), "recess")
    for i in range(4):
        y = 1.6 + i * 1.1
        g.box("magazine", f"mag_shell_{i}", (9.38, y, 2.6), (9.48, y + 0.9, 5.0), "shell_red")
        g.box("magazine", f"mag_head_{i}", (9.38, y, 5.0), (9.48, y + 0.9, 5.6), "brass")
    g.box("magazine", "mag_rib_l", (6.45, 1.0, 2.6), (6.6, 6.2, 3.2), "polymer")
    g.box("magazine", "mag_base", (6.4, -0.1, 1.6), (9.6, 0.6, 6.6), "polymer_grey")
    # Side-folding skeleton stock on a hinge block.
    g.box("stock", "hinge", (6.8, 7.4, 14.2), (9.2, 10.2, 15.4), "steel_dark")
    g.box("stock", "hinge_pin", (9.2, 7.8, 14.4), (9.4, 9.8, 15.2), "steel")
    g.box("stock", "strut_top", (7.0, 9.2, 15.4), (9.0, 10.2, 24.4), "anodized")
    g.box("stock", "strut_bottom", (7.0, 5.4, 18.4), (9.0, 6.4, 24.4), "anodized")
    g.box("stock", "strut_diag", (7.0, 6.2, 15.4), (9.0, 7.2, 20.4), "anodized", rot=("x", 22.5, (8, 7.2, 15.4)))
    g.box("stock", "cheek_pad", (7.1, 10.2, 18.4), (8.9, 10.8, 23.4), "rubber")
    butt_pad(g, "stock", 24.4, 4.8, 10.8, width=2.8)
    sling_loop(g, "stock", "sling_rear", 9.0, 9.7, 22.0)
    rear_center = rail_top + 1.3
    aperture_rear_sight(g, "sights", rail_top, 13.2, rear_center)
    long_gun(g, pivot, sight=[8, rear_center, 13.2], muzzle=[8, 8.7, -16.0], left_hand=[8, 6.4, -4.0],
             mounts={"optic": [8, rail_top, 8.8], "muzzle": [8, 8.7, -12.4], "underbarrel": [8, 6.8, -4.6],
                     "stock": [8, 8.8, 14.2], "magazine": [8, 6.8, 4.1]},
             pivots={"action": [8, 8.8, 6.4], "magazine": [8, 6.8, 4.1]}, shell="shotshell", flash=1.3,
             eject=[9.7, 8.8, 7.6])
    return g


# ==================================================================================================================
# Thunderpump: pump-action 12 gauge with walnut furniture
# ==================================================================================================================

def thunderpump():
    g = Gun("thunderpump", "pump", scale=0.5)
    g.box("body", "receiver", (6.6, 6.6, 1.0), (9.4, 10.4, 12.6), "gunmetal", decal="crest")
    g.box("body", "receiver_top", (6.9, 10.4, 1.0), (9.1, 10.75, 12.6), "gunmetal")
    g.box("body", "port", (9.36, 7.6, 3.4), (9.45, 9.8, 8.6), "recess")
    g.box("body", "loading_port", (7.2, 6.55, 3.0), (8.8, 6.62, 7.6), "recess")
    g.box("body", "shell_lifter", (7.3, 6.5, 3.4), (8.7, 6.58, 7.2), "steel")
    g.box("body", "action_release", (6.95, 6.2, 9.6), (7.35, 6.6, 10.6), "steel")
    g.box("body", "safety_button", (7.5, 6.6, 12.0), (8.5, 7.0, 12.6), "steel")
    g.box("action", "bolt_face", (9.38, 7.9, 4.2), (9.46, 9.4, 7.6), "steel")
    # Side saddle with four shells on the left.
    g.box("body", "saddle", (6.25, 7.0, 2.2), (6.6, 10.0, 9.6), "polymer")
    for i in range(4):
        z = 2.5 + i * 1.8
        g.box("body", f"saddle_shell_{i}", (5.75, 7.2, z), (6.25, 9.0, z + 1.2), "shell_red")
        g.box("body", f"saddle_head_{i}", (5.75, 9.0, z), (6.25, 9.8, z + 1.2), "brass")
    # Long barrel with ventilated rib and bead; magazine tube underneath.
    tube(g, "body", "barrel", 9.2, 0.72, -15.3, 1.0, "gunmetal")
    g.box("body", "rib", (7.6, 9.9, -15.2), (8.4, 10.3, 1.0), "gunmetal")
    for i in range(7):
        z = -14.6 + i * 2.2
        g.box("body", f"rib_post_{i}", (7.75, 9.92, z), (8.25, 10.0, z + 1.1), "recess")
    g.box("body", "fs_base", (7.4, 10.3, -14.9), (8.6, 10.7, -13.7), "gunmetal")
    g.box("body", "fs_ear_l", (7.4, 10.7, -14.7), (7.75, 12.3, -13.9), "gunmetal")
    g.box("body", "fs_ear_r", (8.25, 10.7, -14.7), (8.6, 12.3, -13.9), "gunmetal")
    g.box("body", "fs_post", (7.88, 10.7, -14.45), (8.12, 12.0, -14.15), "steel_light")
    g.box("body", "fs_dot", (7.86, 11.6, -14.48), (8.14, 11.9, -14.45), "accent")
    tube(g, "body", "mag_tube", 7.4, 0.62, -12.2, 1.0, "gunmetal")
    g.box("body", "mag_cap", (7.3, 6.7, -13.2), (8.7, 8.1, -12.2), "steel_dark")
    g.box("body", "barrel_ring", (7.2, 6.6, -12.0), (8.8, 10.0, -11.4), "gunmetal")
    sling_loop(g, "body", "sling_front", 8.7, 7.4, -12.8)
    crown(g, "muzzle", "crown", 9.2, 0.72, -15.3)
    # Pump (action): walnut fore-end with deep grooves on action bars.
    g.box("action", "pump", (6.5, 5.9, -8.4), (9.5, 8.8, -1.8), "wood")
    for i in range(6):
        z = -7.8 + i * 1.0
        g.box("action", f"pump_groove_l_{i}", (6.42, 6.3, z), (6.5, 8.5, z + 0.45), "wood_dark")
        g.box("action", f"pump_groove_r_{i}", (9.5, 6.3, z), (9.58, 8.5, z + 0.45), "wood_dark")
    g.box("action", "action_bar_l", (6.95, 7.4, -1.8), (7.15, 7.9, 3.8), "steel")
    g.box("action", "action_bar_r", (8.85, 7.4, -1.8), (9.05, 7.9, 3.8), "steel")
    # Trigger group and classic walnut stock with a pistol-grip wrist.
    trigger_guard(g, "body", 8.2, 12.0, 4.4, 6.6, "gunmetal", width=1.4, thick=0.45)
    trigger(g, "body", 9.6, 6.6, 4.8, "brass")
    g.box("stock", "wrist", (6.9, 6.0, 12.6), (9.1, 9.7, 16.4), "wood")
    g.box("stock", "wrist_grip", (7.0, 3.4, 13.4), (9.0, 6.4, 16.0), "wood", rot=("x", GRIP_TILT, (8, 6.4, 14.7)))
    g.box("stock", "grip_cap", (6.95, 2.95, 13.3), (9.05, 3.4, 16.1), "steel_dark", rot=("x", GRIP_TILT, (8, 6.4, 14.7)))
    for i in range(3):
        y = 4.0 + i * 0.7
        g.box("stock", f"checker_l_{i}", (6.92, y, 13.8), (7.0, y + 0.35, 15.6), "wood_dark", rot=("x", GRIP_TILT, (8, 6.4, 14.7)))
        g.box("stock", f"checker_r_{i}", (9.0, y, 13.8), (9.08, y + 0.35, 15.6), "wood_dark", rot=("x", GRIP_TILT, (8, 6.4, 14.7)))
    g.box("stock", "belly", (6.85, 5.0, 16.0), (9.15, 7.2, 21.0), "wood", rot=("x", 22.5, (8, 7.2, 16.0)))
    g.box("stock", "butt", (6.8, 4.0, 19.0), (9.2, 9.4, 25.8), "wood")
    g.box("stock", "comb", (6.9, 9.0, 16.4), (9.1, 9.55, 19.0), "wood")
    g.box("stock", "spacer", (6.75, 3.95, 25.8), (9.25, 9.6, 26.0), "white")
    butt_pad(g, "stock", 26.0, 3.95, 9.6, width=2.5)
    sling_loop(g, "stock", "sling_rear", 9.2, 5.0, 23.0)
    pivot = rot_point([8, 4.9, 14.7], "x", GRIP_TILT, (8, 6.4, 14.7))
    aperture_rear_sight(g, "sights", 10.75, 11.4, 12.0)
    long_gun(g, pivot, sight=[8, 12.0, 11.4], muzzle=[8, 9.2, -15.6], left_hand=[8, 5.9, -5.2],
             mounts={"optic": [8, 10.75, 6.8], "muzzle": [8, 9.2, -15.4], "underbarrel": [8, 6.0, -5.2],
                     "stock": [8, 8.0, 12.6]},
             pivots={"action": [8, 7.4, -5.0], "magazine": [8, 7.4, -12.2]}, shell="shotshell", flash=1.35,
             eject=[9.7, 8.6, 6.0])
    return g




# ==================================================================================================================
# Ghostline M24: bolt-action precision rifle in a tan composite stock
# ==================================================================================================================

def ghostline_m24():
    g = Gun("ghostline_m24", "bolt", scale=0.46)
    # Round receiver with a full-length scope rail.
    tube(g, "body", "receiver", 9.8, 1.15, 3.6, 14.6, "steel_dark")
    rail_top = picatinny(g, "body", "rail", 10.95, 3.9, 14.3, width=1.8)
    g.box("body", "port", (9.13, 9.55, 7.2), (9.2, 10.65, 11.2), "recess")
    g.box("body", "recoil_lug", (7.2, 8.4, 3.6), (8.8, 8.9, 4.2), "steel_dark")
    # Bolt (action): body visible in the port, angled handle with a tactical knob, shroud and cocking indicator.
    g.box("action", "bolt_body", (9.15, 9.75, 7.6), (9.22, 10.45, 10.8), "steel_light")
    g.box("action", "bolt_root", (9.1, 9.5, 12.4), (10.25, 10.25, 13.1), "steel")
    g.box("action", "bolt_arm", (9.9, 8.1, 12.45), (10.45, 10.0, 13.05), "steel", rot=("z", 22.5, (10.2, 9.9, 12.75)))
    g.box("action", "bolt_knob", (10.4, 7.25, 12.15), (11.5, 8.35, 13.35), "steel_dark")
    g.box("action", "bolt_shroud", (7.25, 9.05, 14.6), (8.75, 10.55, 15.6), "steel")
    g.box("action", "cocking_indicator", (7.8, 9.35, 15.6), (8.2, 9.8, 15.85), "red")
    # Heavy fluted barrel and a compact brake.
    tube(g, "body", "barrel", 9.8, 0.72, -13.8, 3.6, "steel_dark")
    tube(g, "body", "barrel_shank", 9.8, 0.88, 1.0, 3.6, "steel_dark")
    for i, (dx, dy) in enumerate(((1, 0), (-1, 0), (0, 1))):
        hx, hy = (0.03, 0.2) if dx else (0.2, 0.03)
        x, y = 8 + dx * 0.72, 9.8 + dy * 0.72
        g.box("body", f"flute_{i}", (x - hx, y - hy, -11.0), (x + hx, y + hy, -1.4), "anodized")
    tube(g, "muzzle", "brake", 9.8, 0.88, -15.6, -13.6, "steel_dark")
    g.box("muzzle", "brake_port_l", (7.08, 9.45, -15.2), (7.14, 10.15, -14.6), "recess")
    g.box("muzzle", "brake_port_r", (8.86, 9.45, -15.2), (8.92, 10.15, -14.6), "recess")
    g.box("muzzle", "brake_port_l2", (7.08, 9.45, -14.3), (7.14, 10.15, -13.9), "recess")
    g.box("muzzle", "brake_port_r2", (8.86, 9.45, -14.3), (8.92, 10.15, -13.9), "recess")
    crown(g, "muzzle", "crown", 9.8, 0.7, -15.6)
    # Composite stock: fore-end with checkering and a bipod rail, bedding block and integral grip.
    g.box("body", "forend", (6.55, 6.4, -7.6), (9.45, 9.4, 3.6), "tan")
    g.box("body", "forend_tip", (6.75, 6.8, -8.2), (9.25, 9.2, -7.6), "tan")
    g.box("body", "check_l", (6.45, 6.9, -6.0), (6.55, 8.7, 0.6), "tan_grip")
    g.box("body", "check_r", (9.45, 6.9, -6.0), (9.55, 8.7, 0.6), "tan_grip")
    bottom_rail(g, "body", "bipod_rail", 6.4, -7.0, -3.4, width=1.6)
    g.box("body", "action_bed", (6.45, 5.8, 3.6), (9.55, 9.6, 14.8), "tan", decal="stamp")
    g.box("body", "floorplate", (7.1, 5.6, 6.4), (8.9, 5.8, 11.0), "steel_dark")
    screw(g, "body", "action_screw", 8.9, 5.7, 6.9)
    trigger_guard(g, "body", 11.2, 14.4, 3.8, 5.8, "steel_dark", width=1.4, thick=0.45)
    trigger(g, "body", 12.4, 5.8, 4.3, "steel")
    g.box("body", "wrist", (6.7, 6.0, 14.8), (9.3, 9.6, 18.0), "tan")
    pivot = pistol_grip(g, "body", 15.0, 6.2, length=2.8, depth=6.2, width=2.3, mat="tan_grip", finger_grooves=False,
                        cap_mat="tan", tilt=-22.5)
    g.box("body", "safety", (7.6, 9.6, 15.4), (8.4, 9.9, 16.2), "steel")
    sling_loop(g, "body", "sling_front", 9.45, 7.2, -6.6)
    # Butt (stock): adjustable cheek riser, length-of-pull spacers, recoil pad.
    g.box("stock", "butt", (6.6, 4.2, 18.0), (9.4, 9.4, 28.2), "tan")
    g.box("stock", "butt_check_l", (6.5, 5.0, 20.4), (6.6, 8.2, 25.6), "tan_grip")
    g.box("stock", "butt_check_r", (9.4, 5.0, 20.4), (9.5, 8.2, 25.6), "tan_grip")
    g.box("stock", "cheek_riser", (6.9, 9.4, 19.4), (9.1, 10.5, 25.8), "tan")
    g.box("stock", "riser_gap", (6.95, 9.4, 19.6), (9.05, 9.55, 25.6), "recess")
    g.box("stock", "riser_knob", (9.4, 9.0, 22.3), (9.85, 9.7, 23.0), "steel_dark")
    g.box("stock", "spacer", (6.7, 4.2, 28.2), (9.3, 9.4, 28.6), "steel_dark")
    butt_pad(g, "stock", 28.6, 4.2, 9.4, width=2.6)
    sling_loop(g, "stock", "sling_rear", 9.4, 5.2, 26.2)
    # Back-up iron sights on the rail (hidden under the default scope).
    rear_center = rail_top + 1.2
    aperture_rear_sight(g, "sights", rail_top, 13.4, rear_center)
    folding_front_sight(g, "sights", rail_top, 4.8)
    long_gun(g, pivot, sight=[8, rear_center, 13.4], muzzle=[8, 9.8, -15.8], left_hand=[8, 6.4, -3.0],
             mounts={"optic": [8, rail_top, 9.2], "muzzle": [8, 9.8, -13.6], "underbarrel": [8, 6.4, -5.2],
                     "stock": [8, 7.6, 18.0]},
             pivots={"action": [8, 9.8, 12.75], "magazine": [8, 5.8, 8.7]}, hip=[0.30, -0.36, -0.56], ads=0.26,
             eject=[9.4, 10.2, 9.2])
    return g


# ==================================================================================================================
# Longshot .50: anti-materiel rifle with a skeleton stock and monopod
# ==================================================================================================================

def longshot_50():
    g = Gun("longshot_50", "bolt_mag", scale=0.44)
    g.box("body", "receiver", (6.3, 7.4, 2.0), (9.7, 11.2, 15.0), "anodized", decal="stamp")
    g.box("body", "receiver_bevel_l", (6.2, 8.0, 2.4), (6.3, 10.6, 14.6), "polymer_grey")
    g.box("body", "receiver_bevel_r", (9.7, 8.0, 2.4), (9.8, 9.0, 14.6), "polymer_grey")
    rail_top = picatinny(g, "body", "rail", 11.2, -8.0, 14.8, width=2.0)
    g.box("body", "port", (9.66, 9.1, 6.4), (9.74, 10.8, 11.2), "recess")
    # Bolt (action).
    g.box("action", "bolt_body", (9.7, 9.3, 6.8), (9.78, 10.6, 10.8), "steel_light")
    g.box("action", "bolt_root", (9.7, 9.95, 12.7), (11.0, 10.65, 13.5), "steel")
    g.box("action", "bolt_knob", (10.8, 9.55, 12.5), (12.1, 11.05, 13.7), "polymer")
    g.box("action", "bolt_shroud", (7.1, 8.9, 15.0), (8.9, 10.7, 16.1), "steel")
    # Skeletonised handguard with lightening cuts.
    g.box("body", "handguard", (6.5, 7.2, -8.4), (9.5, 11.2, 2.0), "anodized")
    for i, z in enumerate((-7.6, -5.4, -3.2, -1.0)):
        g.box("body", f"hg_cut_l_{i}", (6.44, 8.0, z), (6.5, 10.2, z + 1.6), "recess")
        g.box("body", f"hg_cut_r_{i}", (9.5, 8.0, z), (9.56, 10.2, z + 1.6), "recess")
    bottom_rail(g, "body", "hg_bottom_rail", 7.2, -7.8, -2.6)
    sling_loop(g, "body", "sling_front", 9.5, 9.6, -6.6)
    # Fluted heavy barrel and a two-chamber brake.
    tube(g, "body", "barrel", 9.6, 0.95, -12.9, -8.4, "steel_dark")
    for i, (dx, dy) in enumerate(((1, 0), (-1, 0), (0, 1), (0, -1))):
        hx, hy = (0.03, 0.25) if dx else (0.25, 0.03)
        x, y = 8 + dx * 0.95, 9.6 + dy * 0.95
        g.box("body", f"flute_{i}", (x - hx, y - hy, -12.6), (x + hx, y + hy, -8.6), "anodized")
    g.box("muzzle", "brake", (6.6, 8.2, -15.6), (9.4, 11.0, -12.8), "steel_dark")
    for i, z in enumerate((-15.2, -13.9)):
        g.box("muzzle", f"brake_window_l_{i}", (6.54, 8.7, z), (6.6, 10.5, z + 0.9), "recess")
        g.box("muzzle", f"brake_window_r_{i}", (9.4, 8.7, z), (9.46, 10.5, z + 0.9), "recess")
        g.box("muzzle", f"brake_window_t_{i}", (7.1, 10.96, z), (8.9, 11.02, z + 0.9), "recess")
    g.box("muzzle", "brake_face", (6.8, 8.4, -15.7), (9.2, 10.8, -15.6), "steel")
    g.box("muzzle", "brake_bore", (7.5, 9.1, -15.75), (8.5, 10.1, -15.69), "recess")
    # Magazine, fire controls and grip.
    g.box("body", "magwell", (6.5, 6.2, 4.2), (9.5, 7.4, 9.4), "anodized")
    g.box("magazine", "mag", (6.85, 2.6, 4.6), (9.15, 6.4, 9.0), "steel_dark")
    g.box("magazine", "mag_rib_l", (6.75, 3.2, 5.4), (6.85, 5.8, 8.2), "steel")
    g.box("magazine", "mag_rib_r", (9.15, 3.2, 5.4), (9.25, 5.8, 8.2), "steel")
    g.box("magazine", "mag_base", (6.65, 2.0, 4.4), (9.35, 2.6, 9.3), "polymer")
    g.box("body", "mag_release", (7.3, 6.2, 9.4), (8.7, 6.9, 10.0), "steel")
    trigger_guard(g, "body", 10.2, 13.6, 5.0, 7.4, "anodized", width=1.5, thick=0.5)
    trigger(g, "body", 11.4, 7.4, 5.6, "steel")
    pivot = pistol_grip(g, "body", 13.4, 7.4, length=3.0, depth=7.2, width=2.4, mat="grip")
    selector(g, "body", 6.3, 8.4, 14.0)
    # Skeleton stock with cheek riser and a rear monopod.
    g.box("stock", "hinge", (6.9, 8.4, 15.0), (9.1, 11.0, 16.4), "steel_dark")
    g.box("stock", "strut_top", (7.0, 9.8, 16.4), (9.0, 11.0, 27.6), "anodized")
    g.box("stock", "strut_bottom", (7.0, 5.0, 20.0), (9.0, 6.2, 27.6), "anodized")
    g.box("stock", "strut_diag", (7.0, 7.4, 16.4), (9.0, 8.6, 22.4), "anodized", rot=("x", 22.5, (8, 8.6, 16.4)))
    g.box("stock", "cheek_riser", (7.1, 11.0, 19.2), (8.9, 11.9, 25.8), "polymer_grey")
    g.box("stock", "riser_post", (7.6, 11.0, 20.4), (8.4, 11.2, 21.2), "steel")
    g.box("stock", "butt_plate", (6.8, 4.4, 27.6), (9.2, 11.4, 28.4), "anodized")
    butt_pad(g, "stock", 28.4, 4.4, 11.4, width=2.6)
    g.box("stock", "monopod", (7.6, 2.4, 25.0), (8.4, 5.0, 25.8), "steel")
    g.box("stock", "monopod_knob", (7.3, 3.4, 24.8), (8.7, 4.0, 26.0), "polymer")
    g.box("stock", "monopod_foot", (7.1, 1.9, 24.6), (8.9, 2.4, 26.2), "rubber")
    sling_loop(g, "stock", "sling_rear", 9.0, 5.6, 25.4)
    rear_center = rail_top + 1.2
    aperture_rear_sight(g, "sights", rail_top, 14.0, rear_center)
    folding_front_sight(g, "sights", rail_top, -7.2)
    long_gun(g, pivot, sight=[8, rear_center, 14.0], muzzle=[8, 9.6, -15.8], left_hand=[8, 7.0, -4.8],
             mounts={"optic": [8, rail_top, 7.8], "muzzle": [8, 9.6, -12.8], "underbarrel": [8, 7.2, -5.2],
                     "stock": [8, 9.8, 15.0], "magazine": [8, 6.4, 6.8]},
             pivots={"action": [8, 10.3, 13.1], "magazine": [8, 6.4, 6.8]}, hip=[0.31, -0.37, -0.56], ads=0.26,
             flash=1.6, eject=[9.9, 10.0, 8.8])
    return g


# ==================================================================================================================
# Titan-56: belt-fed squad automatic weapon
# ==================================================================================================================

def titan_56():
    g = Gun("titan_56", "lmg", scale=0.46)
    g.box("body", "receiver", (6.4, 6.4, 0.0), (9.6, 10.4, 13.4), "anodized", decal="stamp")
    for side, x0, x1 in (("l", 6.33, 6.4), ("r", 9.6, 9.67)):
        for i, (y, z) in enumerate(((7.0, 0.8), (9.8, 0.8), (7.0, 12.6), (9.8, 12.6), (7.0, 6.6))):
            g.box("body", f"rivet_{side}_{i}", (x0, y - 0.2, z - 0.2), (x1, y + 0.2, z + 0.2), "steel")
    g.box("body", "port", (9.55, 7.0, 5.2), (9.65, 8.0, 9.8), "recess")
    g.box("body", "charging_handle", (9.6, 8.4, 0.8), (10.8, 9.0, 1.6), "steel")
    g.box("body", "ch_slot", (9.58, 8.55, 0.6), (9.65, 8.85, 8.0), "recess")
    # Feed cover (action) with its own rail; hinged at the rear.
    g.box("action", "feed_cover", (6.6, 10.4, 0.6), (9.4, 11.4, 12.6), "anodized")
    g.box("action", "cover_latch", (7.2, 10.4, 12.6), (8.8, 11.2, 13.3), "steel")
    g.box("action", "cover_rib", (6.9, 11.4, 0.6), (9.1, 11.6, 1.4), "anodized")
    rail_top = picatinny(g, "action", "cover_rail", 11.4, 2.0, 12.2, width=1.8)
    # Belt feed from the left: feed tray, linked rounds, ammunition box (magazine).
    g.box("body", "feed_tray", (5.8, 8.6, 3.0), (6.4, 10.4, 7.0), "steel_dark")
    for i in range(4):
        y = 6.6 + i * 0.75
        g.box("magazine", f"round_{i}", (5.0 + i * 0.12, y, 3.6), (5.8 + i * 0.12, y + 0.6, 5.8), "brass")
        g.box("magazine", f"tip_{i}", (5.1 + i * 0.12, y + 0.1, 3.0), (5.7 + i * 0.12, y + 0.5, 3.6), "copper")
        g.box("magazine", f"link_{i}", (5.0 + i * 0.12, y + 0.05, 5.8), (5.8 + i * 0.12, y + 0.55, 6.4), "steel_dark")
    g.box("magazine", "box", (5.4, 1.6, 2.4), (9.6, 6.4, 8.6), "olive")
    g.box("magazine", "box_lid", (5.3, 6.0, 2.3), (9.7, 6.6, 8.7), "olive")
    g.box("magazine", "box_rib", (5.3, 2.4, 4.8), (9.7, 5.6, 6.2), "olive")
    g.box("magazine", "box_latch", (9.7, 4.6, 5.0), (9.95, 5.8, 6.0), "steel")
    g.box("magazine", "box_stencil", (9.6, 2.8, 3.0), (9.65, 3.6, 8.0), "tan")
    # Barrel group: heat shield, ribbed handguard, carry handle, folded bipod, front sight tower.
    tube(g, "body", "barrel", 9.0, 0.66, -14.4, 0.0, "steel_dark")
    g.box("body", "heat_shield", (7.0, 9.4, -6.0), (9.0, 10.2, 0.0), "vent")
    g.box("body", "handguard", (6.6, 6.8, -6.4), (9.4, 8.8, 0.0), "ribs")
    g.box("body", "gas_tube", (7.4, 7.3, -11.0), (8.6, 8.1, -6.4), "steel_dark")
    g.box("body", "gas_block", (7.0, 7.0, -11.6), (9.0, 9.8, -10.6), "steel_dark")
    g.box("body", "handle_post", (7.6, 10.2, -3.6), (8.4, 11.6, -2.8), "steel_dark")
    g.box("body", "handle_bar", (7.45, 11.6, -5.2), (8.55, 12.3, -1.0), "polymer")
    g.box("body", "bipod_clamp", (7.0, 6.6, -12.6), (9.0, 8.4, -11.6), "steel_dark")
    g.box("body", "bipod_leg_l", (7.1, 6.8, -11.6), (7.7, 7.4, -4.6), "steel")
    g.box("body", "bipod_leg_r", (8.3, 6.8, -11.6), (8.9, 7.4, -4.6), "steel")
    g.box("body", "bipod_foot_l", (7.0, 6.6, -4.8), (7.8, 7.6, -4.2), "rubber")
    g.box("body", "bipod_foot_r", (8.2, 6.6, -4.8), (9.0, 7.6, -4.2), "rubber")
    g.box("body", "fs_tower", (7.5, 9.4, -13.6), (8.5, 11.0, -12.8), "steel_dark")
    g.box("body", "fs_ear_l", (7.15, 11.0, -13.5), (7.5, 13.7, -12.9), "steel_dark")
    g.box("body", "fs_ear_r", (8.5, 11.0, -13.5), (8.85, 13.7, -12.9), "steel_dark")
    rear_center = rail_top + 1.1
    g.box("body", "fs_post", (7.85, 11.0, -13.35), (8.15, rear_center, -13.05), "steel_light")
    tube(g, "muzzle", "flash_hider", 9.0, 0.82, -15.7, -14.3, "steel_dark")
    g.box("muzzle", "fh_slot_l", (7.15, 8.7, -15.5), (7.21, 9.3, -14.6), "recess")
    g.box("muzzle", "fh_slot_r", (8.79, 8.7, -15.5), (8.85, 9.3, -14.6), "recess")
    crown(g, "muzzle", "crown", 9.0, 0.66, -15.7)
    # Fire controls.
    trigger_guard(g, "body", 8.4, 12.2, 3.6, 6.4, "anodized", width=1.6, thick=0.55)
    trigger(g, "body", 9.6, 6.4, 4.4, "steel")
    pivot = pistol_grip(g, "body", 11.6, 6.4, length=3.0, depth=7.2, width=2.4, mat="grip")
    # Polymer stock with a shoulder rest.
    g.box("stock", "stock_neck", (6.8, 6.6, 13.4), (9.2, 10.0, 16.4), "polymer")
    g.box("stock", "stock_body", (6.7, 4.6, 16.4), (9.3, 10.0, 25.2), "polymer")
    g.box("stock", "stock_window_l", (6.64, 5.6, 17.6), (6.7, 7.8, 22.0), "recess")
    g.box("stock", "stock_window_r", (9.3, 5.6, 17.6), (9.36, 7.8, 22.0), "recess")
    g.box("stock", "shoulder_rest", (7.2, 10.0, 23.4), (8.8, 10.4, 25.2), "steel_dark")
    butt_pad(g, "stock", 25.2, 4.6, 10.4, width=2.6)
    sling_loop(g, "stock", "sling_rear", 9.3, 9.0, 24.0)
    aperture_rear_sight(g, "sights", rail_top, 11.6, rear_center)
    long_gun(g, pivot, sight=[8, rear_center, 11.6], muzzle=[8, 9.0, -15.9], left_hand=[8, 6.8, -3.6],
             mounts={"optic": [8, rail_top, 7.4], "muzzle": [8, 9.0, -14.3], "underbarrel": [8, 6.8, -8.0],
                     "stock": [8, 8.3, 13.4]},
             pivots={"action": [8, 10.9, 12.6], "magazine": [8, 6.4, 5.5]}, hip=[0.30, -0.37, -0.55], ads=0.27,
             eject=[9.8, 7.5, 7.5])
    return g


# ==================================================================================================================
# Bulwark-762: general-purpose machine gun
# ==================================================================================================================

def bulwark_762():
    g = Gun("bulwark_762", "lmg", scale=0.45)
    g.box("body", "receiver", (6.5, 7.0, 0.0), (9.5, 10.2, 13.6), "steel_dark", decal="stamp")
    for side, x0, x1 in (("l", 6.43, 6.5), ("r", 9.5, 9.57)):
        for i, (y, z) in enumerate(((7.6, 0.8), (9.6, 0.8), (7.6, 12.8), (9.6, 12.8), (7.6, 4.0), (7.6, 9.6))):
            g.box("body", f"rivet_{side}_{i}", (x0, y - 0.2, z - 0.2), (x1, y + 0.2, z + 0.2), "steel")
    g.box("body", "receiver_rib", (6.45, 8.4, 1.4), (6.5, 8.8, 12.4), "steel")
    g.box("body", "port", (9.45, 7.2, 5.0), (9.55, 8.2, 10.0), "recess")
    g.box("body", "charging_handle", (9.5, 7.4, 10.6), (10.9, 8.0, 11.4), "steel")
    # Feed cover (action), ribbed.
    g.box("action", "feed_cover", (6.7, 10.2, 0.4), (9.3, 11.2, 11.4), "steel_dark")
    for i, z in enumerate((2.0, 4.0, 6.0, 8.0)):
        g.box("action", f"cover_rib_{i}", (6.9, 11.2, z), (9.1, 11.45, z + 0.6), "steel")
    g.box("action", "cover_latch", (7.3, 10.2, 11.4), (8.7, 11.0, 12.2), "steel")
    # Belt from the right into the feed tray; ammunition box below (magazine).
    g.box("body", "feed_tray", (9.5, 8.6, 2.6), (10.1, 10.2, 6.6), "steel_dark")
    for i in range(4):
        y = 7.0 + i * 0.75
        g.box("magazine", f"round_{i}", (10.1 - i * 0.1, y, 3.2), (10.9 - i * 0.1, y + 0.6, 5.6), "brass")
        g.box("magazine", f"tip_{i}", (10.2 - i * 0.1, y + 0.1, 2.6), (10.8 - i * 0.1, y + 0.5, 3.2), "copper")
        g.box("magazine", f"link_{i}", (10.1 - i * 0.1, y + 0.05, 5.6), (10.9 - i * 0.1, y + 0.55, 6.2), "steel_dark")
    g.box("magazine", "box", (6.4, 1.6, 2.2), (10.6, 6.6, 8.2), "olive")
    g.box("magazine", "box_lid", (6.3, 6.2, 2.1), (10.7, 6.9, 8.3), "olive")
    g.box("magazine", "box_handle", (7.6, 6.9, 4.4), (9.4, 7.2, 6.0), "steel_dark")
    g.box("magazine", "box_rib_l", (6.3, 2.2, 3.0), (6.4, 6.0, 3.6), "olive")
    g.box("magazine", "box_rib_l2", (6.3, 2.2, 6.8), (6.4, 6.0, 7.4), "olive")
    # Fluted barrel, gas tube, side carry handle, folded bipod, front sight tower and conical flash hider.
    tube(g, "body", "barrel", 9.0, 0.7, -14.0, 0.0, "steel_dark")
    for i, (dx, dy) in enumerate(((1, 0), (-1, 0), (0, 1))):
        hx, hy = (0.03, 0.22) if dx else (0.22, 0.03)
        x, y = 8 + dx * 0.7, 9.0 + dy * 0.7
        g.box("body", f"flute_{i}", (x - hx, y - hy, -9.6), (x + hx, y + hy, -1.0), "anodized")
    g.box("body", "gas_tube", (7.4, 7.0, -11.0), (8.6, 7.8, 0.0), "steel_dark")
    g.box("body", "gas_block", (7.0, 6.8, -11.6), (9.0, 9.8, -10.6), "steel_dark")
    g.box("body", "barrel_lock", (6.8, 7.0, -0.8), (9.2, 10.0, 0.0), "steel_dark")
    g.box("body", "handle_clamp", (6.6, 8.2, -4.0), (9.4, 9.8, -3.0), "steel_dark")
    g.box("body", "handle_arm", (5.8, 8.6, -4.0), (6.6, 9.4, -3.0), "steel_dark")
    g.box("body", "handle_grip", (5.3, 8.4, -7.0), (6.1, 9.6, -2.6), "wood_dark")
    g.box("body", "bipod_clamp", (7.0, 6.4, -12.6), (9.0, 8.2, -11.6), "steel_dark")
    g.box("body", "bipod_leg_l", (7.1, 6.6, -11.6), (7.7, 7.2, -5.0), "steel")
    g.box("body", "bipod_leg_r", (8.3, 6.6, -11.6), (8.9, 7.2, -5.0), "steel")
    g.box("body", "fs_tower", (7.4, 9.4, -13.2), (8.6, 10.6, -12.2), "steel_dark")
    g.box("body", "fs_ear_l", (7.15, 10.6, -13.1), (7.5, 13.3, -12.3), "steel_dark")
    g.box("body", "fs_ear_r", (8.5, 10.6, -13.1), (8.85, 13.3, -12.3), "steel_dark")
    g.box("body", "fs_post", (7.85, 10.6, -12.85), (8.15, 12.85, -12.55), "steel_light")
    tube(g, "muzzle", "flash_hider", 9.0, 0.95, -15.0, -13.8, "steel_dark")
    tube(g, "muzzle", "flash_hider_tip", 9.0, 0.75, -15.7, -15.0, "steel_dark")
    g.box("muzzle", "fh_slot_l", (7.0, 8.6, -14.8), (7.06, 9.4, -13.9), "recess")
    g.box("muzzle", "fh_slot_r", (8.94, 8.6, -14.8), (9.0, 9.4, -13.9), "recess")
    crown(g, "muzzle", "crown", 9.0, 0.6, -15.7)
    # Rear tangent sight on the front of the cover (sights).
    g.box("sights", "rs_base", (7.1, 11.2, 0.6), (8.9, 12.3, 2.8), "steel_dark")
    g.box("sights", "rs_notch_l", (7.1, 12.3, 0.8), (7.75, 13.2, 1.2), "steel_dark")
    g.box("sights", "rs_notch_r", (8.25, 12.3, 0.8), (8.9, 13.2, 1.2), "steel_dark")
    # Fire controls and a skeletal wood stock.
    trigger_guard(g, "body", 8.6, 12.4, 4.2, 7.0, "steel_dark", width=1.6, thick=0.5)
    trigger(g, "body", 9.8, 7.0, 4.8, "steel")
    pivot = pistol_grip(g, "body", 11.8, 7.0, length=2.8, depth=7.0, width=2.3, mat="wood_dark", finger_grooves=False,
                        cap_mat="steel_dark")
    g.box("stock", "stock_top", (7.0, 8.4, 13.6), (9.0, 10.0, 25.6), "wood_dark")
    g.box("stock", "stock_bottom", (7.0, 4.4, 17.6), (9.0, 5.6, 25.6), "wood_dark")
    g.box("stock", "stock_diag", (7.0, 6.6, 13.8), (9.0, 7.8, 19.4), "wood_dark", rot=("x", 22.5, (8, 7.8, 13.8)))
    g.box("stock", "stock_rear", (6.9, 4.4, 24.6), (9.1, 10.0, 25.6), "wood_dark")
    g.box("stock", "butt_plate", (6.8, 4.3, 25.6), (9.2, 10.1, 26.2), "steel_dark")
    g.box("stock", "shoulder_strap", (7.4, 3.6, 25.0), (8.6, 4.4, 26.2), "steel_dark")
    sling_loop(g, "stock", "sling_rear", 9.0, 9.2, 22.0)
    long_gun(g, pivot, sight=[8, 12.85, 1.0], muzzle=[8, 9.0, -15.9], left_hand=[5.7, 9.0, -4.8],
             mounts={"optic": [8, 11.45, 6.0], "muzzle": [8, 9.0, -13.8], "underbarrel": [8, 6.8, -8.0],
                     "stock": [8, 8.8, 13.6]},
             pivots={"action": [8, 10.7, 11.4], "magazine": [8, 6.6, 5.2]}, hip=[0.30, -0.37, -0.55], ads=0.34,
             flash=1.2, eject=[9.7, 7.6, 7.5])
    return g


# ==================================================================================================================
# Voidcaster: experimental singularity projector
# ==================================================================================================================

def voidcaster():
    g = Gun("voidcaster", "energy", scale=0.5)
    g.box("body", "core", (6.4, 7.0, -4.0), (9.6, 10.8, 12.0), "void")
    g.box("body", "shell_top", (6.6, 10.8, -6.0), (9.4, 11.8, 10.4), "void_plate")
    g.box("body", "shell_top_bevel", (6.75, 10.9, -6.5), (9.25, 11.6, -6.0), "void_plate")
    g.box("body", "plate_l", (6.2, 7.4, -3.0), (6.4, 10.4, 8.0), "void_plate")
    g.box("body", "plate_r", (9.6, 7.4, -3.0), (9.8, 10.4, 8.0), "void_plate")
    g.box("body", "plate_l_rear", (6.2, 7.4, 8.6), (6.4, 10.4, 11.6), "void_plate")
    g.box("body", "plate_r_rear", (9.6, 7.4, 8.6), (9.8, 10.4, 11.6), "void_plate")
    # Glowing seams between armour plates.
    g.box("body", "seam_l", (6.25, 8.75, -3.0), (6.4, 9.05, 8.0), "glow_violet")
    g.box("body", "seam_r", (9.6, 8.75, -3.0), (9.75, 9.05, 8.0), "glow_violet")
    g.box("body", "seam_l_v", (6.3, 7.4, 8.0), (6.4, 10.4, 8.6), "glow_violet")
    g.box("body", "seam_r_v", (9.6, 7.4, 8.0), (9.7, 10.4, 8.6), "glow_violet")
    g.box("body", "seam_top_l", (6.55, 10.8, -5.4), (6.7, 11.0, 9.8), "glow_violet")
    g.box("body", "seam_top_r", (9.3, 10.8, -5.4), (9.45, 11.0, 9.8), "glow_violet")
    # Emitter: focusing barrel with glowing rings, three prongs and a lens.
    tube(g, "body", "emitter", 9.0, 0.8, -11.0, -4.0, "void")
    for i, z in enumerate((-10.0, -8.2, -6.4)):
        ring(g, "body", f"focus_ring_{i}", 9.0, 0.95, z, 0.35, "glow_violet")
    g.box("body", "prong_top", (7.6, 10.4, -12.6), (8.4, 11.0, -4.0), "void_plate")
    g.box("body", "prong_l", (6.0, 7.4, -12.2), (6.6, 8.0, -4.0), "void_plate")
    g.box("body", "prong_r", (9.4, 7.4, -12.2), (10.0, 8.0, -4.0), "void_plate")
    g.box("body", "prong_tip_top", (7.7, 10.2, -12.9), (8.3, 10.8, -12.4), "glow_violet")
    g.box("body", "prong_tip_l", (6.1, 7.6, -12.5), (6.7, 8.2, -12.0), "glow_violet")
    g.box("body", "prong_tip_r", (9.3, 7.6, -12.5), (9.9, 8.2, -12.0), "glow_violet")
    g.box("body", "prong_root_l", (6.0, 7.0, -4.6), (6.6, 8.4, -3.0), "void_plate")
    g.box("body", "prong_root_r", (9.4, 7.0, -4.6), (10.0, 8.4, -3.0), "void_plate")
    g.box("muzzle", "lens", (7.4, 8.4, -11.25), (8.6, 9.6, -11.0), "glow_violet")
    g.box("muzzle", "lens_ring", (7.1, 8.1, -11.1), (8.9, 9.9, -10.9), "void_plate")
    # Heat vent shutter (action) over a glowing channel.
    g.box("body", "vent_glow", (7.2, 11.8, 2.2), (8.8, 11.9, 7.8), "glow_violet")
    g.box("action", "vent_shutter", (7.0, 11.9, 2.0), (9.0, 12.3, 8.0), "void_plate")
    g.box("action", "vent_slats", (7.2, 12.3, 2.6), (8.8, 12.4, 7.4), "vent")
    # Ghost-ring sight with a glowing post (sights).
    g.box("sights", "ring_base", (7.0, 11.8, 9.0), (9.0, 12.1, 10.0), "void_plate")
    g.box("sights", "ring_l", (7.0, 12.1, 9.3), (7.35, 13.9, 9.7), "void_plate")
    g.box("sights", "ring_r", (8.65, 12.1, 9.3), (9.0, 13.9, 9.7), "void_plate")
    g.box("sights", "ring_top", (7.0, 13.9, 9.3), (9.0, 14.2, 9.7), "void_plate")
    g.box("sights", "post", (7.9, 12.1, 9.4), (8.1, 12.7, 9.6), "void_plate")
    g.box("sights", "post_tip", (7.85, 12.7, 9.35), (8.15, 13.0, 9.65), "glow_violet")
    # Grip, trigger and the energy cell (magazine) with a core window.
    trigger_guard(g, "body", 4.8, 8.6, 4.0, 7.0, "void_plate", width=1.5, thick=0.5)
    trigger(g, "body", 6.0, 7.0, 4.6, "steel")
    pivot = pistol_grip(g, "body", 8.6, 7.0, length=2.8, depth=7.0, width=2.3, mat="void_plate", finger_grooves=False,
                        cap_mat="void")
    g.box("body", "grip_glow", (6.8, 3.0, 8.9), (6.86, 6.4, 10.4), "glow_violet", rot=("x", GRIP_TILT, (8, 7.0, 10.0)))
    g.box("body", "cell_well", (6.7, 6.2, 0.4), (9.3, 7.0, 4.4), "void_plate")
    g.box("magazine", "cell", (7.0, 2.8, 0.8), (9.0, 6.2, 4.0), "void")
    g.box("magazine", "cell_window_l", (6.95, 3.4, 1.4), (7.0, 5.6, 3.4), "glow_violet")
    g.box("magazine", "cell_window_r", (9.0, 3.4, 1.4), (9.05, 5.6, 3.4), "glow_violet")
    g.box("magazine", "cell_cap", (6.8, 2.3, 0.6), (9.2, 2.8, 4.2), "void_plate")
    # Heat sink and compact stock.
    g.box("body", "heat_sink", (6.8, 7.6, 12.0), (9.2, 10.6, 15.4), "vent")
    g.box("body", "heat_sink_glow", (7.2, 8.2, 12.6), (8.8, 10.0, 14.8), "glow_violet", hidden=("north", "south"))
    g.box("stock", "stock", (6.9, 6.4, 15.4), (9.1, 10.4, 20.4), "void_plate")
    g.box("stock", "stock_seam", (6.85, 8.2, 15.6), (9.15, 8.5, 20.2), "glow_violet")
    butt_pad(g, "stock", 20.4, 6.4, 10.4, width=2.4)
    long_gun(g, pivot, sight=[8, 12.85, 9.5], muzzle=[8, 9.0, -11.4], left_hand=[8, 7.2, -2.6],
             mounts={}, pivots={"action": [8, 11.9, 8.0], "magazine": [8, 6.2, 2.4]}, hip=[0.29, -0.34, -0.55],
             ads=0.25, shell="none", flash=0.9, eject=[9.8, 9.0, 6.0])
    return g


# ==================================================================================================================
# Eclipse Railgun: capacitor-driven electromagnetic rifle with an integrated scope
# ==================================================================================================================

def eclipse_railgun():
    g = Gun("eclipse_railgun", "railgun", scale=0.46)
    g.box("body", "spine", (6.6, 6.8, -2.0), (9.4, 10.2, 16.0), "gunmetal", decal="stamp")
    g.box("body", "armor_top", (6.8, 10.2, -1.6), (9.2, 10.8, 14.0), "white")
    g.box("body", "armor_l", (6.4, 7.4, 0.0), (6.6, 9.8, 9.0), "white")
    g.box("body", "armor_r", (9.4, 7.4, 0.0), (9.6, 9.8, 9.0), "white")
    g.box("body", "glow_l", (6.45, 8.45, 0.4), (6.6, 8.75, 8.6), "glow_cyan")
    g.box("body", "glow_r", (9.4, 8.45, 0.4), (9.55, 8.75, 8.6), "glow_cyan")
    # Twin rails with glowing inner faces; insulating side braces.
    g.box("body", "rail_top", (6.8, 9.6, -15.6), (9.2, 10.6, -2.0), "steel_dark")
    g.box("body", "rail_bottom", (6.8, 6.4, -15.4), (9.2, 7.4, -2.0), "steel_dark")
    g.box("body", "core_top", (7.4, 9.4, -15.0), (8.6, 9.6, -2.0), "glow_cyan")
    g.box("body", "core_bottom", (7.4, 7.4, -15.0), (8.6, 7.6, -2.0), "glow_cyan")
    g.box("body", "rail_fin_top", (7.6, 10.6, -14.6), (8.4, 11.2, -3.0), "white")
    for i, z in enumerate((-14.2, -10.8, -7.4, -4.0)):
        g.box("body", f"brace_l_{i}", (6.5, 6.2, z), (6.8, 10.8, z + 0.9), "white")
        g.box("body", f"brace_r_{i}", (9.2, 6.2, z), (9.5, 10.8, z + 0.9), "white")
        g.box("body", f"brace_glow_l_{i}", (6.45, 8.3, z + 0.2), (6.5, 8.7, z + 0.7), "glow_cyan")
        g.box("body", f"brace_glow_r_{i}", (9.5, 8.3, z + 0.2), (9.55, 8.7, z + 0.7), "glow_cyan")
    g.box("muzzle", "muzzle_cap_top", (6.6, 9.4, -15.9), (9.4, 10.8, -15.4), "white")
    g.box("muzzle", "muzzle_cap_bottom", (6.6, 6.2, -15.9), (9.4, 7.6, -15.4), "white")
    # Charging collar (action) that slides forward as the capacitors charge.
    g.box("action", "collar", (6.3, 6.0, -2.8), (9.7, 11.0, -1.4), "white")
    g.box("action", "collar_glow", (6.25, 8.2, -2.4), (9.75, 8.8, -1.8), "glow_cyan")
    # Capacitor banks on both flanks.
    for side, cx in (("l", 5.6), ("r", 10.4)):
        tube(g, "body", f"cap_{side}", 8.4, 0.7, 1.0, 9.0, "white", cx=cx)
        for i, z in enumerate((2.4, 4.6, 6.8)):
            ring(g, "body", f"cap_{side}_band_{i}", 8.4, 0.78, z, 0.3, "glow_cyan", cx=cx)
        g.box("body", f"cap_{side}_cap", (cx - 0.5, 7.9, 0.6), (cx + 0.5, 8.9, 1.0), "steel_dark")
    # Integrated scope (sights).
    for i, z in enumerate((2.0, 9.0)):
        g.box("sights", f"scope_mount_{i}", (7.3, 10.8, z), (8.7, 11.4, z + 1.0), "anodized")
        g.box("sights", f"scope_ring_{i}", (7.0, 11.2, z), (9.0, 13.2, z + 1.0), "anodized")
    tube(g, "sights", "scope_tube", 12.2, 0.8, 0.6, 11.0, "anodized")
    tube(g, "sights", "scope_objective", 12.2, 1.15, -1.6, 0.6, "anodized")
    tube(g, "sights", "scope_ocular", 12.2, 1.05, 11.0, 13.2, "anodized")
    g.box("sights", "scope_lens_front", (7.1, 11.3, -1.68), (8.9, 13.1, -1.6), "glow_cyan")
    g.box("sights", "scope_lens_rear", (7.2, 11.4, 13.2), (8.8, 13.0, 13.26), "glass")
    g.box("sights", "turret_top", (7.45, 13.0, 5.2), (8.55, 13.7, 6.4), "white")
    g.box("sights", "turret_side", (8.8, 11.65, 5.2), (9.5, 12.75, 6.4), "white")
    g.box("sights", "turret_glow", (7.6, 13.7, 5.45), (8.4, 13.78, 6.15), "glow_cyan")
    # Grip, energy cell (magazine), forward grip and stock.
    trigger_guard(g, "body", 6.0, 9.8, 4.0, 6.8, "gunmetal", width=1.5, thick=0.5)
    trigger(g, "body", 7.2, 6.8, 4.6, "steel")
    pivot = pistol_grip(g, "body", 9.6, 6.8, length=2.9, depth=7.0, width=2.3, mat="grip", cap_mat="white")
    g.box("magazine", "cell", (6.9, 3.2, 11.6), (9.1, 6.8, 15.2), "white")
    g.box("magazine", "cell_window", (9.1, 3.8, 12.2), (9.16, 6.2, 14.6), "glow_cyan")
    g.box("magazine", "cell_window_l", (6.84, 3.8, 12.2), (6.9, 6.2, 14.6), "glow_cyan")
    g.box("magazine", "cell_cap", (6.8, 2.7, 11.4), (9.2, 3.2, 15.4), "gunmetal")
    g.box("body", "fore_grip", (7.0, 3.4, -6.0), (9.0, 6.4, -4.0), "grip")
    g.box("body", "fore_grip_cap", (6.9, 3.0, -6.1), (9.1, 3.4, -3.9), "white")
    g.box("body", "stock", (6.7, 5.2, 16.0), (9.3, 10.2, 22.4), "white")
    g.box("body", "stock_vent_l", (6.64, 6.0, 17.0), (6.7, 8.8, 21.2), "vent")
    g.box("body", "stock_vent_r", (9.3, 6.0, 17.0), (9.36, 8.8, 21.2), "vent")
    g.box("body", "cheek", (7.0, 10.2, 16.4), (9.0, 10.8, 21.6), "gunmetal")
    butt_pad(g, "body", 22.4, 5.2, 10.8, width=2.6)
    long_gun(g, pivot, sight=[8, 12.2, 13.2], muzzle=[8, 8.5, -16.0], left_hand=[8, 4.9, -5.0],
             mounts={}, pivots={"action": [8, 8.5, -2.1], "magazine": [8, 6.8, 13.4]}, hip=[0.30, -0.36, -0.55],
             ads=0.22, shell="none", flash=1.6, eject=[9.8, 9.0, 6.0])
    return g


ALL = [r9_sidearm, ironfang_50, warden_357, viper_9, spectre_pdw, arx_16, raven_ak, mantis_br, breacher_12, thunderpump,
       ghostline_m24, longshot_50, titan_56, bulwark_762, voidcaster, eclipse_railgun]
