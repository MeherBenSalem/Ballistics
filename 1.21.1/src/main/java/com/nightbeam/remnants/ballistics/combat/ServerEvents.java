package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.network.RemoteAimPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Game-bus listeners for the authoritative weapon simulation. */
@EventBusSubscriber(modid = RemnantsBallistics.MODID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) ServerWeaponHandler.tick(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LagCompensation.forget(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LagCompensation.clear();
    }

    /** New observers learn whether a player is currently aiming so third-person poses are correct immediately. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer target) {
            ShooterState st = ServerWeaponHandler.state(target);
            if (st.aiming || st.chargeStartTick >= 0) {
                PacketDistributor.sendToPlayer(viewer, new RemoteAimPayload(target.getId(), st.aiming, st.chargeStartTick >= 0));
            }
        }
    }

    /** Bullets do not grant post-hit invulnerability, so sustained fire and other damage keep landing. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (ModDamageTypes.isOurs(event.getSource()) && ServerConfig.get(ServerConfig.IGNORE_INVULNERABILITY_FRAMES)) {
            event.setInvulnerabilityTicks(0);
        }
    }
}
