"""2D art: container GUIs, HUD sprites (scope, reticles), muzzle flashes, bullet holes, particles, the void orb and the
mod-list logo. Everything is drawn procedurally with Pillow + numpy; nothing is copied from Minecraft or other mods.
"""
from __future__ import annotations

import math
import os
import random

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

FONT_DIR = "/usr/share/fonts/opentype/inter"

# Container palette (light panels keep the vanilla 0x404040 label text readable).
PANEL = (198, 200, 204)
PANEL_HI = (250, 251, 252)
PANEL_LO = (92, 96, 104)
EDGE = (24, 26, 30)
SLOT = (139, 141, 146)
SLOT_HI = (255, 255, 255)
SLOT_LO = (55, 57, 62)
ACCENT = (226, 116, 38)
STEEL = (64, 72, 86)


def _save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)


# ------------------------------------------------------------------------------------------------------------------
# Containers
# ------------------------------------------------------------------------------------------------------------------

def _panel(d, x0, y0, x1, y1):
    """Rounded, bevelled panel (x1, y1 exclusive)."""
    d.rectangle((x0 + 2, y0, x1 - 3, y0), fill=EDGE)
    d.rectangle((x0 + 2, y1 - 1, x1 - 3, y1 - 1), fill=EDGE)
    d.rectangle((x0, y0 + 2, x0, y1 - 3), fill=EDGE)
    d.rectangle((x1 - 1, y0 + 2, x1 - 1, y1 - 3), fill=EDGE)
    d.point([(x0 + 1, y0 + 1), (x1 - 2, y0 + 1), (x0 + 1, y1 - 2), (x1 - 2, y1 - 2)], fill=EDGE)
    d.rectangle((x0 + 1, y0 + 2, x1 - 2, y1 - 3), fill=PANEL)
    d.rectangle((x0 + 2, y0 + 1, x1 - 3, y1 - 2), fill=PANEL)
    d.rectangle((x0 + 2, y0 + 1, x1 - 4, y0 + 2), fill=PANEL_HI)
    d.rectangle((x0 + 1, y0 + 2, x0 + 2, y1 - 4), fill=PANEL_HI)
    d.rectangle((x0 + 3, y1 - 3, x1 - 3, y1 - 2), fill=PANEL_LO)
    d.rectangle((x1 - 3, y0 + 3, x1 - 2, y1 - 3), fill=PANEL_LO)


def _slot(d, x, y, size=18):
    d.rectangle((x, y, x + size - 1, y + size - 1), fill=SLOT)
    d.line((x, y, x + size - 2, y), fill=SLOT_LO)
    d.line((x, y, x, y + size - 2), fill=SLOT_LO)
    d.line((x + 1, y + size - 1, x + size - 1, y + size - 1), fill=SLOT_HI)
    d.line((x + size - 1, y + 1, x + size - 1, y + size - 1), fill=SLOT_HI)


def _big_slot(d, x, y):
    """26x26 result slot (item at x+5, y+5)."""
    d.rectangle((x, y, x + 25, y + 25), fill=SLOT)
    d.rectangle((x, y, x + 24, y + 1), fill=SLOT_LO)
    d.rectangle((x, y, x + 1, y + 24), fill=SLOT_LO)
    d.rectangle((x + 1, y + 24, x + 25, y + 25), fill=SLOT_HI)
    d.rectangle((x + 24, y + 1, x + 25, y + 25), fill=SLOT_HI)


def _inventory(d):
    for row in range(3):
        for col in range(9):
            _slot(d, 7 + col * 18, 83 + row * 18)
    for col in range(9):
        _slot(d, 7 + col * 18, 141)


