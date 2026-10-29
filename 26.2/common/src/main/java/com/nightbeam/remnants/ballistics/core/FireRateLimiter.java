package com.nightbeam.remnants.ballistics.core;

/**
 * Server-side fire rate validation using a bounded leaky bucket.
 * <p>
 * Packets from a legitimate client arrive bunched because of network jitter, so the limiter tolerates a small burst:
 * {@code jitterTicks} of early arrival, plus idle credit of up to {@code bankedShots} shots that is never worth more
 * than {@code maxBankTicks} ticks. The tick cap keeps slow, hard-hitting weapons (bolt actions, the railgun) from
 * firing two shots in one tick after a pause, while fast weapons still absorb a late packet. Over any window of
 * {@code n} ticks no more than {@code n / interval + min(bankedShots, maxBankTicks / interval) + jitterTicks / interval + 1}
 * shots are accepted.
 */
public final class FireRateLimiter {
    private final double jitterTicks;
    private final double bankedShots;
    private final double maxBankTicks;
    private double nextAllowedTick = Double.NEGATIVE_INFINITY;

    public FireRateLimiter(double jitterTicks, double bankedShots, double maxBankTicks) {
        if (jitterTicks < 0 || bankedShots < 0 || maxBankTicks < 0) throw new IllegalArgumentException("negative tolerance");
        this.jitterTicks = jitterTicks;
        this.bankedShots = bankedShots;
        this.maxBankTicks = maxBankTicks;
    }

    /** Limiter whose idle credit is limited only by {@code bankedShots}. */
    public FireRateLimiter(double jitterTicks, double bankedShots) {
        this(jitterTicks, bankedShots, Double.POSITIVE_INFINITY);
    }

    /**
     * @param nowTick  current server game time in ticks
     * @param interval ticks between shots for the weapon (1200 / rpm)
     * @return true if the shot is accepted (and consumed), false if it arrives too early
     */
    public boolean tryFire(long nowTick, double interval) {
        double earliest = Math.max(nextAllowedTick, nowTick - Math.min(bankedShots * interval, maxBankTicks));
        if (earliest > nowTick + jitterTicks) return false;
        nextAllowedTick = earliest + interval;
        return true;
    }

    /** Forces a delay, e.g. after a reload or weapon switch. */
    public void delayUntil(double tick) {
        nextAllowedTick = Math.max(nextAllowedTick, tick);
    }

    public void reset() {
        nextAllowedTick = Double.NEGATIVE_INFINITY;
    }
}
