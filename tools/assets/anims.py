"""Keyframed weapon animations in the Bedrock/Blockbench animation JSON format.

Every weapon gets its own set (assets/remnants_ballistics/animations/gun/<weapon>.json) built from its family's
choreography and its own geometry, so hands land exactly on that weapon's magazine, charging handle, pump or bolt.

Bones: gun (whole weapon, around the grip), action, magazine (around their pivots), left_arm and right_arm (hand
offsets in model pixels from the left-hand anchor and the grip). Units: model pixels and degrees; +X right, +Y up,
+Z toward the shooter. Positive X rotation lifts the muzzle; positive Z rotation rolls the right side up.

Clips: idle (loop), equip, inspect, fire, fire_last, cycle, reload, reload_empty, reload_start, reload_insert,
reload_end and charge. Reload clips are authored at a nominal length and time-scaled to the server's durations.
"""
from __future__ import annotations

NS = "remnants_ballistics"

# Per-weapon tuning: fire recoil kick (px), muzzle climb (deg), action travel when firing (px along +Z),
# action travel for the empty-reload charge (px along +Z, 0 = none), and the charge handle's world position.
TUNING = {
    "r9_sidearm": dict(kick=0.9, climb=6.0, fire_action=2.4, charge=2.4, fire_len=0.2),
    "ironfang_50": dict(kick=1.4, climb=11.0, fire_action=2.2, charge=2.2, fire_len=0.3),
    "warden_357": dict(kick=1.2, climb=12.0, fire_action=0.0, charge=0.0, fire_len=0.3),
    "viper_9": dict(kick=0.4, climb=1.4, fire_action=0.0, charge=3.4, fire_len=0.09),
    "spectre_pdw": dict(kick=0.35, climb=1.2, fire_action=0.0, charge=3.0, fire_len=0.09),
    "arx_16": dict(kick=0.5, climb=1.8, fire_action=0.6, charge=2.6, fire_len=0.11),
    "raven_ak": dict(kick=0.6, climb=2.4, fire_action=2.8, charge=3.0, fire_len=0.12),
    "mantis_br": dict(kick=0.45, climb=1.6, fire_action=0.0, charge=3.0, fire_len=0.1),
    "breacher_12": dict(kick=1.1, climb=5.5, fire_action=2.4, charge=2.6, fire_len=0.22),
    "thunderpump": dict(kick=1.7, climb=9.0, fire_action=0.0, charge=0.0, fire_len=0.3, pump=3.2),
    "ghostline_m24": dict(kick=1.5, climb=8.0, fire_action=0.0, charge=0.0, fire_len=0.35, bolt=3.6),
    "longshot_50": dict(kick=2.4, climb=11.0, fire_action=0.0, charge=0.0, fire_len=0.45, bolt=4.0),
    "titan_56": dict(kick=0.35, climb=1.0, fire_action=0.0, charge=0.0, fire_len=0.09),
    "bulwark_762": dict(kick=0.45, climb=1.3, fire_action=0.0, charge=0.0, fire_len=0.1),
    "voidcaster": dict(kick=0.3, climb=1.4, fire_action=0.0, charge=0.0, fire_len=0.16),
    "eclipse_railgun": dict(kick=2.6, climb=9.0, fire_action=0.0, charge=0.0, fire_len=0.55),
}

# Where the support hand grabs to charge each weapon (model pixels).
CHARGE_HANDLE = {
    "viper_9": (5.3, 10.1, -6.0),
    "spectre_pdw": (5.6, 10.0, 2.4),
    "arx_16": (8.0, 11.0, 15.4),
    "raven_ak": (10.6, 8.5, 6.7),
    "mantis_br": (5.6, 9.8, -4.0),
    "breacher_12": (10.8, 8.8, 6.3),
}


def _vec(v):
    return [round(float(c), 3) for c in v]


def _add(a, b):
    return tuple(a[i] + b[i] for i in range(3))


