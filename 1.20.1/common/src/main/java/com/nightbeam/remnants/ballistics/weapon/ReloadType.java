package com.nightbeam.remnants.ballistics.weapon;

/** How rounds are loaded into the weapon. */
public enum ReloadType {
    /** Swap a detachable magazine (or battery cell) in one action. */
    MAGAZINE,
    /** Insert rounds one at a time (tube shotguns, internal-magazine bolt rifles, revolvers); interruptible. */
    SINGLE
}
