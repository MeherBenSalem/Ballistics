"""Voxel model description, per-face texture painting and Blockbench/Java model JSON export.

A model is a list of boxes (optionally rotated about one axis by a multiple of 22.5 degrees, the Java item model
limit). Each box belongs to a *part* (body, action, magazine, stock, sights, muzzle) and has a material.
Every face gets its own texels at DENSITY texels per model pixel, so painted detail (edge wear, stippling, M-LOK
slots, grain, engraving) lines up with the geometry like a hand-made Blockbench texture.
"""
from __future__ import annotations

import math
import random
from dataclasses import dataclass, field

from PIL import Image

FACES = ("north", "south", "east", "west", "up", "down")
DENSITY = 2  # texels per model pixel (32x item resolution)

# name: (rgb, painter)
MATERIALS = {
    "anodized": ((62, 65, 72), "anodized"),
    "steel_dark": ((74, 78, 85), "metal"),
    "steel": ((104, 109, 117), "metal"),
    "steel_light": ((152, 157, 164), "metal"),
    "gunmetal": ((70, 78, 92), "metal"),
    "chrome": ((176, 181, 188), "chrome"),
    "polymer": ((52, 54, 58), "polymer"),
    "polymer_grey": ((86, 89, 94), "polymer"),
    "grip": ((48, 50, 54), "grip"),
    "stipple": ((50, 52, 56), "stipple"),
    "rail": ((60, 63, 70), "anodized"),
    "mlok": ((62, 65, 72), "mlok"),
    "vent": ((70, 73, 79), "vent"),
    "ribs": ((52, 54, 58), "ribs"),
    "tan": ((150, 128, 92), "polymer"),
    "tan_grip": ((146, 124, 88), "grip"),
    "olive": ((80, 86, 62), "polymer"),
    "wood": ((124, 79, 44), "wood"),
    "wood_dark": ((88, 56, 32), "wood"),
    "bakelite": ((122, 58, 28), "polymer"),
    "brass": ((204, 166, 78), "brass"),
    "copper": ((160, 94, 56), "brass"),
    "rubber": ((36, 37, 40), "stipple"),
    "accent": ((226, 116, 38), "polymer"),
    "red": ((186, 46, 42), "polymer"),
    "shell_red": ((170, 40, 36), "polymer"),
    "glass": ((56, 112, 136), "glass"),
    "lens_red": ((150, 40, 48), "glass"),
    "glow_violet": ((182, 92, 255), "glow"),
    "glow_cyan": ((96, 226, 255), "glow"),
    "glow_green": ((120, 255, 140), "glow"),
    "void": ((36, 22, 52), "metal"),
    "void_plate": ((48, 30, 70), "anodized"),
    "white": ((214, 216, 220), "polymer"),
    "dot": ((236, 238, 240), "flat"),
    "recess": ((14, 14, 16), "flat"),
    "glow_red": ((255, 66, 54), "glow"),
    "lens_clear": ((150, 200, 220), "clear"),
    "paper_blue": ((58, 104, 172), "polymer"),
    "concrete": ((118, 120, 124), "polymer"),
    # Weapon-skin kit colours (icons only).
    "kit_desert": ((172, 146, 102), "polymer"),
    "kit_arctic": ((210, 214, 218), "polymer"),
    "kit_crimson": ((150, 36, 40), "polymer"),
    "kit_gilded": ((210, 170, 76), "brass"),
    "kit_void": ((58, 34, 86), "anodized"),
}

# Materials recoloured by skins (glass, brass, glow, dots and recesses keep their colour).
SKINNABLE = {"anodized", "steel_dark", "steel", "steel_light", "gunmetal", "polymer", "polymer_grey", "grip", "stipple",
             "rail", "mlok", "vent", "ribs", "tan", "tan_grip", "olive", "wood", "wood_dark", "rubber", "bakelite"}

SKINS = {
    # skin: (furniture rgb, metal rgb, pattern)
    "desert": ((172, 146, 102), (124, 108, 82), "camo"),
    "arctic": ((210, 214, 218), (142, 148, 154), "camo"),
    "crimson": ((136, 32, 36), (70, 44, 46), "plain"),
    "gilded": ((34, 30, 27), (210, 170, 76), "plain"),
    "void": ((30, 18, 46), (58, 34, 86), "veins"),
}


