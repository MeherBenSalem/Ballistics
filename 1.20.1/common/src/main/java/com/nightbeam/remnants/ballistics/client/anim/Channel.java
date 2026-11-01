package com.nightbeam.remnants.ballistics.client.anim;

import org.joml.Vector3f;

import java.util.Arrays;

/** One animated vector property (position, rotation or scale) of a bone: time-sorted keyframes. */
public final class Channel {
    public enum Lerp {
        LINEAR, CATMULLROM, STEP
    }

    private final float[] times;
    private final Vector3f[] values;
    private final Lerp[] lerps;

    public Channel(float[] times, Vector3f[] values, Lerp[] lerps) {
        if (times.length == 0 || times.length != values.length || times.length != lerps.length) {
            throw new IllegalArgumentException("Malformed channel");
        }
        this.times = times;
        this.values = values;
        this.lerps = lerps;
    }

    public static Channel constant(Vector3f value) {
        return new Channel(new float[]{0f}, new Vector3f[]{value}, new Lerp[]{Lerp.LINEAR});
    }

    /** Samples the channel at {@code t} seconds into {@code out}. Values hold before the first and after the last key. */
    public Vector3f sample(float t, Vector3f out) {
        int n = times.length;
        if (n == 1 || t <= times[0]) return out.set(values[0]);
        if (t >= times[n - 1]) return out.set(values[n - 1]);
        int i = Arrays.binarySearch(times, t);
        if (i >= 0) return out.set(values[i]);
        int next = -i - 1;
        int prev = next - 1;
        float span = times[next] - times[prev];
        float f = span <= 0f ? 1f : (t - times[prev]) / span;
        Lerp mode = lerps[next];
        if (mode == Lerp.STEP) return out.set(values[prev]);
        if (mode == Lerp.CATMULLROM) {
            Vector3f p0 = values[Math.max(0, prev - 1)];
            Vector3f p1 = values[prev];
            Vector3f p2 = values[next];
            Vector3f p3 = values[Math.min(n - 1, next + 1)];
            return out.set(catmull(p0.x, p1.x, p2.x, p3.x, f), catmull(p0.y, p1.y, p2.y, p3.y, f), catmull(p0.z, p1.z, p2.z, p3.z, f));
        }
        Vector3f a = values[prev];
        Vector3f b = values[next];
        return out.set(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f, a.z + (b.z - a.z) * f);
    }

    private static float catmull(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        return 0.5f * ((2f * p1) + (-p0 + p2) * t + (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 + (-p0 + 3f * p1 - 3f * p2 + p3) * t3);
    }
}
