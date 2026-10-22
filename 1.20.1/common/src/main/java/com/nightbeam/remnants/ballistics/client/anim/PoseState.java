package com.nightbeam.remnants.ballistics.client.anim;

import java.util.HashMap;
import java.util.Map;

/** Mutable per-frame collection of bone poses for one rendered weapon. */
public final class PoseState {
    public static final String GUN = "gun";
    public static final String ACTION = "action";
    public static final String MAGAZINE = "magazine";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_ARM = "right_arm";

    private final Map<String, BonePose> bones = new HashMap<>();

    public BonePose bone(String name) {
        return bones.computeIfAbsent(name, k -> new BonePose());
    }

    /** Returns the bone's pose or null if nothing animated it this frame. */
    public BonePose get(String name) {
        BonePose pose = bones.get(name);
        return pose == null || pose.isIdentity() ? null : pose;
    }

    public void reset() {
        for (BonePose pose : bones.values()) pose.reset();
    }
}
