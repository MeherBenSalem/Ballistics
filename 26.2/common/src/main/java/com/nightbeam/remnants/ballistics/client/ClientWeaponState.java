package com.nightbeam.remnants.ballistics.client;

import com.nightbeam.remnants.ballistics.client.anim.AnimationPlayer;
import com.nightbeam.remnants.ballistics.combat.ReloadPhase;
import net.minecraft.world.item.Item;

/**
 * Local player's predicted weapon state. Owned by the client thread; reset when the held weapon changes.
 */
public final class ClientWeaponState {
    public static final ClientWeaponState INSTANCE = new ClientWeaponState();

    // Identity of the held weapon
    public int slot = -1;
    public Item item;
    public long equipReadyTick;

    // Spread seed shared by the server and the shot counter
    public long seed;
    public boolean hasSeed;
    public int nextSequence;

    // Trigger and fire timing (client game-time ticks)
    public double nextShotTick;
    public boolean pendingClick;
    public boolean triggerWasDown;
    public int burstRemaining;
    public long lastShotTick = Long.MIN_VALUE / 2;
    public int sprayCount;
    public float bloom;
    public long lastShotNanos;
    public long lastDryFireTick = Long.MIN_VALUE / 2;

    // Aiming
    public boolean aiming;
    public boolean aimToggled;
    public float adsProgress;
    public float prevAdsProgress;
    public float sprintProgress;
    public float prevSprintProgress;

    // Charged weapons
    public long chargeStartTick = -1;

    // Camera recoil still to be applied (degrees) and recoil accumulated for recovery
    public float pendingPitch;
    public float pendingYaw;
    public float recoveryPitch;

    // Visual kick spring (render time)
    public float kick;
    public float kickVelocity;

    // Reload
    public ReloadPhase reloadPhase;
    public boolean reloadPredicted;

    // Animation layers
    public final AnimationPlayer idle = new AnimationPlayer();
    public final AnimationPlayer action = new AnimationPlayer();
    public final AnimationPlayer fire = new AnimationPlayer();

    // Hit marker
    public long hitMarkerNanos;
    public boolean hitMarkerHeadshot;
    public boolean hitMarkerKill;

    private ClientWeaponState() {}

    public boolean isReloading() {
        return reloadPhase != null;
    }

    /** Clears everything tied to a specific weapon (switching, death, disconnect). */
    public void resetWeapon() {
        burstRemaining = 0;
        pendingClick = false;
        aiming = false;
        aimToggled = false;
        chargeStartTick = -1;
        reloadPhase = null;
        reloadPredicted = false;
        sprayCount = 0;
        bloom = 0f;
        kick = 0f;
        kickVelocity = 0f;
        action.stop();
        fire.stop();
    }

    public void resetAll() {
        resetWeapon();
        slot = -1;
        item = null;
        hasSeed = false;
        pendingPitch = pendingYaw = recoveryPitch = 0f;
        adsProgress = prevAdsProgress = 0f;
    }
}
