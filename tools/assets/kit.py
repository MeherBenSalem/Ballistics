"""Reusable, detail-rich weapon components for the voxel models.

Every function adds boxes to a Gun (see guns.py) and returns useful anchor points. Coordinates are model pixels:
barrel toward -Z, +Y up, weapon centred on x = 8. Sub-pixel sizes are fine (Java models accept floats); texture
density keeps 2 texels per pixel so 0.5 px details still get their own painted texels.
"""
from __future__ import annotations

import math

CX = 8.0
GRIP_TILT = -22.5


def rot_point(p, axis, angle, origin):
    a = math.radians(angle)
    x, y, z = (p[i] - origin[i] for i in range(3))
    c, s = math.cos(a), math.sin(a)
    if axis == "x":
        y, z = y * c - z * s, y * s + z * c
    elif axis == "y":
        x, z = x * c + z * s, -x * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return [round(x + origin[0], 3), round(y + origin[1], 3), round(z + origin[2], 3)]


# ------------------------------------------------------------------------------------------------------------------
# Round stock: barrels, tubes, suppressors (square + 45 degree box = octagonal silhouette)
# ------------------------------------------------------------------------------------------------------------------

def tube(g, part, name, cy, r, z0, z1, mat, cx=CX, bevel=0.82):
    g.box(part, name, (cx - r, cy - r, z0), (cx + r, cy + r, z1), mat)
    rr = r * bevel
    g.box(part, name + "_oct", (cx - rr, cy - rr, z0 + 0.01), (cx + rr, cy + rr, z1 - 0.01), mat,
          rot=("z", 45, (cx, cy, (z0 + z1) / 2)))


def ring(g, part, name, cy, r, z, length, mat, cx=CX):
    tube(g, part, name, cy, r, z, z + length, mat, cx)


def crown(g, part, name, cy, r, z, mat="steel_dark", cx=CX):
    """Barrel crown: a thin darker face with a bore recess."""
    g.box(part, name, (cx - r, cy - r, z - 0.2), (cx + r, cy + r, z), mat)
    g.box(part, name + "_bore", (cx - r * 0.45, cy - r * 0.45, z - 0.25), (cx + r * 0.45, cy + r * 0.45, z - 0.19), "recess")


# ------------------------------------------------------------------------------------------------------------------
# Rails and mounting surfaces
# ------------------------------------------------------------------------------------------------------------------

def picatinny(g, part, name, y, z0, z1, width=2.0, mat="rail", cx=CX, pitch=1.0, base_h=0.35, tooth_h=0.45):
    """MIL-STD-1913 style rail: a base strip with raised teeth every `pitch` pixels."""
    g.box(part, name + "_base", (cx - width / 2 + 0.15, y, z0), (cx + width / 2 - 0.15, y + base_h, z1), mat)
    z = z0 + 0.15
    i = 0
    while z + pitch * 0.5 <= z1 - 0.1:
        g.box(part, f"{name}_t{i}", (cx - width / 2, y + base_h, z), (cx + width / 2, y + base_h + tooth_h, z + pitch * 0.55), mat)
        z += pitch
        i += 1
    return y + base_h + tooth_h


def bottom_rail(g, part, name, y, z0, z1, width=2.0, mat="rail", cx=CX, pitch=1.0):
    """Picatinny rail facing down (accessory rail under a handguard)."""
    g.box(part, name + "_base", (cx - width / 2 + 0.15, y - 0.35, z0), (cx + width / 2 - 0.15, y, z1), mat)
    z = z0 + 0.15
    i = 0
    while z + pitch * 0.5 <= z1 - 0.1:
        g.box(part, f"{name}_t{i}", (cx - width / 2, y - 0.8, z), (cx + width / 2, y - 0.35, z + pitch * 0.55), mat)
        z += pitch
        i += 1
    return y - 0.8


