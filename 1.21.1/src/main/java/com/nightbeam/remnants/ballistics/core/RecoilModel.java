package com.nightbeam.remnants.ballistics.core;

/** Camera recoil per shot. Applied client-side to the view; the server receives the resulting aim. */
public final class RecoilModel {
    private RecoilModel() {}

    /** Pitch is negative-up in Minecraft, so callers subtract {@link #pitch()} from the player's X rotation. */
    public record Kick(float pitch, float yaw) {}

    /**
     * @param shotsInSpray shots fired in the current continuous spray (0 for the first)
     * @param adsProgress  0 = hip, 1 = aimed
     * @param crouching    crouching steadies the weapon
     * @param multiplier   global config scale
     */
    public static Kick kick(WeaponStats stats, int shotsInSpray, float adsProgress, boolean crouching, ShotRandom rng, float multiplier) {
        float vertical = stats.get(Stat.RECOIL_VERTICAL);
        float horizontal = stats.get(Stat.RECOIL_HORIZONTAL);
        // Sustained fire climbs a little harder for the first several shots, then plateaus.
        float climb = 1f + Math.min(shotsInSpray, 8) * 0.04f;
        // A gentle, deterministic sideways drift gives sprays a learnable shape on top of the random component.
        float drift = (float) Math.sin(shotsInSpray * 0.7) * 0.35f;
        float ads = 1f - 0.25f * Math.max(0f, Math.min(1f, adsProgress));
        float stance = crouching ? 0.85f : 1f;
        float scale = ads * stance * multiplier;
        float pitch = vertical * climb * (0.85f + 0.3f * rng.nextFloat()) * scale;
        float yaw = horizontal * (rng.nextSigned() + drift) * scale;
        return new Kick(pitch, yaw);
    }
}
