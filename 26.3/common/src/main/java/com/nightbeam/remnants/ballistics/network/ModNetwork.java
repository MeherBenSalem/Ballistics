package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Every payload of the mod. Each loader registers these tables: server-bound handlers run on the server thread with the
 * sending player; client-bound payloads are handled by {@code client.network.ClientPayloadHandler}, which the loaders
 * only reference from client-side code so a dedicated server never loads it.
 */
public final class ModNetwork {
    public static final String PROTOCOL_VERSION = "1";

    public record Serverbound<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                            BiConsumer<T, ServerPlayer> handler) {}

    public record Clientbound<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                            StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {}

    public static final List<Serverbound<?>> SERVERBOUND = List.of(
            new Serverbound<>(FireWeaponPayload.TYPE, FireWeaponPayload.STREAM_CODEC, ServerWeaponHandler::onFire),
            new Serverbound<>(WeaponActionPayload.TYPE, WeaponActionPayload.STREAM_CODEC, ServerWeaponHandler::onAction),
            new Serverbound<>(PingPayload.TO_SERVER, PingPayload.Pong.STREAM_CODEC, ServerWeaponHandler::onPong));

    public static final List<Clientbound<?>> CLIENTBOUND = List.of(
            new Clientbound<>(PingPayload.TO_CLIENT, PingPayload.STREAM_CODEC),
            new Clientbound<>(ShooterSeedPayload.TYPE, ShooterSeedPayload.STREAM_CODEC),
            new Clientbound<>(ShotEffectsPayload.TYPE, ShotEffectsPayload.STREAM_CODEC),
            new Clientbound<>(HitConfirmPayload.TYPE, HitConfirmPayload.STREAM_CODEC),
            new Clientbound<>(ReloadEventPayload.TYPE, ReloadEventPayload.STREAM_CODEC),
            new Clientbound<>(RemoteAimPayload.TYPE, RemoteAimPayload.STREAM_CODEC),
            new Clientbound<>(WeaponCorrectionPayload.TYPE, WeaponCorrectionPayload.STREAM_CODEC));

    private ModNetwork() {}
}
