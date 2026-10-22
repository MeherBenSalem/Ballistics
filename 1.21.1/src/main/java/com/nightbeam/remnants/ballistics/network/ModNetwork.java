package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Payload registration. Server-bound handlers run on the server thread. Client-bound handlers are wrapped in
 * lambdas so {@link ClientPayloadHandler} is only class-loaded when a payload actually arrives on a client;
 * a method reference would resolve the class during registration, which breaks dedicated servers.
 */
public final class ModNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(FireWeaponPayload.TYPE, FireWeaponPayload.STREAM_CODEC, ServerWeaponHandler::onFire);
        registrar.playToServer(WeaponActionPayload.TYPE, WeaponActionPayload.STREAM_CODEC, ServerWeaponHandler::onAction);
        registrar.playToServer(PingPayload.TO_SERVER, PingPayload.Pong.STREAM_CODEC, ServerWeaponHandler::onPong);

        registrar.playToClient(PingPayload.TO_CLIENT, PingPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onPing(p, c));
        registrar.playToClient(ShooterSeedPayload.TYPE, ShooterSeedPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onSeed(p, c));
        registrar.playToClient(ShotEffectsPayload.TYPE, ShotEffectsPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onShotEffects(p, c));
        registrar.playToClient(HitConfirmPayload.TYPE, HitConfirmPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onHitConfirm(p, c));
        registrar.playToClient(ReloadEventPayload.TYPE, ReloadEventPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onReloadEvent(p, c));
        registrar.playToClient(RemoteAimPayload.TYPE, RemoteAimPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onRemoteAim(p, c));
        registrar.playToClient(WeaponCorrectionPayload.TYPE, WeaponCorrectionPayload.STREAM_CODEC, (p, c) -> ClientPayloadHandler.onCorrection(p, c));
    }
}
