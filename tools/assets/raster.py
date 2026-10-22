"""Tiny numpy software rasteriser for the voxel models (icons, viewmodel previews, marketing renders).

Faces use Minecraft's directional shading by default (up 1.0, north/south 0.8, east/west 0.6, down 0.5) so renders
read like the game. Texture sampling is nearest-neighbour, matching Minecraft's pixel-art look.
"""
from __future__ import annotations

import math

import numpy as np
from PIL import Image

from voxel import FACES, VoxelModel, face_dims

MC_SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}


def rotation_matrix(axis, angle_deg):
    a = math.radians(angle_deg)
    c, s = math.cos(a), math.sin(a)
    if axis == "x":
        return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])
    if axis == "y":
        return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


def face_corners(frm, to, face):
    """Corners (top-left, top-right, bottom-right, bottom-left as seen from outside) with uv (0..1)."""
    x0, y0, z0 = frm
    x1, y1, z1 = to
    if face == "north":
        pts = [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)]
    elif face == "south":
        pts = [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)]
    elif face == "east":
        pts = [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)]
    elif face == "west":
        pts = [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)]
    elif face == "up":
        pts = [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)]
    else:
        pts = [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)]
    uvs = [(0, 0), (1, 0), (1, 1), (0, 1)]
    return np.array(pts, dtype=float), uvs


FACE_NORMALS = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}


class Quad:
    __slots__ = ("pts", "uv", "tex", "normal", "face", "tint")

    def __init__(self, pts, uv, tex, normal, face, tint=None):
        self.pts = pts  # 4x3 model-space points (pixels)
        self.uv = uv  # 4x2 texel coordinates
        self.tex = tex  # HxWx4 uint8 array
        self.normal = normal
        self.face = face
        self.tint = tint


def model_quads(model: VoxelModel, texture: Image.Image, uvs, parts=None):
    tex = np.asarray(texture.convert("RGBA"))
    quads = []
    for bi, b in enumerate(model.boxes):
        if parts is not None and b.part not in parts:
            continue
        for f in FACES:
            if (bi, f) not in uvs:
                continue
            pts, corner_uv = face_corners(b.frm, b.to, f)
            normal = np.array(FACE_NORMALS[f], dtype=float)
            if b.rot is not None:
                axis, angle, origin = b.rot
                r = rotation_matrix(axis, angle)
                o = np.array(origin, dtype=float)
                pts = (pts - o) @ r.T + o
                normal = r @ normal
            x, y, w, h = uvs[(bi, f)]
            uw, uh = w, h
            uv = np.array([[x + cu * uw, y + cv * uh] for cu, cv in corner_uv], dtype=float)
            quads.append(Quad(pts, uv, tex, normal, f))
    return quads


def box_quads(frm, to, color_fn, transform=None, part_tex=None):
    """Flat-coloured box quads (used for preview arms). color_fn(face, v) -> rgba for a vertical fraction v."""
    quads = []
    for f in FACES:
        pts, corner_uv = face_corners(frm, to, f)
        normal = np.array(FACE_NORMALS[f], dtype=float)
        tex = np.zeros((16, 1, 4), dtype=np.uint8)
        for j in range(16):
            tex[j, 0] = color_fn(f, j / 15.0)
        uv = np.array([[cu * 1.0, cv * 16.0] for cu, cv in corner_uv], dtype=float)
        q = Quad(pts, uv, tex, normal, f)
        quads.append(q)
    if transform is not None:
        for q in quads:
            q.pts, q.normal = transform(q.pts, q.normal)
    return quads


class Camera:
    """Either orthographic (scale = pixels per unit) or perspective (fov in degrees, vertical)."""

    def __init__(self, width, height, eye=(0, 0, 0), target=(0, 0, -1), up=(0, 1, 0), fov=None, scale=None, near=0.02):
        self.w, self.h = width, height
        self.eye = np.array(eye, dtype=float)
        f = np.array(target, dtype=float) - self.eye
        f /= np.linalg.norm(f)
        r = np.cross(f, np.array(up, dtype=float))
        r /= np.linalg.norm(r)
        u = np.cross(r, f)
        self.basis = np.stack([r, u, -f])  # view-space rows
        self.fov = fov
        self.scale = scale
        self.near = near

    def to_view(self, pts):
        return (pts - self.eye) @ self.basis.T

    def project(self, v):
        """View-space points -> screen x, y, depth (positive in front)."""
        depth = -v[:, 2]
        if self.fov is not None:
            f = 1.0 / math.tan(math.radians(self.fov) / 2.0)
            sx = (v[:, 0] * f / np.maximum(depth, 1e-6)) * (self.h / 2.0) + self.w / 2.0
            sy = -(v[:, 1] * f / np.maximum(depth, 1e-6)) * (self.h / 2.0) + self.h / 2.0
        else:
            sx = v[:, 0] * self.scale + self.w / 2.0
            sy = -v[:, 1] * self.scale + self.h / 2.0
        return sx, sy, depth


