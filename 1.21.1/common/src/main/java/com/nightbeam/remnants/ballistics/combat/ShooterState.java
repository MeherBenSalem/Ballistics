package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.FireRateLimiter;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Transient server-side shooting state for one player (NeoForge data attachment, never serialised).
 * All fields are only touched on the server thread.
 */
public final class ShooterState {
    /** Ticks a weapon needs after being drawn before it can fire. */
    public static final int EQUIP_TICKS = 8;
    public static final double MAX_BANK_TICKS = 4.0;

    /** One shot of idle credit, worth at most {@link #MAX_BANK_TICKS}, absorbs a late packet without allowing double taps. */
    public final FireRateLimiter limiter = new FireRateLimiter(ServerConfig.get(ServerConfig.FIRE_RATE_TOLERANCE_TICKS), 1.0, MAX_BANK_TICKS);
    /** Secret mixed into shot spread; shared with the owning client only. */
    public long spreadSeed;
    public boolean seedSent;
    /** Highest accepted shot sequence number; packets with a lower or equal number are duplicates or replays. */
    public int lastShotSeq = -1;

    public boolean aiming;
    public long aimChangeTick;

    public float bloom;
    public long lastShotTick = Long.MIN_VALUE / 2;
    public int sprayCount;

    public long chargeStartTick = -1;

    public int heldSlot = -1;
    @Nullable
    public Item heldItem;
    public long equipReadyTick;

    @Nullable
    public ReloadState reload;

    /** Smoothed round-trip time measured with our own ping payload, in milliseconds. */
    public float rttMs = 100f;
    public long pingSentNanos;
    public int pingId;

    public float adsProgress(long now, float adsTicks) {
        if (!aiming) return 0f;
        return Math.min(1f, (now - aimChangeTick) / Math.max(1f, adsTicks));
    }

    public boolean isReloading() {
        return reload != null;
    }

    /** One active reload. Single-load weapons step through START → INSERT* → END phases. */
    public static final class ReloadState {
        public final int slot;
        public final Item item;
        public ReloadPhase phase;
        public long phaseEndTick;
        public final boolean empty;

        public ReloadState(int slot, Item item, ReloadPhase phase, long phaseEndTick, boolean empty) {
            this.slot = slot;
            this.item = item;
            this.phase = phase;
            this.phaseEndTick = phaseEndTick;
            this.empty = empty;
        }
    }
}
