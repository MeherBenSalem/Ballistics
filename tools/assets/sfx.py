"""Procedural sound design: every sound is synthesised from noise, oscillators, filters and a small convolution
reverb (numpy only), then encoded to mono Ogg Vorbis with ffmpeg. All audio is original.

Gunshots are layered like a real recording: a sharp transient crack, a pitched-down body thump, a filtered
mechanical clack and an environmental tail. Profiles vary every layer so each weapon family sounds distinct.
"""
from __future__ import annotations

import os
import subprocess
import tempfile
import wave

import numpy as np

SR = 44100


# ------------------------------------------------------------------------------------------------------------------
# DSP toolkit
# ------------------------------------------------------------------------------------------------------------------

def t_axis(seconds):
    return np.arange(int(SR * seconds)) / SR


def noise(seconds, rng):
    return rng.uniform(-1, 1, int(SR * seconds))


def env_exp(seconds, decay, attack=0.0005):
    t = t_axis(seconds)
    a = np.clip(t / max(attack, 1e-6), 0, 1)
    return a * np.exp(-t / decay)


def spectral(x, gain_fn):
    """Zero-phase FFT filter: gain_fn(freqs) -> gains."""
    n = len(x)
    size = 1 << (n - 1).bit_length()
    spec = np.fft.rfft(x, size)
    f = np.fft.rfftfreq(size, 1 / SR)
    return np.fft.irfft(spec * gain_fn(f), size)[:n]


def lowpass(x, fc, order=2):
    return spectral(x, lambda f: 1 / np.sqrt(1 + (f / fc) ** (2 * order)))


def highpass(x, fc, order=2):
    return spectral(x, lambda f: 1 / np.sqrt(1 + (fc / np.maximum(f, 1e-3)) ** (2 * order)))


def bandpass(x, lo, hi, order=2):
    return highpass(lowpass(x, hi, order), lo, order)


def peak(x, fc, q, gain):
    """Resonant boost around fc (gain in linear amplitude added)."""
    return spectral(x, lambda f: 1 + gain / (1 + ((f - fc) / (fc / q)) ** 2))


def sweep_sine(seconds, f0, f1, curve=4.0, phase=0.0):
    t = t_axis(seconds)
    f = f1 + (f0 - f1) * np.exp(-t * curve / seconds)
    return np.sin(2 * np.pi * np.cumsum(f) / SR + phase)


def pad(x, seconds):
    n = int(SR * seconds)
    if len(x) >= n:
        return x[:n]
    return np.concatenate([x, np.zeros(n - len(x))])


def mix(*layers):
    n = max(len(layer) for layer in layers)
    out = np.zeros(n)
    for layer in layers:
        out[:len(layer)] += layer
    return out


def delay(x, seconds):
    return np.concatenate([np.zeros(int(SR * seconds)), x])


def reverb(x, rng, seconds=1.2, decay=0.35, lp=5000, wet=0.3, predelay=0.012):
    ir = noise(seconds, rng) * env_exp(seconds, decay, attack=0.002)
    ir = lowpass(ir, lp)
    ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
    n = len(x) + len(ir)
    size = 1 << (n - 1).bit_length()
    wet_sig = np.fft.irfft(np.fft.rfft(x, size) * np.fft.rfft(ir, size), size)[:n]
    return mix(x * (1 - wet * 0.5), delay(wet_sig * wet, predelay))


def ping(seconds, freq, decay, rng, partials=(1.0, 2.76, 5.4), detune=0.004):
    """Metallic modal ping (inharmonic partials)."""
    t = t_axis(seconds)
    out = np.zeros_like(t)
    for i, p in enumerate(partials):
        f = freq * p * (1 + rng.uniform(-detune, detune))
        out += np.sin(2 * np.pi * f * t + rng.uniform(0, 6.28)) * np.exp(-t / (decay / (1 + i * 0.7))) / (1 + i)
    return out


def click(rng, freq=4000, decay=0.004, seconds=0.05, body=0.0):
    x = highpass(noise(seconds, rng), freq * 0.5) * env_exp(seconds, decay)
    if body:
        x = mix(x, ping(seconds, freq * 0.4, decay * 3, rng) * body)
    return x


