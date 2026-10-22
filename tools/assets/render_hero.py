"""Large three-quarter "hero" renders of weapons and attachments (quality review and marketing artwork).

    python3 tools/assets/render_hero.py out_dir [weapon ...]
"""
from __future__ import annotations

import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(__file__))

from raster import Camera, Renderer, model_quads  # noqa: E402
from voxel import build_texture  # noqa: E402


def hero(model, texture, uvs, size=(1600, 900), yaw=118.0, pitch=16.0, distance=None, fov=26.0, background=(0, 0, 0, 0),
         light=(0.55, 0.8, -0.25), fill=1.0, margin=0.62):
    lo, hi = model.bounds()
    center = np.array([(lo[i] + hi[i]) / 2 for i in range(3)])
    extent = max(hi[i] - lo[i] for i in range(3))
    if distance is None:
        radius = 0.5 * math.sqrt(sum((hi[i] - lo[i]) ** 2 for i in range(3)))
        distance = radius / math.sin(math.radians(fov) / 2) * margin
    yr, pr = math.radians(yaw), math.radians(pitch)
    # yaw 0 = behind the weapon (+Z), 90 = right side (+X), 180 = in front of the muzzle (-Z).
    eye = center + distance * np.array([math.sin(yr) * math.cos(pr), math.sin(pr), math.cos(yr) * math.cos(pr)])
    cam = Camera(size[0], size[1], eye=tuple(eye), target=tuple(center), fov=fov, near=0.5)
    r = Renderer(size[0], size[1], background=background)
    r.draw(model_quads(model, texture, uvs), cam, shading="lambert", light_dir=light)
    return r.image()


def side(model, texture, uvs, size=(1600, 600), background=(0, 0, 0, 0)):
    lo, hi = model.bounds()
    center = np.array([(lo[i] + hi[i]) / 2 for i in range(3)])
    extent = max(hi[2] - lo[2], (hi[1] - lo[1]) * size[0] / size[1])
    cam = Camera(size[0], size[1], eye=tuple(center + np.array([60, 0, 0])), target=tuple(center), scale=size[0] * 0.9 / extent)
    r = Renderer(size[0], size[1], background=background)
    r.draw(model_quads(model, texture, uvs), cam, shading="mc")
    return r.image()


def main():
    import guns as gun_defs
    out_dir = sys.argv[1] if len(sys.argv) > 1 else "hero"
    names = set(sys.argv[2:])
    os.makedirs(out_dir, exist_ok=True)
    for fn in gun_defs.ALL:
        g = fn()
        if names and g.name not in names:
            continue
        tex, mats, uvs = build_texture(g.model, seed=sum(map(ord, g.name)))
        hero(g.model, tex, uvs, background=(26, 28, 32, 255)).save(os.path.join(out_dir, g.name + "_hero.png"))
        hero(g.model, tex, uvs, yaw=-62, pitch=14, light=(-0.6, 0.8, -0.1), background=(26, 28, 32, 255)).save(os.path.join(out_dir, g.name + "_hero_left.png"))
        side(g.model, tex, uvs, background=(26, 28, 32, 255)).save(os.path.join(out_dir, g.name + "_side.png"))
        tex.save(os.path.join(out_dir, g.name + "_texture.png"))
        print(g.name, len(g.model.boxes), "boxes, texture", tex.size)


if __name__ == "__main__":
    main()