def serrations(g, part, name, x_side, y0, y1, z0, z1, count, mat="steel_dark", depth=0.12):
    """Raised vertical ridges on a slide or receiver side (x_side = face x; depth grows outward)."""
    step = (z1 - z0) / count
    sign = 1 if x_side > CX else -1
    for i in range(count):
        z = z0 + i * step
        x0, x1 = (x_side, x_side + depth) if sign > 0 else (x_side - depth, x_side)
        g.box(part, f"{name}_{i}", (x0, y0, z), (x1, y1, z + step * 0.45), mat)


def screw(g, part, name, x_side, y, z, mat="steel", size=0.5):
    sign = 1 if x_side > CX else -1
    x0, x1 = (x_side, x_side + 0.08) if sign > 0 else (x_side - 0.08, x_side)
    g.box(part, name, (x0, y - size / 2, z - size / 2), (x1, y + size / 2, z + size / 2), mat)


def both_sides(fn, g, part, name, x_left, x_right, *args, **kwargs):
    fn(g, part, name + "_l", x_left, *args, **kwargs)
    fn(g, part, name + "_r", x_right, *args, **kwargs)


# ------------------------------------------------------------------------------------------------------------------
# Fire control
# ------------------------------------------------------------------------------------------------------------------

def trigger_guard(g, part, front_z, back_z, bottom_y, top_y, mat="polymer", width=1.4, thick=0.55, cx=CX):
    hw = width / 2
    g.box(part, "guard_front", (cx - hw, bottom_y, front_z), (cx + hw, top_y, front_z + thick), mat)
    g.box(part, "guard_bottom", (cx - hw, bottom_y, front_z), (cx + hw, bottom_y + thick, back_z), mat)
    g.box(part, "guard_corner", (cx - hw + 0.05, bottom_y - 0.1, front_z - 0.15), (cx + hw - 0.05, bottom_y + 0.6, front_z + 0.6), mat,
          rot=("x", 45, (cx, bottom_y + 0.25, front_z + 0.25)))


def trigger(g, part, z, top_y, bottom_y, mat="steel", blade=False, cx=CX):
    g.box(part, "trigger", (cx - 0.35, (top_y + bottom_y) / 2, z), (cx + 0.35, top_y, z + 0.6), mat)
    g.box(part, "trigger_curve", (cx - 0.35, bottom_y, z + 0.1), (cx + 0.35, (top_y + bottom_y) / 2 + 0.2, z + 0.65), mat,
          rot=("x", 22.5, (cx, (top_y + bottom_y) / 2, z + 0.3)))
    if blade:
        g.box(part, "trigger_safety", (cx - 0.1, bottom_y + 0.3, z - 0.12), (cx + 0.1, top_y - 0.6, z + 0.05), "steel_light")


def pistol_grip(g, part, z, top_y, length=3.0, depth=7.5, width=2.4, mat="grip", cx=CX, finger_grooves=True,
                cap_mat=None, tilt=GRIP_TILT):
    """Raked pistol grip with finger grooves, palm swell and bottom cap. Returns the hand anchor (grip centre)."""
    origin = (cx, top_y, z + length / 2)
    hw = width / 2
    rot = ("x", tilt, origin)
    g.box(part, "grip", (cx - hw, top_y - depth, z), (cx + hw, top_y, z + length), mat, rot=rot)
    g.box(part, "grip_swell", (cx - hw - 0.12, top_y - depth * 0.75, z + length * 0.35), (cx + hw + 0.12, top_y - depth * 0.2, z + length - 0.3),
          mat, rot=rot)
    if finger_grooves:
        for i in range(3):
            y = top_y - depth * (0.3 + 0.22 * i)
            g.box(part, f"grip_finger_{i}", (cx - hw + 0.2, y - 0.5, z - 0.3), (cx + hw - 0.2, y, z + 0.05), mat, rot=rot)
    g.box(part, "grip_cap", (cx - hw - 0.1, top_y - depth - 0.45, z - 0.15), (cx + hw + 0.1, top_y - depth, z + length + 0.15),
          cap_mat or "polymer", rot=rot)
    c = [cx, top_y - depth * 0.5, z + length / 2]
    return rot_point(c, "x", tilt, origin)