def friction(seconds, rng, lo=900, hi=5000, rough=0.5):
    n = noise(seconds, rng)
    grains = np.repeat(rng.uniform(1 - rough, 1, int(seconds * 400) + 1), int(SR / 400) + 1)[:len(n)]
    e = np.sin(np.pi * np.clip(t_axis(seconds) / seconds, 0, 1)) ** 0.7
    return bandpass(n * grains, lo, hi) * e


def fade_out(x, seconds=0.01):
    n = min(len(x), int(SR * seconds))
    if n > 0:
        x = x.copy()
        x[-n:] *= np.linspace(1, 0, n)
    return x


def normalize(x, peak_db=-1.0):
    m = np.max(np.abs(x)) + 1e-9
    return x / m * 10 ** (peak_db / 20)


def soft_clip(x, drive=1.5):
    return np.tanh(x * drive) / np.tanh(drive)


# ------------------------------------------------------------------------------------------------------------------
# Gunshots
# ------------------------------------------------------------------------------------------------------------------

PROFILES = {
    # crack: transient brightness (Hz), body: (start Hz, end Hz, decay s), boom gain, mech clack gain, tail decay, length
    "pistol": dict(crack=3800, body=(170, 60, 0.05), boom=0.8, mech=0.35, tail=0.25, length=0.9, drive=1.8),
    "magnum": dict(crack=3000, body=(140, 45, 0.09), boom=1.25, mech=0.3, tail=0.4, length=1.3, drive=2.4),
    "revolver": dict(crack=3200, body=(150, 48, 0.08), boom=1.1, mech=0.15, tail=0.35, length=1.2, drive=2.2, ring=1200),
    "smg": dict(crack=4600, body=(190, 70, 0.035), boom=0.6, mech=0.45, tail=0.18, length=0.6, drive=1.6),
    "pdw": dict(crack=5600, body=(220, 85, 0.03), boom=0.5, mech=0.5, tail=0.16, length=0.55, drive=1.5),
    "rifle": dict(crack=5200, body=(160, 52, 0.06), boom=1.0, mech=0.4, tail=0.35, length=1.1, drive=2.2),
    "ak": dict(crack=4200, body=(130, 44, 0.075), boom=1.2, mech=0.5, tail=0.4, length=1.2, drive=2.6),
    "burst": dict(crack=6000, body=(180, 60, 0.05), boom=0.85, mech=0.35, tail=0.3, length=0.9, drive=2.0),
    "shotgun": dict(crack=2600, body=(110, 38, 0.12), boom=1.5, mech=0.3, tail=0.5, length=1.4, drive=2.8),
    "pump": dict(crack=2400, body=(100, 35, 0.14), boom=1.6, mech=0.2, tail=0.55, length=1.5, drive=3.0),
    "sniper": dict(crack=6800, body=(120, 40, 0.1), boom=1.4, mech=0.2, tail=0.9, length=2.2, drive=2.8),
    "heavy_sniper": dict(crack=5200, body=(85, 30, 0.16), boom=1.9, mech=0.2, tail=1.2, length=2.8, drive=3.4),
    "lmg": dict(crack=5000, body=(150, 50, 0.06), boom=1.05, mech=0.55, tail=0.35, length=1.0, drive=2.3),
    "heavy_lmg": dict(crack=4300, body=(125, 42, 0.075), boom=1.3, mech=0.6, tail=0.42, length=1.2, drive=2.7),
}


def gunshot(profile, rng, distant=False, suppressed=False):
    p = PROFILES[profile]
    length = p["length"]
    crack = highpass(noise(0.08, rng), p["crack"] * 0.6) * env_exp(0.08, 0.006, attack=0.0002)
    crack = peak(crack, p["crack"], 2.0, 1.5)
    f0, f1, d = p["body"]
    body = sweep_sine(0.6, f0 * rng.uniform(0.95, 1.05), f1, curve=6) * env_exp(0.6, d, attack=0.0008)
    thump = lowpass(noise(0.4, rng), 300) * env_exp(0.4, d * 1.4) * 1.6
    mech = click(rng, freq=2500 * rng.uniform(0.9, 1.1), decay=0.01, seconds=0.08, body=0.6)
    mech = delay(mech, 0.012 + rng.uniform(0, 0.006))
    layers = [crack * 1.2, body * p["boom"], thump * p["boom"], mech * p["mech"]]
    if "ring" in p:
        layers.append(ping(0.4, p["ring"], 0.06, rng) * 0.15)
    x = mix(*layers)
    x = soft_clip(x, p["drive"])
    tail_lp = 2600 if not distant else 900
    x = reverb(x, rng, seconds=p["tail"] * 3.0, decay=p["tail"], lp=tail_lp, wet=0.55)
    if suppressed:
        x = lowpass(x, 1400) * 0.7 + highpass(noise(len(x) / SR, rng), 3000) * env_exp(len(x) / SR, 0.02) * 0.15
        x = mix(x, delay(mech * 0.5, 0.0))
    if distant:
        x = lowpass(x, 700, order=3)
        x = delay(x, 0.02)
    return fade_out(pad(normalize(x, -0.8), length + (1.2 if distant else 0)), 0.15)


