package com.nightbeam.remnants.ballistics.forge.client;

import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.hud.WeaponHud;
import com.nightbeam.remnants.ballistics.client.particle.BallisticParticles;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.client.render.VoidOrbRenderer;
import com.nightbeam.remnants.ballistics.client.screen.AmmoPressScreen;
import com.nightbeam.remnants.ballistics.client.screen.GunsmithScreen;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client entry point and mod-bus client registration. Only loaded on the physical client (the mod constructor checks the
 * dist before calling {@link #init}). Game-bus listeners are in {@link ForgeClientEvents}.
 */
public final class ForgeClientSetup {
    private ForgeClientSetup() {}

    @SuppressWarnings("removal") // see RemnantsBallisticsForge
    public static void init(IEventBus modBus) {
        // Forge 1.20.1 has no in-game config screen: the TOML files are edited directly.
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modBus.addListener(ForgeClientSetup::onClientSetup);
        modBus.addListener(ForgeClientSetup::onKeyMappings);
        modBus.addListener(ForgeClientSetup::onGuiOverlays);
        modBus.addListener(ForgeClientSetup::onParticles);
        modBus.addListener(ForgeClientSetup::onEntityRenderers);
        modBus.addListener(ForgeClientSetup::onRegisterModels);
        modBus.addListener(ForgeClientSetup::onReloadListeners);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.GUNSMITH.get(), GunsmithScreen::new);
            MenuScreens.register(ModMenus.AMMO_PRESS.get(), AmmoPressScreen::new);
        });
    }

    private static void onKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : Keybinds.ALL) {
            key.setKeyConflictContext(KeyConflictContext.IN_GAME);
            event.register(key);
        }
    }

    private static void onGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), WeaponHud.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> WeaponHud.INSTANCE.render(graphics, partialTick));
    }

    private static void onParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SPARK.get(), BallisticParticles.Spark.Provider::new);
        event.registerSpriteSet(ModParticles.MUZZLE_SMOKE.get(), BallisticParticles.Smoke.MuzzleProvider::new);
        event.registerSpriteSet(ModParticles.IMPACT_DUST.get(), BallisticParticles.Smoke.DustProvider::new);
        event.registerSpriteSet(ModParticles.CASING.get(), BallisticParticles.Casing.BrassProvider::new);
        event.registerSpriteSet(ModParticles.SHOTGUN_SHELL.get(), BallisticParticles.Casing.ShellProvider::new);
        event.registerSpriteSet(ModParticles.VOID_SPARK.get(), BallisticParticles.Energy.Provider::new);
        event.registerSpriteSet(ModParticles.RAIL_SPARK.get(), BallisticParticles.Energy.Provider::new);
    }

    private static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VOID_ORB.get(), VoidOrbRenderer::new);
    }

    private static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        GunModels.standaloneModels().forEach(event::register);
    }

    private static void onReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GunAnimations.INSTANCE);
        event.registerReloadListener(GunVisualsLoader.INSTANCE);
    }
}
