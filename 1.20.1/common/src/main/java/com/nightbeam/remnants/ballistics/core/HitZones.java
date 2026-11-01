package com.nightbeam.remnants.ballistics.core;

/** Classifies a hit position on an entity's bounding box into a damage zone. */
public final class HitZones {
    private HitZones() {}

    public enum Zone {
        HEAD, BODY, LIMB
    }

    /**
     * @param hitY     world Y of the impact point
     * @param minY     bottom of the (possibly rewound) bounding box
     * @param height   bounding box height
     * @param eyeOffset entity eye height above {@code minY}
     * @param humanoid whether the entity has an upright head/torso/legs layout
     */
    public static Zone classify(double hitY, double minY, double height, double eyeOffset, boolean humanoid) {
        if (height <= 0) return Zone.BODY;
        double rel = hitY - minY;
        double headSpan = Math.max(0.18, Math.min(0.45, height - eyeOffset + 0.04));
        double headStart = eyeOffset - Math.max(0.12, headSpan * 0.5);
        if (!humanoid) {
            // Non-upright mobs: only a narrow band around the eyes counts as the head.
            return Math.abs(rel - eyeOffset) <= Math.max(0.12, height * 0.1) ? Zone.HEAD : Zone.BODY;
        }
        if (rel >= headStart) return Zone.HEAD;
        if (rel < height * 0.38) return Zone.LIMB;
        return Zone.BODY;
    }
}
