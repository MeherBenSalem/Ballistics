package com.nightbeam.remnants.ballistics.fabric.client;

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
import com.nightbeam.remnants.ballistics.network.ModPayload;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
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
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Fabric client entry point. Fabric API callbacks cover registration, ticks and world rendering; the hooks Fabric API
 * has no event for (hand rendering, FOV, mouse sensitivity, camera shake, crosshair, arm poses, interaction blocking)
 * are mixins in {@code fabric.mixin.client}.
 */
public final class RemnantsBallisticsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 1.20.1 has no in-game config screen (neither Forge nor Forge Config API Port 8 provide one): the TOML files are
        // edited directly, and the server config syncs to clients.
        ForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.CLIENT, ClientConfig.SPEC);

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
        // After translucent terrain and particles; on 1.20.1 the world pose stack carries the camera rotation, as with
        // Forge's AFTER_PARTICLES stage.
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> FxRenderer.render(context.camera(), context.matrixStack()));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> RemoteWeaponStates.forget(entity.getId()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientGunHandler.onLogout());
    }

    /** Fabric calls client receivers on the network thread: decode there, handle on the client thread. */
    private static <T extends ModPayload> void clientbound(ModNetwork.Clientbound<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type().id(), (client, handler, buf, sender) -> {
            T payload = entry.decoder().apply(buf);
            client.execute(() -> ClientPayloadHandler.handle(payload));
        });
    }
}