def _icon(d, kind, x, y, color=(108, 110, 116)):
    """Faint slot glyphs for the attachment slots (drawn inside a 16x16 slot interior at x, y)."""
    c = color
    if kind == "optic":
        d.rectangle((x + 3, y + 6, x + 12, y + 9), fill=c)
        d.rectangle((x + 1, y + 5, x + 3, y + 10), fill=c)
        d.rectangle((x + 12, y + 5, x + 14, y + 10), fill=c)
        d.rectangle((x + 6, y + 10, x + 9, y + 12), fill=c)
    elif kind == "muzzle":
        d.rectangle((x + 2, y + 6, x + 13, y + 9), fill=c)
        for i in range(3):
            d.rectangle((x + 4 + i * 3, y + 6, x + 4 + i * 3, y + 9), fill=SLOT)
    elif kind == "underbarrel":
        d.rectangle((x + 3, y + 3, x + 12, y + 5), fill=c)
        d.rectangle((x + 6, y + 5, x + 9, y + 13), fill=c)
    elif kind == "magazine":
        d.polygon([(x + 5, y + 2), (x + 10, y + 2), (x + 12, y + 13), (x + 7, y + 13)], fill=c)
    elif kind == "stock":
        d.polygon([(x + 1, y + 5), (x + 9, y + 5), (x + 14, y + 3), (x + 14, y + 12), (x + 9, y + 9), (x + 1, y + 9)], fill=c)
    elif kind == "skin":
        d.rectangle((x + 5, y + 4, x + 10, y + 13), fill=c)
        d.rectangle((x + 6, y + 2, x + 9, y + 4), fill=c)
        d.rectangle((x + 10, y + 3, x + 12, y + 4), fill=c)
    elif kind == "repair":
        d.line((x + 3, y + 12, x + 11, y + 4), fill=c, width=2)
        d.rectangle((x + 10, y + 2, x + 13, y + 5), fill=c)


def _grid(d, x0, y0, x1, y1, step=6, color=(184, 187, 192)):
    for x in range(x0, x1 + 1, step):
        d.line((x, y0, x, y1), fill=color)
    for y in range(y0, y1 + 1, step):
        d.line((x0, y, x1, y), fill=color)


def gunsmith_gui():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    _panel(d, 0, 0, 176, 166)
    # Schematic bench area with a blueprint grid.
    d.rectangle((26, 14, 152, 80), fill=(186, 190, 196))
    _grid(d, 27, 15, 151, 79)
    d.rectangle((26, 14, 152, 14), fill=PANEL_LO)
    d.rectangle((26, 14, 26, 80), fill=PANEL_LO)
    d.rectangle((26, 80, 152, 80), fill=PANEL_HI)
    d.rectangle((152, 14, 152, 80), fill=PANEL_HI)
    # Connector traces from the weapon to each attachment slot.
    trace = (150, 154, 160)
    cx, cy = 88, 48
    for tx, ty in ((88, 26), (124, 48), (106, 70), (70, 70), (52, 48)):
        d.line((cx, cy, tx, cy), fill=trace)
        d.line((tx, cy, tx, ty), fill=trace)
    # Weapon slot: large framed socket with an accent underline.
    d.rectangle((75, 35, 100, 60), fill=STEEL)
    _slot(d, 79, 39)
    d.rectangle((75, 61, 100, 61), fill=ACCENT)
    from_slots = [("optic", 80, 18), ("muzzle", 116, 40), ("underbarrel", 98, 62), ("magazine", 62, 62), ("stock", 44, 40), ("skin", 8, 62)]
    for kind, x, y in from_slots:
        _slot(d, x - 1, y - 1)
        _icon(d, kind, x, y)
    _slot(d, 151, 61)
    _icon(d, "repair", 152, 62)
    _inventory(d)
    return img


def stat_panel():
    w, h = 128, 166
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, w - 1, h - 1), radius=3, fill=(22, 24, 28, 245), outline=(10, 11, 13, 255))
    d.rounded_rectangle((1, 1, w - 2, h - 2), radius=3, outline=(58, 62, 70, 255))
    # Preview well with a radial glow and a subtle grid.
    well = Image.new("RGBA", (w - 8, 48), (0, 0, 0, 0))
    wd = ImageDraw.Draw(well)
    for r in range(40, 0, -1):
        a = int(70 * (1 - r / 40) ** 1.5)
        wd.ellipse((60 - r * 1.6, 24 - r * 0.8, 60 + r * 1.6, 24 + r * 0.8), fill=(60, 120, 180, a))
    img.alpha_composite(well, (4, 5))
    d.rectangle((4, 4, w - 5, 53), outline=(48, 52, 60, 255))
    for x in range(8, w - 6, 8):
        d.point((x, 52), fill=(70, 76, 86, 255))
    d.rectangle((4, 54, w - 5, 54), fill=ACCENT + (255,))
    return img


