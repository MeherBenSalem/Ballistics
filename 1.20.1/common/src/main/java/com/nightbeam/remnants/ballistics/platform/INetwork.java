package com.nightbeam.remnants.ballistics.platform;

import com.nightbeam.remnants.ballistics.network.Payload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** Payload sending. Payload channels and handlers are declared once in {@code ModNetwork} and registered per loader. */
public interface INetwork {
    void sendToServer(Payload payload);

    void sendToPlayer(ServerPlayer player, Payload payload);

    /** Players tracking the entity, excluding the entity itself. */
    void sendToTracking(Entity entity, Payload payload);

    /** Players tracking the entity, plus the entity if it is a player. */
    void sendToTrackingAndSelf(Entity entity, Payload payload);

    /** Players within {@code radius} blocks of a point, optionally excluding one. */
    void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                           Payload payload);
}
