package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.hud.WeaponHud;
import com.nightbeam.remnants.ballistics.client.particle.BallisticParticles;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.client.render.VoidOrbRenderer;
import com.nightbeam.remnants.ballistics.client.screen.AmmoPressScreen;
import com.nightbeam.remnants.ballistics.client.screen.GunsmithScreen;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import com.nightbeam.remnants.ballistics.registry.RegistryHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Mod-bus client registration (all listeners here are mod-bus events). */
@EventBusSubscriber(modid = RemnantsBallistics.MODID, value = Dist.CLIENT)
public final class NeoForgeClientSetup {
    private NeoForgeClientSetup() {}

    @SubscribeEvent
    public static void onKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : Keybinds.ALL) {
            key.setKeyConflictContext(KeyConflictContext.IN_GAME);
            event.register(key);
        }
    }

    @SubscribeEvent
    public static void onGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, WeaponHud.ID, WeaponHud.INSTANCE);
    }

    @SubscribeEvent
    public static void onParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SPARK.get(), BallisticParticles.Spark.Provider::new);
        event.registerSpriteSet(ModParticles.MUZZLE_SMOKE.get(), BallisticParticles.Smoke.MuzzleProvider::new);
        event.registerSpriteSet(ModParticles.IMPACT_DUST.get(), BallisticParticles.Smoke.DustProvider::new);
        event.registerSpriteSet(ModParticles.CASING.get(), BallisticParticles.Casing.BrassProvider::new);
        event.registerSpriteSet(ModParticles.SHOTGUN_SHELL.get(), BallisticParticles.Casing.ShellProvider::new);
        event.registerSpriteSet(ModParticles.VOID_SPARK.get(), BallisticParticles.Energy.Provider::new);
        event.registerSpriteSet(ModParticles.RAIL_SPARK.get(), BallisticParticles.Energy.Provider::new);
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VOID_ORB.get(), VoidOrbRenderer::new);
    }

    @SubscribeEvent
    public static void onMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GUNSMITH.get(), GunsmithScreen::new);
        event.register(ModMenus.AMMO_PRESS.get(), AmmoPressScreen::new);
    }

    @SubscribeEvent
    public static void onClientExtensions(RegisterClientExtensionsEvent event) {
        Item[] guns = ModItems.guns().stream().map(RegistryHolder::get).toArray(Item[]::new);
        event.registerItem(GunClientExtensions.INSTANCE, guns);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        GunModels.standaloneModels().forEach(id -> event.register(ModelResourceLocation.standalone(id)));
    }

    @SubscribeEvent
    public static void onReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GunAnimations.INSTANCE);
        event.registerReloadListener(GunVisualsLoader.INSTANCE);
    }
}
