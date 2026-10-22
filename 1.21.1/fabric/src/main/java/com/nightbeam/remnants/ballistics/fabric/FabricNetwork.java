package com.nightbeam.remnants.ballistics.fabric;

import com.nightbeam.remnants.ballistics.fabric.client.FabricClientNetwork;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.platform.INetwork;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class FabricNetwork implements INetwork {
    /** Payload types (both sides) and server-bound handlers. Client-bound handlers are registered by the client entry point. */
    static void register() {
        for (ModNetwork.Entry<?> entry : ModNetwork.SERVER_BOUND) registerToServer(entry);
        for (ModNetwork.Entry<?> entry : ModNetwork.CLIENT_BOUND) PayloadTypeRegistry.playS2C().register(entry.type(), entry.codec());
    }

    private static <T extends CustomPacketPayload> void registerToServer(ModNetwork.Entry<T> entry) {
        PayloadTypeRegistry.playC2S().register(entry.type(), entry.codec());
        ServerPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) -> entry.handler().accept(payload, context.player()));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        // Kept in a client-only class so dedicated servers never resolve ClientPlayNetworking.
        FabricClientNetwork.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            if (player != entity) ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer self) ServerPlayNetworking.send(self, payload);
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                                  CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.around(level, new Vec3(x, y, z), radius)) {
            if (player != excluded) ServerPlayNetworking.send(player, payload);
        }
    }
}
