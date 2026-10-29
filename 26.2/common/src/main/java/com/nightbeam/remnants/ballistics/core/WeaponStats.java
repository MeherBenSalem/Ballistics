package com.nightbeam.remnants.ballistics.core;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable set of weapon statistics. Instances are cheap to derive: attachment and config modifiers produce
 * new instances via {@link #with(Iterable)}.
 */
public final class WeaponStats {
    private static final Stat[] STATS = Stat.values();
    private final float[] values;

    private WeaponStats(float[] values) {
        this.values = values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float get(Stat stat) {
        return values[stat.ordinal()];
    }

    public int getInt(Stat stat) {
        return Math.round(values[stat.ordinal()]);
    }

    /** Applies modifiers using the documented ADD-then-MULTIPLY order and clamps each stat to its legal range. */
    public WeaponStats with(Iterable<StatModifier> modifiers) {
        float[] add = new float[STATS.length];
        float[] mul = new float[STATS.length];
        boolean any = false;
        for (StatModifier m : modifiers) {
            any = true;
            if (m.op() == StatModifier.Op.ADD) add[m.stat().ordinal()] += m.value();
            else mul[m.stat().ordinal()] += m.value();
        }
        if (!any) return this;
        float[] out = new float[STATS.length];
        for (int i = 0; i < STATS.length; i++) {
            float v = (values[i] + add[i]) * Math.max(0f, 1f + mul[i]);
            out[i] = STATS[i].clamp(v);
        }
        return new WeaponStats(out);
    }

    /** Multiplies a single stat (used for global config scaling such as damage multipliers). */
    public WeaponStats scaled(Stat stat, float factor) {
        if (factor == 1f) return this;
        float[] out = values.clone();
        out[stat.ordinal()] = stat.clamp(out[stat.ordinal()] * factor);
        return new WeaponStats(out);
    }

    /** Replaces a single stat. */
    public WeaponStats withValue(Stat stat, float value) {
        float[] out = values.clone();
        out[stat.ordinal()] = stat.clamp(value);
        return new WeaponStats(out);
    }

    /** Ticks between shots for the current fire rate. */
    public double ticksPerShot() {
        return 1200.0 / Math.max(1f, get(Stat.FIRE_RATE));
    }

    public Map<Stat, Float> asMap() {
        Map<Stat, Float> map = new EnumMap<>(Stat.class);
        for (Stat s : STATS) map.put(s, values[s.ordinal()]);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WeaponStats other && Arrays.equals(values, other.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WeaponStats{");
        for (Stat s : STATS) sb.append(s.key()).append('=').append(values[s.ordinal()]).append(' ');
        return sb.append('}').toString();
    }

    public static final class Builder {
        private final float[] values = new float[STATS.length];

        private Builder() {
            // Sensible defaults so definitions only list what makes a weapon distinct.
            set(Stat.DAMAGE, 5f);
            set(Stat.PELLETS, 1);
            set(Stat.FIRE_RATE, 400);
            set(Stat.MAGAZINE, 15);
            set(Stat.RELOAD_TIME, 30);
            set(Stat.EMPTY_RELOAD_TIME, 38);
            set(Stat.FALLOFF_START, 24f);
            set(Stat.FALLOFF_END, 64f);
            set(Stat.MIN_DAMAGE, 0.5f);
            set(Stat.MAX_RANGE, 128f);
            set(Stat.HIP_SPREAD, 2.5f);
            set(Stat.ADS_SPREAD, 0.3f);
            set(Stat.MOVE_SPREAD, 1.5f);
            set(Stat.BLOOM, 0.4f);
            set(Stat.MAX_BLOOM, 4f);
            set(Stat.BLOOM_RECOVERY, 12f);
            set(Stat.RECOIL_VERTICAL, 1.5f);
            set(Stat.RECOIL_HORIZONTAL, 0.5f);
            set(Stat.RECOIL_RECOVERY, 0.6f);
            set(Stat.KICK, 1f);
            set(Stat.ADS_TIME, 5);
            set(Stat.ZOOM, 1.15f);
            set(Stat.MOVE_SPEED, 1f);
            set(Stat.ADS_MOVE_SPEED, 0.75f);
            set(Stat.MUZZLE_VELOCITY, 400f);
            set(Stat.GRAVITY, 0f);
            set(Stat.PENETRATION, 0.5f);
            set(Stat.HEADSHOT, 1f);
            set(Stat.SWAY, 1f);
            set(Stat.LOUDNESS, 64f);
            set(Stat.CHARGE_TIME, 0f);
        }

        public Builder set(Stat stat, float value) {
            values[stat.ordinal()] = stat.clamp(value);
            return this;
        }

        public WeaponStats build() {
            return new WeaponStats(values.clone());
        }
    }
}
