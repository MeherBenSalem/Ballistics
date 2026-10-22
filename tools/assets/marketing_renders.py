"""Offline renders for the marketing kit (marketing/source/renders and marketing/images/renders).

These are renders of the mod's own models made by the software rasteriser in this folder, not in-game screenshots:
three-quarter hero shots, flat side views, attachment shots and first-person views on a small voxel shooting range
that mirror the in-game viewmodel placement (see preview_viewmodel.py).

    python3 tools/assets/marketing_renders.py
"""
from __future__ import annotations

import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import attachments as att_defs  # noqa: E402
import guns as gun_defs  # noqa: E402
from preview_viewmodel import ARM_LENGTH, ARM_SCALE, LEFT_SHOULDER, RIGHT_SHOULDER, apply, rotation_to, viewmodel_matrix  # noqa: E402
from raster import Camera, Quad, Renderer, box_quads, model_quads, rotation_matrix  # noqa: E402
from render_hero import hero  # noqa: E402
from voxel import build_texture, skin_texture  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
SRC = os.path.join(ROOT, "marketing", "source", "renders")
GALLERY = os.path.join(ROOT, "marketing", "images", "renders")


def seed_of(name):
    return sum((i + 1) * ord(c) for i, c in enumerate(name))


def textured(g, skin=None):
    tex, mats, uvs = build_texture(g.model, seed=seed_of(g.name))
    if skin:
        tex = skin_texture(tex, mats, skin, seed_of(g.name + skin))
    return tex, uvs


def supersampled(fn, size, ss=2):
    img = fn((size[0] * ss, size[1] * ss))
    return img.resize(size, Image.LANCZOS)


def crop_alpha(img, pad=8):
    box = img.getbbox()
    if not box:
        return img
    x0, y0, x1, y1 = box
    return img.crop((max(0, x0 - pad), max(0, y0 - pad), min(img.width, x1 + pad), min(img.height, y1 + pad)))


# ------------------------------------------------------------------------------------------------------------------
# Studio shots
# ------------------------------------------------------------------------------------------------------------------

def hero_shot(g, skin=None, yaw=118, pitch=14, size=(1600, 900)):
    tex, uvs = textured(g, skin)
    return supersampled(lambda s: hero(g.model, tex, uvs, size=s, yaw=yaw, pitch=pitch, margin=0.56), size)


