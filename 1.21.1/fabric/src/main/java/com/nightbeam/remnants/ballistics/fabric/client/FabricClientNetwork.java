package com.nightbeam.remnants.ballistics.fabric.client;

import com.nightbeam.remnants.ballistics.network.ModNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client half of the Fabric networking glue. */
public final class FabricClientNetwork {
    private FabricClientNetwork() {}

    static void registerReceivers() {
        for (ModNetwork.Entry<?> entry : ModNetwork.CLIENT_BOUND) registerReceiver(entry);
    }

    private static <T extends CustomPacketPayload> void registerReceiver(ModNetwork.Entry<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) -> entry.handler().accept(payload, context.player()));
    }

    public static void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
