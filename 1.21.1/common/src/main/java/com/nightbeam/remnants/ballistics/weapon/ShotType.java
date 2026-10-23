package com.nightbeam.remnants.ballistics.weapon;

/** How a shot travels. */
public enum ShotType {
    /** Instant server-side trace with optional bullet drop, penetration and ricochet. */
    HITSCAN,
    /** A simulated projectile entity (slow energy orbs). */
    PROJECTILE
}
