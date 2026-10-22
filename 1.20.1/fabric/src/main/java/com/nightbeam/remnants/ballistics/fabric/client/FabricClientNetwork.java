package com.nightbeam.remnants.ballistics.fabric.client;

import com.nightbeam.remnants.ballistics.fabric.FabricNetwork;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.network.Payload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client half of the Fabric networking glue. */
public final class FabricClientNetwork {
    private FabricClientNetwork() {}

    static void registerReceivers() {
        for (ModNetwork.Entry<?> entry : ModNetwork.CLIENT_BOUND) registerReceiver(entry);
    }

    private static <T extends Payload> void registerReceiver(ModNetwork.Entry<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.channel(), (client, handler, buf, sender) -> {
            // Decoded on the network thread, handled on the render thread.
            T payload = entry.decoder().apply(buf);
            client.execute(() -> {
                if (client.player != null) entry.handler().accept(payload, client.player);
            });
        });
    }

    public static void send(Payload payload) {
        ClientPlayNetworking.send(payload.channel(), FabricNetwork.encode(payload));
    }
}