def energy_shot(rng):
    t = t_axis(0.7)
    f = 900 * np.exp(-t * 7) + 120
    tone = np.sin(2 * np.pi * np.cumsum(f) / SR) * env_exp(0.7, 0.18)
    sub = np.sin(2 * np.pi * np.cumsum(60 + 40 * np.exp(-t * 5)) / SR) * env_exp(0.7, 0.25) * 0.8
    shimmer = bandpass(noise(0.7, rng), 3000, 9000) * env_exp(0.7, 0.05) * 0.5
    warble = 1 + 0.35 * np.sin(2 * np.pi * 34 * t)
    x = mix(tone * warble, sub, shimmer)
    x = reverb(x, rng, seconds=1.0, decay=0.3, lp=6000, wet=0.4)
    return fade_out(pad(normalize(x, -1.0), 1.0), 0.1)


def railgun_shot(rng):
    zap = highpass(noise(0.12, rng), 2500) * env_exp(0.12, 0.01) * 1.4
    crack = sweep_sine(0.9, 2400, 60, curve=9) * env_exp(0.9, 0.12)
    boom = lowpass(noise(1.5, rng), 180) * env_exp(1.5, 0.35) * 2.5
    hiss = bandpass(noise(1.6, rng), 4000, 12000) * env_exp(1.6, 0.4) * 0.3
    x = soft_clip(mix(zap, crack, boom, hiss), 2.2)
    x = reverb(x, rng, seconds=3.0, decay=0.9, lp=3500, wet=0.55)
    return fade_out(pad(normalize(x, -0.8), 2.6), 0.3)


def railgun_charge(rng):
    seconds = 1.1
    t = t_axis(seconds)
    f = 180 + 1700 * (t / seconds) ** 1.6
    whine = np.sin(2 * np.pi * np.cumsum(f) / SR) * (0.3 + 0.7 * t / seconds)
    whine2 = np.sin(2 * np.pi * np.cumsum(f * 1.5) / SR) * 0.3 * (t / seconds)
    crackle = bandpass(noise(seconds, rng), 2000, 8000) * (rng.uniform(0, 1, len(t)) > 0.985) * 1.5
    hum = np.sin(2 * np.pi * 60 * t) * 0.3
    x = mix(whine, whine2, crackle, hum)
    return fade_out(normalize(x, -3.0), 0.05)


# ------------------------------------------------------------------------------------------------------------------
# Mechanical handling
# ------------------------------------------------------------------------------------------------------------------

def metal_clack(rng, pitch=1.0, decay=0.03, body=0.6, seconds=0.25):
    c = click(rng, freq=3000 * pitch, decay=0.006, seconds=seconds)
    p = ping(seconds, 1400 * pitch * rng.uniform(0.95, 1.05), decay, rng) * body
    thud = lowpass(noise(seconds, rng), 600) * env_exp(seconds, 0.015) * 0.6
    return mix(c, p, thud)


