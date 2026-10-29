package com.nightbeam.remnants.ballistics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireRateLimiterTest {
    @Test
    void acceptsSteadyFireAtTheWeaponRate() {
        FireRateLimiter limiter = new FireRateLimiter(1.0, 1.0);
        double interval = 1200.0 / 600.0; // 600 rpm -> one shot every 2 ticks
        for (long tick = 0; tick < 200; tick += 2) {
            assertTrue(limiter.tryFire(tick, interval), "shot at tick " + tick);
        }
    }

    @Test
    void rejectsSpamFasterThanTheRate() {
        FireRateLimiter limiter = new FireRateLimiter(1.0, 1.0);
        double interval = 1200.0 / 300.0; // 4 ticks per shot
        int accepted = 0;
        for (long tick = 0; tick < 400; tick++) {
            for (int i = 0; i < 5; i++) {
                if (limiter.tryFire(tick, interval)) accepted++;
            }
        }
        // 400 ticks / 4 = 100 legitimate shots plus a bounded burst allowance.
        assertTrue(accepted <= 100 + 3, "accepted " + accepted);
        assertTrue(accepted >= 99, "accepted " + accepted);
    }

    @Test
    void toleratesJitteredArrivalOfLegitimateShots() {
        FireRateLimiter limiter = new FireRateLimiter(1.0, 1.0);
        double interval = 2.0;
        // Two shots arrive in the same tick after a one-tick network stall, then the stream continues normally.
        assertTrue(limiter.tryFire(10, interval));
        assertTrue(limiter.tryFire(13, interval));
        assertTrue(limiter.tryFire(13, interval));
        assertTrue(limiter.tryFire(16, interval));
    }

    @Test
    void idleCreditIsBounded() {
        FireRateLimiter limiter = new FireRateLimiter(0.0, 2.0);
        double interval = 10.0;
        int accepted = 0;
        for (int i = 0; i < 10; i++) if (limiter.tryFire(1000, interval)) accepted++;
        assertEquals(3, accepted, "banked credit must not grow without bound while idle");
    }

    @Test
    void slowWeaponsCannotDoubleTapFromIdleCredit() {
        FireRateLimiter limiter = new FireRateLimiter(2.0, 1.0, 4.0);
        double interval = 1200.0 / 48.0; // bolt-action sniper: 25 ticks per shot
        assertTrue(limiter.tryFire(1000, interval));
        assertFalse(limiter.tryFire(1000, interval), "second shot in the same tick after idling");
        assertFalse(limiter.tryFire(1010, interval), "second shot well before the bolt could cycle");
    }

    @Test
    void cappedCreditStillAbsorbsALatePacket() {
        FireRateLimiter limiter = new FireRateLimiter(2.0, 1.0, 4.0);
        double interval = 25.0;
        // The client fired at 1000 and 1025; the first packet was held up for three ticks.
        assertTrue(limiter.tryFire(1003, interval));
        assertTrue(limiter.tryFire(1025, interval));
    }

    @Test
    void fastWeaponsKeepTheirFullBurstAllowance() {
        FireRateLimiter limiter = new FireRateLimiter(1.0, 1.0, 4.0);
        double interval = 2.0;
        assertTrue(limiter.tryFire(10, interval));
        assertTrue(limiter.tryFire(13, interval));
        assertTrue(limiter.tryFire(13, interval));
    }

    @Test
    void delayBlocksUntilTheGivenTick() {
        FireRateLimiter limiter = new FireRateLimiter(0.0, 0.0);
        limiter.delayUntil(50);
        assertFalse(limiter.tryFire(40, 1.0));
        assertTrue(limiter.tryFire(50, 1.0));
    }
}
