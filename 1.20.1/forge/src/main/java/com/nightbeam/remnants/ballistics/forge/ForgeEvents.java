package com.nightbeam.remnants.ballistics.forge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ServerHooks;
import com.nightbeam.remnants.ballistics.loot.LootInjections;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Game-bus listeners that route Forge events to the common server hooks. */
@Mod.EventBusSubscriber(modid = RemnantsBallistics.MODID)
public final class ForgeEvents {
    private ForgeEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) ServerHooks.playerTick(player);
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