def _sub(a, b):
    return tuple(a[i] - b[i] for i in range(3))


def track(keys, smooth=True):
    """keys: [(time, (x, y, z)) or (time, (x, y, z), lerp)] -> Bedrock keyframe dict."""
    out = {}
    for k in keys:
        t, v = k[0], k[1]
        lerp = k[2] if len(k) > 2 else ("catmullrom" if smooth else "linear")
        value = _vec(v)
        out[f"{round(t, 4):g}"] = {"post": value, "lerp_mode": lerp} if lerp != "linear" else value
    return out


class Clip:
    def __init__(self, length, loop=False):
        self.length = length
        self.loop = loop
        self.bones = {}
        self.sounds = {}

    def channel(self, bone, channel, keys, smooth=True):
        self.bones.setdefault(bone, {})[channel] = track(keys, smooth)
        return self

    def sound(self, t, name, volume=1.0, pitch=1.0):
        effect = {"effect": f"{NS}:{name}"}
        if volume != 1.0:
            effect["volume"] = volume
        if pitch != 1.0:
            effect["pitch"] = pitch
        self.sounds.setdefault(f"{round(t, 4):g}", []).append(effect)
        return self

    def json(self):
        out = {"loop": True} if self.loop else {}
        out["animation_length"] = round(self.length, 4)
        out["bones"] = self.bones
        if self.sounds:
            out["sound_effects"] = dict(sorted(self.sounds.items(), key=lambda kv: float(kv[0])))
        return out


ZERO = (0, 0, 0)
ONE = (1, 1, 1)


# ------------------------------------------------------------------------------------------------------------------
# Shared clips
# ------------------------------------------------------------------------------------------------------------------

def idle():
    c = Clip(3.2, loop=True)
    c.channel("gun", "rotation", [(0, (0, 0, 0)), (0.8, (0.45, 0.15, 0.1)), (1.6, (0.1, -0.1, -0.08)), (2.4, (-0.35, 0.05, 0.05)),
                                  (3.2, (0, 0, 0))])
    c.channel("gun", "position", [(0, (0, 0, 0)), (1.6, (0, -0.14, 0.04)), (3.2, (0, 0, 0))])
    return c


def equip(heavy=False):
    length = 0.7 if heavy else 0.5
    c = Clip(length)
    c.channel("gun", "rotation", [(0, (-55, 18, -14)), (length * 0.7, (3, -1, 1)), (length, ZERO)])
    c.channel("gun", "position", [(0, (2, -7, 3)), (length * 0.7, (0, 0.3, -0.2)), (length, ZERO)])
    return c  # The draw sound is played by ClientGunHandler when the weapon is selected.


def inspect(g, action_peek=0.0):
    c = Clip(3.4)
    c.channel("gun", "rotation", [(0, ZERO), (0.7, (-6, -28, 38)), (1.6, (-4, -24, 34)), (2.2, (8, 22, -18)), (2.8, (6, 18, -14)),
                                  (3.4, ZERO)])
    c.channel("gun", "position", [(0, ZERO), (0.7, (-1.5, 1.0, -1.0)), (1.6, (-1.2, 1.2, -1.0)), (2.2, (1.0, 0.6, -0.5)), (3.4, ZERO)])
    if action_peek:
        c.channel("action", "position", [(0, ZERO), (0.9, ZERO), (1.1, (0, 0, action_peek)), (1.4, (0, 0, action_peek)), (1.55, ZERO),
                                         (3.4, ZERO)])
        c.sound(0.95, "gun.slide_back", 0.6)
        c.sound(1.5, "gun.slide_release", 0.6)
    return c  # The inspect rustle is played by ClientGunHandler when the key is pressed.


