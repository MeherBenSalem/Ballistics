package com.nightbeam.remnants.ballistics.neoforge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.LagCompensation;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Game-bus listeners for the authoritative weapon simulation. */
@EventBusSubscriber(modid = RemnantsBallistics.MODID)
public final class NeoForgeServerEvents {
    private NeoForgeServerEvents() {}

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

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer) ServerWeaponHandler.onStartTracking(viewer, event.getTarget());
    }
}