def side_shot(model, tex, uvs, width=900):
    lo, hi = model.bounds()
    length = hi[2] - lo[2]
    height = hi[1] - lo[1]
    w, h = width * 2, int(width * 2 * max(0.3, height / length) * 1.15)
    center = np.array([(lo[i] + hi[i]) / 2 for i in range(3)])
    cam = Camera(w, h, eye=tuple(center + np.array([80.0, 6.0, -10.0])), target=tuple(center), scale=w * 0.92 / length)
    r = Renderer(w, h)
    r.draw(model_quads(model, tex, uvs), cam, shading="lambert", light_dir=(0.6, 0.8, -0.3))
    return crop_alpha(r.image().resize((w // 2, h // 2), Image.LANCZOS))


def build_shot(g, fitted, size=(1600, 900), yaw=118, pitch=14, skin=None):
    """Weapon with attachment models placed on its mounts, hiding the parts they replace (as GunRenderer does)."""
    tex, uvs = textured(g, skin)
    slots = {a().slot for a in fitted}
    hidden = {"optic": "sights", "stock": "stock", "muzzle": "muzzle"}
    removed = {hidden[s] for s in slots if s in hidden}
    if any(a().name == "drum_mag" for a in fitted):
        removed.add("magazine")  # only drum magazines are drawn; they replace the weapon's own magazine
    parts = [p for p in ("body", "action", "magazine", "stock", "sights", "muzzle") if p not in removed]
    quads = model_quads(g.model, tex, uvs, parts=parts)
    for fn in fitted:
        a = fn()
        if a.slot == "magazine" and a.name != "drum_mag":
            continue
        atex, _, auvs = build_texture(a.model, seed=seed_of(a.name))
        mount = np.array(g.visuals["mounts"][a.slot], dtype=float)
        for q in model_quads(a.model, atex, auvs):
            q.pts = q.pts + (mount - 8.0)
            quads.append(q)
    pts = np.concatenate([q.pts for q in quads])
    lo, hi = pts.min(axis=0), pts.max(axis=0)
    center = (lo + hi) / 2
    radius = 0.5 * float(np.linalg.norm(hi - lo))

    def render(s):
        fov = 26.0
        distance = radius / math.sin(math.radians(fov) / 2) * 0.56
        yr, pr = math.radians(yaw), math.radians(pitch)
        eye = center + distance * np.array([math.sin(yr) * math.cos(pr), math.sin(pr), math.cos(yr) * math.cos(pr)])
        cam = Camera(s[0], s[1], eye=tuple(eye), target=tuple(center), fov=fov, near=0.5)
        r = Renderer(s[0], s[1])
        r.draw(quads, cam, shading="lambert", light_dir=(0.55, 0.8, -0.25))
        return r.image()

    return supersampled(render, size)


# ------------------------------------------------------------------------------------------------------------------
# First-person range scene
# ------------------------------------------------------------------------------------------------------------------

def _tex(img):
    return np.asarray(img.convert("RGBA"))


def _quad(pts, img, face="up", normal=(0, 1, 0), repeat=(1, 1)):
    tex = _tex(img)
    h, w = tex.shape[:2]
    uv = np.array([[0, 0], [w, 0], [w, h], [0, h]], dtype=float)
    return Quad(np.array(pts, dtype=float), uv, tex, np.array(normal, dtype=float), face)


def _noise_tile(w, h, base, var, seed, blocks=True):
    rng = np.random.default_rng(seed)
    n = rng.integers(-var, var + 1, size=(h, w, 1))
    arr = np.clip(np.array(base)[None, None, :] + n, 0, 255).astype(np.uint8)
    return Image.fromarray(np.dstack([arr, np.full((h, w, 1), 255, np.uint8)]), "RGBA")


def _target_face(size=64):
    img = Image.new("RGBA", (size, size), (236, 232, 222, 255))
    d = ImageDraw.Draw(img)
    c = size / 2
    for i, col in enumerate([(205, 52, 44), (236, 232, 222), (205, 52, 44), (236, 232, 222), (205, 52, 44)]):
        r = c * (0.95 - i * 0.19)
        d.ellipse((c - r, c - r, c + r, c + r), fill=col + (255,))
    d.rectangle((0, 0, size - 1, size - 1), outline=(120, 92, 60, 255), width=3)
    return img


def range_scene():
    """World-space quads (camera at the origin looking down -Z, eyes 1.62 above the grass)."""
    g = -1.62
    quads = []
    grass = _noise_tile(64, 80, (96, 148, 62), 9, 1)
    quads.append(_quad([(-32, g, -70), (32, g, -70), (32, g, 10), (-32, g, 10)], grass, "up", (0, 1, 0)))
    gravel = _noise_tile(8, 70, (128, 122, 112), 12, 2)
    quads.append(_quad([(-2.5, g + 0.01, -62), (2.5, g + 0.01, -62), (2.5, g + 0.01, 2), (-2.5, g + 0.01, 2)], gravel, "up", (0, 1, 0)))
    wall = _noise_tile(40, 6, (118, 120, 124), 10, 3)
    quads.append(_quad([(-20, g + 6, -62), (20, g + 6, -62), (20, g, -62), (-20, g, -62)], wall, "south", (0, 0, 1)))
    berm = _noise_tile(40, 4, (112, 84, 58), 8, 4)
    quads.append(_quad([(-20, g + 6, -62), (20, g + 6, -62), (20, g + 6, -58), (-20, g + 6, -58)], berm, "up", (0, 1, 0)))
    target = _target_face()
    wood = _noise_tile(2, 16, (140, 98, 58), 10, 5)
    for x, z, s in ((-0.8, -16, 1.2), (1.6, -26, 1.2), (-2.2, -38, 1.4), (2.6, -50, 1.6)):
        top = g + 2.2
        quads.append(_quad([(x - s / 2, top, z), (x + s / 2, top, z), (x + s / 2, top - s, z), (x - s / 2, top - s, z)], target, "south", (0, 0, 1)))
        quads.append(_quad([(x - 0.08, top - s, z - 0.02), (x + 0.08, top - s, z - 0.02), (x + 0.08, g, z - 0.02), (x - 0.08, g, z - 0.02)],
                           wood, "south", (0, 0, 1)))
    return quads


def sky(size):
    w, h = size
    y = np.linspace(0, 1, h)[:, None]
    top = np.array([86, 132, 206])
    bottom = np.array([186, 214, 240])
    rgb = top[None, None, :] * (1 - y[..., None]) + bottom[None, None, :] * y[..., None]
    rgb = np.broadcast_to(rgb, (h, w, 3))
    return Image.fromarray(np.dstack([rgb, np.full((h, w), 255)]).astype(np.uint8), "RGBA")


GLOVE = (44, 46, 50, 255)
CUFF = (30, 31, 34, 255)
SLEEVE = (86, 92, 72, 255)


def arm_quads(hand, shoulder, right):
    """Same placement as preview_viewmodel.arm_quads, dressed in a glove and an olive sleeve."""
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
        return local @ r.T + hand, r @ normal

    def color(face, v):
        if v < 0.2:
            return GLOVE
        if v < 0.26:
            return CUFF
        shade = 1.0 - 0.06 * ((int(v * 15) % 3) == 0)
        return tuple(int(c * shade) for c in SLEEVE[:3]) + (255,)

    return box_quads(frm, to, color, transform)


def first_person(g, ads, skin=None, size=(1920, 1080), attachments=()):
    tex, uvs = textured(g, skin)
    vis = g.visuals

    def render(s):
        quads = model_quads(g.model, tex, uvs, parts=None)
        m = viewmodel_matrix(vis, ads)
        for q in quads:
            q.pts = apply(m, q.pts / 16.0)
            q.normal = m[:3, :3] @ q.normal
            q.normal /= np.linalg.norm(q.normal)
        right = apply(m, np.array([vis["pivot"]]) / 16.0)[0]
        left = apply(m, np.array([vis["left_hand"]]) / 16.0)[0]
        quads += arm_quads(right, RIGHT_SHOULDER, True)
        quads += arm_quads(left, LEFT_SHOULDER, False)
        cam = Camera(s[0], s[1], eye=(0, 0, 0), target=(0, 0, -1), fov=70, near=0.04)
        world = Renderer(s[0], s[1])
        world.draw(range_scene(), cam, shading="lambert", light_dir=(0.35, 0.9, 0.3))
        img = sky(s)
        img.alpha_composite(world.image())
        hand = Renderer(s[0], s[1])
        hand.draw(quads, cam, shading="lambert", light_dir=(0.3, 0.9, 0.4))
        img.alpha_composite(hand.image())
        if not ads:
            d = ImageDraw.Draw(img)
            cx, cy = s[0] // 2, s[1] // 2
            k = s[1] // 360
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                d.line((cx + dx * 6 * k, cy + dy * 6 * k, cx + dx * 14 * k, cy + dy * 14 * k), fill=(255, 255, 255, 230), width=max(1, k))
        return img

    return supersampled(render, size).convert("RGB")


# ------------------------------------------------------------------------------------------------------------------
# Main
# ------------------------------------------------------------------------------------------------------------------

def main():
    os.makedirs(SRC, exist_ok=True)
    os.makedirs(GALLERY, exist_ok=True)
    guns = {fn().name: fn for fn in gun_defs.ALL}
    for name, fn in guns.items():
        g = fn()
        crop_alpha(hero_shot(g), 10).save(os.path.join(SRC, f"hero-{name}.png"))
        tex, uvs = textured(g)
        side_shot(g.model, tex, uvs).save(os.path.join(SRC, f"side-{name}.png"))
        print("rendered", name)
    builds = {
        "arx_16": ([att_defs.tripoint_holo, att_defs.suppressor, att_defs.vertical_grip, att_defs.tactical_stock], None),
        "ghostline_m24": ([att_defs.longeye_scope, att_defs.muzzle_brake, att_defs.heavy_stock], None),
        "viper_9": ([att_defs.holo_reflex, att_defs.flash_hider, att_defs.angled_grip, att_defs.drum_mag], "arctic"),
    }
    for name, (fitted, skin) in builds.items():
        crop_alpha(build_shot(guns[name](), fitted, skin=skin), 10).save(os.path.join(SRC, f"build-{name}.png"))
    for skin in ("desert", "arctic", "crimson", "gilded", "void"):
        crop_alpha(hero_shot(guns["arx_16"](), skin=skin, size=(1200, 675)), 10).save(os.path.join(SRC, f"skin-arx_16-{skin}.png"))
    for fn in att_defs.ALL:
        a = fn()
        if a.slot == "skin":
            continue
        tex, mats, uvs = build_texture(a.model, seed=seed_of(a.name))
        img = supersampled(lambda s: hero(a.model, tex, uvs, size=s, yaw=62, pitch=18, margin=0.62), (600, 600))
        crop_alpha(img).save(os.path.join(SRC, f"att-{a.name}.png"))
    shots = [("arx_16", 0.0, None), ("arx_16", 1.0, None), ("raven_ak", 1.0, None), ("ghostline_m24", 0.0, None),
             ("thunderpump", 0.0, None), ("r9_sidearm", 1.0, None), ("voidcaster", 0.0, None), ("eclipse_railgun", 0.0, None),
             ("spectre_pdw", 0.0, "desert"), ("titan_56", 0.0, None)]
    for name, ads, skin in shots:
        img = first_person(guns[name](), ads, skin)
        tag = f"fp-{name}-{'ads' if ads else 'hip'}" + (f"-{skin}" if skin else "")
        img.save(os.path.join(GALLERY, tag + ".jpg"), quality=92)
        img.resize((1600, 900), Image.LANCZOS).save(os.path.join(SRC, tag + ".jpg"), quality=92)
        print("rendered", tag)


if __name__ == "__main__":
    main()
