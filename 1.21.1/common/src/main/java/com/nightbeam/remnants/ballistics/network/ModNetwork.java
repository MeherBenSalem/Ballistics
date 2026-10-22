package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Every payload, its codec and its handler, declared once; each loader registers these lists with its own networking
 * API. Handlers run on the receiving side's main thread.
 * <p>
 * Client-bound handlers are lambdas so {@link ClientPayloadHandler} is only class-loaded when a payload actually
 * arrives on a client; a method reference would resolve the class during registration, which breaks dedicated servers.
 */
public final class ModNetwork {
    public static final String PROTOCOL_VERSION = "1";

    /** A payload type with its codec and the handler run on the receiving side (the player is the sender or the local player). */
    public record Entry<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                       StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                       BiConsumer<T, Player> handler) {}

    public static final List<Entry<?>> SERVER_BOUND = List.of(
            new Entry<>(FireWeaponPayload.TYPE, FireWeaponPayload.STREAM_CODEC, ServerWeaponHandler::onFire),
            new Entry<>(WeaponActionPayload.TYPE, WeaponActionPayload.STREAM_CODEC, ServerWeaponHandler::onAction),
            new Entry<>(PingPayload.TO_SERVER, PingPayload.Pong.STREAM_CODEC, ServerWeaponHandler::onPong));

    public static final List<Entry<?>> CLIENT_BOUND = List.of(
            new Entry<>(PingPayload.TO_CLIENT, PingPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onPing(p)),
            new Entry<>(ShooterSeedPayload.TYPE, ShooterSeedPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onSeed(p)),
            new Entry<>(ShotEffectsPayload.TYPE, ShotEffectsPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onShotEffects(p)),
            new Entry<>(HitConfirmPayload.TYPE, HitConfirmPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onHitConfirm(p)),
            new Entry<>(ReloadEventPayload.TYPE, ReloadEventPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onReloadEvent(p)),
            new Entry<>(RemoteAimPayload.TYPE, RemoteAimPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onRemoteAim(p)),
            new Entry<>(WeaponCorrectionPayload.TYPE, WeaponCorrectionPayload.STREAM_CODEC, (p, player) -> ClientPayloadHandler.onCorrection(p)));

    private ModNetwork() {}
}