def ammo_press_gui():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    _panel(d, 0, 0, 176, 166)
    d.rectangle((22, 12, 50, 74), fill=(186, 190, 196))
    _grid(d, 23, 13, 49, 73)
    for y in (17, 35, 53):
        _slot(d, 29, y - 1)
    _big_slot(d, 119, 30)
    # Empty arrow (background) and the filled arrow sprite at (176, 0).
    def arrow(dd, ox, oy, fill, edge):
        pts = [(ox, oy + 5), (ox + 14, oy + 5), (ox + 14, oy), (ox + 23, oy + 8), (ox + 14, oy + 16), (ox + 14, oy + 11), (ox, oy + 11)]
        dd.polygon(pts, fill=fill, outline=edge)
    arrow(d, 72, 34, (150, 152, 158), (110, 112, 118))
    arrow(d, 176, 0, ACCENT, (150, 70, 20))
    d.line((177, 8, 189, 8), fill=(255, 190, 120))
    # Press column illustration between inputs and output.
    d.rectangle((60, 16, 66, 30), fill=(150, 152, 158))
    d.rectangle((56, 14, 70, 17), fill=(120, 122, 128))
    _inventory(d)
    return img


# ------------------------------------------------------------------------------------------------------------------
# HUD sprites
# ------------------------------------------------------------------------------------------------------------------

