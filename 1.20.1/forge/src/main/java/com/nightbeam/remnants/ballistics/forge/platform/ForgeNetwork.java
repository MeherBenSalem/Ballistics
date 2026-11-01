package com.nightbeam.remnants.ballistics.forge.platform;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.network.ModPayload;
import com.nightbeam.remnants.ballistics.platform.services.INetworkHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

/** Every payload goes through one {@link SimpleChannel}, indexed in {@link ModNetwork} order. */
public final class ForgeNetwork implements INetworkHelper {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(RemnantsBallistics.id("main"),
            () -> ModNetwork.PROTOCOL_VERSION, ModNetwork.PROTOCOL_VERSION::equals, ModNetwork.PROTOCOL_VERSION::equals);
    private static int nextId;

    /**
     * Called once from the mod constructor. Server-bound handlers run on the server thread. Client-bound handlers are
     * lambdas, so {@link ClientPayloadHandler} is only class-loaded when a payload actually arrives on a client; a method
     * reference would resolve the class during registration, which breaks dedicated servers.
     */
    public static void register() {
        ModNetwork.SERVERBOUND.forEach(ForgeNetwork::serverbound);
        ModNetwork.CLIENTBOUND.forEach(ForgeNetwork::clientbound);
    }

    private static <T extends ModPayload> void serverbound(ModNetwork.Serverbound<T> entry) {
        CHANNEL.messageBuilder(entry.type().payloadClass(), nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ModPayload::write)
                .decoder(entry.decoder())
                .consumerMainThread((payload, context) -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) entry.handler().accept(payload, player);
                })
                .add();
    }

    private static <T extends ModPayload> void clientbound(ModNetwork.Clientbound<T> entry) {
        CHANNEL.messageBuilder(entry.type().payloadClass(), nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ModPayload::write)
                .decoder(entry.decoder())
                .consumerMainThread((payload, context) -> ClientPayloadHandler.handle(payload))
                .add();
    }

    @Override
    public void sendToServer(ModPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ModPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                                  ModPayload payload) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension())), payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, ModPayload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
    }

    @Override
    public void sendToPlayersTrackingEntityAndSelf(Entity entity, ModPayload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }
}