def fire(t, last=False, action=None):
    length = t["fire_len"]
    c = Clip(length + (0.15 if last else 0))
    k, climb = t["kick"], t["climb"]
    c.channel("gun", "position", [(0, ZERO), (length * 0.18, (0, 0.1 * k, k)), (length, ZERO)])
    c.channel("gun", "rotation", [(0, ZERO), (length * 0.2, (climb, climb * 0.12, climb * 0.08)), (length, ZERO)])
    travel = t["fire_action"] if action is None else action
    if travel:
        if last:
            c.channel("action", "position", [(0, ZERO), (length * 0.25, (0, 0, travel)), (length + 0.15, (0, 0, travel))], smooth=False)
        else:
            c.channel("action", "position", [(0, ZERO), (length * 0.25, (0, 0, travel)), (length * 0.7, ZERO)], smooth=False)
    return c


# ------------------------------------------------------------------------------------------------------------------
# Magazine reloads
# ------------------------------------------------------------------------------------------------------------------

def mag_reload(g, t, empty, pistol=False, bullpup=False, lmg=False):
    """Magazine swap: tilt, drop the old magazine, fetch and seat a new one, optionally charge the action."""
    vis = g.visuals
    L = tuple(vis["left_hand"])
    M = tuple(vis["pivots"]["magazine"])
    to_mag = _sub(M, L)
    length = 1.6 if pistol else 2.4
    c = Clip(length)
    T = lambda f: f * length  # noqa: E731
    tilt = (10, 10, 24) if pistol else ((6, 18, 20) if bullpup else (7, 12, 18))
    if lmg:
        tilt = (4, 6, 10)
    end_settle = 0.9 if empty and t["charge"] else 0.82
    c.channel("gun", "rotation", [(0, ZERO), (T(0.12), tilt), (T(0.5), _add(tilt, (2, 0, 3))), (T(0.6), _add(tilt, (-2, 0, 0))),
                                  (T(end_settle), (2, 0, 2)), (T(1.0), ZERO)])
    c.channel("gun", "position", [(0, ZERO), (T(0.12), (-1.0, 0.6, -1.0)), (T(0.6), (-1.0, 1.1, -1.0)), (T(0.62), (-1.0, 0.7, -1.0)),
                                  (T(end_settle), (0, 0.2, 0)), (T(1.0), ZERO)])
    drop = (0, -12, 1.5) if not bullpup else (0, -12, 0.5)
    c.channel("magazine", "position", [(0, ZERO), (T(0.2), ZERO), (T(0.24), (0, -1.0, 0.1)), (T(0.36), drop), (T(0.44), (0, -9, 0)),
                                       (T(0.56), (0, -1.6, 0)), (T(0.62), ZERO), (T(1.0), ZERO)], smooth=False)
    c.channel("magazine", "scale", [(0, ONE, "step"), (T(0.36), (0, 0, 0), "step"), (T(0.44), ONE, "step")])
    c.channel("magazine", "rotation", [(0, ZERO), (T(0.24), ZERO), (T(0.36), (14, 0, 0)), (T(0.44), (8, 0, 0)), (T(0.6), ZERO)])
    grab = _add(to_mag, (0, -1.5, 0))
    fetch = _add(to_mag, (0.5, -9, 2))
    hand = [(0, ZERO), (T(0.14), _add(grab, (0, 0, 1))), (T(0.22), grab), (T(0.36), _add(fetch, (0, 3, 0))), (T(0.44), fetch),
            (T(0.56), _add(to_mag, (0, -3.2, 0))), (T(0.62), _add(to_mag, (0, -2.0, 0)))]
    if empty and t["charge"]:
        handle = CHARGE_HANDLE.get(g.name)
        if handle is not None:
            to_handle = _sub(handle, L)
            hand += [(T(0.72), to_handle), (T(0.8), _add(to_handle, (0, 0, t["charge"]))), (T(0.84), _add(to_handle, (0, 0.6, t["charge"] + 0.6))),
                     (T(0.95), ZERO)]
            c.channel("action", "position", [(0, ZERO), (T(0.74), ZERO), (T(0.8), (0, 0, t["charge"])), (T(0.84), ZERO)], smooth=False)
            c.sound(T(0.76), "gun.slide_back")
            c.sound(T(0.84), "gun.slide_release")
        elif pistol:
            # Slide locked back after the last shot; released with the thumb once the new magazine is seated.
            c.channel("action", "position", [(0, (0, 0, t["charge"])), (T(0.7), (0, 0, t["charge"])), (T(0.74), ZERO)], smooth=False)
            c.sound(T(0.72), "gun.slide_release")
            hand += [(T(0.8), ZERO)]
        else:
            hand += [(T(0.8), ZERO)]
    else:
        hand += [(T(0.78), ZERO)]
    hand.append((T(1.0), ZERO))
    c.channel("left_arm", "position", hand)
    c.sound(T(0.22), "gun.mag_out")
    c.sound(T(0.62), "gun.mag_in")
    return c