def scope_overlay(size=512):
    """Black mask with a round eyepiece, soft vignette, mil-dot crosshair and a centre aiming point."""
    yy, xx = np.mgrid[0:size, 0:size]
    c = (size - 1) / 2
    r = np.sqrt((xx - c) ** 2 + (yy - c) ** 2) / (size / 2)
    alpha = np.clip((r - 0.86) / 0.1, 0, 1)  # vignette into the black ring
    alpha = np.where(r > 0.96, 1.0, alpha)
    img = np.zeros((size, size, 4), dtype=np.float32)
    img[..., 3] = alpha * 255
    # Lens tint at the rim.
    rim = np.clip((r - 0.7) / 0.26, 0, 1) * (r <= 0.96)
    img[..., 2] += rim * 20
    img[..., 3] = np.maximum(img[..., 3], rim * 40)
    out = Image.fromarray(img.astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(out)
    k = size / 512
    thick = max(1, int(3 * k))
    thin = max(1, int(1 * k))
    gap = int(10 * k)
    # Thick outer posts, thin inner crosshair.
    d.rectangle((0, c - thick, c - size * 0.22, c + thick), fill=(0, 0, 0, 255))
    d.rectangle((c + size * 0.22, c - thick, size, c + thick), fill=(0, 0, 0, 255))
    d.rectangle((c - thick, c + size * 0.22, c + thick, size), fill=(0, 0, 0, 255))
    d.rectangle((c - thin, 0, c + thin, c - size * 0.22), fill=(0, 0, 0, 255))
    d.line((c - size * 0.22, c, c - gap, c), fill=(0, 0, 0, 255), width=thin * 2)
    d.line((c + gap, c, c + size * 0.22, c), fill=(0, 0, 0, 255), width=thin * 2)
    d.line((c, c - size * 0.22, c, c - gap), fill=(0, 0, 0, 255), width=thin * 2)
    d.line((c, c + gap, c, c + size * 0.22), fill=(0, 0, 0, 255), width=thin * 2)
    # Mil dots.
    for i in range(1, 5):
        o = i * size * 0.045
        for dx, dy in ((o, 0), (-o, 0), (0, o), (0, -o)):
            rr = max(1.5, 2.5 * k)
            d.ellipse((c + dx - rr, c + dy - rr, c + dx + rr, c + dy + rr), fill=(0, 0, 0, 255))
    # Illuminated centre dot.
    rr = 2.5 * k
    d.ellipse((c - rr, c - rr, c + rr, c + rr), fill=(255, 60, 50, 255))
    return out


def reflex_dot():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r <= 1.2:
                img.putpixel((x, y), (255, 236, 230, 255))
            elif r <= 2.2:
                img.putpixel((x, y), (255, 64, 52, 255))
            elif r <= 3.6:
                img.putpixel((x, y), (255, 40, 30, int(120 * (3.6 - r) / 1.4)))
    return img


def holo_reticle():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r <= 0.8:
                img.putpixel((x, y), (255, 236, 230, 255))
            elif 5.6 <= r <= 6.8:
                img.putpixel((x, y), (255, 64, 52, 230))
            elif 6.8 < r <= 7.6:
                img.putpixel((x, y), (255, 40, 30, 70))
    for x in (7, 8):
        img.putpixel((x, 1), (255, 64, 52, 255))
        img.putpixel((x, 14), (255, 64, 52, 255))
    return img


# ------------------------------------------------------------------------------------------------------------------
# FX
# ------------------------------------------------------------------------------------------------------------------

def _radial_flash(size, rng, petals, core, mid, outer, stretch=1.0):
    """Additive flash on black: bright core, petal rays with jitter."""
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    c = (size - 1) / 2
    dx, dy = (xx - c) / (size / 2), (yy - c) / (size / 2)
    r = np.sqrt(dx ** 2 + dy ** 2)
    ang = np.arctan2(dy, dx)
    rays = np.zeros_like(r)
    for i in range(petals):
        a0 = rng.uniform(0, 2 * np.pi)
        width = rng.uniform(0.12, 0.3)
        length = rng.uniform(0.55, 1.0) * stretch
        da = np.angle(np.exp(1j * (ang - a0)))
        rays = np.maximum(rays, np.exp(-(da / width) ** 2) * np.clip(1 - r / length, 0, 1))
    body = np.clip(1 - r / 0.45, 0, 1) ** 1.5
    inten = np.clip(body * 1.2 + rays * 0.9, 0, 1)
    core_mask = np.clip(1 - r / 0.18, 0, 1)
    col = (np.array(outer)[None, None, :] * inten[..., None] + (np.array(mid) - np.array(outer))[None, None, :] * (inten ** 2)[..., None]
           + (np.array(core) - np.array(mid))[None, None, :] * core_mask[..., None])
    rgb = np.clip(col, 0, 255)
    return rgb


def muzzle_flash(energy=False, frame=64, seed=7):
    rng = np.random.default_rng(seed)
    sheet = np.zeros((frame, frame * 4, 3), dtype=np.float32)
    for f in range(4):
        if energy:
            rgb = _radial_flash(frame, rng, 5, (255, 255, 255), (150, 230, 255), (150, 70, 255), stretch=0.9 - f * 0.12)
        else:
            rgb = _radial_flash(frame, rng, 7 - f, (255, 252, 230), (255, 196, 90), (230, 90, 20), stretch=1.0 - f * 0.18)
        rgb *= (1.0 - f * 0.18)
        sheet[:, f * frame:(f + 1) * frame] = rgb
    img = Image.fromarray(sheet.astype(np.uint8), "RGB").convert("RGBA")
    return img


def bullet_hole(size=32, seed=3):
    rng = random.Random(seed)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    c = size / 2 - 0.5
    # Soot halo.
    for rr in range(13, 4, -1):
        a = int(90 * (1 - (rr - 4) / 9) ** 1.4)
        d.ellipse((c - rr, c - rr, c + rr, c + rr), fill=(20, 18, 16, a))
    # Cracks.
    for i in range(7):
        ang = rng.uniform(0, 2 * math.pi)
        length = rng.uniform(5, 11)
        x, y = c, c
        for s in range(int(length)):
            ang += rng.uniform(-0.4, 0.4)
            nx, ny = x + math.cos(ang), y + math.sin(ang)
            d.line((x, y, nx, ny), fill=(30, 28, 26, 200))
            x, y = nx, ny
    d.ellipse((c - 3.5, c - 3.5, c + 3.5, c + 3.5), fill=(70, 66, 62, 255))
    d.ellipse((c - 2.5, c - 2.5, c + 2.5, c + 2.5), fill=(8, 8, 8, 255))
    return img


def void_orb(size=32):
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    c = (size - 1) / 2
    r = np.sqrt((xx - c) ** 2 + (yy - c) ** 2) / (size / 2)
    glow = np.clip(1 - r, 0, 1) ** 1.6
    ring = np.exp(-((r - 0.42) / 0.08) ** 2)
    core = np.clip(1 - r / 0.22, 0, 1)
    rgb = np.zeros((size, size, 3), dtype=np.float32)
    rgb += glow[..., None] * np.array([120, 50, 220])
    rgb += ring[..., None] * np.array([200, 120, 255])
    rgb *= (1 - core[..., None] * 0.95)  # dark singularity centre
    rgb += np.exp(-((r - 0.24) / 0.04) ** 2)[..., None] * np.array([255, 220, 255])
    return Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


# ------------------------------------------------------------------------------------------------------------------
# Particles
# ------------------------------------------------------------------------------------------------------------------

def particles():
    """Returns {name: [Image, ...]} for every particle sprite set."""
    rng = random.Random(11)
    out = {}
    sparks = []
    for i in range(4):
        img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        if i % 2 == 0:
            d.line((1, 6 - i // 2, 6, 1 + i // 2), fill=(255, 190, 80, 255), width=1)
            d.point((3 + i // 2, 4), fill=(255, 250, 220, 255))
        else:
            d.rectangle((3, 3, 4, 4), fill=(255, 240, 200, 255))
            d.point([(2, 3), (5, 4), (3, 2), (4, 5)], fill=(255, 160, 60, 255))
        sparks.append(img)
    out["spark"] = sparks

    def puff(n, base, rough):
        frames = []
        blobs = [(rng.uniform(-0.3, 0.3), rng.uniform(-0.3, 0.3), rng.uniform(0.25, 0.45)) for _ in range(5)]
        for f in range(n):
            size = 16
            yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
            dens = np.zeros((size, size), dtype=np.float32)
            grow = 0.6 + 0.4 * f / (n - 1)
            for bx, by, br in blobs:
                cx, cy = 7.5 + bx * 8 * grow, 7.5 + by * 8 * grow
                dens += np.exp(-(((xx - cx) ** 2 + (yy - cy) ** 2) / (br * 8 * grow) ** 2) * 2.2)
            noise = np.array([[rng.uniform(1 - rough, 1) for _ in range(size)] for _ in range(size)], dtype=np.float32)
            a = np.clip(dens * noise * (1 - 0.55 * f / (n - 1)), 0, 1)
            rgb = np.ones((size, size, 3), dtype=np.float32) * np.array(base) * (0.85 + 0.15 * noise[..., None])
            arr = np.dstack([rgb, a * 255]).astype(np.uint8)
            frames.append(Image.fromarray(arr, "RGBA"))
        return frames

    out["smoke"] = puff(8, (236, 236, 236), 0.25)
    out["dust"] = puff(8, (214, 204, 186), 0.6)

    casings = []
    for i in range(4):
        img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        if i == 0:
            d.rectangle((2, 3, 5, 4), fill=(214, 172, 80, 255)); d.point((6, 3), fill=(150, 110, 40, 255)); d.point((2, 3), fill=(240, 210, 130, 255))
        elif i == 1:
            d.rectangle((3, 2, 4, 5), fill=(214, 172, 80, 255)); d.point((3, 6), fill=(150, 110, 40, 255)); d.point((3, 2), fill=(240, 210, 130, 255))
        elif i == 2:
            d.line((2, 5, 5, 2), fill=(214, 172, 80, 255), width=2); d.point((5, 2), fill=(150, 110, 40, 255))
        else:
            d.line((2, 2, 5, 5), fill=(214, 172, 80, 255), width=2); d.point((2, 2), fill=(240, 210, 130, 255))
        casings.append(img)
    out["casing"] = casings
    shells = []
    for i in range(4):
        img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        if i % 2 == 0:
            d.rectangle((1, 2, 5, 5), fill=(176, 40, 36, 255)); d.rectangle((5, 2, 6, 5), fill=(210, 170, 80, 255))
        else:
            d.rectangle((2, 1, 5, 5), fill=(176, 40, 36, 255)); d.rectangle((2, 5, 5, 6), fill=(210, 170, 80, 255))
        shells.append(img.rotate(90 * (i // 2)))
    out["shotgun_shell"] = shells

    def motes(color):
        frames = []
        for i in range(4):
            img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
            for y in range(8):
                for x in range(8):
                    r = math.hypot(x - 3.5, y - 3.5)
                    lim = 3.6 - i * 0.5
                    if r < lim:
                        k = 1 - r / lim
                        col = tuple(int(c + (255 - c) * k ** 3) for c in color)
                        img.putpixel((x, y), col + (int(255 * min(1, k * 1.6)),))
            frames.append(img)
        return frames

    out["void_spark"] = motes((170, 80, 255))
    out["rail_spark"] = motes((80, 220, 255))
    return out


# ------------------------------------------------------------------------------------------------------------------
# Logo
# ------------------------------------------------------------------------------------------------------------------

def logo(hero_img, size=(512, 256)):
    """Mod-list logo: weapon hero render over a dark gradient with the wordmark."""
    w, h = size
    bg = Image.new("RGBA", size, (0, 0, 0, 255))
    px = bg.load()
    for y in range(h):
        for x in range(w):
            t = y / h
            g = math.exp(-(((x - w * 0.65) / (w * 0.45)) ** 2 + ((y - h * 0.45) / (h * 0.6)) ** 2))
            px[x, y] = (int(16 + 20 * g), int(18 + 26 * g), int(22 + 34 * g + 6 * t), 255)
    gun = hero_img.crop(hero_img.getbbox())
    gun.thumbnail((int(w * 0.95), int(h * 0.8)), Image.LANCZOS)
    bg.alpha_composite(gun, (w - gun.width - 6, int(h * 0.05)))
    d = ImageDraw.Draw(bg)
    title = ImageFont.truetype(os.path.join(FONT_DIR, "InterDisplay-Black.otf"), 46)
    sub = ImageFont.truetype(os.path.join(FONT_DIR, "InterDisplay-Bold.otf"), 30)
    d.text((22, h - 92), "REMNANTS", font=title, fill=(242, 244, 247, 255))
    d.text((24, h - 44), "BALLISTICS", font=sub, fill=ACCENT + (255,))
    d.rectangle((22, h - 98, 92, h - 95), fill=ACCENT + (255,))
    return bg


def write_all(assets_dir, ns):
    """Writes every 2D texture. Returns the particle sprite names per particle type."""
    tex = os.path.join(assets_dir, "textures")
    _save(gunsmith_gui(), os.path.join(tex, "gui/container/gunsmith.png"))
    _save(stat_panel(), os.path.join(tex, "gui/container/stat_panel.png"))
    _save(ammo_press_gui(), os.path.join(tex, "gui/container/ammo_press.png"))
    _save(scope_overlay(), os.path.join(tex, "gui/sprites/hud/scope.png"))
    _save(reflex_dot(), os.path.join(tex, "gui/sprites/hud/reflex_dot.png"))
    _save(holo_reticle(), os.path.join(tex, "gui/sprites/hud/holo_reticle.png"))
    _save(muzzle_flash(), os.path.join(tex, "fx/muzzle_flash.png"))
    _save(muzzle_flash(energy=True, seed=9), os.path.join(tex, "fx/energy_flash.png"))
    _save(bullet_hole(), os.path.join(tex, "fx/bullet_hole.png"))
    _save(void_orb(), os.path.join(tex, "entity/void_orb.png"))
    sets = {}
    for name, frames in particles().items():
        names = []
        for i, im in enumerate(frames):
            _save(im, os.path.join(tex, "particle", f"{name}_{i}.png"))
            names.append(f"{ns}:{name}_{i}")
        sets[name] = names
    return sets
