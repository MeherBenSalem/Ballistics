package com.nightbeam.remnants.ballistics.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.fx.FxRenderer;
import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.client.particle.BallisticParticles;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.client.render.VoidOrbRenderer;
import com.nightbeam.remnants.ballistics.client.screen.AmmoPressScreen;
import com.nightbeam.remnants.ballistics.client.screen.GunsmithScreen;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * Fabric client entry point. Fabric API callbacks cover registration, ticks and world rendering; the hooks Fabric API
 * has no event for (hand rendering, FOV, mouse sensitivity, camera shake, crosshair, arm poses, interaction blocking)
 * are mixins in {@code fabric.mixin.client}.
 */
public final class RemnantsBallisticsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.CLIENT, ClientConfig.SPEC);
        // Forge Config API Port provides NeoForge's config screen, listed in Mod Menu when it is installed.
        ConfigScreenFactoryRegistry.INSTANCE.register(RemnantsBallistics.MODID, ConfigurationScreen::new);

        ModNetwork.CLIENTBOUND.forEach(RemnantsBallisticsFabricClient::clientbound);
        Keybinds.ALL.forEach(KeyBindingHelper::registerKeyBinding);

        ParticleFactoryRegistry particles = ParticleFactoryRegistry.getInstance();
        particles.register(ModParticles.SPARK.get(), BallisticParticles.Spark.Provider::new);
        particles.register(ModParticles.MUZZLE_SMOKE.get(), BallisticParticles.Smoke.MuzzleProvider::new);
        particles.register(ModParticles.IMPACT_DUST.get(), BallisticParticles.Smoke.DustProvider::new);
        particles.register(ModParticles.CASING.get(), BallisticParticles.Casing.BrassProvider::new);
        particles.register(ModParticles.SHOTGUN_SHELL.get(), BallisticParticles.Casing.ShellProvider::new);
        particles.register(ModParticles.VOID_SPARK.get(), BallisticParticles.Energy.Provider::new);
        particles.register(ModParticles.RAIL_SPARK.get(), BallisticParticles.Energy.Provider::new);

        EntityRendererRegistry.register(ModEntities.VOID_ORB.get(), VoidOrbRenderer::new);
        MenuScreens.register(ModMenus.GUNSMITH.get(), GunsmithScreen::new);
        MenuScreens.register(ModMenus.AMMO_PRESS.get(), AmmoPressScreen::new);
        // Item models are builtin/entity; the renderer is created on first use, once the game has its render dispatchers.
        ModItems.guns().forEach(gun -> BuiltinItemRendererRegistry.INSTANCE.register(gun.get(),
                (stack, context, poseStack, buffers, light, overlay) -> GunItemRenderer.INSTANCE.renderByItem(stack, context, poseStack, buffers, light, overlay)));
        ModelLoadingPlugin.register(context -> context.addModels(GunModels.standaloneModels()));

        ResourceManagerHelper resources = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        resources.registerReloadListener(new IdentifiedReloadListener(RemnantsBallistics.id("gun_animations"), GunAnimations.INSTANCE));
        resources.registerReloadListener(new IdentifiedReloadListener(RemnantsBallistics.id("gun_visuals"), GunVisualsLoader.INSTANCE));

        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientGunHandler.onClientTickPre());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientGunHandler.onClientTickPost();
            FxRenderer.tick();
        });
        // After translucent terrain and particles; the camera rotation is already on the model-view stack, as with
        // NeoForge's AFTER_PARTICLES stage, so the pose stack starts empty.
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> FxRenderer.render(context.camera(), new PoseStack()));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> RemoteWeaponStates.forget(entity.getId()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientGunHandler.onLogout());
    }

    /** Fabric runs client receivers on the client thread. */
    private static <T extends CustomPacketPayload> void clientbound(ModNetwork.Clientbound<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) -> ClientPayloadHandler.handle(payload));
    }
}
