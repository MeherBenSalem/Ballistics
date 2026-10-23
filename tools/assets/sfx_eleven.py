"""Real-recording style firearm sounds: generated with the ElevenLabs sound-generation API, filtered and cleaned into
mono Ogg Vorbis that replaces the procedural files from sfx.py. Energy weapons, impacts, UI and ambience stay procedural.

    ELEVENLABS_API_KEY=... python3 tools/assets/sfx_eleven.py              # fill every stem not yet recorded
    python3 tools/assets/sfx_eleven.py --only gun/ak_fire --redo           # re-roll one stem with fresh takes
    python3 tools/assets/sfx_eleven.py --pick gun/ak_fire=t00,t04,t07      # hand-pick cached takes after listening
    python3 tools/assets/sfx_eleven.py --reclean                           # re-clean recorded takes, no API calls
    python3 tools/assets/sfx_eleven.py --estimate                          # credits the run would spend

Every stem gets several candidate takes (raw API audio, cached under build/sfx_eleven so re-selecting is free). Takes
that are clipped, mostly rumble, faint, late or double-hit are rejected; the best ones are cleaned (40 Hz high-pass,
de-click, natural tail kept then faded, loudness matched to the procedural sound they replace) and written. Chosen
takes and their prompts go to sfx_recorded.json, which generate.py honours so a full regeneration keeps these files.
Output from the free ElevenLabs plan must credit elevenlabs.io (see NOTICE).
"""
from __future__ import annotations

import argparse
import concurrent.futures
import hashlib
import json
import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.request

import numpy as np

import sfx

SR = 44100
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
SOUNDS = os.path.join(ROOT, "1.21.1", "src", "main", "resources", "assets", "remnants_ballistics", "sounds")
CACHE = os.path.join(ROOT, "build", "sfx_eleven")
API = "https://api.elevenlabs.io/v1/sound-generation?output_format=pcm_44100"
MODEL = "eleven_text_to_sound_v2"
CREDITS_PER_SECOND = 10


# ------------------------------------------------------------------------------------------------------------------
# Catalogue
# ------------------------------------------------------------------------------------------------------------------

def shot(gun, character, echo="natural outdoor echo", extra=""):
    return (f"Authentic {gun} firing a single round outdoors, real gunshot recording, close microphone, {character}, "
            f"{echo}. One shot only{extra}, no music.")


def foley(action):
    return f"Close-up foley recording of {action}. Real firearm handling sound, single action, no gunshot, no music."


