package com.nightbeam.remnants.ballistics.fabric;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ServerHooks;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.loot.LootInjections;
import com.nightbeam.remnants.ballistics.recipe.ConfigConditions;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.fml.config.ModConfig;

/** Fabric entry point: registries, configs (through Forge Config API Port), payloads and server-side hooks. */
public final class RemnantsBallisticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RemnantsBallistics.init();
        ForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        ForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.COMMON, CommonConfig.SPEC);
        // {"condition": "remnants_ballistics:config", "option": "..."} in fabric:load_conditions
        ResourceConditions.register(ConfigConditions.ID, json -> ConfigConditions.test(GsonHelper.getAsString(json, "option")));
        FabricNetwork.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) ServerHooks.playerTick(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ServerHooks.playerLoggedOut(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerHooks.serverStopped());
        EntityTrackingEvents.START_TRACKING.register((target, viewer) -> ServerHooks.startTracking(viewer, target));
        // Weapons are fired through payloads; the attack key never melees with one (the client also blocks it).
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                player.getMainHandItem().getItem() instanceof GunItem ? InteractionResult.FAIL : InteractionResult.PASS);
        LootTableEvents.MODIFY.register((resources, loot, id, table, source) -> {
            for (LootInjections.Injection injection : LootInjections.forTable(id)) table.withPool(LootInjections.pool(injection));
        });

        RemnantsBallistics.setup();
    }
}
