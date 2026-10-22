package com.nightbeam.remnants.ballistics.weapon;

import java.util.function.BooleanSupplier;

/**
 * Modifier-key state for item tooltips. Items are common code, but key state lives in client classes, so the client
 * installs a reader at startup; on a dedicated server keys always read as released.
 */
public final class TooltipKeys {
    private static BooleanSupplier shift = () -> false;

    private TooltipKeys() {}

    public static boolean shiftDown() {
        return shift.getAsBoolean();
    }

    public static void install(BooleanSupplier shiftDown) {
        shift = shiftDown;
    }
}
