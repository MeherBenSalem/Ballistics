package com.nightbeam.remnants.ballistics.core;

import java.util.ArrayList;
import java.util.List;

/** Pure math helpers for shot directions, bullet drop and ricochets. */
public final class BallisticMath {
    private BallisticMath() {}

    /** Look vector using Minecraft's yaw/pitch convention (degrees; pitch positive = looking down). */
    public static V3 direction(float yawDeg, float pitchDeg) {
        double pitch = Math.toRadians(pitchDeg);
        double yaw = Math.toRadians(-yawDeg);
        double cosPitch = Math.cos(pitch);
        return new V3(Math.sin(yaw) * cosPitch, -Math.sin(pitch), Math.cos(yaw) * cosPitch);
    }

    /**
     * Rotates {@code forward} by a random angle within a cone. The radial distribution is triangular
     * (biased toward the centre), which reads as "accurate but imperfect" in play.
     */
    public static V3 spread(V3 forward, float coneDeg, ShotRandom rng) {
        V3 f = forward.normalize();
        if (coneDeg <= 0f) return f;
        double angle = Math.toRadians(coneDeg) * (rng.nextFloat() + rng.nextFloat()) * 0.5;
        double theta = rng.nextFloat() * Math.PI * 2.0;
        V3 helper = Math.abs(f.y()) > 0.99 ? new V3(1, 0, 0) : V3.UP;
        V3 right = f.cross(helper).normalize();
        V3 up = right.cross(f).normalize();
        V3 offset = right.scale(Math.cos(theta)).add(up.scale(Math.sin(theta)));
        return f.scale(Math.cos(angle)).add(offset.scale(Math.sin(angle))).normalize();
    }

    /** Angle in degrees between the direction of travel and the hit surface (0 = grazing, 90 = head-on). */
    public static double impactAngleDeg(V3 dir, V3 normal) {
        double d = Math.abs(dir.normalize().dot(normal.normalize()));
        return Math.toDegrees(Math.asin(Math.min(1.0, d)));
    }

    /** Mirror reflection of {@code dir} about the surface {@code normal}. */
    public static V3 reflect(V3 dir, V3 normal) {
        V3 n = normal.normalize();
        return dir.sub(n.scale(2.0 * dir.dot(n))).normalize();
    }

    /**
     * Samples a ballistic trajectory as a polyline. Each segment is traced against the world in turn, which gives
     * hitscan weapons physically plausible bullet drop while remaining instant and server-authoritative.
     *
     * @param velocity     blocks per second
     * @param gravity      blocks per second squared (0 = straight line)
     * @param maxDistance  total path length to cover
     * @param segmentLength approximate length of each traced segment
     */
    public static List<V3> trajectory(V3 origin, V3 dir, double velocity, double gravity, double maxDistance, double segmentLength) {
        List<V3> points = new ArrayList<>();
        points.add(origin);
        V3 d = dir.normalize();
        if (gravity <= 0.0 || velocity <= 0.0) {
            points.add(origin.add(d.scale(maxDistance)));
            return points;
        }
        double dt = segmentLength / velocity;
        double travelled = 0.0;
        V3 pos = origin;
        V3 vel = d.scale(velocity);
        // Hard cap on segments keeps worst-case server cost bounded regardless of config values.
        for (int i = 0; i < 512 && travelled < maxDistance; i++) {
            V3 next = pos.add(vel.scale(dt)).add(new V3(0, -0.5 * gravity * dt * dt, 0));
            travelled += next.distanceTo(pos);
            vel = vel.add(new V3(0, -gravity * dt, 0));
            pos = next;
            points.add(pos);
        }
        return points;
    }

    /** Vertical drop (blocks) after travelling {@code distance} horizontally-ish at {@code velocity}. */
    public static double dropAt(double distance, double velocity, double gravity) {
        if (velocity <= 0) return 0;
        double t = distance / velocity;
        return 0.5 * gravity * t * t;
    }
}
