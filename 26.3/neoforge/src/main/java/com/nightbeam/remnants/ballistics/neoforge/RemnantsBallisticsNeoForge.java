package com.nightbeam.remnants.ballistics.neoforge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.neoforge.gametest.NeoForgeGameTests;
import com.nightbeam.remnants.ballistics.neoforge.loot.NeoForgeLootModifiers;
import com.nightbeam.remnants.ballistics.registry.Registrar;
import com.nightbeam.remnants.ballistics.registry.RegistryHolder;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

/**
 * NeoForge entry point. Registers the common content, configs and payloads; gameplay listeners are in
 * {@link NeoForgeServerEvents}, client setup in {@code neoforge.client}.
 */
@Mod(RemnantsBallistics.MODID)
public final class RemnantsBallisticsNeoForge {
    public RemnantsBallisticsNeoForge(IEventBus modBus, ModContainer container) {
        Registrar.bootstrap();
        modBus.addListener(RemnantsBallisticsNeoForge::register);
        NeoForgeLootModifiers.SERIALIZERS.register(modBus);
        NeoForgeLootModifiers.CONDITIONS.register(modBus);
        NeoForgeGameTests.register(modBus);

        // SYNCED and LOCAL are the former SERVER and COMMON types; keep their file names so existing configs still load.
        container.registerConfig(ModConfig.Type.SYNCED, ServerConfig.SPEC, RemnantsBallistics.MODID + "-server.toml");
        container.registerConfig(ModConfig.Type.LOCAL, CommonConfig.SPEC, RemnantsBallistics.MODID + "-common.toml");

        modBus.addListener(RemnantsBallisticsNeoForge::registerPayloads);
        modBus.addListener((FMLCommonSetupEvent event) -> RemnantsBallistics.commonSetup());
    }

    /** NeoForge fires one RegisterEvent per registry, in its own dependency order. */
    private static void register(RegisterEvent event) {
        List<RegistryHolder<?>> entries = Registrar.entries().get(event.getRegistryKey());
        if (entries != null) entries.forEach(holder -> registerEntry(event, holder));
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerEntry(RegisterEvent event, RegistryHolder<T> holder) {
        event.register((ResourceKey<? extends Registry<T>>) holder.registry(), holder.id(), () -> {
            T value = holder.create();
            holder.bind(value);
            return value;
        });
    }

    /**
     * Server-bound handlers run on the server thread. Client-bound handlers are lambdas, so
     * {@link ClientPayloadHandler} is only class-loaded when a payload actually arrives on a client; a method reference
     * would resolve the class during registration, which breaks dedicated servers.
     */
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ModNetwork.PROTOCOL_VERSION);
        ModNetwork.SERVERBOUND.forEach(entry -> serverbound(registrar, entry));
        ModNetwork.CLIENTBOUND.forEach(entry -> clientbound(registrar, entry));
    }

    private static <T extends CustomPacketPayload> void serverbound(PayloadRegistrar registrar, ModNetwork.Serverbound<T> entry) {
        registrar.playToServer(entry.type(), entry.codec(), (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) entry.handler().accept(payload, player);
        });
    }

    private static <T extends CustomPacketPayload> void clientbound(PayloadRegistrar registrar, ModNetwork.Clientbound<T> entry) {
        registrar.playToClient(entry.type(), entry.codec(), (payload, context) -> ClientPayloadHandler.handle(payload));
    }
}
