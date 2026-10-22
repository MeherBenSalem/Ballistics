package com.nightbeam.remnants.ballistics.neoforge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ServerHooks;
import com.nightbeam.remnants.ballistics.loot.LootInjections;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Game-bus listeners that route NeoForge events to the common server hooks. */
@EventBusSubscriber(modid = RemnantsBallistics.MODID)
public final class NeoForgeEvents {
    private NeoForgeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) ServerHooks.playerTick(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerHooks.playerLoggedOut(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ServerHooks.serverStopped();
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer) ServerHooks.startTracking(viewer, event.getTarget());
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        for (LootInjections.Injection injection : LootInjections.forTable(event.getName())) {
            event.getTable().addPool(LootInjections.pool(injection).build());
        }
    }
}
