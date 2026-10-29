package com.nightbeam.remnants.ballistics.fabric.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.fx.FxRenderer;
import com.nightbeam.remnants.ballistics.client.hud.WeaponHud;
import com.nightbeam.remnants.ballistics.client.network.ClientPayloadHandler;
import com.nightbeam.remnants.ballistics.client.particle.BallisticParticles;
import com.nightbeam.remnants.ballistics.client.render.GunItemModel;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.client.render.VoidOrbRenderer;
import com.nightbeam.remnants.ballistics.client.screen.AmmoPressScreen;
import com.nightbeam.remnants.ballistics.client.screen.GunPreviewRenderer;
import com.nightbeam.remnants.ballistics.client.screen.GunsmithScreen;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * Fabric client entry point. Fabric API callbacks cover registration, ticks, the HUD and world rendering; the hooks
 * Fabric API has no event for (hand rendering, FOV, mouse sensitivity, camera shake, arm poses, interaction blocking)
 * are mixins in {@code fabric.mixin.client}.
 */
public final class RemnantsBallisticsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.CLIENT, ClientConfig.SPEC);
        // Forge Config API Port provides NeoForge's config screen, listed in Mod Menu when it is installed.
        ConfigScreenFactoryRegistry.INSTANCE.register(RemnantsBallistics.MODID, ConfigurationScreen::new);

        ModNetwork.CLIENTBOUND.forEach(RemnantsBallisticsFabricClient::clientbound);
        KeyMapping.Category.register(Keybinds.CATEGORY.id());
        Keybinds.ALL.forEach(KeyMappingHelper::registerKeyMapping);

        ParticleProviderRegistry particles = ParticleProviderRegistry.getInstance();
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
        ItemModels.ID_MAPPER.put(GunItemModel.TYPE, GunItemModel.Unbaked.MAP_CODEC);
        ModelLoadingPlugin.register(FabricClientPlatform::registerStandaloneModels);
        PictureInPictureRendererRegistry.register(context -> new GunPreviewRenderer());

        ResourceLoader resources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        resources.registerReloadListener(RemnantsBallistics.id("gun_animations"), GunAnimations.INSTANCE);
        resources.registerReloadListener(RemnantsBallistics.id("gun_visuals"), GunVisualsLoader.INSTANCE);

        // The weapon HUD draws right after the crosshair (NeoForge: a GUI layer above CROSSHAIR) and replaces the vanilla
        // crosshair while it shows its own.
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, WeaponHud.ID, WeaponHud.INSTANCE::render);
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanilla -> (graphics, delta) -> {
            if (!ClientGunHandler.hidesVanillaCrosshair()) vanilla.extractRenderState(graphics, delta);
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientGunHandler.onClientTickPre());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientGunHandler.onClientTickPost();
            FxRenderer.tick();
        });
        // In the level's submit phase, like NeoForge's SubmitCustomGeometryEvent: the pose stack is camera-relative.
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            CameraRenderState camera = context.levelState().cameraRenderState;
            FxRenderer.render(camera.pos, camera.orientation, context.poseStack(), context.submitNodeCollector());
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> RemoteWeaponStates.forget(entity.getId()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientGunHandler.onLogout());
    }

    /** Fabric runs client receivers on the client thread. */
    private static <T extends CustomPacketPayload> void clientbound(ModNetwork.Clientbound<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type(), (payload, context) -> ClientPayloadHandler.handle(payload));
    }
}
