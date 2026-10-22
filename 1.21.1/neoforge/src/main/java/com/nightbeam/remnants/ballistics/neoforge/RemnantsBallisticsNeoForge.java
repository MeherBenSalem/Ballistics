package com.nightbeam.remnants.ballistics.neoforge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** NeoForge entry point: registries, configs and payloads; gameplay hooks are in {@link NeoForgeEvents}. */
@Mod(RemnantsBallistics.MODID)
public final class RemnantsBallisticsNeoForge {
    public RemnantsBallisticsNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgePlatform.modBus = modBus;
        RemnantsBallistics.init();
        NeoForgeConditions.CONDITIONS.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        modBus.addListener(RemnantsBallisticsNeoForge::registerPayloads);
        modBus.addListener((FMLCommonSetupEvent event) -> RemnantsBallistics.setup());
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ModNetwork.PROTOCOL_VERSION);
        for (ModNetwork.Entry<?> entry : ModNetwork.SERVER_BOUND) toServer(registrar, entry);
        for (ModNetwork.Entry<?> entry : ModNetwork.CLIENT_BOUND) toClient(registrar, entry);
    }

    private static <T extends CustomPacketPayload> void toServer(PayloadRegistrar registrar, ModNetwork.Entry<T> entry) {
        registrar.playToServer(entry.type(), entry.codec(), (payload, context) -> entry.handler().accept(payload, context.player()));
    }

    private static <T extends CustomPacketPayload> void toClient(PayloadRegistrar registrar, ModNetwork.Entry<T> entry) {
        registrar.playToClient(entry.type(), entry.codec(), (payload, context) -> entry.handler().accept(payload, context.player()));
    }
}
