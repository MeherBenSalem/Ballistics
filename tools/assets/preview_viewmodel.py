"""Offline preview of first-person weapon poses.

Mirrors ViewmodelRenderer.java: view space (+X right, +Y up, -Z forward), Minecraft's fixed 70 degree hand FOV,
the hip/ADS placement, the model-pixel scale and the arm placement toward fixed shoulder points.

    python3 tools/assets/preview_viewmodel.py out_dir [weapon ...]
"""
from __future__ import annotations

import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(__file__))

import guns as gun_defs  # noqa: E402
from raster import Camera, Renderer, box_quads, model_quads, rotation_matrix  # noqa: E402
from voxel import build_texture  # noqa: E402

RIGHT_SHOULDER = np.array([0.42, -0.72, 0.18])
LEFT_SHOULDER = np.array([-0.36, -0.74, 0.12])
ARM_SCALE = 0.55
ARM_LENGTH = 12 / 16
SKIN = (196, 140, 110, 255)
SLEEVE = (40, 160, 170, 255)


def mat4_translate(t):
    m = np.eye(4)
    m[:3, 3] = t
    return m


def mat4_scale(s):
    m = np.eye(4)
    m[0, 0], m[1, 1], m[2, 2] = s
    return m


def mat4_rot(axis, deg):
    m = np.eye(4)
    m[:3, :3] = rotation_matrix(axis, deg)
    return m


def rotation_to(a, b):
    """Shortest-arc rotation matrix mapping unit vector a onto unit vector b (like JOML rotationTo)."""
    a = a / np.linalg.norm(a)
    b = b / np.linalg.norm(b)
    v = np.cross(a, b)
    c = float(np.dot(a, b))
    if c < -0.999999:
        return np.diag([1, -1, -1])
    vx = np.array([[0, -v[2], v[1]], [v[2], 0, -v[0]], [-v[1], v[0], 0]])
    return np.eye(3) + vx + vx @ vx * (1 / (1 + c))


def viewmodel_matrix(vis, ads):
    pivot = np.array(vis["pivot"], dtype=float)
    sight = np.array(vis["sight"], dtype=float)
    s = vis["scale"]
    hip = np.array(vis["hip"], dtype=float)
    ads_pos = np.array([0, 0, -vis["ads_distance"]]) - (sight - pivot) * (s / 16)
    pos = hip + (ads_pos - hip) * ads
    rot = np.array(vis.get("hip_rotation", [0, 0, 0]), dtype=float) * (1 - ads)
    m = mat4_translate(pos) @ mat4_rot("z", rot[2]) @ mat4_rot("y", rot[1]) @ mat4_rot("x", rot[0])
    m = m @ mat4_scale((s, s, s)) @ mat4_translate(-pivot / 16)
    return m


def apply(m, pts):
    h = np.c_[pts, np.ones(len(pts))]
    return (h @ m.T)[:, :3]


def arm_quads(hand, shoulder, right):
    d = hand - shoulder
    dist = np.linalg.norm(d)
    d /= dist
    r = rotation_to(np.array([0, 1.0, 0]), d) @ rotation_matrix("y", 90 if right else -90)
    length_scale = max(ARM_SCALE, dist / ARM_LENGTH)
    hand_local = np.array([-6 / 16, 12 / 16, 0]) if right else np.array([6 / 16, 12 / 16, 0])
    frm = (-8 / 16, 0, -2 / 16) if right else (4 / 16, 0, -2 / 16)
    to = (-4 / 16, 12 / 16, 2 / 16) if right else (8 / 16, 12 / 16, 2 / 16)

    def transform(pts, normal):
        local = (pts - hand_local) * np.array([ARM_SCALE, length_scale, ARM_SCALE])
        world = local @ r.T + hand
        return world, r @ normal

    def color(face, v):
        return SKIN if v < 0.66 else SLEEVE

    return box_quads(frm, to, color, transform)


def render_viewmodel(gun, ads, size=(960, 540)):
    vis = gun.visuals
    img, mats, uvs = build_texture(gun.model, seed=hash(gun.name) & 0xFFFF)
    quads = model_quads(gun.model, img, uvs)
    m = viewmodel_matrix(vis, ads)
    for q in quads:
        q.pts = apply(m, q.pts / 16.0)
        q.normal = m[:3, :3] @ q.normal
        q.normal /= np.linalg.norm(q.normal)
    right_hand = apply(m, np.array([vis["pivot"]]) / 16.0)[0]
    left_hand = apply(m, np.array([vis["left_hand"]]) / 16.0)[0]
    quads += arm_quads(right_hand, RIGHT_SHOULDER, True)
    quads += arm_quads(left_hand, LEFT_SHOULDER, False)
    cam = Camera(size[0], size[1], eye=(0, 0, 0), target=(0, 0, -1), fov=70, near=0.04)
    r = Renderer(size[0], size[1], background=(118, 160, 214, 255))
    r.draw(quads, cam, shading="lambert", light_dir=(0.3, 0.9, 0.4))
    out = r.image()
    d = ImageDraw.Draw(out)
    cx, cy = size[0] // 2, size[1] // 2
    d.line((cx - 6, cy, cx + 6, cy), fill=(255, 255, 255, 255))
    d.line((cx, cy - 6, cx, cy + 6), fill=(255, 255, 255, 255))
    return out


def main():
    out_dir = sys.argv[1] if len(sys.argv) > 1 else "viewmodel_preview"
    names = set(sys.argv[2:])
    os.makedirs(out_dir, exist_ok=True)
    for fn in gun_defs.ALL:
        gun = fn()
        if names and gun.name not in names:
            continue
        for ads in (0.0, 1.0):
            img = render_viewmodel(gun, ads)
            img.save(os.path.join(out_dir, f"{gun.name}_{'ads' if ads else 'hip'}.png"))
        print("rendered", gun.name)


if __name__ == "__main__":
    main()