def mech(name, rng):
    if name == "mag_out":
        x = mix(metal_clack(rng, 1.1, 0.02, 0.3, 0.2), delay(friction(0.12, rng, 1200, 6000) * 0.5, 0.02))
    elif name == "mag_in":
        x = mix(friction(0.08, rng, 800, 5000) * 0.4, delay(metal_clack(rng, 0.9, 0.04, 0.7, 0.3), 0.07),
                delay(click(rng, 5000, 0.003) * 0.6, 0.11))
    elif name == "slide_back":
        x = mix(friction(0.11, rng, 1500, 7000) * 0.6, delay(metal_clack(rng, 1.3, 0.02, 0.4, 0.15) * 0.7, 0.1))
    elif name == "slide_release":
        x = mix(metal_clack(rng, 1.2, 0.05, 0.9, 0.35), delay(friction(0.04, rng, 2000, 8000) * 0.3, 0.0))
    elif name == "bolt_open":
        x = mix(metal_clack(rng, 0.8, 0.03, 0.5, 0.2), delay(friction(0.16, rng, 900, 5000) * 0.7, 0.05))
    elif name == "bolt_close":
        x = mix(friction(0.14, rng, 900, 5000) * 0.6, delay(metal_clack(rng, 0.75, 0.06, 0.9, 0.35), 0.13),
                delay(click(rng, 3500, 0.004) * 0.6, 0.22))
    elif name == "pump_back":
        x = mix(friction(0.1, rng, 500, 4000, rough=0.7) * 0.8, delay(metal_clack(rng, 0.6, 0.04, 0.6, 0.25), 0.08))
    elif name == "pump_forward":
        x = mix(friction(0.08, rng, 500, 4000, rough=0.7) * 0.7, delay(metal_clack(rng, 0.55, 0.06, 0.9, 0.35) * 1.1, 0.07))
    elif name == "shell_insert":
        x = mix(friction(0.07, rng, 600, 3500) * 0.5, delay(metal_clack(rng, 0.9, 0.015, 0.25, 0.15), 0.05))
    elif name == "cylinder_open":
        x = mix(click(rng, 4500, 0.003) * 0.8, delay(friction(0.12, rng, 1200, 6000) * 0.4, 0.02),
                delay(ping(0.4, 2100, 0.12, rng) * 0.25, 0.12))
    elif name == "cylinder_close":
        x = mix(metal_clack(rng, 1.0, 0.07, 1.0, 0.4), delay(ping(0.5, 2600, 0.15, rng) * 0.2, 0.0))
    elif name == "cell_out":
        t = t_axis(0.3)
        chirp = np.sin(2 * np.pi * np.cumsum(1800 - 1400 * t / 0.3) / SR) * env_exp(0.3, 0.08) * 0.4
        x = mix(chirp, click(rng, 3000, 0.004, body=0.3), delay(friction(0.1, rng, 1000, 5000) * 0.4, 0.03))
    elif name == "cell_in":
        t = t_axis(0.35)
        chirp = np.sin(2 * np.pi * np.cumsum(500 + 1800 * t / 0.35) / SR) * env_exp(0.35, 0.12) * 0.4
        x = mix(metal_clack(rng, 1.1, 0.03, 0.5, 0.2), delay(chirp, 0.05), delay(click(rng, 6000, 0.002) * 0.4, 0.3))
    elif name == "cover_open":
        x = mix(metal_clack(rng, 0.7, 0.04, 0.6, 0.25), delay(friction(0.12, rng, 400, 3000) * 0.4, 0.04))
    elif name == "cover_close":
        x = mix(metal_clack(rng, 0.5, 0.09, 1.1, 0.45) * 1.2, delay(click(rng, 3000, 0.004) * 0.5, 0.05))
    elif name == "casing_drop":
        x = mix(*[delay(ping(0.25, rng.uniform(4500, 6500), 0.04, rng, partials=(1.0, 2.4, 3.9)) * a, d)
                  for d, a in ((0.0, 0.6), (0.11, 0.35), (0.19, 0.2), (0.24, 0.1))])
    elif name == "shell_drop":
        x = mix(*[delay(mix(lowpass(noise(0.08, rng), 1600) * env_exp(0.08, 0.012), ping(0.1, 700, 0.02, rng) * 0.3) * a, d)
                  for d, a in ((0.0, 0.8), (0.14, 0.4), (0.22, 0.2))])
    elif name == "dry_fire":
        x = mix(click(rng, 4000, 0.003), delay(metal_clack(rng, 1.4, 0.015, 0.3, 0.1) * 0.5, 0.01))
    elif name == "fire_mode":
        x = mix(click(rng, 5000, 0.002) * 0.7, delay(click(rng, 3500, 0.003, body=0.4) * 0.6, 0.035))
    elif name == "jammed":
        x = mix(lowpass(noise(0.2, rng), 700) * env_exp(0.2, 0.03), metal_clack(rng, 0.5, 0.02, 0.4, 0.2) * 0.6)
    elif name == "equip":
        x = mix(friction(0.25, rng, 300, 2500, rough=0.8) * 0.5, delay(metal_clack(rng, 0.9, 0.03, 0.5, 0.2) * 0.6, 0.18))
    elif name in ("aim_in", "aim_out"):
        x = friction(0.18 if name == "aim_in" else 0.14, rng, 250, 2200, rough=0.9) * 0.6
        if name == "aim_in":
            x = mix(x, delay(click(rng, 2500, 0.003) * 0.25, 0.14))
    elif name == "inspect":
        x = mix(friction(0.4, rng, 250, 2000, rough=0.9) * 0.45, delay(metal_clack(rng, 1.0, 0.02, 0.3, 0.15) * 0.4, 0.3))
    else:
        raise ValueError(name)
    x = reverb(x, rng, seconds=0.4, decay=0.08, lp=6000, wet=0.18, predelay=0.004)
    return fade_out(normalize(x, -2.0), 0.02)


