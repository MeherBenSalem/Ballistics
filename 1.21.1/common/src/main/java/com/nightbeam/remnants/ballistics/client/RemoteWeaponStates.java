package com.nightbeam.remnants.ballistics.client;

import com.nightbeam.remnants.ballistics.client.anim.AnimationPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/** Third-person weapon state of other players, driven by server events. */
public final class RemoteWeaponStates {
    private static final Map<Integer, State> STATES = new HashMap<>();

    private RemoteWeaponStates() {}

    public static final class State {
        public boolean aiming;
        public boolean charging;
        public long lastShotNanos;
        public final AnimationPlayer action = new AnimationPlayer();
        public final AnimationPlayer fire = new AnimationPlayer();
    }

    public static State get(Entity entity) {
        return STATES.computeIfAbsent(entity.getId(), k -> new State());
    }

    public static State get(int entityId) {
        return STATES.computeIfAbsent(entityId, k -> new State());
    }

    public static State peek(int entityId) {
        return STATES.get(entityId);
    }

    public static void forget(int entityId) {
        STATES.remove(entityId);
    }

    public static void clear() {
        STATES.clear();
    }
}