# stem: (kind, variants, candidates, request seconds, prompt influence, max length s, target LUFS, prompt)
# kind: "shot" (gunshot rules), "mech" (a mechanical action, anchored to its loudest click), "foley" (rustles, drops)
# Target loudness is the procedural sound each stem replaces, so the in-game mix stays where it was.
CATALOGUE = {
    "gun/pistol_fire": ("shot", 3, 6, 1.6, 0.5, 1.4, -16.8,
                        "Authentic 9mm handgun firing one round outdoors, real gunshot recording, close microphone, "
                        "natural reflections and echo tail. One shot only."),
    "gun/magnum_fire": ("shot", 3, 6, 2.0, 0.6, 2.0, -16.1,
                        "Loud .50 caliber magnum pistol fired once, real gunshot recorded close to the shooter, massive "
                        "concussive blast and crack, natural outdoor echo. One shot only, no music."),
    "gun/revolver_fire": ("shot", 3, 6, 2.0, 0.6, 1.8, -17.3,
                          shot(".357 Magnum revolver", "loud sharp blast with a ringing metallic crack")),
    "gun/smg_fire": ("shot", 3, 6, 1.2, 0.6, 1.0, -18.6,
                     shot("9mm MP5 submachine gun", "quick snappy report with a light mechanical clatter",
                          "natural reflections", ", no burst")),
    "gun/pdw_fire": ("shot", 3, 6, 1.2, 0.6, 0.9, -17.3,
                     shot("FN P90 5.7mm personal defense weapon", "sharp high-pitched crack", "natural reflections",
                          ", no burst")),
    "gun/rifle_fire": ("shot", 3, 6, 1.6, 0.6, 1.2, -18.2,
                       shot("AR-15 5.56mm rifle", "loud sharp supersonic crack and blast", extra=", no burst")),
    "gun/ak_fire": ("shot", 3, 6, 1.6, 0.6, 1.3, -19.6,
                    shot("AK-47 7.62x39mm rifle", "heavy punchy blast with a deep crack", extra=", no burst")),
    "gun/burst_fire": ("shot", 3, 6, 1.5, 0.6, 1.1, -17.6,
                       shot("FAMAS bullpup 5.56mm assault rifle", "sharp crack with a tight mechanical snap")),
    "gun/shotgun_fire": ("shot", 3, 6, 2.0, 0.6, 1.6, -17.4,
                         shot("semi-automatic 12 gauge shotgun", "huge booming blast", extra=", no pump")),
    "gun/pump_fire": ("shot", 3, 6, 2.0, 0.6, 1.6, -17.8,
                      shot("Remington 870 pump-action 12 gauge shotgun", "massive deep boom", extra=", no pumping")),
    "gun/sniper_fire": ("shot", 3, 6, 3.0, 0.7, 3.0, -19.5,
                        "Real field recording of a single gunshot from an M24 .308 bolt-action sniper rifle fired at an "
                        "outdoor long-distance range, recorded from the shooter's position. Very loud supersonic crack "
                        "and heavy muzzle blast followed by a long natural echo rolling across open terrain. Authentic "
                        "firearm audio, one shot only, no bolt action, no music, no voices."),
    "gun/heavy_sniper_fire": ("shot", 3, 6, 3.0, 0.7, 3.0, -19.1,
                              "Real field recording of a single gunshot from a Barrett M82 .50 BMG anti-materiel rifle "
                              "fired at an outdoor range, recorded from the shooter's position. Enormous concussive "
                              "blast and crack with a long natural echo rolling across open terrain. Authentic firearm "
                              "audio, one shot only, no music, no voices."),
    "gun/lmg_fire": ("shot", 3, 6, 1.5, 0.6, 1.2, -16.0,
                     shot("M249 5.56mm light machine gun", "loud sharp report with mechanical clatter",
                          extra=", no burst")),
    "gun/heavy_lmg_fire": ("shot", 3, 6, 1.6, 0.6, 1.3, -17.0,
                           shot("M240 7.62mm machine gun", "heavy powerful blast with mechanical clatter",
                                extra=", no burst")),
    "gun/suppressed_fire": ("shot", 3, 6, 1.2, 0.6, 1.1, -17.9,
                            shot("suppressed rifle", "muffled thump with the metallic clack of the action",
                                 "short quiet tail", ", no loud crack")),
    "gun/distant_fire": ("shot", 3, 6, 2.5, 0.6, 2.3, -17.0,
                         "Real recording of a single rifle gunshot heard from far away, about half a kilometre across "
                         "open terrain, muffled boom with a long rolling echo. One shot only, no music."),
    "gun/mag_out": ("mech", 2, 4, 1.0, 0.6, 0.8, -25.1, foley("a magazine release button pressed and the magazine pulled out of a pistol")),
    "gun/mag_in": ("mech", 2, 4, 1.0, 0.6, 0.9, -24.5, foley("a rifle magazine inserted and seated with a firm locking click")),
    "gun/slide_back": ("mech", 2, 4, 1.0, 0.6, 0.8, -21.7, foley("a pistol slide pulled back to the rear, metallic scrape")),
    "gun/slide_release": ("mech", 2, 4, 1.0, 0.6, 0.8, -22.4, foley("a pistol slide released and snapping forward into battery")),
    "gun/bolt_open": ("mech", 2, 4, 1.0, 0.6, 0.8, -22.8, foley("a bolt-action rifle bolt lifted and pulled back")),
    "gun/bolt_close": ("mech", 2, 4, 1.0, 0.6, 1.0, -20.7, foley("a bolt-action rifle bolt pushed forward and locked down, chambering a round")),
    "gun/pump_back": ("mech", 2, 4, 1.0, 0.6, 0.9, -24.9, foley("a pump-action shotgun forend racked back, ejecting a shell")),
    "gun/pump_forward": ("mech", 2, 4, 1.0, 0.6, 0.9, -24.0, foley("a pump-action shotgun forend pushed forward and locking")),
    "gun/shell_insert": ("mech", 2, 4, 1.0, 0.6, 0.8, -24.2, foley("a single 12 gauge shell pushed into a shotgun tube magazine")),
    "gun/cylinder_open": ("mech", 2, 4, 1.0, 0.6, 1.0, -20.0, foley("a revolver cylinder released and swung open")),
    "gun/cylinder_close": ("mech", 2, 4, 1.0, 0.6, 1.0, -24.1, foley("a revolver cylinder snapped shut with a firm click")),
    "gun/cover_open": ("mech", 2, 4, 1.0, 0.6, 0.8, -25.3, foley("a machine gun feed tray cover unlatched and opened")),
    "gun/cover_close": ("mech", 2, 4, 1.0, 0.6, 1.0, -23.4, foley("a machine gun feed tray cover slammed shut")),
    "gun/casing_drop": ("foley", 2, 4, 1.0, 0.6, 1.0, -19.4,
                        "Real recording of a single empty brass bullet casing falling and bouncing on a concrete floor, "
                        "metallic tinkle. No gunshot, no music."),
    "gun/shell_drop": ("foley", 2, 4, 1.0, 0.6, 0.9, -28.9,
                       "Real recording of a single empty plastic shotgun shell falling and bouncing on the ground. "
                       "No gunshot, no music."),
    "gun/dry_fire": ("mech", 2, 4, 0.6, 0.6, 0.6, -25.4, foley("a pistol dry firing on an empty chamber, trigger pull and hammer click")),
    "gun/fire_mode": ("mech", 2, 4, 0.6, 0.6, 0.6, -25.8, foley("a rifle fire selector switch flipped with a small metallic click")),
    "gun/jammed": ("mech", 2, 4, 0.8, 0.6, 0.8, -26.4, foley("a rifle jamming, trigger pulled with a dull mechanical clunk and the action stuck")),
    "gun/equip": ("foley", 2, 4, 1.0, 0.6, 0.9, -23.9, foley("a rifle picked up and shouldered, cloth rustle and a light rattle of the weapon")),
    "gun/aim_in": ("foley", 2, 4, 0.8, 0.6, 0.7, -19.7, foley("a soldier quickly raising a rifle to aim, clothing and gear rustle")),
    "gun/aim_out": ("foley", 2, 4, 0.8, 0.6, 0.7, -21.9, foley("a soldier lowering a rifle from the shoulder, short clothing rustle")),
    "gun/inspect": ("foley", 2, 4, 1.2, 0.6, 1.0, -21.1, foley("a rifle being turned over and inspected in the hands, gentle metal and polymer rattles")),
}