# ------------------------------------------------------------------------------------------------------------------
# Impacts, UI, blocks, ambience
# ------------------------------------------------------------------------------------------------------------------

def impact(kind, rng):
    if kind == "stone":
        x = mix(highpass(noise(0.15, rng), 1500) * env_exp(0.15, 0.012) * 1.2,
                *[delay(bandpass(noise(0.05, rng), 2000, 7000) * env_exp(0.05, 0.006) * rng.uniform(0.15, 0.4), rng.uniform(0.02, 0.25))
                  for _ in range(6)])
    elif kind == "metal":
        x = mix(click(rng, 5000, 0.002), ping(0.6, rng.uniform(1800, 2600), 0.12, rng, partials=(1.0, 2.3, 3.7, 5.1)) * 0.7)
    elif kind == "wood":
        x = mix(lowpass(noise(0.15, rng), 1800) * env_exp(0.15, 0.02), ping(0.2, rng.uniform(350, 480), 0.03, rng, partials=(1.0, 2.1)) * 0.6,
                delay(bandpass(noise(0.1, rng), 1500, 5000) * env_exp(0.1, 0.02) * 0.3, 0.02))
    elif kind == "glass":
        x = mix(highpass(noise(0.3, rng), 3500) * env_exp(0.3, 0.05),
                *[delay(ping(0.3, rng.uniform(3000, 7000), 0.05, rng) * rng.uniform(0.1, 0.3), rng.uniform(0, 0.2)) for _ in range(8)])
    elif kind == "dirt":
        x = mix(lowpass(noise(0.2, rng), 900) * env_exp(0.2, 0.03) * 1.3, delay(bandpass(noise(0.25, rng), 800, 3000) * env_exp(0.25, 0.06) * 0.3, 0.03))
    elif kind == "water":
        t = t_axis(0.4)
        bloop = np.sin(2 * np.pi * np.cumsum(300 + 900 * np.exp(-t * 20)) / SR) * env_exp(0.4, 0.04) * 0.5
        x = mix(highpass(noise(0.4, rng), 1200) * env_exp(0.4, 0.07) * 0.8, bloop)
    elif kind == "flesh":
        x = mix(lowpass(noise(0.18, rng), 700) * env_exp(0.18, 0.025) * 1.4, delay(bandpass(noise(0.08, rng), 600, 2500) * env_exp(0.08, 0.01) * 0.5, 0.005))
    elif kind == "energy":
        t = t_axis(0.4)
        zap = np.sin(2 * np.pi * np.cumsum(2000 * np.exp(-t * 12) + 200) / SR) * env_exp(0.4, 0.06)
        x = mix(zap * 0.7, highpass(noise(0.4, rng), 3000) * env_exp(0.4, 0.03) * 0.5)
    elif kind == "ricochet":
        t = t_axis(0.6)
        f = 3200 * np.exp(-t * 1.8) + 900
        whine = np.sin(2 * np.pi * np.cumsum(f) / SR) * env_exp(0.6, 0.18, attack=0.01)
        x = mix(click(rng, 5000, 0.002), delay(whine * 0.6, 0.01))
    elif kind == "whiz":
        t = t_axis(0.35)
        e = np.exp(-((t - 0.12) / 0.05) ** 2)
        x = bandpass(noise(0.35, rng), 1200, 6000) * e * 1.2
        x = mix(x, np.sin(2 * np.pi * np.cumsum(1400 - 900 * t / 0.35) / SR) * e * 0.3)
    else:
        raise ValueError(kind)
    x = reverb(x, rng, seconds=0.6, decay=0.12, lp=5000, wet=0.2, predelay=0.005)
    return fade_out(normalize(x, -2.0), 0.03)


