package com.nightbeam.remnants.ballistics.weapon;

import java.util.Locale;

/**
 * Gunshot sound families. Each profile maps to a {@code gun.<profile>.fire} sound event (see {@code ModSounds}),
 * which keeps weapon definitions independent of registry initialisation order.
 */
public enum SoundProfile {
    PISTOL, MAGNUM, REVOLVER, SMG, PDW, RIFLE, AK, BURST, SHOTGUN, PUMP, SNIPER, HEAVY_SNIPER, LMG, HEAVY_LMG, VOIDCASTER, RAILGUN;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
