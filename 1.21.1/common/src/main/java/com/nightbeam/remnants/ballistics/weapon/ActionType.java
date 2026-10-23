package com.nightbeam.remnants.ballistics.weapon;

/** Mechanical action, used for cycling animations and sounds after each shot. */
public enum ActionType {
    /** Self-loading: slide or bolt cycles automatically. */
    SELF_LOADING,
    /** Pump-action: the fore-end is racked after each shot. */
    PUMP,
    /** Bolt-action: the bolt is cycled by hand after each shot. */
    BOLT,
    /** Energy weapons: no mechanical cycling, may require charging. */
    ENERGY
}
