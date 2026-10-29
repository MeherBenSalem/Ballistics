package com.nightbeam.remnants.ballistics.fabric;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.LagCompensation;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.fabric.loot.FabricConfigCondition;
import com.nightbeam.remnants.ballistics.fabric.loot.FabricLootInjection;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.fml.config.ModConfig;

/**
 * Fabric entry point: registers the common content, configs (through Forge Config API Port), payloads, data hooks and
 * the server events NeoForge delivers through its event bus.
 */
public final class RemnantsBallisticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricRegistration.registerAll();

        ConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        ConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.COMMON, CommonConfig.SPEC);

        ModNetwork.SERVERBOUND.forEach(RemnantsBallisticsFabric::serverbound);
        ModNetwork.CLIENTBOUND.forEach(RemnantsBallisticsFabric::clientbound);

        FabricConfigCondition.register();
        FabricLootInjection.register();

        // Runs after every entity has ticked; NeoForge ticks each player in PlayerTickEvent.Post.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) ServerWeaponHandler.tick(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LagCompensation.forget(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LagCompensation.clear());
        EntityTrackingEvents.START_TRACKING.register((tracked, viewer) -> ServerWeaponHandler.onStartTracking(viewer, tracked));
        // Weapons never melee (NeoForge: GunItem#onLeftClickEntity).
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                player.getItemInHand(hand).getItem() instanceof GunItem && !player.isSpectator() ? InteractionResult.FAIL : InteractionResult.PASS);

        RemnantsBallistics.commonSetup();
    }

    /** Client-bound payload types (their receivers are registered by the client entry point). */
    private static <T extends CustomPacketPayload> void clientbound(ModNetwork.Clientbound<T> entry) {
        PayloadTypeRegistry.clientboundPlay().register(entry.type(), entry.codec());
    }

    /** Server-bound payloads; Fabric runs the receiver on the server thread. */
    private static <T extends CustomPacketPayload> void serverbound(ModNetwork.Serverbound<T> entry) {
        PayloadTypeRegistry.serverboundPlay().register(entry.type(), entry.codec());
        ServerPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) -> entry.handler().accept(payload, context.player()));
    }
}
