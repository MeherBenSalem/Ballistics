package com.nightbeam.remnants.ballistics.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaponStatsTest {
    @Test
    void addsThenMultipliesAndSumsPercentages() {
        WeaponStats base = WeaponStats.builder().set(Stat.MAGAZINE, 30).set(Stat.RECOIL_VERTICAL, 2f).build();
        WeaponStats mod = base.with(List.of(
                StatModifier.add(Stat.MAGAZINE, 10),
                StatModifier.percent(Stat.MAGAZINE, 0.5f),
                StatModifier.percent(Stat.RECOIL_VERTICAL, -0.2f),
                StatModifier.percent(Stat.RECOIL_VERTICAL, -0.1f)));
        assertEquals(60, mod.getInt(Stat.MAGAZINE));
        assertEquals(1.4f, mod.get(Stat.RECOIL_VERTICAL), 1e-5);
    }

    @Test
    void clampsToLegalRanges() {
        WeaponStats base = WeaponStats.builder().set(Stat.HIP_SPREAD, 1f).build();
        WeaponStats mod = base.with(List.of(StatModifier.percent(Stat.HIP_SPREAD, -5f)));
        assertEquals(0f, mod.get(Stat.HIP_SPREAD));
        assertEquals(500f, WeaponStats.builder().set(Stat.MAGAZINE, 10_000).build().get(Stat.MAGAZINE));
    }

    @Test
    void noModifiersReturnsSameInstance() {
        WeaponStats base = WeaponStats.builder().build();
        assertSame(base, base.with(List.of()));
    }

    @Test
    void ticksPerShotMatchesRpm() {
        WeaponStats s = WeaponStats.builder().set(Stat.FIRE_RATE, 600).build();
        assertEquals(2.0, s.ticksPerShot(), 1e-9);
    }

    @Test
    void beneficialFlagFollowsStatDirection() {
        assertTrue(StatModifier.percent(Stat.RECOIL_VERTICAL, -0.1f).isBeneficial());
        assertFalse(StatModifier.percent(Stat.DAMAGE, -0.1f).isBeneficial());
        assertTrue(StatModifier.add(Stat.MAGAZINE, 5).isBeneficial());
    }
}
