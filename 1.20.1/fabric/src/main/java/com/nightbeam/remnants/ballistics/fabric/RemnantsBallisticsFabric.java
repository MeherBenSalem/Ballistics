package com.nightbeam.remnants.ballistics.fabric;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.LagCompensation;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.fabric.loot.FabricConfigCondition;
import com.nightbeam.remnants.ballistics.fabric.loot.FabricLootInjection;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.network.ModPayload;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Fabric entry point: registers the common content, configs (through Forge Config API Port), payloads, data hooks and
 * the server events Forge delivers through its event bus.
 */
public final class RemnantsBallisticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricRegistration.registerAll();

        ForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        ForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.COMMON, CommonConfig.SPEC);

        ModNetwork.SERVERBOUND.forEach(RemnantsBallisticsFabric::serverbound);

        FabricConfigCondition.register();
        FabricLootInjection.register();

        // Runs after every entity has ticked; Forge ticks each player in PlayerTickEvent (END phase).
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) ServerWeaponHandler.tick(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LagCompensation.forget(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LagCompensation.clear());
        EntityTrackingEvents.START_TRACKING.register((tracked, viewer) -> ServerWeaponHandler.onStartTracking(viewer, tracked));
        // Weapons never melee (Forge: GunItem#onLeftClickEntity).
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                player.getItemInHand(hand).getItem() instanceof GunItem && !player.isSpectator() ? InteractionResult.FAIL : InteractionResult.PASS);

        RemnantsBallistics.commonSetup();
    }

    /**
     * Server-bound payloads. Fabric calls the receiver on the network thread with a buffer it releases afterwards, so the
     * payload is decoded right away and handled on the server thread.
     */
    private static <T extends ModPayload> void serverbound(ModNetwork.Serverbound<T> entry) {
        ServerPlayNetworking.registerGlobalReceiver(entry.type().id(), (server, player, handler, buf, sender) -> {
            T payload = entry.decoder().apply(buf);
            server.execute(() -> entry.handler().accept(payload, player));
        });
    }
}
