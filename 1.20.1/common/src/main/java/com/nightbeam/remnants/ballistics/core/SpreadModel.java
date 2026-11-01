package com.nightbeam.remnants.ballistics.core;

/** Computes the current spread cone from weapon stats, aim state, movement and accumulated bloom. */
public final class SpreadModel {
    private SpreadModel() {}

    /**
     * @param speedFactor horizontal speed relative to walking speed (0 = still, 1 = walking)
     */
    public record Movement(float speedFactor, boolean sprinting, boolean crouching, boolean airborne) {
        public static final Movement STILL = new Movement(0f, false, false, false);
    }

    /**
     * @param adsProgress 0 = hip, 1 = fully aimed
     * @param bloom       accumulated bloom in degrees
     * @param accuracyMultiplier global config scale (1 = default)
     * @return cone half-angle in degrees
     */
    public static float spread(WeaponStats stats, float adsProgress, Movement movement, float bloom, float accuracyMultiplier) {
        float ads = clamp01(adsProgress);
        float hip = stats.get(Stat.HIP_SPREAD);
        float base = hip + (stats.get(Stat.ADS_SPREAD) - hip) * ads;
        float move = stats.get(Stat.MOVE_SPREAD) * Math.min(1.5f, Math.max(0f, movement.speedFactor())) * (1f - 0.6f * ads);
        if (movement.sprinting()) move += hip * 0.5f;
        if (movement.airborne()) move += hip + 2f;
        float total = base + move + Math.max(0f, bloom) * (1f - 0.5f * ads);
        if (movement.crouching() && !movement.airborne()) total *= 0.75f;
        return Math.max(0f, total * accuracyMultiplier);
    }

    /** Bloom after firing one more shot. */
    public static float addBloom(WeaponStats stats, float bloom) {
        return Math.min(stats.get(Stat.MAX_BLOOM), bloom + stats.get(Stat.BLOOM));
    }

    /** Bloom after {@code seconds} without firing. */
    public static float recoverBloom(WeaponStats stats, float bloom, float seconds) {
        return Math.max(0f, bloom - stats.get(Stat.BLOOM_RECOVERY) * seconds);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
