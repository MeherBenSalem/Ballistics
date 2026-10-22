package com.nightbeam.remnants.ballistics.fabric;

import com.nightbeam.remnants.ballistics.fabric.client.FabricClientNetwork;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.network.Payload;
import com.nightbeam.remnants.ballistics.platform.INetwork;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** One channel per payload. Server-bound receivers are registered here; client-bound ones by the client entry point. */
public final class FabricNetwork implements INetwork {
    static void register() {
        for (ModNetwork.Entry<?> entry : ModNetwork.SERVER_BOUND) registerToServer(entry);
    }

    private static <T extends Payload> void registerToServer(ModNetwork.Entry<T> entry) {
        ServerPlayNetworking.registerGlobalReceiver(entry.channel(), (server, player, handler, buf, sender) -> {
            // Decoded on the network thread, handled on the server thread.
            T payload = entry.decoder().apply(buf);
            server.execute(() -> entry.handler().accept(payload, player));
        });
    }

    /** The payload written to a fresh buffer; one per recipient, since sending releases it. */
    public static FriendlyByteBuf encode(Payload payload) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        return buf;
    }

    @Override
    public void sendToServer(Payload payload) {
        // Kept in a client-only class so dedicated servers never resolve ClientPlayNetworking.
        FabricClientNetwork.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Payload payload) {
        ServerPlayNetworking.send(player, payload.channel(), encode(payload));
    }

    @Override
    public void sendToTracking(Entity entity, Payload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            if (player != entity) sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, Payload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer self) sendToPlayer(self, payload);
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                                  Payload payload) {
        for (ServerPlayer player : PlayerLookup.around(level, new Vec3(x, y, z), radius)) {
            if (player != excluded) sendToPlayer(player, payload);
        }
    }
}
