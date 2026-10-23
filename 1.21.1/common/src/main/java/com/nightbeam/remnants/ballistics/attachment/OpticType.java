package com.nightbeam.remnants.ballistics.attachment;

/** Sighting systems. Drives ADS alignment, reticle rendering and the scope overlay. */
public enum OpticType {
    /** Weapon's own iron sights. */
    IRON,
    /** Unmagnified or low-magnification sight with a projected reticle. */
    REFLEX,
    /** Magnified optic: full-screen scope overlay while fully aimed. */
    SCOPE
}
