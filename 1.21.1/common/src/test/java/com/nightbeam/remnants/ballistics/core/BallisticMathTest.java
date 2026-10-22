package com.nightbeam.remnants.ballistics.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BallisticMathTest {
    @Test
    void directionMatchesMinecraftConvention() {
        V3 south = BallisticMath.direction(0f, 0f);
        assertEquals(1.0, south.z(), 1e-9);
        V3 west = BallisticMath.direction(90f, 0f);
        assertEquals(-1.0, west.x(), 1e-9);
        V3 down = BallisticMath.direction(0f, 90f);
        assertEquals(-1.0, down.y(), 1e-9);
    }

    @Test
    void spreadStaysInsideTheCone() {
        V3 forward = BallisticMath.direction(37f, -12f);
        for (int shot = 0; shot < 2000; shot++) {
            V3 d = BallisticMath.spread(forward, 3f, ShotRandom.forShot(42L, shot));
            double angle = Math.toDegrees(Math.acos(Math.min(1.0, d.dot(forward))));
            assertTrue(angle <= 3.0 + 1e-6, "angle " + angle);
            assertEquals(1.0, d.length(), 1e-9);
        }
    }

    @Test
    void spreadIsDeterministicPerSeedAndShot() {
        V3 f = BallisticMath.direction(10f, 5f);
        assertEquals(BallisticMath.spread(f, 2f, ShotRandom.forShot(7L, 3)), BallisticMath.spread(f, 2f, ShotRandom.forShot(7L, 3)));
        assertNotEquals(BallisticMath.spread(f, 2f, ShotRandom.forShot(7L, 3)), BallisticMath.spread(f, 2f, ShotRandom.forShot(7L, 4)));
    }

    @Test
    void zeroSpreadIsExact() {
        V3 f = BallisticMath.direction(10f, 5f);
        assertEquals(f.normalize(), BallisticMath.spread(f, 0f, new ShotRandom(1)));
    }

    @Test
    void straightTrajectoryIsOneSegment() {
        List<V3> path = BallisticMath.trajectory(V3.ZERO, new V3(0, 0, 1), 400, 0, 100, 8);
        assertEquals(2, path.size());
        assertEquals(100.0, path.get(1).z(), 1e-9);
    }

    @Test
    void trajectoryDropMatchesAnalyticDrop() {
        double v = 200, g = 20, dist = 160;
        List<V3> path = BallisticMath.trajectory(V3.ZERO, new V3(0, 0, 1), v, g, dist, 4);
        V3 end = path.get(path.size() - 1);
        double expected = BallisticMath.dropAt(end.z(), v, g);
        assertEquals(-expected, end.y(), 0.05);
        assertTrue(end.y() < -5.0, "a 200 m/s bullet drops several blocks at 160 blocks");
    }

    @Test
    void reflectionMirrorsAboutTheNormal() {
        V3 r = BallisticMath.reflect(new V3(1, -1, 0).normalize(), V3.UP);
        assertEquals(new V3(1, 1, 0).normalize().x(), r.x(), 1e-9);
        assertEquals(new V3(1, 1, 0).normalize().y(), r.y(), 1e-9);
        assertEquals(45.0, BallisticMath.impactAngleDeg(new V3(1, -1, 0), V3.UP), 1e-9);
    }
}
