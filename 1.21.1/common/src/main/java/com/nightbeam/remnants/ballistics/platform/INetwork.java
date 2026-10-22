package com.nightbeam.remnants.ballistics.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** Payload sending. Payload types and handlers are declared once in {@code ModNetwork} and registered per loader. */
public interface INetwork {
    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** Players tracking the entity, excluding the entity itself. */
    void sendToTracking(Entity entity, CustomPacketPayload payload);

    /** Players tracking the entity, plus the entity if it is a player. */
    void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload);

    /** Players within {@code radius} blocks of a point, optionally excluding one. */
    void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                           CustomPacketPayload payload);
}
