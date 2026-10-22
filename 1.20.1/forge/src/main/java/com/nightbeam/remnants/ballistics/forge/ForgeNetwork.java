package com.nightbeam.remnants.ballistics.forge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.forge.client.ForgeClientNetwork;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.network.Payload;
import com.nightbeam.remnants.ballistics.platform.INetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

/** One SimpleChannel carries every payload; message ids follow the order of {@link ModNetwork}'s lists. */
public final class ForgeNetwork implements INetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(RemnantsBallistics.id("main"),
            () -> ModNetwork.PROTOCOL_VERSION, ModNetwork.PROTOCOL_VERSION::equals, ModNetwork.PROTOCOL_VERSION::equals);

    static void register() {
        int id = 0;
        for (ModNetwork.Entry<?> entry : ModNetwork.SERVER_BOUND) toServer(id++, entry);
        for (ModNetwork.Entry<?> entry : ModNetwork.CLIENT_BOUND) toClient(id++, entry);
    }

    private static <T extends Payload> void toServer(int id, ModNetwork.Entry<T> entry) {
        CHANNEL.messageBuilder(entry.type(), id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Payload::write)
                .decoder(entry.decoder())
                .consumerMainThread((payload, context) -> {
                    ServerPlayer sender = context.get().getSender();
                    if (sender != null) entry.handler().accept(payload, sender);
                })
                .add();
    }

    private static <T extends Payload> void toClient(int id, ModNetwork.Entry<T> entry) {
        CHANNEL.messageBuilder(entry.type(), id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Payload::write)
                .decoder(entry.decoder())
                // Kept in a client-only class so dedicated servers never resolve the client player.
                .consumerMainThread((payload, context) -> ForgeClientNetwork.handle(entry, payload))
                .add();
    }

    @Override
    public void sendToServer(Payload payload) {
        CHANNEL.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Payload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToTracking(Entity entity, Payload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, Payload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
                                  Payload payload) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension())), payload);
    }
}
