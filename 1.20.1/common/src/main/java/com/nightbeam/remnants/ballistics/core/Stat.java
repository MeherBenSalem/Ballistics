package com.nightbeam.remnants.ballistics.core;

/**
 * Every tunable weapon statistic. Stored as floats in {@link WeaponStats}; integral stats are rounded on read.
 * <p>
 * This package is deliberately free of Minecraft imports so it can be unit tested without the game.
 */
public enum Stat {
    /** Damage per bullet/pellet in half-hearts (vanilla health points). */
    DAMAGE("damage", true, Format.DECIMAL, 0f, 200f),
    /** Projectiles per shot (shotguns fire several pellets). */
    PELLETS("pellets", true, Format.INTEGER, 1f, 32f),
    /** Rounds per minute. */
    FIRE_RATE("fire_rate", true, Format.INTEGER, 1f, 2400f),
    /** Magazine capacity (not counting a chambered round). */
    MAGAZINE("magazine", true, Format.INTEGER, 1f, 500f),
    /** Tactical reload duration in ticks (rounds left in the magazine), or per-shell insert time for single-load weapons. */
    RELOAD_TIME("reload_time", false, Format.TICKS, 1f, 400f),
    /** Empty reload duration in ticks (includes chambering), or start+end overhead for single-load weapons. */
    EMPTY_RELOAD_TIME("empty_reload_time", false, Format.TICKS, 1f, 400f),
    /** Distance in blocks where damage falloff begins. */
    FALLOFF_START("falloff_start", true, Format.BLOCKS, 0f, 512f),
    /** Distance in blocks where damage reaches its minimum multiplier. */
    FALLOFF_END("falloff_end", true, Format.BLOCKS, 1f, 1024f),
    /** Damage multiplier at and beyond {@link #FALLOFF_END}. */
    MIN_DAMAGE("min_damage", true, Format.PERCENT, 0f, 1f),
    /** Maximum hitscan range in blocks. */
    MAX_RANGE("max_range", true, Format.BLOCKS, 1f, 1024f),
    /** Hip-fire spread cone half-angle in degrees. */
    HIP_SPREAD("hip_spread", false, Format.DEGREES, 0f, 45f),
    /** Aimed spread cone half-angle in degrees. */
    ADS_SPREAD("ads_spread", false, Format.DEGREES, 0f, 45f),
    /** Extra spread in degrees while moving at full walk speed. */
    MOVE_SPREAD("move_spread", false, Format.DEGREES, 0f, 45f),
    /** Spread bloom added per shot in degrees. */
    BLOOM("bloom", false, Format.DEGREES, 0f, 10f),
    /** Maximum accumulated bloom in degrees. */
    MAX_BLOOM("max_bloom", false, Format.DEGREES, 0f, 45f),
    /** Bloom recovery in degrees per second. */
    BLOOM_RECOVERY("bloom_recovery", true, Format.DEGREES, 0f, 200f),
    /** Vertical camera recoil per shot in degrees. */
    RECOIL_VERTICAL("recoil_vertical", false, Format.DEGREES, 0f, 30f),
    /** Maximum horizontal camera recoil per shot in degrees (random sign). */
    RECOIL_HORIZONTAL("recoil_horizontal", false, Format.DEGREES, 0f, 30f),
    /** Fraction of camera recoil recovered automatically after firing stops. */
    RECOIL_RECOVERY("recoil_recovery", true, Format.PERCENT, 0f, 1f),
    /** Visual weapon kick (viewmodel only), arbitrary units. */
    KICK("kick", false, Format.DECIMAL, 0f, 10f),
    /** Time to aim down sights in ticks. */
    ADS_TIME("ads_time", false, Format.TICKS, 1f, 60f),
    /** Field of view divisor while aiming (1.0 = no zoom). Optics override this. */
    ZOOM("zoom", true, Format.MULTIPLIER, 1f, 16f),
    /** Movement speed multiplier while held. */
    MOVE_SPEED("move_speed", true, Format.PERCENT, 0.1f, 1.5f),
    /** Additional movement speed multiplier while aiming. */
    ADS_MOVE_SPEED("ads_move_speed", true, Format.PERCENT, 0.1f, 1.5f),
    /** Bullet speed in blocks per second (drives bullet drop). */
    MUZZLE_VELOCITY("muzzle_velocity", true, Format.DECIMAL, 10f, 2000f),
    /** Bullet drop acceleration in blocks per second squared (0 disables drop). */
    GRAVITY("gravity", false, Format.DECIMAL, 0f, 100f),
    /** Penetration power: soft blocks / entities a bullet can pass through. */
    PENETRATION("penetration", true, Format.DECIMAL, 0f, 10f),
    /** Weapon-specific headshot multiplier (multiplied with the global config multiplier). */
    HEADSHOT("headshot", true, Format.MULTIPLIER, 0f, 10f),
    /** Weapon sway amplitude while aiming, arbitrary units. */
    SWAY("sway", false, Format.DECIMAL, 0f, 10f),
    /** Distance in blocks the gunshot can be heard. */
    LOUDNESS("loudness", false, Format.BLOCKS, 4f, 256f),
    /** Charge time in ticks for charged weapons (0 = instant). */
    CHARGE_TIME("charge_time", false, Format.TICKS, 0f, 200f);

    private final String key;
    private final boolean higherIsBetter;
    private final Format format;
    private final float min;
    private final float max;

    Stat(String key, boolean higherIsBetter, Format format, float min, float max) {
        this.key = key;
        this.higherIsBetter = higherIsBetter;
        this.format = format;
        this.min = min;
        this.max = max;
    }

    public String key() {
        return key;
    }

    public boolean higherIsBetter() {
        return higherIsBetter;
    }

    public Format format() {
        return format;
    }

    public float clamp(float value) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean isIntegral() {
        return format == Format.INTEGER || format == Format.TICKS;
    }

    public static Stat byKey(String key) {
        for (Stat s : values()) {
            if (s.key.equals(key)) return s;
        }
        return null;
    }

    /** How a stat is rendered in tooltips and the gunsmith preview. */
    public enum Format {
        DECIMAL, INTEGER, TICKS, BLOCKS, DEGREES, PERCENT, MULTIPLIER
    }
}