def lmg_reload(g, t):
    """Open the feed cover, swap the ammunition box and belt, close the cover."""
    vis = g.visuals
    L = tuple(vis["left_hand"])
    A = tuple(vis["pivots"]["action"])
    M = tuple(vis["pivots"]["magazine"])
    to_cover = _sub(_add(A, (0, 0.8, -6)), L)
    to_box = _sub(M, L)
    length = 3.6
    c = Clip(length)
    T = lambda f: f * length  # noqa: E731
    c.channel("gun", "rotation", [(0, ZERO), (T(0.1), (5, 10, 14)), (T(0.5), (6, 12, 16)), (T(0.85), (4, 8, 10)), (T(1.0), ZERO)])
    c.channel("gun", "position", [(0, ZERO), (T(0.1), (-1.2, 0.8, -1.5)), (T(0.85), (-1.0, 0.6, -1.2)), (T(1.0), ZERO)])
    c.channel("action", "rotation", [(0, ZERO), (T(0.1), ZERO), (T(0.16), (72, 0, 0)), (T(0.74), (72, 0, 0)), (T(0.8), ZERO)])
    c.channel("magazine", "position", [(0, ZERO), (T(0.24), ZERO), (T(0.36), (-1, -12, 1)), (T(0.46), (-1, -10, 0)),
                                       (T(0.6), (0, -1.0, 0)), (T(0.66), ZERO)], smooth=False)
    c.channel("magazine", "scale", [(0, ONE, "step"), (T(0.36), (0, 0, 0), "step"), (T(0.46), ONE, "step")])
    c.channel("left_arm", "position", [(0, ZERO), (T(0.08), to_cover), (T(0.16), _add(to_cover, (0, 3, 0))), (T(0.24), to_box),
                                       (T(0.36), _add(to_box, (0, -9, 1))), (T(0.46), _add(to_box, (0, -8, 0))), (T(0.62), _add(to_box, (0, -1, 0))),
                                       (T(0.7), _add(to_cover, (0, 3, 0))), (T(0.8), to_cover), (T(0.92), ZERO), (T(1.0), ZERO)])
    c.sound(T(0.12), "gun.cover_open")
    c.sound(T(0.28), "gun.mag_out")
    c.sound(T(0.62), "gun.mag_in")
    c.sound(T(0.8), "gun.cover_close")
    return c


