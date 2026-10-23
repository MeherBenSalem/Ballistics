package com.nightbeam.remnants.ballistics.fabric.platform;

import com.nightbeam.remnants.ballistics.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class FabricNetwork implements INetworkHelper {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        Client.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                                  CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.around(level, new Vec3(x, y, z), radius)) {
            if (player != excluded) ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        sendToPlayersTrackingEntity(entity, payload);
        if (entity instanceof ServerPlayer self) ServerPlayNetworking.send(self, payload);
    }

    /** Keeps the client networking class out of the server code path. */
    private static final class Client {
        private static void send(CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
        }
    }
}
