package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Every payload, its decoder and its handler, declared once; each loader registers these lists with its own networking
 * API (Forge: a SimpleChannel; Fabric: one channel per payload). Handlers run on the receiving side's main thread.
 * <p>
 * Client-bound handlers are lambdas so {@link ClientPayloadHandler} is only class-loaded when a payload actually
 * arrives on a client; a method reference would resolve the class during registration, which breaks dedicated servers.
 */
public final class ModNetwork {
    public static final String PROTOCOL_VERSION = "1";

    /**
     * A payload channel with its decoder and the handler run on the receiving side's main thread (the player is the
     * sender, or the local player on the client).
     */
    public record Entry<T extends Payload>(ResourceLocation channel, Class<T> type, Function<FriendlyByteBuf, T> decoder,
                                           BiConsumer<T, Player> handler) {}

    public static final List<Entry<?>> SERVER_BOUND = List.of(
            new Entry<>(FireWeaponPayload.TYPE, FireWeaponPayload.class, FireWeaponPayload.DECODER, ServerWeaponHandler::onFire),
            new Entry<>(WeaponActionPayload.TYPE, WeaponActionPayload.class, WeaponActionPayload.DECODER, ServerWeaponHandler::onAction),
            new Entry<>(PingPayload.TO_SERVER, PingPayload.Pong.class, PingPayload.Pong.DECODER, ServerWeaponHandler::onPong));

    public static final List<Entry<?>> CLIENT_BOUND = List.of(
            new Entry<>(PingPayload.TO_CLIENT, PingPayload.class, PingPayload.DECODER, (p, player) -> ClientPayloadHandler.onPing(p)),
            new Entry<>(ShooterSeedPayload.TYPE, ShooterSeedPayload.class, ShooterSeedPayload.DECODER, (p, player) -> ClientPayloadHandler.onSeed(p)),
            new Entry<>(ShotEffectsPayload.TYPE, ShotEffectsPayload.class, ShotEffectsPayload.DECODER, (p, player) -> ClientPayloadHandler.onShotEffects(p)),
            new Entry<>(HitConfirmPayload.TYPE, HitConfirmPayload.class, HitConfirmPayload.DECODER, (p, player) -> ClientPayloadHandler.onHitConfirm(p)),
            new Entry<>(ReloadEventPayload.TYPE, ReloadEventPayload.class, ReloadEventPayload.DECODER, (p, player) -> ClientPayloadHandler.onReloadEvent(p)),
            new Entry<>(RemoteAimPayload.TYPE, RemoteAimPayload.class, RemoteAimPayload.DECODER, (p, player) -> ClientPayloadHandler.onRemoteAim(p)),
            new Entry<>(WeaponCorrectionPayload.TYPE, WeaponCorrectionPayload.class, WeaponCorrectionPayload.DECODER, (p, player) -> ClientPayloadHandler.onCorrection(p)));

    private ModNetwork() {}
}
