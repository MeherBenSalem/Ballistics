package com.nightbeam.remnants.ballistics.forge.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.ClientRegistration;
import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.hud.WeaponHud;
import com.nightbeam.remnants.ballistics.client.render.VoidOrbRenderer;
import com.nightbeam.remnants.ballistics.client.screen.AmmoPressScreen;
import com.nightbeam.remnants.ballistics.client.screen.GunsmithScreen;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Mod-bus client registration. */
@Mod.EventBusSubscriber(modid = RemnantsBallistics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ForgeClientSetup {
    private ForgeClientSetup() {}

    @SubscribeEvent
    public static void onKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : Keybinds.ALL) {
            key.setKeyConflictContext(KeyConflictContext.IN_GAME);
            event.register(key);
        }
    }

    @SubscribeEvent
    public static void onGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), WeaponHud.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> WeaponHud.INSTANCE.render(graphics, partialTick));
    }

    @SubscribeEvent
    public static void onParticles(RegisterParticleProvidersEvent event) {
        for (ClientRegistration.Particle particle : ClientRegistration.PARTICLES) {
            event.registerSpriteSet(particle.type().get(), particle.provider()::apply);
        }
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VOID_ORB.get(), VoidOrbRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.GUNSMITH.get(), GunsmithScreen::new);
            MenuScreens.register(ModMenus.AMMO_PRESS.get(), AmmoPressScreen::new);
        });
    }

    @SubscribeEvent
    public static void onReloadListeners(RegisterClientReloadListenersEvent event) {
        for (ClientRegistration.ReloadListener listener : ClientRegistration.RELOAD_LISTENERS) event.registerReloadListener(listener.listener());
    }
}
