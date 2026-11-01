package com.nightbeam.remnants.ballistics.core;

import java.util.Objects;

/**
 * A single change to a {@link Stat}. {@link Op#ADD} values are summed first, then all {@link Op#MULTIPLY}
 * percentages are summed and applied once: {@code (base + sum(add)) * (1 + sum(multiply))}.
 * Summing percentages (rather than chaining them) keeps stacked attachments predictable.
 */
public record StatModifier(Stat stat, Op op, float value) {
    public StatModifier {
        Objects.requireNonNull(stat, "stat");
        Objects.requireNonNull(op, "op");
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Non-finite modifier for " + stat);
    }

    public static StatModifier add(Stat stat, float value) {
        return new StatModifier(stat, Op.ADD, value);
    }

    /** @param percent e.g. {@code -0.15f} for -15% */
    public static StatModifier percent(Stat stat, float percent) {
        return new StatModifier(stat, Op.MULTIPLY, percent);
    }

    /** Whether this modifier makes the weapon better according to {@link Stat#higherIsBetter()}. */
    public boolean isBeneficial() {
        boolean increases = value > 0;
        return increases == stat.higherIsBetter();
    }

    public enum Op {
        ADD, MULTIPLY
    }
}
