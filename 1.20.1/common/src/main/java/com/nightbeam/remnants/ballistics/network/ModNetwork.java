package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Every payload of the mod. Each loader registers these tables: server-bound handlers run on the server thread with the
 * sending player; client-bound payloads are handled by {@code client.network.ClientPayloadHandler}, which the loaders
 * only reference from client-side code so a dedicated server never loads it.
 */
public final class ModNetwork {
    public static final String PROTOCOL_VERSION = "1";

    public record Serverbound<T extends ModPayload>(ModPayload.Type<T> type, Function<FriendlyByteBuf, T> decoder,
                                                   BiConsumer<T, ServerPlayer> handler) {}

    public record Clientbound<T extends ModPayload>(ModPayload.Type<T> type, Function<FriendlyByteBuf, T> decoder) {}

    public static final List<Serverbound<?>> SERVERBOUND = List.of(
            new Serverbound<>(FireWeaponPayload.TYPE, FireWeaponPayload.DECODER, ServerWeaponHandler::onFire),
            new Serverbound<>(WeaponActionPayload.TYPE, WeaponActionPayload.DECODER, ServerWeaponHandler::onAction),
            new Serverbound<>(PingPayload.TO_SERVER, PingPayload.Pong.DECODER, ServerWeaponHandler::onPong));

    public static final List<Clientbound<?>> CLIENTBOUND = List.of(
            new Clientbound<>(PingPayload.TO_CLIENT, PingPayload.DECODER),
            new Clientbound<>(ShooterSeedPayload.TYPE, ShooterSeedPayload.DECODER),
            new Clientbound<>(ShotEffectsPayload.TYPE, ShotEffectsPayload.DECODER),
            new Clientbound<>(HitConfirmPayload.TYPE, HitConfirmPayload.DECODER),
            new Clientbound<>(ReloadEventPayload.TYPE, ReloadEventPayload.DECODER),
            new Clientbound<>(RemoteAimPayload.TYPE, RemoteAimPayload.DECODER),
            new Clientbound<>(WeaponCorrectionPayload.TYPE, WeaponCorrectionPayload.DECODER));

    private ModNetwork() {}
}
