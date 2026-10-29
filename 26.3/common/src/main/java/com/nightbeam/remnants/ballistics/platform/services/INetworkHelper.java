package com.nightbeam.remnants.ballistics.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** Sends the payloads declared in {@code network.ModNetwork}. */
public interface INetworkHelper {
    /** Client only. */
    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** Every player within {@code radius} blocks of the point, except {@code excluded}. */
    void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                           CustomPacketPayload payload);

    void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload);

    /** Tracking players plus the entity itself when it is a player. */
    void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload);
}
