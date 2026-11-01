package com.nightbeam.remnants.ballistics.fabric.platform;

import com.nightbeam.remnants.ballistics.network.ModPayload;
import com.nightbeam.remnants.ballistics.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Payloads travel as custom-payload packets on their own channel id ({@link ModPayload.Type#id()}). */
public final class FabricNetwork implements INetworkHelper {
    @Override
    public void sendToServer(ModPayload payload) {
        Client.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ModPayload payload) {
        player.connection.send(packet(payload));
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                                  ModPayload payload) {
        Packet<ClientGamePacketListener> packet = null;
        for (ServerPlayer player : PlayerLookup.around(level, new Vec3(x, y, z), radius)) {
            if (player == excluded) continue;
            if (packet == null) packet = packet(payload);
            player.connection.send(packet);
        }
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, ModPayload payload) {
        Packet<ClientGamePacketListener> packet = null;
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            if (packet == null) packet = packet(payload);
            player.connection.send(packet);
        }
    }

    @Override
    public void sendToPlayersTrackingEntityAndSelf(Entity entity, ModPayload payload) {
        sendToPlayersTrackingEntity(entity, payload);
        if (entity instanceof ServerPlayer self) sendToPlayer(self, payload);
    }

    /** One packet can go to several players: the vanilla custom-payload packet copies its buffer when written. */
    private static Packet<ClientGamePacketListener> packet(ModPayload payload) {
        return ServerPlayNetworking.createS2CPacket(payload.type().id(), encode(payload));
    }

    static FriendlyByteBuf encode(ModPayload payload) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        return buf;
    }

    /** Keeps the client networking class out of the server code path. */
    private static final class Client {
        private static void send(ModPayload payload) {
            ClientPlayNetworking.send(payload.type().id(), encode(payload));
        }
    }
}