@dataclass
class Box:
    part: str
    name: str
    frm: tuple
    to: tuple
    mat: str
    rot: tuple | None = None  # (axis, angle, origin)
    hidden: tuple = ()  # faces to omit (fully covered faces)
    decal: str | None = None  # "stamp" or "crest" painted on the east/west faces

    def size(self):
        return tuple(self.to[i] - self.frm[i] for i in range(3))


@dataclass
class VoxelModel:
    name: str
    boxes: list = field(default_factory=list)

    def box(self, part, name, frm, to, mat, rot=None, hidden=(), decal=None):
        frm = tuple(round(float(v), 4) for v in frm)
        to = tuple(round(float(v), 4) for v in to)
        if mat not in MATERIALS:
            raise ValueError(f"{self.name}/{name}: unknown material {mat}")
        for i in range(3):
            if to[i] < frm[i]:
                raise ValueError(f"{self.name}/{name}: inverted box on axis {i}")
            if frm[i] < -16 or to[i] > 32:
                raise ValueError(f"{self.name}/{name}: outside the -16..32 Java model range")
        if rot is not None:
            axis, angle, origin = rot
            if angle not in (-45, -22.5, 0, 22.5, 45):
                raise ValueError(f"{self.name}/{name}: rotation {angle} is not allowed in Java models")
        self.boxes.append(Box(part, name, frm, to, mat, rot, tuple(hidden), decal))
        return self

    def parts(self):
        out = []
        for b in self.boxes:
            if b.part not in out:
                out.append(b.part)
        return out

    def bounds(self):
        lo = [min(b.frm[i] for b in self.boxes) for i in range(3)]
        hi = [max(b.to[i] for b in self.boxes) for i in range(3)]
        return lo, hi


def face_dims(box: Box, face: str):
    sx, sy, sz = box.size()
    if face in ("north", "south"):
        return sx, sy
    if face in ("east", "west"):
        return sz, sy
    return sx, sz


# ----------------------------------------------------------------------------------------------------------------
# Texture painting
# ----------------------------------------------------------------------------------------------------------------

def _clamp(v):
    return max(0, min(255, int(round(v))))


def _shade(rgb, d):
    return tuple(_clamp(c + d) for c in rgb)


def _mul(rgb, f):
    return tuple(_clamp(c * f) for c in rgb)


GLYPHS = [
    ["111", "101", "110", "101", "101"],  # R
    ["110", "101", "110", "101", "110"],  # B
    ["010", "010", "010", "010", "010"],  # 1
    ["111", "001", "010", "100", "111"],  # 2
    ["101", "101", "111", "001", "001"],  # 4
]