def selector(g, part, x_side, y, z, mat="steel"):
    sign = 1 if x_side > CX else -1
    x0, x1 = (x_side, x_side + 0.25) if sign > 0 else (x_side - 0.25, x_side)
    g.box(part, "selector_hub" + ("_r" if sign > 0 else "_l"), (x0, y - 0.35, z - 0.35), (x1, y + 0.35, z + 0.35), mat)
    g.box(part, "selector_lever" + ("_r" if sign > 0 else "_l"), (x0, y - 0.15, z - 0.2), (x1 + 0.1 * sign, y + 0.15, z + 1.1), mat)


# ------------------------------------------------------------------------------------------------------------------
# Sights
# ------------------------------------------------------------------------------------------------------------------

def folding_front_sight(g, part, rail_top, z, cx=CX, mat="steel_dark"):
    """Flip-up front sight with protective wings and a centre post. Returns the post tip height."""
    g.box(part, "fs_clamp", (cx - 1.0, rail_top - 0.45, z - 0.8), (cx + 1.0, rail_top + 0.35, z + 0.8), mat)
    g.box(part, "fs_body", (cx - 0.7, rail_top + 0.35, z - 0.45), (cx + 0.7, rail_top + 0.9, z + 0.45), mat)
    g.box(part, "fs_wing_l", (cx - 1.0, rail_top + 0.9, z - 0.35), (cx - 0.55, rail_top + 2.5, z + 0.35), mat)
    g.box(part, "fs_wing_r", (cx + 0.55, rail_top + 0.9, z - 0.35), (cx + 1.0, rail_top + 2.5, z + 0.35), mat)
    tip = rail_top + 1.95
    g.box(part, "fs_post", (cx - 0.16, rail_top + 0.9, z - 0.16), (cx + 0.16, tip, z + 0.16), "steel_light")
    return tip


def aperture_rear_sight(g, part, rail_top, z, center_y, cx=CX, mat="steel_dark"):
    """Flip-up rear aperture: clamp, ears and a ring whose centre sits at center_y."""
    g.box(part, "rs_clamp", (cx - 1.0, rail_top - 0.45, z - 0.8), (cx + 1.0, rail_top + 0.35, z + 0.8), mat)
    g.box(part, "rs_ear_l", (cx - 1.05, rail_top + 0.35, z - 0.35), (cx - 0.65, center_y + 1.0, z + 0.35), mat)
    g.box(part, "rs_ear_r", (cx + 0.65, rail_top + 0.35, z - 0.35), (cx + 1.05, center_y + 1.0, z + 0.35), mat)
    g.box(part, "rs_ring_top", (cx - 0.65, center_y + 0.35, z - 0.15), (cx + 0.65, center_y + 0.7, z + 0.15), mat)
    g.box(part, "rs_ring_bottom", (cx - 0.65, center_y - 0.7, z - 0.15), (cx + 0.65, center_y - 0.35, z + 0.15), mat)
    g.box(part, "rs_ring_l", (cx - 0.65, center_y - 0.35, z - 0.15), (cx - 0.35, center_y + 0.35, z + 0.15), mat)
    g.box(part, "rs_ring_r", (cx + 0.35, center_y - 0.35, z - 0.15), (cx + 0.65, center_y + 0.35, z + 0.15), mat)
    g.box(part, "rs_stem", (cx - 0.3, rail_top + 0.35, z - 0.15), (cx + 0.3, center_y - 0.7, z + 0.15), mat)