# ------------------------------------------------------------------------------------------------------------------
# Generation and cache
# ------------------------------------------------------------------------------------------------------------------

def take_dir(stem):
    _, _, _, seconds, influence, _, _, prompt = CATALOGUE[stem]
    key = hashlib.sha1(f"{MODEL}|{seconds}|{influence}|{prompt}".encode()).hexdigest()[:10]
    return os.path.join(CACHE, stem, key)


def cached_takes(stem):
    d = take_dir(stem)
    return sorted(os.path.join(d, f) for f in os.listdir(d) if f.endswith(".pcm")) if os.path.isdir(d) else []


def request(key, prompt, seconds, influence):
    body = json.dumps({"model_id": MODEL, "text": prompt, "duration_seconds": seconds,
                       "prompt_influence": influence}).encode()
    for attempt in range(6):
        req = urllib.request.Request(API, data=body, method="POST",
                                     headers={"xi-api-key": key, "Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=180) as r:
                return r.read()
        except urllib.error.HTTPError as e:
            if e.code in (429, 500, 502, 503) and attempt < 5:
                time.sleep(2 ** attempt * 2)
                continue
            raise RuntimeError(f"ElevenLabs HTTP {e.code}: {e.read().decode(errors='replace')[:300]}") from None


def generate_takes(key, stem, count):
    _, _, _, seconds, influence, _, _, prompt = CATALOGUE[stem]
    d = take_dir(stem)
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, "prompt.txt"), "w", encoding="utf-8") as f:
        f.write(f"{MODEL} seconds={seconds} influence={influence}\n{prompt}\n")
    start = len(cached_takes(stem))
    paths = [os.path.join(d, f"t{start + i:02d}.pcm") for i in range(count)]
    for path in paths:
        with open(path + ".part", "wb") as f:
            f.write(request(key, prompt, seconds, influence))
        os.replace(path + ".part", path)
    return paths


