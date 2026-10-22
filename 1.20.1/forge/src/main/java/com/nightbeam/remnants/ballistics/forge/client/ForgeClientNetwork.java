package com.nightbeam.remnants.ballistics.forge.client;

import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.network.Payload;
import net.minecraft.client.Minecraft;

/** Client half of the Forge networking glue; only loaded when a client-bound payload arrives. */
public final class ForgeClientNetwork {
    private ForgeClientNetwork() {}

    public static <T extends Payload> void handle(ModNetwork.Entry<T> entry, T payload) {
        if (Minecraft.getInstance().player != null) entry.handler().accept(payload, Minecraft.getInstance().player);
    }
}
