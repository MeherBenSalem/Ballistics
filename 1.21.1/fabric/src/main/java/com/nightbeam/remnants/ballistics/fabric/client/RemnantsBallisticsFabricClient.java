package com.nightbeam.remnants.ballistics.fabric.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.ClientHooks;
import com.nightbeam.remnants.ballistics.client.ClientRegistration;
import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.hud.WeaponHud;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import com.nightbeam.remnants.ballistics.client.render.VoidOrbRenderer;
import com.nightbeam.remnants.ballistics.client.screen.AmmoPressScreen;
import com.nightbeam.remnants.ballistics.client.screen.GunsmithScreen;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.config.ModConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Fabric client entry point. Hooks Fabric API has no event for (the first-person hand, attack blocking, FOV, mouse
 * sensitivity, camera shake, the vanilla crosshair and third-person arm poses) are mixins in
 * {@code com.nightbeam.remnants.ballistics.fabric.mixin.client}, each calling the same common hook as NeoForge's events.
 */
public final class RemnantsBallisticsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.CLIENT, ClientConfig.SPEC);
        FabricClientNetwork.registerReceivers();

        for (KeyMapping key : Keybinds.ALL) KeyBindingHelper.registerKeyBinding(key);
        HudRenderCallback.EVENT.register(WeaponHud.INSTANCE::render);
        for (ClientRegistration.Particle particle : ClientRegistration.PARTICLES) {
            ParticleFactoryRegistry.getInstance().register(particle.type().get(), sprites -> particle.provider().apply(sprites));
        }
        EntityRendererRegistry.register(ModEntities.VOID_ORB.get(), VoidOrbRenderer::new);
        MenuScreens.register(ModMenus.GUNSMITH.get(), GunsmithScreen::new);
        MenuScreens.register(ModMenus.AMMO_PRESS.get(), AmmoPressScreen::new);
        for (RegistryObject<?, GunItem> gun : ModItems.guns()) {
            // The renderer is created lazily: it needs the block entity render dispatcher, which exists once the client has started.
            BuiltinItemRendererRegistry.INSTANCE.register(gun.get(),
                    (stack, context, poseStack, buffers, light, overlay) -> GunItemRenderer.INSTANCE.renderByItem(stack, context, poseStack, buffers, light, overlay));
        }
        for (ClientRegistration.ReloadListener listener : ClientRegistration.RELOAD_LISTENERS) {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new Identified(listener.id(), listener.listener()));
        }

        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientHooks.tickStart());
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientHooks.tickEnd());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> ClientHooks.renderLevelEffects(context.matrixStack(), context.camera()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientHooks.loggedOut());
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> ClientHooks.entityLeftLevel(entity));
    }

    /** Wraps a vanilla reload listener with the id Fabric's resource loader requires. */
    private record Identified(ResourceLocation id, PreparableReloadListener listener) implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return id;
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler,
                                              ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return listener.reload(barrier, manager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor);
        }

        @Override
        public String getName() {
            return listener.getName();
        }
    }
}