# ------------------------------------------------------------------------------------------------------------------
# Scoring and cleaning
# ------------------------------------------------------------------------------------------------------------------

def decode(path, declip):
    """Raw API PCM (s16le stereo) -> mono float, optionally de-clipped, 4th-order 40 Hz high-pass (DC + rumble)."""
    chain = ("adeclip," if declip else "") + "highpass=f=40,highpass=f=40"
    raw = subprocess.run(["ffmpeg", "-loglevel", "error", "-f", "s16le", "-ar", str(SR), "-ac", "2", "-i", path,
                          "-af", chain, "-ac", "2", "-f", "f32le", "-"], capture_output=True, check=True).stdout
    return np.frombuffer(raw, np.float32).reshape(-1, 2).astype(np.float64).mean(axis=1)


def envelope_db(x, ms=5):
    win = max(1, int(SR * ms / 1000))
    rms = np.sqrt(np.convolve(x ** 2, np.ones(win) / win, "same"))
    return 20 * np.log10(rms / (rms.max() + 1e-12) + 1e-12)


def score(path, kind):
    """(score, reasons it was rejected). Higher is better; any reason means the take is unusable."""
    st = np.fromfile(path, "<i2").reshape(-1, 2)
    clipped = int((np.abs(st.astype(np.int32)) >= 32700).sum())
    full = st.astype(np.float64).mean(axis=1) / 32768
    x = decode(path, declip=False)
    level = 20 * np.log10(np.abs(x).max() + 1e-12)
    rumble = 1 - (x ** 2).sum() / (((full - full.mean()) ** 2).sum() + 1e-12)
    db = envelope_db(x)
    peak = int(np.argmax(db))
    rms = 10 ** (db / 20)
    body = rms[peak + int(SR * 0.05): peak + int(SR * 0.2)]
    attack = 20 * np.log10(rms[: peak + int(SR * 0.02)].max() / (body.mean() + 1e-12)) if len(body) else 0.0
    reasons = []
    if level < (-32 if kind == "shot" else -40):
        reasons.append(f"faint ({level:.0f} dBFS)")
    if rumble > 0.85:
        reasons.append(f"rumble {rumble:.0%}")
    if clipped > (1500 if kind == "shot" else 500):
        reasons.append(f"clipped {clipped}")
    if kind == "shot":
        onset = int(np.argmax(db > -20))
        if (peak - onset) / SR > 0.06:
            reasons.append(f"late peak {(peak - onset) / SR * 1000:.0f} ms")
        # a second blast: after the shot decays, the (20 ms) envelope climbs 10 dB back up to within 12 dB of the peak
        smooth = envelope_db(x, 20)
        after = smooth[int(np.argmax(smooth)) + int(SR * 0.06):]
        if len(after) and np.any((after - np.minimum.accumulate(after) > 10) & (after > -12)):
            reasons.append("second hit")
    value = min(attack, 12) - clipped / 300 - rumble * 5 + min(level + 30, 10) / 5
    return value, reasons


