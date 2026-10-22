package com.nightbeam.remnants.ballistics.combat;

/** Reload phases, shared by the server state machine, network events and client animations. */
public enum ReloadPhase {
    /** Whole-magazine (or cell) swap. */
    MAGAZINE,
    /** Single-load: open the action / start loading. */
    START,
    /** Single-load: insert one round. */
    INSERT,
    /** Single-load: close the action. */
    END,
    /** Reload finished successfully. */
    DONE,
    /** Reload aborted (weapon switched, fired or rejected). */
    CANCEL;

    public static ReloadPhase byId(int id) {
        ReloadPhase[] values = values();
        return values[Math.floorMod(id, values.length)];
    }
}
