package com.nightbeam.remnants.ballistics.core;

/**
 * Deterministic, platform-independent random stream (SplitMix64) seeded from a per-player secret and the shot
 * sequence number. Client and server derive identical spread for the same shot, so predicted tracers match the
 * authoritative server trace, while a client cannot pick favourable seeds because the server owns the sequence.
 */
public final class ShotRandom {
    private long state;

    public ShotRandom(long seed) {
        this.state = seed;
    }

    /** Derives the stream for one shot. */
    public static ShotRandom forShot(long playerSeed, int shotSequence) {
        return new ShotRandom(mix(playerSeed ^ (0x9E3779B97F4A7C15L * (shotSequence + 1L))));
    }

    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public long nextLong() {
        state += 0x9E3779B97F4A7C15L;
        return mix(state);
    }

    /** Uniform in [0, 1). */
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }

    /** Uniform in [-1, 1). */
    public float nextSigned() {
        return nextFloat() * 2f - 1f;
    }

    public boolean nextBoolean() {
        return (nextLong() & 1L) == 1L;
    }
}