def lufs(x):
    pad = np.concatenate([x, np.zeros(SR)]).astype("<f4")
    err = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-f", "f32le", "-ar", str(SR), "-ac", "1", "-i", "-",
                          "-af", "ebur128", "-f", "null", "-"], input=pad.tobytes(), capture_output=True).stderr
    return float(err.decode().rsplit("I:", 1)[1].split("LUFS")[0])


def clean(path, kind, max_len, target, peak_db=-0.8, max_drive=1.5):
    clipped = int((np.abs(np.fromfile(path, "<i2").astype(np.int32)) >= 32700).sum())
    x = decode(path, declip=clipped > 0)
    # onset: trim to 2 ms before the first sound, 0.5 ms raised-cosine fade-in so a hot start does not click. Mechanical
    # actions are anchored to their loudest transient instead (80 ms of lead-in kept, faded in), so the click lands on
    # the animation keyframe that plays it
    onset = int(np.argmax(np.abs(x) > np.abs(x).max() * (0.1 if kind == "shot" else 0.05)))
    start = max(0, onset - int(SR * 0.002))
    n = int(SR * 0.0005)
    if kind == "mech":
        lead = int(np.argmax(envelope_db(x))) - int(SR * 0.08)
        if lead > start:
            start, n = lead, int(SR * 0.015)
    x = x[start:]
    x[:n] *= 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, n))
    # tail: keep the natural echo down to -60 dB (shots) / -50 dB (foley), cap the length, raised-cosine fade
    db = envelope_db(x, 10)
    end = min(len(x), int(np.flatnonzero(db > (-60 if kind == "shot" else -50))[-1]) + int(SR * 0.03), int(SR * max_len))
    x = x[:end]
    peak = int(np.argmax(np.abs(x)))
    fade = min(max(int(SR * (0.12 if kind == "shot" else 0.04)), int((end - peak) * 0.4)), end - peak)
    x[len(x) - fade:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fade))

    # loudness: turn down to the target, or reach it with gentle tanh saturation (as sfx.soft_clip), capped so it
    # never colours the recording
    def shaped(drive):
        y = np.tanh(x * drive) / np.tanh(drive) if drive > 1e-3 else x
        return y / np.abs(y).max() * 10 ** (peak_db / 20)

    y = shaped(0)
    if lufs(y) > target:
        return y * 10 ** ((target - lufs(y)) / 20)
    lo, hi = 0.0, max_drive
    for _ in range(10):
        mid = (lo + hi) / 2
        lo, hi = (mid, hi) if lufs(shaped(mid)) < target else (lo, mid)
    return shaped(lo)


# ------------------------------------------------------------------------------------------------------------------
# Driver
# ------------------------------------------------------------------------------------------------------------------

def load_manifest():
    if not os.path.exists(sfx.RECORDED):
        return {}
    with open(sfx.RECORDED, encoding="utf-8") as f:
        return json.load(f)


def save_manifest(manifest):
    with open(sfx.RECORDED, "w", encoding="utf-8") as f:
        json.dump(dict(sorted(manifest.items())), f, indent=2)
        f.write("\n")


