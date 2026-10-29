package com.nightbeam.remnants.ballistics.client;

import net.minecraft.client.Minecraft;

/** Client keyboard state for common code; only call behind {@code Services.PLATFORM.isPhysicalClient()}. */
public final class ClientKeys {
    private ClientKeys() {}

    public static boolean shiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