def ui(kind, rng):
    t = t_axis(0.25)
    if kind == "hitmarker":
        x = mix(click(rng, 6000, 0.0015) * 0.6, np.sin(2 * np.pi * 2400 * t) * env_exp(0.25, 0.012) * 0.5)
    elif kind == "headshot":
        x = mix(click(rng, 6000, 0.0015) * 0.5, ping(0.4, 1760, 0.12, rng, partials=(1.0, 2.0, 3.0), detune=0) * 0.6)
    elif kind == "kill":
        a = np.sin(2 * np.pi * 1320 * t) * env_exp(0.25, 0.05)
        b = delay(np.sin(2 * np.pi * 1980 * t) * env_exp(0.25, 0.08), 0.07)
        x = mix(a * 0.5, b * 0.5, click(rng, 5000, 0.002) * 0.4)
    else:
        raise ValueError(kind)
    return fade_out(normalize(x, -4.0), 0.02)


def block_sound(kind, rng):
    if kind == "gunsmith":
        x = mix(metal_clack(rng, 0.8, 0.05, 0.8, 0.3), delay(friction(0.2, rng, 800, 4000) * 0.4, 0.08),
                delay(metal_clack(rng, 1.2, 0.03, 0.5, 0.2) * 0.6, 0.3))
    else:
        x = mix(lowpass(noise(0.3, rng), 500) * env_exp(0.3, 0.05) * 1.2, metal_clack(rng, 0.6, 0.08, 0.9, 0.4),
                delay(click(rng, 4000, 0.003, body=0.4) * 0.6, 0.25))
    x = reverb(x, rng, seconds=0.6, decay=0.15, lp=5000, wet=0.25)
    return fade_out(normalize(x, -2.0), 0.03)


def void_hum(rng):
    seconds = 2.0
    t = t_axis(seconds)
    base = np.sin(2 * np.pi * 55 * t) + 0.5 * np.sin(2 * np.pi * 110.5 * t) + 0.25 * np.sin(2 * np.pi * 220 * t + 1)
    wobble = 1 + 0.3 * np.sin(2 * np.pi * 3 * t)
    airy = bandpass(noise(seconds, rng), 400, 1500) * 0.15
    x = (base * wobble + airy)
    # Seamless loop: crossfade end into start.
    n = int(SR * 0.1)
    x[:n] = x[:n] * np.linspace(0, 1, n) + x[-n:] * np.linspace(1, 0, n)
    return normalize(x[:-n], -6.0)


# ------------------------------------------------------------------------------------------------------------------
# Catalogue and encoding
# ------------------------------------------------------------------------------------------------------------------

FIRE_PROFILES = ["pistol", "magnum", "revolver", "smg", "pdw", "rifle", "ak", "burst", "shotgun", "pump", "sniper",
                 "heavy_sniper", "lmg", "heavy_lmg", "voidcaster", "railgun"]

MECH = ["mag_out", "mag_in", "slide_back", "slide_release", "bolt_open", "bolt_close", "pump_back", "pump_forward",
        "shell_insert", "cylinder_open", "cylinder_close", "cell_out", "cell_in", "cover_open", "cover_close",
        "casing_drop", "shell_drop", "dry_fire", "fire_mode", "jammed", "equip", "aim_in", "aim_out", "inspect"]

IMPACTS = ["stone", "metal", "wood", "glass", "dirt", "water", "flesh", "energy", "ricochet", "whiz"]


