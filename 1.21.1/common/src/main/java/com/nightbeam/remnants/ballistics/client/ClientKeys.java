package com.nightbeam.remnants.ballistics.client;

import net.minecraft.client.gui.screens.Screen;

/** Client keyboard state for common code; only call behind {@code Services.PLATFORM.isPhysicalClient()}. */
public final class ClientKeys {
    private ClientKeys() {}

    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }
}