def cell_reload(g, t, collar=False):
    vis = g.visuals
    L = tuple(vis["left_hand"])
    M = tuple(vis["pivots"]["magazine"])
    to_cell = _sub(M, L)
    length = 2.0
    c = Clip(length)
    T = lambda f: f * length  # noqa: E731
    c.channel("gun", "rotation", [(0, ZERO), (T(0.15), (8, 14, 20)), (T(0.6), (9, 14, 22)), (T(0.85), (2, 2, 2)), (T(1.0), ZERO)])
    c.channel("gun", "position", [(0, ZERO), (T(0.15), (-1, 0.8, -1)), (T(0.85), (0, 0.2, 0)), (T(1.0), ZERO)])
    c.channel("magazine", "position", [(0, ZERO), (T(0.22), ZERO), (T(0.3), (0, -2.5, 0)), (T(0.4), (0, -11, 0)), (T(0.48), (0, -9, 0)),
                                       (T(0.62), (0, -1.5, 0)), (T(0.68), ZERO)], smooth=False)
    c.channel("magazine", "scale", [(0, ONE, "step"), (T(0.4), (0, 0, 0), "step"), (T(0.48), ONE, "step")])
    c.channel("left_arm", "position", [(0, ZERO), (T(0.16), _add(to_cell, (0, -1.5, 0))), (T(0.3), _add(to_cell, (0, -4, 0))),
                                       (T(0.44), _add(to_cell, (0, -9, 1))), (T(0.62), _add(to_cell, (0, -3, 0))), (T(0.7), _add(to_cell, (0, -1.5, 0))),
                                       (T(0.86), ZERO), (T(1.0), ZERO)])
    action_axis = (0, 0, -1.6) if collar else (0, 0.5, 0)
    c.channel("action", "position", [(0, ZERO), (T(0.12), action_axis), (T(0.74), action_axis), (T(0.82), ZERO)])
    c.sound(T(0.26), "gun.cell_out")
    c.sound(T(0.66), "gun.cell_in")
    return c


# ------------------------------------------------------------------------------------------------------------------
# Manual actions
# ------------------------------------------------------------------------------------------------------------------

def pump_cycle(g, t):
    p = t["pump"]
    c = Clip(0.5)
    c.channel("action", "position", [(0, ZERO), (0.18, (0, 0, p)), (0.24, (0, 0, p)), (0.42, ZERO)], smooth=False)
    c.channel("left_arm", "position", [(0, ZERO), (0.18, (0, 0, p)), (0.24, (0, 0, p)), (0.42, ZERO)], smooth=False)
    c.channel("gun", "rotation", [(0, ZERO), (0.2, (-1.5, 0, 2)), (0.45, ZERO)])
    c.sound(0.04, "gun.pump_back")
    c.sound(0.3, "gun.pump_forward")
    return c


def bolt_cycle(g, t, length=1.0):
    vis = g.visuals
    P = tuple(vis["pivot"])
    A = tuple(vis["pivots"]["action"])
    knob = _add(A, (3.2, -2.0, 0))
    to_knob = _sub(knob, P)
    b = t["bolt"]
    c = Clip(length)
    T = lambda f: f * length  # noqa: E731
    c.channel("action", "rotation", [(0, ZERO), (T(0.18), ZERO), (T(0.3), (0, 0, 62)), (T(0.72), (0, 0, 62)), (T(0.84), ZERO)], smooth=False)
    c.channel("action", "position", [(0, ZERO), (T(0.3), ZERO), (T(0.46), (0, 0, b)), (T(0.56), (0, 0, b)), (T(0.72), ZERO)], smooth=False)
    c.channel("right_arm", "position", [(0, ZERO), (T(0.18), to_knob), (T(0.3), _add(to_knob, (-0.8, 2.6, 0))),
                                        (T(0.46), _add(to_knob, (-0.8, 2.6, b))), (T(0.56), _add(to_knob, (-0.8, 2.6, b))),
                                        (T(0.72), _add(to_knob, (-0.8, 2.6, 0))), (T(0.84), to_knob), (T(1.0), ZERO)])
    c.channel("gun", "rotation", [(0, ZERO), (T(0.2), (2, -4, -8)), (T(0.8), (2, -4, -8)), (T(1.0), ZERO)])
    c.sound(T(0.26), "gun.bolt_open")
    c.sound(T(0.76), "gun.bolt_close")
    return c