def catalogue():
    """[(event name, file stem, variant count, generator(rng), subtitle)]"""
    out = []
    for p in FIRE_PROFILES:
        if p == "voidcaster":
            gen = energy_shot
        elif p == "railgun":
            gen = railgun_shot
        else:
            gen = (lambda prof: (lambda rng: gunshot(prof, rng)))(p)
        out.append((f"gun.{p}.fire", f"gun/{p}_fire", 3, gen, "Gunshot" if p not in ("voidcaster", "railgun") else
                    ("Voidcaster fires" if p == "voidcaster" else "Railgun fires")))
    out.append(("gun.suppressed.fire", "gun/suppressed_fire", 3, lambda rng: gunshot("rifle", rng, suppressed=True), "Suppressed shot"))
    out.append(("gun.distant.fire", "gun/distant_fire", 3, lambda rng: gunshot("rifle", rng, distant=True), "Distant gunfire"))
    out.append(("gun.railgun.charge", "gun/railgun_charge", 1, railgun_charge, "Railgun charges"))
    subtitles = {
        "mag_out": "Magazine removed", "mag_in": "Magazine inserted", "slide_back": "Slide racked", "slide_release": "Slide released",
        "bolt_open": "Bolt opens", "bolt_close": "Bolt closes", "pump_back": "Shotgun pumped", "pump_forward": "Shotgun pumped",
        "shell_insert": "Round loaded", "cylinder_open": "Cylinder opens", "cylinder_close": "Cylinder closes",
        "cell_out": "Energy cell ejected", "cell_in": "Energy cell inserted", "cover_open": "Feed cover opens",
        "cover_close": "Feed cover closes", "casing_drop": "Casing clinks", "shell_drop": "Shell drops", "dry_fire": "Dry fire",
        "fire_mode": "Fire mode switched", "jammed": "Weapon jams", "equip": "Weapon drawn", "aim_in": "Aiming",
        "aim_out": "Aim lowered", "inspect": "Weapon inspected",
    }
    for m in MECH:
        out.append((f"gun.{m}", f"gun/{m}", 2, (lambda name: (lambda rng: mech(name, rng)))(m), subtitles[m]))
    impact_sub = {"stone": "Bullet hits stone", "metal": "Bullet hits metal", "wood": "Bullet hits wood", "glass": "Bullet hits glass",
                  "dirt": "Bullet hits dirt", "water": "Bullet hits water", "flesh": "Bullet hits flesh", "energy": "Energy impact",
                  "ricochet": "Ricochet", "whiz": "Bullet whizzes by"}
    for k in IMPACTS:
        out.append((f"impact.{k}", f"impact/{k}", 3, (lambda name: (lambda rng: impact(name, rng)))(k), impact_sub[k]))
    for k, sub in (("hitmarker", "Hit confirmed"), ("headshot", "Headshot"), ("kill", "Target eliminated")):
        out.append((f"ui.{k}", f"ui/{k}", 1, (lambda name: (lambda rng: ui(name, rng)))(k), sub))
    out.append(("block.gunsmith.use", "block/gunsmith_use", 2, lambda rng: block_sound("gunsmith", rng), "Gunsmith workbench used"))
    out.append(("block.ammo_press.use", "block/ammo_press_use", 2, lambda rng: block_sound("press", rng), "Ammunition press used"))
    out.append(("entity.void_orb.hum", "entity/void_orb_hum", 1, void_hum, "Void orb hums"))
    return out


def write_ogg(samples, path, quality=5):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    pcm = (np.clip(samples, -1, 1) * 32767).astype("<i2")
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        wav_path = tmp.name
    try:
        with wave.open(wav_path, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(SR)
            w.writeframes(pcm.tobytes())
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav_path, "-c:a", "libvorbis", "-q:a", str(quality),
                        "-map_metadata", "-1", "-fflags", "+bitexact", "-flags:a", "+bitexact", path], check=True)
    finally:
        os.unlink(wav_path)


def generate(assets_dir, ns, seed=20260):
    """Writes sounds/*.ogg and returns the sounds.json dict and subtitle translations."""
    sounds_json = {}
    subtitles = {}
    for event, stem, variants, gen, subtitle in catalogue():
        entries = []
        for v in range(variants):
            rng = np.random.default_rng(seed + sum(map(ord, event)) * 31 + v)
            samples = gen(rng)
            name = f"{stem}_{v + 1}" if variants > 1 else stem
            write_ogg(samples, os.path.join(assets_dir, "sounds", name + ".ogg"))
            entries.append({"name": f"{ns}:{name}"})
        key = f"subtitles.{ns}.{event}"
        sounds_json[event] = {"sounds": entries, "subtitle": key}
        subtitles[key] = subtitle
    return sounds_json, subtitles