def notch_rear_sight(g, part, top_y, z, height=1.0, cx=CX, mat="steel_dark", dots=True, length=1.0):
    g.box(part, "rear_base", (cx - 1.1, top_y, z), (cx + 1.1, top_y + 0.3, z + length), mat)
    g.box(part, "rear_ear_l", (cx - 1.1, top_y + 0.3, z), (cx - 0.35, top_y + height, z + length), mat)
    g.box(part, "rear_ear_r", (cx + 0.35, top_y + 0.3, z), (cx + 1.1, top_y + height, z + length), mat)
    if dots:
        g.box(part, "rear_dot_l", (cx - 0.85, top_y + height - 0.55, z + length), (cx - 0.55, top_y + height - 0.25, z + length + 0.03), "dot")
        g.box(part, "rear_dot_r", (cx + 0.55, top_y + height - 0.55, z + length), (cx + 0.85, top_y + height - 0.25, z + length + 0.03), "dot")
    return top_y + height - 0.4


def post_front_sight(g, part, top_y, z, height=0.8, cx=CX, mat="steel_dark", dot=True):
    g.box(part, "front_post", (cx - 0.35, top_y, z), (cx + 0.35, top_y + height, z + 0.9), mat)
    if dot:
        g.box(part, "front_dot", (cx - 0.15, top_y + height - 0.45, z + 0.9), (cx + 0.15, top_y + height - 0.15, z + 0.93), "dot")
    return top_y + height


# ------------------------------------------------------------------------------------------------------------------
# Magazines and stocks
# ------------------------------------------------------------------------------------------------------------------

def stick_mag(g, part, top_y, z0, z1, straight, curved=0.0, width=2.0, mat="ribs", base_mat="polymer", cx=CX, curve_angle=22.5):
    """Box magazine: straight section (top_y - straight .. top_y) plus an optional forward-curving lower section."""
    hw = width / 2
    g.box(part, "mag_body", (cx - hw, top_y - straight, z0), (cx + hw, top_y, z1), mat)
    g.box(part, "mag_spine", (cx - hw + 0.3, top_y - straight, z1), (cx + hw - 0.3, top_y - 0.6, z1 + 0.25), mat)
    bottom = top_y - straight
    rot = None
    zc = (z0 + z1) / 2
    if curved > 0:
        origin = (cx, bottom, zc)
        rot = ("x", curve_angle, origin)
        g.box(part, "mag_curve", (cx - hw, bottom - curved, z0), (cx + hw, bottom + 0.2, z1), mat, rot=rot)
        bottom -= curved
    g.box(part, "mag_base", (cx - hw - 0.2, bottom - 0.55, z0 - 0.25), (cx + hw + 0.2, bottom + 0.05, z1 + 0.3), base_mat, rot=rot)
    g.box(part, "mag_base_lip", (cx - hw - 0.05, bottom - 0.7, z0 + 0.3), (cx + hw + 0.05, bottom - 0.55, z1 - 0.2), base_mat, rot=rot)


def butt_pad(g, part, z, y0, y1, width=3.0, cx=CX, mat="rubber"):
    hw = width / 2
    g.box(part, "butt_pad", (cx - hw, y0, z), (cx + hw, y1, z + 0.8), mat)
    for i in range(3):
        y = y0 + (y1 - y0) * (0.25 + 0.25 * i)
        g.box(part, f"butt_rib_{i}", (cx - hw + 0.2, y - 0.15, z + 0.8), (cx + hw - 0.2, y + 0.15, z + 0.95), mat)


def sling_loop(g, part, name, x_side, y, z, mat="steel_dark"):
    sign = 1 if x_side > CX else -1
    x0, x1 = (x_side, x_side + 0.35) if sign > 0 else (x_side - 0.35, x_side)
    g.box(part, name, (x0, y - 0.5, z - 0.5), (x1, y + 0.5, z + 0.5), mat)
    g.box(part, name + "_hole", (x0 - 0.01 * sign, y - 0.2, z - 0.2), (x1 + 0.01 * sign, y + 0.2, z + 0.2), "recess")