def single_load(g, t, kind):
    """reload_start / reload_insert / reload_end for revolvers, pump shotguns and internal-magazine bolt rifles."""
    vis = g.visuals
    L = tuple(vis["left_hand"])
    P = tuple(vis["pivot"])
    clips = {}
    if kind == "revolver":
        A = tuple(vis["pivots"]["action"])
        tilt = (12, 10, -26)
        open_rot = (0, 0, 78)
        to_cyl = _add(_sub(A, L), (-1.5, 1.5, 3.0))
        s = Clip(0.6)
        s.channel("gun", "rotation", [(0, ZERO), (0.35, tilt), (0.6, tilt)])
        s.channel("gun", "position", [(0, ZERO), (0.35, (-1, 1, -1)), (0.6, (-1, 1, -1))])
        s.channel("action", "rotation", [(0, ZERO), (0.12, ZERO), (0.3, open_rot), (0.6, open_rot)])
        s.channel("left_arm", "position", [(0, ZERO), (0.25, to_cyl), (0.6, _add(to_cyl, (0, -2, 1)))])
        s.sound(0.14, "gun.cylinder_open")
        i = Clip(0.45)
        i.channel("gun", "rotation", [(0, tilt), (0.25, _add(tilt, (1.5, 0, 0))), (0.45, tilt)])
        i.channel("gun", "position", [(0, (-1, 1, -1)), (0.45, (-1, 1, -1))])
        i.channel("action", "rotation", [(0, open_rot), (0.45, open_rot)])
        i.channel("left_arm", "position", [(0, _add(to_cyl, (0, -2, 1))), (0.15, _add(to_cyl, (0, 0, 2.5))), (0.25, _add(to_cyl, (0, 0, 1.2))),
                                           (0.45, _add(to_cyl, (0, -2, 1)))])
        i.sound(0.24, "gun.shell_insert")
        e = Clip(0.6)
        e.channel("gun", "rotation", [(0, tilt), (0.2, _add(tilt, (-4, 0, 6))), (0.6, ZERO)])
        e.channel("gun", "position", [(0, (-1, 1, -1)), (0.6, ZERO)])
        e.channel("action", "rotation", [(0, open_rot), (0.16, ZERO)], smooth=False)
        e.channel("left_arm", "position", [(0, _add(to_cyl, (0, -2, 1))), (0.12, to_cyl), (0.18, _add(to_cyl, (1.5, 0, 0))), (0.5, ZERO)])
        e.sound(0.14, "gun.cylinder_close")
    elif kind == "pump":
        port = (8, 6.3, 5.0)
        to_port = _sub(port, L)
        tilt = (10, -6, -32)
        s = Clip(0.5)
        s.channel("gun", "rotation", [(0, ZERO), (0.35, tilt), (0.5, tilt)])
        s.channel("gun", "position", [(0, ZERO), (0.35, (-0.5, 1.5, -1.5)), (0.5, (-0.5, 1.5, -1.5))])
        s.channel("left_arm", "position", [(0, ZERO), (0.4, _add(to_port, (0, -3, 0))), (0.5, _add(to_port, (0, -3, 0)))])
        i = Clip(0.4)
        i.channel("gun", "rotation", [(0, tilt), (0.22, _add(tilt, (1.5, 0, -1))), (0.4, tilt)])
        i.channel("gun", "position", [(0, (-0.5, 1.5, -1.5)), (0.4, (-0.5, 1.5, -1.5))])
        i.channel("left_arm", "position", [(0, _add(to_port, (0, -3, 0))), (0.12, _add(to_port, (0, -1.2, 1))), (0.22, _add(to_port, (0, -0.2, -1.5))),
                                           (0.4, _add(to_port, (0, -3, 0)))])
        i.sound(0.2, "gun.shell_insert")
        e = Clip(0.75)
        p = t["pump"]
        e.channel("gun", "rotation", [(0, tilt), (0.25, ZERO), (0.75, ZERO)])
        e.channel("gun", "position", [(0, (-0.5, 1.5, -1.5)), (0.25, ZERO)])
        e.channel("left_arm", "position", [(0, _add(to_port, (0, -3, 0))), (0.25, ZERO), (0.42, (0, 0, p)), (0.48, (0, 0, p)), (0.64, ZERO)])
        e.channel("action", "position", [(0, ZERO), (0.25, ZERO), (0.42, (0, 0, p)), (0.48, (0, 0, p)), (0.64, ZERO)], smooth=False)
        e.sound(0.3, "gun.pump_back")
        e.sound(0.54, "gun.pump_forward")
    else:  # bolt
        A = tuple(vis["pivots"]["action"])
        b = t["bolt"]
        knob = _add(A, (3.2, -2.0, 0))
        to_knob = _sub(knob, P)
        port = _add(A, (1.2, 0.4, -4.0))
        to_port = _sub(port, L)
        tilt = (6, -4, -14)
        s = Clip(0.7)
        s.channel("gun", "rotation", [(0, ZERO), (0.3, tilt), (0.7, tilt)])
        s.channel("action", "rotation", [(0, ZERO), (0.18, ZERO), (0.32, (0, 0, 62)), (0.7, (0, 0, 62))], smooth=False)
        s.channel("action", "position", [(0, ZERO), (0.32, ZERO), (0.5, (0, 0, b)), (0.7, (0, 0, b))], smooth=False)
        s.channel("right_arm", "position", [(0, ZERO), (0.18, to_knob), (0.32, _add(to_knob, (-0.8, 2.6, 0))), (0.5, _add(to_knob, (-0.8, 2.6, b))),
                                            (0.7, _add(to_knob, (-0.8, 2.6, b)))])
        s.channel("left_arm", "position", [(0, ZERO), (0.6, _add(to_port, (0, 2.0, 1.0))), (0.7, _add(to_port, (0, 2.0, 1.0)))])
        s.sound(0.24, "gun.bolt_open")
        i = Clip(0.42)
        hold_r = _add(to_knob, (-0.8, 2.6, b))
        i.channel("gun", "rotation", [(0, tilt), (0.42, tilt)])
        i.channel("action", "rotation", [(0, (0, 0, 62)), (0.42, (0, 0, 62))])
        i.channel("action", "position", [(0, (0, 0, b)), (0.42, (0, 0, b))])
        i.channel("right_arm", "position", [(0, hold_r), (0.42, hold_r)])
        i.channel("left_arm", "position", [(0, _add(to_port, (0, 2.0, 1.0))), (0.14, _add(to_port, (0, 0.8, 0))), (0.24, _add(to_port, (0, -0.3, -0.6))),
                                           (0.42, _add(to_port, (0, 2.0, 1.0)))])
        i.sound(0.22, "gun.shell_insert")
        e = Clip(0.7)
        e.channel("gun", "rotation", [(0, tilt), (0.5, tilt), (0.7, ZERO)])
        e.channel("action", "position", [(0, (0, 0, b)), (0.2, ZERO)], smooth=False)
        e.channel("action", "rotation", [(0, (0, 0, 62)), (0.2, (0, 0, 62)), (0.32, ZERO)], smooth=False)
        e.channel("right_arm", "position", [(0, hold_r), (0.2, _add(to_knob, (-0.8, 2.6, 0))), (0.32, to_knob), (0.55, ZERO)])
        e.channel("left_arm", "position", [(0, _add(to_port, (0, 2.0, 1.0))), (0.3, ZERO)])
        e.sound(0.24, "gun.bolt_close")
    clips["reload_start"], clips["reload_insert"], clips["reload_end"] = s, i, e
    # Fallback single clip (used only if a phase clip were missing).
    full = Clip(s.length + i.length + e.length)
    full.bones = s.bones
    full.sounds = dict(s.sounds)
    clips["reload"] = full
    return clips


