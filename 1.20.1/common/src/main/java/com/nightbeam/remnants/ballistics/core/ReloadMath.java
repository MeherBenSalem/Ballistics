package com.nightbeam.remnants.ballistics.core;

/** Magazine bookkeeping shared by client prediction and the authoritative server reload. */
public final class ReloadMath {
    private ReloadMath() {}

    /** Rounds moved from reserve into the weapon. */
    public static int roundsToLoad(int current, int capacity, int reserve) {
        return Math.max(0, Math.min(capacity - current, reserve));
    }

    /** Effective capacity after a reload: closed-bolt weapons keep a round in the chamber on a tactical reload. */
    public static int reloadCapacity(int magazine, boolean hasChamber, int current) {
        return magazine + (hasChamber && current > 0 ? 1 : 0);
    }

    /** Whether a reload would load anything at all. */
    public static boolean canReload(int current, int magazine, boolean hasChamber, int reserve) {
        return reserve > 0 && current < reloadCapacity(magazine, hasChamber, current);
    }
}
