package com.nightbeam.remnants.ballistics.client.anim;

import org.joml.Vector3f;

/**
 * Accumulated transform of one bone: translation in model pixels, rotation in degrees (applied Z, then Y, then X
 * around the bone pivot) and scale. Multiple animation layers add their contributions with a weight.
 */
public final class BonePose {
    public final Vector3f position = new Vector3f();
    public final Vector3f rotation = new Vector3f();
    public final Vector3f scale = new Vector3f(1f, 1f, 1f);

    public void reset() {
        position.zero();
        rotation.zero();
        scale.set(1f, 1f, 1f);
    }

    public boolean isIdentity() {
        return position.lengthSquared() < 1.0E-8f && rotation.lengthSquared() < 1.0E-8f
                && Math.abs(scale.x - 1f) < 1.0E-5f && Math.abs(scale.y - 1f) < 1.0E-5f && Math.abs(scale.z - 1f) < 1.0E-5f;
    }
}