def railgun_charge():
    c = Clip(1.0)
    c.channel("action", "position", [(0, ZERO), (0.9, (0, 0, -2.6)), (1.0, (0, 0, -2.6))])
    c.channel("gun", "position", [(0, ZERO), (0.25, (0.05, 0.02, 0)), (0.5, (-0.05, -0.02, 0.05)), (0.75, (0.06, 0.03, 0.05)), (1.0, (0, 0, 0.1))],
              smooth=False)
    c.channel("gun", "rotation", [(0, ZERO), (1.0, (-1.0, 0, 0))])
    return c  # The charge whine is played by ClientGunHandler (and by the server for other players).


def railgun_fire(t):
    c = fire(t)
    c.channel("action", "position", [(0, (0, 0, -2.6)), (0.08, (0, 0, 0.4)), (0.3, ZERO)])
    return c


def energy_fire(t):
    c = fire(t)
    c.channel("action", "position", [(0, ZERO), (0.03, (0, 0.5, 0)), (0.14, ZERO)])
    return c


# ------------------------------------------------------------------------------------------------------------------
# Assembly
# ------------------------------------------------------------------------------------------------------------------

def build(g):
    """Returns (set name, Bedrock animation JSON) for one weapon."""
    family = g.visuals["animations"].split(":")[1]
    t = TUNING[g.name]
    clips = {"idle": idle(), "equip": equip(heavy=family in ("lmg", "bolt_mag", "railgun"))}
    clips["fire"] = fire(t)
    if family == "pistol":
        clips["fire_last"] = fire(t, last=True)
        clips["inspect"] = inspect(g, action_peek=1.4)
        clips["reload"] = mag_reload(g, t, empty=False, pistol=True)
        clips["reload_empty"] = mag_reload(g, t, empty=True, pistol=True)
    elif family == "revolver":
        clips["inspect"] = inspect(g)
        clips.update(single_load(g, t, "revolver"))
    elif family in ("smg", "rifle", "shotgun", "bullpup"):
        clips["inspect"] = inspect(g)
        bullpup = family == "bullpup"
        clips["reload"] = mag_reload(g, t, empty=False, bullpup=bullpup)
        clips["reload_empty"] = mag_reload(g, t, empty=True, bullpup=bullpup)
    elif family == "pump":
        clips["inspect"] = inspect(g)
        clips["cycle"] = pump_cycle(g, t)
        clips.update(single_load(g, t, "pump"))
    elif family == "bolt":
        clips["inspect"] = inspect(g)
        clips["cycle"] = bolt_cycle(g, t)
        clips.update(single_load(g, t, "bolt"))
    elif family == "bolt_mag":
        clips["inspect"] = inspect(g)
        clips["cycle"] = bolt_cycle(g, t, length=1.1)
        clips["reload"] = mag_reload(g, t, empty=False)
        empty = mag_reload(g, t, empty=False)
        # Empty: swap the magazine, then work the bolt to chamber a round.
        cyc = bolt_cycle(g, t, length=1.1)
        empty.length += cyc.length
        for bone, channels in cyc.bones.items():
            for ch, keys in channels.items():
                shifted = {f"{round(float(k) + 2.4, 4):g}": v for k, v in keys.items()}
                empty.bones.setdefault(bone, {}).setdefault(ch, {}).update(shifted)
        for k, v in cyc.sounds.items():
            empty.sounds[f"{round(float(k) + 2.4, 4):g}"] = v
        clips["reload_empty"] = empty
    elif family == "lmg":
        clips["inspect"] = inspect(g)
        clips["reload"] = lmg_reload(g, t)
        clips["reload_empty"] = lmg_reload(g, t)
    elif family == "energy":
        clips["inspect"] = inspect(g)
        clips["fire"] = energy_fire(t)
        clips["reload"] = cell_reload(g, t)
        clips["reload_empty"] = cell_reload(g, t)
    elif family == "railgun":
        clips["inspect"] = inspect(g)
        clips["fire"] = railgun_fire(t)
        clips["charge"] = railgun_charge()
        clips["reload"] = cell_reload(g, t, collar=True)
        clips["reload_empty"] = cell_reload(g, t, collar=True)
    else:
        raise ValueError(f"{g.name}: unknown animation family {family}")
    animations = {f"animation.{NS}.{g.name}.{name}": clip.json() for name, clip in clips.items()}
    return g.name, {"format_version": "1.8.0", "animations": animations}