class Renderer:
    def __init__(self, width, height, background=(0, 0, 0, 0)):
        self.w, self.h = width, height
        self.color = np.zeros((height, width, 4), dtype=np.float32)
        self.color[:, :] = np.array(background, dtype=np.float32)
        self.depth = np.full((height, width), np.inf, dtype=np.float32)

    def draw(self, quads, camera: Camera, shading="mc", light_dir=(0.4, 0.8, 0.45), view_transform=None):
        ld = np.array(light_dir, dtype=float)
        ld /= np.linalg.norm(ld)
        for q in quads:
            pts = q.pts
            normal = q.normal
            if view_transform is not None:
                pts, normal = view_transform(pts, normal)
            v = camera.to_view(pts)
            n_view = camera.basis @ normal
            if shading == "mc":
                shade = MC_SHADE[q.face] if view_transform is None else _mc_like(normal)
            else:
                shade = 0.5 + 0.55 * max(0.0, float(np.dot(normal, ld)))
            # Back-face culling (both windings are emitted only where needed).
            center = v.mean(axis=0)
            if camera.fov is not None:
                if np.dot(n_view, -center) <= 0:
                    continue
            elif n_view[2] <= 0:
                continue
            for tri in ((0, 1, 2), (0, 2, 3)):
                tv, tuv = v[list(tri)], q.uv[list(tri)]
                if camera.fov is None:
                    self._triangle(tv, tuv, q.tex, shade, camera, q.tint)
                    continue
                for cv, cuv in _clip_near(tv, tuv, camera.near):
                    self._triangle(cv, cuv, q.tex, shade, camera, q.tint)

    def _triangle(self, v, uv, tex, shade, cam, tint):
        sx, sy, depth = cam.project(v)
        minx = max(int(math.floor(sx.min())), 0)
        maxx = min(int(math.ceil(sx.max())), self.w - 1)
        miny = max(int(math.floor(sy.min())), 0)
        maxy = min(int(math.ceil(sy.max())), self.h - 1)
        if minx > maxx or miny > maxy:
            return
        xs, ys = np.meshgrid(np.arange(minx, maxx + 1) + 0.5, np.arange(miny, maxy + 1) + 0.5)
        x0, x1, x2 = sx
        y0, y1, y2 = sy
        area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
        if abs(area) < 1e-9:
            return
        w0 = ((x1 - xs) * (y2 - ys) - (x2 - xs) * (y1 - ys)) / area
        w1 = ((x2 - xs) * (y0 - ys) - (x0 - xs) * (y2 - ys)) / area
        w2 = 1.0 - w0 - w1
        inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
        if not inside.any():
            return
        if cam.fov is not None:
            iz = np.array([1.0 / d for d in depth])
            denom = w0 * iz[0] + w1 * iz[1] + w2 * iz[2]
            z = 1.0 / denom
            u = (w0 * uv[0, 0] * iz[0] + w1 * uv[1, 0] * iz[1] + w2 * uv[2, 0] * iz[2]) * z
            vv = (w0 * uv[0, 1] * iz[0] + w1 * uv[1, 1] * iz[1] + w2 * uv[2, 1] * iz[2]) * z
        else:
            z = w0 * depth[0] + w1 * depth[1] + w2 * depth[2]
            u = w0 * uv[0, 0] + w1 * uv[1, 0] + w2 * uv[2, 0]
            vv = w0 * uv[0, 1] + w1 * uv[1, 1] + w2 * uv[2, 1]
        th, tw = tex.shape[0], tex.shape[1]
        ui = np.clip(np.floor(u).astype(int), 0, tw - 1)
        vi = np.clip(np.floor(vv).astype(int), 0, th - 1)
        texel = tex[vi, ui].astype(np.float32)
        visible = inside & (texel[..., 3] > 8)
        region_depth = self.depth[miny:maxy + 1, minx:maxx + 1]
        closer = visible & (z < region_depth)
        if not closer.any():
            return
        rgb = texel[..., :3] * shade
        if tint is not None:
            rgb = rgb * np.array(tint[:3], dtype=np.float32) / 255.0
        region_color = self.color[miny:maxy + 1, minx:maxx + 1]
        region_color[closer, :3] = rgb[closer]
        region_color[closer, 3] = 255
        region_depth[closer] = z[closer]

    def image(self):
        return Image.fromarray(np.clip(self.color, 0, 255).astype(np.uint8), "RGBA")


def _clip_near(v, uv, near):
    """Clips a view-space triangle against the near plane (z = -near); returns 0..2 triangles."""
    inside = [-p[2] >= near for p in v]
    if all(inside):
        return [(v, uv)]
    if not any(inside):
        return []
    poly, polyuv = [], []
    for i in range(3):
        a, b = v[i], v[(i + 1) % 3]
        ua, ub = uv[i], uv[(i + 1) % 3]
        ia, ib = inside[i], inside[(i + 1) % 3]
        if ia:
            poly.append(a)
            polyuv.append(ua)
        if ia != ib:
            t = (-near - a[2]) / (b[2] - a[2])
            poly.append(a + (b - a) * t)
            polyuv.append(ua + (ub - ua) * t)
    tris = []
    for i in range(1, len(poly) - 1):
        tris.append((np.array([poly[0], poly[i], poly[i + 1]]), np.array([polyuv[0], polyuv[i], polyuv[i + 1]])))
    return tris


def _mc_like(normal):
    n = normal / max(1e-9, np.linalg.norm(normal))
    # Blend Minecraft's axis shading by the normal's components.
    return float(abs(n[1]) * (1.0 if n[1] > 0 else 0.5) + abs(n[2]) * 0.8 + abs(n[0]) * 0.6) / max(1e-9, abs(n[0]) + abs(n[1]) + abs(n[2]))


def add_outline(img: Image.Image, darken=0.45):
    """1px dark outline around the alpha silhouette (crisp inventory icons)."""
    a = np.asarray(img).copy()
    alpha = a[..., 3] > 0
    out = a.copy()
    h, w = alpha.shape
    for y in range(h):
        for x in range(w):
            if alpha[y, x]:
                continue
            neigh = []
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and alpha[ny, nx]:
                    neigh.append(a[ny, nx, :3])
            if neigh:
                c = np.mean(neigh, axis=0) * darken
                out[y, x] = [int(c[0]), int(c[1]), int(c[2]), 255]
    return Image.fromarray(out, "RGBA")