def paint_face(img, mats, x0, y0, w, h, box: Box, face, rng):
    """Paints one face rectangle (w x h texels). `mats` records the material per texel for skin recolouring."""
    rgb, painter = MATERIALS[box.mat]
    d = DENSITY
    long_u = w >= h
    big = w >= 3 * d and h >= 2 * d
    for j in range(h):
        for i in range(w):
            c = rgb
            n = rng.uniform(-1, 1)
            on_left, on_right = i == 0, i == w - 1
            on_top, on_bottom = j == 0, j == h - 1
            edge = on_left or on_right or on_top or on_bottom
            fv = j / max(1, h - 1)
            if painter in ("metal", "anodized", "chrome"):
                c = _shade(c, n * (1.6 if painter != "metal" else 2.4))
                if face in ("north", "south", "east", "west"):
                    c = _shade(c, (0.5 - fv) * (10 if painter != "chrome" else 46))
                if face == "up":
                    c = _shade(c, 12)
                elif face == "down":
                    c = _shade(c, -12)
                if big:
                    # Crisp machined edges: lit top line, soft side lines, shadowed bottom line.
                    if on_top:
                        c = _shade(c, 24)
                    elif on_bottom:
                        c = _shade(c, -16)
                    elif on_left or on_right:
                        c = _shade(c, 9)
            elif painter == "polymer":
                c = _shade(c, n * 1.5)
                if big:
                    if on_top:
                        c = _shade(c, 12)
                    elif on_bottom:
                        c = _shade(c, -12)
                    elif on_left or on_right:
                        c = _shade(c, -4)
            elif painter == "stipple":
                c = _shade(c, rng.uniform(-4, 4))
                if big and (on_top or on_bottom):
                    c = _shade(c, 8 if on_top else -10)
            elif painter == "grip":
                c = _shade(c, (6 if (i + j) % 2 == 0 else -2) + n)
                if big:
                    if on_top:
                        c = _shade(c, 10)
                    elif on_bottom:
                        c = _shade(c, -12)
            elif painter == "ribs":
                k = j if long_u else i
                c = _shade(c, (10 if (k // d) % 3 == 0 else -2) + n * 2)
            elif painter in ("mlok", "vent"):
                k = i if long_u else j
                m = j if long_u else i
                span = h if long_u else w
                period = 5 * d if painter == "mlok" else 3 * d
                slot_len = int(3 * d) if painter == "mlok" else int(1.5 * d)
                in_slot = (k % period) < slot_len and (k % period) > 0 and span >= 3 * d and d <= m < span - d
                c = _shade(c, n * 2.5)
                if in_slot:
                    c = _shade(c, -44)
                    if m == span - d - 1:
                        c = _shade(c, 14)
                elif big and edge:
                    c = _shade(c, 18 if on_top else -10)
            elif painter == "wood":
                k = j if long_u else i
                grain = math.sin(k * 0.9 / d * 1.9 + math.sin((i if long_u else j) * 0.07) * 2.0) * 9
                c = _shade(c, grain + n * 4)
                if big and edge:
                    c = _shade(c, -10)
            elif painter == "brass":
                c = _shade(c, (0.5 - fv) * 34 + n * 3)
                if j == max(0, h // 4):
                    c = _shade(c, 26)
            elif painter == "glass":
                c = _shade(c, (0.5 - fv) * 40)
                if abs(i - j) <= max(0, d // 2) and i < w * 0.6:
                    c = _mul(c, 1.6)
            elif painter == "glow":
                center = abs((j if long_u else i) - ((h if long_u else w) - 1) / 2) / max(1, (h if long_u else w) / 2)
                c = _shade(c, 50 - center * 60)
            elif painter == "flat":
                c = _shade(c, n * 2)
            alpha = 255
            if painter == "clear":
                # See-through lens: only a thin tinted rim and a short glint streak are opaque (cutout rendering).
                glint = j == i + 1 and i < max(2, w // 4) and w >= 4 * d
                alpha = 255 if (edge and w > 2 and h > 2) or glint else 0
                c = _shade(c, 30) if glint else c
            img.putpixel((x0 + i, y0 + j), c + (alpha,))
            mats[(x0 + i, y0 + j)] = box.mat
    if box.decal and face in ("east", "west") and w >= 12 and h >= 7:
        _decal(img, x0, y0, w, h, box.decal, rng)


def _decal(img, x0, y0, w, h, kind, rng):
    if kind == "stamp":
        # Engraved roll-mark: two short rows of dashes, slightly lighter than the surface.
        rows = (y0 + h // 2 - 2, y0 + h // 2 + 1)
        for row_index, py in enumerate(rows):
            px = x0 + max(2, w // 6)
            end = x0 + w - max(2, w // 6) - (w // 5 if row_index else 0)
            while px < end:
                length = rng.choice((1, 2, 2, 3))
                for k in range(length):
                    if px + k < end:
                        r, gg, b, a = img.getpixel((px + k, py))
                        img.putpixel((px + k, py), (_clamp(r + 22), _clamp(gg + 22), _clamp(b + 24), a))
                px += length + 1
    elif kind == "crest":
        cx, cy = x0 + w // 2, y0 + h // 2
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                if abs(dx) + abs(dy) == 3 or (dx == 0 and abs(dy) <= 1):
                    r, g, b, a = img.getpixel((cx + dx, cy + dy))
                    img.putpixel((cx + dx, cy + dy), (_clamp(r + 60), _clamp(g + 48), _clamp(b + 20), a))


class Packer:
    """Shelf packer for face rectangles."""

    def __init__(self, width):
        self.width = width
        self.x = 0
        self.y = 0
        self.shelf = 0

    def add(self, w, h):
        if self.x + w > self.width:
            self.x = 0
            self.y += self.shelf
            self.shelf = 0
        r = (self.x, self.y, w, h)
        self.x += w
        self.shelf = max(self.shelf, h)
        return r

    def height(self):
        return self.y + self.shelf


def texel_size(v):
    return max(1, int(math.ceil(v * DENSITY - 1e-6)))


def build_texture(model: VoxelModel, seed: int):
    """Allocates and paints texels for every face. Returns (image, materials, uv map {(box_index, face): rect})."""
    faces = []
    for bi, b in enumerate(model.boxes):
        for f in FACES:
            if f in b.hidden:
                continue
            w, h = face_dims(b, f)
            if w <= 0 or h <= 0:
                continue
            faces.append((bi, f, texel_size(w), texel_size(h)))
    area = sum(w * h for _, _, w, h in faces)
    width = 32
    while width * width < area * 1.35 or max(w for _, _, w, _ in faces) > width:
        width *= 2
    order = sorted(faces, key=lambda t: (-t[3], -t[2]))
    while True:
        packer = Packer(width)
        uvs = {}
        for bi, f, w, h in order:
            uvs[(bi, f)] = packer.add(w, h)
        if packer.height() <= width:
            break
        width *= 2
    img = Image.new("RGBA", (width, width), (0, 0, 0, 0))
    mats = {}
    rng = random.Random(seed)
    for (bi, f), (x, y, w, h) in uvs.items():
        paint_face(img, mats, x, y, w, h, model.boxes[bi], f, rng)
    return img, mats, uvs


def skin_texture(img: Image.Image, mats: dict, skin: str, seed: int):
    furniture, metal, pattern = SKINS[skin]
    out = img.copy()
    rng = random.Random(seed)
    blobs = [(rng.randrange(img.width), rng.randrange(img.height), rng.uniform(3, 8) * DENSITY / 2)
             for _ in range(img.width * img.height // 60)]
    for (x, y), mat in mats.items():
        if mat not in SKINNABLE:
            continue
        r, g, b, a = img.getpixel((x, y))
        base_rgb, painter = MATERIALS[mat]
        lum = (r + g + b) / max(1, sum(base_rgb))
        target = metal if painter in ("metal", "anodized", "chrome", "mlok", "vent") else furniture
        c = tuple(_clamp(t * lum) for t in target)
        if pattern == "camo":
            for bx, by, br in blobs:
                if (x - bx) ** 2 + (y - by) ** 2 < br * br:
                    c = _shade(c, -26 if (bx + by) % 2 else 18)
                    break
        elif pattern == "veins" and (x * 7 + y * 3) % 13 == 0:
            c = (156, 76, 236)
        out.putpixel((x, y), c + (a,))
    return out


# ----------------------------------------------------------------------------------------------------------------
# Java item model export (also opens in Blockbench as a "Java Block/Item" model)
# ----------------------------------------------------------------------------------------------------------------

def element_json(box: Box, uvs, bi, tex_size, texture_key="0"):
    faces = {}
    for f in FACES:
        if (bi, f) not in uvs:
            continue
        x, y, w, h = uvs[(bi, f)]
        s = 16.0 / tex_size
        uv = [round(x * s, 4), round(y * s, 4), round((x + w) * s, 4), round((y + h) * s, 4)]
        faces[f] = {"uv": uv, "texture": "#" + texture_key}
    el = {"name": box.name, "from": list(box.frm), "to": list(box.to), "faces": faces}
    if box.rot is not None:
        axis, angle, origin = box.rot
        el["rotation"] = {"angle": angle, "axis": axis, "origin": [round(v, 4) for v in origin]}
    return el


def part_model_json(model: VoxelModel, part: str, uvs, tex_size, texture: str):
    elements = [element_json(b, uvs, i, tex_size) for i, b in enumerate(model.boxes) if b.part == part]
    return {
        "credit": "Remnants: Ballistics (NAIZO / NightBeam) - generated by tools/assets, Blockbench compatible",
        "texture_size": [tex_size, tex_size],
        "textures": {"0": texture, "particle": texture},
        "elements": elements,
    }


def full_model_json(model: VoxelModel, uvs, tex_size, texture: str):
    """Single Blockbench project-style file with every element, grouped by part in the outliner."""
    elements = [element_json(b, uvs, i, tex_size) for i, b in enumerate(model.boxes)]
    outliner = []
    for part in model.parts():
        children = [i for i, b in enumerate(model.boxes) if b.part == part]
        outliner.append({"name": part, "origin": [8, 8, 8], "color": 0, "children": children})
    return {
        "credit": "Remnants: Ballistics (NAIZO / NightBeam) - generated by tools/assets, Blockbench compatible",
        "texture_size": [tex_size, tex_size],
        "textures": {"0": texture, "particle": texture},
        "elements": elements,
        "groups": outliner,
    }
