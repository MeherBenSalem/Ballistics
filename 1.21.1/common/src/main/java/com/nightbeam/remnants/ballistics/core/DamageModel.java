package com.nightbeam.remnants.ballistics.core;

/** Distance falloff and penetration damage rules. */
public final class DamageModel {
    private DamageModel() {}

    /** Linear falloff from 1.0 at {@code start} to {@code minMultiplier} at {@code end}. */
    public static float falloff(double distance, float start, float end, float minMultiplier) {
        if (distance <= start) return 1f;
        if (end <= start || distance >= end) return minMultiplier;
        double t = (distance - start) / (end - start);
        return (float) (1.0 - t * (1.0 - minMultiplier));
    }

    public static float falloff(WeaponStats stats, double distance) {
        return falloff(distance, stats.get(Stat.FALLOFF_START), stats.get(Stat.FALLOFF_END), stats.get(Stat.MIN_DAMAGE));
    }

    /**
     * Damage retained after passing through an obstacle. Penetration power is spent by the obstacle's resistance;
     * the remaining fraction of power scales the damage.
     *
     * @return remaining penetration power (negative = the bullet stops)
     */
    public static float spendPenetration(float power, float resistance) {
        return power - resistance;
    }

    /** Damage multiplier applied after each penetration (diminishing returns). */
    public static float penetrationDamageFactor(float remainingPower, float initialPower) {
        if (initialPower <= 0f) return 0f;
        float ratio = Math.max(0f, Math.min(1f, remainingPower / initialPower));
        return 0.35f + 0.5f * ratio;
    }
}
