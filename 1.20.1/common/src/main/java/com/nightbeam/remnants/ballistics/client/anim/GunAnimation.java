package com.nightbeam.remnants.ballistics.client.anim;

import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

/**
 * One keyframed clip (Bedrock/Blockbench animation subset): per-bone channels plus timed sound cues.
 *
 * @param length authored length in seconds; playback is time-scaled to the server-dictated duration
 */
public record GunAnimation(String name, float length, boolean loop, Map<String, Bone> bones, List<SoundCue> sounds) {
    public record Bone(Channel position, Channel rotation, Channel scale) {}

    /** @param sound sound event path in this mod's namespace, e.g. {@code gun.mag_out} */
    public record SoundCue(float time, String sound, float volume, float pitch) {}

    private static final Vector3f TMP = new Vector3f();

    /**
     * Adds this clip's contribution at {@code t} seconds into the pose with the given blend weight.
     */
    public void apply(float t, float weight, PoseState pose) {
        if (weight <= 0f) return;
        float time = loop && length > 0f ? t % length : Math.min(t, length);
        for (Map.Entry<String, Bone> e : bones.entrySet()) {
            BonePose bone = pose.bone(e.getKey());
            Bone b = e.getValue();
            if (b.position() != null) bone.position.add(b.position().sample(time, TMP).mul(weight));
            if (b.rotation() != null) bone.rotation.add(b.rotation().sample(time, TMP).mul(weight));
            if (b.scale() != null) {
                b.scale().sample(time, TMP);
                bone.scale.mul(1f + (TMP.x - 1f) * weight, 1f + (TMP.y - 1f) * weight, 1f + (TMP.z - 1f) * weight);
            }
        }
    }
}