def process(key, stem, redo, log, pick=None):
    kind, variants, candidates, *_ = CATALOGUE[stem]
    if redo and os.path.isdir(take_dir(stem)):
        shutil.rmtree(take_dir(stem))
    takes = cached_takes(stem)
    if pick:
        # hand-picked takes (by ear) override the scorer
        picks = [os.path.join(take_dir(stem), f"{t}.pcm") for t in pick]
        missing = [p for p in picks if p not in takes]
        if missing or len(picks) != variants:
            raise RuntimeError(f"--pick {stem} needs {variants} cached takes, got {pick}")
        return write_stem(stem, kind, picks, log)
    if len(takes) < candidates:
        takes += generate_takes(key, stem, candidates - len(takes))
    scored = [(score(p, kind), p) for p in takes]
    good = sorted(((s, p) for (s, r), p in scored if not r), reverse=True)
    if len(good) < variants:
        # one more round of takes before falling back to the least-bad rejects
        extra = generate_takes(key, stem, candidates)
        scored += [(score(p, kind), p) for p in extra]
        good = sorted(((s, p) for (s, r), p in scored if not r), reverse=True)
    picks = [p for _, p in good[:variants]]
    if len(picks) < variants:
        rejects = sorted(((s, p) for (s, r), p in scored if r), reverse=True)
        picks += [p for _, p in rejects[: variants - len(picks)]]
        log(f"  {stem}: only {len(good)} clean takes, using rejects for the rest")
    for (s, reasons), p in scored:
        log(f"  {stem} {os.path.basename(p)} score={s:5.1f} {'PICK' if p in picks else ''} {', '.join(reasons)}")
    return write_stem(stem, kind, picks, log)


def write_stem(stem, kind, picks, log):
    _, _, _, seconds, influence, max_len, target, prompt = CATALOGUE[stem]
    for i, p in enumerate(picks):
        sfx.write_ogg(clean(p, kind, max_len, target), os.path.join(SOUNDS, f"{stem}_{i + 1}.ogg"))
    log(f"  {stem}: wrote {', '.join(os.path.basename(p)[:-4] for p in picks)}")
    return stem, {"model": MODEL, "prompt": prompt, "seconds": seconds, "influence": influence,
                  "takes": [os.path.relpath(p, CACHE).replace(os.sep, "/") for p in picks]}


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--only", nargs="*", help="stems to process (default: every stem not yet in sfx_recorded.json)")
    ap.add_argument("--redo", action="store_true", help="discard cached takes and generate fresh ones")
    ap.add_argument("--reclean", action="store_true",
                    help="re-clean the takes recorded in sfx_recorded.json (after changing the cleaning); no API calls")
    ap.add_argument("--pick", nargs="*", default=[], metavar="STEM=TAKE,TAKE",
                    help="use these cached takes for a stem instead of the scorer's, e.g. gun/ak_fire=t00,t04,t07")
    ap.add_argument("--estimate", action="store_true", help="print the credits the run would spend and stop")
    ap.add_argument("--jobs", type=int, default=2, help="concurrent API requests (free plan allows 2)")
    a = ap.parse_args()

    manifest = load_manifest()
    picks = {s: t.split(",") for s, t in (p.split("=", 1) for p in a.pick)}
    if a.reclean:
        picks = {s: [os.path.basename(t)[:-4] for t in e["takes"]] for s, e in manifest.items()
                 if not a.only or s in a.only} | picks
    stems = a.only or list(picks) or [s for s in CATALOGUE if s not in manifest]
    unknown = [s for s in stems if s not in CATALOGUE]
    if unknown:
        sys.exit(f"unknown stems: {', '.join(unknown)}")
    cost = sum(max(0, CATALOGUE[s][2] - (0 if a.redo else len(cached_takes(s)))) * CATALOGUE[s][3] * CREDITS_PER_SECOND
               for s in stems if s not in picks)
    print(f"{len(stems)} stems, about {cost:.0f} credits of new takes (more if a stem needs a second round)")
    if a.estimate or not stems:
        return
    key = os.environ.get("ELEVENLABS_API_KEY", "").strip()
    if cost and not key:
        sys.exit("set ELEVENLABS_API_KEY")

    with concurrent.futures.ThreadPoolExecutor(a.jobs) as pool:
        futures = {pool.submit(process, key, s, a.redo, print, picks.get(s)): s for s in stems}
        for fut in concurrent.futures.as_completed(futures):
            try:
                stem, entry = fut.result()
            except Exception as e:  # keep the other stems going; the failed one stays procedural
                print(f"FAILED {futures[fut]}: {e}")
                continue
            manifest[stem] = entry
            save_manifest(manifest)
            print(f"done {stem}")


if __name__ == "__main__":
    main()
